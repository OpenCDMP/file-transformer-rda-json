package org.opencdmp.filetransformer.rda.service.rdafiletransformer.importer;

import org.opencdmp.commonmodels.models.plandescriptionfields.PropertyDefinitionFieldSetItemModel;
import org.opencdmp.commonmodels.models.plandescriptionfields.PropertyDefinitionFieldSetModel;
import gr.cite.tools.exception.MyApplicationException;
import org.opencdmp.commonmodels.enums.*;
import org.opencdmp.commonmodels.models.*;
import org.opencdmp.commonmodels.models.description.*;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.FieldModel;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.FieldSetModel;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.fielddata.ReferenceTypeDataModel;
import org.opencdmp.commonmodels.models.plan.PlanContactModel;
import org.opencdmp.commonmodels.models.plan.PlanPropertiesModel;
import org.opencdmp.commonmodels.models.planblueprint.PlanBlueprintModel;
import org.opencdmp.commonmodels.models.plandescriptiontemplate.PlanDescriptionTemplateModel;
import org.opencdmp.commonmodels.models.reference.ReferenceModel;
import org.opencdmp.commonmodels.models.user.UserModel;
import org.opencdmp.filetransformer.rda.model.rda.*;
import org.opencdmp.filetransformerbase.models.misc.*;
import java.time.*;
import java.util.*;
import java.util.stream.Stream;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaMapperContext;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaMapperSupport;
import static org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaSemantics.*;

/** Writes the dmp-level properties of an RDA file into the answers of a plan blueprint. */
public class PlanImporter extends RdaMapperSupport {

    public PlanImporter(RdaMapperContext context) {
        super(context);
    }

    List<PlanUserModel> buildPlanUsersByRdaContributors(List<Contributor> contributors) {
        List<PlanUserModel> models = new ArrayList<>();
        if (contributors == null) return models;
        for (Contributor contributor : contributors){
            models.add(this.buildPlanUserByRdaContributor(contributor));
        }
        return models;
    }

    PlanUserModel buildPlanUserByRdaContributor(Contributor contributor) {
        if (contributor == null) throw new MyApplicationException("Contributor is missing");

        PlanUserModel planUserModel = new PlanUserModel();
//        if (contributor.getContributorId() != null && contributor.getContributorId().getIdentifier() != null) {
//            try {
//                UserModel userModel = new UserModel();
//                userModel.setId(UUID.fromString(contributor.getContributorId().getIdentifier()));
//                planUserModel.setUser(userModel);
//            }
//            catch (IllegalArgumentException e) {}
//        }

        if (contributor.getRole() != null && !contributor.getRole().isEmpty()){
            if (contributor.getRole().contains(PlanUserRole.Owner.name())) planUserModel.setRole(PlanUserRole.Owner);
            else if (contributor.getRole().contains(PlanUserRole.DescriptionContributor.name())) planUserModel.setRole(PlanUserRole.DescriptionContributor);
            else if (contributor.getRole().contains(PlanUserRole.Reviewer.name())) planUserModel.setRole(PlanUserRole.Reviewer);
            else if (contributor.getRole().contains(PlanUserRole.Viewer.name())) planUserModel.setRole(PlanUserRole.Viewer);
            else if (contributor.getRole().contains(PlanUserRole.DataSteward.name())) planUserModel.setRole(PlanUserRole.DataSteward);
            else if (contributor.getRole().contains(PlanUserRole.DataPrivacyOfficer.name())) planUserModel.setRole(PlanUserRole.DataPrivacyOfficer);
            else if (contributor.getRole().contains(PlanUserRole.EthicsReviewer.name())) planUserModel.setRole(PlanUserRole.EthicsReviewer);

        }

        return planUserModel;
    }

