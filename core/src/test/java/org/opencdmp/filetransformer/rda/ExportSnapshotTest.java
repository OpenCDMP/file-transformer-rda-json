package org.opencdmp.filetransformer.rda;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.opencdmp.filetransformer.rda.support.Outcome;
import org.opencdmp.filetransformer.rda.support.RdaSchema;
import org.opencdmp.filetransformer.rda.support.RdaServiceFactory;
import org.opencdmp.filetransformer.rda.support.Scenarios;
import org.opencdmp.filetransformer.rda.support.Scenarios.Scenario;
import org.opencdmp.filetransformer.rda.support.Snapshots;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

/**
 * Pins the RDA JSON each scenario exports, and the RDA 1.3 schema violations in it.
 * <p>
 * The violations file is a baseline, not a pass/fail gate: it records what is wrong today,
 * so a fix (or a regression) shows up as a change to it. An export that throws is pinned as
 * {@code .export-failure.txt} instead.
 */
class ExportSnapshotTest {

    static Stream<Scenario> scenarios() {
        return Scenarios.all();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarios")
    void export(Scenario scenario) {
        Outcome<String> export = Outcome.of(() -> new String(RdaServiceFactory.create().exportPlan(scenario.plan().get(), null).getFile(), StandardCharsets.UTF_8));
        if (export.failed()) {
            Snapshots.assertMatches(scenario.name() + ".export-failure.txt", export.failure());
            Snapshots.assertAbsent(scenario.name() + ".rda.json", scenario.name() + ".schema-violations.txt");
            return;
        }
        Snapshots.assertAbsent(scenario.name() + ".export-failure.txt");
        Snapshots.assertMatches(scenario.name() + ".rda.json", Snapshots.canonicalJson(export.value(), false));
        Snapshots.assertMatches(scenario.name() + ".schema-violations.txt", RdaSchema.violations(export.value()));
    }
}
