package org.opencdmp.filetransformer.rda.support;

import org.opencdmp.commonmodels.enums.FieldType;
import org.opencdmp.commonmodels.enums.PlanUserRole;
import org.opencdmp.commonmodels.models.descriptiotemplate.DescriptionTemplateModel;
import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.commonmodels.models.reference.ReferenceModel;
import org.opencdmp.commonmodels.models.referencetype.ReferenceTypeModel;

import java.util.List;

import static org.opencdmp.filetransformer.rda.support.DefinitionBuilder.definition;
import static org.opencdmp.filetransformer.rda.support.PlanBuilder.plan;
import static org.opencdmp.filetransformer.rda.support.ReferenceBuilder.reference;
import static org.opencdmp.filetransformer.rda.support.ReferenceBuilder.referenceType;
import static org.opencdmp.filetransformer.rda.support.ValuesBuilder.values;

/**
 * One scenario per family of RDA objects, together using every semantic key the transformer reads,
 * so that moving code around cannot silently change a mapping no scenario looks at.
 */
final class ObjectScenarios {

    private static final List<String> YES_NO_UNKNOWN = List.of("yes", "Yes", "no", "No", "unknown", "I don't know");
    private static final ReferenceTypeModel DATASETS = referenceType("datasets").build();

    private ObjectScenarios() {
    }

