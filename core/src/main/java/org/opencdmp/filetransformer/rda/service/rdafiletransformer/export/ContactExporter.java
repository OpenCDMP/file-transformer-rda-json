package org.opencdmp.filetransformer.rda.service.rdafiletransformer.export;

import org.opencdmp.commonmodels.models.plandescriptionfields.PropertyDefinitionFieldSetItemModel;
import org.opencdmp.commonmodels.enums.*;
import org.opencdmp.commonmodels.models.*;
import org.opencdmp.commonmodels.models.description.*;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.FieldSetModel;
import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.commonmodels.models.user.UserModel;
import org.opencdmp.filetransformer.rda.model.rda.*;
import org.opencdmp.filetransformerbase.models.misc.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.AnswerGroup;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaMapperContext;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaMapperSupport;
import static org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaSemantics.*;

/** Builds the dmp {@code contact}, falling back to the plan contact preset field and then the plan owner. */
public class ContactExporter extends RdaMapperSupport {

    public ContactExporter(RdaMapperContext context) {
        super(context);
    }

    public Contact buildRdaContact(PlanModel model) {
        Contact item = new Contact();
        ContactId rdaContactId = new ContactId();
        Affiliation affiliation = new Affiliation();
        AffiliationId affiliationId = new AffiliationId();

        for (AnswerGroup answers : this.groups(model.getPlanBlueprint(), model.getProperties().getFieldSets(), List.of(
                SEMANTIC_CONTACT_NAME,SEMANTIC_CONTACT_MBOX, SEMANTIC_CONTACT_ID_IDENTIFIER, SEMANTIC_CONTACT_ID_TYPE,
                SEMANTIC_CONTACT_AFFILIATION_NAME, SEMANTIC_CONTACT_AFFILIATION_ID_IDENTIFIER, SEMANTIC_CONTACT_AFFILIATION_ID_TYPE))) {

            String fieldValue = answers.value(SEMANTIC_CONTACT_NAME);
            if (fieldValue != null) {
                item.setName(fieldValue);
            }

            fieldValue = answers.value(SEMANTIC_CONTACT_MBOX);
            if (fieldValue != null) {
                item.setMbox(fieldValue);
            }


            fieldValue = answers.value(SEMANTIC_CONTACT_ID_IDENTIFIER);
            if (fieldValue != null) {
                rdaContactId.setIdentifier(fieldValue);
            }

            fieldValue = answers.value(SEMANTIC_CONTACT_ID_TYPE);
            if (fieldValue != null) {
                rdaContactId.setType(fieldValue);
            }

            fieldValue = answers.value(SEMANTIC_CONTACT_AFFILIATION_NAME);
            if (fieldValue != null) {
                affiliation.setName(fieldValue);
            }

            fieldValue = answers.value(SEMANTIC_CONTACT_AFFILIATION_ID_IDENTIFIER);
            if (fieldValue != null) {
                affiliationId.setIdentifier(fieldValue);
            }

            fieldValue = answers.value(SEMANTIC_CONTACT_AFFILIATION_ID_TYPE);
            if (fieldValue != null) {
                affiliationId.setType(fieldValue);
            }

            if(affiliationId.getIdentifier() != null || affiliationId.getType() != null) {
                affiliation.setAffiliationId(affiliationId);
                item.setAffiliation(List.of(affiliation));
            }

            if (rdaContactId.getIdentifier() != null && rdaContactId.getType() == null) rdaContactId.setType(inferIdentifierType(rdaContactId.getIdentifier()));
            item.setContactId(List.of(rdaContactId));

        }

        //fallback to system contacts
        if (model.getProperties() != null && model.getProperties().getContacts() != null && !model.getProperties().getContacts().isEmpty()) {
            if (item.getName() == null) {
                String name = Stream.of(model.getProperties().getContacts().getFirst().getFirstName(), model.getProperties().getContacts().getFirst().getLastName())
                        .filter(x -> x != null && !x.isBlank()).collect(Collectors.joining(" "));
                if (!name.isEmpty()) item.setName(name);
            }
            if (item.getMbox() == null) {
                item.setMbox(model.getProperties().getContacts().getFirst().getEmail());
            }

        }

        // if values are null fallback to default
        UserModel creator = model.getCreator();
        if (creator == null) {
            if (model.getUsers() == null || model.getUsers().isEmpty()) return item;
            creator = model.getUsers().stream().filter(x -> x.getRole().equals(PlanUserRole.Owner) && x.getUser() != null && x.getUser().getContacts() != null && x.getUser().getContacts()
                    .stream().anyMatch(y -> y.getType().equals(ContactInfoType.Email) && y.getValue() != null && !y.getValue().isBlank())).map(PlanUserModel::getUser).findFirst().orElse(null);
        }

        if (creator == null) return item;
        UserContactInfoModel emailContact = creator.getContacts() != null ? creator.getContacts().stream().filter(userContactInfo -> userContactInfo.getType().equals(ContactInfoType.Email)).findFirst().orElse(null) : null;

        if (item.getName() == null && creator.getName() != null && !creator.getName().isBlank()) item.setName(creator.getName());

        if (emailContact != null) {
            if (item.getMbox() == null) item.setMbox(emailContact.getValue());
            if (emailContact.getId() != null) {
                if (rdaContactId.getIdentifier() == null) rdaContactId.setIdentifier(emailContact.getId().toString());
                if (rdaContactId.getType() == null) rdaContactId.setType(inferIdentifierType(rdaContactId.getIdentifier()));
                item.setContactId(List.of(rdaContactId));
            }
        }

        return item;
    }
}
