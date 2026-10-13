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

/** Builds the dataset {@code creator} list. */
public class CreatorExporter extends RdaMapperSupport {

    public CreatorExporter(RdaMapperContext context) {
        super(context);
    }

    List<Creator> buildRdaDatasetCreator(DescriptionModel model){
            List<Creator> values = new ArrayList<>();
            for (AnswerGroup answers : this.groups(model.getDescriptionTemplate(), model.getProperties().getFieldSets(), List.of(SEMANTIC_DATASET_CREATOR_NAME, SEMANTIC_DATASET_CREATOR_MBOX, SEMANTIC_DATASET_CREATOR_ID_IDENTIFIER,
                    SEMANTIC_DATASET_CREATOR_ID_TYPE, SEMANTIC_DATASET_CREATOR_AFFILIATION_NAME, SEMANTIC_DATASET_CREATOR_AFFILIATION_ID_TYPE, SEMANTIC_DATASET_CREATOR_AFFILIATION_ID_IDENTIFIER, SEMANTIC_DATASET_CREATOR_ID ))) {
                boolean valueFound = false;
                Creator item = new Creator();

                String fieldValue = answers.value(SEMANTIC_DATASET_CREATOR_NAME);
                if (fieldValue != null) {
                    item.setName(fieldValue);
                    valueFound = true;
                }

                fieldValue = answers.value(SEMANTIC_DATASET_CREATOR_MBOX);
                if (fieldValue != null) {
                    item.setMbox(fieldValue);
                    valueFound = true;
                }


                item.setAffiliation(this.buildRdaDatasetCreatorAffiliation(answers));
                item.setCreatorId(this.buildRdaDatasetCreatorId(answers));

                if (valueFound || (item.getAffiliation() != null && !item.getAffiliation().isEmpty()) || (item.getCreatorId() != null && !item.getCreatorId().isEmpty())) values.add(item);

            }
            return values.isEmpty() ? null : values;
        }

    List<CreatorId> buildRdaDatasetCreatorId(AnswerGroup answers) {

        List<CreatorId> creatorIds = new ArrayList<>();
        CreatorId creatorId = new CreatorId();
        String fieldValue = answers.value(SEMANTIC_DATASET_CREATOR_ID_IDENTIFIER);
        if (fieldValue != null) {
            creatorId.setIdentifier(fieldValue);
        }

        fieldValue = answers.value(SEMANTIC_DATASET_CREATOR_ID_TYPE);
        if (fieldValue != null) {
            creatorId.setType(fieldValue);
        }
        if (creatorId.getIdentifier() != null && creatorId.getType() == null) creatorId.setType(inferIdentifierType(creatorId.getIdentifier()));
        if (creatorId.getIdentifier() != null || creatorId.getType() != null) {
            creatorIds.add(creatorId);
        }
        return creatorIds;
    }

    List<Affiliation> buildRdaDatasetCreatorAffiliation(AnswerGroup answers) {

        List<Affiliation> affiliations = new ArrayList<>();
        Affiliation affiliation = new Affiliation();
        String fieldValue = answers.value(SEMANTIC_DATASET_CREATOR_AFFILIATION_NAME);
        if (fieldValue != null) {
            affiliation.setName(fieldValue);
        }

        affiliation.setAffiliationId(this.buildRdaDatasetCreatorAffiliationId(answers));

        if (affiliation.getName() != null || affiliation.getAffiliationId() != null) {
            affiliations.add(affiliation);
        }
        return affiliations;
    }

    AffiliationId buildRdaDatasetCreatorAffiliationId(AnswerGroup answers) {

        AffiliationId affiliationId = new AffiliationId();
        String fieldValue = answers.value(SEMANTIC_DATASET_CREATOR_AFFILIATION_ID_IDENTIFIER);
        if (fieldValue != null) {
            affiliationId.setIdentifier(fieldValue);
        }

        fieldValue = answers.value(SEMANTIC_DATASET_CREATOR_AFFILIATION_ID_TYPE);
        if (fieldValue != null) {
            affiliationId.setType(fieldValue);
        }


        if (affiliationId.getIdentifier() != null || affiliationId.getType() != null) {
            return affiliationId;
        }
        return null;
    }
}
