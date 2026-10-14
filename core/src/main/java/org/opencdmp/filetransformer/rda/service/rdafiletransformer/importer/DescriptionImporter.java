package org.opencdmp.filetransformer.rda.service.rdafiletransformer.importer;

import org.opencdmp.commonmodels.models.plandescriptionfields.ExternalIdentifierModel;
import org.opencdmp.commonmodels.models.plandescriptionfields.PropertyDefinitionFieldSetItemModel;
import org.opencdmp.commonmodels.models.plandescriptionfields.PropertyDefinitionFieldSetModel;
import gr.cite.tools.exception.MyApplicationException;
import org.opencdmp.commonmodels.enums.*;
import org.opencdmp.commonmodels.models.*;
import org.opencdmp.commonmodels.models.description.*;
import org.opencdmp.commonmodels.models.descriptiotemplate.DescriptionTemplateModel;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.FieldModel;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.FieldSetModel;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.fielddata.ReferenceTypeDataModel;
import org.opencdmp.commonmodels.models.reference.ReferenceModel;
import org.opencdmp.filetransformer.rda.model.rda.*;
import org.opencdmp.filetransformerbase.models.misc.*;
import java.time.*;
import java.util.*;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaMapperContext;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaMapperSupport;
import static org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaSemantics.*;

/** Writes one RDA dataset into the answers of a description template. */
public class DescriptionImporter extends RdaMapperSupport {

    public DescriptionImporter(RdaMapperContext context) {
        super(context);
    }

    /**
     * @param placed the plan-level objects already imported (by the blueprint or an earlier description); this
     *               description skips them and adds the ones it takes
     */
    public DescriptionModel importDescriptionWithModel(DescriptionImportModel descriptionImportModel, Dataset dataset, Dmp dmp, Set<String> placed) {
        if (descriptionImportModel == null) throw new MyApplicationException("description import is missing");
        if (dataset == null) throw new MyApplicationException("dataset is missing");

        DescriptionModel model = new DescriptionModel();

        if (descriptionImportModel.getId() != null && !descriptionImportModel.getId().isBlank()) {
            try {
                model.setId(UUID.fromString(descriptionImportModel.getId()));
            } catch (Exception ex) {

            }
        }
        model.setLabel(dataset.getTitle());
        model.setDescription(dataset.getDescription());
        model.setDescriptionTemplate(descriptionImportModel.getDescriptionTemplate());
        model.setSectionId(descriptionImportModel.getSectionId());
        model.setTags(dataset.getKeyword());
        model.setProperties(this.buildPropertyDefinitionModel(descriptionImportModel.getDescriptionTemplate(), dataset, dmp, placed));

        return model;
    }

    PropertyDefinitionModel buildPropertyDefinitionModel(DescriptionTemplateModel descriptionTemplateModel, Dataset dataset, Dmp dmp, Set<String> placed) {
        PropertyDefinitionModel model = new PropertyDefinitionModel();
        if (descriptionTemplateModel == null) throw new MyApplicationException("description template is missing");

        Map<String, PropertyDefinitionFieldSetModel> fieldSets = this.buildFieldSetMap(descriptionTemplateModel, dataset, dmp);

        if (dataset.getMetadata() != null) this.mergeAnswers(fieldSets, this.buildFieldByRdaMetadata(descriptionTemplateModel, dataset.getMetadata()));
        if (dataset.getSecurityAndPrivacy() != null) this.mergeAnswers(fieldSets, this.buildFieldByRdaSecurityAndPrivacy(descriptionTemplateModel, dataset.getSecurityAndPrivacy()));
        if (dataset.getTechnicalResource() != null) this.mergeAnswers(fieldSets, this.buildFieldByRdaTechnicalResource(descriptionTemplateModel, dataset.getTechnicalResource()));
        if (dmp.getCost() != null) this.placeOnce(placed, OBJECT_COST, fieldSets, () -> this.buildFieldByRdaCost(descriptionTemplateModel, dmp.getCost()));
        if (dmp.getContributor() != null) this.placeOnce(placed, OBJECT_CONTRIBUTOR, fieldSets, () -> this.buildFieldByRdaContributor(descriptionTemplateModel, dmp.getContributor()));
        if (dataset.getDistribution() != null) this.mergeAnswers(fieldSets, this.buildFieldByRdaDistribution(descriptionTemplateModel, dataset.getDistribution()));
        if (dataset.getDistribution() == null || dataset.getDistribution().isEmpty()) this.mergeAnswers(fieldSets, this.chooseNothingToExport(descriptionTemplateModel.getDefinition().getSections(), SEMANTIC_DATASET_DISTRIBUTION));
        if (dmp.getProject() != null) this.placeOnce(placed, OBJECT_PROJECT, fieldSets, () -> this.buildFieldByRdaProject(descriptionTemplateModel, dmp.getProject()));
        if (dmp.getAlternateIdentifier() != null) this.placeOnce(placed, OBJECT_ALTERNATE_IDENTIFIER, fieldSets, () -> this.buildFieldByRdaDmpAlternateIdentifiers(descriptionTemplateModel, dmp.getAlternateIdentifier()));
        if (dmp.getRelatedIdentifier() != null) this.placeOnce(placed, OBJECT_RELATED_IDENTIFIER, fieldSets, () -> this.buildFieldByRdaDmpRelatedIdentifier(descriptionTemplateModel, dmp.getRelatedIdentifier()));
        if (dataset.getRelatedIdentifier() != null) this.mergeAnswers(fieldSets, this.buildFieldByRdaDatasetRelatedIdentifier(descriptionTemplateModel, dataset.getRelatedIdentifier()));
        if (dataset.getAlternateIdentifier() != null) this.mergeAnswers(fieldSets, this.buildFieldByRdaDatasetAlternateIdentifiers(descriptionTemplateModel, dataset.getAlternateIdentifier()));
        if (dataset.getCreator() != null) this.mergeAnswers(fieldSets, this.buildFieldByRdaCreator(descriptionTemplateModel, dataset.getCreator()));
        if (dmp.getAffiliation() != null) this.placeOnce(placed, OBJECT_AFFILIATION, fieldSets, () -> this.buildFieldByRdaDmpAffiliations(descriptionTemplateModel, dmp.getAffiliation()));

        this.trimToMultiplicity(descriptionTemplateModel.getDefinition().getAllFieldSets(), fieldSets);

        for (FieldSetModel fieldSetModel: descriptionTemplateModel.getDefinition().getAllFieldSets()){
            if (!fieldSets.containsKey(fieldSetModel.getId())){
                Map<String, org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel> fieldsMap = new HashMap<>();
                for (org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.FieldModel templateFieldModel: fieldSetModel.getFields()){
                    org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel fieldModel = new org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel();
                    fieldModel.setId(templateFieldModel.getId());
                    fieldsMap.put(templateFieldModel.getId(), fieldModel);
                }

                PropertyDefinitionFieldSetModel propertyDefinitionFieldSetModel = new PropertyDefinitionFieldSetModel();
                propertyDefinitionFieldSetModel.setItems(new ArrayList<>());

                PropertyDefinitionFieldSetItemModel propertyDefinitionFieldSetItemModel = new PropertyDefinitionFieldSetItemModel();
                propertyDefinitionFieldSetItemModel.setFields(fieldsMap);
                propertyDefinitionFieldSetItemModel.setOrdinal(propertyDefinitionFieldSetModel.getItems().size());

                propertyDefinitionFieldSetModel.getItems().add(propertyDefinitionFieldSetItemModel);

                fieldSets.put(fieldSetModel.getId(), propertyDefinitionFieldSetModel);
            }
        }

        model.setFieldSets(fieldSets);
        return model;
    }

