package org.opencdmp.filetransformer.rda.support;

import org.opencdmp.commonmodels.enums.ReferenceTypeFieldDataType;
import org.opencdmp.commonmodels.models.reference.ReferenceDefinitionModel;
import org.opencdmp.commonmodels.models.reference.ReferenceFieldModel;
import org.opencdmp.commonmodels.models.reference.ReferenceModel;
import org.opencdmp.commonmodels.models.referencetype.ReferenceTypeDefinitionModel;
import org.opencdmp.commonmodels.models.referencetype.ReferenceTypeFieldModel;
import org.opencdmp.commonmodels.models.referencetype.ReferenceTypeModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Reference types (with semantics on their own fields, as admins configure them) and references of those types,
 * e.g. a re3data repository whose "name" field carries rda.dataset.distribution.host.title.
 */
public final class ReferenceBuilder {

    private ReferenceBuilder() {
    }

    public static ReferenceTypeBuilder referenceType(String code) {
        return new ReferenceTypeBuilder(code);
    }

    public static final class ReferenceTypeBuilder {
        private final ReferenceTypeModel type = new ReferenceTypeModel();

        private ReferenceTypeBuilder(String code) {
            this.type.setId(Ids.of("reference-type:" + code));
            this.type.setCode(code);
            this.type.setName(code);
            ReferenceTypeDefinitionModel definition = new ReferenceTypeDefinitionModel();
            definition.setFields(new ArrayList<>());
            this.type.setDefinition(definition);
        }

        public ReferenceTypeBuilder field(String code, String... semantics) {
            ReferenceTypeFieldModel field = new ReferenceTypeFieldModel();
            field.setCode(code);
            field.setLabel(code);
            field.setDataType(ReferenceTypeFieldDataType.Text);
            field.setSemantics(new ArrayList<>(List.of(semantics)));
            this.type.getDefinition().getFields().add(field);
            return this;
        }

        public ReferenceTypeModel build() {
            return this.type;
        }
    }

    /** Field values are given as code/value pairs. */
    public static ReferenceModel reference(ReferenceTypeModel type, String reference, String label, String... codeValuePairs) {
        ReferenceModel model = new ReferenceModel();
        model.setId(Ids.of("reference:" + type.getCode() + ":" + reference));
        model.setType(type);
        model.setReference(reference);
        model.setLabel(label);
        model.setSource("test");
        List<ReferenceFieldModel> fields = new ArrayList<>();
        for (int i = 0; i < codeValuePairs.length; i += 2) {
            ReferenceFieldModel field = new ReferenceFieldModel();
            field.setCode(codeValuePairs[i]);
            field.setDataType(ReferenceTypeFieldDataType.Text);
            field.setValue(codeValuePairs[i + 1]);
            fields.add(field);
        }
        ReferenceDefinitionModel definition = new ReferenceDefinitionModel();
        definition.setFields(fields);
        model.setDefinition(definition);
        return model;
    }
}