    /** Plan-level people and identifiers answered in blueprint questions, plus plan-level references. */
    static PlanModel planPeopleAndIdentifiers() {
        ReferenceTypeModel researchers = referenceType("researchers").field("primaryEmail").build();
        ReferenceTypeModel projects = referenceType("projects").field("startDate").field("endDate").build();
        ReferenceTypeModel grants = referenceType("grants").build();
        ReferenceTypeModel funders = referenceType("funders").build();

        var blueprint = definition()
                .section("basics")
                .question("plan-title").input("plan-title", FieldType.FREE_TEXT, "rda.dmp.title")
                .question("plan-description").input("plan-description", FieldType.RICH_TEXT_AREA, "rda.dmp.description")
                .question("plan-language").select("plan-language", List.of("pt", "Portuguese", "en", "English"), "rda.dmp.language")
                .question("plan-modified").input("plan-modified", FieldType.FREE_TEXT, "rda.dmp.modified")
                .question("ethics-report").input("ethics-report", FieldType.FREE_TEXT, "rda.dmp.ethical_issues_report")
                .section("contact")
                .question("contact")
                .input("contact-name", FieldType.FREE_TEXT, "rda.dmp.contact.name")
                .input("contact-mbox", FieldType.FREE_TEXT, "rda.dmp.contact.mbox")
                .input("contact-id", FieldType.FREE_TEXT, "rda.dmp.contact.contact_id.identifier")
                .select("contact-id-type", List.of("orcid", "ORCID", "other", "Other"), "rda.dmp.contact.contact_id.type")
                .input("contact-affiliation", FieldType.FREE_TEXT, "rda.dmp.contact.affiliation.name")
                .input("contact-affiliation-id", FieldType.FREE_TEXT, "rda.dmp.contact.affiliation.affiliation_id.identifier")
                .input("contact-affiliation-id-type", FieldType.FREE_TEXT, "rda.dmp.contact.affiliation.affiliation_id.type")
                .section("people")
                .repeatingQuestion("contributor")
                .input("contributor-name", FieldType.FREE_TEXT, "rda.dmp.contributor.name")
                .input("contributor-mbox", FieldType.FREE_TEXT, "rda.dmp.contributor.mbox")
                .select("contributor-role", List.of("DataCurator", "Data curator", "ProjectLeader", "Project leader"), "rda.dmp.contributor.role")
                .input("contributor-id", FieldType.FREE_TEXT, "rda.dmp.contributor.contributor_id.identifier")
                .input("contributor-id-type", FieldType.FREE_TEXT, "rda.dmp.contributor.contributor_id.type")
                .input("contributor-affiliation", FieldType.FREE_TEXT, "rda.dmp.contributor.affiliation.name")
                .input("contributor-affiliation-id", FieldType.FREE_TEXT, "rda.dmp.contributor.affiliation.affiliation_id.identifier")
                .input("contributor-affiliation-id-type", FieldType.FREE_TEXT, "rda.dmp.contributor.affiliation.affiliation_id.type")
                .question("researchers").reference("researchers", researchers, true, "rda.dmp.contributor")
                .section("identifiers")
                .repeatingQuestion("related")
                .input("related-id", FieldType.FREE_TEXT, "rda.dmp.related_identifier.identifier")
                .input("related-relation", FieldType.FREE_TEXT, "rda.dmp.related_identifier.relation_type")
                .input("related-resource", FieldType.FREE_TEXT, "rda.dmp.related_identifier.resource_type")
                .input("related-type", FieldType.FREE_TEXT, "rda.dmp.related_identifier.type")
                .input("related-scheme", FieldType.FREE_TEXT, "rda.dmp.related_identifier.metadata_scheme")
                .input("related-scheme-type", FieldType.FREE_TEXT, "rda.dmp.related_identifier.scheme_type")
                .input("related-scheme-uri", FieldType.FREE_TEXT, "rda.dmp.related_identifier.scheme_uri")
                .question("alternate")
                .input("alternate-id", FieldType.FREE_TEXT, "rda.dmp.alternate_identifier.identifier")
                .input("alternate-type", FieldType.FREE_TEXT, "rda.dmp.alternate_identifier.type")
                .repeatingQuestion("affiliation")
                .input("affiliation-name", FieldType.FREE_TEXT, "rda.dmp.affiliation.name")
                .input("affiliation-id", FieldType.FREE_TEXT, "rda.dmp.affiliation.affiliation_id.identifier")
                .input("affiliation-id-type", FieldType.FREE_TEXT, "rda.dmp.affiliation.affiliation_id.type")
                .question("affiliation-plain").input("affiliation-plain", FieldType.FREE_TEXT, "rda.dmp.affiliation.affiliation_id")
                .section("project")
                .question("project")
                .input("project-title", FieldType.FREE_TEXT, "rda.dmp.project.title")
                .input("project-description", FieldType.TEXT_AREA, "rda.dmp.project.description")
                .input("project-id", FieldType.FREE_TEXT, "rda.dmp.project.project_id.identifier")
                .input("project-id-type", FieldType.FREE_TEXT, "rda.dmp.project.project_id.type")
                .reference("project-funder", funders, false, "rda.dmp.project.funding.funder_id")
                .input("project-funder-type", FieldType.FREE_TEXT, "rda.dmp.project.funding.funder_id.type")
                .reference("project-grant", grants, false, "rda.dmp.project.funding.grant_id")
                .input("project-grant-type", FieldType.FREE_TEXT, "rda.dmp.project.funding.grant_id.type")
                .section(DefinitionBuilder.DESCRIPTIONS_SECTION)
                .toBlueprint("People and identifiers blueprint");

        var answers = values()
                .answer("plan-title", a -> a.text("plan-title", "Estuary monitoring DMP"))
                .answer("plan-description", a -> a.text("plan-description", "<p>How estuary data is managed</p>"))
                .answer("plan-language", a -> a.list("plan-language", "pt"))
                .answer("plan-modified", a -> a.text("plan-modified", "2026-03-01T12:00:00Z"))
                .answer("ethics-report", a -> a.text("ethics-report", "https://ethics.example.org/report/42"))
                .answer("contact", a -> a.text("contact-name", "Rui Costa").text("contact-mbox", "rui.costa@example.org")
                        .text("contact-id", "https://orcid.org/0000-0002-1825-0097").list("contact-id-type", "orcid")
                        .text("contact-affiliation", "Example University").text("contact-affiliation-id", "https://ror.org/000000000").text("contact-affiliation-id-type", "ror"))
                .answer("contributor", a -> a.text("contributor-name", "Ana Pereira").text("contributor-mbox", "ana.pereira@example.org").list("contributor-role", "DataCurator")
                        .text("contributor-id", "https://orcid.org/0000-0001-5109-3700").text("contributor-id-type", "orcid")
                        .text("contributor-affiliation", "Example University").text("contributor-affiliation-id", "https://ror.org/000000000").text("contributor-affiliation-id-type", "ror"))
                .answer("contributor", a -> a.text("contributor-name", "João Santos").list("contributor-role", "ProjectLeader").text("contributor-id", "js-internal-7"))
                .answer("researchers", a -> a.references("researchers",
                        reference(researchers, "orcid:0000-0003-1419-2405", "Inês Moreira", "primaryEmail", "ines.moreira@example.org")))
                .answer("related", a -> a.text("related-id", "https://doi.org/10.1234/paper.1").text("related-relation", "IsCitedBy").text("related-resource", "Text")
                        .text("related-type", "doi").text("related-scheme", "DataCite").text("related-scheme-type", "xsd").text("related-scheme-uri", "https://schema.datacite.org/meta/kernel-4/"))
                .answer("alternate", a -> a.text("alternate-id", "EST-DMP-2026").text("alternate-type", "internal"))
                .answer("affiliation", a -> a.text("affiliation-name", "Example University").text("affiliation-id", "https://ror.org/000000000").text("affiliation-id-type", "ror"))
                .answer("affiliation-plain", a -> a.text("affiliation-plain", "https://ror.org/111111111"))
                .answer("project", a -> a.text("project-title", "Estuary monitoring").text("project-description", "Five-year monitoring programme")
                        .text("project-id", "EST-2026").text("project-id-type", "other")
                        .references("project-funder", reference(funders, "501100001871", "Fundação para a Ciência e a Tecnologia"))
                        .text("project-funder-type", "fundref")
                        .references("project-grant", reference(grants, "PTDC/CTA-AMB/1234/2026", "Estuary grant"))
                        .text("project-grant-type", "other"));

        DescriptionTemplateModel template = definition()
                .section("general").question("title").input("title", FieldType.FREE_TEXT, "rda.dataset.title")
                .toTemplate("Dataset basics");

        return plan("Plan with people and identifiers")
                .blueprint(blueprint, answers)
                .reference(reference(projects, "EST-PROJECT", "Estuary monitoring (from references)", "startDate", "2026-01-01", "endDate", "2030-12-31"))
                .reference(reference(grants, "PTDC/CTA-AMB/1234/2026", "Estuary grant"))
                .reference(reference(funders, "501100001871", "FCT"))
                .reference(reference(researchers, "orcid:0000-0002-9079-593X", "Paulo Lima", "primaryEmail", "paulo.lima@example.org"))
                .member("Sofia Rocha", "sofia.rocha@example.org", PlanUserRole.Reviewer)
                .description("Water samples", template, values().answer("title", a -> a.text("title", "Water samples")))
                .build();
    }

