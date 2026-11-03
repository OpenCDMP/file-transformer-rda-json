package org.opencdmp.filetransformer.rda;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.opencdmp.commonmodels.enums.FieldType;
import org.opencdmp.commonmodels.models.descriptiotemplate.DescriptionTemplateModel;
import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.commonmodels.models.referencetype.ReferenceTypeModel;
import org.opencdmp.filetransformer.rda.support.RdaServiceFactory;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.opencdmp.filetransformer.rda.support.DefinitionBuilder.definition;
import static org.opencdmp.filetransformer.rda.support.PlanBuilder.plan;
import static org.opencdmp.filetransformer.rda.support.ReferenceBuilder.reference;
import static org.opencdmp.filetransformer.rda.support.ReferenceBuilder.referenceType;
import static org.opencdmp.filetransformer.rda.support.ValuesBuilder.values;

/**
 * A reference input without semantics of its own gives keys through its reference type: the type maps the picked
 * reference's id and name, e.g. an organisation's ROR id and name to a creator's affiliation.
 */
class ReferenceSystemFieldSemanticsTest {

    private static final ReferenceTypeModel ORGANISATIONS = referenceType("organisations")
            .referenceIdSemantics("rda.dataset.creator.affiliation.affiliation_id.identifier")
            .labelSemantics("rda.dataset.creator.affiliation.name")
            .build();

    @Test
    void fillsTheAffiliationFromAPickedReference() throws Exception {
        DescriptionTemplateModel template = definition()
                .section("affiliation").sectionDescribes("rda.dataset.creator.affiliation")
                .question("organisation").reference("organisation", ORGANISATIONS, false)
                .toTemplate("Creator affiliation");

        PlanModel plan = plan("Plan with an affiliation")
                .description("Estuary samples", template, values().answer("organisation", a -> a.references("organisation", reference(ORGANISATIONS, "https://ror.org/01c27hj86", "University of Porto"))))
                .build();

        JsonNode affiliation = this.export(plan).path("dmp").path("dataset").get(0).path("creator").get(0).path("affiliation").get(0);

        assertThat(affiliation.path("name").asText()).isEqualTo("University of Porto");
        assertThat(affiliation.path("affiliation_id").path("identifier").asText()).isEqualTo("https://ror.org/01c27hj86");
    }

    @Test
    void joinsTheOtherAnswersOfTheCreator() throws Exception {
        DescriptionTemplateModel template = definition()
                .section("creator").sectionDescribes("rda.dataset.creator")
                .question("name").input("name", FieldType.FREE_TEXT, "rda.dataset.creator.name")
                .question("organisation").reference("organisation", ORGANISATIONS, false)
                .toTemplate("Creator");

        PlanModel plan = plan("Plan with a creator")
                .description("Estuary samples", template, values()
                        .answer("name", a -> a.text("name", "Inês Moreira"))
                        .answer("organisation", a -> a.references("organisation", reference(ORGANISATIONS, "https://ror.org/01c27hj86", "University of Porto"))))
                .build();

        JsonNode creators = this.export(plan).path("dmp").path("dataset").get(0).path("creator");

        assertThat(creators.size()).isEqualTo(1);
        assertThat(creators.get(0).path("name").asText()).isEqualTo("Inês Moreira");
        assertThat(creators.get(0).path("affiliation").get(0).path("name").asText()).isEqualTo("University of Porto");
    }

    @Test
    void anInputWithItsOwnKeyKeepsTheReferenceId() throws Exception {
        DescriptionTemplateModel template = definition()
                .section("creator")
                .question("organisation").reference("organisation", ORGANISATIONS, false, "rda.dataset.creator.affiliation.affiliation_id.identifier")
                .toTemplate("Creator affiliation id only");

        PlanModel plan = plan("Plan with an affiliation id")
                .description("Estuary samples", template, values().answer("organisation", a -> a.references("organisation", reference(ORGANISATIONS, "https://ror.org/01c27hj86", "University of Porto"))))
                .build();

        JsonNode affiliation = this.export(plan).path("dmp").path("dataset").get(0).path("creator").get(0).path("affiliation").get(0);

        assertThat(affiliation.path("affiliation_id").path("identifier").asText()).isEqualTo("https://ror.org/01c27hj86");
        assertThat(affiliation.path("name").asText()).isEqualTo("University of Porto");
    }

