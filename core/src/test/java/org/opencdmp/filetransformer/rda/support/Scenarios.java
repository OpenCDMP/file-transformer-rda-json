package org.opencdmp.filetransformer.rda.support;

import org.opencdmp.commonmodels.enums.FieldType;
import org.opencdmp.commonmodels.models.descriptiotemplate.DescriptionTemplateModel;
import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.commonmodels.models.referencetype.ReferenceTypeModel;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.opencdmp.filetransformer.rda.support.DefinitionBuilder.definition;
import static org.opencdmp.filetransformer.rda.support.PlanBuilder.plan;
import static org.opencdmp.filetransformer.rda.support.ReferenceBuilder.reference;
import static org.opencdmp.filetransformer.rda.support.ReferenceBuilder.referenceType;
import static org.opencdmp.filetransformer.rda.support.ValuesBuilder.values;

/**
 * The plans the snapshot tests run through the transformer. Each scenario pins down today's behaviour,
 * including the known bugs noted on it, so a fix shows up as a reviewed change to its snapshot.
 * <p>
 * Plans captured from a real installation (see src/test/resources/fixtures/README.md) are added as
 * scenarios named after their file.
 */
public final class Scenarios {

    private static final Path FIXTURES = Path.of("src", "test", "resources", "fixtures");
    private static final List<String> YES_NO_UNKNOWN = List.of("yes", "Yes", "no", "No", "unknown", "I don't know");

    public record Scenario(String name, Supplier<PlanModel> plan, boolean roundTrip) {
        @Override
        public String toString() {
            return this.name;
        }
    }

    private Scenarios() {
    }

    public static Stream<Scenario> all() {
        List<Scenario> scenarios = new ArrayList<>(List.of(
                new Scenario("minimal-plan", Scenarios::minimalPlan, true),
                new Scenario("plan-level-semantics", Scenarios::planLevelSemantics, true),
                new Scenario("dataset-scalars", Scenarios::datasetScalars, true),
                new Scenario("distribution-repeating-question", Scenarios::distributionRepeatingQuestion, true),
                new Scenario("distribution-split-across-questions", Scenarios::distributionSplitAcrossQuestions, true),
                new Scenario("distribution-host-from-references", Scenarios::distributionHostFromReferences, true),
                new Scenario("distribution-empty-and-invalid", Scenarios::distributionEmptyAndInvalid, false),
                new Scenario("plan-people-and-identifiers", ObjectScenarios::planPeopleAndIdentifiers, true),
                new Scenario("plan-contact-fallback", ObjectScenarios::planContactFallback, true),
                new Scenario("plan-identifier-answered", ObjectScenarios::planIdentifierAnswered, true),
                new Scenario("dataset-people-and-resources", ObjectScenarios::datasetPeopleAndResources, true),
                new Scenario("plan-objects-from-template", ObjectScenarios::planObjectsFromTemplate, true),
                new Scenario("odd-input-types", ObjectScenarios::oddInputTypes, true),
                new Scenario("section-describes-distribution", ObjectModelScenarios::sectionDescribesDistribution, true),
                new Scenario("same-questions-without-object", ObjectModelScenarios::sameQuestionsWithoutObject, true),
                new Scenario("answer-stands-for-host", ObjectModelScenarios::answerStandsForHost, true),
                new Scenario("distribution-all-properties", ObjectScenarios::distributionAllProperties, true),
                new Scenario("distribution-from-reference-field", ObjectScenarios::distributionFromReferenceField, true),
                new Scenario("contributor-without-identifier", ObjectScenarios::contributorWithoutIdentifier, true),
                new Scenario("reference-picked-twice", ObjectScenarios::referencePickedTwice, true),
                new Scenario("dataset-id-on-identifier-input", ObjectScenarios::datasetIdOnIdentifierInput, true),
                new Scenario("grant-id-on-text-input", ObjectScenarios::grantIdOnTextInput, true)));
        scenarios.addAll(captured());
        return scenarios.stream();
    }

    public static Stream<Scenario> roundTrips() {
        return all().filter(Scenario::roundTrip);
    }