    /**
     * A researcher picked in the contributor reference field after a typed-in contributor with no identifier.
     * This used to make the whole export fail; the contributor is now exported, and the schema flags what it lacks.
     */
    static PlanModel contributorWithoutIdentifier() {
        ReferenceTypeModel researchers = referenceType("researchers").field("primaryEmail").build();
        var blueprint = definition()
                .section("people")
                .repeatingQuestion("contributor")
                .input("contributor-name", FieldType.FREE_TEXT, "rda.dmp.contributor.name")
                .reference("researchers", researchers, true, "rda.dmp.contributor")
                .section(DefinitionBuilder.DESCRIPTIONS_SECTION)
                .toBlueprint("Contributors blueprint");
        var answers = values()
                .answer("contributor", a -> a.text("contributor-name", "João Santos"))
                .answer("contributor", a -> a.references("researchers", reference(researchers, "orcid:0000-0003-1419-2405", "Inês Moreira")));
        return plan("Plan with a contributor without identifier")
                .blueprint(blueprint, answers)
                .build();
    }

    /**
     * The same researcher and project picked in two blueprint fields: the plan lists each reference once per field,
     * and the export used to repeat the contributor and the project for every field.
     */
    static PlanModel referencePickedTwice() {
        ReferenceTypeModel researchers = referenceType("researchers").field("primaryEmail").build();
        ReferenceTypeModel projects = referenceType("projects").field("startDate").field("endDate").build();
        ReferenceModel researcher = reference(researchers, "orcid:0000-0002-9079-593X", "Paulo Lima", "primaryEmail", "paulo.lima@example.org");
        ReferenceModel project = reference(projects, "EST-PROJECT", "Estuary monitoring");
        return plan("Plan with references picked twice")
                .reference(researcher).reference(researcher)
                .reference(project).reference(project)
                .build();
    }

    /**
     * rda.dmp.project.funding.grant_id on a text input. Import used to assume a reference input and fail,
     * although the export reads the grant from any input.
     */
    static PlanModel grantIdOnTextInput() {
        var blueprint = definition()
                .section("project")
                .question("project")
                .input("project-title", FieldType.FREE_TEXT, "rda.dmp.project.title")
                .input("project-grant", FieldType.FREE_TEXT, "rda.dmp.project.funding.grant_id")
                .section(DefinitionBuilder.DESCRIPTIONS_SECTION)
                .toBlueprint("Grant as text blueprint");
        var answers = values()
                .answer("project", a -> a.text("project-title", "Estuary monitoring").text("project-grant", "PTDC/CTA-AMB/1234/2026"));
        return plan("Plan with a grant typed as text")
                .blueprint(blueprint, answers)
                .build();
    }

