package org.opencdmp.filetransformer.rda.support;

import org.opencdmp.commonmodels.enums.FieldType;
import org.opencdmp.commonmodels.models.descriptiotemplate.DescriptionTemplateModel;
import org.opencdmp.commonmodels.models.plan.PlanModel;

import java.util.List;

import static org.opencdmp.filetransformer.rda.support.DefinitionBuilder.definition;
import static org.opencdmp.filetransformer.rda.support.DefinitionBuilder.option;
import static org.opencdmp.filetransformer.rda.support.DefinitionBuilder.optionExportingNothing;
import static org.opencdmp.filetransformer.rda.support.PlanBuilder.plan;
import static org.opencdmp.filetransformer.rda.support.ValuesBuilder.values;

/**
 * Questions and sections that say which RDA object they describe, and answer options standing for key values:
 * the way a few plain questions fill a whole distribution with its host and licences.
 */
final class ObjectModelScenarios {

    private static final List<String> ACCESS = List.of("open", "Open", "shared", "Shared", "closed", "Closed");

    private ObjectModelScenarios() {
    }

    /**
     * A "Data sharing" section describing a distribution: separate questions for the repository, access, licences
     * (one per answer) and the release date make up one distribution.
     */
    static PlanModel sectionDescribesDistribution() {
        DescriptionTemplateModel template = sharingSection(true).toTemplate("Distribution section");
        return plan("Plan with a distribution section")
                .description("Survey data", template, values()
                        .answer("where", a -> a.text("host-title", "Zenodo").text("host-url", "https://zenodo.org"))
                        .answer("access", a -> a.list("data-access", "open"))
                        .answer("licences", a -> a.text("licence-url", "https://creativecommons.org/licenses/by/4.0/").date("licence-start", "2026-09-01"))
                        .answer("licences", a -> a.text("licence-url", "https://creativecommons.org/publicdomain/zero/1.0/").date("licence-start", "2027-01-01"))
                        .answer("available", a -> a.date("available", "2026-09-01")))
                .build();
    }

    /** The same questions without the section object: each question is its own distribution, as before. */
    static PlanModel sameQuestionsWithoutObject() {
        DescriptionTemplateModel template = sharingSection(false).toTemplate("Distribution questions");
        return plan("Plan with distribution questions and no object")
                .description("Survey data", template, values()
                        .answer("where", a -> a.text("host-title", "Zenodo").text("host-url", "https://zenodo.org"))
                        .answer("access", a -> a.list("data-access", "open")))
                .build();
    }

    private static DefinitionBuilder sharingSection(boolean describesDistribution) {
        DefinitionBuilder definition = definition().section("sharing");
        if (describesDistribution) definition.sectionDescribes("rda.dataset.distribution");
        return definition
                .question("where").questionDescribes("rda.dataset.distribution.host")
                .input("host-title", FieldType.FREE_TEXT, "rda.dataset.distribution.host.title")
                .input("host-url", FieldType.FREE_TEXT, "rda.dataset.distribution.host.url")
                .question("access").select("data-access", ACCESS, "rda.dataset.distribution.data_access")
                .repeatingQuestion("licences").questionDescribes("rda.dataset.distribution.license")
                .input("licence-url", FieldType.FREE_TEXT, "rda.dataset.distribution.license.license_ref")
                .input("licence-start", FieldType.DATE_PICKER, "rda.dataset.distribution.license.start_date")
                .question("available").input("available", FieldType.DATE_PICKER, "rda.dataset.distribution.available_until");
    }

    /**
     * One plain question, "Where will the data be deposited?", whose answers stand for the host's values; an access
     * question answered separately wins over the answer's own data_access. The second asset picks "Not deposited yet",
     * which exports no distribution at all.
     */
    static PlanModel answerStandsForHost() {
        DescriptionTemplateModel template = definition()
                .section("sharing").sectionDescribes("rda.dataset.distribution")
                .question("where").questionDescribes("rda.dataset.distribution.host")
                .choice("repository", List.of(
                        option("zenodo", "Zenodo",
                                "rda.dataset.distribution.host.title", "Zenodo",
                                "rda.dataset.distribution.host.url", "https://zenodo.org",
                                "rda.dataset.distribution.host.pid_system", "doi",
                                "rda.dataset.distribution.host.support_versioning", "yes",
                                "rda.dataset.distribution.data_access", "open"),
                        option("institutional", "Our institutional repository",
                                "rda.dataset.distribution.host.title", "Institutional repository",
                                "rda.dataset.distribution.host.url", "https://repository.example.org",
                                "rda.dataset.distribution.data_access", "shared"),
                        optionExportingNothing("none", "Not deposited yet")))
                .question("access").select("data-access", ACCESS, "rda.dataset.distribution.data_access")
                .toTemplate("Where the data goes");
        return plan("Plan answering where the data goes")
                .description("Survey data", template, values()
                        .answer("where", a -> a.text("repository", "zenodo"))
                        .answer("access", a -> a.list("data-access", "closed")))
                .description("Interview notes", template, values()
                        .answer("where", a -> a.text("repository", "none")))
                .build();
    }
}
