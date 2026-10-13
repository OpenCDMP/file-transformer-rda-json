package org.opencdmp.filetransformer.rda.support;

import org.opencdmp.commonmodels.models.plandescriptionfields.ExternalIdentifierModel;
import org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel;
import org.opencdmp.commonmodels.models.plandescriptionfields.PropertyDefinitionFieldSetItemModel;
import org.opencdmp.commonmodels.models.plandescriptionfields.PropertyDefinitionFieldSetModel;
import org.opencdmp.commonmodels.models.reference.ReferenceModel;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Builds the answers of a plan or description: fieldSet id > items (one per answer of a question) > field values.
 * Values are stored the way the backend stores them (radio in textValue, select and tags in textListValue).
 */
public final class ValuesBuilder {

    private final Map<String, PropertyDefinitionFieldSetModel> fieldSets = new LinkedHashMap<>();

    public static ValuesBuilder values() {
        return new ValuesBuilder();
    }

    /** Adds one answer to a question; call again on the same question for a repeating one. */
    public ValuesBuilder answer(String questionId, Consumer<Answer> inputs) {
        PropertyDefinitionFieldSetModel fieldSet = this.fieldSets.computeIfAbsent(questionId, id -> {
            PropertyDefinitionFieldSetModel model = new PropertyDefinitionFieldSetModel();
            model.setItems(new ArrayList<>());
            return model;
        });
        Answer answer = new Answer();
        inputs.accept(answer);
        PropertyDefinitionFieldSetItemModel item = new PropertyDefinitionFieldSetItemModel();
        item.setOrdinal(fieldSet.getItems().size());
        item.setFields(answer.fields);
        fieldSet.getItems().add(item);
        return this;
    }

    public Map<String, PropertyDefinitionFieldSetModel> build() {
        return new HashMap<>(this.fieldSets);
    }

    public static final class Answer {
        private final Map<String, FieldModel> fields = new LinkedHashMap<>();

        private FieldModel field(String inputId) {
            return this.fields.computeIfAbsent(inputId, id -> {
                FieldModel model = new FieldModel();
                model.setId(id);
                return model;
            });
        }

        public Answer text(String inputId, String value) {
            this.field(inputId).setTextValue(value);
            return this;
        }

        /** Select, tags and multi-value inputs. */
        public Answer list(String inputId, String... values) {
            this.field(inputId).setTextListValue(new ArrayList<>(List.of(values)));
            return this;
        }

        public Answer bool(String inputId, boolean value) {
            this.field(inputId).setBooleanValue(value);
            return this;
        }

        public Answer number(String inputId, double value) {
            this.field(inputId).setNumberValue(value);
            return this;
        }

        public Answer date(String inputId, String isoDate) {
            this.field(inputId).setDateValue(LocalDate.parse(isoDate).atStartOfDay().toInstant(ZoneOffset.UTC));
            return this;
        }

        public Answer identifier(String inputId, String identifier, String type) {
            ExternalIdentifierModel model = new ExternalIdentifierModel();
            model.setIdentifier(identifier);
            model.setType(type);
            this.field(inputId).setExternalIdentifier(model);
            return this;
        }

        public Answer references(String inputId, ReferenceModel... references) {
            this.field(inputId).setReferences(new ArrayList<>(List.of(references)));
            return this;
        }

        /** An input the author left empty: present in the answer, with no value. */
        public Answer empty(String inputId) {
            this.field(inputId);
            return this;
        }
    }
}