    /** The plan's own identifier answered in the blueprint: exported as dmp_id (typed from its form) when there is no DOI. */
    static PlanModel planIdentifierAnswered() {
        var blueprint = definition()
                .section("basics")
                .question("plan-id").input("plan-id", FieldType.FREE_TEXT, "rda.dmp.dmp_id.identifier")
                .section(DefinitionBuilder.DESCRIPTIONS_SECTION)
                .toBlueprint("Plan identifier blueprint");
        var answers = values().answer("plan-id", a -> a.text("plan-id", "https://doi.org/10.1234/dmp.5"));
        return plan("Plan with its own identifier")
                .blueprint(blueprint, answers)
                .build();
    }

    /** No contact semantics: the contact comes from the plan's contact preset field. */
    static PlanModel planContactFallback() {
        DescriptionTemplateModel template = definition()
                .section("general").question("title").input("title", FieldType.FREE_TEXT, "rda.dataset.title")
                .toTemplate("Dataset basics");
        return plan("Plan with a contact preset")
                .contact("Clara", "Nunes", "clara.nunes@example.org")
                .description("Water samples", template, values().answer("title", a -> a.text("title", "Water samples")))
                .build();
    }

    /** Dataset-level objects in a template: creators, technical resources, security, metadata, identifiers. */
    static PlanModel datasetPeopleAndResources() {
        DescriptionTemplateModel template = definition()
                .section("identity")
                .question("title").input("title", FieldType.FREE_TEXT, "rda.dataset.title")
                .question("type").input("type", FieldType.FREE_TEXT, "rda.dataset.type")
                .question("language").select("language", List.of("por", "Portuguese", "eng", "English"), "rda.dataset.language")
                .question("reused").input("reused", FieldType.CHECK_BOX, "rda.dataset.is_reused")
                .question("rights").input("rights", FieldType.TEXT_AREA, "rda.dataset.rights")
                .question("dataset-id").reference("dataset-id", DATASETS, false, "rda.dataset.dataset_id")
                .question("alternate")
                .input("alternate-id", FieldType.FREE_TEXT, "rda.dataset.alternate_identifier.identifier")
                .input("alternate-type", FieldType.FREE_TEXT, "rda.dataset.alternate_identifier.type")
                .section("people")
                .repeatingQuestion("creator")
                .input("creator-name", FieldType.FREE_TEXT, "rda.dataset.creator.name")
                .input("creator-mbox", FieldType.FREE_TEXT, "rda.dataset.creator.mbox")
                .input("creator-legacy-id", FieldType.FREE_TEXT, "rda.dataset.creator.id")
                .input("creator-id", FieldType.FREE_TEXT, "rda.dataset.creator.creator_id.identifier")
                .input("creator-id-type", FieldType.FREE_TEXT, "rda.dataset.creator.creator_id.type")
                .input("creator-affiliation", FieldType.FREE_TEXT, "rda.dataset.creator.affiliation.name")
                .input("creator-affiliation-id", FieldType.FREE_TEXT, "rda.dataset.creator.affiliation.affiliation_id.identifier")
                .input("creator-affiliation-id-type", FieldType.FREE_TEXT, "rda.dataset.creator.affiliation.affiliation_id.type")
                .section("resources")
                .repeatingQuestion("technical")
                .input("technical-name", FieldType.FREE_TEXT, "rda.dataset.technical_resource.name")
                .input("technical-description", FieldType.TEXT_AREA, "rda.dataset.technical_resource.description")
                .input("technical-id", FieldType.FREE_TEXT, "rda.dataset.technical_resource.id.identifier")
                .input("technical-id-type", FieldType.FREE_TEXT, "rda.dataset.technical_resource.id.type")
                .repeatingQuestion("security")
                .input("security-title", FieldType.FREE_TEXT, "rda.dataset.security_and_privacy.title")
                .input("security-description", FieldType.TEXT_AREA, "rda.dataset.security_and_privacy.description")
                .repeatingQuestion("metadata")
                .select("metadata-language", List.of("eng", "English", "por", "Portuguese"), "rda.dataset.metadata.language")
                .input("metadata-description", FieldType.TEXT_AREA, "rda.dataset.metadata.description")
                .input("metadata-standard", FieldType.FREE_TEXT, "rda.dataset.metadata.metadata_standard_id.identifier")
                .select("metadata-standard-type", List.of("url", "URL", "other", "Other"), "rda.dataset.metadata.metadata_standard_id.type")
                .question("metadata-plain").input("metadata-plain", FieldType.FREE_TEXT, "rda.dataset.metadata.metadata_standard_id")
                .section("links")
                .repeatingQuestion("related")
                .input("related-id", FieldType.FREE_TEXT, "rda.dataset.related_identifier.identifier")
                .input("related-relation", FieldType.FREE_TEXT, "rda.dataset.related_identifier.relation_type")
                .input("related-resource", FieldType.FREE_TEXT, "rda.dataset.related_identifier.resource_type")
                .input("related-type", FieldType.FREE_TEXT, "rda.dataset.related_identifier.type")
                .input("related-scheme", FieldType.FREE_TEXT, "rda.dataset.related_identifier.metadata_scheme")
                .input("related-scheme-type", FieldType.FREE_TEXT, "rda.dataset.related_identifier.scheme_type")
                .input("related-scheme-uri", FieldType.FREE_TEXT, "rda.dataset.related_identifier.scheme_uri")
                .toTemplate("Dataset people and resources");

        var answers = values()
                .answer("title", a -> a.text("title", "Water samples 2026"))
                .answer("type", a -> a.text("type", "Dataset"))
                .answer("language", a -> a.list("language", "por"))
                .answer("reused", a -> a.bool("reused", false))
                .answer("rights", a -> a.text("rights", "Open after embargo"))
                .answer("dataset-id", a -> a.references("dataset-id", reference(DATASETS, "https://hdl.handle.net/20.500.12345/678", "Water samples 2026")))
                .answer("alternate", a -> a.text("alternate-id", "WS-2026").text("alternate-type", "local"))
                .answer("creator", a -> a.text("creator-name", "Rui Costa").text("creator-mbox", "rui.costa@example.org").text("creator-legacy-id", "rc-01")
                        .text("creator-id", "https://orcid.org/0000-0002-1825-0097").text("creator-id-type", "orcid")
                        .text("creator-affiliation", "Example University").text("creator-affiliation-id", "https://ror.org/000000000").text("creator-affiliation-id-type", "ror"))
                .answer("creator", a -> a.text("creator-name", "Ana Pereira"))
                .answer("technical", a -> a.text("technical-name", "Multiparameter probe").text("technical-description", "Field sensor for salinity")
                        .text("technical-id", "PROBE-7").text("technical-id-type", "serial"))
                .answer("security", a -> a.text("security-title", "Location data").text("security-description", "Sampling sites are generalised"))
                .answer("metadata", a -> a.list("metadata-language", "eng").text("metadata-description", "DataCite metadata")
                        .text("metadata-standard", "https://schema.datacite.org/").list("metadata-standard-type", "url"))
                .answer("metadata-plain", a -> a.text("metadata-plain", "https://www.dublincore.org/specifications/dublin-core/"))
                .answer("related", a -> a.text("related-id", "https://doi.org/10.1234/protocol.9").text("related-relation", "IsDocumentedBy").text("related-resource", "Text")
                        .text("related-type", "doi").text("related-scheme", "DataCite").text("related-scheme-type", "xsd").text("related-scheme-uri", "https://schema.datacite.org/meta/kernel-4/"));

        return plan("Plan with dataset people and resources")
                .description("Water samples", template, answers)
                .build();
    }

