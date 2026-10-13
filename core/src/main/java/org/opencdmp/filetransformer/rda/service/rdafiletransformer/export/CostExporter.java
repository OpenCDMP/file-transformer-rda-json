package org.opencdmp.filetransformer.rda.service.rdafiletransformer.export;

import org.opencdmp.commonmodels.models.plandescriptionfields.PropertyDefinitionFieldSetItemModel;
import org.opencdmp.commonmodels.enums.*;
import org.opencdmp.commonmodels.models.*;
import org.opencdmp.commonmodels.models.description.*;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.FieldSetModel;
import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.filetransformer.rda.model.rda.*;
import org.opencdmp.filetransformerbase.models.misc.*;
import java.time.*;
import java.util.*;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.AnswerGroup;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaMapperContext;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaMapperSupport;
import static org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaSemantics.*;

/** Builds the dmp {@code cost} list, from the blueprint and from each asset template. */
public class CostExporter extends RdaMapperSupport {

    public CostExporter(RdaMapperContext context) {
        super(context);
    }

    List<Cost> buildRdaCosts(PlanModel model){
        List<Cost> values = new ArrayList<>();
        for (AnswerGroup answers : this.groups(model.getPlanBlueprint(), model.getProperties().getFieldSets(), List.of(SEMANTIC_COST_CURRENCY_CODE, SEMANTIC_COST_VALUE, SEMANTIC_COST_DESCRIPTION, SEMANTIC_COST_TITLE))) {
            boolean valueFound = false;
            Cost item = new Cost();

            String fieldValue = answers.value(SEMANTIC_COST_CURRENCY_CODE);
            if (fieldValue != null) {
                try {
                    item.setCurrencyCode(Cost.CurrencyCode.fromValue(fieldValue));
                    valueFound = true;
                } catch (Exception e) {
                    logger.warn("invalid cost currency cost value: " + fieldValue);
                }
            }

            fieldValue = answers.value(SEMANTIC_COST_DESCRIPTION);
            if (fieldValue != null) {
                item.setDescription(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_COST_TITLE);
            if (fieldValue != null) {
                item.setTitle(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_COST_VALUE);
            if (fieldValue != null) {
                try {
                    item.setValue(Double.valueOf(fieldValue));
                    valueFound = true;
                } catch (Exception ex) {
                    logger.warn("invalid cost value: " + fieldValue);
                }
            }

            if (valueFound) values.add(item);
        }
        return values;
    }

    List<Cost> buildRdaCosts(DescriptionModel model){
        List<Cost> values = new ArrayList<>();
        for (AnswerGroup answers : this.groups(model.getDescriptionTemplate(), model.getProperties().getFieldSets(), List.of(SEMANTIC_COST_CURRENCY_CODE, SEMANTIC_COST_VALUE, SEMANTIC_COST_DESCRIPTION, SEMANTIC_COST_TITLE))) {
            boolean valueFound = false;
            Cost item = new Cost();

            String fieldValue = answers.value(SEMANTIC_COST_CURRENCY_CODE);
            if (fieldValue != null) {
                try {
                    item.setCurrencyCode(Cost.CurrencyCode.fromValue(fieldValue));
                    valueFound = true;
                } catch (Exception e) {
                    logger.warn("invalid cost currency cost value: " + fieldValue);
                }
            }

            fieldValue = answers.value(SEMANTIC_COST_DESCRIPTION);
            if (fieldValue != null) {
                item.setDescription(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_COST_TITLE);
            if (fieldValue != null) {
                item.setTitle(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_COST_VALUE);
            if (fieldValue != null) {
                try {
                    item.setValue(Double.valueOf(fieldValue));
                    valueFound = true;
                } catch (Exception ex) {
                    logger.warn("invalid cost value: " + fieldValue);
                }
            }

            if (valueFound) values.add(item);
        }
        return values;
    }
}
