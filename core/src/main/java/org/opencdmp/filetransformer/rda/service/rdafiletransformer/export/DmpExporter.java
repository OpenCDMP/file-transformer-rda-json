package org.opencdmp.filetransformer.rda.service.rdafiletransformer.export;

import org.opencdmp.commonmodels.models.plandescriptionfields.PropertyDefinitionFieldSetItemModel;
import gr.cite.tools.exception.MyApplicationException;
import org.opencdmp.commonmodels.enums.*;
import org.opencdmp.commonmodels.models.*;
import org.opencdmp.commonmodels.models.description.*;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.FieldSetModel;
import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.commonmodels.models.planreference.PlanReferenceModel;
import org.opencdmp.commonmodels.models.plugin.PluginFieldModel;
import org.opencdmp.commonmodels.models.plugin.PluginModel;
import org.opencdmp.commonmodels.models.reference.ReferenceModel;
import org.opencdmp.filetransformer.rda.model.rda.*;
import org.opencdmp.filetransformerbase.models.misc.*;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.AnswerGroup;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaMapperContext;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaMapperSupport;
import static org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaSemantics.*;

/** Builds the RDA {@code dmp} object from a plan: its own properties, then everything below it. */
public class DmpExporter extends RdaMapperSupport {
    // match the configuration-fields codes in rda-file-transformer.yml
    private static final String FIELD_GRANTS_REFERENCE_TYPE = "grants-reference-type";
    private static final String FIELD_FUNDERS_REFERENCE_TYPE = "funders-reference-type";
    private static final String FIELD_PROJECTS_REFERENCE_TYPE = "projects-reference-type";
    private static final String FIELD_RESEARCHERS_REFERENCE_TYPE = "researchers-reference-type";
    private static final String FIELD_DMP_ID_DOI_REPOSITORY = "dmp-id-doi-repository";

    private final ContactExporter contactExporter;
    private final ContributorExporter contributorExporter;
    private final CostExporter costExporter;
    private final DatasetExporter datasetExporter;
    private final EthicsExporter ethicsExporter;
    private final IdentifierExporter identifierExporter;
    private final ProjectExporter projectExporter;

    public DmpExporter(RdaMapperContext context, ContactExporter contactExporter, ContributorExporter contributorExporter, CostExporter costExporter, DatasetExporter datasetExporter, EthicsExporter ethicsExporter, IdentifierExporter identifierExporter, ProjectExporter projectExporter) {
        super(context);
        this.contactExporter = contactExporter;
        this.contributorExporter = contributorExporter;
        this.costExporter = costExporter;
        this.datasetExporter = datasetExporter;
        this.ethicsExporter = ethicsExporter;
        this.identifierExporter = identifierExporter;
        this.projectExporter = projectExporter;
    }

