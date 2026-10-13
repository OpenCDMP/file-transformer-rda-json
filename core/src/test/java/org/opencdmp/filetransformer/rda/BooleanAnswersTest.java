package org.opencdmp.filetransformer.rda;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.opencdmp.commonmodels.enums.FieldType;
import org.opencdmp.commonmodels.models.descriptiotemplate.DescriptionTemplateModel;
import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.filetransformer.rda.support.RdaServiceFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.opencdmp.filetransformer.rda.support.DefinitionBuilder.definition;
import static org.opencdmp.filetransformer.rda.support.PlanBuilder.plan;
import static org.opencdmp.filetransformer.rda.support.ValuesBuilder.values;

/**
 * RDA booleans (is_reused) take a Yes/No answer in any case, from a text, radio or select input, as well as true/false.
 */
class BooleanAnswersTest {

    @Test
    void readsYesAndNoInAnyCase() throws Exception {
        DescriptionTemplateModel text = definition()
                .section("general").question("reused").input("reused", FieldType.FREE_TEXT, "rda.dataset.is_reused")
                .toTemplate("Reused as text");
        DescriptionTemplateModel radio = definition()
                .section("general").question("reused").radio("reused", List.of("Yes", "Yes", "No", "No"), "rda.dataset.is_reused")
                .toTemplate("Reused as radio");
        DescriptionTemplateModel select = definition()
                .section("general").question("reused").select("reused", List.of("yes", "Yes", "no", "No"), "rda.dataset.is_reused")
                .toTemplate("Reused as select");

        PlanModel plan = plan("Plan with Yes/No answers")
                .description("Text YES", text, values().answer("reused", a -> a.text("reused", " YES ")))
                .description("Text no", text, values().answer("reused", a -> a.text("reused", "no")))
                .description("Radio No", radio, values().answer("reused", a -> a.text("reused", "No")))
                .description("Select yes", select, values().answer("reused", a -> a.list("reused", "yes")))
                .description("Text True", text, values().answer("reused", a -> a.text("reused", "True")))
                .description("Text maybe", text, values().answer("reused", a -> a.text("reused", "maybe")))
                .build();

        JsonNode datasets = this.export(plan).path("dmp").path("dataset");

        assertThat(datasets.get(0).path("is_reused").asText()).isEqualTo("true");
        assertThat(datasets.get(1).path("is_reused").asText()).isEqualTo("false");
        assertThat(datasets.get(2).path("is_reused").asText()).isEqualTo("false");
        assertThat(datasets.get(3).path("is_reused").asText()).isEqualTo("true");
        assertThat(datasets.get(4).path("is_reused").asText()).isEqualTo("true");
        assertThat(datasets.get(5).has("is_reused")).isFalse();
    }

    private JsonNode export(PlanModel plan) throws Exception {
        byte[] file = RdaServiceFactory.create().exportPlan(plan, null).getFile();
        return new ObjectMapper().readTree(new String(file, StandardCharsets.UTF_8));
    }
}
