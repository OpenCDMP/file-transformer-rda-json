package org.opencdmp.filetransformer.rda.service.rdafiletransformer.export;

import org.opencdmp.commonmodels.models.plandescriptionfields.PropertyDefinitionFieldSetItemModel;
import gr.cite.tools.exception.MyApplicationException;
import org.opencdmp.commonmodels.enums.*;
import org.opencdmp.commonmodels.models.*;
import org.opencdmp.commonmodels.models.description.*;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.FieldSetModel;
import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.filetransformer.rda.model.rda.*;
import org.opencdmp.filetransformerbase.models.misc.*;
import java.net.URI;
import java.time.*;
import java.util.*;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.AnswerGroup;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaMapperContext;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaMapperSupport;
import static org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaSemantics.*;

/** Builds identifiers and affiliations: dmp and dataset related and alternate identifiers, dmp affiliations and {@code dataset_id}. */
public class IdentifierExporter extends RdaMapperSupport {

    public IdentifierExporter(RdaMapperContext context) {
        super(context);
    }

    List<Affiliation> buildAffiliation(PlanModel model){
        List<Affiliation> values = new ArrayList<>();

        for (AnswerGroup answers : this.groups(model.getPlanBlueprint(), model.getProperties().getFieldSets(), List.of(SEMANTIC_DMP_AFFILIATION_NAME, SEMANTIC_DMP_AFFILIATION_ID_TYPE, SEMANTIC_DMP_AFFILIATION_ID_IDENTIFIER))) {
            boolean valueFound = false;

            Affiliation item = new Affiliation();

            String fieldValue = answers.value(SEMANTIC_DMP_AFFILIATION_NAME);
            if (fieldValue != null) {
                item.setName(fieldValue);
                valueFound = true;
            }

            AffiliationId affiliationId = new AffiliationId();

            fieldValue = answers.value(SEMANTIC_DMP_AFFILIATION_ID);
            if (fieldValue != null) {
                affiliationId.setIdentifier(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_DMP_AFFILIATION_ID_IDENTIFIER);
            if (fieldValue != null) {
                affiliationId.setIdentifier(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_DMP_AFFILIATION_ID_TYPE);
            if (fieldValue != null) {
                affiliationId.setType(fieldValue);
                valueFound = true;
            }

            if(affiliationId.getIdentifier() != null || affiliationId.getType() != null) {
                item.setAffiliationId(affiliationId);
            }

            if (valueFound) values.add(item);
        }

        return values;
    }

    public List<AlternateIdentifier> buildRdaAlternateIdentifier(PlanModel model) {
        if (model == null) throw new MyApplicationException("Plan is missing");

        List<AlternateIdentifier> values = new ArrayList<>();
        for (AnswerGroup answers : this.groups(model.getPlanBlueprint(), model.getProperties().getFieldSets(), List.of(SEMANTIC_DMP_ALTERNATE_IDENTIFIER_ID, SEMANTIC_DMP_ALTERNATE_IDENTIFIER_TYPE))) {
            boolean valueFound = false;
            AlternateIdentifier item = new AlternateIdentifier();

            String fieldValue = answers.value(SEMANTIC_DMP_ALTERNATE_IDENTIFIER_ID);
            if (fieldValue != null) {
                item.setIdentifier(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_DMP_ALTERNATE_IDENTIFIER_TYPE);
            if (fieldValue != null) {
                item.setType(fieldValue);
                valueFound = true;
            }

            if (valueFound) values.add(item);
        }

        return values;
    }

    List<RelatedIdentifier> buildRdaRelatedIdentifier(PlanModel model) {
        List<RelatedIdentifier> values = new ArrayList<>();
        for (AnswerGroup answers : this.groups(model.getPlanBlueprint(), model.getProperties().getFieldSets(), List.of(SEMANTIC_DMP_RELATED_IDENTIFIER_IDENTIFIER, SEMANTIC_DMP_RELATED_IDENTIFIER_METADATA_SCHEME, SEMANTIC_DMP_RELATED_IDENTIFIER_RELATION_TYPE, SEMANTIC_DMP_RELATED_IDENTIFIER_RESOURCE_TYPE,
                SEMANTIC_DMP_RELATED_IDENTIFIER_SCHEME_TYPE, SEMANTIC_DMP_RELATED_IDENTIFIER_SCHEME_URI, SEMANTIC_DMP_RELATED_IDENTIFIER_TYPE))) {
            boolean valueFound = false;
            RelatedIdentifier item = new RelatedIdentifier();

            String fieldValue = answers.value(SEMANTIC_DMP_RELATED_IDENTIFIER_IDENTIFIER);
            if (fieldValue != null) {
                item.setIdentifier(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_DMP_RELATED_IDENTIFIER_METADATA_SCHEME);
            if (fieldValue != null) {
                item.setMetadataScheme(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_DMP_RELATED_IDENTIFIER_RELATION_TYPE);
            if (fieldValue != null) {
                item.setRelationType(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_DMP_RELATED_IDENTIFIER_RESOURCE_TYPE);
            if (fieldValue != null) {
                item.setResourceType(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_DMP_RELATED_IDENTIFIER_SCHEME_TYPE);
            if (fieldValue != null) {
                item.setSchemeType(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_DMP_RELATED_IDENTIFIER_SCHEME_URI);
            if (fieldValue != null) {
                try{
                    item.setSchemeUri(URI.create(fieldValue));
                    valueFound = true;
                }catch (Exception ex){
                    logger.warn("invalid Scheme Uri value: " + fieldValue);
                }
            }

            fieldValue = answers.value(SEMANTIC_DMP_RELATED_IDENTIFIER_TYPE);
            if (fieldValue != null) {
                item.setType(fieldValue);
                valueFound = true;
            }

            if (valueFound ) values.add(item);
        }
        return values;
    }

    List<RelatedIdentifier> buildRdaDatasetRelatedIdentifier(DescriptionModel model){
        List<RelatedIdentifier> values = new ArrayList<>();
        for (AnswerGroup answers : this.groups(model.getDescriptionTemplate(), model.getProperties().getFieldSets(), List.of(
                SEMANTIC_DATASET_RELATED_IDENTIFIER_IDENTIFIER, SEMANTIC_DATASET_RELATED_IDENTIFIER_METADATA_SCHEME, SEMANTIC_DATASET_RELATED_IDENTIFIER_RELATION_TYPE,
                SEMANTIC_DATASET_RELATED_IDENTIFIER_RESOURCE_TYPE, SEMANTIC_DATASET_RELATED_IDENTIFIER_SCHEME_TYPE,
                SEMANTIC_DATASET_RELATED_IDENTIFIER_SCHEME_URI, SEMANTIC_DATASET_RELATED_IDENTIFIER_TYPE))) {
            boolean valueFound = false;
            RelatedIdentifier item = new RelatedIdentifier();

            String fieldValue = answers.value(SEMANTIC_DATASET_RELATED_IDENTIFIER_IDENTIFIER);
            if (fieldValue != null) {
                item.setIdentifier(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_DATASET_RELATED_IDENTIFIER_METADATA_SCHEME);
            if (fieldValue != null) {
                item.setMetadataScheme(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_DATASET_RELATED_IDENTIFIER_RELATION_TYPE);
            if (fieldValue != null) {
                item.setRelationType(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_DATASET_RELATED_IDENTIFIER_RESOURCE_TYPE);
            if (fieldValue != null) {
                item.setResourceType(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_DATASET_RELATED_IDENTIFIER_SCHEME_TYPE);
            if (fieldValue != null) {
                item.setSchemeType(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_DATASET_RELATED_IDENTIFIER_SCHEME_URI);
            if (fieldValue != null) {
                try {
                    item.setSchemeUri(URI.create(fieldValue));
                    valueFound = true;
                } catch (Exception ex) {
                    logger.warn("invalid dataset related identifier value: " + fieldValue);
                }
            }

            fieldValue = answers.value(SEMANTIC_DATASET_RELATED_IDENTIFIER_TYPE);
            if (fieldValue != null) {
                item.setType(fieldValue);
                valueFound = true;
            }

            if (valueFound) values.add(item);

        }
        return values.isEmpty() ? null : values;
    }

    List<AlternateIdentifier> buildRdaDatasetAlternateIdentifier(DescriptionModel model){
        List<AlternateIdentifier> values = new ArrayList<>();
        for (AnswerGroup answers : this.groups(model.getDescriptionTemplate(), model.getProperties().getFieldSets(), List.of(SEMANTIC_DATASET_ALTERNATE_IDENTIFIER_ID, SEMANTIC_DATASET_ALTERNATE_IDENTIFIER_TYPE))) {
            boolean valueFound = false;
            AlternateIdentifier item = new AlternateIdentifier();

            String fieldValue = answers.value(SEMANTIC_DATASET_ALTERNATE_IDENTIFIER_ID);
            if (fieldValue != null) {
                item.setIdentifier(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_DATASET_ALTERNATE_IDENTIFIER_TYPE);
            if (fieldValue != null) {
                item.setType(fieldValue);
                valueFound = true;
            }

            if (valueFound) values.add(item);
        }
        return values;
    }

    AlternateIdentifier buildRdaDatasetAlternateIdentifierBySystem(DescriptionModel model){
        if (model == null || model.getId() == null) return null;

        AlternateIdentifier alternateIdentifier = new AlternateIdentifier();
        alternateIdentifier.setIdentifier(DATASET_SYSTEM_ALTERNATE_IDENTIFIER_PREFIX + model.getId().toString());
        alternateIdentifier.setType("internal");
        return alternateIdentifier;
    }

    List<AlternateIdentifier> buildRdaDmpAlternateIdentifiers(DescriptionModel model){
        List<AlternateIdentifier> values = new ArrayList<>();
        for (AnswerGroup answers : this.groups(model.getDescriptionTemplate(), model.getProperties().getFieldSets(), List.of(SEMANTIC_DMP_ALTERNATE_IDENTIFIER_ID, SEMANTIC_DMP_ALTERNATE_IDENTIFIER_TYPE))) {
            boolean valueFound = false;
            AlternateIdentifier item = new AlternateIdentifier();

            String fieldValue = answers.value(SEMANTIC_DMP_ALTERNATE_IDENTIFIER_ID);
            if (fieldValue != null) {
                item.setIdentifier(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_DMP_ALTERNATE_IDENTIFIER_TYPE);
            if (fieldValue != null) {
                item.setType(fieldValue);
                valueFound = true;
            }


            if (valueFound) values.add(item);
        }
        return values;
    }

    List<Affiliation> buildRdaDmpAffiliations(DescriptionModel model){
        List<Affiliation> values = new ArrayList<>();
        for (AnswerGroup answers : this.groups(model.getDescriptionTemplate(), model.getProperties().getFieldSets(), List.of(SEMANTIC_DMP_AFFILIATION_NAME, SEMANTIC_DMP_AFFILIATION_ID_TYPE, SEMANTIC_DMP_AFFILIATION_ID_IDENTIFIER))) {
            boolean valueFound = false;

            Affiliation item = new Affiliation();

            String fieldValue = answers.value(SEMANTIC_DMP_AFFILIATION_NAME);
            if (fieldValue != null) {
                item.setName(fieldValue);
                valueFound = true;
            }

            AffiliationId affiliationId = new AffiliationId();

            fieldValue = answers.value(SEMANTIC_DMP_AFFILIATION_ID);
            if (fieldValue != null) {
                affiliationId.setIdentifier(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_DMP_AFFILIATION_ID_IDENTIFIER);
            if (fieldValue != null) {
                affiliationId.setIdentifier(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_DMP_AFFILIATION_ID_TYPE);
            if (fieldValue != null) {
                affiliationId.setType(fieldValue);
                valueFound = true;
            }

            if(affiliationId.getIdentifier() != null || affiliationId.getType() != null) {
                item.setAffiliationId(affiliationId);
            }

            if (valueFound) values.add(item);
        }
        return values;
    }

    List<RelatedIdentifier> buildRdaRelatedIdentifiers(DescriptionModel model){
        List<RelatedIdentifier> values = new ArrayList<>();
        for (AnswerGroup answers : this.groups(model.getDescriptionTemplate(), model.getProperties().getFieldSets(), List.of(SEMANTIC_DMP_RELATED_IDENTIFIER_IDENTIFIER, SEMANTIC_DMP_RELATED_IDENTIFIER_METADATA_SCHEME, SEMANTIC_DMP_RELATED_IDENTIFIER_RELATION_TYPE, SEMANTIC_DMP_RELATED_IDENTIFIER_RESOURCE_TYPE,
                SEMANTIC_DMP_RELATED_IDENTIFIER_SCHEME_TYPE, SEMANTIC_DMP_RELATED_IDENTIFIER_SCHEME_URI, SEMANTIC_DMP_RELATED_IDENTIFIER_TYPE))) {
            boolean valueFound = false;
            RelatedIdentifier item = new RelatedIdentifier();

           String fieldValue = answers.value(SEMANTIC_DMP_RELATED_IDENTIFIER_IDENTIFIER);
            if (fieldValue != null) {
                    item.setIdentifier(fieldValue);
                    valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_DMP_RELATED_IDENTIFIER_METADATA_SCHEME);
            if (fieldValue != null) {
                item.setMetadataScheme(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_DMP_RELATED_IDENTIFIER_RELATION_TYPE);
            if (fieldValue != null) {
                item.setRelationType(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_DMP_RELATED_IDENTIFIER_RESOURCE_TYPE);
            if (fieldValue != null) {
                item.setResourceType(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_DMP_RELATED_IDENTIFIER_SCHEME_TYPE);
            if (fieldValue != null) {
                item.setSchemeType(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_DMP_RELATED_IDENTIFIER_SCHEME_URI);
            if (fieldValue != null) {
                try{
                    item.setSchemeUri(URI.create(fieldValue));
                    valueFound = true;
                }catch (Exception ex){
                    logger.warn("invalid Scheme Uri value: " + fieldValue);
                }
            }

            fieldValue = answers.value(SEMANTIC_DMP_RELATED_IDENTIFIER_TYPE);
            if (fieldValue != null) {
                item.setType(fieldValue);
                valueFound = true;
            }

            if (valueFound ) values.add(item);
        }
        return values;
    }

    DatasetId buildRdaDatasetId(DescriptionModel model){
        DatasetId datasetId = new DatasetId();
        datasetId.setType(DMP_TYPE_OTHER);

        for (AnswerGroup answers : this.groups(model.getDescriptionTemplate(), model.getProperties().getFieldSets(), List.of(SEMANTIC_DATASET_DATASET_ID))) {

            String fieldValue = answers.value(SEMANTIC_DATASET_DATASET_ID);
            if (fieldValue != null && !fieldValue.isBlank()) {
                datasetId.setIdentifier(fieldValue);
                datasetId.setType(inferPersistentIdType(fieldValue));
                return datasetId;
            }
        }
        
        for (AnswerGroup answers : this.groups(model.getDescriptionTemplate(), model.getProperties().getFieldSets(), List.of(SEMANTIC_DATASET_DATASET_ID_ID, SEMANTIC_DATASET_DATASET_ID_TYPE))) {
            boolean valueFound = false;

            String fieldValue = answers.value(SEMANTIC_DATASET_DATASET_ID_ID);
            if (fieldValue != null) {
                datasetId.setIdentifier(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_DATASET_DATASET_ID_TYPE);
            if (fieldValue != null) {
                datasetId.setType(fieldValue);
                valueFound = true;
            }

            if (valueFound) {
                if (answers.value(SEMANTIC_DATASET_DATASET_ID_TYPE) == null && datasetId.getIdentifier() != null) datasetId.setType(inferPersistentIdType(datasetId.getIdentifier()));
                return datasetId;
            }
        }
        return new DatasetId(model.getId().toString(), DMP_TYPE_OTHER);
    }
}