    public Dmp buildRdaDmp(PlanModel plan){
        if (plan == null) throw new MyApplicationException("Plan is missing");
        if (plan.getCreatedAt() == null) throw new MyApplicationException("Plan Created is missing");
        if (plan.getUpdatedAt() == null) throw new MyApplicationException("Plan Modified is missing");
        if (plan.getLabel() == null) throw new MyApplicationException("Plan Label is missing");
        if (plan.getProperties() == null)  throw new MyApplicationException("Plan is missing language and contact properties");

        List<ReferenceModel> grants = this.getReferenceModelOfTypeCode(plan, this.setting(plan, FIELD_GRANTS_REFERENCE_TYPE, this.configuration.getRdaFileTransformerServiceProperties().getGrantReferenceCode()));
        List<ReferenceModel> researchers = this.getReferenceModelOfTypeCode(plan, this.setting(plan, FIELD_RESEARCHERS_REFERENCE_TYPE, this.configuration.getRdaFileTransformerServiceProperties().getResearcherReferenceCode()));
        List<ReferenceModel> funders = this.getReferenceModelOfTypeCode(plan, this.setting(plan, FIELD_FUNDERS_REFERENCE_TYPE, this.configuration.getRdaFileTransformerServiceProperties().getFunderReferenceCode()));
        List<ReferenceModel> projects = this.getReferenceModelOfTypeCode(plan, this.setting(plan, FIELD_PROJECTS_REFERENCE_TYPE, this.configuration.getRdaFileTransformerServiceProperties().getProjectReferenceCode()));
        List<ReferenceModel> licences = this.getReferenceModelOfTypeCode(plan, this.configuration.getRdaFileTransformerServiceProperties().getLicenceReferenceCode());

        Dmp rda = new Dmp();

        rda.setDmpId(this.buildRdaDmpId(plan));
        rda.setCreated(this.buildDmpCreated(plan));
        rda.setModified(this.buildDmpModified(plan));
        rda.setTitle(this.buildRdaDmpTitle(plan));
        rda.setDescription(this.buildRdaDmpDescription(plan));
        rda.setLanguage(this.buildDmpLanguage(plan));

        rda.setContributor(this.contributorExporter.buildRdaContributors(researchers, plan.getUsers()));
        this.contributorExporter.addDistinct(rda.getContributor(), this.contributorExporter.buildContributor(plan));

        rda.setContact(this.contactExporter.buildRdaContact(plan));
        rda.setEthicalIssuesExist(this.ethicsExporter.buildRdaEthicalIssuesExist(plan));
        rda.setEthicalIssuesDescription(this.ethicsExporter.buildRdaEthicalIssuesDescription(plan));
        rda.setEthicalIssuesReport(this.ethicsExporter.buildRdaEthicalIssueReport(plan));

        rda.setProject(this.projectExporter.buildRdaProjects(projects, grants, funders));
        this.projectExporter.addDistinct(rda.getProject(), this.projectExporter.buildProject(plan));

        rda.setCost(this.costExporter.buildRdaCosts(plan));
        rda.setRelatedIdentifier(this.identifierExporter.buildRdaRelatedIdentifier(plan));
        rda.setAlternateIdentifier(this.identifierExporter.buildRdaAlternateIdentifier(plan));
        rda.setAffiliation(this.identifierExporter.buildAffiliation(plan));

        rda.setDataset(new ArrayList<>());
        if (plan.getDescriptions() != null){
            for (DescriptionModel descriptionModel : plan.getDescriptions()) rda.getDataset().add(this.datasetExporter.buildRdaDataset(descriptionModel, rda));
        }
        
        return rda;
    }

    Language buildDmpLanguage(PlanModel model){

        for (AnswerGroup answers : this.groups(model.getPlanBlueprint(), model.getProperties().getFieldSets(), List.of(SEMANTIC_DMP_LANGUAGE))) {

            String fieldValue = answers.value(SEMANTIC_DMP_LANGUAGE);
            if (fieldValue != null) {
                String rdaLanguage = this.configuration.getRdaFileTransformerServiceProperties().getLanguageMap().getOrDefault(fieldValue, null);
                if(rdaLanguage != null) {
                    try {
                        return Language.fromValue(rdaLanguage);
                    } catch (Exception e) {
                        logger.warn("invalid language value: " + rdaLanguage);
                    }
                }
            }
        }

        String rdaLanguage = this.configuration.getRdaFileTransformerServiceProperties().getLanguageMap().getOrDefault(model.getLanguage(), null);
        try {
            return Language.fromValue(rdaLanguage);
        } catch (Exception e) {
            logger.warn("invalid language value: " + rdaLanguage);
        }
        // RDA requires a language
        return Language.ENG;
    }

    Instant buildDmpCreated(PlanModel model) {

        for (AnswerGroup answers : this.groups(model.getPlanBlueprint(), model.getProperties().getFieldSets(), List.of(SEMANTIC_DMP_CREATED))) {

            String fieldValue = answers.value(SEMANTIC_DMP_CREATED);
            if (fieldValue != null && !fieldValue.isBlank()) {
                try {
                    return this.parseDateTime(fieldValue);
                } catch (DateTimeParseException e) {
                    logger.warn("invalid date for created value: " + fieldValue);
                }
            }
        }
        return model.getCreatedAt();
    }

    Instant buildDmpModified(PlanModel model){
        for (AnswerGroup answers : this.groups(model.getPlanBlueprint(), model.getProperties().getFieldSets(), List.of(SEMANTIC_DMP_MODIFIED))) {

            String fieldValue = answers.value(SEMANTIC_DMP_MODIFIED);
            if (fieldValue != null && !fieldValue.isBlank()) {
                try {
                    return this.parseDateTime(fieldValue);
                } catch (DateTimeParseException e) {
                    logger.warn("invalid date for modified value: " + fieldValue);
                }
            }
        }

        return model.getUpdatedAt();
    }

