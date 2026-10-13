package org.opencdmp.filetransformer.rda;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.opencdmp.commonmodels.models.FileEnvelopeModel;
import org.opencdmp.commonmodels.models.description.DescriptionModel;
import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaFileTransformerService;
import org.opencdmp.filetransformer.rda.support.JsonDiff;
import org.opencdmp.filetransformer.rda.support.Outcome;
import org.opencdmp.filetransformer.rda.support.RdaServiceFactory;
import org.opencdmp.filetransformer.rda.support.Scenarios;
import org.opencdmp.filetransformer.rda.support.Scenarios.Scenario;
import org.opencdmp.filetransformer.rda.support.Snapshots;
import org.opencdmp.filetransformerbase.models.misc.DescriptionImportModel;
import org.opencdmp.filetransformerbase.models.misc.PlanImportModel;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Export, import the result into the same blueprint and templates, then export again.
 * <p>
 * Two snapshots per scenario: the answers the import wrote ({@code .import.json}), and what changed between
 * the first and the second export ({@code .roundtrip-diff.txt}). A lossless round trip reads "(identical)".
 * An import that throws is pinned as {@code .import-failure.txt}. Scenarios whose export fails are left to
 * {@link ExportSnapshotTest}.
 */
class ImportRoundTripTest {

    static Stream<Scenario> scenarios() {
        return Scenarios.roundTrips();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarios")
    void roundTrip(Scenario scenario) {
        RdaFileTransformerService service = RdaServiceFactory.create();
        PlanModel original = scenario.plan().get();
        String firstExport = export(service, original);

        Outcome<PlanModel> importing = Outcome.of(() -> service.importPlan(importModel(original, firstExport)));
        if (importing.failed()) {
            Snapshots.assertMatches(scenario.name() + ".import-failure.txt", importing.failure());
            Snapshots.assertAbsent(scenario.name() + ".import.json", scenario.name() + ".roundtrip-diff.txt");
            return;
        }
        Snapshots.assertAbsent(scenario.name() + ".import-failure.txt");
        PlanModel imported = importing.value();
        Snapshots.assertMatches(scenario.name() + ".import.json", Snapshots.canonicalJson(answers(imported), true));

        PlanModel reloaded = withImportedAnswers(scenario.plan().get(), imported);
        String secondExport = export(service, reloaded);
        Snapshots.assertMatches(scenario.name() + ".roundtrip-diff.txt",
                JsonDiff.between(Snapshots.canonicalJson(firstExport, false), Snapshots.canonicalJson(secondExport, false)));
    }

    private static String export(RdaFileTransformerService service, PlanModel plan) {
        return new String(service.exportPlan(plan, null).getFile(), StandardCharsets.UTF_8);
    }

    private static PlanImportModel importModel(PlanModel plan, String rdaJson) {
        FileEnvelopeModel file = new FileEnvelopeModel();
        file.setFile(rdaJson.getBytes(StandardCharsets.UTF_8));
        file.setFilename(plan.getLabel() + ".json");
        PlanImportModel model = new PlanImportModel();
        model.setFile(file);
        model.setBlueprintModel(plan.getPlanBlueprint());
        List<DescriptionImportModel> descriptions = new ArrayList<>();
        for (DescriptionModel description : plan.getDescriptions()) {
            DescriptionImportModel descriptionImport = new DescriptionImportModel();
            descriptionImport.setId(description.getId().toString());
            descriptionImport.setSectionId(description.getSectionId());
            descriptionImport.setDescriptionTemplate(description.getDescriptionTemplate());
            descriptions.add(descriptionImport);
        }
        model.setDescriptions(descriptions);
        return model;
    }

    /** Only the answers: the templates and blueprint the import echoes back are the ones passed in. */
    private static Map<String, Object> answers(PlanModel imported) {
        Map<String, Object> answers = new LinkedHashMap<>();
        answers.put("label", imported.getLabel());
        answers.put("plan", imported.getProperties() == null ? null : imported.getProperties().getFieldSets());
        List<Object> descriptions = new ArrayList<>();
        if (imported.getDescriptions() != null) {
            for (DescriptionModel description : imported.getDescriptions()) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("label", description.getLabel());
                entry.put("answers", description.getProperties() == null ? null : description.getProperties().getFieldSets());
                descriptions.add(entry);
            }
        }
        answers.put("descriptions", descriptions);
        return answers;
    }

    /** The original plan, with its answers replaced by the imported ones (descriptions matched in order). */
    private static PlanModel withImportedAnswers(PlanModel plan, PlanModel imported) {
        if (imported.getProperties() != null && imported.getProperties().getFieldSets() != null) {
            plan.getProperties().setFieldSets(imported.getProperties().getFieldSets());
        }
        List<DescriptionModel> importedDescriptions = imported.getDescriptions() == null ? List.of() : imported.getDescriptions();
        for (int i = 0; i < plan.getDescriptions().size() && i < importedDescriptions.size(); i++) {
            plan.getDescriptions().get(i).setProperties(importedDescriptions.get(i).getProperties());
        }
        return plan;
    }
}
