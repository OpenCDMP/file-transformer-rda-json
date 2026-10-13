package org.opencdmp.filetransformer.rda.service.rdafiletransformer.export;

import org.opencdmp.commonmodels.models.plandescriptionfields.PropertyDefinitionFieldSetItemModel;
import gr.cite.tools.exception.MyApplicationException;
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

/** Builds one RDA {@code dataset} per description: its own properties, then the objects below it. */
public class DatasetExporter extends RdaMapperSupport {
    private final ContributorExporter contributorExporter;
    private final CostExporter costExporter;
    private final CreatorExporter creatorExporter;
    private final DistributionExporter distributionExporter;
    private final EthicsExporter ethicsExporter;
    private final IdentifierExporter identifierExporter;
    private final MetadataExporter metadataExporter;
    private final ProjectExporter projectExporter;
    private final SecurityAndPrivacyExporter securityAndPrivacyExporter;
    private final TechnicalResourceExporter technicalResourceExporter;

    public DatasetExporter(RdaMapperContext context, ContributorExporter contributorExporter, CostExporter costExporter, CreatorExporter creatorExporter, DistributionExporter distributionExporter, EthicsExporter ethicsExporter, IdentifierExporter identifierExporter, MetadataExporter metadataExporter, ProjectExporter projectExporter, SecurityAndPrivacyExporter securityAndPrivacyExporter, TechnicalResourceExporter technicalResourceExporter) {
        super(context);
        this.contributorExporter = contributorExporter;
        this.costExporter = costExporter;
        this.creatorExporter = creatorExporter;
        this.distributionExporter = distributionExporter;
        this.ethicsExporter = ethicsExporter;
        this.identifierExporter = identifierExporter;
        this.metadataExporter = metadataExporter;
        this.projectExporter = projectExporter;
        this.securityAndPrivacyExporter = securityAndPrivacyExporter;
        this.technicalResourceExporter = technicalResourceExporter;
    }

    Dataset buildRdaDataset(DescriptionModel model, Dmp dmpRda){
        if (model == null) throw new MyApplicationException("Model is missing");
        if (model.getLabel() == null)  throw new MyApplicationException("Dataset Label is missing");

        Dataset rda = new Dataset();

        rda.setAlternateIdentifier(this.identifierExporter.buildRdaDatasetAlternateIdentifier(model));
        AlternateIdentifier system = this.identifierExporter.buildRdaDatasetAlternateIdentifierBySystem(model);
        if (system != null) rda.getAlternateIdentifier().add(system);
        rda.setDatasetId(this.identifierExporter.buildRdaDatasetId(model));
        rda.setCreator(this.creatorExporter.buildRdaDatasetCreator(model));
        rda.setTitle(this.buildRdaDatasetTitle(model));
        rda.setDescription(this.buildRdaDatasetDescription(model));
        rda.setLanguage(this.buildRdaDatasetLanguage(model, dmpRda.getLanguage()));
        rda.setType(this.buildRdaDatasetType(model));
        rda.setIssued(this.buildRdaDatasetIssued(model));
        rda.setIsReused(this.buildRdaDatasetIsReused(model));
        rda.setKeyword(this.buildRdaDatasetKeywords(model));
        if (rda.getKeyword().isEmpty() && model.getTags() != null){
            rda.getKeyword().addAll(model.getTags());
        }
        rda.setPersonalData(this.buildRdaDatasetPersonalData(model));
        rda.setSensitiveData(this.buildRdaDatasetSensitiveData(model));
        rda.setRights(this.buildRdaDatasetRights(model));
        rda.setRelatedIdentifier(this.identifierExporter.buildRdaDatasetRelatedIdentifier(model));
        rda.setPreservationStatement(this.buildRdaDatasetPreservationStatement(model));
        rda.setTechnicalResource(this.technicalResourceExporter.buildRdaDatasetTechnicalResources(model));
        rda.setSecurityAndPrivacy(this.securityAndPrivacyExporter.buildRdaDatasetSecurityAndPrivacy(model));
        rda.setMetadata(this.metadataExporter.buildRdaDatasetMetadata(model));
        rda.setDataQualityAssurance(this.buildDataQualityAssurances(model));
        rda.setDistribution(this.distributionExporter.buildRdaDistribution(model));

        dmpRda.getAlternateIdentifier().addAll(this.identifierExporter.buildRdaDmpAlternateIdentifiers(model));
        dmpRda.getCost().addAll(this.costExporter.buildRdaCosts(model));
        dmpRda.getRelatedIdentifier().addAll(this.identifierExporter.buildRdaRelatedIdentifiers(model));
        dmpRda.setContributor(this.contributorExporter.buildContributor(model, dmpRda.getContributor()));
        dmpRda.getAffiliation().addAll(this.identifierExporter.buildRdaDmpAffiliations(model));

        this.ethicsExporter.mergeEthicalIssues(model, dmpRda);

        dmpRda.setProject(this.projectExporter.buildProject(model, dmpRda.getProject()));
            
        return rda;
    }

