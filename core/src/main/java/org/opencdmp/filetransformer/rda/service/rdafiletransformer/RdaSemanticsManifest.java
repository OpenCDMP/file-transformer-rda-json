package org.opencdmp.filetransformer.rda.service.rdafiletransformer;

import org.opencdmp.commonmodels.enums.PluginEntityType;
import org.opencdmp.commonmodels.enums.SemanticKeyLevel;
import org.opencdmp.commonmodels.enums.SemanticValueFormat;
import org.opencdmp.commonmodels.models.semantics.SemanticKeyUsage;
import org.opencdmp.commonmodels.models.semantics.SemanticObject;
import org.opencdmp.commonmodels.models.semantics.SemanticVocabulary;
import org.opencdmp.commonmodels.models.semantics.SemanticsManifest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaSemantics.*;

/**
 * The semantics manifest the RDA transformer publishes with its configuration, built from {@link RdaSemanticCatalog}:
 * one usage per exported value, with every key that fills it.
 * <p>
 * RDA requires more than these, but the export fills the rest from plan or asset data (title, dates, language,
 * contact). A value is "needs" when RDA requires it in an object every export has and the export can only write a
 * placeholder in its place ("unknown", or an internal id where a real identifier belongs), or when RDA requires it
 * inside an object and the export has nothing to put in its place: an object without it fails the schema. Editors
 * count a value inside an object only when the structure uses that object, so the second kind shows up only where
 * it matters. Everything else it writes is "can use"; keys it recognises but never writes, and keys read only from
 * reference type fields, are left out, since no template or blueprint input can supply them.
 */
public final class RdaSemanticsManifest {

    public static final String PREFIX = "rda";

    private static final Set<String> NEEDS = Set.of(
            SEMANTIC_DATASET_DATASET_ID_ID,
            SEMANTIC_DATASET_PERSONAL_DATA,
            SEMANTIC_DATASET_SENSITIVE_DATA,
            SEMANTIC_ETHICAL_ISSUES_EXISTS,
            SEMANTIC_CONTACT_ID_IDENTIFIER
    );

    /**
     * Needed values a template can also give but a blueprint normally does: "needs" for blueprints, "can use" for
     * templates, so a template does not show the plan's gap.
     */
    private static final Set<String> NEEDS_ON_BLUEPRINTS = Set.of(SEMANTIC_ETHICAL_ISSUES_EXISTS);

    /** Objects every export has, mapped or not: the contact (plan contact or owner) and the identifiers it always writes. */
    public static final Set<String> ALWAYS_PRESENT = Set.of("rda.dmp.dmp_id", "rda.dmp.contact", "rda.dmp.contact.contact_id", "rda.dataset.dataset_id");

    private static final SemanticsManifest MANIFEST = build();

    private RdaSemanticsManifest() {
    }

    public static SemanticsManifest get() {
        return MANIFEST;
    }

    private static SemanticsManifest build() {
        Map<String, List<RdaSemanticCatalog.Entry>> byPath = new LinkedHashMap<>();
        for (RdaSemanticCatalog.Entry entry : RdaSemanticCatalog.ENTRIES) {
            if (entry.level() == RdaSemanticCatalog.Level.NOT_EXPORTED) continue;
            if (entityTypes(entry).isEmpty()) continue;
            byPath.computeIfAbsent(entry.path(), x -> new ArrayList<>()).add(entry);
        }

        List<SemanticKeyUsage> usages = new ArrayList<>();
        for (Map.Entry<String, List<RdaSemanticCatalog.Entry>> group : byPath.entrySet()) {
            List<RdaSemanticCatalog.Entry> entries = group.getValue().stream().sorted(Comparator.comparing(x -> x.key().length())).toList();
            boolean needs = entries.stream().anyMatch(x -> NEEDS.contains(x.key())) || requiredInObject(entries);
            List<PluginEntityType> entityTypes = entries.stream().flatMap(x -> entityTypes(x).stream()).distinct().sorted().toList();
            boolean onBlueprintsOnly = needs && entityTypes.size() > 1 && entries.stream().anyMatch(x -> NEEDS_ON_BLUEPRINTS.contains(x.key()));
            if (onBlueprintsOnly) {
                usages.add(usage(entries, group.getKey(), SemanticKeyLevel.Needs, List.of(PluginEntityType.Plan)));
                usages.add(usage(entries, group.getKey(), SemanticKeyLevel.CanUse, entityTypes.stream().filter(x -> x != PluginEntityType.Plan).toList()));
            } else {
                usages.add(usage(entries, group.getKey(), needs ? SemanticKeyLevel.Needs : SemanticKeyLevel.CanUse, entityTypes));
            }
        }
        SemanticsManifest manifest = new SemanticsManifest(List.of(new SemanticVocabulary(PREFIX, "RDA maDMP", true)), usages);
        manifest.setObjects(objects(usages));
        return manifest;
    }