    /**
     * rda.dataset.dataset_id on a dataset identifier input. Import used to assume a reference input and fail on
     * any other input type.
     */
    static PlanModel datasetIdOnIdentifierInput() {
        DescriptionTemplateModel template = definition()
                .section("identity")
                .question("title").input("title", FieldType.FREE_TEXT, "rda.dataset.title")
                .question("dataset-id").input("dataset-id", FieldType.DATASET_IDENTIFIER, "rda.dataset.dataset_id")
                .toTemplate("Dataset identifier input");
        var answers = values()
                .answer("title", a -> a.text("title", "Water samples 2026"))
                .answer("dataset-id", a -> a.identifier("dataset-id", "https://hdl.handle.net/20.500.12345/678", "handle"));
        return plan("Plan with a dataset identifier input")
                .description("Water samples", template, answers)
                .build();
    }

    /**
     * Properties put on less usual input types, so the import has to convert between value kinds
     * (a date into a text input, keywords into a reference, yes/no into a Yes/No input, and so on).
     */
    static PlanModel oddInputTypes() {
        ReferenceTypeModel keywords = referenceType("keywords").build();
        ReferenceTypeModel licences = referenceType("licenses").build();
        DescriptionTemplateModel template = definition()
                .section("identity")
                .question("title").input("title", FieldType.FREE_TEXT, "rda.dataset.title")
                .question("keywords").reference("keywords", keywords, true, "rda.dataset.keyword")
                .question("summary").input("summary", FieldType.TAGS, "rda.dataset.description")
                .question("issued").input("issued", FieldType.DATE_PICKER, "rda.dataset.issued")
                .question("identifier").input("identifier", FieldType.VALIDATION, "rda.dataset.dataset_id.identifier")
                .question("language").radio("language", List.of("eng", "English", "por", "Portuguese"), "rda.dataset.language")
                .question("reused").input("reused", FieldType.BOOLEAN_DECISION, "rda.dataset.is_reused")
                .question("personal").input("personal", FieldType.BOOLEAN_DECISION, "rda.dataset.personal_data")
                .question("type").select("type", List.of("Dataset", "Dataset", "Software", "Software"), "rda.dataset.type")
                .section("sharing")
                .repeatingQuestion("distribution")
                .input("dist-title", FieldType.FREE_TEXT, "rda.dataset.distribution.title")
                .input("dist-issued", FieldType.FREE_TEXT, "rda.dataset.distribution.issued")
                .input("dist-format", FieldType.FREE_TEXT, "rda.dataset.distribution.format")
                .input("dist-size", FieldType.NUMBER, "rda.dataset.distribution.byte_size")
                .radio("dist-access", List.of("open", "Open", "shared", "Shared", "closed", "Closed"), "rda.dataset.distribution.data_access")
                .input("host-title", FieldType.FREE_TEXT, "rda.dataset.distribution.host.title")
                .input("host-url", FieldType.FREE_TEXT, "rda.dataset.distribution.host.url")
                .reference("licence", licences, false, "rda.dataset.distribution.license.license_ref")
                .input("licence-start", FieldType.FREE_TEXT, "rda.dataset.distribution.license.start_date")
                .section("plan-level")
                .question("project")
                .input("project-title", FieldType.FREE_TEXT, "rda.dmp.project.title")
                .input("project-start", FieldType.FREE_TEXT, "rda.dmp.project.start")
                .input("project-end", FieldType.DATE_PICKER, "rda.dmp.project.end")
                .question("cost")
                .input("cost-title", FieldType.FREE_TEXT, "rda.dmp.cost.title")
                .input("cost-value", FieldType.FREE_TEXT, "rda.dmp.cost.value")
                .toTemplate("Odd input types");

        var answers = values()
                .answer("title", a -> a.text("title", "Tide gauges"))
                .answer("keywords", a -> a.references("keywords", reference(keywords, "tides", "tides"), reference(keywords, "sea level", "sea level")))
                .answer("summary", a -> a.list("summary", "Hourly sea level"))
                .answer("issued", a -> a.date("issued", "2026-08-15"))
                .answer("identifier", a -> a.identifier("identifier", "10.5281/zenodo.7654321", "doi"))
                .answer("language", a -> a.text("language", "eng"))
                .answer("reused", a -> a.bool("reused", true))
                .answer("personal", a -> a.bool("personal", false))
                .answer("type", a -> a.list("type", "Dataset"))
                .answer("distribution", a -> a.text("dist-title", "Gauge readings").text("dist-issued", "2026-08-15").text("dist-format", "text/csv")
                        .number("dist-size", 4096).text("dist-access", "open")
                        .text("host-title", "Zenodo").text("host-url", "https://zenodo.org")
                        .references("licence", reference(licences, "https://creativecommons.org/licenses/by/4.0/", "CC BY 4.0"))
                        .text("licence-start", "2026-08-15"))
                .answer("project", a -> a.text("project-title", "Sea level watch").text("project-start", "2026-04-01T00:00:00Z").date("project-end", "2029-03-31"))
                .answer("cost", a -> a.text("cost-title", "Sensors").text("cost-value", "950"));

        return plan("Plan with odd input types")
                .description("Tide gauges", template, answers)
                .build();
    }

