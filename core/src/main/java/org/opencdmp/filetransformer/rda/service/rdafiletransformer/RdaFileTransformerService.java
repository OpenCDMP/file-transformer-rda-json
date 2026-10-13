package org.opencdmp.filetransformer.rda.service.rdafiletransformer;

import org.opencdmp.filetransformer.rda.convention.ConventionService;
import org.opencdmp.filetransformer.rda.service.planblueprintsearcher.PlanBlueprintFieldSearcherService;
import tools.jackson.core.JacksonException;
import gr.cite.tools.exception.MyApplicationException;
import org.opencdmp.commonmodels.enums.*;
import org.opencdmp.commonmodels.models.*;
import org.opencdmp.commonmodels.models.description.*;
import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.commonmodels.models.plandescriptiontemplate.PlanDescriptionTemplateModel;
import org.opencdmp.filetransformer.rda.model.rda.*;
import org.opencdmp.filetransformer.rda.service.descriptiontemplatesearcher.TemplateFieldSearcherService;
import org.opencdmp.filetransformerbase.interfaces.FileTransformerClient;
import org.opencdmp.filetransformerbase.interfaces.FileTransformerConfiguration;
import org.opencdmp.filetransformer.rda.service.json.JsonHandlingService;
import org.opencdmp.filetransformer.rda.service.storage.FileStorageService;
import org.opencdmp.filetransformerbase.models.misc.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.util.stream.Stream;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.export.ContactExporter;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.export.ContributorExporter;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.export.CostExporter;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.export.CreatorExporter;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.export.DistributionExporter;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.export.EthicsExporter;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.export.IdentifierExporter;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.export.MetadataExporter;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.export.ProjectExporter;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.export.SecurityAndPrivacyExporter;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.export.TechnicalResourceExporter;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.export.DatasetExporter;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.export.DmpExporter;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.importer.DescriptionImporter;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.importer.PlanImporter;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RdaFileTransformerService extends RdaMapperSupport implements FileTransformerClient {

    private final FileStorageService storageService;
    private final DmpExporter dmpExporter;
    private final PlanImporter planImporter;
    private final DescriptionImporter descriptionImporter;

    private final static List<PluginEntityType> FILE_TRANSFORMER_ENTITY_TYPES = List.of(PluginEntityType.Plan);

    @Autowired
    public RdaFileTransformerService(FileStorageService storageService, JsonHandlingService jsonHandlingService, RdaFileTransformerServiceConfiguration configuration, TemplateFieldSearcherService templateFieldSearcherService, PlanBlueprintFieldSearcherService blueprintFieldSearcherService, ConventionService conventionService) {
        this(storageService, new RdaMapperContext(jsonHandlingService, configuration, templateFieldSearcherService, blueprintFieldSearcherService, conventionService));
    }

    private RdaFileTransformerService(FileStorageService storageService, RdaMapperContext context) {
        super(context);
        this.storageService = storageService;
        ContactExporter contactExporter = new ContactExporter(context);
        ContributorExporter contributorExporter = new ContributorExporter(context);
        CostExporter costExporter = new CostExporter(context);
        CreatorExporter creatorExporter = new CreatorExporter(context);
        DistributionExporter distributionExporter = new DistributionExporter(context);
        EthicsExporter ethicsExporter = new EthicsExporter(context);
        IdentifierExporter identifierExporter = new IdentifierExporter(context);
        MetadataExporter metadataExporter = new MetadataExporter(context);
        ProjectExporter projectExporter = new ProjectExporter(context);
        SecurityAndPrivacyExporter securityAndPrivacyExporter = new SecurityAndPrivacyExporter(context);
        TechnicalResourceExporter technicalResourceExporter = new TechnicalResourceExporter(context);
        DatasetExporter datasetExporter = new DatasetExporter(context, contributorExporter, costExporter, creatorExporter, distributionExporter, ethicsExporter, identifierExporter, metadataExporter, projectExporter, securityAndPrivacyExporter, technicalResourceExporter);
        this.dmpExporter = new DmpExporter(context, contactExporter, contributorExporter, costExporter, datasetExporter, ethicsExporter, identifierExporter, projectExporter);
        this.planImporter = new PlanImporter(context);
        this.descriptionImporter = new DescriptionImporter(context);
    }

    @Override
    public FileEnvelopeModel exportPlan(PlanModel planFileTransformerModel, String variant){
        Dmp dmp = this.dmpExporter.buildRdaDmp(planFileTransformerModel);
        RDAModel rdaModel = new RDAModel();
        rdaModel.setDmp(dmp);
        String dmpJson = jsonHandlingService.toJsonSafe(rdaModel);
        
        byte[] bytes = dmpJson.getBytes(StandardCharsets.UTF_8);
        FileEnvelopeModel exportFile = new FileEnvelopeModel();
        if (this.getConfiguration().isUseSharedStorage()) {
            String fileRef = this.storageService.storeFile(bytes);
            exportFile.setFileRef(fileRef);
        } else {
            exportFile.setFile(bytes);
        }
        exportFile.setFilename(fileName(planFileTransformerModel.getLabel()) + ".json");
        return exportFile;
    }

    /** The plan title as a file name, without path separators, characters file systems reject or control characters. */
    private static String fileName(String label) {
        String name = label == null ? "" : label.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_").trim();
        return name.isEmpty() ? "plan" : name;
    }

    @Override
    public FileEnvelopeModel exportDescription(DescriptionModel descriptionFileTransformerModel, String format) {
        throw new MyApplicationException("Not implemented");
    }

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
            if (rdaModel.getDmp().getLanguage() != null && rdaModel.getDmp().getLanguage().value() != null) {
                Stream<String> keys = this.configuration.getRdaFileTransformerServiceProperties().getLanguageMap()
                        .entrySet()
                        .stream()
                        .filter(entry -> rdaModel.getDmp().getLanguage().value().equals(entry.getValue()))
                        .map(Map.Entry::getKey);

                String code = keys.findFirst().orElse(null);
                if (code != null && !code.isBlank()) model.setLanguage(code);
                else model.setLanguage("en");
            }
            model.setCreatedAt(rdaModel.getDmp().getCreated());
            model.setUpdatedAt(rdaModel.getDmp().getModified());
//            model.setUsers(buildDmpUsersByRdaContributors(rdaModel.getDmp().getContributor()));
            Set<String> placedPlanObjects = new HashSet<>();
            model.setProperties(this.planImporter.buildPlanPropertiesModel(planImportModel.getBlueprintModel(), rdaModel.getDmp(), placedPlanObjects));

            if (planImportModel.getBlueprintModel() != null) model.setPlanBlueprint(planImportModel.getBlueprintModel());

            if (planImportModel.getDescriptions() != null && !planImportModel.getDescriptions().isEmpty()){
                List<PlanDescriptionTemplateModel> planDescriptionTemplateModels = new ArrayList<>();
                for (DescriptionImportModel descriptionImportModel: planImportModel.getDescriptions()) {
                    planDescriptionTemplateModels.add(this.planImporter.buildPlanDescriptionTemplateModel(descriptionImportModel));
                }
                model.setDescriptionTemplates(planDescriptionTemplateModels);
            }

            if (rdaModel.getDmp().getDataset() != null && !rdaModel.getDmp().getDataset().isEmpty() && planImportModel.getDescriptions() != null){
                model.setDescriptions(new ArrayList<>());
                for (Dataset dataset: rdaModel.getDmp().getDataset()) {
                    if (dataset.getDatasetId().getIdentifier() != null) {
                        DescriptionImportModel descriptionImportModel = planImportModel.getDescriptions().stream().filter(x -> x.getId().equals(dataset.getDatasetId().getIdentifier())).findFirst().orElse(null);
                        if (descriptionImportModel == null && dataset.getAlternateIdentifier() != null && !dataset.getAlternateIdentifier().isEmpty()) {

                            List<String> alternateIdentifiers = dataset.getAlternateIdentifier().stream().filter(x -> x.getIdentifier() != null && x.getIdentifier().startsWith(DATASET_SYSTEM_ALTERNATE_IDENTIFIER_PREFIX)).map(AlternateIdentifier:: getIdentifier).toList();
                            if (!alternateIdentifiers.isEmpty()) {
                                for (String alternateIdentifier: alternateIdentifiers) {
                                    try {
                                        if (descriptionImportModel != null) break;

                                        String uuidValue = alternateIdentifier.substring(DATASET_SYSTEM_ALTERNATE_IDENTIFIER_PREFIX.length());
                                        if (!uuidValue.isEmpty()) descriptionImportModel = planImportModel.getDescriptions().stream().filter(x -> x.getId().equals(uuidValue)).findFirst().orElse(null);
                                    } catch (IllegalArgumentException ex) {

                                    }
                                }

                            }

                        }
                        if (descriptionImportModel != null) model.getDescriptions().add(this.descriptionImporter.importDescriptionWithModel(descriptionImportModel, dataset, rdaModel.getDmp(), placedPlanObjects));
                    }
                }
            }
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }

        return model;
    }

    @Override
    public DescriptionModel importDescription(DescriptionImportModel descriptionImportModel) {
        throw new UnsupportedOperationException("import not supported");
    }

    @Override
    public FileTransformerConfiguration getConfiguration() {
        List<FileFormat> supportedFormats = List.of(new FileFormat("json", false, null));
        FileTransformerConfiguration configuration = new FileTransformerConfiguration();
        configuration.setFileTransformerId(this.configuration.getRdaFileTransformerServiceProperties().getTransformerId());
        configuration.setExportVariants(supportedFormats);
        configuration.setExportEntityTypes(FILE_TRANSFORMER_ENTITY_TYPES);
        configuration.setImportVariants(supportedFormats);
        configuration.setImportEntityTypes(FILE_TRANSFORMER_ENTITY_TYPES);
        configuration.setUseSharedStorage(this.configuration.getRdaFileTransformerServiceProperties().isUseSharedStorage());
        configuration.setConfigurationFields(this.configuration.getRdaFileTransformerServiceProperties().getConfigurationFields());
        configuration.setUserConfigurationFields(this.configuration.getRdaFileTransformerServiceProperties().getUserConfigurationFields());
        configuration.setSemantics(RdaSemanticsManifest.get());
        return configuration;
    }

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
        } catch (JacksonException e) {
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
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }

    }

    public PreprocessingDescriptionModel preprocessingDescriptionFromDataset(Dataset dataset) {
        PreprocessingDescriptionModel preprocessingDescriptionModel = new PreprocessingDescriptionModel();

        if (dataset == null) return preprocessingDescriptionModel;

        preprocessingDescriptionModel.setLabel(dataset.getTitle());

        if (dataset.getAlternateIdentifier() != null && !dataset.getAlternateIdentifier().isEmpty()) {

            List<String> alternateIdentifiers = dataset.getAlternateIdentifier().stream().filter(x -> x.getIdentifier() != null && x.getIdentifier().startsWith(DATASET_SYSTEM_ALTERNATE_IDENTIFIER_PREFIX)).map(AlternateIdentifier:: getIdentifier).toList();
            if (!alternateIdentifiers.isEmpty()) {
                for (String alternateIdentifier: alternateIdentifiers) {
                    try {
                        String uuidValue = alternateIdentifier.substring(DATASET_SYSTEM_ALTERNATE_IDENTIFIER_PREFIX.length());
                        if (!uuidValue.isEmpty()) preprocessingDescriptionModel.setId(uuidValue);
                    } catch (IllegalArgumentException ex) {

                    }
                }

            }

        }

        if (preprocessingDescriptionModel.getId() == null && dataset.getDatasetId() != null) preprocessingDescriptionModel.setId(dataset.getDatasetId().getIdentifier());

        return preprocessingDescriptionModel;
    }
}
