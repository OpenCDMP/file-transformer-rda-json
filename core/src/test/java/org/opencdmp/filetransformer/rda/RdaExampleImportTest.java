package org.opencdmp.filetransformer.rda;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.opencdmp.commonmodels.models.FileEnvelopeModel;
import org.opencdmp.commonmodels.models.description.DescriptionModel;
import org.opencdmp.commonmodels.models.descriptiotemplate.DescriptionTemplateModel;
import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.commonmodels.models.planblueprint.PlanBlueprintModel;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaFileTransformerService;
import org.opencdmp.filetransformer.rda.support.CatalogueDefinitions;
import org.opencdmp.filetransformer.rda.support.DefinitionBuilder;
import org.opencdmp.filetransformer.rda.support.ImportedAnswers;
import org.opencdmp.filetransformer.rda.support.JsonDiff;
import org.opencdmp.filetransformer.rda.support.Outcome;
import org.opencdmp.filetransformer.rda.support.PlanBuilder;
import org.opencdmp.filetransformer.rda.support.RdaServiceFactory;
import org.opencdmp.filetransformer.rda.support.Snapshots;
import org.opencdmp.filetransformer.rda.support.ValuesBuilder;
import org.opencdmp.filetransformerbase.models.misc.DescriptionImportModel;
import org.opencdmp.filetransformerbase.models.misc.PlanImportModel;
import org.opencdmp.filetransformerbase.models.misc.PreprocessingDescriptionModel;
import org.opencdmp.filetransformerbase.models.misc.PreprocessingPlanModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Imports the official RDA DMP Common Standard 1.3 examples (RDA-DMP-Common/RDA-DMP-Common-Standard,
 * examples/JSON, copied unmodified into src/test/resources/rda-examples), the way the import wizard does:
 * preprocess the file, map every dataset to a template, import.
 * <p>
 * The targets are {@link CatalogueDefinitions}: a blueprint and a template carrying every key the catalogue says is
 * read, as an admin following it would build them. Pinned per example: the imported answers (or the failure), and
 * what exporting those answers again changes compared with the original file.
 */
class RdaExampleImportTest {

    static Stream<String> examples() {
        return Stream.of("ex1-header-fundedProject", "ex2-dataset-planned", "ex3-dataset-finished", "ex4-dataset-embargo",
                "ex5-dataset-planned-host", "ex6-dataset-closed", "ex7-dataset-many", "ex8-dmp-minimal-content",
                "ex9-dmp-long", "ex10-fairsharing");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("examples")
    void importExample(String example) throws IOException {
        byte[] file = read(example);
        RdaFileTransformerService service = RdaServiceFactory.create();
        PlanBlueprintModel blueprint = CatalogueDefinitions.blueprint();
        DescriptionTemplateModel template = CatalogueDefinitions.template();

        String prefix = "rda-examples/" + example;
        Outcome<PlanModel> importing = Outcome.of(() -> service.importPlan(importModel(service, file, blueprint, template)));
        if (importing.failed()) {
            Snapshots.assertMatches(prefix + ".import-failure.txt", importing.failure());
            Snapshots.assertAbsent(prefix + ".import.json", prefix + ".reexport-diff.txt");
            return;
        }
        Snapshots.assertAbsent(prefix + ".import-failure.txt");
        PlanModel imported = importing.value();
        Snapshots.assertMatches(prefix + ".import.json", Snapshots.canonicalJson(ImportedAnswers.of(imported), true));

        Outcome<String> reexport = Outcome.of(() -> new String(service.exportPlan(asPlan(imported, blueprint, template), null).getFile()));
        Snapshots.assertMatches(prefix + ".reexport-diff.txt", reexport.failed()
                ? "re-export " + reexport.failure()
                : JsonDiff.between(Snapshots.canonicalJson(new String(file), false), Snapshots.canonicalJson(reexport.value(), false)));
    }

    private static PlanImportModel importModel(RdaFileTransformerService service, byte[] file, PlanBlueprintModel blueprint, DescriptionTemplateModel template) {
        FileEnvelopeModel envelope = new FileEnvelopeModel();
        envelope.setFile(file);
        PreprocessingPlanModel preprocessed = service.preprocessingPlan(envelope);
        List<DescriptionImportModel> descriptions = new ArrayList<>();
        if (preprocessed.getPreprocessingDescriptionModels() != null) {
            for (PreprocessingDescriptionModel description : preprocessed.getPreprocessingDescriptionModels()) {
                DescriptionImportModel descriptionImport = new DescriptionImportModel();
                descriptionImport.setId(description.getId());
                descriptionImport.setSectionId(DefinitionBuilder.DESCRIPTIONS_SECTION);
                descriptionImport.setDescriptionTemplate(template);
                descriptions.add(descriptionImport);
            }
        }
        PlanImportModel model = new PlanImportModel();
        model.setFile(envelope);
        model.setBlueprintModel(blueprint);
        model.setDescriptions(descriptions);
        return model;
    }

    /** The imported answers as a plan the backend would hand back to the transformer for export. */
    private static PlanModel asPlan(PlanModel imported, PlanBlueprintModel blueprint, DescriptionTemplateModel template) {
        ValuesBuilder none = ValuesBuilder.values();
        PlanBuilder builder = PlanBuilder.plan(imported.getLabel() == null ? "Imported plan" : imported.getLabel()).blueprint(blueprint, none);
        PlanModel plan = builder.build();
        plan.setDescription(imported.getDescription());
        if (imported.getLanguage() != null) plan.setLanguage(imported.getLanguage());
        if (imported.getCreatedAt() != null) plan.setCreatedAt(imported.getCreatedAt());
        if (imported.getUpdatedAt() != null) plan.setUpdatedAt(imported.getUpdatedAt());
        if (imported.getProperties() != null && imported.getProperties().getFieldSets() != null) {
            plan.getProperties().setFieldSets(imported.getProperties().getFieldSets());
        }
        if (imported.getDescriptions() != null) {
            for (DescriptionModel description : imported.getDescriptions()) {
                builder.description(description.getLabel() == null ? "Imported asset" : description.getLabel(), template, none);
                plan.getDescriptions().getLast().setProperties(description.getProperties());
            }
        }
        return plan;
    }

    private static byte[] read(String example) throws IOException {
        try (InputStream stream = RdaExampleImportTest.class.getResourceAsStream("/rda-examples/" + example + ".json")) {
            if (stream == null) throw new IllegalStateException(example + " is missing from src/test/resources/rda-examples");
            return stream.readAllBytes();
        }
    }
}
