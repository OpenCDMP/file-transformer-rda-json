package org.opencdmp.filetransformer.rda.support;

import org.opencdmp.commonmodels.enums.FieldType;
import org.opencdmp.commonmodels.models.descriptiotemplate.DefinitionModel;
import org.opencdmp.commonmodels.models.descriptiotemplate.DescriptionTemplateModel;
import org.opencdmp.commonmodels.models.planblueprint.PlanBlueprintModel;
import org.opencdmp.commonmodels.models.planblueprint.RootSectionModel;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.FieldModel;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.FieldSetModel;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.MultiplicityModel;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.SectionModel;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.fielddata.BaseFieldDataModel;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.fielddata.LabelDataModel;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.fielddata.RadioBoxDataModel;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.fielddata.ReferenceTypeDataModel;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.fielddata.SelectDataModel;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.fielddata.SemanticValueModel;
import org.opencdmp.commonmodels.models.referencetype.ReferenceTypeModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluent builder for the section > fieldSet > field structure shared by blueprints and description templates.
 * Ids are the readable names given here; values are attached to them with {@link ValuesBuilder}.
 */
public final class DefinitionBuilder {

    private final List<RootSectionModel> sections = new ArrayList<>();
    private RootSectionModel section;
    private FieldSetModel fieldSet;

    public static DefinitionBuilder definition() {
        return new DefinitionBuilder();
    }

    public DefinitionBuilder section(String id) {
        this.section = new RootSectionModel();
        this.section.setId(id);
        this.section.setOrdinal(this.sections.size());
        this.section.setTitle(id);
        this.section.setSections(new ArrayList<>());
        this.section.setFieldSets(new ArrayList<>());
        this.section.setHasTemplates(false);
        this.sections.add(this.section);
        this.fieldSet = null;
        return this;
    }

    /** What the current section describes, e.g. {@code rda.dataset.distribution}: its questions make up one such object. */
    public DefinitionBuilder sectionDescribes(String... objects) {
        if (this.section == null) throw new IllegalStateException("Call section() first");
        this.section.setSemantics(new ArrayList<>(List.of(objects)));
        return this;
    }

    /** What the current question describes, e.g. {@code rda.dataset.distribution.license}: each answer is one such object. */
    public DefinitionBuilder questionDescribes(String... objects) {
        if (this.fieldSet == null) throw new IllegalStateException("Call question() first");
        this.fieldSet.setSemantics(new ArrayList<>(List.of(objects)));
        return this;
    }

