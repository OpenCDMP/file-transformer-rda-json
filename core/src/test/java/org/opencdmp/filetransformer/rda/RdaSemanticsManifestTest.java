package org.opencdmp.filetransformer.rda;

import org.junit.jupiter.api.Test;
import org.opencdmp.commonmodels.enums.PluginEntityType;
import org.opencdmp.commonmodels.enums.SemanticKeyLevel;
import org.opencdmp.commonmodels.enums.SemanticValueFormat;
import org.opencdmp.commonmodels.models.semantics.SemanticKeyUsage;
import org.opencdmp.commonmodels.models.semantics.SemanticObject;
import org.opencdmp.commonmodels.models.semantics.SemanticsManifest;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaSemanticCatalog;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaSemantics;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaSemanticsManifest;
import org.opencdmp.commonmodels.enums.FieldType;
import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.filetransformer.rda.support.DefinitionBuilder;
import org.opencdmp.filetransformer.rda.support.PlanBuilder;
import org.opencdmp.filetransformer.rda.support.RdaServiceFactory;
import org.opencdmp.filetransformer.rda.support.ValuesBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class RdaSemanticsManifestTest {

    private final SemanticsManifest manifest = RdaSemanticsManifest.get();

    @Test
    void everyExportedKeyAnInputCanCarryAppearsOnce() {
        List<String> expected = RdaSemanticCatalog.ENTRIES.stream()
                .filter(x -> x.level() != RdaSemanticCatalog.Level.NOT_EXPORTED)
                .filter(x -> x.sources().contains(RdaSemanticCatalog.Source.BLUEPRINT) || x.sources().contains(RdaSemanticCatalog.Source.TEMPLATE))
                .map(RdaSemanticCatalog.Entry::key)
                .toList();
        List<String> published = this.manifest.getKeys().stream().flatMap(x -> x.getKeys().stream()).distinct().toList();

        assertThat(published).containsExactlyInAnyOrderElementsOf(expected);
        // a key may have one usage per kind of structure, never two for the same one
        for (PluginEntityType entityType : PluginEntityType.values()) {
            List<String> forType = this.manifest.getKeys().stream().filter(x -> x.getEntityTypes().contains(entityType)).flatMap(x -> x.getKeys().stream()).toList();
            assertThat(forType).as("%s", entityType).doesNotHaveDuplicates();
        }
    }

    @Test
    void keysFillingTheSameValueShareOneUsage() {
        SemanticKeyUsage datasetId = this.usageOf(RdaSemantics.SEMANTIC_DATASET_DATASET_ID_ID);

        assertThat(datasetId.getKeys()).contains(RdaSemantics.SEMANTIC_DATASET_DATASET_ID);
        assertThat(datasetId.getLevel()).isEqualTo(SemanticKeyLevel.Needs);
    }

    @Test
    void placeholderFilledValuesAndValuesAnObjectCannotLackAreNeeded() {
        List<SemanticKeyUsage> needed = this.manifest.getKeys().stream().filter(x -> x.getLevel() == SemanticKeyLevel.Needs).toList();
        List<String> placeholders = List.of(
                "dmp.dataset[].dataset_id.identifier",
                "dmp.dataset[].personal_data",
                "dmp.dataset[].sensitive_data",
                "dmp.ethical_issues_exist",
                "dmp.contact.contact_id[].identifier");

        assertThat(needed.stream().map(SemanticKeyUsage::getTarget)).containsAll(placeholders);
        // the rest are required inside an object, with nothing the export could put in their place
        assertThat(needed.stream().filter(x -> !placeholders.contains(x.getTarget()))).allSatisfy(x -> {
            assertThat(x.getObject()).as("%s", x.getKeys()).isNotNull();
            assertThat(x.getFallback()).as("%s", x.getKeys()).isNull();
        });
        assertThat(needed.stream().map(SemanticKeyUsage::getTarget)).contains(
                "dmp.dataset[].metadata[].language",
                "dmp.dataset[].related_identifier[].type",
                "dmp.dataset[].distribution[].data_access",
                "dmp.dataset[].distribution[].host.title");

        assertThat(this.usageOf(RdaSemantics.SEMANTIC_DMP_TITLE).getLevel()).isEqualTo(SemanticKeyLevel.CanUse);
        // filled from the host, or inferred from the identifier: not needed
        assertThat(this.usageOf(RdaSemantics.SEMANTIC_DATASET_DISTRIBUTION_TITLE).getLevel()).isEqualTo(SemanticKeyLevel.CanUse);
        assertThat(this.usageOf(RdaSemantics.SEMANTIC_DATASET_CREATOR_ID_TYPE).getLevel()).isEqualTo(SemanticKeyLevel.CanUse);
        // optional since RDA 1.3
        assertThat(this.usageOf(RdaSemantics.SEMANTIC_DATASET_DISTRIBUTION_HOST_URL).getLevel()).isEqualTo(SemanticKeyLevel.CanUse);
    }

    @Test
    void ethicalIssuesAreNeededFromBlueprintsOnly() {
        List<SemanticKeyUsage> usages = this.manifest.getKeys().stream().filter(x -> x.getKeys().contains(RdaSemantics.SEMANTIC_ETHICAL_ISSUES_EXISTS)).toList();

        assertThat(usages).hasSize(2);
        assertThat(usages).anySatisfy(x -> {
            assertThat(x.getEntityTypes()).containsExactly(PluginEntityType.Plan);
            assertThat(x.getLevel()).isEqualTo(SemanticKeyLevel.Needs);
        });
        assertThat(usages).anySatisfy(x -> {
            assertThat(x.getEntityTypes()).containsExactly(PluginEntityType.Description);
            assertThat(x.getLevel()).isEqualTo(SemanticKeyLevel.CanUse);
        });
    }

    @Test
    void everyUsageSaysWhereAndWhyAndIsReadFromAnInput() {
        assertThat(this.manifest.getVocabularies()).singleElement().satisfies(x -> {
            assertThat(x.getPrefix()).isEqualTo(RdaSemanticsManifest.PREFIX);
            assertThat(x.isStandard()).isTrue();
        });
        assertThat(this.manifest.getKeys()).allSatisfy(x -> {
            assertThat(x.getKeys()).isNotEmpty().allMatch(k -> k.startsWith(RdaSemanticsManifest.PREFIX + "."));
            assertThat(x.getEntityTypes()).isNotEmpty();
            assertThat(x.getLabel()).isNotBlank();
            assertThat(x.getPurpose()).isNotBlank();
            assertThat(x.getTarget()).isNotBlank();
        });
    }

    @Test
    void valuesSayWhatFormTheExportKeeps() {
        assertThat(this.usageOf(RdaSemantics.SEMANTIC_DATASET_DISTRIBUTION_DATA_ACCESS)).satisfies(x -> {
            assertThat(x.getFormat()).isEqualTo(SemanticValueFormat.Choice);
            assertThat(x.getValues()).containsExactlyInAnyOrder("open", "restricted", "closed");
        });
        assertThat(this.usageOf(RdaSemantics.SEMANTIC_DATASET_DISTRIBUTION_BYTE_SIZE).getFormat()).isEqualTo(SemanticValueFormat.Integer);
        assertThat(this.usageOf(RdaSemantics.SEMANTIC_DMP_CREATED).getFormat()).isEqualTo(SemanticValueFormat.DateTime);
        assertThat(this.usageOf(RdaSemantics.SEMANTIC_DMP_LANGUAGE).getFormat()).isEqualTo(SemanticValueFormat.LanguageTwoLetter);
        assertThat(this.usageOf(RdaSemantics.SEMANTIC_DATASET_KEYWORD).isMultiple()).isTrue();
        assertThat(this.usageOf(RdaSemantics.SEMANTIC_DATASET_TITLE).isMultiple()).isFalse();
        assertThat(this.manifest.getKeys()).allSatisfy(x -> assertThat(x.getFormat()).as("%s", x.getKeys()).isNotNull());
    }

    @Test
    void fallbacksAndReferenceFieldsComeFromTheCatalogue() {
        assertThat(this.usageOf(RdaSemantics.SEMANTIC_DATASET_TITLE).getFallback()).isEqualTo("The asset's title.");
        assertThat(this.usageOf(RdaSemantics.SEMANTIC_DATASET_DATASET_ID_ID).getFallback()).isNotBlank();
        assertThat(this.usageOf(RdaSemantics.SEMANTIC_DATASET_DISTRIBUTION_TITLE).getFallback()).isEqualTo("The host's title.");
        assertThat(this.usageOf(RdaSemantics.SEMANTIC_DATASET_DISTRIBUTION_HOST_URL).getFallback()).isNull();
        assertThat(this.usageOf(RdaSemantics.SEMANTIC_DATASET_DISTRIBUTION_HOST_TITLE).isFromReferenceFields()).isTrue();
        assertThat(this.usageOf(RdaSemantics.SEMANTIC_DATASET_TITLE).isFromReferenceFields()).isFalse();
    }

    @Test
    void objectsFormATreeTheValuesBelongTo() {
        Map<String, SemanticObject> objects = this.manifest.getObjects().stream().collect(Collectors.toMap(SemanticObject::getKey, x -> x));

        assertThat(this.manifest.getKeys()).allSatisfy(x -> {
            if (x.getObject() != null) assertThat(objects).as("object of %s", x.getKeys()).containsKey(x.getObject());
        });
        assertThat(objects.values()).allSatisfy(x -> {
            if (x.getParent() != null) assertThat(objects).as("parent of %s", x.getKey()).containsKey(x.getParent());
            assertThat(x.getLabel()).isNotBlank();
            assertThat(x.getEntityTypes()).isNotEmpty();
        });

        SemanticObject distribution = objects.get("rda.dataset.distribution");
        assertThat(distribution.getParent()).isNull();
        assertThat(distribution.isMultiple()).isTrue();
        assertThat(distribution.isFromReference()).isTrue();
        assertThat(distribution.getTarget()).isEqualTo("dmp.dataset[].distribution[]");

        SemanticObject host = objects.get("rda.dataset.distribution.host");
        assertThat(host.getParent()).isEqualTo("rda.dataset.distribution");
        assertThat(host.isMultiple()).isFalse();
        assertThat(host.isFromReference()).isTrue();
        assertThat(host.getTarget()).isEqualTo("dmp.dataset[].distribution[].host");

        assertThat(objects.get("rda.dataset.distribution.license").isMultiple()).isTrue();
        assertThat(objects.get("rda.dmp.contact").isMultiple()).isFalse();
        assertThat(objects.get("rda.dmp.contact").isFromReference()).isFalse();
        assertThat(objects).doesNotContainKeys("rda.dmp", "rda.dataset");
    }

    @Test
    void alwaysPresentObjectsAreInEveryExport() throws Exception {
        List<String> always = this.manifest.getObjects().stream().filter(SemanticObject::isAlwaysPresent).map(SemanticObject::getKey).toList();
        assertThat(always).containsExactlyInAnyOrderElementsOf(RdaSemanticsManifest.ALWAYS_PRESENT);

        // a plan with no semantics at all still exports each of them
        PlanModel plan = PlanBuilder.plan("No semantics")
                .description("Asset", DefinitionBuilder.definition().section("s").question("q").input("q", FieldType.FREE_TEXT).toTemplate("Plain"), ValuesBuilder.values())
                .build();
        JsonNode dmp = JsonMapper.builder().build().readTree(RdaServiceFactory.create().exportPlan(plan, null).getFile()).path("dmp");
        assertThat(dmp.path("dmp_id").path("identifier").isString()).isTrue();
        assertThat(dmp.path("contact").isObject()).isTrue();
        assertThat(dmp.path("contact").path("contact_id").path(0).path("identifier").isString()).isTrue();
        assertThat(dmp.path("dataset").path(0).path("dataset_id").path("identifier").isString()).isTrue();
        assertThat(this.manifest.getObjects().stream().filter(x -> x.getKey().equals("rda.dataset.distribution")).findFirst().orElseThrow().isAlwaysPresent()).isFalse();
    }

    private SemanticKeyUsage usageOf(String key) {
        return this.manifest.getKeys().stream().filter(x -> x.getKeys().contains(key)).findFirst().orElseThrow();
    }
}