    Boolean buildRdaDatasetIsReused(DescriptionModel model){
        for (AnswerGroup answers : this.groups(model.getDescriptionTemplate(), model.getProperties().getFieldSets(), List.of(SEMANTIC_DATASET_IS_REUSED))) {

            Boolean value = parseBoolean(answers.value(SEMANTIC_DATASET_IS_REUSED));
            if (value != null) return value;
        }

        return null;
    }

    String buildRdaDatasetDescription(DescriptionModel model){
        for (AnswerGroup answers : this.groups(model.getDescriptionTemplate(), model.getProperties().getFieldSets(), List.of(SEMANTIC_DATASET_DESCRIPTION ))) {

            String fieldValue = answers.value(SEMANTIC_DATASET_DESCRIPTION);
            if (fieldValue != null) {
                return fieldValue;
            }
        }

        return model.getDescription();
    }

    /** The rights statement, only when answered: the asset's title is no rights statement. */
    String buildRdaDatasetRights(DescriptionModel model){
        for (AnswerGroup answers : this.groups(model.getDescriptionTemplate(), model.getProperties().getFieldSets(), List.of(SEMANTIC_DATASET_RIGHTS))) {

            String fieldValue = answers.value(SEMANTIC_DATASET_RIGHTS);
            if (fieldValue != null) {
                return fieldValue;
            }
        }

        return null;
    }

    String buildRdaDatasetTitle(DescriptionModel model){
        for (AnswerGroup answers : this.groups(model.getDescriptionTemplate(), model.getProperties().getFieldSets(), List.of(SEMANTIC_DATASET_TITLE))) {

            String fieldValue = answers.value(SEMANTIC_DATASET_TITLE);
            if (fieldValue != null) {
                return fieldValue;
            }
        }

        return model.getLabel();
    }

    List<String> buildDataQualityAssurances(DescriptionModel model){
        List<String> values = new ArrayList<>();

        for (AnswerGroup answers : this.groups(model.getDescriptionTemplate(), model.getProperties().getFieldSets(), List.of(SEMANTIC_DATASET_DATA_QUALITY_ASSURANCE))) {

            List<String> fieldValues = answers.values(SEMANTIC_DATASET_DATA_QUALITY_ASSURANCE);
            if (fieldValues != null && !fieldValues.isEmpty()) {
                values.addAll(fieldValues);
            }
        }
        return values;
    }

    String buildRdaDatasetPreservationStatement(DescriptionModel model){
        for (AnswerGroup answers : this.groups(model.getDescriptionTemplate(), model.getProperties().getFieldSets(), List.of(SEMANTIC_DATASET_PRESERVATION_STATEMENT))) {

            String fieldValue = answers.value(SEMANTIC_DATASET_PRESERVATION_STATEMENT);
            if (fieldValue != null) {
                return fieldValue;
            }
        }
        return null;
    }