    private static SemanticKeyUsage usage(List<RdaSemanticCatalog.Entry> entries, String path, SemanticKeyLevel level, List<PluginEntityType> entityTypes) {
        RdaSemanticCatalog.Entry first = entries.getFirst();
        SemanticKeyUsage usage = new SemanticKeyUsage(
                entries.stream().map(RdaSemanticCatalog.Entry::key).toList(),
                false,
                level,
                entityTypes,
                first.label(),
                purpose(entries),
                path);
        usage.setFormat(format(first.format()));
        usage.setValues(first.values().isEmpty() ? null : first.values());
        usage.setMultiple(first.kind() == RdaSemanticCatalog.Kind.LIST);
        usage.setFallback(fallback(entries));
        usage.setObject(isObject(first.object()) ? first.object() : null);
        usage.setFromReferenceFields(entries.stream().anyMatch(x -> x.sources().contains(RdaSemanticCatalog.Source.REFERENCE_TYPE)));
        usage.setImported(entries.stream().anyMatch(RdaSemanticCatalog.Entry::imported));
        return usage;
    }

    /** RDA requires the value inside an object and the export has nothing to put in its place. */
    private static boolean requiredInObject(List<RdaSemanticCatalog.Entry> entries) {
        return isObject(entries.getFirst().object()) && fallback(entries) == null
                && entries.stream().anyMatch(x -> x.level() == RdaSemanticCatalog.Level.REQUIRED);
    }

    private static String fallback(List<RdaSemanticCatalog.Entry> entries) {
        return entries.stream().map(RdaSemanticCatalog.Entry::fallback).filter(x -> x != null && !x.isBlank()).findFirst().orElse(null);
    }

    /** The plan and the dataset are what a blueprint and a template describe, not objects inside them. */
    private static boolean isObject(String key) {
        return key != null && !key.equals("rda.dmp") && !key.equals("rda.dataset");
    }

    /**
     * Every object the published values belong to, with the objects around them (a host sits in a distribution),
     * each saying whether the export holds a list of them and whether a picked reference can fill it whole.
     */
    private static List<SemanticObject> objects(List<SemanticKeyUsage> usages) {
        Map<String, String> targets = new LinkedHashMap<>();
        for (SemanticKeyUsage usage : usages) {
            if (usage.getObject() == null) continue;
            // a whole-object key lands where the object does; a value in it lands one name further down
            String target = isWholeObject(usage.getObject()) && usage.getKeys().contains(usage.getObject())
                    ? usage.getTarget()
                    : usage.getTarget().substring(0, usage.getTarget().lastIndexOf('.'));
            for (String object = usage.getObject(); isObject(object); object = object.substring(0, object.lastIndexOf('.'))) {
                targets.putIfAbsent(object, target);
                if (!target.contains(".")) break;
                target = target.substring(0, target.lastIndexOf('.'));
            }
        }
        List<SemanticObject> objects = new ArrayList<>();
        for (Map.Entry<String, String> entry : targets.entrySet()) {
            String key = entry.getKey();
            boolean pickable = isWholeObject(key);
            List<SemanticKeyUsage> inside = usages.stream().filter(x -> x.getObject() != null && (x.getObject().equals(key) || x.getObject().startsWith(key + "."))).toList();
            String parent = key.substring(0, key.lastIndexOf('.'));
            SemanticObject object = new SemanticObject();
            object.setKey(key);
            object.setLabel(label(key, inside));
            object.setParent(targets.containsKey(parent) ? parent : null);
            object.setMultiple(entry.getValue().endsWith("[]"));
            object.setFromReference(pickable);
            object.setAlwaysPresent(ALWAYS_PRESENT.contains(key));
            object.setEntityTypes(inside.stream().flatMap(x -> x.getEntityTypes().stream()).distinct().sorted().toList());
            object.setTarget(entry.getValue());
            objects.add(object);
        }
        return objects;
    }