    /** Plan-level objects answered inside an asset template, which the transformer merges into the dmp. */
    static PlanModel planObjectsFromTemplate() {
        DescriptionTemplateModel template = definition()
                .section("general")
                .question("title").input("title", FieldType.FREE_TEXT, "rda.dataset.title")
                .section("plan-level")
                .question("ethics")
                .radio("ethics-exist", YES_NO_UNKNOWN, "rda.dmp.ethical_issues_exist")
                .input("ethics-description", FieldType.TEXT_AREA, "rda.dmp.ethical_issues_description")
                .input("ethics-report", FieldType.FREE_TEXT, "rda.dmp.ethical_issues_report")
                .repeatingQuestion("cost")
                .input("cost-title", FieldType.FREE_TEXT, "rda.dmp.cost.title")
                .input("cost-description", FieldType.TEXT_AREA, "rda.dmp.cost.description")
                .input("cost-value", FieldType.NUMBER, "rda.dmp.cost.value")
                .input("cost-currency", FieldType.FREE_TEXT, "rda.dmp.cost.currency_code")
                .repeatingQuestion("contributor")
                .input("contributor-name", FieldType.FREE_TEXT, "rda.dmp.contributor.name")
                .input("contributor-mbox", FieldType.FREE_TEXT, "rda.dmp.contributor.mbox")
                .input("contributor-role", FieldType.FREE_TEXT, "rda.dmp.contributor.role")
                .input("contributor-id", FieldType.FREE_TEXT, "rda.dmp.contributor.contributor_id.identifier")
                .question("project")
                .input("project-title", FieldType.FREE_TEXT, "rda.dmp.project.title")
                .input("project-start", FieldType.DATE_PICKER, "rda.dmp.project.start")
                .input("project-funder", FieldType.FREE_TEXT, "rda.dmp.project.funding.funder_id.identifier")
                .input("project-status", FieldType.FREE_TEXT, "rda.dmp.project.funding.funding_status")
                .question("affiliation")
                .input("affiliation-name", FieldType.FREE_TEXT, "rda.dmp.affiliation.name")
                .input("affiliation-id", FieldType.FREE_TEXT, "rda.dmp.affiliation.affiliation_id.identifier")
                .question("related")
                .input("related-id", FieldType.FREE_TEXT, "rda.dmp.related_identifier.identifier")
                .input("related-relation", FieldType.FREE_TEXT, "rda.dmp.related_identifier.relation_type")
                .question("alternate")
                .input("alternate-id", FieldType.FREE_TEXT, "rda.dmp.alternate_identifier.identifier")
                .input("alternate-type", FieldType.FREE_TEXT, "rda.dmp.alternate_identifier.type")
                .toTemplate("Template with plan-level questions");

        var answers = values()
                .answer("title", a -> a.text("title", "Fish counts"))
                .answer("ethics", a -> a.text("ethics-exist", "no").text("ethics-description", "No human subjects").text("ethics-report", "https://ethics.example.org/none"))
                .answer("cost", a -> a.text("cost-title", "Field trips").text("cost-description", "Boat hire").number("cost-value", 3000).text("cost-currency", "EUR"))
                .answer("contributor", a -> a.text("contributor-name", "Marta Dias").text("contributor-mbox", "marta.dias@example.org").text("contributor-role", "DataCollector")
                        .text("contributor-id", "https://orcid.org/0000-0001-2345-6789"))
                .answer("project", a -> a.text("project-title", "Fish census").date("project-start", "2026-04-01")
                        .text("project-funder", "http://dx.doi.org/10.13039/501100001871").text("project-status", "applied"))
                .answer("affiliation", a -> a.text("affiliation-name", "Marine Institute").text("affiliation-id", "https://ror.org/222222222"))
                .answer("related", a -> a.text("related-id", "https://doi.org/10.1234/census.2025").text("related-relation", "IsNewVersionOf"))
                .answer("alternate", a -> a.text("alternate-id", "FC-2026").text("alternate-type", "local"));

        return plan("Plan with plan-level questions in a template")
                .description("Fish counts", template, answers)
                .build();
    }