    String buildRdaDmpDescription(PlanModel model){
        for (AnswerGroup answers : this.groups(model.getPlanBlueprint(), model.getProperties().getFieldSets(), List.of(SEMANTIC_DMP_DESCRIPTION))) {

            String fieldValue = answers.value(SEMANTIC_DMP_DESCRIPTION);
            if (fieldValue != null && !fieldValue.isBlank()) {
                return fieldValue;
            }
        }

        return model.getDescription();
    }

    String buildRdaDmpTitle(PlanModel model){
        for (AnswerGroup answers : this.groups(model.getPlanBlueprint(), model.getProperties().getFieldSets(), List.of(SEMANTIC_DMP_TITLE))) {

            String fieldValue = answers.value(SEMANTIC_DMP_TITLE);
            if (fieldValue != null && !fieldValue.isBlank()) {
                return fieldValue;
            }
        }

        return model.getLabel();
    }

    DmpId buildRdaDmpId(PlanModel plan){
        if (plan == null) throw new MyApplicationException("Plan is missing");
        String doiRepository = this.setting(plan, FIELD_DMP_ID_DOI_REPOSITORY, REPOSITORY_ZENODO);
        EntityDoiModel model = plan.getEntityDois() == null ? null : plan.getEntityDois().stream().filter(x -> x.getRepositoryId() != null && x.getDoi() != null && !x.getDoi().isBlank() && x.getRepositoryId().equalsIgnoreCase(doiRepository)).findFirst().orElse(null);
        DmpId rdaModel = new DmpId();
        if (model != null) {
            rdaModel.setIdentifier(model.getDoi());
            rdaModel.setType(DMP_TYPE_DOI);
            return rdaModel;
        }
        // no deposited DOI: the identifier the blueprint asks for, if answered, else the plan's own id
        for (AnswerGroup answers : this.groups(plan.getPlanBlueprint(), plan.getProperties().getFieldSets(), List.of(SEMANTIC_DMP_DMP_ID, SEMANTIC_DMP_DMP_ID_IDENTIFIER, SEMANTIC_DMP_DMP_ID_TYPE))) {
            String plain = answers.value(SEMANTIC_DMP_DMP_ID);
            String identifier = plain != null ? plain : answers.value(SEMANTIC_DMP_DMP_ID_IDENTIFIER);
            if (identifier == null) continue;
            String type = plain == null ? answers.value(SEMANTIC_DMP_DMP_ID_TYPE) : null;
            rdaModel.setIdentifier(identifier);
            rdaModel.setType(type != null ? type : inferPersistentIdType(identifier));
            return rdaModel;
        }
        rdaModel.setIdentifier(plan.getId().toString());
        rdaModel.setType(DMP_TYPE_OTHER);
        return rdaModel;
    }

    /** The organisation's or blueprint's value for one of this transformer's settings, sent with the blueprint. */
    String setting(PlanModel plan, String code, String fallback) {
        if (plan.getPlanBlueprint() == null || plan.getPlanBlueprint().getDefinition() == null || plan.getPlanBlueprint().getDefinition().getPlugins() == null) return fallback;
        String transformerId = this.configuration.getRdaFileTransformerServiceProperties().getTransformerId();
        for (PluginModel plugin : plan.getPlanBlueprint().getDefinition().getPlugins()) {
            if (!transformerId.equals(plugin.getCode()) || !PluginType.FileTransformer.equals(plugin.getType()) || plugin.getFields() == null) continue;
            for (PluginFieldModel field : plugin.getFields()) {
                if (code.equals(field.getCode()) && field.getTextValue() != null && !field.getTextValue().isBlank()) return field.getTextValue().trim();
            }
        }
        return fallback;
    }

    List<ReferenceModel> getReferenceModelOfTypeCode(PlanModel plan, String code){
        List<ReferenceModel> response = new ArrayList<>();
        if (plan.getReferences() == null) return response;
        for (PlanReferenceModel planReferenceModel : plan.getReferences()){
            if (planReferenceModel.getReference() != null && planReferenceModel.getReference().getType() != null && planReferenceModel.getReference().getType().getCode() != null  && planReferenceModel.getReference().getType().getCode().equals(code)){
                // a reference picked in several fields is listed once per field; export it once
                ReferenceModel reference = planReferenceModel.getReference();
                if (reference.getId() == null || response.stream().noneMatch(x -> reference.getId().equals(x.getId()))) response.add(reference);
            }
        }

        return response;
    }
}