    @Test
    void picksAreOneAffiliationEach() throws Exception {
        String affiliationId = "rda.dataset.creator.affiliation.affiliation_id.identifier";
        ReferenceTypeModel researchers = referenceType("researchers").labelSemantics("rda.dataset.creator.affiliation.name")
                .field("firstName", "rda.dataset.creator.affiliation.name").field("lastName").build();
        DescriptionTemplateModel template = definition()
                .section("data")
                .question("owner").questionDescribes("rda.dataset.creator.affiliation")
                .reference("organisations", ORGANISATIONS, true, affiliationId)
                .reference("researchers", researchers, true, affiliationId)
                .toTemplate("Who will own the data");

        PlanModel plan = plan("Plan with owners")
                .description("Estuary samples", template, values().answer("owner", a -> a
                        .references("organisations",
                                reference(ORGANISATIONS, "https://ror.org/01c27hj86", "University of Porto"),
                                reference(ORGANISATIONS, "https://ror.org/03yrm5c26", "California Digital Library"))
                        .references("researchers", reference(researchers, "orcid:0000-0002-1825-0097", "Inês Moreira", "firstName", "Inês", "lastName", "Moreira"))))
                .build();

        JsonNode affiliations = this.export(plan).path("dmp").path("dataset").get(0).path("creator").get(0).path("affiliation");

        assertThat(affiliations.size()).isEqualTo(3);
        assertThat(affiliations.get(0).path("name").asText()).isEqualTo("University of Porto");
        assertThat(affiliations.get(0).path("affiliation_id").path("identifier").asText()).isEqualTo("https://ror.org/01c27hj86");
        assertThat(affiliations.get(0).path("affiliation_id").path("type").asText()).isEqualTo("ror");
        assertThat(affiliations.get(1).path("name").asText()).isEqualTo("California Digital Library");
        assertThat(affiliations.get(2).path("name").asText()).as("the name system field wins over a field carrying the same key").isEqualTo("Inês Moreira");
        assertThat(affiliations.get(2).path("affiliation_id").path("identifier").asText()).isEqualTo("orcid:0000-0002-1825-0097");
    }

    @Test
    void picksAreOneDmpAffiliationEach() throws Exception {
        ReferenceTypeModel organisations = referenceType("organisations").labelSemantics("rda.dmp.affiliation.name").build();
        var blueprint = definition()
                .section("plan")
                .question("affiliations").questionDescribes("rda.dmp.affiliation")
                .reference("organisations", organisations, true, "rda.dmp.affiliation.affiliation_id.identifier")
                .toBlueprint("Affiliations");

        PlanModel plan = plan("Plan with affiliations")
                .blueprint(blueprint, values().answer("affiliations", a -> a.references("organisations",
                        reference(organisations, "https://ror.org/01c27hj86", "University of Porto"),
                        reference(organisations, "https://ror.org/03yrm5c26", "California Digital Library"))))
                .build();

        JsonNode affiliations = this.export(plan).path("dmp").path("affiliation");

        assertThat(affiliations.size()).isEqualTo(2);
        assertThat(affiliations.get(1).path("name").asText()).isEqualTo("California Digital Library");
        assertThat(affiliations.get(1).path("affiliation_id").path("identifier").asText()).isEqualTo("https://ror.org/03yrm5c26");
    }

    @Test
    void picksAreOneContributorEach() throws Exception {
        ReferenceTypeModel grants = referenceType("grants").labelSemantics("rda.dmp.contributor.name").build();
        var blueprint = definition()
                .section("plan")
                .question("contributors").questionDescribes("rda.dmp.contributor")
                .reference("grants", grants, true, "rda.dmp.contributor.contributor_id.identifier")
                .toBlueprint("Contributors");

        PlanModel plan = plan("Plan with grant contributors")
                .blueprint(blueprint, values().answer("contributors", a -> a.references("grants",
                        reference(grants, "13FH019KA2", "133-001 24P"),
                        reference(grants, "2021/B1", "2021/B1 grant"))))
                .build();

        JsonNode contributors = this.export(plan).path("dmp").path("contributor");

        assertThat(contributors.findValues("name").stream().map(JsonNode::asText)).contains("133-001 24P", "2021/B1 grant");
        assertThat(contributors.findValues("identifier").stream().map(JsonNode::asText)).contains("13FH019KA2", "2021/B1");
    }