    /** No semantics anywhere: everything in the export comes from plan data and fallbacks. */
    private static PlanModel minimalPlan() {
        DescriptionTemplateModel template = definition()
                .section("general").question("notes").input("notes", FieldType.TEXT_AREA)
                .toTemplate("Plain template");
        return plan("Minimal plan")
                .description("Survey data", template, values().answer("notes", a -> a.text("notes", "Collected by questionnaire")))
                .tags("survey", "social science")
                .build();
    }

    /**
     * Blueprint questions mapped to dmp-level properties.
     * Known: rda.dmp.created on a date input is ignored (it only parses a full instant) and falls back to the plan date.
     */
    private static PlanModel planLevelSemantics() {
        var blueprint = definition()
                .section("ethics")
                .question("ethical-issues")
                .radio("ethical-issues-exist", YES_NO_UNKNOWN, "rda.dmp.ethical_issues_exist")
                .input("ethical-issues-description", FieldType.TEXT_AREA, "rda.dmp.ethical_issues_description")
                .question("plan-created").input("plan-created", FieldType.DATE_PICKER, "rda.dmp.created")
                .section("costs")
                .repeatingQuestion("cost")
                .input("cost-title", FieldType.FREE_TEXT, "rda.dmp.cost.title")
                .input("cost-value", FieldType.NUMBER, "rda.dmp.cost.value")
                .select("cost-currency", List.of("EUR", "Euro", "USD", "US dollar"), "rda.dmp.cost.currency_code")
                .section("project")
                .question("project")
                .input("project-title", FieldType.FREE_TEXT, "rda.dmp.project.title")
                .input("project-start", FieldType.DATE_PICKER, "rda.dmp.project.start")
                .input("project-end", FieldType.DATE_PICKER, "rda.dmp.project.end")
                .input("funder", FieldType.FREE_TEXT, "rda.dmp.project.funding.funder_id.identifier")
                .input("grant", FieldType.FREE_TEXT, "rda.dmp.project.funding.grant_id.identifier")
                .select("funding-status", List.of("granted", "Granted", "applied", "Applied"), "rda.dmp.project.funding.funding_status")
                .section(DefinitionBuilder.DESCRIPTIONS_SECTION)
                .toBlueprint("Funder blueprint");
        var answers = values()
                .answer("ethical-issues", a -> a.text("ethical-issues-exist", "yes").text("ethical-issues-description", "Interviews with minors"))
                .answer("plan-created", a -> a.date("plan-created", "2025-11-20"))
                .answer("cost", a -> a.text("cost-title", "Storage").number("cost-value", 1200).list("cost-currency", "EUR"))
                .answer("cost", a -> a.text("cost-title", "Curation").number("cost-value", 800.5).list("cost-currency", "EUR"))
                .answer("project", a -> a.text("project-title", "Coastal Futures").date("project-start", "2026-01-01").date("project-end", "2028-12-31")
                        .text("funder", "http://dx.doi.org/10.13039/501100000780").text("grant", "101000123").list("funding-status", "granted"));
        DescriptionTemplateModel template = definition()
                .section("general").question("title").input("title", FieldType.FREE_TEXT, "rda.dataset.title")
                .toTemplate("Dataset basics");
        return plan("Plan with plan-level semantics")
                .blueprint(blueprint, answers)
                .description("Interview transcripts", template, values().answer("title", a -> a.text("title", "Interview transcripts")))
                .build();
    }

