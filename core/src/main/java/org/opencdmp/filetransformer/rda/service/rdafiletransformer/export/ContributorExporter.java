package org.opencdmp.filetransformer.rda.service.rdafiletransformer.export;

import org.opencdmp.commonmodels.models.plandescriptionfields.PropertyDefinitionFieldSetItemModel;
import gr.cite.tools.exception.MyApplicationException;
import org.opencdmp.commonmodels.enums.*;
import org.opencdmp.commonmodels.models.*;
import org.opencdmp.commonmodels.models.description.*;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.FieldModel;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.FieldSetModel;
import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.commonmodels.models.reference.ReferenceModel;
import org.opencdmp.filetransformer.rda.model.rda.*;
import org.opencdmp.filetransformerbase.models.misc.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.AnswerGroup;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaMapperContext;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaMapperSupport;
import static org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaSemantics.*;

/** Builds the dmp {@code contributor} list from blueprint and template questions, researcher references and plan members. */
public class ContributorExporter extends RdaMapperSupport {

    public ContributorExporter(RdaMapperContext context) {
        super(context);
    }

    List<Contributor> buildContributor(PlanModel model){
        List<Contributor> values = new ArrayList<>();

        for (AnswerGroup answers : this.groups(model.getPlanBlueprint(), model.getProperties().getFieldSets(), List.of(SEMANTIC_DATASET_CONTRIBUTOR, SEMANTIC_DATASET_CONTRIBUTOR_CONTRIBUTOR_ID_IDENTIFIER, SEMANTIC_DATASET_CONTRIBUTOR_CONTRIBUTOR_ID_TYPE,
                SEMANTIC_DATASET_CONTRIBUTOR_MBOX, SEMANTIC_DATASET_CONTRIBUTOR_NAME, SEMANTIC_DATASET_CONTRIBUTOR_ROLE, SEMANTIC_DATASET_CONTRIBUTOR_AFFILIATION_NAME, SEMANTIC_DATASET_CONTRIBUTOR_AFFILIATION_ID_IDENTIFIER, SEMANTIC_DATASET_CONTRIBUTOR_AFFILIATION_ID_TYPE))) {
            boolean valueFound = false;
            Contributor item = new Contributor();

            org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel fieldModel = answers.input(SEMANTIC_DATASET_CONTRIBUTOR);
            if (fieldModel != null && fieldModel.getReferences() != null  && !fieldModel.getReferences().isEmpty()) {
                for (ReferenceModel referenceModel : fieldModel.getReferences()){
                    Contributor contributor = this.buildRdaContributor(referenceModel);
                    if (values.isEmpty()) values.add(contributor);
                    else if (values.stream().noneMatch(x -> sameIdentifier(x, contributor))) values.add(contributor);
                }
            }

            String fieldValue = answers.value(SEMANTIC_DATASET_CONTRIBUTOR_MBOX);
            if (fieldValue != null) {
                item.setMbox(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_DATASET_CONTRIBUTOR_NAME);
            if (fieldValue != null) {
                item.setName(fieldValue);
                valueFound = true;
            }

            List<String> fieldValues = answers.values(SEMANTIC_DATASET_CONTRIBUTOR_ROLE);
            if (fieldValues != null && !fieldValues.isEmpty()) {
                item.setRole(new HashSet<>(fieldValues));
                valueFound = true;
            }

            ContributorId id = new ContributorId();

            fieldValue = answers.value(SEMANTIC_DATASET_CONTRIBUTOR_CONTRIBUTOR_ID_IDENTIFIER);
            if (fieldValue != null) {
                id.setIdentifier(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_DATASET_CONTRIBUTOR_CONTRIBUTOR_ID_TYPE);
            if (fieldValue != null) {
                id.setType(fieldValue);
                valueFound = true;
            }

            if (id.getIdentifier() != null && id.getType() == null) id.setType(inferIdentifierType(id.getIdentifier()));
            if (id.getIdentifier() != null || id.getType() != null) item.setContributorId(List.of(id));

            Affiliation affiliation = new Affiliation();

            fieldValue = answers.value(SEMANTIC_DATASET_CONTRIBUTOR_AFFILIATION_NAME);
            if (fieldValue != null) {
                affiliation.setName(fieldValue);
                valueFound = true;
            }

            AffiliationId affiliationId = new AffiliationId();

            fieldValue = answers.value(SEMANTIC_DATASET_CONTRIBUTOR_AFFILIATION_ID_IDENTIFIER);
            if (fieldValue != null) {
                affiliationId.setIdentifier(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_DATASET_CONTRIBUTOR_AFFILIATION_ID_TYPE);
            if (fieldValue != null) {
                affiliationId.setType(fieldValue);
                valueFound = true;
            }

            if (affiliationId.getIdentifier() != null || affiliationId.getType() != null){
                affiliation.setAffiliationId(affiliationId);
                item.setAffiliation(List.of(affiliation));
            }

            if (valueFound) values.add(item);
        }

        return values;
    }

    public List<Contributor> buildRdaContributors(List<ReferenceModel> referenceModels, List<PlanUserModel> userModels) {
        List<Contributor> rdaModels = new ArrayList<>();
        for (ReferenceModel researcher : referenceModels){
            Contributor item = this.buildRdaContributor(researcher);
            if (item != null) rdaModels.add(item);
        }
        if (userModels != null && !userModels.isEmpty()) {
            // RDA requires a contributor name: a member without one is left out rather than failing the export
            Map<UUID, List<PlanUserModel>> userModelsGrouped = userModels.stream().filter(x -> x.getUser() != null && x.getUser().getId() != null && x.getUser().getName() != null && !x.getUser().getName().isBlank())
                    .collect(Collectors.groupingBy(x -> x.getUser().getId()));
            if (userModelsGrouped != null) {
                for (UUID userId: userModelsGrouped.keySet()) {
                    rdaModels.add(this.buildRdaContributorsByUser(userModelsGrouped.get(userId)));
                }
            }
        }

        return rdaModels;
    }

    public Contributor buildRdaContributorsByUser(List<PlanUserModel> useModelGroupByRoles) {
        Contributor rdaModel = new Contributor();
        rdaModel.setContributorId(List.of(buildRdaContributorId(useModelGroupByRoles.getFirst().getUser().getId().toString())));

        rdaModel.setName(useModelGroupByRoles.getFirst().getUser().getName());
        UserContactInfoModel emailContact = useModelGroupByRoles.getFirst().getUser().getContacts() == null ? null : useModelGroupByRoles.getFirst().getUser().getContacts().stream().filter(userContactInfo -> userContactInfo.getType().equals(ContactInfoType.Email)).findFirst().orElse(null);
        if (emailContact != null) {
            rdaModel.setMbox(emailContact.getValue());
        }
        rdaModel.setRole(new HashSet<>(useModelGroupByRoles.stream().map(x -> x.getRole().name()).collect(Collectors.toList())));
        return rdaModel;
    }

    public Contributor buildRdaContributor(ReferenceModel model) {
        if (model == null) throw new MyApplicationException("Contributor is missing");

        Contributor rdaModel = new Contributor();
        rdaModel.setContributorId(List.of(buildRdaContributorId(model.getReference())));
        rdaModel.setName(model.getLabel());
        rdaModel.setRole(new HashSet<>(List.of(model.getType().getName())));
        if (model.getDefinition() != null && model.getDefinition().getFields() != null) {
            model.getDefinition().getFields().stream().filter(field -> this.configuration.getRdaFileTransformerServiceProperties().getResearcherMalCode().equalsIgnoreCase(field.getCode())).findFirst().ifPresent(emailField -> rdaModel.setMbox(emailField.getValue()));
        }
        return rdaModel;
    }

    public ContributorId buildRdaContributorId(String id) {
        if (id == null || id.isBlank()) throw  new MyApplicationException("ContributorId is missing"); 
        ContributorId rda = new ContributorId();
        String[] idParts = id.split(":");
        String prefix = idParts.length > 1 ? idParts[0] : id;
        if (prefix.equals(PREFIX_ORCID)) {
            String finalId = id.replace(prefix + ":", "");
            rda.setIdentifier(ORCID_URL + finalId);
            rda.setType(DMP_TYPE_ORCID);
        } else {
            rda.setIdentifier(id);
            rda.setType(inferIdentifierType(id));
        }
        return rda;
    }

    List<Contributor> buildContributor(DescriptionModel model, List<Contributor> values){
        if (values == null) values = new ArrayList<>();

        for (AnswerGroup answers : this.groups(model.getDescriptionTemplate(), model.getProperties().getFieldSets(), List.of(SEMANTIC_DATASET_CONTRIBUTOR, SEMANTIC_DATASET_CONTRIBUTOR_CONTRIBUTOR_ID_IDENTIFIER, SEMANTIC_DATASET_CONTRIBUTOR_CONTRIBUTOR_ID_TYPE,
                SEMANTIC_DATASET_CONTRIBUTOR_MBOX, SEMANTIC_DATASET_CONTRIBUTOR_NAME, SEMANTIC_DATASET_CONTRIBUTOR_ROLE, SEMANTIC_DATASET_CONTRIBUTOR_AFFILIATION_NAME, SEMANTIC_DATASET_CONTRIBUTOR_AFFILIATION_ID_IDENTIFIER, SEMANTIC_DATASET_CONTRIBUTOR_AFFILIATION_ID_TYPE))) {
            boolean valueFound = false;
            Contributor item = new Contributor();

            org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel fieldModel = answers.input(SEMANTIC_DATASET_CONTRIBUTOR);
            if (fieldModel != null && fieldModel.getReferences() != null  && !fieldModel.getReferences().isEmpty()) {
                for (ReferenceModel referenceModel : fieldModel.getReferences()){
                    Contributor contributor = this.buildRdaContributor(referenceModel);
                    if (values.isEmpty()) values.add(contributor);
                    else if (values.stream().noneMatch(x -> sameIdentifier(x, contributor))) values.add(contributor);
                }
            }

            String fieldValue = answers.value(SEMANTIC_DATASET_CONTRIBUTOR_MBOX);
            if (fieldValue != null) {
                item.setMbox(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_DATASET_CONTRIBUTOR_NAME);
            if (fieldValue != null) {
                item.setName(fieldValue);
                valueFound = true;
            }

            List<String> fieldValues = answers.values(SEMANTIC_DATASET_CONTRIBUTOR_ROLE);
            if (fieldValues != null && !fieldValues.isEmpty()) {
                item.setRole(new HashSet<>(fieldValues));
                valueFound = true;
            }

            ContributorId id = new ContributorId();

            fieldValue = answers.value(SEMANTIC_DATASET_CONTRIBUTOR_CONTRIBUTOR_ID_IDENTIFIER);
            if (fieldValue != null) {
                id.setIdentifier(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_DATASET_CONTRIBUTOR_CONTRIBUTOR_ID_TYPE);
            if (fieldValue != null) {
                    id.setType(fieldValue);
                    valueFound = true;
            }

            if (id.getIdentifier() != null && id.getType() == null) id.setType(inferIdentifierType(id.getIdentifier()));
            if (id.getIdentifier() != null || id.getType() != null) item.setContributorId(List.of(id));

            Affiliation affiliation = new Affiliation();

            fieldValue = answers.value(SEMANTIC_DATASET_CONTRIBUTOR_AFFILIATION_NAME);
            if (fieldValue != null) {
                affiliation.setName(fieldValue);
                valueFound = true;
            }

            AffiliationId affiliationId = new AffiliationId();

            fieldValue = answers.value(SEMANTIC_DATASET_CONTRIBUTOR_AFFILIATION_ID_IDENTIFIER);
            if (fieldValue != null) {
                affiliationId.setIdentifier(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_DATASET_CONTRIBUTOR_AFFILIATION_ID_TYPE);
            if (fieldValue != null) {
                affiliationId.setType(fieldValue);
                valueFound = true;
            }

            if (affiliationId.getIdentifier() != null || affiliationId.getType() != null){
                affiliation.setAffiliationId(affiliationId);
                item.setAffiliation(List.of(affiliation));
            }

            if (valueFound && values.stream().noneMatch(x -> sameIdentifier(x, item))) values.add(item);
        }

        return values;
    }

    /** Adds the contributors not already listed under the same identifier (plan members come first). */
    void addDistinct(List<Contributor> contributors, List<Contributor> more) {
        for (Contributor contributor : more) {
            if (contributors.stream().noneMatch(x -> sameIdentifier(x, contributor))) contributors.add(contributor);
        }
    }

    /** Two contributors are the same person when they share an identifier; one without identifiers never matches. */
    private static boolean sameIdentifier(Contributor a, Contributor b) {
        if (a.getContributorId() == null || b.getContributorId() == null) return false;
        return a.getContributorId().stream().anyMatch(id1 -> id1.getIdentifier() != null
                && b.getContributorId().stream().anyMatch(id2 -> id1.getIdentifier().equals(id2.getIdentifier())));
    }
}
