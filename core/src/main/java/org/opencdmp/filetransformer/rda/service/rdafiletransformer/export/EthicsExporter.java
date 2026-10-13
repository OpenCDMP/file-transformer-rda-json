package org.opencdmp.filetransformer.rda.service.rdafiletransformer.export;

import org.opencdmp.commonmodels.models.plandescriptionfields.PropertyDefinitionFieldSetItemModel;
import gr.cite.tools.exception.MyApplicationException;
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

/** Builds the dmp ethical issues properties, from the blueprint and from each asset template. */
public class EthicsExporter extends RdaMapperSupport {

    public EthicsExporter(RdaMapperContext context) {
        super(context);
    }

    public Dmp.EthicalIssuesExist buildRdaEthicalIssuesExist(PlanModel model) {
        if (model == null) throw new MyApplicationException("Plan is missing");
        for (AnswerGroup answers : this.groups(model.getPlanBlueprint(), model.getProperties().getFieldSets(), List.of(SEMANTIC_ETHICAL_ISSUES_EXISTS))) {

            String fieldValue = answers.value(SEMANTIC_ETHICAL_ISSUES_EXISTS);
            if (fieldValue != null) {
                try{
                    return Dmp.EthicalIssuesExist.fromValue(fieldValue);
                }catch (Exception e){
                    logger.warn("invalid rda ethical issue exist for value: " + fieldValue);
                }
            }
        }

        return Dmp.EthicalIssuesExist.UNKNOWN;
    }

    public String buildRdaEthicalIssueReport(PlanModel model) {
        if (model == null) throw new MyApplicationException("Plan is missing");
        for (AnswerGroup answers : this.groups(model.getPlanBlueprint(), model.getProperties().getFieldSets(), List.of(SEMANTIC_ETHICAL_ISSUES_REPORT))) {

            String fieldValue = answers.value(SEMANTIC_ETHICAL_ISSUES_REPORT);
            if (fieldValue != null) {
                return fieldValue;
            }
        }
        return null;
    }

    public String buildRdaEthicalIssuesDescription(PlanModel model) {
        if (model == null) throw new MyApplicationException("Plan is missing");
        for (AnswerGroup answers : this.groups(model.getPlanBlueprint(), model.getProperties().getFieldSets(), List.of(SEMANTIC_ETHICAL_ISSUES_DESCRIPTION))) {

            String fieldValue = answers.value(SEMANTIC_ETHICAL_ISSUES_DESCRIPTION);
            if (fieldValue != null) {
                return fieldValue;
            }
        }
        return null;
    }

    void mergeEthicalIssues(DescriptionModel model, Dmp dmpRda){
        for (AnswerGroup answers : this.groups(model.getDescriptionTemplate(), model.getProperties().getFieldSets(), List.of(SEMANTIC_ETHICAL_ISSUES_EXISTS ))) {

            String fieldValue = answers.value(SEMANTIC_ETHICAL_ISSUES_EXISTS);
            try {
                Dmp.EthicalIssuesExist value = Dmp.EthicalIssuesExist.fromValue(fieldValue);
                if (!Dmp.EthicalIssuesExist.UNKNOWN.equals(value)) dmpRda.setEthicalIssuesExist(value);
            } catch (Exception e) {
                logger.warn("invalid dmp ethical issues exist value: " + fieldValue);
            }
        }
        for (AnswerGroup answers : this.groups(model.getDescriptionTemplate(), model.getProperties().getFieldSets(), List.of(SEMANTIC_ETHICAL_ISSUES_EXISTS, SEMANTIC_ETHICAL_ISSUES_DESCRIPTION, SEMANTIC_ETHICAL_ISSUES_REPORT))) {

            String fieldValue = answers.value(SEMANTIC_ETHICAL_ISSUES_EXISTS);
            if (fieldValue != null) {
                try {
                    Dmp.EthicalIssuesExist value = Dmp.EthicalIssuesExist.fromValue(fieldValue);
                    if (!Dmp.EthicalIssuesExist.UNKNOWN.equals(value)) dmpRda.setEthicalIssuesExist(value);
                } catch (Exception e) {
                    logger.warn("invalid dmp ethical issues exist value: " + fieldValue);
                }
            }

            if (dmpRda.getEthicalIssuesDescription() == null || dmpRda.getEthicalIssuesDescription().isBlank()) {
                fieldValue = answers.value(SEMANTIC_ETHICAL_ISSUES_DESCRIPTION);
                if (fieldValue != null) {
                    dmpRda.setEthicalIssuesDescription(fieldValue);
                }
            }

            if (dmpRda.getEthicalIssuesReport() == null) {
                fieldValue = answers.value(SEMANTIC_ETHICAL_ISSUES_REPORT);
                if (fieldValue != null) {
                    try {
                        dmpRda.setEthicalIssuesReport(fieldValue);
                    } catch (Exception ex) {
                        logger.warn("invalid dmp ethical issues report value: " + fieldValue);
                    }
                }
            }
        }
    }
}