    /** Every distribution and host property answered as its own input. */
    static PlanModel distributionAllProperties() {
        DescriptionTemplateModel template = definition()
                .section("sharing")
                .repeatingQuestion("distribution")
                .input("title", FieldType.FREE_TEXT, "rda.dataset.distribution.title")
                .input("description", FieldType.TEXT_AREA, "rda.dataset.distribution.description")
                .input("download", FieldType.FREE_TEXT, "rda.dataset.distribution.download_url")
                .input("access", FieldType.FREE_TEXT, "rda.dataset.distribution.access_url")
                .input("data-access", FieldType.FREE_TEXT, "rda.dataset.distribution.data_access")
                .input("size", FieldType.FREE_TEXT, "rda.dataset.distribution.byte_size")
                .input("issued", FieldType.DATE_PICKER, "rda.dataset.distribution.issued")
                .input("host-title", FieldType.FREE_TEXT, "rda.dataset.distribution.host.title")
                .input("host-description", FieldType.TEXT_AREA, "rda.dataset.distribution.host.description")
                .input("host-url", FieldType.FREE_TEXT, "rda.dataset.distribution.host.url")
                .input("host-availability", FieldType.FREE_TEXT, "rda.dataset.distribution.host.availability")
                .input("host-backup-frequency", FieldType.FREE_TEXT, "rda.dataset.distribution.host.backup_frequency")
                .input("host-backup-type", FieldType.FREE_TEXT, "rda.dataset.distribution.host.backup_type")
                .input("host-certified", FieldType.FREE_TEXT, "rda.dataset.distribution.host.certified_with")
                .input("host-geo", FieldType.FREE_TEXT, "rda.dataset.distribution.host.geo_location")
                .input("host-storage", FieldType.FREE_TEXT, "rda.dataset.distribution.host.storage_type")
                .input("host-pid", FieldType.FREE_TEXT, "rda.dataset.distribution.host.pid_system")
                .input("host-versioning", FieldType.FREE_TEXT, "rda.dataset.distribution.host.support_versioning")
                .input("host-id", FieldType.FREE_TEXT, "rda.dataset.distribution.host.host_id.identifier")
                .input("host-id-type", FieldType.FREE_TEXT, "rda.dataset.distribution.host.host_id.type")
                .input("exists", FieldType.BOOLEAN_DECISION, "rda.dataset.distribution.exists")
                .toTemplate("Distribution with every property");

        var answers = values()
                .answer("distribution", a -> a.text("title", "Sensor data").text("description", "Hourly readings")
                        .text("download", "https://repository.example.org/files/sensors.zip").text("access", "https://repository.example.org/record/1")
                        .text("data-access", "open").text("size", "1048576").date("issued", "2026-05-01")
                        .text("host-title", "Institutional repository").text("host-description", "Run by the library").text("host-url", "https://repository.example.org")
                        .text("host-availability", "99.5").text("host-backup-frequency", "daily").text("host-backup-type", "tape")
                        .text("host-certified", "coretrustseal").text("host-geo", "PT").text("host-storage", "disk")
                        .text("host-pid", "handle").text("host-versioning", "yes").text("host-id", "https://www.re3data.org/repository/r3d100000001").text("host-id-type", "url")
                        .bool("exists", true));

        return plan("Plan with every distribution property")
                .description("Sensor data", template, answers)
                .build();
    }

