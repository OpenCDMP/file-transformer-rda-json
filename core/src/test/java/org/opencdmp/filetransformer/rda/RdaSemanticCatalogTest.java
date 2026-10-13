package org.opencdmp.filetransformer.rda;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.opencdmp.commonmodels.enums.FieldType;
import org.opencdmp.commonmodels.models.descriptiotemplate.DescriptionTemplateModel;
import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.commonmodels.models.reference.ReferenceModel;
import org.opencdmp.commonmodels.models.referencetype.ReferenceTypeModel;
import org.opencdmp.filetransformer.rda.model.rda.Dataset;
import org.opencdmp.filetransformer.rda.model.rda.Distribution;
import org.opencdmp.filetransformer.rda.model.rda.Dmp;
import org.opencdmp.filetransformer.rda.model.rda.Funding;
import org.opencdmp.filetransformer.rda.model.rda.Host;
import org.opencdmp.filetransformer.rda.model.rda.PidSystem;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaSemanticCatalog;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaSemanticCatalog.Entry;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaSemanticCatalog.Format;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaSemanticCatalog.Kind;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaSemanticCatalog.Source;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaSemantics;
import org.opencdmp.filetransformer.rda.support.DefinitionBuilder;
import org.opencdmp.filetransformer.rda.support.RdaServiceFactory;
import org.opencdmp.filetransformer.rda.support.ValuesBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.opencdmp.filetransformer.rda.support.DefinitionBuilder.definition;
import static org.opencdmp.filetransformer.rda.support.PlanBuilder.plan;
import static org.opencdmp.filetransformer.rda.support.ReferenceBuilder.reference;
import static org.opencdmp.filetransformer.rda.support.ReferenceBuilder.referenceType;

/**
 * Keeps {@link RdaSemanticCatalog} truthful, since it is what the plugin will tell OpenCDMP about each key.
 * <p>
 * Source claims are checked by behaviour: answering a key, next to the other keys of its object, must change
 * the export when it is put where the catalogue says it is read, and must not change it anywhere else.
 */
class RdaSemanticCatalogTest {

