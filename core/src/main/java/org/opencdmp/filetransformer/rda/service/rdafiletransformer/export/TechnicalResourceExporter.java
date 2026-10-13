package org.opencdmp.filetransformer.rda.service.rdafiletransformer.export;

import org.opencdmp.commonmodels.models.plandescriptionfields.PropertyDefinitionFieldSetItemModel;
import org.opencdmp.commonmodels.enums.*;
import org.opencdmp.commonmodels.models.*;
import org.opencdmp.commonmodels.models.description.*;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.FieldSetModel;
import org.opencdmp.filetransformer.rda.model.rda.*;
import org.opencdmp.filetransformerbase.models.misc.*;
import java.time.*;
import java.util.*;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.AnswerGroup;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaMapperContext;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaMapperSupport;
import static org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaSemantics.*;

/** Builds the dataset {@code technical_resource} list. */
public class TechnicalResourceExporter extends RdaMapperSupport {

    public TechnicalResourceExporter(RdaMapperContext context) {
        super(context);
    }

    List<TechnicalResource> buildRdaDatasetTechnicalResources(DescriptionModel model) {
        List<TechnicalResource> values = new ArrayList<>();
        for (AnswerGroup answers : this.groups(model.getDescriptionTemplate(), model.getProperties().getFieldSets(), List.of(SEMANTIC_DATASET_TECHNICAL_RESOURCE_NAME, SEMANTIC_DATASET_TECHNICAL_RESOURCE_DESCRIPTION))) {
            boolean valueFound = false;
            TechnicalResource item = new TechnicalResource();

            String fieldValue = answers.value(SEMANTIC_DATASET_TECHNICAL_RESOURCE_NAME);
            if (fieldValue != null) {
                item.setName(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_DATASET_TECHNICAL_RESOURCE_DESCRIPTION);
            if (fieldValue != null) {
                item.setDescription(fieldValue);
                valueFound = true;
            }

           item.setTechnicalResourceId(this.buildRdaDatasetTechnicalResourceId(answers));

            if (valueFound || (item.getTechnicalResourceId() != null && !item.getTechnicalResourceId().isEmpty())) values.add(item);
        }
        return values;

    }

    List<TechnicalResourceId> buildRdaDatasetTechnicalResourceId(AnswerGroup answers) {

        List<TechnicalResourceId> technicalResourceIds = new ArrayList<>();
        TechnicalResourceId technicalResourceId = new TechnicalResourceId();
        String fieldValue = answers.value(SEMANTIC_DATASET_TECHNICAL_RESOURCE_ID_IDENTIFIER);
        if (fieldValue != null) {
            technicalResourceId.setIdentifier(fieldValue);
        }

        fieldValue = answers.value(SEMANTIC_DATASET_TECHNICAL_RESOURCE_ID_TYPE);
        if (fieldValue != null) {
            technicalResourceId.setType(fieldValue);
        }
        if (technicalResourceId.getIdentifier() != null || technicalResourceId.getType() != null) {
            technicalResourceIds.add(technicalResourceId);
        }
        return technicalResourceIds;
    }
}
