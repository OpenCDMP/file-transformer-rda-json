package org.opencdmp.filetransformer.rda.service.rdafiletransformer;

import com.fasterxml.jackson.core.JsonProcessingException;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.commonmodels.enums.*;
import org.opencdmp.commonmodels.models.*;
import org.opencdmp.commonmodels.models.description.*;
import org.opencdmp.commonmodels.models.descriptiotemplate.DescriptionTemplateModel;
import org.opencdmp.commonmodels.models.descriptiotemplate.FieldModel;
import org.opencdmp.commonmodels.models.descriptiotemplate.FieldSetModel;
import org.opencdmp.commonmodels.models.descriptiotemplate.fielddata.ReferenceTypeDataModel;
import org.opencdmp.commonmodels.models.plan.PlanBlueprintValueModel;
import org.opencdmp.commonmodels.models.plan.PlanContactModel;
import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.commonmodels.models.plan.PlanPropertiesModel;
import org.opencdmp.commonmodels.models.planblueprint.PlanBlueprintModel;
import org.opencdmp.commonmodels.models.planblueprint.ReferenceTypeFieldModel;
import org.opencdmp.commonmodels.models.planblueprint.SectionModel;
import org.opencdmp.commonmodels.models.plandescriptiontemplate.PlanDescriptionTemplateModel;
import org.opencdmp.commonmodels.models.planreference.PlanReferenceDataModel;
import org.opencdmp.commonmodels.models.planreference.PlanReferenceModel;
import org.opencdmp.commonmodels.models.reference.ReferenceFieldModel;
import org.opencdmp.commonmodels.models.reference.ReferenceModel;
import org.opencdmp.commonmodels.models.reference.ReferenceTypeModel;
import org.opencdmp.filetransformer.rda.model.rda.*;
import org.opencdmp.filetransformer.rda.service.descriptiontemplatesearcher.TemplateFieldSearcherService;
import org.opencdmp.filetransformerbase.enums.FileTransformerEntityType;
import org.opencdmp.filetransformerbase.interfaces.FileTransformerClient;
import org.opencdmp.filetransformerbase.interfaces.FileTransformerConfiguration;
import org.opencdmp.filetransformer.rda.service.json.JsonHandlingService;
import org.opencdmp.filetransformer.rda.service.storage.FileStorageService;
import org.opencdmp.filetransformerbase.models.misc.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RdaFileTransformerService implements FileTransformerClient {
    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(RdaFileTransformerService.class));

    private final FileStorageService storageService;
    private final JsonHandlingService jsonHandlingService;
    private final RdaFileTransformerServiceConfiguration configuration;
    private final TemplateFieldSearcherService templateFieldSearcherService;
    
    private final static List<FileTransformerEntityType> FILE_TRANSFORMER_ENTITY_TYPES = List.of(FileTransformerEntityType.Plan);
    private final static String REPOSITORY_ZENODO = "Zenodo";
    private final static String PREFIX_ORCID = "orcid";
    private final static String ORCID_URL = "http://orcid.org/";
    private static final String SEMANTIC_ETHICAL_ISSUES_EXISTS = "rda.dmp.ethical_issues_exist";
    private static final String SEMANTIC_ETHICAL_ISSUES_DESCRIPTION = "rda.dmp.ethical_issues_description";
    private static final String SEMANTIC_ETHICAL_ISSUES_REPORT = "rda.dmp.ethical_issues_report";
    private static final String SEMANTIC_COST_CURRENCY_CODE = "rda.dmp.cost.currency_code";
    private static final String SEMANTIC_COST_DESCRIPTION = "rda.dmp.cost.description";
    private static final String SEMANTIC_COST_TITLE = "rda.dmp.cost.title";
    private static final String SEMANTIC_COST_VALUE = "rda.dmp.cost.value";
    private static final String SEMANTIC_DATASET_LANGUAGE = "rda.dataset.language";
    private static final String SEMANTIC_DATASET_TYPE = "rda.dataset.type";
    private static final String SEMANTIC_DATASET_ISSUED = "rda.dataset.issued";
    private static final String SEMANTIC_DATASET_KEYWORD = "rda.dataset.keyword";
    private static final String SEMANTIC_DATASET_PERSONAL_DATA = "rda.dataset.personal_data";
    private static final String SEMANTIC_DATASET_SENSITIVE_DATA = "rda.dataset.sensitive_data";
    private static final String SEMANTIC_DATASET_PRESERVATION_STATEMENT = "rda.dataset.preservation_statement";
    private static final String SEMANTIC_DATASET_DATA_QUALITY_ASSURANCE = "rda.dataset.data_quality_assurance";
    private static final String SEMANTIC_DATASET_TECHNICAL_RESOURCE_DESCRIPTION = "rda.dataset.technical_resource.description";
    private static final String SEMANTIC_DATASET_TECHNICAL_RESOURCE_NAME = "rda.dataset.technical_resource.name";
    private static final String SEMANTIC_DATASET_SECURITY_AND_PRIVACY_DESCRIPTION = "rda.dataset.security_and_privacy.description";
    private static final String SEMANTIC_DATASET_SECURITY_AND_PRIVACY_TITLE = "rda.dataset.security_and_privacy.title";
    private static final String SEMANTIC_DATASET_METADATA_LANGUAGE = "rda.dataset.metadata.language";
    private static final String SEMANTIC_DATASET_METADATA_DESCRIPTION = "rda.dataset.metadata.description";
    private static final String SEMANTIC_DATASET_METADATA_STANDARD_ID = "rda.dataset.metadata.metadata_standard_id";
    private static final String SEMANTIC_DATASET_METADATA_STANDARD_ID_IDENTIFIER = "rda.dataset.metadata.metadata_standard_id.identifier";
    private static final String SEMANTIC_DATASET_METADATA_STANDARD_ID_TYPE = "rda.dataset.metadata.metadata_standard_id.type";
    private static final String SEMANTIC_DATASET_DATASET_ID = "rda.dataset.dataset_id";
    private static final String SEMANTIC_DATASET_DATASET_ID_ID = "rda.dataset.dataset_id.identifier";
    private static final String SEMANTIC_DATASET_DATASET_ID_TYPE = "rda.dataset.dataset_id.type";
    private static final String SEMANTIC_DATASET_DISTRIBUTION_ACCESS_URL = "rda.dataset.distribution.access_url";
    private static final String SEMANTIC_DATASET_DISTRIBUTION_AVAILABLE_UTIL = "rda.dataset.distribution.available_until";
    private static final String SEMANTIC_DATASET_DISTRIBUTION_BYTE_SIZE = "rda.dataset.distribution.byte_size";
    private static final String SEMANTIC_DATASET_DISTRIBUTION_DATA_ACCESS = "rda.dataset.distribution.data_access";
    private static final String SEMANTIC_DATASET_DISTRIBUTION_DESCRIPTION = "rda.dataset.distribution.description";
    private static final String SEMANTIC_DATASET_DISTRIBUTION_DOWNLOAD_URL = "rda.dataset.distribution.download_url";
    private static final String SEMANTIC_DATASET_DISTRIBUTION_FORMAT = "rda.dataset.distribution.format";
    private static final String SEMANTIC_DATASET_DISTRIBUTION_TITLE = "rda.dataset.distribution.title";
    private static final String SEMANTIC_DATASET_DISTRIBUTION_LICENCE = "rda.dataset.distribution.license";
    private static final String SEMANTIC_DATASET_DISTRIBUTION_LICENCE_LICENSE_REF = "rda.dataset.distribution.license.license_ref";
    private static final String SEMANTIC_DATASET_DISTRIBUTION_LICENCE_START_DATE = "rda.dataset.distribution.license.start_date";
    private static final String SEMANTIC_DATASET_DISTRIBUTION_HOST_AVAILABILITY = "rda.dataset.distribution.host.availability";
    private static final String SEMANTIC_DATASET_DISTRIBUTION_HOST_BACKUP_FREQUENCY = "rda.dataset.distribution.host.backup_frequency";
    private static final String SEMANTIC_DATASET_DISTRIBUTION_HOST_BACKUP_TYPE = "rda.dataset.distribution.host.backup_type";
    private static final String SEMANTIC_DATASET_DISTRIBUTION_HOST_CERTIFIED_WITH = "rda.dataset.distribution.host.certified_with";
    private static final String SEMANTIC_DATASET_DISTRIBUTION_HOST_DESCRIPTION = "rda.dataset.distribution.host.description";
    private static final String SEMANTIC_DATASET_DISTRIBUTION_HOST_GEO_LOCATION = "rda.dataset.distribution.host.geo_location";
    private static final String SEMANTIC_DATASET_DISTRIBUTION_HOST_PID_SYSTEM = "rda.dataset.distribution.host.pid_system";
    private static final String SEMANTIC_DATASET_DISTRIBUTION_HOST_STORAGE_TYPE = "rda.dataset.distribution.host.storage_type";
    private static final String SEMANTIC_DATASET_DISTRIBUTION_HOST_SUPPORT_VERSIONING = "rda.dataset.distribution.host.supports_versioning";
    private static final String SEMANTIC_DATASET_DISTRIBUTION_HOST_TITLE = "rda.dataset.distribution.host.title";
    private static final String SEMANTIC_DATASET_DISTRIBUTION_HOST_URL = "rda.dataset.distribution.host.url";
    private static final String SEMANTIC_DATASET_CONTRIBUTOR = "rda.dmp.contributor";
    
    @Autowired
    public RdaFileTransformerService(FileStorageService storageService, JsonHandlingService jsonHandlingService, RdaFileTransformerServiceConfiguration configuration, TemplateFieldSearcherService templateFieldSearcherService) {
        this.storageService = storageService;
	    this.configuration = configuration;
	    this.jsonHandlingService = jsonHandlingService;
	    this.templateFieldSearcherService = templateFieldSearcherService;
    }
    @Override
    public FileEnvelopeModel exportPlan(PlanModel planFileTransformerModel, String variant)  {
        Dmp dmp = this.buildRdaDmp(planFileTransformerModel);
        RDAModel rdaModel = new RDAModel();
        rdaModel.setDmp(dmp);
        String dmpJson = jsonHandlingService.toJsonSafe(rdaModel);
        
        byte[] bytes = dmpJson.getBytes(StandardCharsets.UTF_8);
        FileEnvelopeModel wordFile = new FileEnvelopeModel();
        if (this.getConfiguration().isUseSharedStorage()) {
            String fileRef = this.storageService.storeFile(bytes);
            wordFile.setFileRef(fileRef);
        } else {
            wordFile.setFile(bytes);
        }
        wordFile.setFilename(planFileTransformerModel.getLabel() + ".json");
        return wordFile;
    }

    @Override
    public FileEnvelopeModel exportDescription(DescriptionModel descriptionFileTransformerModel, String format) {
        throw new MyApplicationException("Not implemented");
//        Dmp dmp = this.dmpRDAMapper.toRDA(descriptionFileTransformerModel.getDmp());
//        Map<String, Object> datasetExtraData = new HashMap<>();
//        datasetExtraData.put("dmp", dmp);
//        Dataset dataset = this.descriptionRDAMapper.toRDA(descriptionFileTransformerModel, datasetExtraData);
//        String dmpJson = jsonHandlingService.toJsonSafe(dataset);
//
//        byte[] bytes = dmpJson.getBytes(StandardCharsets.UTF_8);
//        FileEnvelopeModel wordFile = new FileEnvelopeModel();
//        if (this.getConfiguration().isUseSharedStorage()) {
//            String fileRef = this.storageService.storeFile(bytes);
//            wordFile.setFileRef(fileRef);
//        } else {
//            wordFile.setFile(bytes);
//        }
//        wordFile.setFilename(descriptionFileTransformerModel.getLabel() + ".json");
//        return wordFile;
    }
    
    //region export
    
    //region dmp
    
    private Dmp buildRdaDmp(PlanModel plan){
        if (plan == null) throw new MyApplicationException("Plan is missing");
        if (plan.getCreatedAt() == null) throw new MyApplicationException("Plan Created is missing");
        if (plan.getUpdatedAt() == null) throw new MyApplicationException("Plan Modified is missing");
        if (plan.getLabel() == null) throw new MyApplicationException("Plan Label is missing");
        if (plan.getProperties() == null)  throw new MyApplicationException("Plan is missing language and contact properties");

        List<ReferenceModel> grants = this.getReferenceModelOfTypeCode(plan, this.configuration.getRdaFileTransformerServiceProperties().getGrantReferenceCode());
        List<ReferenceModel> researchers = this.getReferenceModelOfTypeCode(plan, this.configuration.getRdaFileTransformerServiceProperties().getResearcherReferenceCode());
        List<ReferenceModel> funders = this.getReferenceModelOfTypeCode(plan, this.configuration.getRdaFileTransformerServiceProperties().getFunderReferenceCode());
        List<ReferenceModel> projects = this.getReferenceModelOfTypeCode(plan, this.configuration.getRdaFileTransformerServiceProperties().getProjectReferenceCode());
        List<ReferenceModel> licences = this.getReferenceModelOfTypeCode(plan, this.configuration.getRdaFileTransformerServiceProperties().getLicenceReferenceCode());

        Dmp rda = new Dmp();
        rda.setDmpId(this.buildRdaDmpId(plan));
        rda.setCreated(plan.getCreatedAt());
        rda.setDescription(plan.getDescription());
        rda.setModified(plan.getUpdatedAt());
        rda.setTitle(plan.getLabel());
        rda.setLanguage(this.buildRdaLanguage(plan.getLanguage() != null ? plan.getLanguage() : "en"));
        rda.setContributor(buildRdaContributors(researchers, plan.getUsers()));
        rda.setContact(buildRdaContact(plan));
        rda.setEthicalIssuesExist(buildRdaEthicalIssuesExist(plan));
        rda.setEthicalIssuesDescription(buildRdaEthicalIssuesDescription(plan));
        rda.setEthicalIssuesReport(buildRdaEthicalIssueReport(plan));
        if (projects.size() == 1 && grants.size() == 1 && funders.size() == 1) { //TODO: Project hierarchy not implemented should rewrite this logic 
            rda.setProject(buildRdaProjects(projects, grants, funders));
        } else if (!projects.isEmpty()){
            rda.setProject(buildRdaProjects(projects, new ArrayList<>(), new ArrayList<>()));
        } else if (grants.size() == 1 && funders.size() == 1) {
            rda.setProject(buildRdaProjects(grants, grants, funders));
        }
        Cost cost = buildRdaCost(plan);
        rda.setCost(new ArrayList<>());
        if (cost != null) rda.getCost().add(cost);

        if (plan.getDescriptions() != null){
            rda.setDataset(new ArrayList<>());
            for (DescriptionModel descriptionModel : plan.getDescriptions()) rda.getDataset().add(this.buildRdaDataset(descriptionModel, rda));
        }
        
        return rda;
    }

    public Cost buildRdaCost(PlanModel model) {
        if (model == null) throw new MyApplicationException("Plan is missing");
        Cost cost = new Cost();
        
        List<org.opencdmp.commonmodels.models.planblueprint.FieldModel> titleFields = this.getFieldsOfSemantic(model.getPlanBlueprint(), SEMANTIC_COST_TITLE);
        for(org.opencdmp.commonmodels.models.planblueprint.FieldModel field : titleFields) {
            PlanBlueprintValueModel valueModel = field != null ? this.getPLanBlueprintValue(model, field.getId()) : null;
            if (valueModel != null && valueModel.getValue() != null && !valueModel.getValue().isBlank()) cost.setTitle(valueModel.getValue());
        }

        List<org.opencdmp.commonmodels.models.planblueprint.FieldModel> descriptionFields = this.getFieldsOfSemantic(model.getPlanBlueprint(), SEMANTIC_COST_DESCRIPTION);
        for(org.opencdmp.commonmodels.models.planblueprint.FieldModel field : descriptionFields) {
            PlanBlueprintValueModel valueModel = field != null ? this.getPLanBlueprintValue(model, field.getId()) : null;
            if (valueModel != null && valueModel.getValue() != null && !valueModel.getValue().isBlank()) cost.setDescription(valueModel.getValue());
        }

        List<org.opencdmp.commonmodels.models.planblueprint.FieldModel> valueFields = this.getFieldsOfSemantic(model.getPlanBlueprint(), SEMANTIC_COST_VALUE);
        for(org.opencdmp.commonmodels.models.planblueprint.FieldModel field : valueFields) {
            PlanBlueprintValueModel valueModel = field != null ? this.getPLanBlueprintValue(model, field.getId()) : null;
            if (valueModel != null && valueModel.getNumberValue() != null) cost.setValue(valueModel.getNumberValue());
        }

        List<org.opencdmp.commonmodels.models.planblueprint.FieldModel> codeFields = this.getFieldsOfSemantic(model.getPlanBlueprint(), SEMANTIC_COST_CURRENCY_CODE);
        for(org.opencdmp.commonmodels.models.planblueprint.FieldModel field : codeFields) {
            PlanBlueprintValueModel valueModel = field != null ? this.getPLanBlueprintValue(model, field.getId()) : null;
            if (valueModel != null && valueModel.getValue() != null && !valueModel.getValue().isBlank()) {
                try {
                    cost.setCurrencyCode(Cost.CurrencyCode.fromValue(valueModel.getValue()));
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    logger.warn("Dmp cost currency code is not valid and will not be set.");
                }
            }
        }
        return cost.getValue() != null ? cost : null;
    }

    public Project buildRdaProject(ReferenceModel project, List<ReferenceModel> grants,  List<ReferenceModel> funders) {
        if (project == null) throw new MyApplicationException("Project is missing");
        if (project.getLabel() == null || project.getLabel().isBlank()) throw new MyApplicationException("Project Title is missing");
        Project rda = new Project();
        rda.setTitle(project.getLabel());
        rda.setDescription(project.getDescription());
        if (project.getDefinition() != null && project.getDefinition().getFields() != null) {
	        project.getDefinition().getFields().stream().filter(field -> this.configuration.getRdaFileTransformerServiceProperties().getProjectStartDateCode().equalsIgnoreCase(field.getCode()) && field.getValue() != null && !field.getValue().isBlank()).map(ReferenceFieldModel::getValue).findFirst().ifPresent(rda::setStart);
	        project.getDefinition().getFields().stream().filter(field -> this.configuration.getRdaFileTransformerServiceProperties().getProjectEndDateCode().equalsIgnoreCase(field.getCode()) && field.getValue() != null && !field.getValue().isBlank()).map(ReferenceFieldModel::getValue).findFirst().ifPresent(rda::setEnd);
        }
        if (!funders.isEmpty()) rda.setFunding(List.of(this.buildRRdaFunding(funders.getFirst(), grants.isEmpty() ? null : grants.getFirst())));

        return rda;
    }

    public List<Project> buildRdaProjects(List<ReferenceModel> projects, List<ReferenceModel> grants,  List<ReferenceModel> funder) {
        List<Project> rdaModels = new ArrayList<>();
        if (projects == null) return rdaModels;
        for (ReferenceModel userDMP : projects){
            rdaModels.add(this.buildRdaProject(userDMP, grants, funder));
        }
        return rdaModels;
    }

    public Funding buildRRdaFunding(ReferenceModel funder, ReferenceModel grant) {
        if (grant == null && funder == null) return null;
        Funding rda = new Funding();
        if (funder != null && funder.getReference() != null) {
            FunderId funderId = new FunderId();
            funderId.setIdentifier(funder.getReference());
            funderId.setType(FunderId.Type.FUNDREF);
            rda.setFunderId(funderId);
        } else if (funder != null){
            FunderId funderId = new FunderId();
            funderId.setIdentifier(funder.getId().toString());
            funderId.setType(FunderId.Type.OTHER);
            rda.setFunderId(funderId);
        }
        if (grant != null && grant.getReference() != null) {
            GrantId grantId = new GrantId();
            grantId.setIdentifier(grant.getReference());
            grantId.setType(GrantId.Type.OTHER);
            rda.setGrantId(grantId);
        } else if (grant != null) {
            GrantId grantId = new GrantId();
            grantId.setIdentifier(grant.getId().toString());
            grantId.setType(GrantId.Type.OTHER);
            rda.setGrantId(grantId);
        }
        return rda;
    }


    public Dmp.EthicalIssuesExist buildRdaEthicalIssuesExist(PlanModel model) {
        if (model == null) throw new MyApplicationException("Plan is missing");
        List<org.opencdmp.commonmodels.models.planblueprint.FieldModel> fields = this.getFieldsOfSemantic(model.getPlanBlueprint(), SEMANTIC_ETHICAL_ISSUES_EXISTS);
        for(org.opencdmp.commonmodels.models.planblueprint.FieldModel field : fields) {
            PlanBlueprintValueModel valueModel = field != null ? this.getPLanBlueprintValue(model, field.getId()) : null;
            if (valueModel != null && valueModel.getValue() != null && !valueModel.getValue().isBlank()) return Dmp.EthicalIssuesExist.fromValue(valueModel.getValue());
        }
        return Dmp.EthicalIssuesExist.UNKNOWN;
    }

    public URI buildRdaEthicalIssueReport(PlanModel model) {
        if (model == null) throw new MyApplicationException("Plan is missing");
        List<org.opencdmp.commonmodels.models.planblueprint.FieldModel> fields = this.getFieldsOfSemantic(model.getPlanBlueprint(), SEMANTIC_ETHICAL_ISSUES_REPORT);
        for(org.opencdmp.commonmodels.models.planblueprint.FieldModel field : fields) {
            PlanBlueprintValueModel valueModel = field != null ? this.getPLanBlueprintValue(model, field.getId()) : null;
            if (valueModel != null && valueModel.getValue() != null && !valueModel.getValue().isBlank()) return URI.create(valueModel.getValue());
        }
        return null;
    }

    public String buildRdaEthicalIssuesDescription(PlanModel model) {
        if (model == null) throw new MyApplicationException("Plan is missing");
        List<org.opencdmp.commonmodels.models.planblueprint.FieldModel> fields = this.getFieldsOfSemantic(model.getPlanBlueprint(), SEMANTIC_ETHICAL_ISSUES_DESCRIPTION);
        for(org.opencdmp.commonmodels.models.planblueprint.FieldModel field : fields) {
            PlanBlueprintValueModel valueModel = field != null ? this.getPLanBlueprintValue(model, field.getId()) : null;
            if (valueModel != null && valueModel.getValue() != null && !valueModel.getValue().isBlank()) return valueModel.getValue();
        }
        return null;
    }

    public Contact buildRdaContact(PlanModel model) {
        if (model == null) throw new MyApplicationException("Plan is missing");
        UserModel creator = model.getCreator();
        if (creator == null) {
            creator = model.getUsers().stream().filter(x -> x.getRole().equals(PlanUserRole.Owner) && x.getUser() != null && x.getUser().getContacts() != null && x.getUser().getContacts()
                    .stream().anyMatch(y -> y.getType().equals(ContactInfoType.Email) && y.getValue() != null && !y.getValue().isBlank())).map(PlanUserModel::getUser).findFirst().orElse(null);
        }

        if (creator == null) throw new MyApplicationException("Creator Name is missing");
        if (creator.getName() == null) throw new MyApplicationException("Contact Name is missing");
        UserContactInfoModel emailContact = creator.getContacts() != null ? creator.getContacts().stream().filter(userContactInfo -> userContactInfo.getType().equals(ContactInfoType.Email)).findFirst().orElse(null) : null;

        Contact rdaModel = new Contact();
        rdaModel.setName(creator.getName());

        if (emailContact != null) {
            rdaModel.setMbox(emailContact.getValue());
            if (emailContact.getId() != null) {
                ContactId rdaContactId = new ContactId();
                rdaContactId.setIdentifier(emailContact.getId().toString());
                rdaContactId.setType(ContactId.Type.OTHER);
                rdaModel.setContactId(rdaContactId);
            }
        }
        
        return rdaModel;
    }


    public List<Contributor> buildRdaContributors(List<ReferenceModel> referenceModels, List<PlanUserModel> userModels) {
        List<Contributor> rdaModels = new ArrayList<>();
        for (ReferenceModel researcher : referenceModels){
            Contributor item = this.buildRdaContributor(researcher);
            if (item != null) rdaModels.add(item);
        }
        if (userModels != null && !userModels.isEmpty()) {
            if (userModels.stream().anyMatch(x -> x.getUser() == null || x.getUser().getId() == null|| x.getUser().getName() == null || x.getUser().getName().isEmpty())) throw new MyApplicationException("User is missing");
            Map<UUID, List<PlanUserModel>> userModelsGrouped = userModels.stream().collect(Collectors.groupingBy(x -> x.getUser().getId()));
            if (userModelsGrouped != null) {
                for (UUID userId: userModelsGrouped.keySet()) {
                    rdaModels.add(this.buildRdaContributorsByUser(userModelsGrouped.get(userId)));
                }
            }
        }

        return rdaModels;
    }

    public Contributor buildRdaContributorsByUser(List<PlanUserModel> useModelGroupByRoles) {
        Contributor rdaModel = new Contributor();
        rdaModel.setContributorId(buildRdaContributorId(useModelGroupByRoles.getFirst().getUser().getId().toString()));

        rdaModel.setName(useModelGroupByRoles.getFirst().getUser().getName());
        UserContactInfoModel emailContact = useModelGroupByRoles.getFirst().getUser().getContacts() == null ? null : useModelGroupByRoles.getFirst().getUser().getContacts().stream().filter(userContactInfo -> userContactInfo.getType().equals(ContactInfoType.Email)).findFirst().orElse(null);
        if (emailContact != null) {
            rdaModel.setMbox(emailContact.getValue());
        }
        rdaModel.setRole(new HashSet<>(useModelGroupByRoles.stream().map(x -> x.getRole().name()).collect(Collectors.toList())));
        return rdaModel;
    }

    public Contributor buildRdaContributor(ReferenceModel model) {
        if (model == null) throw new MyApplicationException("Contributor is missing");

        Contributor rdaModel = new Contributor();
        rdaModel.setContributorId(buildRdaContributorId(model.getReference()));
        rdaModel.setName(model.getLabel());
        rdaModel.setRole(new HashSet<>(List.of(model.getType().getName())));
        if (model.getDefinition() != null && model.getDefinition().getFields() != null) {
            model.getDefinition().getFields().stream().filter(field -> this.configuration.getRdaFileTransformerServiceProperties().getResearcherMalCode().equalsIgnoreCase(field.getCode())).findFirst().ifPresent(emailField -> rdaModel.setMbox(emailField.getValue()));
        }
        return rdaModel;
    }

    public ContributorId buildRdaContributorId(String id) {
        if (id == null || id.isBlank()) throw  new MyApplicationException("ContributorId is missing"); 
        ContributorId rda = new ContributorId();
        String[] idParts = id.split(":");
        String prefix = idParts.length > 1 ? idParts[0] : id;
        if (prefix.equals(PREFIX_ORCID)) {
            String finalId = id.replace(prefix + ":", "");
            rda.setIdentifier(ORCID_URL + finalId);
            rda.setType(ContributorId.Type.ORCID);
        } else {
            rda.setIdentifier(id);
            rda.setType(ContributorId.Type.OTHER);
        }
        return rda;
    }

    private DmpId buildRdaDmpId(PlanModel plan){
        if (plan == null) throw new MyApplicationException("Plan is missing");
        EntityDoiModel model = plan.getEntityDois() == null ? null : plan.getEntityDois().stream().filter(x -> x.getRepositoryId() != null && x.getDoi() != null && !x.getDoi().isBlank() && x.getRepositoryId().equalsIgnoreCase(REPOSITORY_ZENODO)).findFirst().orElse(null);
        DmpId rdaModel = new DmpId();
        if (model == null){
            rdaModel.setIdentifier(plan.getId().toString());
            rdaModel.setType(DmpId.Type.OTHER);
        } else {
            rdaModel.setIdentifier(model.getDoi());
            rdaModel.setType(DmpId.Type.DOI);
        }
        
        return rdaModel;
    }

    private Language buildRdaLanguage(String language){
        if (language == null) throw new MyApplicationException("Language is missing");
        String rdaLanguage = this.configuration.getRdaFileTransformerServiceProperties().getLanguageMap().getOrDefault(language, null);

        return rdaLanguage != null ? Language.fromValue(rdaLanguage) : null;
    }

    private List<ReferenceModel> getReferenceModelOfTypeCode(PlanModel plan, String code){
        List<ReferenceModel> response = new ArrayList<>();
        if (plan.getReferences() == null) return response;
        for (PlanReferenceModel planReferenceModel : plan.getReferences()){
            if (planReferenceModel.getReference() != null && planReferenceModel.getReference().getType() != null && planReferenceModel.getReference().getType().getCode() != null  && planReferenceModel.getReference().getType().getCode().equals(code)){
                response.add(planReferenceModel.getReference());
            }
        }

        return response;
    }

    private List<org.opencdmp.commonmodels.models.planblueprint.FieldModel> getFieldsOfSemantic(PlanBlueprintModel blueprintModel, String semanticKey){
        if (blueprintModel == null || blueprintModel.getDefinition() == null || blueprintModel.getDefinition().getSections() == null) return null;
        List<org.opencdmp.commonmodels.models.planblueprint.FieldModel> fieldModels = new ArrayList<>();
        for (SectionModel sectionModel : blueprintModel.getDefinition().getSections()){
            if (sectionModel.getFields() != null){
	            sectionModel.getFields().stream().filter(x -> x.getSemantics() != null && x.getSemantics().contains(semanticKey)).findFirst().ifPresent(fieldModels::add);
            }
        }
        return fieldModels;
    }

    private PlanBlueprintValueModel getPLanBlueprintValue(PlanModel plan, UUID id){
        if (plan == null || plan.getProperties() == null || plan.getProperties().getPlanBlueprintValues() == null) return null;
        return plan.getProperties().getPlanBlueprintValues().stream().filter(x-> x.getFieldId().equals(id)).findFirst().orElse(null);
    }
    
    //endregion
    
    //region dataset

    private Dataset buildRdaDataset(DescriptionModel model, Dmp dmpRda){
        if (model == null) throw new MyApplicationException("Model is missing");
        if (model.getLabel() == null)  throw new MyApplicationException("Dataset Label is missing");

        Dataset rda = new Dataset();
        rda.setDatasetId(this.buildRdaDatasetId(model));
        rda.setTitle(model.getLabel());
        rda.setDescription(model.getDescription());
        rda.setLanguage(this.buildRdaDatasetLanguage(model));
        rda.setType(this.buildRdaDatasetType(model));
        rda.setIssued(this.buildRdaDatasetIssued(model));
        rda.setKeyword(this.buildRdaDatasetKeywords(model));
        if (rda.getKeyword().isEmpty() && model.getTags() != null){
            rda.getKeyword().addAll(model.getTags());
        }
        rda.setPersonalData(this.buildRdaDatasetPersonalData(model));
        rda.setSensitiveData(this.buildRdaDatasetSensitiveData(model));
        rda.setPreservationStatement(this.buildRdaDatasetPreservationStatement(model));
        rda.setTechnicalResource(this.buildRdaDatasetTechnicalResources(model));
        rda.setSecurityAndPrivacy(this.buildRdaDatasetSecurityAndPrivacy(model));
        rda.setMetadata(this.buildRdaDatasetMetadata(model));
        rda.setDataQualityAssurance(this.buildDataQualityAssurances(model));
        rda.setDistribution(this.buildRdaDistribution(model));

        dmpRda.getCost().addAll(this.buildRdaCosts(model));
        dmpRda.setContributor(this.buildContributor(model, dmpRda.getContributor()));
        this.mergeEthicalIssues(model, dmpRda);
            
        return rda;
    }

    private List<Contributor> buildContributor(DescriptionModel model, List<Contributor> values){
        if (values == null) values = new ArrayList<>();
        for (FieldModel field : this.templateFieldSearcherService.searchFieldsBySemantics(model.getDescriptionTemplate(), SEMANTIC_DATASET_CONTRIBUTOR)) {
            List<org.opencdmp.commonmodels.models.description.FieldModel> fieldValues = this.findValueField(field, model.getProperties());
            for (org.opencdmp.commonmodels.models.description.FieldModel fieldValue : fieldValues) {
                if (fieldValue.getReferences() != null  && !fieldValue.getReferences().isEmpty()) {
                    for (ReferenceModel referenceModel : fieldValue.getReferences()){
                        Contributor contributor = this.buildRdaContributor(referenceModel);
                        if (values.isEmpty()) values.add(contributor);
                        else if (values.stream().filter(x -> x.getContributorId().getIdentifier().equals(contributor.getContributorId().getIdentifier())).findFirst().orElse(null) == null) values.add(contributor);
                    }
                }
            }
        }
        return values;
    }
    
    
    private void mergeEthicalIssues(DescriptionModel model, Dmp dmpRda){
        for (FieldModel field : this.templateFieldSearcherService.searchFieldsBySemantics(model.getDescriptionTemplate(), SEMANTIC_ETHICAL_ISSUES_EXISTS)) {
            List<org.opencdmp.commonmodels.models.description.FieldModel> fieldValues = this.findValueField(field, model.getProperties());
            for (org.opencdmp.commonmodels.models.description.FieldModel fieldValue : fieldValues) {
                if (fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
                    try {
                        Dmp.EthicalIssuesExist value = Dmp.EthicalIssuesExist.fromValue(fieldValue.getTextValue());
                        if (!Dmp.EthicalIssuesExist.UNKNOWN.equals(value)) dmpRda.setEthicalIssuesExist(value);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }
        for (FieldSetModel fieldSet : this.templateFieldSearcherService.searchFieldSetsBySemantics(model.getDescriptionTemplate(), List.of(SEMANTIC_ETHICAL_ISSUES_EXISTS, SEMANTIC_ETHICAL_ISSUES_DESCRIPTION, SEMANTIC_ETHICAL_ISSUES_REPORT))) {
            List<org.opencdmp.commonmodels.models.description.PropertyDefinitionFieldSetItemModel> propertyDefinitionFieldSetItemModels = this.findFieldSetValue(fieldSet, model.getProperties());
            for (org.opencdmp.commonmodels.models.description.PropertyDefinitionFieldSetItemModel propertyDefinitionFieldSetItemModel : propertyDefinitionFieldSetItemModels) {

                org.opencdmp.commonmodels.models.description.FieldModel fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_ETHICAL_ISSUES_EXISTS);
                if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
                    try {
                        Dmp.EthicalIssuesExist value = Dmp.EthicalIssuesExist.fromValue(fieldValue.getTextValue());
                        if (!Dmp.EthicalIssuesExist.UNKNOWN.equals(value)) dmpRda.setEthicalIssuesExist(value);
                    } catch (Exception e) { logger.error(e.getMessage(), e); }
                }
                if (fieldValue != null && fieldValue.getTextListValue() != null && !fieldValue.getTextListValue().isEmpty()) {
                    for (String val : fieldValue.getTextListValue()) {
                        try {
                            Dmp.EthicalIssuesExist value = Dmp.EthicalIssuesExist.fromValue(val);
                            if (!Dmp.EthicalIssuesExist.UNKNOWN.equals(value)) dmpRda.setEthicalIssuesExist(value);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }

                if (dmpRda.getEthicalIssuesDescription() == null || dmpRda.getEthicalIssuesDescription().isBlank()) {
                    fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_ETHICAL_ISSUES_DESCRIPTION);
                    if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
                        dmpRda.setEthicalIssuesDescription(fieldValue.getTextValue());
                    }
                }

                if (dmpRda.getEthicalIssuesReport() == null) {
                    fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_ETHICAL_ISSUES_REPORT);
                    if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
                        try {
                            dmpRda.setEthicalIssuesReport(URI.create(fieldValue.getTextValue()));
                        } catch (Exception ex) {
                            logger.error(ex.getMessage(), ex);
                        }
                    }
                }
            }
        }
    }

    private List<Cost> buildRdaCosts(DescriptionModel model){
        List<Cost> values = new ArrayList<>();
        for (FieldSetModel fieldSet : this.templateFieldSearcherService.searchFieldSetsBySemantics(model.getDescriptionTemplate(), List.of(SEMANTIC_COST_CURRENCY_CODE, SEMANTIC_COST_VALUE, SEMANTIC_COST_DESCRIPTION, SEMANTIC_COST_TITLE))) {
            List<org.opencdmp.commonmodels.models.description.PropertyDefinitionFieldSetItemModel> propertyDefinitionFieldSetItemModels = this.findFieldSetValue(fieldSet, model.getProperties());
            for (org.opencdmp.commonmodels.models.description.PropertyDefinitionFieldSetItemModel propertyDefinitionFieldSetItemModel : propertyDefinitionFieldSetItemModels) {
                boolean valueFound = false;
                Cost item = new Cost();

                org.opencdmp.commonmodels.models.description.FieldModel fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_COST_CURRENCY_CODE);
                if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
                    try {
                        item.setCurrencyCode(Cost.CurrencyCode.fromValue(fieldValue.getTextValue()));
                        valueFound = true;
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }

                fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_COST_DESCRIPTION);
                if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
                    item.setDescription(fieldValue.getTextValue());
                    valueFound = true;
                }

                fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_COST_TITLE);
                if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
                    item.setTitle(fieldValue.getTextValue());
                    valueFound = true;
                }

                fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_COST_VALUE);
                if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
                    try {
                        item.setValue(Double.valueOf(fieldValue.getTextValue()));
                        valueFound = true;
                    } catch (Exception ex) {
                        logger.error(ex.getMessage(), ex);
                    }
                }

                if (valueFound) values.add(item);
            }
        }
        return values;
    }

    private List<Distribution> buildRdaDistribution(DescriptionModel model){
        List<Distribution> values = new ArrayList<>();
        for (FieldSetModel fieldSet : this.templateFieldSearcherService.searchFieldSetsBySemantics(model.getDescriptionTemplate(), List.of(
                SEMANTIC_DATASET_DISTRIBUTION_LICENCE, SEMANTIC_DATASET_DISTRIBUTION_LICENCE_LICENSE_REF, SEMANTIC_DATASET_DISTRIBUTION_LICENCE_START_DATE,
                SEMANTIC_DATASET_DISTRIBUTION_TITLE, SEMANTIC_DATASET_DISTRIBUTION_FORMAT,
                SEMANTIC_DATASET_DISTRIBUTION_DOWNLOAD_URL, SEMANTIC_DATASET_DISTRIBUTION_DESCRIPTION, SEMANTIC_DATASET_DISTRIBUTION_DATA_ACCESS,
                SEMANTIC_DATASET_DISTRIBUTION_BYTE_SIZE, SEMANTIC_DATASET_DISTRIBUTION_AVAILABLE_UTIL, SEMANTIC_DATASET_DISTRIBUTION_ACCESS_URL,
                SEMANTIC_DATASET_DISTRIBUTION_HOST_TITLE, SEMANTIC_DATASET_DISTRIBUTION_HOST_URL, SEMANTIC_DATASET_DISTRIBUTION_HOST_AVAILABILITY,
                SEMANTIC_DATASET_DISTRIBUTION_HOST_BACKUP_FREQUENCY, SEMANTIC_DATASET_DISTRIBUTION_HOST_BACKUP_TYPE, SEMANTIC_DATASET_DISTRIBUTION_HOST_CERTIFIED_WITH,
                SEMANTIC_DATASET_DISTRIBUTION_HOST_DESCRIPTION, SEMANTIC_DATASET_DISTRIBUTION_HOST_GEO_LOCATION, SEMANTIC_DATASET_DISTRIBUTION_HOST_PID_SYSTEM,
                SEMANTIC_DATASET_DISTRIBUTION_HOST_STORAGE_TYPE, SEMANTIC_DATASET_DISTRIBUTION_HOST_SUPPORT_VERSIONING))) {
            List<org.opencdmp.commonmodels.models.description.PropertyDefinitionFieldSetItemModel> propertyDefinitionFieldSetItemModels = this.findFieldSetValue(fieldSet, model.getProperties());
            for (org.opencdmp.commonmodels.models.description.PropertyDefinitionFieldSetItemModel propertyDefinitionFieldSetItemModel : propertyDefinitionFieldSetItemModels) {
                boolean valueFound = false;
                Distribution item = new Distribution();

                org.opencdmp.commonmodels.models.description.FieldModel fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_DATASET_DISTRIBUTION_TITLE);
                if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
                    item.setTitle(fieldValue.getTextValue());
                    valueFound = true;
                }

                fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_DATASET_DISTRIBUTION_FORMAT);
                item.setFormat(new ArrayList<>());
                if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) { 
                    item.getFormat().add(fieldValue.getTextValue());
                    valueFound = true;
                }
                if (fieldValue != null && fieldValue.getTextListValue() != null && !fieldValue.getTextListValue().isEmpty()) {
                    item.getFormat().addAll(fieldValue.getTextListValue());
                    valueFound = true;
                }

                fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_DATASET_DISTRIBUTION_DOWNLOAD_URL);
                if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
                    try {
                        item.setDownloadUrl(URI.create(fieldValue.getTextValue()));
                        valueFound = true;
                    } catch (Exception ex) { logger.error(ex.getMessage(), ex); }
                }

                fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_DATASET_DISTRIBUTION_DESCRIPTION);
                if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
                    item.setDescription(fieldValue.getTextValue());
                    valueFound = true;
                }

                fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_DATASET_DISTRIBUTION_DATA_ACCESS);
                if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
                    try {
                        item.setDataAccess(Distribution.DataAccess.fromValue(fieldValue.getTextValue()));
                        valueFound = true;
                    } catch (Exception ex) { logger.error(ex.getMessage(), ex); }
                }
                if (fieldValue != null && fieldValue.getTextListValue() != null && !fieldValue.getTextListValue().isEmpty()) {
                    for (String val : fieldValue.getTextListValue()) {
                        try {
                            item.setDataAccess(Distribution.DataAccess.fromValue(val));
                            valueFound = true;
                        } catch (Exception ex) {
                            logger.error(ex.getMessage(), ex);
                        }
                    }
                    
                }

                fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_DATASET_DISTRIBUTION_BYTE_SIZE);
                if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
                    try {
                        item.setByteSize(Integer.valueOf(fieldValue.getTextValue()));
                        valueFound = true;
                    } catch (Exception ex) { logger.error(ex.getMessage(), ex); }
                }

                fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_DATASET_DISTRIBUTION_AVAILABLE_UTIL);
                if (fieldValue != null && fieldValue.getDateValue() != null) {
                    LocalDateTime ldt = LocalDateTime.ofInstant(fieldValue.getDateValue(), ZoneOffset.UTC);
                    item.setAvailableUntil(ldt.format(DateTimeFormatter.ISO_LOCAL_DATE));
                    valueFound = true;
                } else if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
                    item.setAvailableUntil(fieldValue.getTextValue());
                    valueFound = true;
                }

                fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_DATASET_DISTRIBUTION_ACCESS_URL);
                if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
                    item.setAccessUrl(fieldValue.getTextValue());
                    valueFound = true;
                }

                fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_DATASET_DISTRIBUTION_LICENCE);
                if (fieldValue != null && fieldValue.getReferences() != null && !fieldValue.getReferences().isEmpty()) {
                    item.setLicense(new ArrayList<>());
                    for (ReferenceModel referenceModel : fieldValue.getReferences()) {
                        if (referenceModel.getDefinition() != null && referenceModel.getDefinition().getFields() != null) {
                            try {
                                License license = new License();
                                String refValue = referenceModel.getReference();
                                if (refValue != null && !refValue.isBlank()) license.setLicenseRef(URI.create(refValue));
                                String startDateValue = referenceModel.getDefinition().getFields().stream().filter(f -> this.configuration.getRdaFileTransformerServiceProperties().getLicenseStartDateCode().equalsIgnoreCase(f.getCode()) && f.getValue() != null && !f.getValue().isBlank()).map(ReferenceFieldModel::getValue).findFirst().orElse(null);
                                if (refValue != null && !refValue.isBlank()) license.setStartDate(startDateValue);
                                else license.setStartDate(Instant.now().toString());

                                item.getLicense().add(license);
                            } catch (Exception ex) {
                                logger.error(ex.getMessage(), ex);
                            }
                        }
                    }
                    if (!item.getLicense().isEmpty()) valueFound = true;
                }

                if (valueFound && item.getLicense() != null) item.getLicense().addAll(this.buildRdaDistributionLicense(propertyDefinitionFieldSetItemModel, fieldSet));
                else item.setLicense(this.buildRdaDistributionLicense(propertyDefinitionFieldSetItemModel, fieldSet));

                item.setHost(this.buildRdaDistributionHost(propertyDefinitionFieldSetItemModel, fieldSet));
                values.add(item);
            }
        }
        return values.isEmpty() ? null : values;
    }

    private List<License> buildRdaDistributionLicense(org.opencdmp.commonmodels.models.description.PropertyDefinitionFieldSetItemModel propertyDefinitionFieldSetItemModel, FieldSetModel fieldSet) {

        List<License> licenses = new ArrayList<>();
        License license = new License();
        org.opencdmp.commonmodels.models.description.FieldModel fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_DATASET_DISTRIBUTION_LICENCE_LICENSE_REF);
        if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
            try {
                license.setLicenseRef(URI.create(fieldValue.getTextValue()));
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }

        fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_DATASET_DISTRIBUTION_LICENCE_START_DATE);
        if (fieldValue != null && fieldValue.getDateValue() != null) {
            LocalDateTime ldt = LocalDateTime.ofInstant(fieldValue.getDateValue(), ZoneOffset.UTC);
            license.setStartDate(ldt.format(DateTimeFormatter.ISO_LOCAL_DATE));
        } else if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
            license.setStartDate(fieldValue.getTextValue());
        }

        if (license.getLicenseRef() != null || license.getStartDate() != null) {
            licenses.add(license);
        }
        return licenses;
    }

    private Host buildRdaDistributionHost(org.opencdmp.commonmodels.models.description.PropertyDefinitionFieldSetItemModel propertyDefinitionFieldSetItemModel, FieldSetModel fieldSet) {
        boolean valueFound = false;
        Host item = new Host();
        org.opencdmp.commonmodels.models.description.FieldModel fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_DATASET_DISTRIBUTION_HOST_TITLE);
        if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
            item.setTitle(fieldValue.getTextValue());
            valueFound = true;
        }

        fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_DATASET_DISTRIBUTION_HOST_URL);
        if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
            try {
                item.setUrl(URI.create(fieldValue.getTextValue()));
                valueFound = true;
            } catch (Exception ex) { logger.error(ex.getMessage(), ex); }
        }

        fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_DATASET_DISTRIBUTION_HOST_DESCRIPTION);
        if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
            item.setDescription(fieldValue.getTextValue());
            valueFound = true;
        }

        fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_DATASET_DISTRIBUTION_HOST_STORAGE_TYPE);
        if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
            item.setStorageType(fieldValue.getTextValue());
            valueFound = true;
        }

        fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_DATASET_DISTRIBUTION_HOST_BACKUP_TYPE);
        if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
            item.setBackupType(fieldValue.getTextValue());
            valueFound = true;
        }

        fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_DATASET_DISTRIBUTION_HOST_AVAILABILITY);
        if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
            item.setAvailability(fieldValue.getTextValue());
            valueFound = true;
        }

        fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_DATASET_DISTRIBUTION_HOST_BACKUP_FREQUENCY);
        if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
            item.setBackupFrequency(fieldValue.getTextValue());
            valueFound = true;
        }

        fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_DATASET_DISTRIBUTION_HOST_GEO_LOCATION);
        if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
            try {
                item.setGeoLocation(Host.GeoLocation.fromValue(fieldValue.getTextValue()));
                valueFound = true;
            } catch (Exception ex) { logger.error(ex.getMessage(), ex); }
        }
        if (fieldValue != null && fieldValue.getTextListValue() != null && !fieldValue.getTextListValue().isEmpty()) {
            for (String val : fieldValue.getTextListValue()) {
                try {
                    item.setGeoLocation(Host.GeoLocation.fromValue(val));
                    valueFound = true;
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        }

        fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_DATASET_DISTRIBUTION_HOST_SUPPORT_VERSIONING);
        if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
            try {
                item.setSupportVersioning(Host.SupportVersioning.fromValue(fieldValue.getTextValue()));
                valueFound = true;
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
        if (fieldValue != null && fieldValue.getTextListValue() != null && !fieldValue.getTextListValue().isEmpty()) {
            for (String val : fieldValue.getTextListValue()) {
                try {
                    item.setSupportVersioning(Host.SupportVersioning.fromValue(val));
                    valueFound = true;
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        }

        fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_DATASET_DISTRIBUTION_HOST_CERTIFIED_WITH);
        if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
            try {
                item.setCertifiedWith(Host.CertifiedWith.fromValue(fieldValue.getTextValue()));
                valueFound = true;
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
        if (fieldValue != null && fieldValue.getTextListValue() != null && !fieldValue.getTextListValue().isEmpty()) {
            for (String val : fieldValue.getTextListValue()) {
                try {
                    item.setCertifiedWith(Host.CertifiedWith.fromValue(val));
                    valueFound = true;
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        }
        
        fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_DATASET_DISTRIBUTION_HOST_PID_SYSTEM);
        item.setPidSystem(new ArrayList<>());
        if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
            try {
                item.getPidSystem().add(PidSystem.fromValue(fieldValue.getTextValue()));
                valueFound = true;
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
        if (fieldValue != null && fieldValue.getTextListValue() != null && !fieldValue.getTextListValue().isEmpty()) {
            for (String itm : fieldValue.getTextListValue()) {
                try {

                    item.getPidSystem().add(PidSystem.fromValue(itm));
                    valueFound = true;
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        }

        if (valueFound) return item;
        return null;
    }

    private List<String> buildDataQualityAssurances(DescriptionModel model){
        List<String> values = new ArrayList<>();
        for (FieldModel field : this.templateFieldSearcherService.searchFieldsBySemantics(model.getDescriptionTemplate(), SEMANTIC_DATASET_DATA_QUALITY_ASSURANCE)) {
            List<org.opencdmp.commonmodels.models.description.FieldModel> fieldValues = this.findValueField(field, model.getProperties());
            for (org.opencdmp.commonmodels.models.description.FieldModel fieldValue : fieldValues) {
                if (fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
                    values.add(fieldValue.getTextValue());
                }
                if (fieldValue.getTextListValue() != null && !fieldValue.getTextListValue().isEmpty()) values.addAll(fieldValue.getTextListValue());
            }
        }
        return values;
    }

    private DatasetId buildRdaDatasetId(DescriptionModel model){
        DatasetId datasetId = new DatasetId();
        datasetId.setType(DatasetId.Type.OTHER);

        for (FieldModel field : this.templateFieldSearcherService.searchFieldsBySemantics(model.getDescriptionTemplate(), SEMANTIC_DATASET_DATASET_ID)) {
            List<org.opencdmp.commonmodels.models.description.FieldModel> fieldValues = this.findValueField(field, model.getProperties());
            for (org.opencdmp.commonmodels.models.description.FieldModel fieldValue : fieldValues) {
                if (fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
                    datasetId.setIdentifier(fieldValue.getTextValue() );
                    return datasetId;
                }
                if (fieldValue.getExternalIdentifier() != null) {
                    datasetId.setIdentifier(fieldValue.getExternalIdentifier().getIdentifier());
                    try {
                        datasetId.setType(DatasetId.Type.fromValue(fieldValue.getExternalIdentifier().getType()));
                    } catch (Exception ex) {
                        logger.error(ex.getMessage(), ex);
                    }
                    return datasetId;
                }
            }
        }
        
        for (FieldSetModel fieldSet : this.templateFieldSearcherService.searchFieldSetsBySemantics(model.getDescriptionTemplate(), List.of(SEMANTIC_DATASET_DATASET_ID_ID, SEMANTIC_DATASET_DATASET_ID_TYPE))) {
            List<org.opencdmp.commonmodels.models.description.PropertyDefinitionFieldSetItemModel> propertyDefinitionFieldSetItemModels = this.findFieldSetValue(fieldSet, model.getProperties());
            for (org.opencdmp.commonmodels.models.description.PropertyDefinitionFieldSetItemModel propertyDefinitionFieldSetItemModel : propertyDefinitionFieldSetItemModels) {
                boolean valueFound = false;

                org.opencdmp.commonmodels.models.description.FieldModel fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_DATASET_DATASET_ID_ID);
                if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
                    datasetId.setIdentifier(fieldValue.getTextValue());
                    valueFound = true;
                }

                fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_DATASET_DATASET_ID_TYPE);
                if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
                    try {
                        datasetId.setType(DatasetId.Type.fromValue(fieldValue.getTextValue()));
                        valueFound = true;
                    } catch (Exception ex){
                        logger.error(ex.getMessage(), ex);
                    }
                }
                if (fieldValue != null && fieldValue.getTextListValue() != null && !fieldValue.getTextListValue().isEmpty()) {
                    for (String val : fieldValue.getTextListValue()) {
                        try {
                            datasetId.setType(DatasetId.Type.fromValue(val));
                            valueFound = true;
                        } catch (Exception ex) {
                            logger.error(ex.getMessage(), ex);
                        }
                    }
                }

                if (valueFound) return datasetId;
            }
        }
        return new DatasetId(model.getId().toString(), DatasetId.Type.OTHER);
    }

    private List<Metadatum> buildRdaDatasetMetadata(DescriptionModel model){
        List<Metadatum> values = new ArrayList<>();
        for (FieldSetModel fieldSet : this.templateFieldSearcherService.searchFieldSetsBySemantics(model.getDescriptionTemplate(), List.of(SEMANTIC_DATASET_METADATA_DESCRIPTION, SEMANTIC_DATASET_METADATA_LANGUAGE, SEMANTIC_DATASET_METADATA_STANDARD_ID, SEMANTIC_DATASET_METADATA_STANDARD_ID_IDENTIFIER, SEMANTIC_DATASET_METADATA_STANDARD_ID_TYPE))) {
            List<org.opencdmp.commonmodels.models.description.PropertyDefinitionFieldSetItemModel> propertyDefinitionFieldSetItemModels = this.findFieldSetValue(fieldSet, model.getProperties());
            for (org.opencdmp.commonmodels.models.description.PropertyDefinitionFieldSetItemModel propertyDefinitionFieldSetItemModel : propertyDefinitionFieldSetItemModels) {
                boolean valueFound = false;
                Metadatum item = new Metadatum();

                org.opencdmp.commonmodels.models.description.FieldModel fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_DATASET_METADATA_LANGUAGE);
                if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
                    try {
                        item.setLanguage(Metadatum.Language.fromValue(fieldValue.getTextValue()));
                        valueFound = true;
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
                if (fieldValue != null && fieldValue.getTextListValue() != null && !fieldValue.getTextListValue().isEmpty()) {
                    for (String val : fieldValue.getTextListValue()) {
                        try {
                            item.setLanguage(Metadatum.Language.fromValue(val));
                            valueFound = true;
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
                

                fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_DATASET_METADATA_DESCRIPTION);
                if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
                    item.setDescription(fieldValue.getTextValue());
                    valueFound = true;
                }
                MetadataStandardId standardId = this.buildRdaMetadataStandardId(propertyDefinitionFieldSetItemModel, fieldSet);
                if (standardId != null) {
                    item.setMetadataStandardId(standardId);
                    valueFound = true;

                }

                if (valueFound) values.add(item);
            }
        }
        return values.isEmpty() ? null : values;
    }

    private MetadataStandardId buildRdaMetadataStandardId(org.opencdmp.commonmodels.models.description.PropertyDefinitionFieldSetItemModel propertyDefinitionFieldSetItemModel, FieldSetModel fieldSet){
        boolean valueFound = false;
        MetadataStandardId standardId = new MetadataStandardId();
        standardId.setType(MetadataStandardId.Type.OTHER);

        org.opencdmp.commonmodels.models.description.FieldModel fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_DATASET_METADATA_STANDARD_ID);
        if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
            standardId.setIdentifier(fieldValue.getTextValue());
            return standardId;
        }

        fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_DATASET_METADATA_STANDARD_ID_IDENTIFIER);
        if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
            standardId.setIdentifier(fieldValue.getTextValue());
            valueFound = true;
        }

        fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_DATASET_METADATA_STANDARD_ID_TYPE);
        if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
            try {
                standardId.setType(MetadataStandardId.Type.fromValue(fieldValue.getTextValue()));
            } catch (Exception ex){
                logger.error(ex.getMessage(), ex);
            }
        }
        if (fieldValue != null && fieldValue.getTextListValue() != null && !fieldValue.getTextListValue().isEmpty()) {
            for (String val : fieldValue.getTextListValue()) {
                try {
                    standardId.setType(MetadataStandardId.Type.fromValue(val));
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        }

        if (valueFound) return standardId;
        return null;
    }

    private List<SecurityAndPrivacy> buildRdaDatasetSecurityAndPrivacy(DescriptionModel model) {
        List<SecurityAndPrivacy> values = new ArrayList<>();
        for (FieldSetModel fieldSet : this.templateFieldSearcherService.searchFieldSetsBySemantics(model.getDescriptionTemplate(), List.of(SEMANTIC_DATASET_SECURITY_AND_PRIVACY_TITLE, SEMANTIC_DATASET_SECURITY_AND_PRIVACY_DESCRIPTION))) {
            List<org.opencdmp.commonmodels.models.description.PropertyDefinitionFieldSetItemModel> propertyDefinitionFieldSetItemModels = this.findFieldSetValue(fieldSet, model.getProperties());
            for (org.opencdmp.commonmodels.models.description.PropertyDefinitionFieldSetItemModel propertyDefinitionFieldSetItemModel : propertyDefinitionFieldSetItemModels) {
                boolean valueFound = false;
                SecurityAndPrivacy item = new SecurityAndPrivacy();

                org.opencdmp.commonmodels.models.description.FieldModel fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_DATASET_SECURITY_AND_PRIVACY_TITLE);
                if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
                    item.setTitle(fieldValue.getTextValue());
                    valueFound = true;
                }

                fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_DATASET_SECURITY_AND_PRIVACY_DESCRIPTION);
                if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
                    item.setDescription(fieldValue.getTextValue());
                    valueFound = true;
                }

                if (valueFound) values.add(item);
            }
        }
        return values.isEmpty() ? null : values;
    }
        
        

    private List<TechnicalResource> buildRdaDatasetTechnicalResources(DescriptionModel model) {
        List<TechnicalResource> values = new ArrayList<>();
        for (FieldSetModel fieldSet : this.templateFieldSearcherService.searchFieldSetsBySemantics(model.getDescriptionTemplate(), List.of(SEMANTIC_DATASET_TECHNICAL_RESOURCE_NAME, SEMANTIC_DATASET_TECHNICAL_RESOURCE_DESCRIPTION))) {
            List<org.opencdmp.commonmodels.models.description.PropertyDefinitionFieldSetItemModel> propertyDefinitionFieldSetItemModels = this.findFieldSetValue(fieldSet, model.getProperties());
            for (org.opencdmp.commonmodels.models.description.PropertyDefinitionFieldSetItemModel propertyDefinitionFieldSetItemModel : propertyDefinitionFieldSetItemModels) {
                boolean valueFound = false;
                TechnicalResource item = new TechnicalResource();

                org.opencdmp.commonmodels.models.description.FieldModel fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_DATASET_TECHNICAL_RESOURCE_NAME);
                if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
                    item.setName(fieldValue.getTextValue());
                    valueFound = true;
                }

                fieldValue = this.findValueFieldBySemantic(fieldSet, propertyDefinitionFieldSetItemModel, SEMANTIC_DATASET_TECHNICAL_RESOURCE_DESCRIPTION);
                if (fieldValue != null && fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
                    item.setDescription(fieldValue.getTextValue());
                    valueFound = true;
                }

                if (valueFound) values.add(item);
            }
        }
        return values;

    }

    private String buildRdaDatasetPreservationStatement(DescriptionModel model){
        for (FieldModel field : this.templateFieldSearcherService.searchFieldsBySemantics(model.getDescriptionTemplate(), SEMANTIC_DATASET_PRESERVATION_STATEMENT)) {
            List<org.opencdmp.commonmodels.models.description.FieldModel> fieldValues = this.findValueField(field, model.getProperties());
            for (org.opencdmp.commonmodels.models.description.FieldModel fieldValue : fieldValues) {
                if (fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
                    return fieldValue.getTextValue();
                }
            }
        }
        return null;
    }

    private Dataset.SensitiveData buildRdaDatasetSensitiveData(DescriptionModel model){
        Dataset.SensitiveData value = null;
        for (FieldModel field : this.templateFieldSearcherService.searchFieldsBySemantics(model.getDescriptionTemplate(), SEMANTIC_DATASET_SENSITIVE_DATA)) {
            List<org.opencdmp.commonmodels.models.description.FieldModel> fieldValues = this.findValueField(field, model.getProperties());
            for (org.opencdmp.commonmodels.models.description.FieldModel fieldValue : fieldValues) {
                if (fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
                    try {
                        value = Dataset.SensitiveData.fromValue(fieldValue.getTextValue());
                        return value;
                    } catch (IllegalArgumentException | MyApplicationException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
                if (fieldValue.getTextListValue() != null && !fieldValue.getTextListValue().isEmpty()) {
                    for (String val : fieldValue.getTextListValue()) {
                        try {
                            value = Dataset.SensitiveData.fromValue(val);
                            return value;
                        } catch (IllegalArgumentException | MyApplicationException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }
        }
        return Dataset.SensitiveData.UNKNOWN;
    }
    
    private Dataset.PersonalData buildRdaDatasetPersonalData(DescriptionModel model){
        Dataset.PersonalData value = null;
        for (FieldModel field : this.templateFieldSearcherService.searchFieldsBySemantics(model.getDescriptionTemplate(), SEMANTIC_DATASET_PERSONAL_DATA)) {
            List<org.opencdmp.commonmodels.models.description.FieldModel> fieldValues = this.findValueField(field, model.getProperties());
            for (org.opencdmp.commonmodels.models.description.FieldModel fieldValue : fieldValues) {
                if (fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
                    try {
                        value = Dataset.PersonalData.fromValue(fieldValue.getTextValue());
                        return value;
                    } catch (IllegalArgumentException | MyApplicationException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
                if (fieldValue.getTextListValue() != null && !fieldValue.getTextListValue().isEmpty()) {
                    for (String val : fieldValue.getTextListValue()) {
                        try {
                            value = Dataset.PersonalData.fromValue(val);
                            return value;
                        } catch (IllegalArgumentException | MyApplicationException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }
        }
        return Dataset.PersonalData.UNKNOWN;
    }

    private List<String> buildRdaDatasetKeywords(DescriptionModel model){
        List<String> values = new ArrayList<>();
        for (FieldModel field : this.templateFieldSearcherService.searchFieldsBySemantics(model.getDescriptionTemplate(), SEMANTIC_DATASET_KEYWORD)) {
            List<org.opencdmp.commonmodels.models.description.FieldModel> fieldValues = this.findValueField(field, model.getProperties());
            for (org.opencdmp.commonmodels.models.description.FieldModel fieldValue : fieldValues) {
                if (fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
                    values.add(fieldValue.getTextValue());
                }
                if (fieldValue.getTextListValue() != null && !fieldValue.getTextListValue().isEmpty()) {
                    values.addAll(fieldValue.getTextListValue());
                }
            }
        }
        return values;
    }

    private String buildRdaDatasetIssued(DescriptionModel model){
        for (FieldModel field : this.templateFieldSearcherService.searchFieldsBySemantics(model.getDescriptionTemplate(), SEMANTIC_DATASET_ISSUED)) {
            List<org.opencdmp.commonmodels.models.description.FieldModel> fieldValues = this.findValueField(field, model.getProperties());
            for (org.opencdmp.commonmodels.models.description.FieldModel fieldValue : fieldValues) {
                if (fieldValue.getDateValue() != null){
                    LocalDateTime ldt = LocalDateTime.ofInstant(fieldValue.getDateValue(), ZoneOffset.UTC);
                    return ldt.format(DateTimeFormatter.ISO_LOCAL_DATE);
                } else if (fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
                    return fieldValue.getTextValue();
                }
            }
        }
        return null;
    }
    
    private Language buildRdaDatasetLanguage(DescriptionModel model){
        Language value = null;
        for (FieldModel field : this.templateFieldSearcherService.searchFieldsBySemantics(model.getDescriptionTemplate(), SEMANTIC_DATASET_LANGUAGE)) {
            List<org.opencdmp.commonmodels.models.description.FieldModel> fieldValues = this.findValueField(field, model.getProperties());
            for (org.opencdmp.commonmodels.models.description.FieldModel fieldValue : fieldValues) {
                if (fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
                    try {
                        value = Language.fromValue(fieldValue.getTextValue());
                        return value;
                    } catch (IllegalArgumentException | MyApplicationException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
                if (fieldValue.getTextListValue() != null && !fieldValue.getTextListValue().isEmpty()) {
                    for (String val : fieldValue.getTextListValue()) {
                        try {
                            value = Language.fromValue(val);
                            return value;
                        } catch (IllegalArgumentException | MyApplicationException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }
        }
        return buildRdaLanguage(model.getDescriptionTemplate().getLanguage());
    }

    private String buildRdaDatasetType(DescriptionModel model){
        for (FieldModel field : this.templateFieldSearcherService.searchFieldsBySemantics(model.getDescriptionTemplate(), SEMANTIC_DATASET_TYPE)) {
            List<org.opencdmp.commonmodels.models.description.FieldModel> fieldValues = this.findValueField(field, model.getProperties());
            for (org.opencdmp.commonmodels.models.description.FieldModel fieldValue : fieldValues) {
                if (fieldValue.getTextValue() != null && !fieldValue.getTextValue().isBlank()) {
                    return fieldValue.getTextValue();
                }

                if (fieldValue.getTextListValue() != null && !fieldValue.getTextListValue().isEmpty()) {
                    return fieldValue.getTextListValue().getFirst();
                }
            }
        }
        return "DMP Dataset";
    }

    private List<org.opencdmp.commonmodels.models.description.FieldModel> findValueField(FieldModel fieldModel, PropertyDefinitionModel descriptionTemplateModel){
        List<org.opencdmp.commonmodels.models.description.FieldModel> items = new ArrayList<>();
        if (fieldModel == null || descriptionTemplateModel == null || descriptionTemplateModel.getFieldSets() == null) return items;
        for (PropertyDefinitionFieldSetModel propertyDefinitionFieldSetModel : descriptionTemplateModel.getFieldSets().values()){
            if (propertyDefinitionFieldSetModel.getItems() == null) continue;
            for (PropertyDefinitionFieldSetItemModel propertyDefinitionFieldSetItemModel : propertyDefinitionFieldSetModel.getItems()){
                if (propertyDefinitionFieldSetItemModel.getFields() == null) continue;
                org.opencdmp.commonmodels.models.description.FieldModel valueField = propertyDefinitionFieldSetItemModel.getFields().getOrDefault(fieldModel.getId(), null);
                if (valueField != null) items.add(valueField);
            }
        }
        return items;
    }

    private List<org.opencdmp.commonmodels.models.description.PropertyDefinitionFieldSetItemModel> findFieldSetValue(FieldSetModel fieldSetModel, PropertyDefinitionModel descriptionTemplateModel){
        List<org.opencdmp.commonmodels.models.description.PropertyDefinitionFieldSetItemModel> items = new ArrayList<>();
        if (fieldSetModel == null || descriptionTemplateModel == null || descriptionTemplateModel.getFieldSets() == null) return items;
        PropertyDefinitionFieldSetModel propertyDefinitionFieldSetModel =  descriptionTemplateModel.getFieldSets().getOrDefault(fieldSetModel.getId(), null);
        if (propertyDefinitionFieldSetModel != null && propertyDefinitionFieldSetModel.getItems() != null) return propertyDefinitionFieldSetModel.getItems();
        return items;
    }
    
    private org.opencdmp.commonmodels.models.description.FieldModel findValueFieldBySemantic(FieldSetModel fieldSet, org.opencdmp.commonmodels.models.description.PropertyDefinitionFieldSetItemModel propertyDefinitionFieldSetItemModel, String semantic){
        FieldModel field = this.templateFieldSearcherService.findFieldBySemantic(fieldSet, semantic);
        return field != null ? propertyDefinitionFieldSetItemModel.getFields().getOrDefault(field.getId(), null) : null;
    }
    //endregion


    //region import dmp
    
    public PlanModel importPlan(PlanImportModel planImportModel) {
        if (planImportModel.getFile() == null) throw new MyApplicationException("Not file defined");

        byte[] bytes = null;
        if (this.getConfiguration().isUseSharedStorage()) {
            bytes = this.storageService.readFile(planImportModel.getFile().getFileRef());
        } else {
            bytes = planImportModel.getFile().getFile();
        }
        PlanModel model = new PlanModel();
        try {
            RDAModel rdaModel = this.jsonHandlingService.fromJson(RDAModel.class, new String(bytes, StandardCharsets.UTF_8));
            if (rdaModel == null || rdaModel.getDmp() == null) throw new MyApplicationException("DMP is missing");

            model.setLabel(rdaModel.getDmp().getTitle());
            model.setDescription(rdaModel.getDmp().getDescription());
            model.setAccessType(PlanAccessType.Restricted);
            if (rdaModel.getDmp().getLanguage() != null && rdaModel.getDmp().getLanguage().value() != null) model.setLanguage(this.configuration.getRdaFileTransformerServiceProperties().getLanguageMap().getOrDefault(rdaModel.getDmp().getLanguage().value(), "en"));
            model.setCreatedAt(rdaModel.getDmp().getCreated());
            model.setUpdatedAt(rdaModel.getDmp().getModified());
            model.setStatus(PlanStatus.Draft);
//            model.setUsers(buildDmpUsersByRdaContributors(rdaModel.getDmp().getContributor()));
            model.setProperties(buildPlanPropertiesModel(planImportModel.getBlueprintModel(), rdaModel.getDmp()));
            model.setReferences(buildPlanReferences(planImportModel.getBlueprintModel(), rdaModel.getDmp().getProject(), rdaModel.getDmp().getContributor()));

            if (planImportModel.getBlueprintModel() != null) model.setPlanBlueprint(planImportModel.getBlueprintModel());

            if (planImportModel.getDescriptions() != null && !planImportModel.getDescriptions().isEmpty()){
                List<PlanDescriptionTemplateModel> planDescriptionTemplateModels = new ArrayList<>();
                for (DescriptionImportModel descriptionImportModel: planImportModel.getDescriptions()) {
                    planDescriptionTemplateModels.add(this.buildPlanDescriptionTemplateModel(descriptionImportModel));
                }
                model.setDescriptionTemplates(planDescriptionTemplateModels);
            }

            if (rdaModel.getDmp().getDataset() != null && !rdaModel.getDmp().getDataset().isEmpty() && planImportModel.getDescriptions() != null){
                model.setDescriptions(new ArrayList<>());
                for (Dataset dataset: rdaModel.getDmp().getDataset()) {
                    if (dataset.getDatasetId().getIdentifier() != null) {
                        DescriptionImportModel descriptionImportModel = planImportModel.getDescriptions().stream().filter(x -> x.getId().equals(dataset.getDatasetId().getIdentifier())).findFirst().orElse(null);
                        if (descriptionImportModel != null) model.getDescriptions().add(this.importDescriptionWithModel(descriptionImportModel, dataset, rdaModel.getDmp()));
                    }
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return model;
    }

    private List<PlanUserModel> buildPlanUsersByRdaContributors(List<Contributor> contributors) {
        List<PlanUserModel> models = new ArrayList<>();
        if (contributors == null) return models;
        for (Contributor contributor : contributors){
            models.add(this.buildPlanUserByRdaContributor(contributor));
        }
        return models;
    }

    private PlanUserModel buildPlanUserByRdaContributor(Contributor contributor) {
        if (contributor == null) throw new MyApplicationException("Contributor is missing");

        PlanUserModel planUserModel = new PlanUserModel();
        if (contributor.getContributorId() != null && contributor.getContributorId().getIdentifier() != null) {
            try {
                UserModel userModel = new UserModel();
                userModel.setId(UUID.fromString(contributor.getContributorId().getIdentifier()));
                planUserModel.setUser(userModel);
            }
            catch (IllegalArgumentException e) {}
        }

        if (contributor.getRole() != null){
            if (contributor.getRole().equals(PlanUserRole.Owner.name())) planUserModel.setRole(PlanUserRole.Owner);
            else if (contributor.getRole().equals(PlanUserRole.DescriptionContributor.name())) planUserModel.setRole(PlanUserRole.DescriptionContributor);
            else if (contributor.getRole().equals(PlanUserRole.Reviewer.name())) planUserModel.setRole(PlanUserRole.Reviewer);
            else if (contributor.getRole().equals(PlanUserRole.Viewer.name())) planUserModel.setRole(PlanUserRole.Viewer);
        }

        return planUserModel;
    }

    private PlanPropertiesModel buildPlanPropertiesModel(PlanBlueprintModel planBlueprintModel, Dmp dmp) {
        PlanPropertiesModel planPropertiesModel = new PlanPropertiesModel();
        if (dmp == null) return planPropertiesModel;

        if (dmp.getContact() != null) {
            planPropertiesModel.setContacts(new ArrayList<>());
            planPropertiesModel.getContacts().add(this.buildPlanContactModelByRdaContact(dmp.getContact()));
        }

        planPropertiesModel.setPlanBlueprintValues(new ArrayList<>());

        if (dmp.getCost() != null && !dmp.getCost().isEmpty()){
            for (Cost cost: dmp.getCost()) {
                planPropertiesModel.getPlanBlueprintValues().addAll(buildPlanBlueprintValuesByRdaCost(planBlueprintModel, cost));
            }
        }

        if (dmp.getEthicalIssuesExist() != null) planPropertiesModel.getPlanBlueprintValues().addAll(buildPlanBlueprintValuesByRdaEthicalIssuesExist(planBlueprintModel, dmp.getEthicalIssuesExist()));
        if (dmp.getEthicalIssuesDescription() != null && !dmp.getEthicalIssuesDescription().isEmpty()) planPropertiesModel.getPlanBlueprintValues().addAll(buildPlanBlueprintValuesByRdaEthicalIssuesDescription(planBlueprintModel, dmp.getEthicalIssuesDescription()));
        if (dmp.getEthicalIssuesReport() != null) planPropertiesModel.getPlanBlueprintValues().addAll(buildPlanBlueprintValuesByRdaEthicalIssueReport(planBlueprintModel, dmp.getEthicalIssuesReport().toString()));

        return planPropertiesModel;
    }


    public List<PlanBlueprintValueModel> buildPlanBlueprintValuesByRdaCost(PlanBlueprintModel planBlueprintModel, Cost cost) {
        List<PlanBlueprintValueModel> planBlueprintValueModels = new ArrayList<>();

        if (planBlueprintModel == null || cost == null) return planBlueprintValueModels;

        if (cost.getTitle() != null) {
            List<org.opencdmp.commonmodels.models.planblueprint.FieldModel> titleFields = this.getFieldsOfSemantic(planBlueprintModel, SEMANTIC_COST_TITLE);
            for(org.opencdmp.commonmodels.models.planblueprint.FieldModel field : titleFields) {
                if (field != null){
                    PlanBlueprintValueModel valueModel = new PlanBlueprintValueModel();
                    valueModel.setFieldId(field.getId());
                    valueModel.setValue(cost.getTitle());
                    planBlueprintValueModels.add(valueModel);
                }
            }
        }

        if (cost.getDescription() != null) {
            List<org.opencdmp.commonmodels.models.planblueprint.FieldModel> descriptionFields = this.getFieldsOfSemantic(planBlueprintModel, SEMANTIC_COST_DESCRIPTION);
            for(org.opencdmp.commonmodels.models.planblueprint.FieldModel field : descriptionFields) {
                if (field != null){
                    PlanBlueprintValueModel valueModel = new PlanBlueprintValueModel();
                    valueModel.setFieldId(field.getId());
                    valueModel.setValue(cost.getDescription());
                    planBlueprintValueModels.add(valueModel);
                }
            }
        }

        if (cost.getValue() != null) {
            List<org.opencdmp.commonmodels.models.planblueprint.FieldModel> valueFields = this.getFieldsOfSemantic(planBlueprintModel, SEMANTIC_COST_VALUE);
            for(org.opencdmp.commonmodels.models.planblueprint.FieldModel field : valueFields) {
                if (field != null){
                    PlanBlueprintValueModel valueModel = new PlanBlueprintValueModel();
                    valueModel.setFieldId(field.getId());
                    valueModel.setNumberValue(cost.getValue());
                    planBlueprintValueModels.add(valueModel);
                }
            }
        }

        if (cost.getCurrencyCode() != null) {
            List<org.opencdmp.commonmodels.models.planblueprint.FieldModel> codeFields = this.getFieldsOfSemantic(planBlueprintModel, SEMANTIC_COST_CURRENCY_CODE);
            for(org.opencdmp.commonmodels.models.planblueprint.FieldModel field : codeFields) {
                if (field != null){
                    PlanBlueprintValueModel valueModel = new PlanBlueprintValueModel();
                    valueModel.setFieldId(field.getId());
                    valueModel.setValue(cost.getCurrencyCode().value());
                    planBlueprintValueModels.add(valueModel);
                }
            }
        }

        return planBlueprintValueModels;
    }

    public List<PlanBlueprintValueModel> buildPlanBlueprintValuesByRdaEthicalIssuesExist(PlanBlueprintModel planBlueprintModel, Dmp.EthicalIssuesExist ethicalIssuesExist) {
        List<PlanBlueprintValueModel> planBlueprintValueModels = new ArrayList<>();

        if (planBlueprintModel == null || ethicalIssuesExist == null) return planBlueprintValueModels;

        List<org.opencdmp.commonmodels.models.planblueprint.FieldModel> fields = this.getFieldsOfSemantic(planBlueprintModel, SEMANTIC_ETHICAL_ISSUES_EXISTS);
        for(org.opencdmp.commonmodels.models.planblueprint.FieldModel field : fields) {
            if (field != null){
                PlanBlueprintValueModel valueModel = new PlanBlueprintValueModel();
                valueModel.setFieldId(field.getId());
                valueModel.setValue(ethicalIssuesExist.value());
                planBlueprintValueModels.add(valueModel);
            }
        }

        return planBlueprintValueModels;
    }

    public List<PlanBlueprintValueModel> buildPlanBlueprintValuesByRdaEthicalIssuesDescription(PlanBlueprintModel planBlueprintModel, String ethicalIssuesDescription) {
        List<PlanBlueprintValueModel> planBlueprintValueModels = new ArrayList<>();

        if (planBlueprintModel == null || ethicalIssuesDescription == null) return planBlueprintValueModels;

        List<org.opencdmp.commonmodels.models.planblueprint.FieldModel> fields = this.getFieldsOfSemantic(planBlueprintModel, SEMANTIC_ETHICAL_ISSUES_DESCRIPTION);
        for(org.opencdmp.commonmodels.models.planblueprint.FieldModel field : fields) {
            if (field != null){
                PlanBlueprintValueModel valueModel = new PlanBlueprintValueModel();
                valueModel.setFieldId(field.getId());
                valueModel.setValue(ethicalIssuesDescription);
                planBlueprintValueModels.add(valueModel);
            }
        }

        return planBlueprintValueModels;
    }

    public List<PlanBlueprintValueModel> buildPlanBlueprintValuesByRdaEthicalIssueReport(PlanBlueprintModel planBlueprintModel, String ethicalIssuesReport) {
        List<PlanBlueprintValueModel> dmpBlueprintValueModels = new ArrayList<>();

        if (planBlueprintModel == null || ethicalIssuesReport == null) return dmpBlueprintValueModels;

        List<org.opencdmp.commonmodels.models.planblueprint.FieldModel> fields = this.getFieldsOfSemantic(planBlueprintModel, SEMANTIC_ETHICAL_ISSUES_REPORT);
        for(org.opencdmp.commonmodels.models.planblueprint.FieldModel field : fields) {
            if (field != null){
                PlanBlueprintValueModel valueModel = new PlanBlueprintValueModel();
                valueModel.setFieldId(field.getId());
                valueModel.setValue(ethicalIssuesReport);
                dmpBlueprintValueModels.add(valueModel);
            }
        }

        return dmpBlueprintValueModels;
    }

    private PlanContactModel buildPlanContactModelByRdaContact(Contact contact) {
        PlanContactModel planContactModel = new PlanContactModel();

        if (contact == null) return planContactModel;

        planContactModel.setFirstName(contact.getName());
        planContactModel.setEmail(contact.getMbox());

        return planContactModel;
    }

    private PlanDescriptionTemplateModel buildPlanDescriptionTemplateModel(DescriptionImportModel model) {
        PlanDescriptionTemplateModel persist = new PlanDescriptionTemplateModel();

        if (model == null) return persist;

        if (model.getDescriptionTemplate() != null) persist.setDescriptionTemplateGroupId(model.getDescriptionTemplate().getGroupId());
        persist.setSectionId(model.getSectionId());

        return persist;
    }

    private List<PlanReferenceModel> buildPlanReferences(PlanBlueprintModel planBlueprintModel, List<Project> projects, List<Contributor> contributors) {
        List<PlanReferenceModel> models = new ArrayList<>();
        if (planBlueprintModel == null) return models;

        if (projects != null && !projects.isEmpty()){
            for (Project project: projects) {
                models.add(buildPlanReferenceModel(project.getTitle(), null, planBlueprintModel, this.configuration.getRdaFileTransformerServiceProperties().getProjectReferenceCode()));

                if (project.getFunding() != null && !projects.isEmpty()) {
                    for (Funding funding : project.getFunding()) {
                        if (funding.getFunderId() != null && funding.getFunderId().getType()!= null && funding.getFunderId().getType().equals(FunderId.Type.FUNDREF)) {
                            models.add(buildPlanReferenceModel(null, funding.getFunderId().getIdentifier(), planBlueprintModel, this.configuration.getRdaFileTransformerServiceProperties().getFunderReferenceCode()));
                        }
                        if (funding.getGrantId() != null) {
                            models.add(buildPlanReferenceModel(null, funding.getGrantId().getIdentifier(), planBlueprintModel, this.configuration.getRdaFileTransformerServiceProperties().getGrantReferenceCode()));
                        }
                    }
                }
            }
        }

        if (contributors != null && !contributors.isEmpty()){
            for (Contributor contributor: contributors){
                if (contributor.getContributorId() != null && contributor.getName() != null){
                    models.add(buildPlanReferenceModel(contributor.getName(), contributor.getContributorId().toString(), planBlueprintModel, this.configuration.getRdaFileTransformerServiceProperties().getResearcherReferenceCode()));
                }
            }
        }

        return models;
    }

    private PlanReferenceModel buildPlanReferenceModel(String label, String reference, PlanBlueprintModel planBlueprintModel, String code){
        PlanReferenceModel dmpReferenceModel = new PlanReferenceModel();
        ReferenceTypeFieldModel referenceTypeFieldModel = this.findBlueprintReferenceFieldIdByCode(code, planBlueprintModel);
        if (referenceTypeFieldModel != null){
            dmpReferenceModel.setData(this.buildPlanReferenceDataModel(referenceTypeFieldModel.getId()));
            dmpReferenceModel.setReference(this.buildReference(label, reference, referenceTypeFieldModel.getReferenceType()));
        }

        return dmpReferenceModel;
    }

    private PlanReferenceDataModel buildPlanReferenceDataModel(UUID blueprintFieldId) {
        PlanReferenceDataModel dmpReferenceDataModel = new PlanReferenceDataModel();

        if (blueprintFieldId == null) return dmpReferenceDataModel;
        dmpReferenceDataModel.setBlueprintFieldId(blueprintFieldId);

        return dmpReferenceDataModel;
    }

    private ReferenceModel buildReference(String label, String reference, ReferenceTypeModel referenceTypeModel) {
        ReferenceModel referenceModel = new ReferenceModel();

        referenceModel.setLabel(label);
        referenceModel.setReference(reference);
        referenceModel.setType(referenceTypeModel);

        return referenceModel;
    }

    private ReferenceTypeFieldModel findBlueprintReferenceFieldIdByCode(String code, PlanBlueprintModel planBlueprintModel){
        if (planBlueprintModel != null && planBlueprintModel.getDefinition() != null && planBlueprintModel.getDefinition().getSections() != null) {
            List<SectionModel> sections = planBlueprintModel.getDefinition().getSections();
            if (sections != null){
                for (SectionModel section : planBlueprintModel.getDefinition().getSections()) {
                    if (section.getFields() != null){
                        for (org.opencdmp.commonmodels.models.planblueprint.FieldModel field : section.getFields()) {
                            if (field.getCategory().equals(PlanBlueprintFieldCategory.ReferenceType)){
                                ReferenceTypeFieldModel referenceField = (ReferenceTypeFieldModel) field;
                                if (referenceField != null && referenceField.getReferenceType() != null && referenceField.getReferenceType().getCode().equals(code)){
                                    return referenceField;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    //endregion

    //region import description

    @Override
    public DescriptionModel importDescription(DescriptionImportModel descriptionImportModel) {
        throw new UnsupportedOperationException("import not supported");
    }

    private DescriptionModel importDescriptionWithModel(DescriptionImportModel descriptionImportModel, Dataset dataset, Dmp dmp) {
        if (descriptionImportModel == null) throw new MyApplicationException("description import is missing");
        if (dataset == null) throw new MyApplicationException("dataset is missing");

        DescriptionModel model = new DescriptionModel();

        model.setLabel(dataset.getTitle());
        model.setDescription(dataset.getDescription());
        model.setDescriptionTemplate(descriptionImportModel.getDescriptionTemplate());
        model.setSectionId(descriptionImportModel.getSectionId());
        model.setStatus(DescriptionStatus.Draft);
        model.setTags(dataset.getKeyword());
        model.setProperties(this.buildPropertyDefinitionModel(descriptionImportModel.getDescriptionTemplate(), dataset, dmp));

        return model;
    }

    private PropertyDefinitionModel buildPropertyDefinitionModel(DescriptionTemplateModel descriptionTemplateModel, Dataset dataset, Dmp dmp) {
        PropertyDefinitionModel model = new PropertyDefinitionModel();
        if (descriptionTemplateModel == null) throw new MyApplicationException("description template is missing");

        Map<String, PropertyDefinitionFieldSetModel> fieldSets = this.buildFieldSetMap(descriptionTemplateModel, dataset, dmp);

        if (dataset.getMetadata() != null) fieldSets.putAll(this.buildFieldByRdaMetadata(descriptionTemplateModel, dataset.getMetadata()));
        if (dataset.getSecurityAndPrivacy() != null) fieldSets.putAll(this.buildFieldByRdaSecurityAndPrivacy(descriptionTemplateModel, dataset.getSecurityAndPrivacy()));
        if (dataset.getTechnicalResource() != null) fieldSets.putAll(this.buildFieldByRdaTechnicalResource(descriptionTemplateModel, dataset.getTechnicalResource()));
        if (dmp.getCost() != null) fieldSets.putAll(this.buildFieldByRdaCost(descriptionTemplateModel, dmp.getCost()));
        if (dmp.getContributor() != null) fieldSets.putAll(this.buildFieldByRdaContributor(descriptionTemplateModel, dmp.getContributor()));
        if (dataset.getDistribution() != null) fieldSets.putAll(this.buildFieldByRdaDistribution(descriptionTemplateModel, dataset.getDistribution()));

        for (FieldSetModel fieldSetModel: descriptionTemplateModel.getDefinition().getAllFieldSets()){
            if (!fieldSets.containsKey(fieldSetModel.getId())){
                Map<String, org.opencdmp.commonmodels.models.description.FieldModel> fieldsMap = new HashMap<>();
                for (org.opencdmp.commonmodels.models.descriptiotemplate.FieldModel templateFieldModel: fieldSetModel.getFields()){
                    org.opencdmp.commonmodels.models.description.FieldModel fieldModel = new org.opencdmp.commonmodels.models.description.FieldModel();
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

    private Map<String, PropertyDefinitionFieldSetModel> buildFieldSetMap(DescriptionTemplateModel descriptionTemplateModel, Dataset dataset, Dmp dmp){
        Map<String, PropertyDefinitionFieldSetModel> fieldSetModelMap = new HashMap<>();
        Map<String, PropertyDefinitionFieldSetModel> extrafieldSetModelMap = new HashMap<>();

        if (descriptionTemplateModel == null) throw new MyApplicationException("description template is missing");

        List<FieldSetModel> fieldSetsWithSemantics = this.templateFieldSearcherService.searchFieldSetsBySemantics(descriptionTemplateModel, List.of(SEMANTIC_DATASET_DATASET_ID, SEMANTIC_DATASET_PERSONAL_DATA, SEMANTIC_DATASET_SENSITIVE_DATA,
                SEMANTIC_DATASET_LANGUAGE, SEMANTIC_DATASET_TYPE, SEMANTIC_DATASET_ISSUED, SEMANTIC_DATASET_KEYWORD, SEMANTIC_DATASET_PRESERVATION_STATEMENT, SEMANTIC_DATASET_DATA_QUALITY_ASSURANCE,
                SEMANTIC_ETHICAL_ISSUES_EXISTS, SEMANTIC_ETHICAL_ISSUES_DESCRIPTION, SEMANTIC_ETHICAL_ISSUES_REPORT, SEMANTIC_DATASET_TECHNICAL_RESOURCE_NAME, SEMANTIC_DATASET_TECHNICAL_RESOURCE_DESCRIPTION,
                SEMANTIC_DATASET_DISTRIBUTION_DESCRIPTION, SEMANTIC_DATASET_DISTRIBUTION_TITLE,
                SEMANTIC_DATASET_DISTRIBUTION_DATA_ACCESS, SEMANTIC_DATASET_DISTRIBUTION_BYTE_SIZE, SEMANTIC_DATASET_DISTRIBUTION_ACCESS_URL, SEMANTIC_DATASET_DISTRIBUTION_AVAILABLE_UTIL, SEMANTIC_DATASET_DISTRIBUTION_DOWNLOAD_URL,
                SEMANTIC_DATASET_DISTRIBUTION_LICENCE, SEMANTIC_DATASET_DISTRIBUTION_LICENCE_LICENSE_REF, SEMANTIC_DATASET_DISTRIBUTION_LICENCE_START_DATE,
                SEMANTIC_DATASET_DISTRIBUTION_HOST_AVAILABILITY, SEMANTIC_DATASET_DISTRIBUTION_HOST_BACKUP_FREQUENCY, SEMANTIC_DATASET_DISTRIBUTION_HOST_BACKUP_TYPE,
                SEMANTIC_DATASET_DISTRIBUTION_HOST_CERTIFIED_WITH, SEMANTIC_DATASET_DISTRIBUTION_HOST_DESCRIPTION, SEMANTIC_DATASET_DISTRIBUTION_HOST_PID_SYSTEM, SEMANTIC_DATASET_DISTRIBUTION_HOST_STORAGE_TYPE,
                SEMANTIC_DATASET_DISTRIBUTION_HOST_TITLE, SEMANTIC_DATASET_DISTRIBUTION_HOST_URL, SEMANTIC_DATASET_DISTRIBUTION_HOST_GEO_LOCATION, SEMANTIC_DATASET_DISTRIBUTION_HOST_SUPPORT_VERSIONING,
                SEMANTIC_DATASET_DISTRIBUTION_FORMAT, SEMANTIC_DATASET_METADATA_DESCRIPTION, SEMANTIC_DATASET_METADATA_LANGUAGE,
                SEMANTIC_DATASET_METADATA_STANDARD_ID_IDENTIFIER, SEMANTIC_DATASET_METADATA_STANDARD_ID_TYPE, SEMANTIC_DATASET_SECURITY_AND_PRIVACY_TITLE, SEMANTIC_DATASET_SECURITY_AND_PRIVACY_DESCRIPTION, SEMANTIC_COST_CURRENCY_CODE, SEMANTIC_COST_TITLE,
                SEMANTIC_COST_VALUE, SEMANTIC_COST_DESCRIPTION, SEMANTIC_DATASET_CONTRIBUTOR));
        if (!fieldSetsWithSemantics.isEmpty()){
            for (FieldSetModel templateFieldSetModel: fieldSetsWithSemantics) {
                PropertyDefinitionFieldSetModel propertyDefinitionFieldSetModel = fieldSetModelMap.getOrDefault(templateFieldSetModel.getId(), null);
                if(propertyDefinitionFieldSetModel == null) {
                    propertyDefinitionFieldSetModel = new PropertyDefinitionFieldSetModel();
                    propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                    fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
                }

                PropertyDefinitionFieldSetItemModel propertyDefinitionFieldSetItemModel = new PropertyDefinitionFieldSetItemModel();
                Map<String, org.opencdmp.commonmodels.models.description.FieldModel> fieldsMap = new HashMap<>();
                for (FieldModel templateFieldModel: templateFieldSetModel.getFields()) {
                    if (templateFieldModel.getSemantics() != null && !templateFieldModel.getSemantics().isEmpty()) {
                        boolean valueFound = false;
                        org.opencdmp.commonmodels.models.description.FieldModel fieldModel = new org.opencdmp.commonmodels.models.description.FieldModel();
                        fieldModel.setId(templateFieldModel.getId());
                        if (dataset != null) {
                            if (dataset.getDatasetId() != null && (templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DATASET_ID))){
                                fieldModel.setTextValue(dataset.getDatasetId().getIdentifier());
                                ExternalIdentifierModel externalIdentifierModel = new ExternalIdentifierModel();
                                externalIdentifierModel.setIdentifier(dataset.getDatasetId().getIdentifier());
                                externalIdentifierModel.setType(dataset.getDatasetId().getType().toString());
                                fieldModel.setExternalIdentifier(externalIdentifierModel);
                                valueFound = true;
                            }
                            if (dataset.getDatasetId() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DATASET_ID_ID)){
                                fieldModel.setTextValue(dataset.getDatasetId().getIdentifier());
                                valueFound = true;
                            }
                            if (dataset.getDatasetId() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DATASET_ID_TYPE)){
                                fieldModel.setTextValue(dataset.getDatasetId().getIdentifier());
                                valueFound = true;
                            }
                            if (dataset.getPersonalData() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_PERSONAL_DATA)){
                                fieldModel.setTextValue(dataset.getPersonalData().value());
                                valueFound = true;
                            }
                            if (dataset.getSensitiveData() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_SENSITIVE_DATA)){
                                fieldModel.setTextValue(dataset.getSensitiveData().value());
                                valueFound = true;
                            }
                            if (dataset.getLanguage() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_LANGUAGE)){
                                fieldModel.setTextValue(dataset.getLanguage().value());
                                valueFound = true;
                            }
                            if (dataset.getType() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_TYPE)){
                                fieldModel.setTextValue(dataset.getType());
                                valueFound = true;
                            }
                            if (dataset.getIssued() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_ISSUED)){
                                fieldModel.setTextValue(dataset.getIssued());
                                try {
                                    fieldModel.setDateValue(LocalDate.parse(dataset.getIssued(), DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
                                } catch (Exception e) {
                                    logger.error(e.getMessage(), e);
                                }
                                valueFound = true;
                            }
                            if (dataset.getKeyword() != null && !dataset.getKeyword().isEmpty() && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_KEYWORD)){
                                fieldModel.setTextValue(dataset.getKeyword().getFirst());
                                fieldModel.setTextListValue(dataset.getKeyword());
                                valueFound = true;
                            }
                            if (dataset.getPreservationStatement() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_PRESERVATION_STATEMENT)){
                                fieldModel.setTextValue(dataset.getPreservationStatement());
                                fieldModel.setTextListValue(List.of(dataset.getPreservationStatement()));
                                valueFound = true;
                            }
                            if (dataset.getDataQualityAssurance() != null && !dataset.getDataQualityAssurance().isEmpty() && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DATA_QUALITY_ASSURANCE)){
                                fieldModel.setTextValue(dataset.getDataQualityAssurance().getFirst());
                                fieldModel.setTextListValue(dataset.getDataQualityAssurance());
                                valueFound = true;
                            }
                        }
                        if (dmp != null) {
                            if (dmp.getEthicalIssuesExist() != null && templateFieldModel.getSemantics().contains(SEMANTIC_ETHICAL_ISSUES_EXISTS)){
                                fieldModel.setTextValue(dmp.getEthicalIssuesExist().value());
                                fieldModel.setTextListValue(List.of(dmp.getEthicalIssuesExist().value()));
                                valueFound = true;
                            }
                            if (dmp.getEthicalIssuesDescription() != null && templateFieldModel.getSemantics().contains(SEMANTIC_ETHICAL_ISSUES_DESCRIPTION)){
                                fieldModel.setTextValue(dmp.getEthicalIssuesDescription());
                                fieldModel.setTextListValue(List.of(dmp.getEthicalIssuesDescription()));
                                valueFound = true;
                            }
                            if (dmp.getEthicalIssuesReport() != null && templateFieldModel.getSemantics().contains(SEMANTIC_ETHICAL_ISSUES_REPORT)){
                                fieldModel.setTextValue(dmp.getEthicalIssuesReport().toString());
                                fieldModel.setTextListValue(List.of(dmp.getEthicalIssuesReport().toString()));
                                valueFound = true;
                            }
                        }

                        if (valueFound) fieldsMap.put(templateFieldModel.getId(), fieldModel);
                    }
                }
                if (propertyDefinitionFieldSetModel.getItems() == null){
                    propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                }
                propertyDefinitionFieldSetItemModel.setFields(fieldsMap);
                propertyDefinitionFieldSetItemModel.setOrdinal(propertyDefinitionFieldSetModel.getItems().size());
                propertyDefinitionFieldSetModel.getItems().add(propertyDefinitionFieldSetItemModel);

                fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
            }
        }

        return fieldSetModelMap;
    }

    private Map<String, PropertyDefinitionFieldSetModel> buildFieldByRdaMetadata(DescriptionTemplateModel descriptionTemplateModel, List<Metadatum> metadatas){
        Map<String, PropertyDefinitionFieldSetModel> fieldSetModelMap = new HashMap<>();

        if (descriptionTemplateModel == null) throw new MyApplicationException("description template is missing");

        if (metadatas == null) return fieldSetModelMap;

        List<FieldSetModel> fieldSetsWithSemantics = this.templateFieldSearcherService.searchFieldSetsBySemantics(descriptionTemplateModel, List.of(SEMANTIC_DATASET_METADATA_DESCRIPTION, SEMANTIC_DATASET_METADATA_LANGUAGE,
                SEMANTIC_DATASET_METADATA_STANDARD_ID_IDENTIFIER, SEMANTIC_DATASET_METADATA_STANDARD_ID_TYPE));
        if (!fieldSetsWithSemantics.isEmpty()){
            for (Metadatum metadata: metadatas) {
                for (FieldSetModel templateFieldSetModel: fieldSetsWithSemantics) {
                    PropertyDefinitionFieldSetModel propertyDefinitionFieldSetModel = fieldSetModelMap.getOrDefault(templateFieldSetModel.getId(), null);
                    if(propertyDefinitionFieldSetModel == null) {
                        propertyDefinitionFieldSetModel = new PropertyDefinitionFieldSetModel();
                        propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                        fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
                    }

                    PropertyDefinitionFieldSetItemModel propertyDefinitionFieldSetItemModel = new PropertyDefinitionFieldSetItemModel();
                    Map<String, org.opencdmp.commonmodels.models.description.FieldModel> fieldsMap = new HashMap<>();
                    for (FieldModel templateFieldModel: templateFieldSetModel.getFields()) {
                        if (templateFieldModel.getSemantics() != null && !templateFieldModel.getSemantics().isEmpty()) {
                            boolean valueFound = false;
                            org.opencdmp.commonmodels.models.description.FieldModel fieldModel = new org.opencdmp.commonmodels.models.description.FieldModel();
                            fieldModel.setId(templateFieldModel.getId());
                            if (metadata.getDescription() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_METADATA_DESCRIPTION)){
                                fieldModel.setTextValue(metadata.getDescription());
                                valueFound = true;
                            }
                            if (metadata.getLanguage() != null && metadata.getLanguage().value() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_METADATA_LANGUAGE)){
                                fieldModel.setTextValue(metadata.getLanguage().value());
                                valueFound = true;
                            }
                            if (metadata.getMetadataStandardId() != null && metadata.getMetadataStandardId().getIdentifier() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_METADATA_STANDARD_ID_IDENTIFIER)){
                                fieldModel.setTextValue(metadata.getMetadataStandardId().getIdentifier());
                                valueFound = true;
                            }
                            if (metadata.getMetadataStandardId() != null && metadata.getMetadataStandardId().getType() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_METADATA_STANDARD_ID_TYPE)){
                                fieldModel.setTextValue(metadata.getMetadataStandardId().getType().value());
                                valueFound = true;
                            }

                            if (valueFound) fieldsMap.put(templateFieldModel.getId(), fieldModel);
                        }
                    }
                    if (propertyDefinitionFieldSetModel.getItems() == null){
                        propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                    }
                    propertyDefinitionFieldSetItemModel.setFields(fieldsMap);
                    propertyDefinitionFieldSetItemModel.setOrdinal(propertyDefinitionFieldSetModel.getItems().size());
                    propertyDefinitionFieldSetModel.getItems().add(propertyDefinitionFieldSetItemModel);

                    fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
                }
            }

        }

        return fieldSetModelMap;
    }

    private Map<String, PropertyDefinitionFieldSetModel> buildFieldByRdaSecurityAndPrivacy(DescriptionTemplateModel descriptionTemplateModel, List<SecurityAndPrivacy> securityAndPrivacies){
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
                    Map<String, org.opencdmp.commonmodels.models.description.FieldModel> fieldsMap = new HashMap<>();
                    for (FieldModel templateFieldModel: templateFieldSetModel.getFields()) {
                        if (templateFieldModel.getSemantics() != null && !templateFieldModel.getSemantics().isEmpty()) {
                            boolean valueFound = false;
                            org.opencdmp.commonmodels.models.description.FieldModel fieldModel = new org.opencdmp.commonmodels.models.description.FieldModel();
                            fieldModel.setId(templateFieldModel.getId());
                            if (securityAndPrivacy.getTitle() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_SECURITY_AND_PRIVACY_TITLE)){
                                fieldModel.setTextValue(securityAndPrivacy.getTitle());
                                valueFound = true;
                            }
                            if (securityAndPrivacy.getDescription() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_SECURITY_AND_PRIVACY_DESCRIPTION)){
                                fieldModel.setTextValue(securityAndPrivacy.getDescription());
                                valueFound = true;
                            }

                            if (valueFound) fieldsMap.put(templateFieldModel.getId(), fieldModel);
                        }
                    }
                    if (propertyDefinitionFieldSetModel.getItems() == null){
                        propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                    }
                    propertyDefinitionFieldSetItemModel.setFields(fieldsMap);
                    propertyDefinitionFieldSetItemModel.setOrdinal(propertyDefinitionFieldSetModel.getItems().size());
                    propertyDefinitionFieldSetModel.getItems().add(propertyDefinitionFieldSetItemModel);

                    fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
                }
            }

        }

        return fieldSetModelMap;
    }

    private Map<String, PropertyDefinitionFieldSetModel> buildFieldByRdaTechnicalResource(DescriptionTemplateModel descriptionTemplateModel, List<TechnicalResource> technicalResources){
        Map<String, PropertyDefinitionFieldSetModel> fieldSetModelMap = new HashMap<>();

        if (descriptionTemplateModel == null) throw new MyApplicationException("description template is missing");

        if (technicalResources == null) return fieldSetModelMap;

        List<FieldSetModel> fieldSetsWithSemantics = this.templateFieldSearcherService.searchFieldSetsBySemantics(descriptionTemplateModel, List.of(SEMANTIC_DATASET_TECHNICAL_RESOURCE_NAME, SEMANTIC_DATASET_TECHNICAL_RESOURCE_DESCRIPTION));
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
                    Map<String, org.opencdmp.commonmodels.models.description.FieldModel> fieldsMap = new HashMap<>();
                    for (FieldModel templateFieldModel: templateFieldSetModel.getFields()) {
                        if (templateFieldModel.getSemantics() != null && !templateFieldModel.getSemantics().isEmpty()) {
                            boolean valueFound = false;
                            org.opencdmp.commonmodels.models.description.FieldModel fieldModel = new org.opencdmp.commonmodels.models.description.FieldModel();
                            fieldModel.setId(templateFieldModel.getId());
                            if (technicalResource.getName() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_TECHNICAL_RESOURCE_NAME)){
                                fieldModel.setTextValue(technicalResource.getName());
                                valueFound = true;
                            }
                            if (technicalResource.getDescription() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_TECHNICAL_RESOURCE_DESCRIPTION)){
                                fieldModel.setTextValue(technicalResource.getDescription());
                                valueFound = true;
                            }

                            if (valueFound) fieldsMap.put(templateFieldModel.getId(), fieldModel);
                        }
                    }
                    if (propertyDefinitionFieldSetModel.getItems() == null){
                        propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                    }
                    propertyDefinitionFieldSetItemModel.setFields(fieldsMap);
                    propertyDefinitionFieldSetItemModel.setOrdinal(propertyDefinitionFieldSetModel.getItems().size());
                    propertyDefinitionFieldSetModel.getItems().add(propertyDefinitionFieldSetItemModel);

                    fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
                }
            }

        }

        return fieldSetModelMap;
    }

    private Map<String, PropertyDefinitionFieldSetModel> buildFieldByRdaCost(DescriptionTemplateModel descriptionTemplateModel, List<Cost> costs){
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
                    Map<String, org.opencdmp.commonmodels.models.description.FieldModel> fieldsMap = new HashMap<>();
                    for (FieldModel templateFieldModel: templateFieldSetModel.getFields()) {
                        if (templateFieldModel.getSemantics() != null && !templateFieldModel.getSemantics().isEmpty()) {
                            boolean valueFound = false;
                            org.opencdmp.commonmodels.models.description.FieldModel fieldModel = new org.opencdmp.commonmodels.models.description.FieldModel();
                            fieldModel.setId(templateFieldModel.getId());
                            if (cost.getCurrencyCode() != null && templateFieldModel.getSemantics().contains(SEMANTIC_COST_CURRENCY_CODE)){
                                fieldModel.setTextValue(cost.getCurrencyCode().value());
                                valueFound = true;
                            }
                            if (cost.getTitle() != null && templateFieldModel.getSemantics().contains(SEMANTIC_COST_TITLE)){
                                fieldModel.setTextValue(cost.getTitle());
                                valueFound = true;
                            }
                            if (cost.getDescription() != null && templateFieldModel.getSemantics().contains(SEMANTIC_COST_DESCRIPTION)){
                                fieldModel.setTextValue(cost.getDescription());
                                valueFound = true;
                            }
                            if (cost.getValue() != null && templateFieldModel.getSemantics().contains(SEMANTIC_COST_VALUE)){
                                fieldModel.setTextValue(cost.getValue().toString());
                                valueFound = true;
                            }

                            if (valueFound) fieldsMap.put(templateFieldModel.getId(), fieldModel);
                        }
                    }
                    if (propertyDefinitionFieldSetModel.getItems() == null){
                        propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                    }
                    propertyDefinitionFieldSetItemModel.setFields(fieldsMap);
                    propertyDefinitionFieldSetItemModel.setOrdinal(propertyDefinitionFieldSetModel.getItems().size());
                    propertyDefinitionFieldSetModel.getItems().add(propertyDefinitionFieldSetItemModel);

                    fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
                }
            }

        }

        return fieldSetModelMap;
    }

    private Map<String, PropertyDefinitionFieldSetModel> buildFieldByRdaContributor(DescriptionTemplateModel descriptionTemplateModel, List<Contributor> contributors){
        Map<String, PropertyDefinitionFieldSetModel> fieldSetModelMap = new HashMap<>();

        if (descriptionTemplateModel == null) throw new MyApplicationException("description template is missing");

        if (contributors == null) return fieldSetModelMap;

        List<FieldSetModel> fieldSetsWithSemantics = this.templateFieldSearcherService.searchFieldSetsBySemantics(descriptionTemplateModel, List.of(SEMANTIC_DATASET_CONTRIBUTOR));
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
                    Map<String, org.opencdmp.commonmodels.models.description.FieldModel> fieldsMap = new HashMap<>();
                    for (FieldModel templateFieldModel: templateFieldSetModel.getFields()) {
                        if (templateFieldModel.getSemantics() != null && !templateFieldModel.getSemantics().isEmpty()){
                            boolean valueFound = false;
                            org.opencdmp.commonmodels.models.description.FieldModel fieldModel = new org.opencdmp.commonmodels.models.description.FieldModel();
                            fieldModel.setId(templateFieldModel.getId());
                            if (contributor.getContributorId() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_CONTRIBUTOR)){
                                ReferenceModel referenceModel = new ReferenceModel();
                                referenceModel.setReference(contributor.getContributorId().toString());
                                referenceModel.setLabel(contributor.getName());
                                ReferenceTypeDataModel referenceTypeDataModel = (ReferenceTypeDataModel) templateFieldModel.getData();
                                if (referenceTypeDataModel != null) referenceModel.setType(referenceTypeDataModel.getReferenceType());
                                fieldModel.setReferences(new ArrayList<>());
                                fieldModel.getReferences().add(referenceModel);
                                valueFound = true;
                            }
                            if (valueFound) fieldsMap.put(templateFieldModel.getId(), fieldModel);
                        }
                    }

                    if (propertyDefinitionFieldSetModel.getItems() == null){
                        propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                    }
                    propertyDefinitionFieldSetItemModel.setFields(fieldsMap);
                    propertyDefinitionFieldSetItemModel.setOrdinal(propertyDefinitionFieldSetModel.getItems().size());
                    propertyDefinitionFieldSetModel.getItems().add(propertyDefinitionFieldSetItemModel);

                    fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
                }
            }

        }

        return fieldSetModelMap;
    }

    private Map<String, PropertyDefinitionFieldSetModel> buildFieldByRdaDistribution(DescriptionTemplateModel descriptionTemplateModel, List<Distribution> distributions){
        Map<String, PropertyDefinitionFieldSetModel> fieldSetModelMap = new HashMap<>();

        if (descriptionTemplateModel == null) throw new MyApplicationException("description template is missing");

        if (distributions == null) return fieldSetModelMap;

        List<FieldSetModel> fieldSetsWithSemantics = this.templateFieldSearcherService.searchFieldSetsBySemantics(descriptionTemplateModel, List.of(SEMANTIC_DATASET_DISTRIBUTION_DESCRIPTION, SEMANTIC_DATASET_DISTRIBUTION_TITLE,
                SEMANTIC_DATASET_DISTRIBUTION_DATA_ACCESS, SEMANTIC_DATASET_DISTRIBUTION_BYTE_SIZE, SEMANTIC_DATASET_DISTRIBUTION_ACCESS_URL, SEMANTIC_DATASET_DISTRIBUTION_AVAILABLE_UTIL, SEMANTIC_DATASET_DISTRIBUTION_DOWNLOAD_URL,
                SEMANTIC_DATASET_DISTRIBUTION_LICENCE, SEMANTIC_DATASET_DISTRIBUTION_LICENCE_LICENSE_REF, SEMANTIC_DATASET_DISTRIBUTION_LICENCE_START_DATE,
                SEMANTIC_DATASET_DISTRIBUTION_HOST_AVAILABILITY, SEMANTIC_DATASET_DISTRIBUTION_HOST_BACKUP_FREQUENCY, SEMANTIC_DATASET_DISTRIBUTION_HOST_BACKUP_TYPE,
                SEMANTIC_DATASET_DISTRIBUTION_HOST_CERTIFIED_WITH, SEMANTIC_DATASET_DISTRIBUTION_HOST_DESCRIPTION, SEMANTIC_DATASET_DISTRIBUTION_HOST_PID_SYSTEM, SEMANTIC_DATASET_DISTRIBUTION_HOST_STORAGE_TYPE,
                SEMANTIC_DATASET_DISTRIBUTION_HOST_TITLE, SEMANTIC_DATASET_DISTRIBUTION_HOST_URL, SEMANTIC_DATASET_DISTRIBUTION_HOST_GEO_LOCATION, SEMANTIC_DATASET_DISTRIBUTION_HOST_SUPPORT_VERSIONING,
                SEMANTIC_DATASET_DISTRIBUTION_FORMAT));
        if (!fieldSetsWithSemantics.isEmpty()){
            for (Distribution distribution: distributions) {
                for (FieldSetModel templateFieldSetModel: fieldSetsWithSemantics) {
                    PropertyDefinitionFieldSetModel propertyDefinitionFieldSetModel = fieldSetModelMap.getOrDefault(templateFieldSetModel.getId(), null);
                    if(propertyDefinitionFieldSetModel == null) {
                        propertyDefinitionFieldSetModel = new PropertyDefinitionFieldSetModel();
                        propertyDefinitionFieldSetModel.setItems(new ArrayList<>());
                        fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
                    }

                    PropertyDefinitionFieldSetItemModel propertyDefinitionFieldSetItemModel = new PropertyDefinitionFieldSetItemModel();
                    Map<String, org.opencdmp.commonmodels.models.description.FieldModel> fieldsMap = new HashMap<>();
                    for (FieldModel templateFieldModel: templateFieldSetModel.getFields()) {
                        if (templateFieldModel.getSemantics() != null && !templateFieldModel.getSemantics().isEmpty()){
                            boolean valueFound = false;
                            org.opencdmp.commonmodels.models.description.FieldModel fieldModel = new org.opencdmp.commonmodels.models.description.FieldModel();
                            fieldModel.setId(templateFieldModel.getId());
                            if (distribution.getDescription() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_DESCRIPTION)){
                                fieldModel.setTextValue(distribution.getDescription());
                                valueFound = true;
                            }
                            if (distribution.getTitle() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_TITLE)){
                                fieldModel.setTextValue(distribution.getTitle());
                                valueFound = true;
                            }
                            if (distribution.getDataAccess() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_DATA_ACCESS)){
                                fieldModel.setTextValue(distribution.getDataAccess().value());
                                valueFound = true;
                            }
                            if (distribution.getByteSize() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_BYTE_SIZE)){
                                fieldModel.setTextValue(distribution.getByteSize().toString());
                                valueFound = true;
                            }
                            if (distribution.getAccessUrl() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_ACCESS_URL)){
                                fieldModel.setTextValue(distribution.getAccessUrl());
                                valueFound = true;
                            }
                            if (distribution.getAvailableUntil() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_AVAILABLE_UTIL)){
                                fieldModel.setTextValue(distribution.getAvailableUntil());
                                try {
                                    fieldModel.setDateValue(LocalDate.parse(distribution.getAvailableUntil(), DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
                                } catch (Exception e) {
                                    logger.error(e.getMessage(), e);
                                }
                                valueFound = true;
                            }
                            if (distribution.getDownloadUrl() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_DOWNLOAD_URL)){
                                fieldModel.setTextValue(distribution.getDownloadUrl().toString());
                                valueFound = true;
                            }
                            if (distribution.getLicense() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_LICENCE)){
                                for (License license: distribution.getLicense()) {
                                    ReferenceModel referenceModel = new ReferenceModel();
                                    referenceModel.setReference(license.getLicenseRef().toString());
                                    ReferenceTypeDataModel referenceTypeDataModel = (ReferenceTypeDataModel) templateFieldModel.getData();
                                    if (referenceTypeDataModel != null) referenceModel.setType(referenceTypeDataModel.getReferenceType());
                                    fieldModel.setReferences(new ArrayList<>());
                                    fieldModel.getReferences().add(referenceModel);
                                }
                                valueFound = true;
                            }
                            if (distribution.getHost() != null && distribution.getHost().getAvailability() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_HOST_AVAILABILITY)){
                                fieldModel.setTextValue(distribution.getHost().getAvailability());
                                valueFound = true;
                            }
                            if (distribution.getHost() != null && distribution.getHost().getBackupFrequency() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_HOST_BACKUP_FREQUENCY)){
                                fieldModel.setTextValue(distribution.getHost().getBackupFrequency());
                                valueFound = true;
                            }
                            if (distribution.getHost() != null && distribution.getHost().getBackupType() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_HOST_BACKUP_TYPE)){
                                fieldModel.setTextValue(distribution.getHost().getBackupType());
                                valueFound = true;
                            }
                            if (distribution.getHost() != null && distribution.getHost().getCertifiedWith() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_HOST_CERTIFIED_WITH)){
                                fieldModel.setTextValue(distribution.getHost().getCertifiedWith().value());
                                valueFound = true;
                            }
                            if (distribution.getHost() != null && distribution.getHost().getDescription() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_HOST_DESCRIPTION)){
                                fieldModel.setTextValue(distribution.getHost().getDescription());
                                valueFound = true;
                            }
                            if (distribution.getHost() != null && distribution.getHost().getPidSystem() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_HOST_PID_SYSTEM)){
                                fieldModel.setTextListValue(distribution.getHost().getPidSystem().stream().map(x -> x.value()).collect(Collectors.toList()));
                                fieldModel.setTextValue(distribution.getHost().getPidSystem().stream().map(x -> x.value()).findFirst().orElse(null));
                                valueFound = true;
                            }
                            if (distribution.getHost() != null && distribution.getHost().getStorageType() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_HOST_STORAGE_TYPE)){
                                fieldModel.setTextValue(distribution.getHost().getStorageType());
                                valueFound = true;
                            }
                            if (distribution.getHost() != null && distribution.getHost().getTitle() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_HOST_TITLE)){
                                fieldModel.setTextValue(distribution.getHost().getTitle());
                                valueFound = true;
                            }
                            if (distribution.getHost() != null && distribution.getHost().getUrl() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_HOST_URL)){
                                fieldModel.setTextValue(distribution.getHost().getUrl().toString());
                                valueFound = true;
                            }
                            if (distribution.getHost() != null && distribution.getHost().getGeoLocation() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_HOST_GEO_LOCATION)){
                                fieldModel.setTextValue(distribution.getHost().getGeoLocation().value());
                                valueFound = true;
                            }
                            if (distribution.getFormat() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_FORMAT)){
                                fieldModel.setTextValue(distribution.getFormat().getFirst());
                                fieldModel.setTextListValue(distribution.getFormat());
                                valueFound = true;
                            }
                            if (distribution.getHost() != null && distribution.getHost().getSupportVersioning() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_HOST_SUPPORT_VERSIONING)){
                                fieldModel.setTextValue(distribution.getHost().getSupportVersioning().value());
                                valueFound = true;
                            }
                            if (distribution.getLicense() != null && !distribution.getLicense().isEmpty()){
                                for (License license: distribution.getLicense()) {
                                    if (license.getLicenseRef() != null  && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_LICENCE_LICENSE_REF)) {
                                        fieldModel.setTextValue(license.getLicenseRef().toString());
                                        valueFound = true;
                                    }
                                    if (license.getStartDate() != null && templateFieldModel.getSemantics().contains(SEMANTIC_DATASET_DISTRIBUTION_LICENCE_START_DATE)) {
                                        fieldModel.setTextValue(license.getStartDate());
                                        try {
                                            fieldModel.setDateValue(LocalDate.parse(license.getStartDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
                                        } catch (Exception e) {
                                            logger.error(e.getMessage(), e);
                                        }
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
                    propertyDefinitionFieldSetItemModel.setFields(fieldsMap);
                    propertyDefinitionFieldSetItemModel.setOrdinal(propertyDefinitionFieldSetModel.getItems().size());
                    propertyDefinitionFieldSetModel.getItems().add(propertyDefinitionFieldSetItemModel);

                    fieldSetModelMap.put(templateFieldSetModel.getId(), propertyDefinitionFieldSetModel);
                }
            }

        }

        return fieldSetModelMap;
    }

    @Override
    public FileTransformerConfiguration getConfiguration() {
        List<FileFormat> supportedFormats = List.of(new FileFormat("json", false, null));
        FileTransformerConfiguration configuration = new FileTransformerConfiguration();
        configuration.setFileTransformerId(this.configuration.getRdaFileTransformerServiceProperties().getTransformerId());
        configuration.setExportVariants(supportedFormats);
        configuration.setExportEntityTypes(FILE_TRANSFORMER_ENTITY_TYPES);
        configuration.setImportVariants(null);
        configuration.setUseSharedStorage(this.configuration.getRdaFileTransformerServiceProperties().isUseSharedStorage());
        return configuration;
    }

    //endregion

    //region preprocessing
    @Override
    public PreprocessingPlanModel preprocessingPlan(FileEnvelopeModel fileEnvelopeModel) {

        PreprocessingPlanModel preprocessingPlanModel = new PreprocessingPlanModel();
        byte[] bytes = null;
        if (this.getConfiguration().isUseSharedStorage()) {
            bytes = this.storageService.readFile(fileEnvelopeModel.getFileRef());
        } else {
            bytes = fileEnvelopeModel.getFile();
        }
        try {
            RDAModel rdaModel = this.jsonHandlingService.fromJson(RDAModel.class, new String(bytes, StandardCharsets.UTF_8));
            if (rdaModel == null || rdaModel.getDmp() == null) throw new MyApplicationException("RDAModel is missing");

            preprocessingPlanModel.setLabel(rdaModel.getDmp().getTitle());

            if (rdaModel.getDmp().getDataset() != null){
                List<PreprocessingDescriptionModel> preprocessingDescriptions = new ArrayList<>();
                for (Dataset dataset: rdaModel.getDmp().getDataset()) {
                    preprocessingDescriptions.add(this.preprocessingDescriptionFromDataset(dataset));
                }
                preprocessingPlanModel.setPreprocessingDescriptionModels(preprocessingDescriptions);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return preprocessingPlanModel;
    }

    @Override
    public PreprocessingDescriptionModel preprocessingDescription(FileEnvelopeModel fileEnvelopeModel) {

        byte[] bytes = null;
        if (this.getConfiguration().isUseSharedStorage()) {
            bytes = this.storageService.readFile(fileEnvelopeModel.getFileRef());
        } else {
            bytes = fileEnvelopeModel.getFile();
        }
        try {
            Dataset dataset = this.jsonHandlingService.fromJson(Dataset.class, new String(bytes, StandardCharsets.UTF_8));
            if (dataset == null) throw new MyApplicationException("description is missing");
            return this.preprocessingDescriptionFromDataset(dataset);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    public PreprocessingDescriptionModel preprocessingDescriptionFromDataset(Dataset dataset) {
        PreprocessingDescriptionModel preprocessingDescriptionModel = new PreprocessingDescriptionModel();

        if (dataset == null) return preprocessingDescriptionModel;

        preprocessingDescriptionModel.setLabel(dataset.getTitle());
        if (dataset.getDatasetId() != null) preprocessingDescriptionModel.setId(dataset.getDatasetId().getIdentifier());

        return preprocessingDescriptionModel;
    }

    //endregion
}