    @Test
    void everyKeyHasExactlyOneEntry() throws IllegalAccessException {
        List<String> constants = new ArrayList<>();
        for (Field field : RdaSemantics.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType() == String.class) constants.add((String) field.get(null));
        }
        List<String> keys = RdaSemanticCatalog.ENTRIES.stream().map(Entry::key).toList();
        assertThat(keys).doesNotHaveDuplicates();
        assertThat(keys).containsExactlyInAnyOrderElementsOf(constants);
    }

    @Test
    void choicesMatchWhatTheExportAccepts() {
        Map<String, List<String>> accepted = Map.of(
                RdaSemantics.SEMANTIC_DATASET_PERSONAL_DATA, valuesOf(Dataset.PersonalData.values(), Dataset.PersonalData::value),
                RdaSemantics.SEMANTIC_DATASET_SENSITIVE_DATA, valuesOf(Dataset.SensitiveData.values(), Dataset.SensitiveData::value),
                RdaSemantics.SEMANTIC_ETHICAL_ISSUES_EXISTS, valuesOf(Dmp.EthicalIssuesExist.values(), Dmp.EthicalIssuesExist::value),
                RdaSemantics.SEMANTIC_DATASET_DISTRIBUTION_DATA_ACCESS, valuesOf(Distribution.DataAccess.values(), Distribution.DataAccess::value),
                RdaSemantics.SEMANTIC_PROJECT_FUNDING_FUNDING_STATUS, valuesOf(Funding.FundingStatus.values(), Funding.FundingStatus::value),
                RdaSemantics.SEMANTIC_DATASET_DISTRIBUTION_HOST_CERTIFIED_WITH, valuesOf(Host.CertifiedWith.values(), Host.CertifiedWith::value),
                RdaSemantics.SEMANTIC_DATASET_DISTRIBUTION_HOST_SUPPORT_VERSIONING, valuesOf(Host.SupportVersioning.values(), Host.SupportVersioning::value),
                RdaSemantics.SEMANTIC_DATASET_DISTRIBUTION_HOST_PID_SYSTEM, valuesOf(PidSystem.values(), PidSystem::value));
        accepted.forEach((key, expected) -> assertThat(RdaSemanticCatalog.find(key).values()).as(key).containsExactlyInAnyOrderElementsOf(expected));

        List<String> unchecked = RdaSemanticCatalog.ENTRIES.stream()
                .filter(e -> e.format() == Format.CHOICE && !accepted.containsKey(e.key()))
                .map(Entry::key).toList();
        assertThat(unchecked).as("choice keys the export does not validate itself (only the RDA schema does)")
                .containsExactly(RdaSemantics.SEMANTIC_DATASET_METADATA_STANDARD_ID_TYPE);
    }

    static Stream<Entry> valueEntries() {
        return RdaSemanticCatalog.ENTRIES.stream().filter(e -> e.kind() != Kind.OBJECT);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("valueEntries")
    void sourcesMatchWhatTheExportReads(Entry entry) {
        List<Entry> siblings = siblings(entry);
        for (Source source : List.of(Source.BLUEPRINT, Source.TEMPLATE)) {
            boolean changes = !exportAnswering(source, siblings, entry, false).equals(exportAnswering(source, siblings, entry, true));
            assertThat(changes).as("%s is read from %s answers", entry.key(), source).isEqualTo(entry.sources().contains(source));
        }
        String objectKey = wholeObjectKey(entry);
        if (objectKey == null) {
            assertThat(entry.sources()).as("%s has no reference input to come from", entry.key()).doesNotContain(Source.REFERENCE_TYPE);
            return;
        }
        boolean changes = !exportPicking(objectKey, siblings, entry, false).equals(exportPicking(objectKey, siblings, entry, true));
        assertThat(changes).as("%s is read from a picked %s reference", entry.key(), objectKey).isEqualTo(entry.sources().contains(Source.REFERENCE_TYPE));
    }

    /**
     * The key plus every other value key of the same top-level object (e.g. all of rda.dmp.contact.*), so the object
     * and its sub-objects are built whether or not the key is answered. Keys directly on the dmp or the dataset only
     * bring their direct siblings.
     * <p>
     * A plain identifier key (rda.dataset.dataset_id, rda.dmp.project.funding.funder_id, ...) and the
     * {@code .identifier}/{@code .type} pair of the same object are alternatives: answering both hides whichever
     * loses, so each is tested without the other.
     */
    private static List<Entry> siblings(Entry entry) {
        String[] parts = entry.object().split("\\.");
        String root = parts.length <= 2 ? entry.object() : String.join(".", Arrays.copyOf(parts, 3));
        boolean direct = parts.length <= 2;
        return RdaSemanticCatalog.ENTRIES.stream()
                .filter(e -> e.kind() != Kind.OBJECT)
                .filter(e -> direct ? e.object().equals(root) : (e.object().equals(root) || e.object().startsWith(root + ".")))
                .filter(e -> e == entry || !isPlainIdentifier(e))
                .filter(e -> e == entry || !isPlainIdentifier(entry) || !e.object().equals(entry.key()))
                .toList();
    }

    private static boolean isPlainIdentifier(Entry e) {
        return e.key().equals(e.object());
    }

    /** The key a reference input carries to fill this key's object, or an object above it, from a reference's fields. */
    private static String wholeObjectKey(Entry entry) {
        for (String object = entry.object(); object.contains("."); object = object.substring(0, object.lastIndexOf('.'))) {
            Entry candidate = RdaSemanticCatalog.find(object);
            if (candidate != null && candidate.kind() == Kind.OBJECT) return candidate.key();
        }
        return null;
    }

    private static String exportAnswering(Source source, List<Entry> siblings, Entry tested, boolean answerTested) {
        DefinitionBuilder questions = definition().section("probe").question("probe");
        for (Entry e : siblings) questions.input(inputId(e), FieldType.FREE_TEXT, e.key());
        ValuesBuilder answers = ValuesBuilder.values().answer("probe", a -> {
            for (Entry e : siblings) if (e != tested || answerTested) a.text(inputId(e), sample(e));
        });
        PlanModel plan = source == Source.BLUEPRINT
                ? plan("Catalogue probe").blueprint(questions.section(DefinitionBuilder.DESCRIPTIONS_SECTION).toBlueprint("Probe blueprint"), answers).build()
                : plan("Catalogue probe").description("Probe", questions.toTemplate("Probe template"), answers).build();
        return export(plan);
    }

    private static String exportPicking(String objectKey, List<Entry> siblings, Entry tested, boolean answerTested) {
        var type = referenceType("probe-type");
        for (Entry e : siblings) type.field(inputId(e), e.key());
        ReferenceTypeModel referenceType = type.build();
        List<String> fieldValues = new ArrayList<>();
        for (Entry e : siblings) {
            if (e == tested && !answerTested) continue;
            fieldValues.add(inputId(e));
            fieldValues.add(sample(e));
        }
        ReferenceModel picked = reference(referenceType, "probe-reference", "Picked reference", fieldValues.toArray(String[]::new));
        DescriptionTemplateModel template = definition().section("probe").question("probe")
                .reference("probe", referenceType, false, objectKey).toTemplate("Probe template");
        return export(plan("Catalogue probe").description("Probe", template, ValuesBuilder.values().answer("probe", a -> a.references("probe", picked))).build());
    }

    private static String export(PlanModel plan) {
        return new String(RdaServiceFactory.create().exportPlan(plan, null).getFile(), StandardCharsets.UTF_8);
    }

    private static String inputId(Entry e) {
        return e.key().replace('.', '-');
    }

    /** A value of the right form, so the export keeps it. */
    private static String sample(Entry e) {
        if (!e.values().isEmpty()) return e.values().getFirst();
        return switch (e.format()) {
            case DATE -> "2026-03-04";
            case DATE_TIME -> "2026-03-04T05:06:07Z";
            case URI -> "https://example.org/" + inputId(e);
            case EMAIL -> "someone@example.org";
            case INTEGER -> "42";
            case NUMBER -> "42.5";
            case BOOLEAN -> "true";
            case LANGUAGE_ISO_639_1 -> "pt";
            case LANGUAGE_ISO_639_3 -> "por";
            case COUNTRY_CODE -> "PT";
            case CURRENCY_CODE -> "EUR";
            case MIME_TYPE -> "text/csv";
            default -> "value of " + e.key();
        };
    }

    private static <E> List<String> valuesOf(E[] constants, Function<E, String> value) {
        return Arrays.stream(constants).map(value).toList();
    }
}