    Map<String, PropertyDefinitionFieldSetModel> buildFieldSetMap(DescriptionTemplateModel descriptionTemplateModel, Dataset dataset, Dmp dmp){
        Map<String, PropertyDefinitionFieldSetModel> fieldSetModelMap = new HashMap<>();
        Map<String, PropertyDefinitionFieldSetModel> extrafieldSetModelMap = new HashMap<>();

        if (descriptionTemplateModel == null) throw new MyApplicationException("description template is missing");

        List<FieldSetModel> fieldSetsWithSemantics = this.templateFieldSearcherService.searchFieldSetsBySemantics(descriptionTemplateModel, List.of(SEMANTIC_DATASET_DATASET_ID, SEMANTIC_DATASET_PERSONAL_DATA, SEMANTIC_DATASET_SENSITIVE_DATA,
                SEMANTIC_DATASET_LANGUAGE, SEMANTIC_DATASET_TYPE, SEMANTIC_DATASET_ISSUED, SEMANTIC_DATASET_KEYWORD, SEMANTIC_DATASET_PRESERVATION_STATEMENT, SEMANTIC_DATASET_DATA_QUALITY_ASSURANCE,
                SEMANTIC_ETHICAL_ISSUES_EXISTS, SEMANTIC_ETHICAL_ISSUES_DESCRIPTION, SEMANTIC_ETHICAL_ISSUES_REPORT, SEMANTIC_DATASET_TECHNICAL_RESOURCE_NAME, SEMANTIC_DATASET_TECHNICAL_RESOURCE_DESCRIPTION, SEMANTIC_DATASET_TECHNICAL_RESOURCE_ID_TYPE, SEMANTIC_DATASET_TECHNICAL_RESOURCE_ID_IDENTIFIER,
                SEMANTIC_DATASET_DISTRIBUTION_DESCRIPTION, SEMANTIC_DATASET_DISTRIBUTION_TITLE, SEMANTIC_DATASET_ALTERNATE_IDENTIFIER_ID, SEMANTIC_DATASET_ALTERNATE_IDENTIFIER_TYPE,
                SEMANTIC_DATASET_DISTRIBUTION_DATA_ACCESS, SEMANTIC_DATASET_DISTRIBUTION_BYTE_SIZE, SEMANTIC_DATASET_DISTRIBUTION_ACCESS_URL, SEMANTIC_DATASET_DISTRIBUTION_AVAILABLE_UTIL, SEMANTIC_DATASET_DISTRIBUTION_DOWNLOAD_URL,
                SEMANTIC_DATASET_DISTRIBUTION_LICENCE, SEMANTIC_DATASET_DISTRIBUTION_LICENCE_LICENSE_REF, SEMANTIC_DATASET_DISTRIBUTION_LICENCE_START_DATE,
                SEMANTIC_DATASET_DISTRIBUTION_HOST_AVAILABILITY, SEMANTIC_DATASET_DISTRIBUTION_HOST_BACKUP_FREQUENCY, SEMANTIC_DATASET_DISTRIBUTION_HOST_BACKUP_TYPE,
                SEMANTIC_DATASET_DISTRIBUTION_HOST_CERTIFIED_WITH, SEMANTIC_DATASET_DISTRIBUTION_HOST_DESCRIPTION, SEMANTIC_DATASET_DISTRIBUTION_HOST_PID_SYSTEM, SEMANTIC_DATASET_DISTRIBUTION_HOST_STORAGE_TYPE,
                SEMANTIC_DATASET_DISTRIBUTION_HOST_TITLE, SEMANTIC_DATASET_DISTRIBUTION_HOST_URL, SEMANTIC_DATASET_DISTRIBUTION_HOST_GEO_LOCATION, SEMANTIC_DATASET_DISTRIBUTION_HOST_SUPPORT_VERSIONING, SEMANTIC_DATASET_DISTRIBUTION_HOST_ID_IDENTIFIER, SEMANTIC_DATASET_DISTRIBUTION_HOST_ID_TYPE,
                SEMANTIC_DATASET_DISTRIBUTION_FORMAT, SEMANTIC_DATASET_METADATA_DESCRIPTION, SEMANTIC_DATASET_METADATA_LANGUAGE,
                SEMANTIC_DATASET_METADATA_STANDARD_ID_IDENTIFIER, SEMANTIC_DATASET_METADATA_STANDARD_ID_TYPE, SEMANTIC_DATASET_SECURITY_AND_PRIVACY_TITLE, SEMANTIC_DATASET_SECURITY_AND_PRIVACY_DESCRIPTION, SEMANTIC_COST_CURRENCY_CODE, SEMANTIC_COST_TITLE,
                SEMANTIC_COST_VALUE, SEMANTIC_COST_DESCRIPTION, SEMANTIC_DATASET_CONTRIBUTOR, SEMANTIC_DATASET_DESCRIPTION, SEMANTIC_DATASET_IS_REUSED, SEMANTIC_DATASET_TITLE, SEMANTIC_DATASET_DATASET_ID_TYPE,
                SEMANTIC_DATASET_RELATED_IDENTIFIER_IDENTIFIER, SEMANTIC_DATASET_RELATED_IDENTIFIER_RELATION_TYPE, SEMANTIC_DATASET_RELATED_IDENTIFIER_METADATA_SCHEME, SEMANTIC_DATASET_RELATED_IDENTIFIER_RESOURCE_TYPE, SEMANTIC_DATASET_RELATED_IDENTIFIER_SCHEME_URI, SEMANTIC_DATASET_RELATED_IDENTIFIER_SCHEME_TYPE, SEMANTIC_DATASET_RELATED_IDENTIFIER_TYPE,
                SEMANTIC_DMP_ALTERNATE_IDENTIFIER_ID, SEMANTIC_DMP_ALTERNATE_IDENTIFIER_TYPE,
                SEMANTIC_DMP_RELATED_IDENTIFIER_IDENTIFIER, SEMANTIC_DMP_RELATED_IDENTIFIER_RELATION_TYPE, SEMANTIC_DMP_RELATED_IDENTIFIER_METADATA_SCHEME, SEMANTIC_DMP_RELATED_IDENTIFIER_RESOURCE_TYPE, SEMANTIC_DMP_RELATED_IDENTIFIER_SCHEME_URI, SEMANTIC_DMP_RELATED_IDENTIFIER_SCHEME_TYPE, SEMANTIC_DMP_RELATED_IDENTIFIER_TYPE, SEMANTIC_DATASET_RIGHTS ));
        if (!fieldSetsWithSemantics.isEmpty()){
            for (FieldSetModel templateFieldSetModel: fieldSetsWithSemantics) {
                PropertyDefinitionFieldSetModel propertyDefinitionFieldSetModel = fieldSetModelMap.getOrDefault(templateFieldSetModel.getId(), null);
                if(propertyDefinitionFieldSetModel == null) {
                    propertyDefinitionFieldSetModel = new PropertyDefinitionFieldSetModel();
                    propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                    fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
                }

                PropertyDefinitionFieldSetItemModel propertyDefinitionFieldSetItemModel = new PropertyDefinitionFieldSetItemModel();
                Map<String, org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel> fieldsMap = new HashMap<>();
                for (FieldModel templateFieldModel: templateFieldSetModel.getFields()) {
                    if (templateFieldModel.getSemantics() != null && !templateFieldModel.getSemantics().isEmpty()) {
                        boolean valueFound = false;
                        org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel fieldModel = new org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel();
                        fieldModel.setId(templateFieldModel.getId());
                        if (dataset != null) {
                            if (dataset.getDatasetId() != null && (templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DATASET_ID))){
                                fieldModel.setTextValue(dataset.getDatasetId().getIdentifier());

                                ExternalIdentifierModel externalIdentifierModel = new ExternalIdentifierModel();
                                externalIdentifierModel.setIdentifier(dataset.getDatasetId().getIdentifier());
                                if (dataset.getDatasetId().getType() != null) externalIdentifierModel.setType(dataset.getDatasetId().getType().toString());
                                fieldModel.setExternalIdentifier(externalIdentifierModel);

                                if (templateFieldModel.getData() instanceof ReferenceTypeDataModel referenceTypeDataModel) {
                                    ReferenceModel referenceModel = new ReferenceModel();
                                    referenceModel.setReference(dataset.getDatasetId().getIdentifier());
                                    referenceModel.setLabel(dataset.getTitle());
                                    referenceModel.setType(referenceTypeDataModel.getReferenceType());
                                    fieldModel.setReferences(new ArrayList<>());
                                    fieldModel.getReferences().add(referenceModel);
                                }

                                valueFound = true;
                            }
                            if (dataset.getDatasetId() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DATASET_ID_ID)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, dataset.getDatasetId().getIdentifier(), null, null, null);
                                valueFound = true;
                            }
                            if (dataset.getTitle() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_TITLE)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, dataset.getTitle(), null, null, null);
                                valueFound = true;
                            }
                            if (dataset.getDescription() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DESCRIPTION)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, dataset.getDescription(), null, null, null);;
                                valueFound = true;
                            }
                            if (dataset.getDatasetId() != null && dataset.getDatasetId().getType() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DATASET_ID_TYPE)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, dataset.getDatasetId().getType().toString(), null, null, null);
                                valueFound = true;
                            }
                            if (dataset.getPersonalData() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_PERSONAL_DATA)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, dataset.getPersonalData().value(), null, null, null);
                                valueFound = true;
                            }
                            if (dataset.getSensitiveData() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_SENSITIVE_DATA)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, dataset.getSensitiveData().value(), null, null, null);
                                valueFound = true;
                            }
                            if (dataset.getLanguage() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_LANGUAGE)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, dataset.getLanguage().value(), null, null, null);
                                valueFound = true;
                            }
                            if (dataset.getType() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_TYPE)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, dataset.getType(), null, null, null);
                                valueFound = true;
                            }
                            if (dataset.getIsReused() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_IS_REUSED)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, null, null, dataset.getIsReused(), null);
                                valueFound = true;
                            }
                            if (dataset.getIssued() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_ISSUED)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, dataset.getIssued(), null, null, null);
                                valueFound = true;
                            }
                            if (dataset.getKeyword() != null && !dataset.getKeyword().isEmpty() && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_KEYWORD)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, null, null, null, dataset.getKeyword());
                                valueFound = true;
                            }
                            if (dataset.getPreservationStatement() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_PRESERVATION_STATEMENT)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, dataset.getPreservationStatement(), null, null, null);
                                valueFound = true;
                            }
                            if (dataset.getDataQualityAssurance() != null && !dataset.getDataQualityAssurance().isEmpty() && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DATA_QUALITY_ASSURANCE)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, null, null, null, dataset.getDataQualityAssurance());
                                valueFound = true;
                            }
                            if (dataset.getRights() != null && !dataset.getRights().isBlank() && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_RIGHTS)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, dataset.getRights(), null, null, null);
                                valueFound = true;
                            }
                        }
                        if (dmp != null) {
                            if (dmp.getTitle() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DMP_TITLE)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, dmp.getTitle(), null, null, null);
                                valueFound = true;
                            }
                            if (dmp.getDescription() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DMP_DESCRIPTION)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, dmp.getDescription(), null, null, null);
                                valueFound = true;
                            }
                            if (dmp.getCreated() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DMP_CREATED)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, null, Date.from(dmp.getCreated()), null, null);
                                valueFound = true;
                            }
                            if (dmp.getModified() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DMP_MODIFIED)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, null, Date.from(dmp.getModified()), null, null);
                                valueFound = true;
                            }
                            if (dmp.getLanguage() != null && dmp.getLanguage().value() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DMP_LANGUAGE)){
                                // the key holds the two-letter code, as the plan importer writes it
                                String code = this.configuration.getRdaFileTransformerServiceProperties().getLanguageMap().entrySet().stream()
                                        .filter(entry -> dmp.getLanguage().value().equals(entry.getValue())).map(Map.Entry::getKey).findFirst().orElse(null);
                                if (code != null) {
                                    fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, code, null, null, null);
                                    valueFound = true;
                                }
                            }
                            if (dmp.getEthicalIssuesExist() != null && templateFieldModel.getSemantics().contains(SEMANTIC_ETHICAL_ISSUES_EXISTS)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, dmp.getEthicalIssuesExist().value(), null, null, null);
                                valueFound = true;
                            }
                            if (dmp.getEthicalIssuesDescription() != null && templateFieldModel.getSemantics().contains(SEMANTIC_ETHICAL_ISSUES_DESCRIPTION)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, dmp.getEthicalIssuesDescription(), null, null, null);
                                valueFound = true;
                            }
                            if (dmp.getEthicalIssuesReport() != null && templateFieldModel.getSemantics().contains(SEMANTIC_ETHICAL_ISSUES_REPORT)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, dmp.getEthicalIssuesReport(), null, null, null);
                                valueFound = true;
                            }
                        }

                        if (valueFound) fieldsMap.put(templateFieldModel.getId(), fieldModel);
                    }
                }
                if (propertyDefinitionFieldSetModel.getItems() == null){
                    propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                }
                if (!fieldsMap.isEmpty()) {
                    propertyDefinitionFieldSetItemModel.setFields(fieldsMap);
                    propertyDefinitionFieldSetItemModel.setOrdinal(propertyDefinitionFieldSetModel.getItems().size());
                    propertyDefinitionFieldSetModel.getItems().add(propertyDefinitionFieldSetItemModel);
                }

                fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
            }
        }

        return fieldSetModelMap;
    }

    Map<String, PropertyDefinitionFieldSetModel> buildFieldByRdaMetadata(DescriptionTemplateModel descriptionTemplateModel, List<Metadata> metadatas){
        Map<String, PropertyDefinitionFieldSetModel> fieldSetModelMap = new HashMap<>();

        if (descriptionTemplateModel == null) throw new MyApplicationException("description template is missing");

        if (metadatas == null) return fieldSetModelMap;

        List<FieldSetModel> fieldSetsWithSemantics = this.templateFieldSearcherService.searchFieldSetsBySemantics(descriptionTemplateModel, List.of(SEMANTIC_DATASET_METADATA_DESCRIPTION, SEMANTIC_DATASET_METADATA_LANGUAGE,
                SEMANTIC_DATASET_METADATA_STANDARD_ID_IDENTIFIER, SEMANTIC_DATASET_METADATA_STANDARD_ID_TYPE));
        if (!fieldSetsWithSemantics.isEmpty()){
            for (Metadata metadata: metadatas) {
                for (FieldSetModel templateFieldSetModel: fieldSetsWithSemantics) {
                    PropertyDefinitionFieldSetModel propertyDefinitionFieldSetModel = fieldSetModelMap.getOrDefault(templateFieldSetModel.getId(), null);
                    if(propertyDefinitionFieldSetModel == null) {
                        propertyDefinitionFieldSetModel = new PropertyDefinitionFieldSetModel();
                        propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                        fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
                    }

                    PropertyDefinitionFieldSetItemModel propertyDefinitionFieldSetItemModel = new PropertyDefinitionFieldSetItemModel();
                    Map<String, org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel> fieldsMap = new HashMap<>();
                    for (FieldModel templateFieldModel: templateFieldSetModel.getFields()) {
                        if (templateFieldModel.getSemantics() != null && !templateFieldModel.getSemantics().isEmpty()) {
                            boolean valueFound = false;
                            org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel fieldModel = new org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel();
                            fieldModel.setId(templateFieldModel.getId());
                            if (metadata.getDescription() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_METADATA_DESCRIPTION)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, metadata.getDescription(), null, null, null);
                                valueFound = true;
                            }
                            if (metadata.getLanguage() != null && metadata.getLanguage().value() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_METADATA_LANGUAGE)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, metadata.getLanguage().value(), null, null, null);
                                valueFound = true;
                            }
                            if (metadata.getMetadataStandardId() != null){
                                for(MetadataStandardId metadataStandardId: metadata.getMetadataStandardId()){
                                    if(metadataStandardId.getIdentifier()!= null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_METADATA_STANDARD_ID_IDENTIFIER)){
                                        fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, metadataStandardId.getIdentifier(), null, null, null);
                                        valueFound = true;
                                    }
                                }
                            }

                            if (metadata.getMetadataStandardId() != null ){
                                for(MetadataStandardId metadataStandardId: metadata.getMetadataStandardId()) {
                                    if(metadataStandardId.getType()!= null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_METADATA_STANDARD_ID_TYPE)) {
                                        fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, metadataStandardId.getType(), null, null, null);
                                        valueFound = true;
                                    }
                                }
                            }

                            if (valueFound) fieldsMap.put(templateFieldModel.getId(), fieldModel);
                        }
                    }
                    if (propertyDefinitionFieldSetModel.getItems() == null){
                        propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                    }
                    if (!fieldsMap.isEmpty()) {
                        propertyDefinitionFieldSetItemModel.setFields(fieldsMap);
                        propertyDefinitionFieldSetItemModel.setOrdinal(propertyDefinitionFieldSetModel.getItems().size());
                        propertyDefinitionFieldSetModel.getItems().add(propertyDefinitionFieldSetItemModel);
                    }

                    fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
                }
            }

        }

        return fieldSetModelMap;
    }

    Map<String, PropertyDefinitionFieldSetModel> buildFieldByRdaSecurityAndPrivacy(DescriptionTemplateModel descriptionTemplateModel, List<SecurityAndPrivacy> securityAndPrivacies){
        Map<String, PropertyDefinitionFieldSetModel> fieldSetModelMap = new HashMap<>();

        if (descriptionTemplateModel == null) throw new MyApplicationException("description template is missing");

        if (securityAndPrivacies == null) return fieldSetModelMap;

        List<FieldSetModel> fieldSetsWithSemantics = this.templateFieldSearcherService.searchFieldSetsBySemantics(descriptionTemplateModel, List.of(SEMANTIC_DATASET_SECURITY_AND_PRIVACY_TITLE, SEMANTIC_DATASET_SECURITY_AND_PRIVACY_DESCRIPTION));
        if (!fieldSetsWithSemantics.isEmpty()){
            for (SecurityAndPrivacy securityAndPrivacy: securityAndPrivacies) {
                for (FieldSetModel templateFieldSetModel: fieldSetsWithSemantics) {
                    PropertyDefinitionFieldSetModel propertyDefinitionFieldSetModel = fieldSetModelMap.getOrDefault(templateFieldSetModel.getId(), null);
                    if(propertyDefinitionFieldSetModel == null) {
                        propertyDefinitionFieldSetModel = new PropertyDefinitionFieldSetModel();
                        propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                        fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
                    }

                    PropertyDefinitionFieldSetItemModel propertyDefinitionFieldSetItemModel = new PropertyDefinitionFieldSetItemModel();
                    Map<String, org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel> fieldsMap = new HashMap<>();
                    for (FieldModel templateFieldModel: templateFieldSetModel.getFields()) {
                        if (templateFieldModel.getSemantics() != null && !templateFieldModel.getSemantics().isEmpty()) {
                            boolean valueFound = false;
                            org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel fieldModel = new org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel();
                            fieldModel.setId(templateFieldModel.getId());
                            if (securityAndPrivacy.getTitle() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_SECURITY_AND_PRIVACY_TITLE)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, securityAndPrivacy.getTitle(), null, null, null);
                                valueFound = true;
                            }
                            if (securityAndPrivacy.getDescription() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_SECURITY_AND_PRIVACY_DESCRIPTION)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, securityAndPrivacy.getDescription(), null, null, null);
                                valueFound = true;
                            }

                            if (valueFound) fieldsMap.put(templateFieldModel.getId(), fieldModel);
                        }
                    }
                    if (propertyDefinitionFieldSetModel.getItems() == null){
                        propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                    }
                    if (!fieldsMap.isEmpty()) {
                        propertyDefinitionFieldSetItemModel.setFields(fieldsMap);
                        propertyDefinitionFieldSetItemModel.setOrdinal(propertyDefinitionFieldSetModel.getItems().size());
                        propertyDefinitionFieldSetModel.getItems().add(propertyDefinitionFieldSetItemModel);
                    }

                    fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
                }
            }

        }

        return fieldSetModelMap;
    }

    Map<String, PropertyDefinitionFieldSetModel> buildFieldByRdaTechnicalResource(DescriptionTemplateModel descriptionTemplateModel, List<TechnicalResource> technicalResources){
        Map<String, PropertyDefinitionFieldSetModel> fieldSetModelMap = new HashMap<>();

        if (descriptionTemplateModel == null) throw new MyApplicationException("description template is missing");

        if (technicalResources == null) return fieldSetModelMap;

        List<FieldSetModel> fieldSetsWithSemantics = this.templateFieldSearcherService.searchFieldSetsBySemantics(descriptionTemplateModel, List.of(SEMANTIC_DATASET_TECHNICAL_RESOURCE_NAME, SEMANTIC_DATASET_TECHNICAL_RESOURCE_DESCRIPTION, SEMANTIC_DATASET_TECHNICAL_RESOURCE_ID_TYPE, SEMANTIC_DATASET_TECHNICAL_RESOURCE_ID_IDENTIFIER));
        if (!fieldSetsWithSemantics.isEmpty()){
            for (TechnicalResource technicalResource: technicalResources) {
                for (FieldSetModel templateFieldSetModel: fieldSetsWithSemantics) {
                    PropertyDefinitionFieldSetModel propertyDefinitionFieldSetModel = fieldSetModelMap.getOrDefault(templateFieldSetModel.getId(), null);
                    if(propertyDefinitionFieldSetModel == null) {
                        propertyDefinitionFieldSetModel = new PropertyDefinitionFieldSetModel();
                        propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                        fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
                    }

                    PropertyDefinitionFieldSetItemModel propertyDefinitionFieldSetItemModel = new PropertyDefinitionFieldSetItemModel();
                    Map<String, org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel> fieldsMap = new HashMap<>();
                    for (FieldModel templateFieldModel: templateFieldSetModel.getFields()) {
                        if (templateFieldModel.getSemantics() != null && !templateFieldModel.getSemantics().isEmpty()) {
                            boolean valueFound = false;
                            org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel fieldModel = new org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel();
                            fieldModel.setId(templateFieldModel.getId());
                            if (technicalResource.getName() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_TECHNICAL_RESOURCE_NAME)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, technicalResource.getName(), null, null, null);
                                valueFound = true;
                            }
                            if (technicalResource.getDescription() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_TECHNICAL_RESOURCE_DESCRIPTION)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, technicalResource.getDescription(), null, null, null);
                                valueFound = true;
                            }

                            for(TechnicalResourceId technicalResourceId: technicalResource.getTechnicalResourceId()){
                                if(technicalResource.getTechnicalResourceId() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_TECHNICAL_RESOURCE_ID_IDENTIFIER)){
                                    fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, technicalResourceId.getIdentifier(), null, null, null);
                                    valueFound = true;
                                }
                                if(technicalResource.getTechnicalResourceId() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_TECHNICAL_RESOURCE_ID_TYPE)){
                                    fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, technicalResourceId.getType(), null, null, null);
                                    valueFound = true;
                                }

                            }


                            if (valueFound) fieldsMap.put(templateFieldModel.getId(), fieldModel);
                        }
                    }
                    if (propertyDefinitionFieldSetModel.getItems() == null){
                        propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                    }
                    if (!fieldsMap.isEmpty()) {
                        propertyDefinitionFieldSetItemModel.setFields(fieldsMap);
                        propertyDefinitionFieldSetItemModel.setOrdinal(propertyDefinitionFieldSetModel.getItems().size());
                        propertyDefinitionFieldSetModel.getItems().add(propertyDefinitionFieldSetItemModel);
                    }

                    fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
                }
            }

        }

        return fieldSetModelMap;
    }

    Map<String, PropertyDefinitionFieldSetModel> buildFieldByRdaDatasetRelatedIdentifier(DescriptionTemplateModel descriptionTemplateModel, List<RelatedIdentifier> relatedIdentifiers){
        Map<String, PropertyDefinitionFieldSetModel> fieldSetModelMap = new HashMap<>();

        if (descriptionTemplateModel == null) throw new MyApplicationException("description template is missing");

        if (relatedIdentifiers == null) return fieldSetModelMap;

        List<FieldSetModel> fieldSetsWithSemantics = this.templateFieldSearcherService.searchFieldSetsBySemantics(descriptionTemplateModel, List.of(SEMANTIC_DATASET_RELATED_IDENTIFIER_IDENTIFIER,
                SEMANTIC_DATASET_RELATED_IDENTIFIER_RELATION_TYPE, SEMANTIC_DATASET_RELATED_IDENTIFIER_METADATA_SCHEME, SEMANTIC_DATASET_RELATED_IDENTIFIER_RESOURCE_TYPE, SEMANTIC_DATASET_RELATED_IDENTIFIER_SCHEME_URI,
                SEMANTIC_DATASET_RELATED_IDENTIFIER_SCHEME_TYPE, SEMANTIC_DATASET_RELATED_IDENTIFIER_TYPE ));

        if (!fieldSetsWithSemantics.isEmpty()){
            for (RelatedIdentifier relatedIdentifier: relatedIdentifiers) {
                for (FieldSetModel templateFieldSetModel: fieldSetsWithSemantics) {
                    PropertyDefinitionFieldSetModel propertyDefinitionFieldSetModel = fieldSetModelMap.getOrDefault(templateFieldSetModel.getId(), null);
                    if(propertyDefinitionFieldSetModel == null) {
                        propertyDefinitionFieldSetModel = new PropertyDefinitionFieldSetModel();
                        propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                        fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
                    }

                    PropertyDefinitionFieldSetItemModel propertyDefinitionFieldSetItemModel = new PropertyDefinitionFieldSetItemModel();
                    Map<String, org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel> fieldsMap = new HashMap<>();
                    for (FieldModel templateFieldModel: templateFieldSetModel.getFields()) {
                        if (templateFieldModel.getSemantics() != null && !templateFieldModel.getSemantics().isEmpty()) {
                            boolean valueFound = false;
                            org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel fieldModel = new org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel();
                            fieldModel.setId(templateFieldModel.getId());
                            if (relatedIdentifier.getIdentifier() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_RELATED_IDENTIFIER_IDENTIFIER)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, relatedIdentifier.getIdentifier(), null, null, null);
                                valueFound = true;
                            }

                            if (relatedIdentifier.getIdentifier() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_RELATED_IDENTIFIER_RELATION_TYPE)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, relatedIdentifier.getRelationType(), null, null, null);
                                valueFound = true;
                            }

                            if (relatedIdentifier.getIdentifier() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_RELATED_IDENTIFIER_METADATA_SCHEME)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, relatedIdentifier.getMetadataScheme(), null, null, null);
                                valueFound = true;
                            }

                            if (relatedIdentifier.getIdentifier() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_RELATED_IDENTIFIER_RESOURCE_TYPE)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, relatedIdentifier.getResourceType(), null, null, null);
                                valueFound = true;
                            }

                            if (relatedIdentifier.getIdentifier() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_RELATED_IDENTIFIER_SCHEME_URI) && relatedIdentifier.getSchemeUri() != null){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, relatedIdentifier.getSchemeUri().toString(), null, null, null);
                                valueFound = true;
                            }

                            if (relatedIdentifier.getIdentifier() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_RELATED_IDENTIFIER_SCHEME_TYPE)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, relatedIdentifier.getSchemeType(), null, null, null);
                                valueFound = true;
                            }

                            if (relatedIdentifier.getIdentifier() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_RELATED_IDENTIFIER_TYPE)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, relatedIdentifier.getType(), null, null, null);
                                valueFound = true;
                            }

                            if (valueFound) fieldsMap.put(templateFieldModel.getId(), fieldModel);
                        }
                    }
                    if (propertyDefinitionFieldSetModel.getItems() == null){
                        propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                    }
                    if (!fieldsMap.isEmpty()) {
                        propertyDefinitionFieldSetItemModel.setFields(fieldsMap);
                        propertyDefinitionFieldSetItemModel.setOrdinal(propertyDefinitionFieldSetModel.getItems().size());
                        propertyDefinitionFieldSetModel.getItems().add(propertyDefinitionFieldSetItemModel);
                    }

                    fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
                }
            }

        }

        return fieldSetModelMap;
    }

    Map<String, PropertyDefinitionFieldSetModel> buildFieldByRdaDmpRelatedIdentifier(DescriptionTemplateModel descriptionTemplateModel, List<RelatedIdentifier> relatedIdentifiers){
        Map<String, PropertyDefinitionFieldSetModel> fieldSetModelMap = new HashMap<>();

        if (descriptionTemplateModel == null) throw new MyApplicationException("description template is missing");

        if (relatedIdentifiers == null) return fieldSetModelMap;

        List<FieldSetModel> fieldSetsWithSemantics = this.templateFieldSearcherService.searchFieldSetsBySemantics(descriptionTemplateModel, List.of(SEMANTIC_DMP_RELATED_IDENTIFIER_IDENTIFIER,
                SEMANTIC_DMP_RELATED_IDENTIFIER_RELATION_TYPE, SEMANTIC_DMP_RELATED_IDENTIFIER_METADATA_SCHEME, SEMANTIC_DMP_RELATED_IDENTIFIER_RESOURCE_TYPE, SEMANTIC_DMP_RELATED_IDENTIFIER_SCHEME_URI,
                SEMANTIC_DMP_RELATED_IDENTIFIER_SCHEME_TYPE, SEMANTIC_DMP_RELATED_IDENTIFIER_TYPE ));

        if (!fieldSetsWithSemantics.isEmpty()){
            for (RelatedIdentifier relatedIdentifier: relatedIdentifiers) {
                for (FieldSetModel templateFieldSetModel: fieldSetsWithSemantics) {
                    PropertyDefinitionFieldSetModel propertyDefinitionFieldSetModel = fieldSetModelMap.getOrDefault(templateFieldSetModel.getId(), null);
                    if(propertyDefinitionFieldSetModel == null) {
                        propertyDefinitionFieldSetModel = new PropertyDefinitionFieldSetModel();
                        propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                        fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
                    }

                    PropertyDefinitionFieldSetItemModel propertyDefinitionFieldSetItemModel = new PropertyDefinitionFieldSetItemModel();
                    Map<String, org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel> fieldsMap = new HashMap<>();
                    for (FieldModel templateFieldModel: templateFieldSetModel.getFields()) {
                        if (templateFieldModel.getSemantics() != null && !templateFieldModel.getSemantics().isEmpty()) {
                            boolean valueFound = false;
                            org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel fieldModel = new org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel();
                            fieldModel.setId(templateFieldModel.getId());
                            if (relatedIdentifier.getIdentifier() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DMP_RELATED_IDENTIFIER_IDENTIFIER)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, relatedIdentifier.getIdentifier(), null, null, null);
                                valueFound = true;
                            }

                            if (relatedIdentifier.getIdentifier() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DMP_RELATED_IDENTIFIER_RELATION_TYPE)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, relatedIdentifier.getRelationType(), null, null, null);
                                valueFound = true;
                            }

                            if (relatedIdentifier.getIdentifier() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DMP_RELATED_IDENTIFIER_METADATA_SCHEME)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, relatedIdentifier.getMetadataScheme(), null, null, null);
                                valueFound = true;
                            }

                            if (relatedIdentifier.getIdentifier() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DMP_RELATED_IDENTIFIER_RESOURCE_TYPE)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, relatedIdentifier.getResourceType(), null, null, null);
                                valueFound = true;
                            }

                            if (relatedIdentifier.getIdentifier() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DMP_RELATED_IDENTIFIER_SCHEME_URI)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, relatedIdentifier.getSchemeUri().toString(), null, null, null);
                                valueFound = true;
                            }

                            if (relatedIdentifier.getIdentifier() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DMP_RELATED_IDENTIFIER_SCHEME_TYPE)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, relatedIdentifier.getSchemeType(), null, null, null);
                                valueFound = true;
                            }

                            if (relatedIdentifier.getIdentifier() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DMP_RELATED_IDENTIFIER_TYPE)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, relatedIdentifier.getType(), null, null, null);
                                valueFound = true;
                            }

                            if (valueFound) fieldsMap.put(templateFieldModel.getId(), fieldModel);
                        }
                    }
                    if (propertyDefinitionFieldSetModel.getItems() == null){
                        propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                    }
                    if (!fieldsMap.isEmpty()) {
                        propertyDefinitionFieldSetItemModel.setFields(fieldsMap);
                        propertyDefinitionFieldSetItemModel.setOrdinal(propertyDefinitionFieldSetModel.getItems().size());
                        propertyDefinitionFieldSetModel.getItems().add(propertyDefinitionFieldSetItemModel);
                    }

                    fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
                }
            }

        }

        return fieldSetModelMap;
    }

    Map<String, PropertyDefinitionFieldSetModel> buildFieldByRdaDatasetAlternateIdentifiers(DescriptionTemplateModel descriptionTemplateModel, List<AlternateIdentifier> alternateIdentifiers){
        Map<String, PropertyDefinitionFieldSetModel> fieldSetModelMap = new HashMap<>();

        if (descriptionTemplateModel == null) throw new MyApplicationException("description template is missing");

        if (alternateIdentifiers == null) return fieldSetModelMap;

        List<FieldSetModel> fieldSetsWithSemantics = this.templateFieldSearcherService.searchFieldSetsBySemantics(descriptionTemplateModel, List.of(SEMANTIC_DATASET_ALTERNATE_IDENTIFIER_TYPE, SEMANTIC_DATASET_ALTERNATE_IDENTIFIER_ID));

        if (!fieldSetsWithSemantics.isEmpty()){
            for (AlternateIdentifier alternateIdentifier: alternateIdentifiers) {
                // the export adds opencdmp_<id> itself to find the description again; it is not an answer
                if (alternateIdentifier.getIdentifier() != null && alternateIdentifier.getIdentifier().startsWith(DATASET_SYSTEM_ALTERNATE_IDENTIFIER_PREFIX)) continue;
                for (FieldSetModel templateFieldSetModel: fieldSetsWithSemantics) {
                    PropertyDefinitionFieldSetModel propertyDefinitionFieldSetModel = fieldSetModelMap.getOrDefault(templateFieldSetModel.getId(), null);
                    if(propertyDefinitionFieldSetModel == null) {
                        propertyDefinitionFieldSetModel = new PropertyDefinitionFieldSetModel();
                        propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                        fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
                    }

                    PropertyDefinitionFieldSetItemModel propertyDefinitionFieldSetItemModel = new PropertyDefinitionFieldSetItemModel();
                    Map<String, org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel> fieldsMap = new HashMap<>();
                    for (FieldModel templateFieldModel: templateFieldSetModel.getFields()) {
                        if (templateFieldModel.getSemantics() != null && !templateFieldModel.getSemantics().isEmpty()) {
                            boolean valueFound = false;
                            org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel fieldModel = new org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel();
                            fieldModel.setId(templateFieldModel.getId());
                            if (alternateIdentifier.getIdentifier() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_ALTERNATE_IDENTIFIER_ID)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, alternateIdentifier.getIdentifier(), null, null, null);
                                valueFound = true;
                            }
                            if (alternateIdentifier.getType() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_ALTERNATE_IDENTIFIER_TYPE)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, alternateIdentifier.getType(), null, null, null);
                                valueFound = true;
                            }

                            if (valueFound) fieldsMap.put(templateFieldModel.getId(), fieldModel);
                        }
                    }
                    if (propertyDefinitionFieldSetModel.getItems() == null){
                        propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                    }
                    if (!fieldsMap.isEmpty()) {
                        propertyDefinitionFieldSetItemModel.setFields(fieldsMap);
                        propertyDefinitionFieldSetItemModel.setOrdinal(propertyDefinitionFieldSetModel.getItems().size());
                        propertyDefinitionFieldSetModel.getItems().add(propertyDefinitionFieldSetItemModel);
                    }

                    fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
                }
            }

        }

        return fieldSetModelMap;
    }

    Map<String, PropertyDefinitionFieldSetModel> buildFieldByRdaDmpAffiliations(DescriptionTemplateModel descriptionTemplateModel, List<Affiliation> affiliations){
        Map<String, PropertyDefinitionFieldSetModel> fieldSetModelMap = new HashMap<>();

        if (descriptionTemplateModel == null) throw new MyApplicationException("description template is missing");

        if (affiliations == null) return fieldSetModelMap;

        List<FieldSetModel> fieldSetsWithSemantics = this.templateFieldSearcherService.searchFieldSetsBySemantics(descriptionTemplateModel, List.of(SEMANTIC_DMP_AFFILIATION_NAME, SEMANTIC_DMP_AFFILIATION_ID, SEMANTIC_DMP_AFFILIATION_ID_TYPE, SEMANTIC_DMP_AFFILIATION_ID_IDENTIFIER));

        if (!fieldSetsWithSemantics.isEmpty()){
            for (Affiliation affiliation: affiliations) {
                if (affiliation == null) continue;
                for (FieldSetModel templateFieldSetModel: fieldSetsWithSemantics) {
                    PropertyDefinitionFieldSetModel propertyDefinitionFieldSetModel = fieldSetModelMap.getOrDefault(templateFieldSetModel.getId(), null);
                    if(propertyDefinitionFieldSetModel == null) {
                        propertyDefinitionFieldSetModel = new PropertyDefinitionFieldSetModel();
                        propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                        fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
                    }

                    PropertyDefinitionFieldSetItemModel propertyDefinitionFieldSetItemModel = new PropertyDefinitionFieldSetItemModel();
                    Map<String, org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel> fieldsMap = new HashMap<>();
                    for (FieldModel templateFieldModel: templateFieldSetModel.getFields()) {
                        if (templateFieldModel.getSemantics() != null && !templateFieldModel.getSemantics().isEmpty()) {
                            boolean valueFound = false;
                            org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel fieldModel = new org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel();
                            fieldModel.setId(templateFieldModel.getId());

                            if (affiliation.getName() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DMP_AFFILIATION_NAME)) {
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, affiliation.getName(), null, null, null);
                                valueFound = true;
                            }
                            if (affiliation.getAffiliationId() != null) {
                                if (affiliation.getAffiliationId().getIdentifier() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DMP_AFFILIATION_ID)){
                                    fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, affiliation.getAffiliationId().getIdentifier(), null, null, null);
                                    valueFound = true;
                                }
                                if (affiliation.getAffiliationId().getIdentifier() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DMP_AFFILIATION_ID_IDENTIFIER)){
                                    fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, affiliation.getAffiliationId().getIdentifier(), null, null, null);
                                    valueFound = true;
                                }
                                if (affiliation.getAffiliationId().getType() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DMP_AFFILIATION_ID_TYPE)){
                                    fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, affiliation.getAffiliationId().getType(), null, null, null);
                                    valueFound = true;
                                }
                            }

                            if (valueFound) fieldsMap.put(templateFieldModel.getId(), fieldModel);
                        }
                    }
                    if (propertyDefinitionFieldSetModel.getItems() == null){
                        propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                    }
                    if (!fieldsMap.isEmpty()) {
                        propertyDefinitionFieldSetItemModel.setFields(fieldsMap);
                        propertyDefinitionFieldSetItemModel.setOrdinal(propertyDefinitionFieldSetModel.getItems().size());
                        propertyDefinitionFieldSetModel.getItems().add(propertyDefinitionFieldSetItemModel);
                    }

                    fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
                }
            }

        }

        return fieldSetModelMap;
    }

    Map<String, PropertyDefinitionFieldSetModel> buildFieldByRdaDmpAlternateIdentifiers(DescriptionTemplateModel descriptionTemplateModel, List<AlternateIdentifier> alternateIdentifiers){
        Map<String, PropertyDefinitionFieldSetModel> fieldSetModelMap = new HashMap<>();

        if (descriptionTemplateModel == null) throw new MyApplicationException("description template is missing");

        if (alternateIdentifiers == null) return fieldSetModelMap;

        List<FieldSetModel> fieldSetsWithSemantics = this.templateFieldSearcherService.searchFieldSetsBySemantics(descriptionTemplateModel, List.of(SEMANTIC_DMP_ALTERNATE_IDENTIFIER_TYPE, SEMANTIC_DMP_ALTERNATE_IDENTIFIER_ID));

        if (!fieldSetsWithSemantics.isEmpty()){
            for (AlternateIdentifier alternateIdentifier: alternateIdentifiers) {
                for (FieldSetModel templateFieldSetModel: fieldSetsWithSemantics) {
                    PropertyDefinitionFieldSetModel propertyDefinitionFieldSetModel = fieldSetModelMap.getOrDefault(templateFieldSetModel.getId(), null);
                    if(propertyDefinitionFieldSetModel == null) {
                        propertyDefinitionFieldSetModel = new PropertyDefinitionFieldSetModel();
                        propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                        fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
                    }

                    PropertyDefinitionFieldSetItemModel propertyDefinitionFieldSetItemModel = new PropertyDefinitionFieldSetItemModel();
                    Map<String, org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel> fieldsMap = new HashMap<>();
                    for (FieldModel templateFieldModel: templateFieldSetModel.getFields()) {
                        if (templateFieldModel.getSemantics() != null && !templateFieldModel.getSemantics().isEmpty()) {
                            boolean valueFound = false;
                            org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel fieldModel = new org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel();
                            fieldModel.setId(templateFieldModel.getId());
                            if (alternateIdentifier.getIdentifier() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DMP_ALTERNATE_IDENTIFIER_ID)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, alternateIdentifier.getIdentifier(), null, null, null);
                                valueFound = true;
                            }
                            if (alternateIdentifier.getType() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DMP_ALTERNATE_IDENTIFIER_TYPE)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, alternateIdentifier.getType(), null, null, null);
                                valueFound = true;
                            }

                            if (valueFound) fieldsMap.put(templateFieldModel.getId(), fieldModel);
                        }
                    }
                    if (propertyDefinitionFieldSetModel.getItems() == null){
                        propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                    }
                    if (!fieldsMap.isEmpty()) {
                        propertyDefinitionFieldSetItemModel.setFields(fieldsMap);
                        propertyDefinitionFieldSetItemModel.setOrdinal(propertyDefinitionFieldSetModel.getItems().size());
                        propertyDefinitionFieldSetModel.getItems().add(propertyDefinitionFieldSetItemModel);
                    }

                    fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
                }
            }

        }

        return fieldSetModelMap;
    }

    Map<String, PropertyDefinitionFieldSetModel> buildFieldByRdaCost(DescriptionTemplateModel descriptionTemplateModel, List<Cost> costs){
        Map<String, PropertyDefinitionFieldSetModel> fieldSetModelMap = new HashMap<>();

        if (descriptionTemplateModel == null) throw new MyApplicationException("description template is missing");

        if (costs == null) return fieldSetModelMap;

        List<FieldSetModel> fieldSetsWithSemantics = this.templateFieldSearcherService.searchFieldSetsBySemantics(descriptionTemplateModel, List.of(SEMANTIC_COST_CURRENCY_CODE, SEMANTIC_COST_TITLE,
                SEMANTIC_COST_VALUE, SEMANTIC_COST_DESCRIPTION));
        if (!fieldSetsWithSemantics.isEmpty()){
            for (Cost cost: costs) {
                for (FieldSetModel templateFieldSetModel: fieldSetsWithSemantics) {
                    PropertyDefinitionFieldSetModel propertyDefinitionFieldSetModel = fieldSetModelMap.getOrDefault(templateFieldSetModel.getId(), null);
                    if(propertyDefinitionFieldSetModel == null) {
                        propertyDefinitionFieldSetModel = new PropertyDefinitionFieldSetModel();
                        propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                        fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
                    }

                    PropertyDefinitionFieldSetItemModel propertyDefinitionFieldSetItemModel = new PropertyDefinitionFieldSetItemModel();
                    Map<String, org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel> fieldsMap = new HashMap<>();
                    for (FieldModel templateFieldModel: templateFieldSetModel.getFields()) {
                        if (templateFieldModel.getSemantics() != null && !templateFieldModel.getSemantics().isEmpty()) {
                            boolean valueFound = false;
                            org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel fieldModel = new org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel();
                            fieldModel.setId(templateFieldModel.getId());
                            if (cost.getCurrencyCode() != null && templateFieldModel.getSemantics().contains(SEMANTIC_COST_CURRENCY_CODE)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, cost.getCurrencyCode().value(), null, null, null);
                                valueFound = true;
                            }
                            if (cost.getTitle() != null && templateFieldModel.getSemantics().contains(SEMANTIC_COST_TITLE)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, cost.getTitle(), null, null, null);
                                valueFound = true;
                            }
                            if (cost.getDescription() != null && templateFieldModel.getSemantics().contains(SEMANTIC_COST_DESCRIPTION)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, cost.getDescription(), null, null, null);
                                valueFound = true;
                            }
                            if (cost.getValue() != null && templateFieldModel.getSemantics().contains(SEMANTIC_COST_VALUE)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, null, null, null, null, cost.getValue());
                                valueFound = true;
                            }

                            if (valueFound) fieldsMap.put(templateFieldModel.getId(), fieldModel);
                        }
                    }
                    if (propertyDefinitionFieldSetModel.getItems() == null){
                        propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                    }
                    if (!fieldsMap.isEmpty()) {
                        propertyDefinitionFieldSetItemModel.setFields(fieldsMap);
                        propertyDefinitionFieldSetItemModel.setOrdinal(propertyDefinitionFieldSetModel.getItems().size());
                        propertyDefinitionFieldSetModel.getItems().add(propertyDefinitionFieldSetItemModel);
                    }

                    fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
                }
            }

        }

        return fieldSetModelMap;
    }

    Map<String, PropertyDefinitionFieldSetModel> buildFieldByRdaContributor(DescriptionTemplateModel descriptionTemplateModel, List<Contributor> contributors){
        Map<String, PropertyDefinitionFieldSetModel> fieldSetModelMap = new HashMap<>();

        if (descriptionTemplateModel == null) throw new MyApplicationException("description template is missing");

        if (contributors == null) return fieldSetModelMap;

        List<FieldSetModel> fieldSetsWithSemantics = this.templateFieldSearcherService.searchFieldSetsBySemantics(descriptionTemplateModel, List.of(SEMANTIC_DATASET_CONTRIBUTOR, SEMANTIC_DATASET_CONTRIBUTOR_CONTRIBUTOR_ID_IDENTIFIER,
                SEMANTIC_DATASET_CONTRIBUTOR_CONTRIBUTOR_ID_TYPE, SEMANTIC_DATASET_CONTRIBUTOR_MBOX, SEMANTIC_DATASET_CONTRIBUTOR_NAME, SEMANTIC_DATASET_CONTRIBUTOR_ROLE,
                SEMANTIC_DATASET_CONTRIBUTOR_AFFILIATION_ID_TYPE,SEMANTIC_DATASET_CONTRIBUTOR_AFFILIATION_ID_IDENTIFIER, SEMANTIC_DATASET_CONTRIBUTOR_AFFILIATION_NAME));
        if (!fieldSetsWithSemantics.isEmpty()){
            for (Contributor contributor: contributors) {
                for (FieldSetModel templateFieldSetModel: fieldSetsWithSemantics) {
                    PropertyDefinitionFieldSetModel propertyDefinitionFieldSetModel = fieldSetModelMap.getOrDefault(templateFieldSetModel.getId(), null);
                    if(propertyDefinitionFieldSetModel == null) {
                        propertyDefinitionFieldSetModel = new PropertyDefinitionFieldSetModel();
                        propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                        fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
                    }

                    PropertyDefinitionFieldSetItemModel propertyDefinitionFieldSetItemModel = new PropertyDefinitionFieldSetItemModel();
                    Map<String, org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel> fieldsMap = new HashMap<>();
                    // one contributor, one form: with an identifier and no typed identifier input it becomes a picked reference,
                    // otherwise it fills the typed inputs (writing both would export the contributor twice)
                    boolean asReference = contributor.getContributorId() != null && !contributor.getContributorId().isEmpty()
                            && this.asksForAny(templateFieldSetModel, List.of(SEMANTIC_DATASET_CONTRIBUTOR))
                            && !this.asksForAny(templateFieldSetModel, List.of(SEMANTIC_DATASET_CONTRIBUTOR_CONTRIBUTOR_ID_IDENTIFIER));
                    for (FieldModel templateFieldModel: templateFieldSetModel.getFields()) {
                        if (templateFieldModel.getSemantics() != null && !templateFieldModel.getSemantics().isEmpty()){
                            if (asReference && !templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_CONTRIBUTOR)) continue;
                            boolean valueFound = false;
                            org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel fieldModel = new org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel();
                            fieldModel.setId(templateFieldModel.getId());

                            if (asReference && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_CONTRIBUTOR)){
                                ReferenceModel referenceModel = new ReferenceModel();
                                referenceModel.setReference(contributor.getContributorId().getFirst().getIdentifier());
                                referenceModel.setLabel(contributor.getName());
                                if (templateFieldModel.getData() instanceof ReferenceTypeDataModel referenceTypeDataModel) referenceModel.setType(referenceTypeDataModel.getReferenceType());
                                fieldModel.setReferences(new ArrayList<>());
                                fieldModel.getReferences().add(referenceModel);
                                valueFound = true;
                            }

                            if (contributor.getContributorId() != null && !contributor.getContributorId().isEmpty()){
                                for(ContributorId contributorId : contributor.getContributorId()){

                                    if (contributorId.getIdentifier() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_CONTRIBUTOR_CONTRIBUTOR_ID_IDENTIFIER)){
                                        fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, contributorId.getIdentifier(), null, null, null);
                                        valueFound = true;
                                    }

                                    if (contributorId.getType() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_CONTRIBUTOR_CONTRIBUTOR_ID_TYPE)){
                                        fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, contributorId.getType(), null, null, null);
                                        valueFound = true;
                                    }

                                }

                            }


                            if (contributor.getAffiliation() != null  && !contributor.getAffiliation().isEmpty()){
                                for(Affiliation affiliation : contributor.getAffiliation()){
                                    if (affiliation.getName() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_CONTRIBUTOR_AFFILIATION_NAME)){
                                        fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, affiliation.getName(), null, null, null);
                                        valueFound = true;
                                    }

                                    if (affiliation.getAffiliationId() != null && affiliation.getAffiliationId().getIdentifier() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_CONTRIBUTOR_AFFILIATION_ID_IDENTIFIER)){
                                        fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, affiliation.getAffiliationId().getIdentifier(), null, null, null);
                                        valueFound = true;
                                    }

                                    if (affiliation.getAffiliationId() != null && affiliation.getAffiliationId().getType() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_CONTRIBUTOR_AFFILIATION_ID_TYPE)){
                                        fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, affiliation.getAffiliationId().getType(), null, null, null);
                                        valueFound = true;
                                    }

                                }
                            }


                            if (contributor.getName() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_CONTRIBUTOR_NAME)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, contributor.getName(), null, null, null);
                                valueFound = true;
                            }
                            if (contributor.getMbox() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_CONTRIBUTOR_MBOX)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, contributor.getMbox(), null, null, null);
                                valueFound = true;
                            }
                            if (contributor.getRole() != null && !contributor.getRole().isEmpty() && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_CONTRIBUTOR_ROLE)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, null, null, null, contributor.getRole().stream().toList());
                                valueFound = true;
                            }
                            if (valueFound) fieldsMap.put(templateFieldModel.getId(), fieldModel);
                        }
                    }

                    if (propertyDefinitionFieldSetModel.getItems() == null){
                        propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                    }
                    if (!fieldsMap.isEmpty()) {
                        propertyDefinitionFieldSetItemModel.setFields(fieldsMap);
                        propertyDefinitionFieldSetItemModel.setOrdinal(propertyDefinitionFieldSetModel.getItems().size());
                        propertyDefinitionFieldSetModel.getItems().add(propertyDefinitionFieldSetItemModel);
                    }

                    fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
                }
            }

        }

        return fieldSetModelMap;
    }

    Map<String, PropertyDefinitionFieldSetModel> buildFieldByRdaCreator(DescriptionTemplateModel descriptionTemplateModel, List<Creator> creators){
        Map<String, PropertyDefinitionFieldSetModel> fieldSetModelMap = new HashMap<>();

        if (descriptionTemplateModel == null) throw new MyApplicationException("description template is missing");

        if (creators == null) return fieldSetModelMap;

        List<FieldSetModel> fieldSetsWithSemantics = this.templateFieldSearcherService.searchFieldSetsBySemantics(descriptionTemplateModel, List.of(SEMANTIC_DATASET_CREATOR_NAME, SEMANTIC_DATASET_CREATOR_MBOX,
                SEMANTIC_DATASET_CREATOR_ID, SEMANTIC_DATASET_CREATOR_ID_IDENTIFIER, SEMANTIC_DATASET_CREATOR_ID_TYPE, SEMANTIC_DATASET_CREATOR_AFFILIATION_NAME, SEMANTIC_DATASET_CREATOR_AFFILIATION_ID_TYPE, SEMANTIC_DATASET_CREATOR_AFFILIATION_ID_IDENTIFIER));
        if (!fieldSetsWithSemantics.isEmpty()){
            for (Creator creator: creators) {
                for (FieldSetModel templateFieldSetModel: fieldSetsWithSemantics) {
                    PropertyDefinitionFieldSetModel propertyDefinitionFieldSetModel = fieldSetModelMap.getOrDefault(templateFieldSetModel.getId(), null);
                    if(propertyDefinitionFieldSetModel == null) {
                        propertyDefinitionFieldSetModel = new PropertyDefinitionFieldSetModel();
                        propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                        fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
                    }

                    PropertyDefinitionFieldSetItemModel propertyDefinitionFieldSetItemModel = new PropertyDefinitionFieldSetItemModel();
                    Map<String, org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel> fieldsMap = new HashMap<>();
                    for (FieldModel templateFieldModel: templateFieldSetModel.getFields()) {
                        if (templateFieldModel.getSemantics() != null && !templateFieldModel.getSemantics().isEmpty()){
                            boolean valueFound = false;
                            org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel fieldModel = new org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel();
                            fieldModel.setId(templateFieldModel.getId());



                            if (creator.getName() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_CREATOR_NAME)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, creator.getName(), null, null, null);
                                valueFound = true;
                            }
                            if (creator.getMbox() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_CREATOR_MBOX)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, creator.getMbox(), null, null, null);
                                valueFound = true;
                            }

                            if (creator.getCreatorId() != null && !creator.getCreatorId().isEmpty()){
                                for(CreatorId creatorId: creator.getCreatorId()){
                                    if (creatorId.getIdentifier() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_CREATOR_ID_IDENTIFIER)){
                                        fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, creatorId.getIdentifier(), null, null, null);
                                        valueFound = true;
                                    }

                                    if (creatorId.getType() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_CREATOR_ID_TYPE)){
                                        fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, creatorId.getType(), null, null, null);
                                        valueFound = true;
                                    }
                                }
                            }

                            if (creator.getAffiliation() != null && !creator.getAffiliation().isEmpty()){
                                for(Affiliation affiliation: creator.getAffiliation()){
                                    if (affiliation.getName() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_CREATOR_AFFILIATION_NAME)){
                                        fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, affiliation.getName(), null, null, null);
                                        valueFound = true;
                                    }

                                    if (affiliation.getAffiliationId() != null &&  affiliation.getAffiliationId().getIdentifier() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_CREATOR_AFFILIATION_ID_IDENTIFIER)){
                                        fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, affiliation.getAffiliationId().getIdentifier(), null, null, null);
                                        valueFound = true;
                                    }

                                    if (affiliation.getAffiliationId() != null &&  affiliation.getAffiliationId().getType() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_CREATOR_AFFILIATION_ID_TYPE)){
                                        fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, affiliation.getAffiliationId().getType(), null, null, null);
                                        valueFound = true;
                                    }
                                }
                            }

                            if (valueFound) fieldsMap.put(templateFieldModel.getId(), fieldModel);
                        }
                    }
                    if (propertyDefinitionFieldSetModel.getItems() == null){
                        propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                    }
                    if (!fieldsMap.isEmpty()) {
                        propertyDefinitionFieldSetItemModel.setFields(fieldsMap);
                        propertyDefinitionFieldSetItemModel.setOrdinal(propertyDefinitionFieldSetModel.getItems().size());
                        propertyDefinitionFieldSetModel.getItems().add(propertyDefinitionFieldSetItemModel);
                    }

                    fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
                }
            }

        }

        return fieldSetModelMap;
    }

    Map<String, PropertyDefinitionFieldSetModel> buildFieldByRdaDistribution(DescriptionTemplateModel descriptionTemplateModel, List<Distribution> distributions){
        Map<String, PropertyDefinitionFieldSetModel> fieldSetModelMap = new HashMap<>();

        if (descriptionTemplateModel == null) throw new MyApplicationException("description template is missing");

        if (distributions == null) return fieldSetModelMap;

        List<FieldSetModel> fieldSetsWithSemantics = this.templateFieldSearcherService.searchFieldSetsBySemantics(descriptionTemplateModel, List.of(
                SEMANTIC_DATASET_DISTRIBUTION,
                SEMANTIC_DATASET_DISTRIBUTION_DESCRIPTION, SEMANTIC_DATASET_DISTRIBUTION_TITLE,
                SEMANTIC_DATASET_DISTRIBUTION_DATA_ACCESS, SEMANTIC_DATASET_DISTRIBUTION_BYTE_SIZE, SEMANTIC_DATASET_DISTRIBUTION_ACCESS_URL, SEMANTIC_DATASET_DISTRIBUTION_AVAILABLE_UTIL, SEMANTIC_DATASET_DISTRIBUTION_DOWNLOAD_URL,
                SEMANTIC_DATASET_DISTRIBUTION_LICENCE, SEMANTIC_DATASET_DISTRIBUTION_LICENCE_LICENSE_REF, SEMANTIC_DATASET_DISTRIBUTION_LICENCE_START_DATE,
                SEMANTIC_DATASET_DISTRIBUTION_HOST,
                SEMANTIC_DATASET_DISTRIBUTION_HOST_AVAILABILITY, SEMANTIC_DATASET_DISTRIBUTION_HOST_BACKUP_FREQUENCY, SEMANTIC_DATASET_DISTRIBUTION_HOST_BACKUP_TYPE,
                SEMANTIC_DATASET_DISTRIBUTION_HOST_CERTIFIED_WITH, SEMANTIC_DATASET_DISTRIBUTION_HOST_DESCRIPTION, SEMANTIC_DATASET_DISTRIBUTION_HOST_PID_SYSTEM, SEMANTIC_DATASET_DISTRIBUTION_HOST_STORAGE_TYPE,
                SEMANTIC_DATASET_DISTRIBUTION_HOST_TITLE, SEMANTIC_DATASET_DISTRIBUTION_HOST_URL, SEMANTIC_DATASET_DISTRIBUTION_HOST_GEO_LOCATION, SEMANTIC_DATASET_DISTRIBUTION_HOST_SUPPORT_VERSIONING, SEMANTIC_DATASET_DISTRIBUTION_HOST_ID_IDENTIFIER, SEMANTIC_DATASET_DISTRIBUTION_HOST_ID_TYPE,
                SEMANTIC_DATASET_DISTRIBUTION_FORMAT,SEMANTIC_DATASET_DISTRIBUTION_ISSUED, SEMANTIC_DATASET_DISTRIBUTION_EXISTS));
        fieldSetsWithSemantics = this.withAnswerOptionsFor(descriptionTemplateModel.getDefinition().getSections(), fieldSetsWithSemantics, SEMANTIC_DATASET_DISTRIBUTION);
        Set<String> keysOnInputs = keysOnInputs(descriptionTemplateModel.getDefinition().getSections());
        if (!fieldSetsWithSemantics.isEmpty()){
            for (Distribution distribution: distributions) {
                Map<String, String> distributionValues = this.flatten(distribution, SEMANTIC_DATASET_DISTRIBUTION);
                for (FieldSetModel templateFieldSetModel: fieldSetsWithSemantics) {
                    PropertyDefinitionFieldSetModel propertyDefinitionFieldSetModel = fieldSetModelMap.getOrDefault(templateFieldSetModel.getId(), null);
                    if(propertyDefinitionFieldSetModel == null) {
                        propertyDefinitionFieldSetModel = new PropertyDefinitionFieldSetModel();
                        propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                        fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
                    }

                    if (templateFieldSetModel.getSemantics() != null && templateFieldSetModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_LICENCE)) {
                        // a question describing a licence: one answer per licence of the distribution
                        for (License license : distribution.getLicense() == null ? List.<License>of() : distribution.getLicense()) {
                            Map<String, org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel> licenceFields = new HashMap<>();
                            for (FieldModel templateFieldModel : templateFieldSetModel.getFields()) {
                                if (templateFieldModel.getSemantics() == null) continue;
                                org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel licenceField = new org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel();
                                licenceField.setId(templateFieldModel.getId());
                                if (license.getLicenseRef() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_LICENCE_LICENSE_REF)) {
                                    licenceFields.put(templateFieldModel.getId(), this.buildFieldModel(licenceField, templateFieldModel, license.getLicenseRef().toString(), null, null, null));
                                } else if (license.getStartDate() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_LICENCE_START_DATE)) {
                                    licenceFields.put(templateFieldModel.getId(), this.buildFieldModel(licenceField, templateFieldModel, license.getStartDate(), null, null, null));
                                }
                            }
                            if (licenceFields.isEmpty()) continue;
                            PropertyDefinitionFieldSetItemModel licenceItem = new PropertyDefinitionFieldSetItemModel();
                            licenceItem.setFields(licenceFields);
                            licenceItem.setOrdinal(propertyDefinitionFieldSetModel.getItems().size());
                            propertyDefinitionFieldSetModel.getItems().add(licenceItem);
                        }
                        continue;
                    }

                    PropertyDefinitionFieldSetItemModel propertyDefinitionFieldSetItemModel = new PropertyDefinitionFieldSetItemModel();
                    Map<String, org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel> fieldsMap = new HashMap<>();
                    for (FieldModel templateFieldModel: templateFieldSetModel.getFields()) {
                        String chosenOption = chooseOption(templateFieldModel, distributionValues, keysOnInputs);
                        if (chosenOption != null) {
                            // the answer whose key values match this distribution's (e.g. "Zenodo" for its host)
                            org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel chosen = new org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel();
                            chosen.setId(templateFieldModel.getId());
                            fieldsMap.put(templateFieldModel.getId(), this.buildFieldModel(chosen, templateFieldModel, chosenOption, null, null, null));
                            continue;
                        }
                        if (templateFieldModel.getSemantics() != null && !templateFieldModel.getSemantics().isEmpty()){
                            boolean valueFound = false;
                            org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel fieldModel = new org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel();
                            fieldModel.setId(templateFieldModel.getId());

                            // a whole distribution picked as a reference: named after the distribution, else after its host
                            String distributionLabel = distribution.getTitle() != null ? distribution.getTitle()
                                    : distribution.getHost() != null ? distribution.getHost().getTitle() : null;
                            if (distributionLabel != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, distributionLabel, null, null, null);
                                valueFound = true;
                            }

                            // semantic SEMANTIC_DATASET_DISTRIBUTION_EXISTS is not part of the distribution
                            if (templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_EXISTS)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, null, null, true, null);
                                valueFound = true;
                            }

                            if (distribution.getDescription() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_DESCRIPTION)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, distribution.getDescription(), null, null, null);
                                valueFound = true;
                            }
                            if (distribution.getTitle() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_TITLE)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, distribution.getTitle(), null, null, null);
                                valueFound = true;
                            }
                            if (distribution.getDataAccess() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_DATA_ACCESS)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, dataAccessAnswer(templateFieldModel, distribution.getDataAccess()), null, null, null);
                                valueFound = true;
                            }
                            if (distribution.getByteSize() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_BYTE_SIZE)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, distribution.getByteSize().toString(), null, null, null);
                                valueFound = true;
                            }
                            if (distribution.getAccessUrl() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_ACCESS_URL)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, distribution.getAccessUrl().toString(), null, null, null);
                                valueFound = true;
                            }
                            if (distribution.getAvailableUntil() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_AVAILABLE_UTIL)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, null, distribution.getAvailableUntil(), null, null);
                                valueFound = true;
                            }
                            if (distribution.getIssued() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_ISSUED)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, null, distribution.getIssued(), null, null);
                                valueFound = true;
                            }
                            if (distribution.getDownloadUrl() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_DOWNLOAD_URL)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, distribution.getDownloadUrl().toString(), null, null, null);
                                valueFound = true;
                            }
                            if (distribution.getLicense() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_LICENCE)){
                                // every licence with a licence URL becomes one picked reference
                                List<ReferenceModel> licenceReferences = new ArrayList<>();
                                for (License license: distribution.getLicense()) {
                                    if (license.getLicenseRef() == null) continue;
                                    ReferenceModel referenceModel = new ReferenceModel();
                                    referenceModel.setReference(license.getLicenseRef().toString());
                                    referenceModel.setLabel(license.getLicenseRef().toString());
                                    if (templateFieldModel.getData() instanceof ReferenceTypeDataModel referenceTypeDataModel) referenceModel.setType(referenceTypeDataModel.getReferenceType());
                                    licenceReferences.add(referenceModel);
                                }
                                if (!licenceReferences.isEmpty()) {
                                    fieldModel.setReferences(licenceReferences);
                                    valueFound = true;
                                }
                            }

                            if (distribution.getHost() != null  && (templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_HOST))){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, distribution.getHost().getTitle(), null, null, null);

                                if (fieldModel != null && fieldModel.getReferences() != null && !fieldModel.getReferences().isEmpty()) {
                                    if (distribution.getHost().getHostId() != null && !distribution.getHost().getHostId().isEmpty()) fieldModel.getReferences().getFirst().setReference(distribution.getHost().getHostId().getFirst().getIdentifier());
                                    if (distribution.getHost().getTitle() != null) fieldModel.getReferences().getFirst().setLabel(distribution.getHost().getTitle());
                                }

                                valueFound = true;
                            }

                            if (distribution.getHost() != null && distribution.getHost().getAvailability() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_HOST_AVAILABILITY)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, distribution.getHost().getAvailability(), null, null, null);
                                valueFound = true;
                            }
                            if (distribution.getHost() != null && distribution.getHost().getBackupFrequency() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_HOST_BACKUP_FREQUENCY)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, distribution.getHost().getBackupFrequency(), null, null, null);
                                valueFound = true;
                            }
                            if (distribution.getHost() != null && distribution.getHost().getBackupType() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_HOST_BACKUP_TYPE)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, distribution.getHost().getBackupType(), null, null, null);
                                valueFound = true;
                            }
                            if (distribution.getHost() != null && distribution.getHost().getCertifiedWith() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_HOST_CERTIFIED_WITH)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel,distribution.getHost().getCertifiedWith().value(), null, null, null);
                                valueFound = true;
                            }
                            if (distribution.getHost() != null && distribution.getHost().getDescription() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_HOST_DESCRIPTION)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, distribution.getHost().getDescription(), null, null, null);
                                valueFound = true;
                            }
                            if (distribution.getHost() != null && distribution.getHost().getPidSystem() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_HOST_PID_SYSTEM)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, null, null, null, distribution.getHost().getPidSystem());
                                valueFound = true;
                            }
                            if (distribution.getHost() != null && distribution.getHost().getStorageType() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_HOST_STORAGE_TYPE)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, distribution.getHost().getStorageType(), null, null, null);
                                valueFound = true;
                            }
                            if (distribution.getHost() != null && distribution.getHost().getTitle() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_HOST_TITLE)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, distribution.getHost().getTitle(), null, null, null);
                                valueFound = true;
                            }
                            if (distribution.getHost() != null && distribution.getHost().getUrl() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_HOST_URL)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, distribution.getHost().getUrl().toString(), null, null, null);
                                valueFound = true;
                            }
                            if (distribution.getHost() != null && distribution.getHost().getGeoLocation() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_HOST_GEO_LOCATION)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, distribution.getHost().getGeoLocation().value(), null, null, null);
                                valueFound = true;
                            }
                            if (distribution.getFormat() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_FORMAT)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, null, null, null, distribution.getFormat());
                                valueFound = true;
                            }
                            if (distribution.getHost() != null && distribution.getHost().getSupportVersioning() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_HOST_SUPPORT_VERSIONING)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel,distribution.getHost().getSupportVersioning().value(), null, null, null);
                                valueFound = true;
                            }

                            if (distribution.getHost() != null && distribution.getHost().getHostId() != null){
                                for(HostId hostId: distribution.getHost().getHostId()){
                                    if (hostId.getIdentifier() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_HOST_ID_IDENTIFIER)){
                                        fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, hostId.getIdentifier(), null, null, null);
                                        valueFound = true;
                                    }

                                    if (hostId.getType() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_HOST_ID_TYPE)){
                                        fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, hostId.getType(), null, null, null);
                                        valueFound = true;
                                    }
                                }
                            }

                            if (distribution.getLicense() != null && !distribution.getLicense().isEmpty()){
                                // one answer has one licence URL and one start date input: the first licence fills them
                                License license = distribution.getLicense().getFirst();
                                if (license.getLicenseRef() != null  && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_LICENCE_LICENSE_REF)) {
                                    fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, license.getLicenseRef().toString(), null, null, null);
                                    valueFound = true;
                                }
                                if (license.getStartDate() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_LICENCE_START_DATE)) {
                                    fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, license.getStartDate(), null, null, null);
                                    valueFound = true;
                                }
                            }

                            if (valueFound) fieldsMap.put(templateFieldModel.getId(), fieldModel);
                        }
                    }
                    if (propertyDefinitionFieldSetModel.getItems() == null){
                        propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                    }
                    if (!fieldsMap.isEmpty()) {
                        propertyDefinitionFieldSetItemModel.setFields(fieldsMap);
                        propertyDefinitionFieldSetItemModel.setOrdinal(propertyDefinitionFieldSetModel.getItems().size());
                        propertyDefinitionFieldSetModel.getItems().add(propertyDefinitionFieldSetItemModel);
                    }

                    fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
                }
            }

        }

        return fieldSetModelMap;
    }

    /** shared and restricted mean the same since RDA 1.3 deprecated shared: answer with the one the input offers. */
    private static String dataAccessAnswer(FieldModel templateFieldModel, Distribution.DataAccess dataAccess) {
        if (dataAccess != Distribution.DataAccess.SHARED && dataAccess != Distribution.DataAccess.RESTRICTED) return dataAccess.value();
        List<String> options = answerOptions(templateFieldModel).stream().map(AnswerOption::value).toList();
        String other = dataAccess == Distribution.DataAccess.SHARED ? Distribution.DataAccess.RESTRICTED.value() : Distribution.DataAccess.SHARED.value();
        return !options.contains(dataAccess.value()) && options.contains(other) ? other : dataAccess.value();
    }

    Map<String, PropertyDefinitionFieldSetModel> buildFieldByRdaProject(DescriptionTemplateModel descriptionTemplateModel, List<Project> projects){
        Map<String, PropertyDefinitionFieldSetModel> fieldSetModelMap = new HashMap<>();

        if (descriptionTemplateModel == null) throw new MyApplicationException("description template is missing");

        if (projects == null) return fieldSetModelMap;

        List<FieldSetModel> fieldSetsWithSemantics = this.templateFieldSearcherService.searchFieldSetsBySemantics(descriptionTemplateModel, List.of(SEMANTIC_PROJECT_FUNDING_GRANT_ID, SEMANTIC_PROJECT_FUNDING_GRANT_ID_IDENTIFIER,
                SEMANTIC_PROJECT_FUNDING_FUNDER_ID, SEMANTIC_PROJECT_FUNDING_FUNDER_ID_IDENTIFIER, SEMANTIC_PROJECT_FUNDING_FUNDER_ID_TYPE, SEMANTIC_PROJECT_FUNDING_FUNDING_STATUS, SEMANTIC_PROJECT_FUNDING_GRANT_ID_TYPE,
                SEMANTIC_PROJECT_START, SEMANTIC_PROJECT_END, SEMANTIC_PROJECT_TITLE, SEMANTIC_PROJECT_DESCRIPTION, SEMANTIC_PROJECT_ID_IDENTIFIER, SEMANTIC_PROJECT_ID_TYPE));
        if (!fieldSetsWithSemantics.isEmpty()){
            for (Project project: projects) {
                for (FieldSetModel templateFieldSetModel: fieldSetsWithSemantics) {
                    PropertyDefinitionFieldSetModel propertyDefinitionFieldSetModel = fieldSetModelMap.getOrDefault(templateFieldSetModel.getId(), null);
                    if(propertyDefinitionFieldSetModel == null) {
                        propertyDefinitionFieldSetModel = new PropertyDefinitionFieldSetModel();
                        propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                        fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
                    }

                    PropertyDefinitionFieldSetItemModel propertyDefinitionFieldSetItemModel = new PropertyDefinitionFieldSetItemModel();
                    Map<String, org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel> fieldsMap = new HashMap<>();
                    for (FieldModel templateFieldModel: templateFieldSetModel.getFields()) {
                        if (templateFieldModel.getSemantics() != null && !templateFieldModel.getSemantics().isEmpty()){
                            boolean valueFound = false;
                            org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel fieldModel = new org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel();
                            fieldModel.setId(templateFieldModel.getId());

                            if (project.getFunding() != null && !project.getFunding().isEmpty()) {
                                for (Funding funding: project.getFunding()) {

                                    if (funding.getFunderId() != null) {
                                        if (funding.getFunderId().getIdentifier() != null && templateFieldModel.getSemantics().contains(SEMANTIC_PROJECT_FUNDING_FUNDER_ID)){
                                            fieldModel = this.writeIdentifier(fieldModel, templateFieldModel, funding.getFunderId().getIdentifier());
                                            valueFound = true;
                                        }

                                        if (funding.getFunderId().getIdentifier() != null && templateFieldModel.getSemantics().contains(SEMANTIC_PROJECT_FUNDING_FUNDER_ID_IDENTIFIER)){
                                            fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, funding.getFunderId().getIdentifier(), null, null, null);
                                            valueFound = true;
                                        }

                                        if (funding.getFunderId().getType() != null && templateFieldModel.getSemantics().contains(SEMANTIC_PROJECT_FUNDING_FUNDER_ID_TYPE)){
                                            fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, funding.getFunderId().getType(), null, null, null);
                                            valueFound = true;
                                        }
                                    }

                                    if (funding.getGrantId() != null) {
                                        if (funding.getGrantId().getIdentifier() != null && templateFieldModel.getSemantics().contains(SEMANTIC_PROJECT_FUNDING_GRANT_ID)){
                                            fieldModel = this.writeIdentifier(fieldModel, templateFieldModel, funding.getGrantId().getIdentifier());
                                            valueFound = true;
                                        }

                                        if (funding.getGrantId().getIdentifier() != null && templateFieldModel.getSemantics().contains(SEMANTIC_PROJECT_FUNDING_GRANT_ID_IDENTIFIER)){
                                            fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, funding.getGrantId().getIdentifier(), null, null, null);
                                            valueFound = true;
                                        }

                                        if (funding.getGrantId().getType() != null && templateFieldModel.getSemantics().contains(SEMANTIC_PROJECT_FUNDING_GRANT_ID_TYPE)){
                                            fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, funding.getGrantId().getType(), null, null, null);
                                            valueFound = true;
                                        }
                                    }

                                    if (funding.getFundingStatus() != null && funding.getFundingStatus().value() != null && templateFieldModel.getSemantics().contains(SEMANTIC_PROJECT_FUNDING_FUNDING_STATUS)){
                                        fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, funding.getFundingStatus().value(), null, null, null);
                                        valueFound = true;
                                    }

                                }
                            }


                            if (project.getProjectId() != null && !project.getProjectId().isEmpty()) {
                                for (ProjectId projectId: project.getProjectId()) {

                                    if (projectId.getIdentifier() != null && templateFieldModel.getSemantics().contains(SEMANTIC_PROJECT_ID_IDENTIFIER)){
                                        fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, projectId.getIdentifier(), null, null, null);
                                        valueFound = true;
                                    }

                                    if (projectId.getType() != null && templateFieldModel.getSemantics().contains(SEMANTIC_PROJECT_ID_TYPE)){
                                        fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, projectId.getType(), null, null, null);
                                        valueFound = true;
                                    }
                                }
                            }

                            if (project.getTitle() != null && templateFieldModel.getSemantics().contains(SEMANTIC_PROJECT_TITLE)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, project.getTitle(), null, null, null);
                                valueFound = true;
                            }

                            if (project.getDescription() != null && templateFieldModel.getSemantics().contains(SEMANTIC_PROJECT_DESCRIPTION)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, project.getDescription(), null, null, null);
                                valueFound = true;
                            }

                            if (project.getStart() != null && templateFieldModel.getSemantics().contains(SEMANTIC_PROJECT_START)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, project.getStart(), null, null, null);
                                valueFound = true;
                            }

                            if (project.getEnd() != null && templateFieldModel.getSemantics().contains(SEMANTIC_PROJECT_END)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, project.getEnd(), null, null, null);
                                valueFound = true;
                            }


                            if (valueFound) fieldsMap.put(templateFieldModel.getId(), fieldModel);
                        }
                    }
                    if (propertyDefinitionFieldSetModel.getItems() == null){
                        propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                    }
                    if (!fieldsMap.isEmpty()) {
                        propertyDefinitionFieldSetItemModel.setFields(fieldsMap);
                        propertyDefinitionFieldSetItemModel.setOrdinal(propertyDefinitionFieldSetModel.getItems().size());
                        propertyDefinitionFieldSetModel.getItems().add(propertyDefinitionFieldSetItemModel);
                    }

                    fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
                }
            }

        }

        return fieldSetModelMap;
    }
}