    /**
     * @param placed filled with the plan-level objects this blueprint took (see {@code placeOnce}), so that
     *               descriptions do not import them a second time
     */
    public PlanPropertiesModel buildPlanPropertiesModel(PlanBlueprintModel planBlueprintModel, Dmp dmp, Set<String> placed) {
        PlanPropertiesModel planPropertiesModel = new PlanPropertiesModel();
        if (dmp == null) return planPropertiesModel;

        if (planBlueprintModel == null) throw new MyApplicationException("planBlueprintModel is missing");

        Map<String, PropertyDefinitionFieldSetModel> fieldSets = this.buildFieldSetMap(planBlueprintModel, dmp);

        if (dmp.getContact() != null) {
            planPropertiesModel.setContacts(new ArrayList<>());
            planPropertiesModel.getContacts().add(this.buildPlanContactModelByRdaContact(dmp.getContact()));
            this.mergeAnswers(fieldSets, this.buildPlanBlueprintValuesByContact(planBlueprintModel, dmp.getContact()));
        }

        if (dmp.getCost() != null) this.placeOnce(placed, OBJECT_COST, fieldSets, () -> this.buildPlanBlueprintValuesByRdaCost(planBlueprintModel, dmp.getCost()));

        if (dmp.getAlternateIdentifier() != null) this.placeOnce(placed, OBJECT_ALTERNATE_IDENTIFIER, fieldSets, () -> this.buildPlanBlueprintValuesByRdaAlternateIdentifier(planBlueprintModel, dmp.getAlternateIdentifier()));

        if (dmp.getRelatedIdentifier() != null) this.placeOnce(placed, OBJECT_RELATED_IDENTIFIER, fieldSets, () -> this.buildPlanBlueprintValuesByRdaRelatedIdentifier(planBlueprintModel, dmp.getRelatedIdentifier()));

        if (dmp.getLanguage() != null && dmp.getLanguage().value() != null) {

            Stream<String> keys = this.configuration.getRdaFileTransformerServiceProperties().getLanguageMap()
                    .entrySet()
                    .stream()
                    .filter(entry -> dmp.getLanguage().value().equals(entry.getValue()))
                    .map(Map.Entry::getKey);

            keys.findFirst().ifPresent(code -> this.mergeAnswers(fieldSets, buildPlanBlueprintValuesBySingleString(planBlueprintModel, code, SEMANTIC_DMP_LANGUAGE)));
        }

        // the plan identifier goes back into the blueprint, unless it is an OpenCDMP plan id: an imported plan is a new plan
        if (dmp.getDmpId() != null && dmp.getDmpId().getIdentifier() != null && !this.isInternalPlanId(dmp.getDmpId())) {
            this.mergeAnswers(fieldSets, this.buildPlanBlueprintValuesBySingleString(planBlueprintModel, dmp.getDmpId().getIdentifier(), SEMANTIC_DMP_DMP_ID));
            this.mergeAnswers(fieldSets, this.buildPlanBlueprintValuesBySingleString(planBlueprintModel, dmp.getDmpId().getIdentifier(), SEMANTIC_DMP_DMP_ID_IDENTIFIER));
            if (dmp.getDmpId().getType() != null) this.mergeAnswers(fieldSets, this.buildPlanBlueprintValuesBySingleString(planBlueprintModel, dmp.getDmpId().getType(), SEMANTIC_DMP_DMP_ID_TYPE));
        }

        if (dmp.getProject() != null) this.placeOnce(placed, OBJECT_PROJECT, fieldSets, () -> this.buildFieldByRdaProject(planBlueprintModel, dmp.getProject()));

        if (dmp.getContributor() != null) this.placeOnce(placed, OBJECT_CONTRIBUTOR, fieldSets, () -> this.buildFieldByRdaContributor(planBlueprintModel, dmp.getContributor()));

        if (dmp.getAffiliation() != null) this.placeOnce(placed, OBJECT_AFFILIATION, fieldSets, () -> this.buildPlanBlueprintValuesByDmpAffiliations(planBlueprintModel, dmp.getAffiliation()));

        this.trimToMultiplicity(planBlueprintModel.getDefinition().getAllFieldSets(), fieldSets);

        for (FieldSetModel fieldSetModel: planBlueprintModel.getDefinition().getAllFieldSets()){
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

        planPropertiesModel.setFieldSets(fieldSets);

        return planPropertiesModel;
    }

    Map<String, PropertyDefinitionFieldSetModel> buildFieldByRdaContributor(PlanBlueprintModel planBlueprintModel, List<Contributor> contributors){
        Map<String, PropertyDefinitionFieldSetModel> fieldSetModelMap = new HashMap<>();

        if (planBlueprintModel == null) throw new MyApplicationException("planBlueprintModel is missing");

        if (contributors == null) return fieldSetModelMap;

        List<FieldSetModel> fieldSetsWithSemantics = this.blueprintFieldSearcherService.searchFieldSetsBySemantics(planBlueprintModel, List.of(SEMANTIC_DATASET_CONTRIBUTOR, SEMANTIC_DATASET_CONTRIBUTOR_CONTRIBUTOR_ID_IDENTIFIER,
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

    Map<String, PropertyDefinitionFieldSetModel> buildPlanBlueprintValuesByRdaRelatedIdentifier(PlanBlueprintModel planBlueprintModel, List<RelatedIdentifier> relatedIdentifiers){
        Map<String, PropertyDefinitionFieldSetModel> fieldSetModelMap = new HashMap<>();

        if (planBlueprintModel == null) throw new MyApplicationException("planBlueprintModel is missing");

        if (relatedIdentifiers == null) return fieldSetModelMap;

        List<FieldSetModel> fieldSetsWithSemantics = this.blueprintFieldSearcherService.searchFieldSetsBySemantics(planBlueprintModel, List.of(SEMANTIC_DMP_RELATED_IDENTIFIER_IDENTIFIER,
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

                            if (relatedIdentifier.getIdentifier() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DMP_RELATED_IDENTIFIER_SCHEME_URI) && relatedIdentifier.getSchemeUri() != null){
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

    Map<String, PropertyDefinitionFieldSetModel> buildPlanBlueprintValuesByContact(PlanBlueprintModel planBlueprintModel, Contact contact){
        Map<String, PropertyDefinitionFieldSetModel> fieldSetModelMap = new HashMap<>();

        if (planBlueprintModel == null) throw new MyApplicationException("planBlueprintModel is missing");

        if (contact == null) return fieldSetModelMap;

        List<FieldSetModel> fieldSetsWithSemantics = this.blueprintFieldSearcherService.searchFieldSetsBySemantics(planBlueprintModel, List.of(SEMANTIC_CONTACT_MBOX, SEMANTIC_CONTACT_NAME, SEMANTIC_CONTACT_AFFILIATION_NAME, SEMANTIC_CONTACT_AFFILIATION_ID_IDENTIFIER,
                SEMANTIC_CONTACT_AFFILIATION_ID_TYPE, SEMANTIC_CONTACT_ID_IDENTIFIER, SEMANTIC_CONTACT_ID_TYPE));
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
                        if (contact.getMbox() != null && templateFieldModel.getSemantics().contains(SEMANTIC_CONTACT_MBOX)){
                            fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, contact.getMbox(), null, null, null);
                            valueFound = true;
                        }
                        if (contact.getName() != null && templateFieldModel.getSemantics().contains(SEMANTIC_CONTACT_NAME)){
                            fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, contact.getName(), null, null, null);
                            valueFound = true;
                        }

                        if (contact.getAffiliation() != null && !contact.getAffiliation().isEmpty()) {
                            Affiliation affiliation = contact.getAffiliation().getFirst();
                            if (affiliation != null) {
                                if (affiliation.getName() != null && templateFieldModel.getSemantics().contains(SEMANTIC_CONTACT_AFFILIATION_NAME)) {
                                    fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, affiliation.getName(), null, null, null);
                                    valueFound = true;
                                }
                                if (affiliation.getAffiliationId() != null) {
                                    if (affiliation.getAffiliationId().getIdentifier() != null && templateFieldModel.getSemantics().contains(SEMANTIC_CONTACT_AFFILIATION_ID_IDENTIFIER)){
                                        fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, affiliation.getAffiliationId().getIdentifier(), null, null, null);
                                        valueFound = true;
                                    }
                                    if (affiliation.getAffiliationId().getType() != null && templateFieldModel.getSemantics().contains(SEMANTIC_CONTACT_AFFILIATION_ID_TYPE)){
                                        fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, affiliation.getAffiliationId().getType(), null, null, null);
                                        valueFound = true;
                                    }
                                }

                            }
                        }

                        if (contact.getContactId() != null && !contact.getContactId().isEmpty()) {
                            ContactId contactId = contact.getContactId().getFirst();
                            if (contactId != null) {
                                if (contactId.getIdentifier() != null && !contactId.getIdentifier().isBlank() && templateFieldModel.getSemantics().contains(SEMANTIC_CONTACT_ID_IDENTIFIER)) {
                                    fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, contactId.getIdentifier(), null, null, null);
                                    valueFound = true;
                                }
                                if (contactId.getType() != null && !contactId.getType().isBlank() && templateFieldModel.getSemantics().contains(SEMANTIC_CONTACT_ID_TYPE)) {
                                    fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, contactId.getType(), null, null, null);
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

        return fieldSetModelMap;
    }

    Map<String, PropertyDefinitionFieldSetModel> buildPlanBlueprintValuesByRdaAlternateIdentifier(PlanBlueprintModel planBlueprintModel, List<AlternateIdentifier> alternateIdentifiers){
        Map<String, PropertyDefinitionFieldSetModel> fieldSetModelMap = new HashMap<>();

        if (planBlueprintModel == null) throw new MyApplicationException("planBlueprintModel is missing");

        if (alternateIdentifiers == null) return fieldSetModelMap;

        List<FieldSetModel> fieldSetsWithSemantics = this.blueprintFieldSearcherService.searchFieldSetsBySemantics(planBlueprintModel, List.of(SEMANTIC_DMP_ALTERNATE_IDENTIFIER_ID, SEMANTIC_DMP_ALTERNATE_IDENTIFIER_TYPE));

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

    Map<String, PropertyDefinitionFieldSetModel> buildPlanBlueprintValuesByDmpAffiliations(PlanBlueprintModel planBlueprintModel, List<Affiliation> affiliations){
        Map<String, PropertyDefinitionFieldSetModel> fieldSetModelMap = new HashMap<>();

        if (planBlueprintModel == null) throw new MyApplicationException("planBlueprintModel is missing");

        if (affiliations == null) return fieldSetModelMap;

        List<FieldSetModel> fieldSetsWithSemantics = this.blueprintFieldSearcherService.searchFieldSetsBySemantics(planBlueprintModel, List.of(SEMANTIC_DMP_AFFILIATION_NAME, SEMANTIC_DMP_AFFILIATION_ID, SEMANTIC_DMP_AFFILIATION_ID_TYPE, SEMANTIC_DMP_AFFILIATION_ID_IDENTIFIER));

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

    Map<String, PropertyDefinitionFieldSetModel> buildFieldSetMap(PlanBlueprintModel planBlueprintModel, Dmp dmp){
        Map<String, PropertyDefinitionFieldSetModel> fieldSetModelMap = new HashMap<>();

        if (planBlueprintModel == null) throw new MyApplicationException("planBlueprintModel is missing");

        List<FieldSetModel> fieldSetsWithSemantics = this.blueprintFieldSearcherService.searchFieldSetsBySemantics(planBlueprintModel, List.of(SEMANTIC_ETHICAL_ISSUES_EXISTS, SEMANTIC_ETHICAL_ISSUES_DESCRIPTION, SEMANTIC_ETHICAL_ISSUES_REPORT,
                SEMANTIC_DMP_TITLE, SEMANTIC_DMP_DESCRIPTION, SEMANTIC_DMP_CREATED, SEMANTIC_DMP_MODIFIED ));
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
                        if (dmp != null) {
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

    /** The id the export writes for a plan without a DOI or an answered identifier: its UUID, typed other. */
    private boolean isInternalPlanId(DmpId dmpId) {
        if (!DMP_TYPE_OTHER.equalsIgnoreCase(dmpId.getType())) return false;
        try {
            java.util.UUID.fromString(dmpId.getIdentifier());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    Map<String, PropertyDefinitionFieldSetModel> buildPlanBlueprintValuesBySingleString(PlanBlueprintModel planBlueprintModel, String value, String semantic){
        Map<String, PropertyDefinitionFieldSetModel> fieldSetModelMap = new HashMap<>();

        if (planBlueprintModel == null) throw new MyApplicationException("planBlueprintModel is missing");

        List<FieldSetModel> fieldSetsWithSemantics = this.blueprintFieldSearcherService.searchFieldSetsBySemantics(planBlueprintModel, List.of(semantic));
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
                        if (value != null && templateFieldModel.getSemantics().contains(semantic)){
                            fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, value, null, null, null);
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

        return fieldSetModelMap;
    }

    Map<String, PropertyDefinitionFieldSetModel> buildFieldByRdaProject(PlanBlueprintModel blueprintModel, List<Project> projects){
        Map<String, PropertyDefinitionFieldSetModel> fieldSetModelMap = new HashMap<>();

        if (blueprintModel == null) throw new MyApplicationException("blueprintModel is missing");

        if (projects == null) return fieldSetModelMap;

        List<FieldSetModel> fieldSetsWithSemantics = this.blueprintFieldSearcherService.searchFieldSetsBySemantics(blueprintModel, List.of(SEMANTIC_PROJECT_FUNDING_GRANT_ID, SEMANTIC_PROJECT_FUNDING_GRANT_ID_IDENTIFIER,
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

    Map<String, PropertyDefinitionFieldSetModel> buildPlanBlueprintValuesByRdaCost(PlanBlueprintModel planBlueprintModel, List<Cost> costs){
        Map<String, PropertyDefinitionFieldSetModel> fieldSetModelMap = new HashMap<>();

        if (planBlueprintModel == null) throw new MyApplicationException("planBlueprintModel is missing");

        if (costs == null) return fieldSetModelMap;

        List<FieldSetModel> fieldSetsWithSemantics = this.blueprintFieldSearcherService.searchFieldSetsBySemantics(planBlueprintModel, List.of(SEMANTIC_COST_TITLE,
                SEMANTIC_COST_DESCRIPTION, SEMANTIC_COST_VALUE, SEMANTIC_COST_CURRENCY_CODE));

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

                            if (cost.getCurrencyCode() != null && templateFieldModel.getSemantics().contains(SEMANTIC_COST_CURRENCY_CODE)){
                                fieldModel = this.buildFieldModel(fieldModel, templateFieldModel, cost.getCurrencyCode().value(), null, null, null);
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

    PlanContactModel buildPlanContactModelByRdaContact(Contact contact) {
        PlanContactModel planContactModel = new PlanContactModel();

        if (contact == null) return planContactModel;

        planContactModel.setFirstName(contact.getName());
        planContactModel.setEmail(contact.getMbox());

        return planContactModel;
    }

    public PlanDescriptionTemplateModel buildPlanDescriptionTemplateModel(DescriptionImportModel model) {
        PlanDescriptionTemplateModel persist = new PlanDescriptionTemplateModel();

        if (model == null) return persist;

        if (model.getDescriptionTemplate() != null) persist.setDescriptionTemplateGroupId(model.getDescriptionTemplate().getGroupId());
        persist.setSectionId(model.getSectionId());

        return persist;
    }
}