    /**
     * A whole distribution picked as a reference whose type fields carry the distribution semantics.
     * Known: the host built from the reference is overwritten by the (empty) host from inputs, so it is lost;
     * the import then fails on the host-less distribution.
     */
    static PlanModel distributionFromReferenceField() {
        ReferenceTypeModel distributions = referenceType("distributions")
                .field("title", "rda.dataset.distribution.title")
                .field("description", "rda.dataset.distribution.description")
                .field("format", "rda.dataset.distribution.format")
                .field("download", "rda.dataset.distribution.download_url")
                .field("access", "rda.dataset.distribution.access_url")
                .field("dataAccess", "rda.dataset.distribution.data_access")
                .field("size", "rda.dataset.distribution.byte_size")
                .field("until", "rda.dataset.distribution.available_until")
                .field("issued", "rda.dataset.distribution.issued")
                .field("hostTitle", "rda.dataset.distribution.host.title")
                .field("hostUrl", "rda.dataset.distribution.host.url")
                .field("licenseUri", "rda.dataset.distribution.license.license_ref")
                .field("licenseStart", "rda.dataset.distribution.license.start_date")
                .build();

        DescriptionTemplateModel template = definition()
                .section("sharing")
                .repeatingQuestion("picked")
                .reference("picked-distribution", distributions, false, "rda.dataset.distribution")
                .input("picked-description", FieldType.TEXT_AREA, "rda.dataset.distribution.description")
                .toTemplate("Distribution from a reference");

        var answers = values()
                .answer("picked", a -> a
                        .references("picked-distribution", reference(distributions, "dist-1", "Model output",
                                "title", "Model output", "description", "From the reference", "format", "application/netcdf",
                                "download", "https://data.example.org/model.nc", "access", "https://data.example.org/model",
                                "dataAccess", "shared", "size", "2048", "until", "2031-12-31", "issued", "2026-07-01",
                                "hostTitle", "Data portal", "hostUrl", "https://data.example.org",
                                "licenseUri", "https://creativecommons.org/licenses/by-nc/4.0/", "licenseStart", "2026-07-01"))
                        .text("picked-description", "Typed by the author"));

        return plan("Plan with a distribution picked as a reference")
                .description("Model output", template, answers)
                .build();
    }
}