    /**
     * The label of the key naming the object (a whole-object or plain identifier key) when there is one, otherwise
     * the label of a value directly in it with the value's own name taken off.
     */
    private static String label(String key, List<SemanticKeyUsage> inside) {
        String named = inside.stream().filter(x -> x.getKeys().contains(key)).map(SemanticKeyUsage::getLabel).findFirst().orElse(null);
        if (named != null) return named;
        String label = inside.stream().filter(x -> key.equals(x.getObject())).map(SemanticKeyUsage::getLabel).findFirst().orElse(inside.getFirst().getLabel());
        return label.contains(" › ") ? label.substring(0, label.lastIndexOf(" › ")) : label;
    }

    /** A key a reference input carries to fill the whole object from the reference's own fields. */
    private static boolean isWholeObject(String key) {
        RdaSemanticCatalog.Entry entry = RdaSemanticCatalog.find(key);
        return entry != null && entry.kind() == RdaSemanticCatalog.Kind.OBJECT;
    }

    private static SemanticValueFormat format(RdaSemanticCatalog.Format format) {
        return switch (format) {
            case TEXT -> SemanticValueFormat.Text;
            case DATE -> SemanticValueFormat.Date;
            case DATE_TIME -> SemanticValueFormat.DateTime;
            case URI -> SemanticValueFormat.Uri;
            case EMAIL -> SemanticValueFormat.Email;
            case INTEGER -> SemanticValueFormat.Integer;
            case NUMBER -> SemanticValueFormat.Number;
            case BOOLEAN -> SemanticValueFormat.Boolean;
            case CHOICE -> SemanticValueFormat.Choice;
            case LANGUAGE_ISO_639_1 -> SemanticValueFormat.LanguageTwoLetter;
            case LANGUAGE_ISO_639_3 -> SemanticValueFormat.LanguageThreeLetter;
            case COUNTRY_CODE -> SemanticValueFormat.CountryCode;
            case CURRENCY_CODE -> SemanticValueFormat.CurrencyCode;
            case MIME_TYPE -> SemanticValueFormat.MimeType;
            case REFERENCE -> SemanticValueFormat.Reference;
        };
    }

    private static List<PluginEntityType> entityTypes(RdaSemanticCatalog.Entry entry) {
        List<PluginEntityType> types = new ArrayList<>();
        if (entry.sources().contains(RdaSemanticCatalog.Source.BLUEPRINT)) types.add(PluginEntityType.Plan);
        if (entry.sources().contains(RdaSemanticCatalog.Source.TEMPLATE)) types.add(PluginEntityType.Description);
        return types;
    }

    private static String purpose(List<RdaSemanticCatalog.Entry> entries) {
        String fallback = fallback(entries);
        boolean required = entries.stream().anyMatch(x -> x.level() == RdaSemanticCatalog.Level.REQUIRED || x.level() == RdaSemanticCatalog.Level.REQUIRED_WITH_FALLBACK);
        if (fallback != null) return (required ? "Required by RDA; unanswered, the export writes: " : "Unanswered, the export writes: ") + (fallback.endsWith(".") ? fallback : fallback + ".");
        if (required) return "Required by RDA within its object: an exported object without it fails the RDA schema.";
        return "Written to the RDA export when answered.";
    }
}