    /** An answer option with the key/value pairs it stands for ("key", "value", ...). */
    public static RadioBoxDataModel.RadioBoxOptionModel option(String value, String label, String... keyValuePairs) {
        RadioBoxDataModel.RadioBoxOptionModel option = new RadioBoxDataModel.RadioBoxOptionModel();
        option.setValue(value);
        option.setLabel(label);
        List<SemanticValueModel> semantics = new ArrayList<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) semantics.add(new SemanticValueModel(keyValuePairs[i], keyValuePairs[i + 1]));
        option.setSemantics(semantics);
        return option;
    }

    /** An answer option meaning there is nothing to export for the question's object. */
    public static RadioBoxDataModel.RadioBoxOptionModel optionExportingNothing(String value, String label) {
        RadioBoxDataModel.RadioBoxOptionModel option = option(value, label);
        option.setExportsNothing(true);
        return option;
    }

    /** A single choice whose options may stand for semantic key values (see {@link #option}). */
    public DefinitionBuilder choice(String id, List<RadioBoxDataModel.RadioBoxOptionModel> options, String... semantics) {
        RadioBoxDataModel data = new RadioBoxDataModel();
        data.setOptions(new ArrayList<>(options));
        return this.add(id, FieldType.RADIO_BOX, data, semantics);
    }

    /** A question answered once. */
    public DefinitionBuilder question(String id) {
        return this.fieldSet(id, false);
    }

    /** A question the author can answer more than once (fieldSet multiplicity). */
    public DefinitionBuilder repeatingQuestion(String id) {
        return this.fieldSet(id, true);
    }

    private DefinitionBuilder fieldSet(String id, boolean repeating) {
        if (this.section == null) throw new IllegalStateException("Call section() before question()");
        this.fieldSet = new FieldSetModel();
        this.fieldSet.setId(id);
        this.fieldSet.setOrdinal(this.section.getFieldSets().size());
        this.fieldSet.setTitle(id);
        this.fieldSet.setHasMultiplicity(repeating);
        if (repeating) {
            MultiplicityModel multiplicity = new MultiplicityModel();
            multiplicity.setMin(0);
            multiplicity.setMax(10);
            this.fieldSet.setMultiplicity(multiplicity);
        }
        this.fieldSet.setFields(new ArrayList<>());
        this.section.getFieldSets().add(this.fieldSet);
        return this;
    }

    public DefinitionBuilder input(String id, FieldType type, String... semantics) {
        if (type == FieldType.RADIO_BOX || type == FieldType.SELECT || type == FieldType.REFERENCE_TYPES) {
            throw new IllegalArgumentException(type + " needs its options or reference type: use radio(), select() or reference()");
        }
        LabelDataModel data = new LabelDataModel();
        return this.add(id, type, data, semantics);
    }

    /** Options are given as value/label pairs: "yes", "Yes", "no", "No". */
    public DefinitionBuilder radio(String id, List<String> valueLabelPairs, String... semantics) {
        RadioBoxDataModel data = new RadioBoxDataModel();
        List<RadioBoxDataModel.RadioBoxOptionModel> options = new ArrayList<>();
        for (int i = 0; i < valueLabelPairs.size(); i += 2) {
            RadioBoxDataModel.RadioBoxOptionModel option = new RadioBoxDataModel.RadioBoxOptionModel();
            option.setValue(valueLabelPairs.get(i));
            option.setLabel(valueLabelPairs.get(i + 1));
            options.add(option);
        }
        data.setOptions(options);
        return this.add(id, FieldType.RADIO_BOX, data, semantics);
    }

    /** Options are given as value/label pairs, like {@link #radio}. */
    public DefinitionBuilder select(String id, List<String> valueLabelPairs, String... semantics) {
        SelectDataModel data = new SelectDataModel();
        List<SelectDataModel.OptionModel> options = new ArrayList<>();
        for (int i = 0; i < valueLabelPairs.size(); i += 2) {
            SelectDataModel.OptionModel option = new SelectDataModel.OptionModel();
            option.setValue(valueLabelPairs.get(i));
            option.setLabel(valueLabelPairs.get(i + 1));
            options.add(option);
        }
        data.setOptions(options);
        return this.add(id, FieldType.SELECT, data, semantics);
    }

    public DefinitionBuilder reference(String id, ReferenceTypeModel referenceType, boolean multipleSelect, String... semantics) {
        ReferenceTypeDataModel data = new ReferenceTypeDataModel();
        data.setReferenceType(referenceType);
        data.setMultipleSelect(multipleSelect);
        return this.add(id, FieldType.REFERENCE_TYPES, data, semantics);
    }

    private DefinitionBuilder add(String id, FieldType type, BaseFieldDataModel data, String... semantics) {
        if (this.fieldSet == null) throw new IllegalStateException("Call question() before adding inputs");
        data.setLabel(id);
        data.setFieldType(type);
        FieldModel field = new FieldModel();
        field.setId(id);
        field.setOrdinal(this.fieldSet.getFields().size());
        field.setSemantics(new ArrayList<>(List.of(semantics)));
        field.setIncludeInExport(true);
        field.setData(data);
        this.fieldSet.getFields().add(field);
        return this;
    }

    public DescriptionTemplateModel toTemplate(String label) {
        DefinitionModel definition = new DefinitionModel();
        definition.setSections(new ArrayList<>(this.sections));
        DescriptionTemplateModel template = new DescriptionTemplateModel();
        template.setId(Ids.of("template:" + label));
        template.setGroupId(Ids.of("template-group:" + label));
        template.setLabel(label);
        template.setVersion((short) 1);
        template.setLanguage("en");
        template.setDefinition(definition);
        return template;
    }

    public PlanBlueprintModel toBlueprint(String label) {
        org.opencdmp.commonmodels.models.planblueprint.DefinitionModel definition = new org.opencdmp.commonmodels.models.planblueprint.DefinitionModel();
        definition.setSections(new ArrayList<>(this.sections));
        PlanBlueprintModel blueprint = new PlanBlueprintModel();
        blueprint.setId(Ids.of("blueprint:" + label));
        blueprint.setGroupId(Ids.of("blueprint-group:" + label));
        blueprint.setLabel(label);
        blueprint.setDefinition(definition);
        return blueprint;
    }

    /** The id of the blueprint section that holds descriptions, when the blueprint has none of its own. */
    public static final String DESCRIPTIONS_SECTION = "descriptions";
}