    @Test
    void picksForAContributorsAffiliationStayOneContributor() throws Exception {
        var blueprint = definition()
                .section("plan")
                .question("contributor").questionDescribes("rda.dmp.contributor")
                .input("name", FieldType.FREE_TEXT, "rda.dmp.contributor.name")
                .reference("organisations", ORGANISATIONS, true, "rda.dmp.contributor.affiliation.affiliation_id.identifier")
                .toBlueprint("Contributor");

        PlanModel plan = plan("Plan with a contributor")
                .blueprint(blueprint, values().answer("contributor", a -> a.text("name", "Inês Moreira").references("organisations",
                        reference(ORGANISATIONS, "https://ror.org/01c27hj86", "University of Porto"),
                        reference(ORGANISATIONS, "https://ror.org/03yrm5c26", "California Digital Library"))))
                .build();

        JsonNode contributors = this.export(plan).path("dmp").path("contributor");
        JsonNode ines = null;
        for (JsonNode contributor : contributors) if ("Inês Moreira".equals(contributor.path("name").asText())) ines = contributor;

        assertThat(contributors.findValues("name").stream().map(JsonNode::asText).filter("Inês Moreira"::equals).count()).isEqualTo(1);
        assertThat(ines).isNotNull();
        assertThat(ines.path("affiliation").size()).isEqualTo(2);
    }

    @Test
    void picksAreOneCreatorEach() throws Exception {
        ReferenceTypeModel researchers = referenceType("researchers").labelSemantics("rda.dataset.creator.name").build();
        DescriptionTemplateModel template = definition()
                .section("data")
                .question("creators").questionDescribes("rda.dataset.creator")
                .reference("researchers", researchers, true, "rda.dataset.creator.creator_id.identifier")
                .toTemplate("Creators");

        PlanModel plan = plan("Plan with creators")
                .description("Estuary samples", template, values().answer("creators", a -> a.references("researchers",
                        reference(researchers, "https://orcid.org/0000-0002-1825-0097", "Inês Moreira"),
                        reference(researchers, "https://orcid.org/0000-0001-5109-3700", "João Silva"))))
                .build();

        JsonNode creators = this.export(plan).path("dmp").path("dataset").get(0).path("creator");

        assertThat(creators.size()).isEqualTo(2);
        assertThat(creators.get(0).path("name").asText()).isEqualTo("Inês Moreira");
        assertThat(creators.get(0).path("creator_id").get(0).path("identifier").asText()).isEqualTo("https://orcid.org/0000-0002-1825-0097");
        assertThat(creators.get(1).path("name").asText()).isEqualTo("João Silva");
    }

    @Test
    void aFunderAndItsGrantsMakeOneFundingPerGrant() throws Exception {
        ReferenceTypeModel funders = referenceType("funders").build();
        ReferenceTypeModel grants = referenceType("grants").build();
        var blueprint = definition()
                .section("plan")
                .question("project").questionDescribes("rda.dmp.project")
                .input("title", FieldType.FREE_TEXT, "rda.dmp.project.title")
                .reference("funder", funders, false, "rda.dmp.project.funding.funder_id.identifier")
                .reference("grants", grants, true, "rda.dmp.project.funding.grant_id.identifier")
                .toBlueprint("Project");

        PlanModel plan = plan("Plan with a funded project")
                .blueprint(blueprint, values().answer("project", a -> a.text("title", "Estuary monitoring")
                        .references("funder", reference(funders, "501100001871", "Fundação para a Ciência e a Tecnologia"))
                        .references("grants", reference(grants, "PTDC/CTA-AMB/0001/2020", "Grant one"), reference(grants, "PTDC/CTA-AMB/0002/2021", "Grant two"))))
                .build();

        JsonNode projects = this.export(plan).path("dmp").path("project");

        assertThat(projects.size()).isEqualTo(1);
        JsonNode fundings = projects.get(0).path("funding");
        assertThat(fundings.size()).isEqualTo(2);
        assertThat(fundings.get(0).path("funder_id").path("identifier").asText()).isEqualTo("501100001871");
        assertThat(fundings.get(0).path("grant_id").path("identifier").asText()).isEqualTo("PTDC/CTA-AMB/0001/2020");
        assertThat(fundings.get(1).path("funder_id").path("identifier").asText()).isEqualTo("501100001871");
        assertThat(fundings.get(1).path("grant_id").path("identifier").asText()).isEqualTo("PTDC/CTA-AMB/0002/2021");
    }

    private JsonNode export(PlanModel plan) throws Exception {
        byte[] file = RdaServiceFactory.create().exportPlan(plan, null).getFile();
        return new ObjectMapper().readTree(new String(file, StandardCharsets.UTF_8));
    }
}
