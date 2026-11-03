package org.opencdmp.filetransformer.rda.service.rdafiletransformer;

import gr.cite.tools.logging.LoggerService;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.FieldModel;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.FieldSetModel;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.fielddata.RadioBoxDataModel;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.fielddata.ReferenceTypeDataModel;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.fielddata.SelectDataModel;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.fielddata.SemanticValueModel;
import org.opencdmp.commonmodels.models.plandescriptionfields.PropertyDefinitionFieldSetItemModel;
import org.opencdmp.commonmodels.models.reference.ReferenceModel;
import org.opencdmp.commonmodels.models.referencetype.ReferenceTypeModel;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * The answers that together describe one RDA object, and the values they give for each semantic key.
 * <p>
 * A group has one member (an answer to a question) unless a section describes the object: then the answers of every
 * question in the section are members of one group. {@link RdaMapperSupport#groups} decides the grouping; mappers
 * only ask for values by key. A value comes from the first member with an answered input carrying the key, else from
 * the key/value pairs of a chosen answer option.
 */
public final class AnswerGroup {
    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(RdaFileTransformerService.class));

    /** One answer to one question. */
    public record Member(FieldSetModel question, PropertyDefinitionFieldSetItemModel answer) {
    }

    /** A chosen option of a select or radio input, with what it stands for. */
    private record ChosenOption(String value, List<SemanticValueModel> semantics, boolean exportsNothing) {
    }

    private final List<Member> members;

    public AnswerGroup(FieldSetModel question, PropertyDefinitionFieldSetItemModel answer) {
        this(List.of(new Member(question, answer)));
    }

    public AnswerGroup(List<Member> members) {
        this.members = List.copyOf(members);
    }

    public List<Member> members() {
        return this.members;
    }

    /**
     * The group split per answer of the questions that describe {@code object} (e.g. one part per licence answer in a
     * distribution section), or the whole group as its only part when no question of it describes that object.
     */
    public List<AnswerGroup> parts(String object) {
        List<AnswerGroup> parts = new ArrayList<>();
        for (Member member : this.members) {
            if (member.question().getSemantics() != null && member.question().getSemantics().contains(object)) parts.add(new AnswerGroup(List.of(member)));
        }
        return parts.isEmpty() ? List.of(this) : parts;
    }

    /**
     * As {@link #parts}, with an answer that picks references split further: one part per object its picks make, each
     * keeping the answer's other values. Inputs filling the same keys of the object are alternatives, so each of their
     * picks is an object of its own (every organisation or researcher picked for an affiliation); inputs filling other
     * keys complete it, one pick of each (a funder and a grant make one funding).
     */
    public List<AnswerGroup> partsPerReference(String object, String... nested) {
        List<AnswerGroup> parts = new ArrayList<>();
        for (AnswerGroup part : this.parts(object)) {
            List<AnswerGroup> picks = new ArrayList<>();
            for (Member member : part.members) {
                for (Map<String, ReferenceModel> combination : combinations(member, object, nested)) picks.add(part.withOnly(member, combination, object, nested));
            }
            if (picks.isEmpty()) parts.add(part);
            else parts.addAll(picks);
        }
        return parts;
    }

    /** The objects the member's picks make, each as the one reference it takes from each input that fills the object. */
    private static List<Map<String, ReferenceModel>> combinations(Member member, String object, String... nested) {
        List<Set<String>> keys = new ArrayList<>();
        List<List<Map.Entry<String, ReferenceModel>>> alternatives = new ArrayList<>();
        for (FieldModel field : fields(member)) {
            if (!fills(field, object, nested) || member.answer() == null || member.answer().getFields() == null) continue;
            org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel stored = member.answer().getFields().get(field.getId());
            if (stored == null || stored.getReferences() == null) continue;
            List<Map.Entry<String, ReferenceModel>> picks = stored.getReferences().stream().filter(Objects::nonNull).map(x -> Map.entry(field.getId(), x)).toList();
            if (picks.isEmpty()) continue;
            Set<String> fieldKeys = new HashSet<>(keysUnder(field, object, nested));
            List<Map.Entry<String, ReferenceModel>> merged = new ArrayList<>(picks);
            for (int i = keys.size() - 1; i >= 0; i--) {
                if (Collections.disjoint(keys.get(i), fieldKeys)) continue;
                fieldKeys.addAll(keys.remove(i));
                merged.addAll(0, alternatives.remove(i));
            }
            keys.add(fieldKeys);
            alternatives.add(merged);
        }
        if (alternatives.isEmpty()) return List.of();
        List<Map<String, ReferenceModel>> combinations = new ArrayList<>(List.of(Map.of()));
        for (List<Map.Entry<String, ReferenceModel>> alternative : alternatives) {
            List<Map<String, ReferenceModel>> next = new ArrayList<>();
            for (Map<String, ReferenceModel> combination : combinations) {
                for (Map.Entry<String, ReferenceModel> pick : alternative) {
                    Map<String, ReferenceModel> more = new LinkedHashMap<>(combination);
                    more.put(pick.getKey(), pick.getValue());
                    next.add(more);
                }
            }
            combinations = next;
        }
        return combinations;
    }

    /** The keys under the object an input fills: its own, and those its reference type gives the picked references. */
    private static Set<String> keysUnder(FieldModel field, String object, String... nested) {
        Set<String> keys = new HashSet<>();
        if (field.getSemantics() != null) field.getSemantics().stream().filter(x -> RdaMapperSupport.under(x, object, nested)).forEach(keys::add);
        if (field.getData() instanceof ReferenceTypeDataModel data) keys.addAll(RdaMapperSupport.referenceTypeKeysUnder(data.getReferenceType(), object, nested));
        return keys;
    }

    /** Does an answer of the group pick references in an input that fills the object (not one of its nested objects)? */
    public boolean picksFor(String object, String... nested) {
        for (AnswerGroup part : this.parts(object)) {
            for (Member member : part.members) {
                for (FieldModel field : fields(member)) {
                    if (!fills(field, object, nested) || member.answer() == null || member.answer().getFields() == null) continue;
                    org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel stored = member.answer().getFields().get(field.getId());
                    if (stored != null && stored.getReferences() != null && stored.getReferences().stream().anyMatch(java.util.Objects::nonNull)) return true;
                }
            }
        }
        return false;
    }

    /**
     * A reference input that gives the object values: by a key of its own under it, or through its reference type; not
     * one standing for the whole object (its exporter reads it) nor one filling only a nested object that is split on its own.
     */
    private static boolean fills(FieldModel field, String object, String... nested) {
        if (!(field.getData() instanceof ReferenceTypeDataModel data)) return false;
        if (field.getSemantics() != null && field.getSemantics().stream().anyMatch(x -> RdaMapperSupport.under(x, object, nested))) return true;
        return RdaMapperSupport.referenceTypeCarriesUnder(data.getReferenceType(), object, nested);
    }

    /** This group with the member's inputs that fill the object answering only the references of the combination. */
    private AnswerGroup withOnly(Member picked, Map<String, ReferenceModel> combination, String object, String... nested) {
        List<Member> members = new ArrayList<>();
        for (Member member : this.members) {
            if (member != picked) {
                members.add(member);
                continue;
            }
            PropertyDefinitionFieldSetItemModel answer = new PropertyDefinitionFieldSetItemModel();
            answer.setOrdinal(member.answer().getOrdinal());
            java.util.Map<String, org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel> fields = new java.util.LinkedHashMap<>(member.answer().getFields());
            for (FieldModel field : fields(member)) {
                if (!fills(field, object, nested) || !fields.containsKey(field.getId())) continue;
                org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel only = new org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel();
                only.setId(fields.get(field.getId()).getId());
                only.setReferences(combination.containsKey(field.getId()) ? List.of(combination.get(field.getId())) : List.of());
                fields.put(field.getId(), only);
            }
            answer.setFields(fields);
            members.add(new Member(member.question(), answer));
        }
        return new AnswerGroup(members);
    }

    /** True when a chosen answer option says there is nothing to export for the object. */
    public boolean exportsNothing() {
        for (Member member : this.members) {
            for (FieldModel field : fields(member)) {
                if (chosenOptions(member, field).stream().anyMatch(ChosenOption::exportsNothing)) return true;
            }
        }
        return false;
    }

    /** The first input of the question that carries the key. */
    private static FieldModel inputDefinition(Member member, String semantic) {
        return fields(member).stream().filter(x -> carries(x, semantic)).findFirst().orElse(null);
    }

    /**
     * An input carries a key it has itself, or that its reference type gives to the picked references' id, name,
     * description or a field of theirs. Not through the type when the input stands for an object around the key
     * (rda.dataset.distribution): its exporter reads the picked references itself.
     */
    private static boolean carries(FieldModel field, String semantic) {
        if (field.getSemantics() != null && field.getSemantics().contains(semantic)) return true;
        if (field.getSemantics() != null && field.getSemantics().stream().anyMatch(x -> semantic.startsWith(x + "."))) return false;
        return field.getData() instanceof ReferenceTypeDataModel data && RdaMapperSupport.referenceTypeCarries(data.getReferenceType(), semantic);
    }

    private static List<FieldModel> fields(Member member) {
        if (member.question() == null || member.question().getFields() == null) return List.of();
        List<FieldModel> fieldModels = member.question().getAllField();
        return fieldModels == null ? List.of() : fieldModels;
    }

    /** The stored answer of the first input carrying the key, or null. */
    public org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel input(String semantic) {
        for (Member member : this.members) {
            FieldModel field = inputDefinition(member, semantic);
            if (field == null) continue;
            org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel stored = member.answer().getFields().getOrDefault(field.getId(), null);
            if (stored != null) return stored;
        }
        return null;
    }

    /** The first value the key has in this group, or null. */
    public String value(String semantic) {
        List<String> values = this.values(semantic);
        return values != null && !values.isEmpty() ? values.getFirst(): null;
    }

    /**
     * A Yes/No answer in the words the key expects: "yes"/"no" for keys that take yes, no or unknown
     * (personal_data, ethical_issues_exist, ...), "true"/"false" otherwise (is_reused).
     */
    private static String booleanValue(String semantic, boolean value) {
        RdaSemanticCatalog.Entry entry = RdaSemanticCatalog.find(semantic);
        if (entry != null && entry.values().contains("yes") && entry.values().contains("no")) return value ? "yes" : "no";
        return Boolean.toString(value);
    }

    /**
     * Every value the key has in this group, as export strings, or null when there is none: from the first answered
     * input carrying the key, else from the chosen answer options standing for the key.
     */
    public List<String> values(String semantic) {
        for (Member member : this.members) {
            for (FieldModel field : fields(member)) {
                if (!carries(field, semantic)) continue;
                List<String> values = valuesOf(member, field, semantic);
                if (!values.isEmpty()) return values;
            }
        }
        List<String> fromOptions = new ArrayList<>();
        for (Member member : this.members) {
            for (FieldModel field : fields(member)) {
                for (ChosenOption option : chosenOptions(member, field)) {
                    if (option.semantics() == null) continue;
                    option.semantics().stream().filter(x -> semantic.equals(x.getKey()) && x.getValue() != null && !x.getValue().isBlank()).forEach(x -> fromOptions.add(x.getValue()));
                }
            }
        }
        return !fromOptions.isEmpty() ? fromOptions : null;
    }

    /** True when an input or a chosen option of the group could give the key a value. */
    public boolean asksFor(String semantic) {
        return this.members.stream().anyMatch(member -> inputDefinition(member, semantic) != null
                || fields(member).stream().anyMatch(field -> options(field).stream().anyMatch(option -> option.semantics() != null && option.semantics().stream().anyMatch(x -> semantic.equals(x.getKey())))));
    }

    /** True when an input or an answer option of the group could give one of the keys a value. */
    public boolean asksForAnyOf(List<String> semantics) {
        return semantics.stream().anyMatch(this::asksFor);
    }

    /** The options of a select or radio input, whether chosen or not. */
    private static List<ChosenOption> options(FieldModel field) {
        List<ChosenOption> options = new ArrayList<>();
        if (field.getData() instanceof SelectDataModel select && select.getOptions() != null) {
            select.getOptions().forEach(x -> options.add(new ChosenOption(x.getValue(), x.getSemantics(), x.isExportsNothing())));
        } else if (field.getData() instanceof RadioBoxDataModel radio && radio.getOptions() != null) {
            radio.getOptions().forEach(x -> options.add(new ChosenOption(x.getValue(), x.getSemantics(), x.isExportsNothing())));
        }
        return options;
    }

    /** The options of a select or radio input the answer chose, matched on value or label like the value lookup. */
    private static List<ChosenOption> chosenOptions(Member member, FieldModel field) {
        org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel stored = member.answer().getFields() == null ? null : member.answer().getFields().getOrDefault(field.getId(), null);
        if (stored == null) return List.of();
        List<ChosenOption> chosen = new ArrayList<>();
        if (field.getData() instanceof SelectDataModel select && select.getOptions() != null && stored.getTextListValue() != null) {
            for (SelectDataModel.OptionModel option : select.getOptions()) {
                if (stored.getTextListValue().contains(option.getValue()) || stored.getTextListValue().contains(option.getLabel())) chosen.add(new ChosenOption(option.getValue(), option.getSemantics(), option.isExportsNothing()));
            }
        } else if (field.getData() instanceof RadioBoxDataModel radio && radio.getOptions() != null) {
            for (RadioBoxDataModel.RadioBoxOptionModel option : radio.getOptions()) {
                boolean inList = stored.getTextListValue() != null && (stored.getTextListValue().contains(option.getValue()) || stored.getTextListValue().contains(option.getLabel()));
                boolean asText = stored.getTextValue() != null && (stored.getTextValue().equalsIgnoreCase(option.getValue()) || stored.getTextValue().equalsIgnoreCase(option.getLabel()));
                if (inList || asText) chosen.add(new ChosenOption(option.getValue(), option.getSemantics(), option.isExportsNothing()));
            }
        }
        return chosen;
    }

    /** The values one member's input gives the key, as export strings. */
    private static List<String> valuesOf(Member member, FieldModel field, String semantic) {
        List<String> values = new ArrayList<>();
        org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel valueField = member.answer().getFields().getOrDefault(field.getId(), null);
        if (valueField != null) {
            switch (field.getData().getFieldType()) {
                case FREE_TEXT, TEXT_AREA, RICH_TEXT_AREA -> {
                    if (valueField.getTextValue() != null && !valueField.getTextValue().isBlank()) values.add(valueField.getTextValue());
                }
                case BOOLEAN_DECISION, CHECK_BOX -> {
                    if (valueField.getBooleanValue() != null) values.add(booleanValue(semantic, valueField.getBooleanValue()));
                }
                case NUMBER -> {
                    if (valueField.getNumberValue() != null) values.add(String.valueOf(valueField.getNumberValue()));
                }
                case DATE_PICKER -> {
                    try {
                        if (valueField.getDateValue() != null) values.add((DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC).format(valueField.getDateValue())));
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
                case DATASET_IDENTIFIER, VALIDATION -> {
                    if (valueField.getExternalIdentifier() != null && valueField.getExternalIdentifier().getIdentifier() != null && !valueField.getExternalIdentifier().getIdentifier().isBlank()) {
                        values.add(valueField.getExternalIdentifier().getIdentifier());
                    }
                }
                case TAGS -> {
                    if (valueField.getTextListValue() != null) {
                        valueField.getTextListValue().stream().filter(tag -> tag != null && !tag.isBlank()).forEach(values::add);
                    }
                }
                case SELECT, RADIO_BOX -> chosenOptions(member, field).forEach(option -> values.add(option.value()));
                case REFERENCE_TYPES -> {
                    if (valueField.getReferences() != null && !valueField.getReferences().isEmpty()) {
                        boolean ownKey = field.getSemantics() != null && field.getSemantics().contains(semantic);
                        ReferenceTypeModel type = ((ReferenceTypeDataModel) field.getData()).getReferenceType();
                        for (ReferenceModel referenceModel : valueField.getReferences()) {
                            if (referenceModel == null) continue;
                            if (!ownKey) {
                                String value = RdaMapperSupport.referenceValue(referenceModel, type == null ? null : type.getDefinition(), semantic);
                                if (value != null) values.add(value);
                            } else if (referenceModel.getReference() != null && !referenceModel.getReference().isBlank()) {
                                values.add(referenceModel.getReference());
                            }
                        }
                    }
                }
            }
        }
        return values;
    }
}
