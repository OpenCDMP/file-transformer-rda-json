package org.opencdmp.filetransformer.rda.support;

import org.opencdmp.commonmodels.enums.FieldType;
import org.opencdmp.commonmodels.models.descriptiotemplate.DescriptionTemplateModel;
import org.opencdmp.commonmodels.models.planblueprint.PlanBlueprintModel;
import org.opencdmp.commonmodels.models.referencetype.ReferenceTypeModel;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaSemanticCatalog;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaSemanticCatalog.Entry;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaSemanticCatalog.Format;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaSemanticCatalog.Kind;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaSemanticCatalog.Level;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaSemanticCatalog.Source;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.opencdmp.filetransformer.rda.support.DefinitionBuilder.definition;
import static org.opencdmp.filetransformer.rda.support.ReferenceBuilder.referenceType;

/**
 * A blueprint and a template that carry every key the catalogue says is read from them, laid out the way a careful
 * admin would today: one question per RDA object (repeating for lists), every key of the object and its
 * sub-objects on an input of the type its format calls for.
 */
public final class CatalogueDefinitions {

    /** Top-level objects that hold one value, not a list. */
    private static final Set<String> SINGLE_OBJECTS = Set.of("rda.dmp.contact", "rda.dataset.dataset_id");

    private CatalogueDefinitions() {
    }

    public static PlanBlueprintModel blueprint() {
        return build(Source.BLUEPRINT).section(DefinitionBuilder.DESCRIPTIONS_SECTION).toBlueprint("Catalogue blueprint");
    }

    public static DescriptionTemplateModel template() {
        return build(Source.TEMPLATE).toTemplate("Catalogue template");
    }

    private static DefinitionBuilder build(Source source) {
        Map<String, List<Entry>> questions = new LinkedHashMap<>();
        for (Entry entry : RdaSemanticCatalog.ENTRIES) {
            // import-only keys (distribution.exists) belong on the template too, since import writes them
            boolean read = entry.sources().contains(source)
                    || (entry.level() == Level.NOT_EXPORTED && entry.imported() && source == Source.TEMPLATE);
            if (!read) continue;
            questions.computeIfAbsent(topObject(entry), k -> new ArrayList<>()).add(entry);
        }
        DefinitionBuilder definition = definition().section(source == Source.BLUEPRINT ? "plan" : "dataset");
        questions.forEach((top, entries) -> {
            String id = top.replace('.', '-');
            boolean direct = top.equals(entries.getFirst().key()) && entries.size() == 1 && !isObjectRoot(top);
            if (direct || SINGLE_OBJECTS.contains(top)) definition.question(id);
            else definition.repeatingQuestion(id);
            for (Entry entry : entries) addInput(definition, entry);
        });
        return definition;
    }

    /** The object directly below rda.dmp or rda.dataset that the key belongs to, or the key itself for direct properties. */
    private static String topObject(Entry entry) {
        String[] parts = entry.key().split("\\.");
        String top = parts.length > 2 ? parts[0] + "." + parts[1] + "." + parts[2] : entry.key();
        return isObjectRoot(top) ? top : entry.key();
    }

    private static boolean isObjectRoot(String key) {
        return RdaSemanticCatalog.ENTRIES.stream().anyMatch(e -> e.object().equals(key) || e.object().startsWith(key + "."))
                && !key.equals("rda.dmp") && !key.equals("rda.dataset");
    }

    private static void addInput(DefinitionBuilder definition, Entry entry) {
        String id = entry.key().replace('.', '-');
        if (entry.kind() == Kind.OBJECT || entry.key().equals(entry.object())) {
            definition.reference(id, referenceTypeFor(entry), entry.kind() == Kind.OBJECT, entry.key());
            return;
        }
        if (entry.format() == Format.CHOICE) {
            List<String> options = new ArrayList<>();
            for (String value : entry.values()) {
                options.add(value);
                options.add(value);
            }
            definition.select(id, options, entry.key());
            return;
        }
        FieldType type = switch (entry.format()) {
            case DATE -> FieldType.DATE_PICKER;
            case BOOLEAN -> FieldType.BOOLEAN_DECISION;
            case INTEGER, NUMBER -> FieldType.NUMBER;
            default -> entry.kind() == Kind.LIST ? FieldType.TAGS : FieldType.FREE_TEXT;
        };
        definition.input(id, type, entry.key());
    }

    /**
     * A reference type whose fields carry the value keys of the object and its sub-objects (a distribution type
     * also fills the host and licences), as admins configure re3data-like types.
     */
    private static ReferenceTypeModel referenceTypeFor(Entry entry) {
        var type = referenceType(entry.key().replace("rda.", "").replace('.', '-'));
        for (Entry child : RdaSemanticCatalog.ENTRIES) {
            if (child.kind() == Kind.OBJECT || child.key().equals(entry.key())) continue;
            boolean inside = child.object().equals(entry.key()) || child.object().startsWith(entry.key() + ".");
            if (inside && child.sources().contains(Source.REFERENCE_TYPE)) {
                type.field(child.key().substring(entry.key().length() + 1).replace('.', '-'), child.key());
            }
        }
        return type.build();
    }
}
