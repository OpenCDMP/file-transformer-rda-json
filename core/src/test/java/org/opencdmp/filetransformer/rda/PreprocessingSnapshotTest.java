package org.opencdmp.filetransformer.rda;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.opencdmp.commonmodels.models.FileEnvelopeModel;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaFileTransformerService;
import org.opencdmp.filetransformer.rda.support.RdaServiceFactory;
import org.opencdmp.filetransformer.rda.support.Scenarios;
import org.opencdmp.filetransformer.rda.support.Scenarios.Scenario;
import org.opencdmp.filetransformer.rda.support.Snapshots;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Pins what the import wizard is shown before an import: the plan and description labels and ids the
 * transformer reads back from an exported file, for the whole plan and for each dataset on its own.
 */
class PreprocessingSnapshotTest {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    static Stream<Scenario> scenarios() {
        return Scenarios.roundTrips();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarios")
    void preprocessing(Scenario scenario) {
        RdaFileTransformerService service = RdaServiceFactory.create();
        FileEnvelopeModel exported = service.exportPlan(scenario.plan().get(), null);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("plan", service.preprocessingPlan(exported));

        List<Object> datasets = new ArrayList<>();
        JsonNode dmp = MAPPER.readTree(new String(exported.getFile(), StandardCharsets.UTF_8)).path("dmp");
        for (JsonNode dataset : dmp.path("dataset")) {
            FileEnvelopeModel single = new FileEnvelopeModel();
            single.setFile(MAPPER.writeValueAsBytes(dataset));
            datasets.add(service.preprocessingDescription(single));
        }
        result.put("datasets", datasets);

        Snapshots.assertMatches(scenario.name() + ".preprocessing.json", Snapshots.canonicalJson(result, true));
    }
}