    Dataset.SensitiveData buildRdaDatasetSensitiveData(DescriptionModel model){
        Dataset.SensitiveData value = null;
        for (AnswerGroup answers : this.groups(model.getDescriptionTemplate(), model.getProperties().getFieldSets(), List.of(SEMANTIC_DATASET_SENSITIVE_DATA))) {

            String fieldValue = answers.value(SEMANTIC_DATASET_SENSITIVE_DATA);
            if (fieldValue != null && !fieldValue.isBlank()) {
                try {
                    value = Dataset.SensitiveData.fromValue(fieldValue);
                    return value;
                } catch (IllegalArgumentException | MyApplicationException e) {
                    logger.warn("invalid dataset sensitive data value: " + fieldValue);
                }
            }
        }

        return Dataset.SensitiveData.UNKNOWN;
    }

    Dataset.PersonalData buildRdaDatasetPersonalData(DescriptionModel model){
        Dataset.PersonalData value = null;
        for (AnswerGroup answers : this.groups(model.getDescriptionTemplate(), model.getProperties().getFieldSets(), List.of(SEMANTIC_DATASET_PERSONAL_DATA))) {

            String fieldValue = answers.value(SEMANTIC_DATASET_PERSONAL_DATA);
            try {
                value = Dataset.PersonalData.fromValue(fieldValue);
                return value;
            } catch (IllegalArgumentException | MyApplicationException e) {
                logger.warn("invalid dataset personal data value: " + fieldValue);
            }
        }
        return Dataset.PersonalData.UNKNOWN;
    }

    /** Every keyword of every answer, in order and without repeats. */
    List<String> buildRdaDatasetKeywords(DescriptionModel model){
        LinkedHashSet<String> keywords = new LinkedHashSet<>();
        for (AnswerGroup answers : this.groups(model.getDescriptionTemplate(), model.getProperties().getFieldSets(), List.of(SEMANTIC_DATASET_KEYWORD))) {
            List<String> fieldValues = answers.values(SEMANTIC_DATASET_KEYWORD);
            if (fieldValues != null) keywords.addAll(fieldValues);
        }
        return new ArrayList<>(keywords);
    }

    String buildRdaDatasetIssued(DescriptionModel model){
        for (AnswerGroup answers : this.groups(model.getDescriptionTemplate(), model.getProperties().getFieldSets(), List.of(SEMANTIC_DATASET_ISSUED))) {

            String fieldValue = answers.value(SEMANTIC_DATASET_ISSUED);
            if (fieldValue != null) {
                return fieldValue;
            }
        }
        return null;
    }

    Language buildRdaDatasetLanguage(DescriptionModel model, Language planLanguage){
        Language value = null;
        for (AnswerGroup answers : this.groups(model.getDescriptionTemplate(), model.getProperties().getFieldSets(), List.of(SEMANTIC_DATASET_LANGUAGE ))) {

            String fieldValue = answers.value(SEMANTIC_DATASET_LANGUAGE);
            if (fieldValue != null && !fieldValue.isBlank()) {
                try {
                    value = Language.fromValue(fieldValue);
                    return value;
                } catch (IllegalArgumentException | MyApplicationException e) {
                    logger.warn("invalid dataset language value: " + fieldValue);
                }
            }
        }

        Language templateLanguage = buildRdaLanguage(model.getDescriptionTemplate().getLanguage());
        return templateLanguage != null ? templateLanguage : planLanguage;
    }

    String buildRdaDatasetType(DescriptionModel model){
        for (AnswerGroup answers : this.groups(model.getDescriptionTemplate(), model.getProperties().getFieldSets(), List.of(SEMANTIC_DATASET_TYPE ))) {

            String fieldValue = answers.value(SEMANTIC_DATASET_TYPE);
            if (fieldValue != null && !fieldValue.isBlank()) {
                return fieldValue;
            }
        }
        return "DMP Dataset";
    }
}
