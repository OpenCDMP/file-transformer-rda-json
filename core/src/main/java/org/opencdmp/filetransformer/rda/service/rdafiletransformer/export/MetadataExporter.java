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

/** Builds the dataset {@code metadata} list. */
public class MetadataExporter extends RdaMapperSupport {

    public MetadataExporter(RdaMapperContext context) {
        super(context);
    }

    List<Metadata> buildRdaDatasetMetadata(DescriptionModel model){
        List<Metadata> values = new ArrayList<>();
        for (AnswerGroup answers : this.groups(model.getDescriptionTemplate(), model.getProperties().getFieldSets(), List.of(SEMANTIC_DATASET_METADATA_DESCRIPTION, SEMANTIC_DATASET_METADATA_LANGUAGE, SEMANTIC_DATASET_METADATA_STANDARD_ID, SEMANTIC_DATASET_METADATA_STANDARD_ID_IDENTIFIER, SEMANTIC_DATASET_METADATA_STANDARD_ID_TYPE))) {
            boolean valueFound = false;
            Metadata item = new Metadata();

           String fieldValue = answers.value(SEMANTIC_DATASET_METADATA_LANGUAGE);
            if (fieldValue != null) {
                try {
                    item.setLanguage(Metadata.Language.fromValue(fieldValue));
                    valueFound = true;
                } catch (Exception e) {
                    logger.warn("invalid metadatum language value: " + fieldValue);
                }
            }
            

            fieldValue = answers.value(SEMANTIC_DATASET_METADATA_DESCRIPTION);
            if (fieldValue != null) {
                item.setDescription(fieldValue);
                valueFound = true;
            }
            MetadataStandardId standardId = this.buildRdaMetadataStandardId(answers);
            if (standardId != null) {
                item.setMetadataStandardId(List.of(standardId));
                valueFound = true;

            }

            if (valueFound) values.add(item);
        }
        return values.isEmpty() ? null : values;
    }

    MetadataStandardId buildRdaMetadataStandardId(AnswerGroup answers){
        boolean valueFound = false;
        MetadataStandardId standardId = new MetadataStandardId();
        standardId.setType(METADATA_STANDARD_TYPE_OTHER);

        String fieldValue = answers.value(SEMANTIC_DATASET_METADATA_STANDARD_ID);
        if (fieldValue != null) {
            standardId.setIdentifier(fieldValue);
            return standardId;
        }

        fieldValue = answers.value(SEMANTIC_DATASET_METADATA_STANDARD_ID_IDENTIFIER);
        if (fieldValue != null) {
            standardId.setIdentifier(fieldValue);
            valueFound = true;
        }

        fieldValue = answers.value(SEMANTIC_DATASET_METADATA_STANDARD_ID_TYPE);
        if (fieldValue != null) {
            try {
                standardId.setType(fieldValue);
            } catch (Exception ex){
                logger.warn("invalid metadata standard id type value: " + fieldValue);
            }
        }

        if (valueFound) return standardId;
        return null;
    }
}