    /**
     * Single-valued dataset properties.
     * Known: only the first tag reaches keyword; a Yes/No input sends "true", which sensitive_data rejects,
     * so it falls back to "unknown".
     */
    private static PlanModel datasetScalars() {
        DescriptionTemplateModel template = definition()
                .section("identity")
                .question("title").input("title", FieldType.FREE_TEXT, "rda.dataset.title")
                .question("summary").input("summary", FieldType.TEXT_AREA, "rda.dataset.description")
                .question("keywords").input("keywords", FieldType.TAGS, "rda.dataset.keyword")
                .question("identifier").input("identifier", FieldType.FREE_TEXT, "rda.dataset.dataset_id.identifier")
                .select("identifier-type", List.of("doi", "DOI", "handle", "Handle"), "rda.dataset.dataset_id.type")
                .question("issued").input("issued", FieldType.DATE_PICKER, "rda.dataset.issued")
                .section("sensitivity")
                .question("personal-data").radio("personal-data", YES_NO_UNKNOWN, "rda.dataset.personal_data")
                .question("sensitive-data").input("sensitive-data", FieldType.BOOLEAN_DECISION, "rda.dataset.sensitive_data")
                .section("care")
                .question("preservation").input("preservation", FieldType.TEXT_AREA, "rda.dataset.preservation_statement")
                .question("quality").input("quality", FieldType.TEXT_AREA, "rda.dataset.data_quality_assurance")
                .toTemplate("Dataset scalars");
        var answers = values()
                .answer("title", a -> a.text("title", "Sediment cores 2026"))
                .answer("summary", a -> a.text("summary", "Cores from three estuaries"))
                .answer("keywords", a -> a.list("keywords", "sediment", "estuary", "geochemistry"))
                .answer("identifier", a -> a.text("identifier", "10.5281/zenodo.1234567").list("identifier-type", "doi"))
                .answer("issued", a -> a.date("issued", "2026-06-30"))
                .answer("personal-data", a -> a.text("personal-data", "no"))
                .answer("sensitive-data", a -> a.bool("sensitive-data", true))
                .answer("preservation", a -> a.text("preservation", "Kept for 10 years"))
                .answer("quality", a -> a.text("quality", "Duplicate measurements"));
        return plan("Plan with dataset scalars")
                .description("Sediment cores", template, answers)
                .build();
    }

    /**
     * The supported way to describe distributions today: one repeating question, every property an input.
     * Known: byte_size from a Number input ("2.5E9") never parses; only the first format tag is kept.
     */
    private static PlanModel distributionRepeatingQuestion() {
        DescriptionTemplateModel template = definition()
                .section("sharing")
                .repeatingQuestion("distribution")
                .input("dist-title", FieldType.FREE_TEXT, "rda.dataset.distribution.title")
                .select("data-access", List.of("open", "Open", "shared", "Shared", "closed", "Closed"), "rda.dataset.distribution.data_access")
                .input("format", FieldType.TAGS, "rda.dataset.distribution.format")
                .input("size", FieldType.NUMBER, "rda.dataset.distribution.byte_size")
                .input("available-until", FieldType.DATE_PICKER, "rda.dataset.distribution.available_until")
                .input("host-title", FieldType.FREE_TEXT, "rda.dataset.distribution.host.title")
                .input("host-url", FieldType.FREE_TEXT, "rda.dataset.distribution.host.url")
                .select("host-pid", List.of("doi", "DOI", "handle", "Handle"), "rda.dataset.distribution.host.pid_system")
                .radio("host-versioning", YES_NO_UNKNOWN, "rda.dataset.distribution.host.support_versioning")
                .input("license-url", FieldType.FREE_TEXT, "rda.dataset.distribution.license.license_ref")
                .input("license-start", FieldType.DATE_PICKER, "rda.dataset.distribution.license.start_date")
                .toTemplate("Distribution as one repeating question");
        var answers = values()
                .answer("distribution", a -> a.text("dist-title", "Raw data").list("data-access", "open").list("format", "text/csv", "application/zip")
                        .number("size", 2_500_000_000d).date("available-until", "2036-06-30")
                        .text("host-title", "Zenodo").text("host-url", "https://zenodo.org").list("host-pid", "doi").text("host-versioning", "yes")
                        .text("license-url", "https://creativecommons.org/licenses/by/4.0/").date("license-start", "2026-06-30"))
                .answer("distribution", a -> a.text("dist-title", "Processed data").list("data-access", "shared")
                        .text("host-title", "Institutional repository").text("host-url", "https://repository.example.org").text("host-versioning", "no"));
        return plan("Plan with a repeating distribution question")
                .description("Sediment cores", template, answers)
                .build();
    }

