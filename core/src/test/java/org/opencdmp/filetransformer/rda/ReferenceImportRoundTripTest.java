package org.opencdmp.filetransformer.rda;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.opencdmp.commonmodels.enums.FieldType;
import org.opencdmp.commonmodels.models.FileEnvelopeModel;
import org.opencdmp.commonmodels.models.description.DescriptionModel;
import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.commonmodels.models.referencetype.ReferenceTypeModel;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaFileTransformerService;
import org.opencdmp.filetransformer.rda.support.RdaServiceFactory;
import org.opencdmp.filetransformerbase.models.misc.DescriptionImportModel;
import org.opencdmp.filetransformerbase.models.misc.PlanImportModel;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.opencdmp.filetransformer.rda.support.DefinitionBuilder.definition;
import static org.opencdmp.filetransformer.rda.support.PlanBuilder.plan;
import static org.opencdmp.filetransformer.rda.support.ReferenceBuilder.reference;
import static org.opencdmp.filetransformer.rda.support.ReferenceBuilder.referenceType;
import static org.opencdmp.filetransformer.rda.support.ValuesBuilder.values;

/** References picked for RDA list objects come back from an import as the same picks, so a re-export gives the same objects. */
class ReferenceImportRoundTripTest {

    private static final ReferenceTypeModel ORGANISATIONS = referenceType("organisations")
            .referenceIdSemantics("rda.dataset.creator.affiliation.affiliation_id.identifier")
            .labelSemantics("rda.dataset.creator.affiliation.name")
            .build();

    @Test
    void creatorAffiliationsPickedTogether() throws Exception {
        Supplier<PlanModel> plan = () -> {
            var template = definition()
                    .section("creator").sectionDescribes("rda.dataset.creator")
                    .question("creator").input("name", FieldType.FREE_TEXT, "rda.dataset.creator.name")
                    .reference("organisations", ORGANISATIONS, true)
                    .toTemplate("Creator");
            return plan("Plan with affiliations")
                    .description("Estuary samples", template, values().answer("creator", a -> a.text("name", "Inês Moreira")
                            .references("organisations", reference(ORGANISATIONS, "https://ror.org/01c27hj86", "University of Porto"),
                                    reference(ORGANISATIONS, "https://ror.org/043pwc612", "University of Minho"))))
                    .build();
        };

        JsonNode[] exports = this.roundTrip(plan);

        JsonNode affiliations = exports[0].path("dmp").path("dataset").get(0).path("creator").get(0).path("affiliation");
        assertThat(affiliations.size()).isEqualTo(2);
        assertThat(exports[1].path("dmp").path("dataset").get(0).path("creator")).isEqualTo(exports[0].path("dmp").path("dataset").get(0).path("creator"));
    }

    @Test
    void creatorsPickedThroughTheirTypeOnly() throws Exception {
        ReferenceTypeModel researchers = referenceType("researchers")
                .referenceIdSemantics("rda.dataset.creator.creator_id.identifier")
                .labelSemantics("rda.dataset.creator.name")
                .build();
        Supplier<PlanModel> plan = () -> {
            var template = definition()
                    .section("data")
                    .question("creators").questionDescribes("rda.dataset.creator")
                    .reference("researchers", researchers, true)
                    .toTemplate("Creators");
            return plan("Plan with creators")
                    .description("Estuary samples", template, values().answer("creators", a -> a.references("researchers",
                            reference(researchers, "https://orcid.org/0000-0002-1825-0097", "Inês Moreira"),
                            reference(researchers, "https://orcid.org/0000-0001-5109-3700", "João Silva"))))
                    .build();
        };

        JsonNode[] exports = this.roundTrip(plan);

        assertThat(exports[0].path("dmp").path("dataset").get(0).path("creator").size()).isEqualTo(2);
        assertThat(exports[1].path("dmp").path("dataset").get(0).path("creator")).isEqualTo(exports[0].path("dmp").path("dataset").get(0).path("creator"));
    }

