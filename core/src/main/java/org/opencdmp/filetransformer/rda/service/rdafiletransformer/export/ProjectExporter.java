package org.opencdmp.filetransformer.rda.service.rdafiletransformer.export;

import org.opencdmp.commonmodels.models.plandescriptionfields.PropertyDefinitionFieldSetItemModel;
import gr.cite.tools.exception.MyApplicationException;
import org.opencdmp.commonmodels.enums.*;
import org.opencdmp.commonmodels.models.*;
import org.opencdmp.commonmodels.models.description.*;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.FieldSetModel;
import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.commonmodels.models.reference.ReferenceFieldModel;
import org.opencdmp.commonmodels.models.reference.ReferenceModel;
import org.opencdmp.filetransformer.rda.model.rda.*;
import org.opencdmp.filetransformerbase.models.misc.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.AnswerGroup;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaMapperContext;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaMapperSupport;
import static org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaSemantics.*;

/** Builds the dmp {@code project} list and its {@code funding}, from plan references and from questions. */
public class ProjectExporter extends RdaMapperSupport {

    public ProjectExporter(RdaMapperContext context) {
        super(context);
    }

    List<Project> buildProject(PlanModel model){
        List<Project> values = new ArrayList<>();

        for (AnswerGroup answers : this.groups(model.getPlanBlueprint(), model.getProperties().getFieldSets(), List.of(SEMANTIC_PROJECT_FUNDING_FUNDER_ID_IDENTIFIER, SEMANTIC_PROJECT_FUNDING_FUNDER_ID, SEMANTIC_PROJECT_FUNDING_FUNDER_ID_TYPE, SEMANTIC_PROJECT_FUNDING_GRANT_ID,
                SEMANTIC_PROJECT_FUNDING_GRANT_ID_IDENTIFIER, SEMANTIC_PROJECT_FUNDING_GRANT_ID_TYPE, SEMANTIC_PROJECT_FUNDING_FUNDING_STATUS, SEMANTIC_PROJECT_START, SEMANTIC_PROJECT_END, SEMANTIC_PROJECT_TITLE, SEMANTIC_PROJECT_DESCRIPTION,
                SEMANTIC_PROJECT_ID_IDENTIFIER, SEMANTIC_PROJECT_ID_TYPE))) {
            boolean valueFound = false;
            Project project = new Project();

            String fieldValue = answers.value(SEMANTIC_PROJECT_TITLE);
            if (fieldValue != null) {
                project.setTitle(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_PROJECT_DESCRIPTION);
            if (fieldValue != null) {
                project.setDescription(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_PROJECT_START);
            if (fieldValue != null) {
                String formatedValue = this.formatTextAsDate(fieldValue);
                if (formatedValue != null) {
                    project.setStart(formatedValue);
                    valueFound = true;
                }
            }

            fieldValue = answers.value(SEMANTIC_PROJECT_END);
            if (fieldValue != null) {
                String formatedValue = this.formatTextAsDate(fieldValue);
                if (formatedValue != null) {
                    project.setEnd(formatedValue);
                    valueFound = true;
                }
            }

            List<Funding> fundings = this.buildRDAFunding(answers);
            if (!fundings.isEmpty()) {
                project.setFunding(fundings);
                valueFound = true;
            }

            List<ProjectId> projectIds = this.buildRDAProjectId(answers);
            if (!projectIds.isEmpty()) {
                project.setProjectId(projectIds);
                valueFound = true;
            }

            if (valueFound) values.add(project);
        }
        return values;
    }

    public Project buildRdaProject(ReferenceModel project, List<Funding> fundings) {
        if (project == null) throw new MyApplicationException("Project is missing");
        if (project.getLabel() == null || project.getLabel().isBlank()) throw new MyApplicationException("Project Title is missing");
        Project rda = new Project();
        rda.setTitle(project.getLabel());
        rda.setDescription(project.getDescription());
        if (project.getDefinition() != null && project.getDefinition().getFields() != null) {
	        project.getDefinition().getFields().stream().filter(field -> this.configuration.getRdaFileTransformerServiceProperties().getProjectStartDateCode().equalsIgnoreCase(field.getCode()) && field.getValue() != null && !field.getValue().isBlank()).map(ReferenceFieldModel::getValue).findFirst().ifPresent(rda::setStart);
	        project.getDefinition().getFields().stream().filter(field -> this.configuration.getRdaFileTransformerServiceProperties().getProjectEndDateCode().equalsIgnoreCase(field.getCode()) && field.getValue() != null && !field.getValue().isBlank()).map(ReferenceFieldModel::getValue).findFirst().ifPresent(rda::setEnd);

            if (rda.getStart() != null && !rda.getStart().isBlank()) {
                try {
                    LocalDateTime ldt = LocalDateTime.ofInstant(Instant.parse(rda.getStart()), ZoneOffset.UTC);
                    rda.setStart(ldt.format(DateTimeFormatter.ISO_LOCAL_DATE));
                } catch (Exception e) {
                    logger.warn("invalid rda project parse date for value: " + rda.getStart());
                }
            }

            if (rda.getEnd() != null && !rda.getEnd().isBlank()) {
                try {
                    LocalDateTime ldt = LocalDateTime.ofInstant(Instant.parse(rda.getEnd()), ZoneOffset.UTC);
                    rda.setEnd(ldt.format(DateTimeFormatter.ISO_LOCAL_DATE));
                } catch (Exception e) {
                    logger.warn("invalid rda project parse date for value: " + rda.getEnd());
                }
            }

        }
        if (!fundings.isEmpty()) rda.setFunding(new ArrayList<>(fundings));

        return rda;
    }

    /**
     * The plan's projects, funded by its funders and grants. Without a project reference each grant stands for its
     * project. Plan references carry no project hierarchy, so the fundings go only where they cannot belong to
     * another project: to the plan's only project, or to the project a grant stands for.
     */
    public List<Project> buildRdaProjects(List<ReferenceModel> projects, List<ReferenceModel> grants, List<ReferenceModel> funders) {
        List<Project> rdaModels = new ArrayList<>();
        if (!projects.isEmpty()) {
            List<Funding> fundings = projects.size() == 1 ? this.buildRdaFundings(grants, funders) : List.of();
            for (ReferenceModel project : projects) rdaModels.add(this.buildRdaProject(project, fundings));
        } else if (funders.size() == 1) {
            for (ReferenceModel grant : grants) rdaModels.add(this.buildRdaProject(grant, List.of(this.buildRRdaFunding(funders.getFirst(), grant))));
        }
        return rdaModels;
    }

    /** Each grant under the plan's funder; with several funders, which grant is whose is unknown, so each funder is funding on its own. */
    List<Funding> buildRdaFundings(List<ReferenceModel> grants, List<ReferenceModel> funders) {
        List<Funding> fundings = new ArrayList<>();
        if (funders.size() == 1 && !grants.isEmpty()) {
            for (ReferenceModel grant : grants) fundings.add(this.buildRRdaFunding(funders.getFirst(), grant));
        } else {
            for (ReferenceModel funder : funders) fundings.add(this.buildRRdaFunding(funder, null));
        }
        return fundings;
    }

    public Funding buildRRdaFunding(ReferenceModel funder, ReferenceModel grant) {
        if (grant == null && funder == null) return null;
        Funding rda = new Funding();
        if (funder != null && funder.getReference() != null) {
            FunderId funderId = new FunderId();
            funderId.setIdentifier(funder.getReference());
            funderId.setType(DMP_TYPE_FUNDREF);
            rda.setFunderId(funderId);
        } else if (funder != null){
            FunderId funderId = new FunderId();
            funderId.setIdentifier(funder.getId().toString());
            funderId.setType(DMP_TYPE_OTHER);
            rda.setFunderId(funderId);
        }
        if (grant != null && grant.getReference() != null) {
            GrantId grantId = new GrantId();
            grantId.setIdentifier(grant.getReference());
            grantId.setType(DMP_TYPE_OTHER);
            rda.setGrantId(grantId);
        } else if (grant != null) {
            GrantId grantId = new GrantId();
            grantId.setIdentifier(grant.getId().toString());
            grantId.setType(DMP_TYPE_OTHER);
            rda.setGrantId(grantId);
        }
        return rda;
    }

    List<Project> buildProject(DescriptionModel model, List<Project> values){
        if (values == null) values = new ArrayList<>();

        for (AnswerGroup answers : this.groups(model.getDescriptionTemplate(), model.getProperties().getFieldSets(), List.of(SEMANTIC_PROJECT_FUNDING_FUNDER_ID_IDENTIFIER, SEMANTIC_PROJECT_FUNDING_FUNDER_ID, SEMANTIC_PROJECT_FUNDING_FUNDER_ID_TYPE, SEMANTIC_PROJECT_FUNDING_GRANT_ID,
                SEMANTIC_PROJECT_FUNDING_GRANT_ID_IDENTIFIER, SEMANTIC_PROJECT_FUNDING_GRANT_ID_TYPE, SEMANTIC_PROJECT_FUNDING_FUNDING_STATUS, SEMANTIC_PROJECT_START, SEMANTIC_PROJECT_END, SEMANTIC_PROJECT_TITLE, SEMANTIC_PROJECT_DESCRIPTION,
                SEMANTIC_PROJECT_ID_IDENTIFIER, SEMANTIC_PROJECT_ID_TYPE))) {
            boolean valueFound = false;
            Project project = new Project();

            String fieldValue = answers.value(SEMANTIC_PROJECT_TITLE);
            if (fieldValue != null) {
                project.setTitle(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_PROJECT_DESCRIPTION);
            if (fieldValue != null) {
                project.setDescription(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_PROJECT_START);
            if (fieldValue != null) {
                String formatedValue = this.formatTextAsDate(fieldValue);
                if (formatedValue != null) {
                    project.setStart(formatedValue);
                    valueFound = true;
                }
            }

            fieldValue = answers.value(SEMANTIC_PROJECT_END);
            if (fieldValue != null) {
                String formatedValue = this.formatTextAsDate(fieldValue);
                if (formatedValue != null) {
                    project.setEnd(formatedValue);
                    valueFound = true;
                }
            }

            List<Funding> fundings = this.buildRDAFunding(answers);
            if (!fundings.isEmpty()) {
                project.setFunding(fundings);
                valueFound = true;
            }

            List<ProjectId> projectIds = this.buildRDAProjectId(answers);
            if (!projectIds.isEmpty()) {
                project.setProjectId(projectIds);
                valueFound = true;
            }

            if (valueFound && values.stream().noneMatch(x -> sameProject(x, project))) values.add(project);
        }
        return values;
    }

    List<ProjectId> buildRDAProjectId(AnswerGroup answers){
        List<ProjectId> projectIds = new ArrayList<>();

        boolean valueFound = false;
        ProjectId projectId = new ProjectId();

        String fieldValue = answers.value(SEMANTIC_PROJECT_ID_IDENTIFIER);
        if (fieldValue != null) {
                projectId.setIdentifier(fieldValue);
                valueFound = true;
        }

        fieldValue = answers.value(SEMANTIC_PROJECT_ID_TYPE);
        if (fieldValue != null) {
                projectId.setType(fieldValue);
                valueFound = true;

        }

        if (valueFound) projectIds.add(projectId);

        return projectIds;
    }

    List<Funding> buildRDAFunding(AnswerGroup answers){
        List<Funding> fundings = new ArrayList<>();

        boolean valueFound = false;
        Funding funding = new Funding();

        String fieldValue = answers.value(SEMANTIC_PROJECT_FUNDING_FUNDER_ID);
        if (fieldValue != null && !fieldValue.isBlank()) {
            FunderId funderId = new FunderId();
            funderId.setIdentifier(fieldValue);
            funderId.setType(DMP_TYPE_OTHER);
            funding.setFunderId(funderId);
            valueFound = true;
        }

        fieldValue = answers.value(SEMANTIC_PROJECT_FUNDING_FUNDER_ID_IDENTIFIER);
        if (fieldValue != null) {
            FunderId funderId = new FunderId();
            funderId.setIdentifier(fieldValue);
            funderId.setType(DMP_TYPE_FUNDREF);
            funding.setFunderId(funderId);
            valueFound = true;
        }

        fieldValue = answers.value(SEMANTIC_PROJECT_FUNDING_FUNDER_ID_TYPE);
        if (fieldValue != null) {
            if (funding.getFunderId() != null) {
                funding.getFunderId().setType(fieldValue);
                valueFound = true;
            } else {
                logger.warn("funding funder id type without an identifier: " + fieldValue);
            }
        }

        fieldValue = answers.value(SEMANTIC_PROJECT_FUNDING_GRANT_ID);
        if (fieldValue != null) {
            GrantId grantId = new GrantId();
            grantId.setIdentifier(fieldValue);
            grantId.setType(DMP_TYPE_OTHER);
            funding.setGrantId(grantId);
            valueFound = true;
        }

        fieldValue = answers.value(SEMANTIC_PROJECT_FUNDING_GRANT_ID_IDENTIFIER);
        if (fieldValue != null) {
            GrantId grantId = new GrantId();
            grantId.setIdentifier(fieldValue);
            grantId.setType(DMP_TYPE_OTHER);
            funding.setGrantId(grantId);
            valueFound = true;
        }

        fieldValue = answers.value(SEMANTIC_PROJECT_FUNDING_GRANT_ID_TYPE);
        if (fieldValue != null) {
            if (funding.getGrantId() != null) {
                funding.getGrantId().setType(fieldValue);
                valueFound = true;
            } else {
                logger.warn("funding grant id type without an identifier: " + fieldValue);
            }
        }


        fieldValue = answers.value(SEMANTIC_PROJECT_FUNDING_FUNDING_STATUS);
        if (fieldValue != null) {
            try {
                Funding.FundingStatus value = Funding.FundingStatus.fromValue(fieldValue);
                funding.setFundingStatus(value);
                valueFound = true;
            } catch (Exception e) {
                logger.warn("invalid Funding Status value: " + fieldValue);
            }
        }

        if (valueFound) fundings.add(funding);

        return fundings;
    }

    /** Adds the projects not already listed (plan references come first). */
    void addDistinct(List<Project> projects, List<Project> more) {
        for (Project project : more) {
            if (projects.stream().noneMatch(x -> sameProject(x, project))) projects.add(project);
        }
    }

    /**
     * Two projects are the same when they share a project identifier, or have the same title and the same grants;
     * two projects that only share a grant stay apart.
     */
    private static boolean sameProject(Project a, Project b) {
        Set<String> idsA = projectIds(a), idsB = projectIds(b);
        if (!idsA.isEmpty() && !idsB.isEmpty() && idsA.stream().anyMatch(idsB::contains)) return true;
        return a.getTitle() != null && b.getTitle() != null && a.getTitle().trim().equalsIgnoreCase(b.getTitle().trim())
                && grantIds(a).equals(grantIds(b));
    }

    private static Set<String> projectIds(Project project) {
        if (project.getProjectId() == null) return Set.of();
        return project.getProjectId().stream().map(ProjectId::getIdentifier).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private static Set<String> grantIds(Project project) {
        if (project.getFunding() == null) return Set.of();
        return project.getFunding().stream().filter(x -> x.getGrantId() != null && x.getGrantId().getIdentifier() != null)
                .map(x -> x.getGrantId().getIdentifier()).collect(Collectors.toSet());
    }
}