    /**
     * The layout users ask for: separate questions for "where" and "who can access".
     * Known: each question becomes its own distribution, one with only a host and one with only data_access.
     */
    private static PlanModel distributionSplitAcrossQuestions() {
        DescriptionTemplateModel template = definition()
                .section("sharing")
                .question("where")
                .input("host-title", FieldType.FREE_TEXT, "rda.dataset.distribution.host.title")
                .input("host-url", FieldType.FREE_TEXT, "rda.dataset.distribution.host.url")
                .question("access")
                .select("data-access", List.of("open", "Open", "shared", "Shared", "closed", "Closed"), "rda.dataset.distribution.data_access")
                .toTemplate("Distribution split across questions");
        var answers = values()
                .answer("where", a -> a.text("host-title", "Zenodo").text("host-url", "https://zenodo.org"))
                .answer("access", a -> a.list("data-access", "open"));
        return plan("Plan with distribution split across questions")
                .description("Sediment cores", template, answers)
                .build();
    }

    /**
     * Host and licence filled from references whose type fields carry semantics (re3data-style repositories).
     * Known: with two repositories picked, only the last one survives, as a single distribution.
     */
    private static PlanModel distributionHostFromReferences() {
        ReferenceTypeModel repositories = referenceType("repositories")
                .field("name", "rda.dataset.distribution.host.title")
                .field("url", "rda.dataset.distribution.host.url")
                .field("pidSystem", "rda.dataset.distribution.host.pid_system")
                .field("versioning", "rda.dataset.distribution.host.support_versioning")
                .build();
        ReferenceTypeModel licences = referenceType("licenses")
                .field("uri", "rda.dataset.distribution.license.license_ref")
                .field("start", "rda.dataset.distribution.license.start_date")
                .build();
        DescriptionTemplateModel template = definition()
                .section("sharing")
                .repeatingQuestion("deposit")
                .reference("repository", repositories, true, "rda.dataset.distribution.host")
                .reference("licence", licences, false, "rda.dataset.distribution.license")
                .toTemplate("Distribution from references");
        var answers = values()
                .answer("deposit", a -> a
                        .references("repository",
                                reference(repositories, "r3d100010468", "Zenodo", "name", "Zenodo", "url", "https://zenodo.org", "pidSystem", "doi", "versioning", "yes"),
                                reference(repositories, "r3d100000044", "Dryad", "name", "Dryad", "url", "https://datadryad.org", "pidSystem", "doi", "versioning", "no"))
                        .references("licence",
                                reference(licences, "CC-BY-4.0", "Creative Commons Attribution 4.0", "uri", "https://creativecommons.org/licenses/by/4.0/", "start", "2026-06-30")));
        return plan("Plan with hosts from references")
                .description("Sediment cores", template, answers)
                .build();
    }

    /**
     * Known: an answer with no values still produces a distribution, and an invalid data_access value
     * ("public") is dropped with only a log warning.
     */
    private static PlanModel distributionEmptyAndInvalid() {
        DescriptionTemplateModel template = definition()
                .section("sharing")
                .repeatingQuestion("distribution")
                .input("dist-title", FieldType.FREE_TEXT, "rda.dataset.distribution.title")
                .input("data-access", FieldType.FREE_TEXT, "rda.dataset.distribution.data_access")
                .toTemplate("Distribution with gaps");
        var answers = values()
                .answer("distribution", a -> a.empty("dist-title").empty("data-access"))
                .answer("distribution", a -> a.text("dist-title", "Raw data").text("data-access", "public"));
        return plan("Plan with an empty and an invalid distribution")
                .description("Sediment cores", template, answers)
                .build();
    }

    private static List<Scenario> captured() {
        if (!Files.isDirectory(FIXTURES)) return List.of();
        JsonMapper mapper = JsonMapper.builder().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build();
        try (Stream<Path> files = Files.list(FIXTURES)) {
            return files
                    .filter(file -> file.getFileName().toString().endsWith(".plan.json"))
                    .sorted()
                    .map(file -> {
                        String name = "captured-" + file.getFileName().toString().replace(".plan.json", "");
                        return new Scenario(name, () -> mapper.readValue(file.toFile(), PlanModel.class), true);
                    })
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