    @Test
    void grantsPickedAsContributors() throws Exception {
        ReferenceTypeModel grants = referenceType("grants")
                .referenceIdSemantics("rda.dmp.contributor.contributor_id.identifier")
                .labelSemantics("rda.dmp.contributor.name")
                .build();
        Supplier<PlanModel> plan = () -> {
            var blueprint = definition()
                    .section("plan")
                    .question("contributors").questionDescribes("rda.dmp.contributor")
                    .reference("grants", grants, true)
                    .toBlueprint("Contributors");
            return plan("Plan with grant contributors")
                    .blueprint(blueprint, values().answer("contributors", a -> a.references("grants",
                            reference(grants, "PTDC/CTA-AMB/0001/2020", "Grant one"),
                            reference(grants, "PTDC/CTA-AMB/0002/2021", "Grant two"))))
                    .build();
        };

        JsonNode[] exports = this.roundTrip(plan);

        // the plan's owner is a contributor too
        assertThat(exports[0].path("dmp").path("contributor").findValuesAsText("name")).contains("Grant one", "Grant two");
        assertThat(exports[1].path("dmp").path("contributor")).isEqualTo(exports[0].path("dmp").path("contributor"));
    }

    @Test
    void aFunderAndItsGrants() throws Exception {
        ReferenceTypeModel funders = referenceType("funders").build();
        ReferenceTypeModel grants = referenceType("grants").build();
        Supplier<PlanModel> plan = () -> {
            var blueprint = definition()
                    .section("plan")
                    .question("project").questionDescribes("rda.dmp.project")
                    .input("title", FieldType.FREE_TEXT, "rda.dmp.project.title")
                    .reference("funder", funders, false, "rda.dmp.project.funding.funder_id.identifier")
                    .reference("grants", grants, true, "rda.dmp.project.funding.grant_id.identifier")
                    .toBlueprint("Project");
            return plan("Plan with a funded project")
                    .blueprint(blueprint, values().answer("project", a -> a.text("title", "Estuary monitoring")
                            .references("funder", reference(funders, "501100001871", "Fundação para a Ciência e a Tecnologia"))
                            .references("grants", reference(grants, "PTDC/CTA-AMB/0001/2020", "Grant one"), reference(grants, "PTDC/CTA-AMB/0002/2021", "Grant two"))))
                    .build();
        };

        JsonNode[] exports = this.roundTrip(plan);

        assertThat(exports[0].path("dmp").path("project").get(0).path("funding").size()).isEqualTo(2);
        assertThat(exports[1].path("dmp").path("project")).isEqualTo(exports[0].path("dmp").path("project"));
    }

    /** The export of the plan, and the export of the plan with the answers its import gives. */
    private JsonNode[] roundTrip(Supplier<PlanModel> plan) throws Exception {
        RdaFileTransformerService service = RdaServiceFactory.create();
        PlanModel original = plan.get();
        byte[] first = service.exportPlan(original, null).getFile();
        PlanModel imported = service.importPlan(importModel(original, first));

        PlanModel reloaded = plan.get();
        reloaded.getProperties().setFieldSets(imported.getProperties().getFieldSets());
        for (int i = 0; i < reloaded.getDescriptions().size(); i++) reloaded.getDescriptions().get(i).setProperties(imported.getDescriptions().get(i).getProperties());
        byte[] second = service.exportPlan(reloaded, null).getFile();

        ObjectMapper mapper = new ObjectMapper();
        return new JsonNode[]{mapper.readTree(new String(first, StandardCharsets.UTF_8)), mapper.readTree(new String(second, StandardCharsets.UTF_8))};
    }

    private static PlanImportModel importModel(PlanModel plan, byte[] rdaJson) {
        FileEnvelopeModel file = new FileEnvelopeModel();
        file.setFile(rdaJson);
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
}
