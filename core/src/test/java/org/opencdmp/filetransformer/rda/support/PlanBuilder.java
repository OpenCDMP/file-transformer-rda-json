package org.opencdmp.filetransformer.rda.support;

import org.opencdmp.commonmodels.enums.ContactInfoType;
import org.opencdmp.commonmodels.enums.DescriptionStatus;
import org.opencdmp.commonmodels.enums.PlanAccessType;
import org.opencdmp.commonmodels.enums.PlanStatus;
import org.opencdmp.commonmodels.enums.PlanUserRole;
import org.opencdmp.commonmodels.models.PlanUserModel;
import org.opencdmp.commonmodels.models.UserContactInfoModel;
import org.opencdmp.commonmodels.models.description.DescriptionModel;
import org.opencdmp.commonmodels.models.description.DescriptionStatusModel;
import org.opencdmp.commonmodels.models.description.PropertyDefinitionModel;
import org.opencdmp.commonmodels.models.descriptiotemplate.DescriptionTemplateModel;
import org.opencdmp.commonmodels.models.plan.PlanContactModel;
import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.commonmodels.models.plan.PlanPropertiesModel;
import org.opencdmp.commonmodels.models.plan.PlanStatusModel;
import org.opencdmp.commonmodels.models.planblueprint.PlanBlueprintModel;
import org.opencdmp.commonmodels.models.plandescriptiontemplate.PlanDescriptionTemplateModel;
import org.opencdmp.commonmodels.models.planreference.PlanReferenceModel;
import org.opencdmp.commonmodels.models.reference.ReferenceModel;
import org.opencdmp.commonmodels.models.user.UserModel;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Builds a plan with the data every real export carries (dates, owner, language, status),
 * so scenarios only spell out the blueprint, templates and answers they are about.
 */
public final class PlanBuilder {

    public static final Instant CREATED = Instant.parse("2026-01-12T09:30:00Z");
    public static final Instant UPDATED = Instant.parse("2026-02-03T16:45:00Z");

    private final PlanModel plan = new PlanModel();

    private PlanBuilder(String label) {
        this.plan.setId(Ids.of("plan:" + label));
        this.plan.setLabel(label);
        this.plan.setDescription("Plan for " + label);
        this.plan.setVersion((short) 1);
        this.plan.setLanguage("en");
        this.plan.setAccessType(PlanAccessType.Restricted);
        this.plan.setCreatedAt(CREATED);
        this.plan.setUpdatedAt(UPDATED);

        PlanStatusModel status = new PlanStatusModel();
        status.setId(Ids.of("plan-status:draft"));
        status.setName("Draft");
        status.setInternalStatus(PlanStatus.Draft);
        this.plan.setStatus(status);

        UserModel owner = user("Maria Silva", "maria.silva@example.org");
        this.plan.setCreator(owner);
        PlanUserModel planUser = new PlanUserModel();
        planUser.setUser(owner);
        planUser.setRole(PlanUserRole.Owner);
        this.plan.setUsers(new ArrayList<>(List.of(planUser)));

        PlanPropertiesModel properties = new PlanPropertiesModel();
        properties.setFieldSets(new HashMap<>());
        properties.setContacts(new ArrayList<>());
        this.plan.setProperties(properties);
        this.plan.setReferences(new ArrayList<>());
        this.plan.setDescriptions(new ArrayList<>());
        this.plan.setDescriptionTemplates(new ArrayList<>());
        this.plan.setEntityDois(new ArrayList<>());
        this.blueprint(DefinitionBuilder.definition().section(DefinitionBuilder.DESCRIPTIONS_SECTION).toBlueprint("Empty blueprint"), ValuesBuilder.values());
    }

    public static PlanBuilder plan(String label) {
        return new PlanBuilder(label);
    }

    public PlanBuilder blueprint(PlanBlueprintModel blueprint, ValuesBuilder answers) {
        this.plan.setPlanBlueprint(blueprint);
        this.plan.getProperties().setFieldSets(answers.build());
        return this;
    }

    public PlanBuilder description(String label, DescriptionTemplateModel template, ValuesBuilder answers) {
        DescriptionModel description = new DescriptionModel();
        description.setId(Ids.of("description:" + this.plan.getLabel() + ":" + label));
        description.setLabel(label);
        description.setDescription("Asset " + label);
        description.setTags(new ArrayList<>());
        description.setCreatedAt(CREATED);
        description.setSectionId(DefinitionBuilder.DESCRIPTIONS_SECTION);
        description.setDescriptionTemplate(template);
        DescriptionStatusModel status = new DescriptionStatusModel();
        status.setId(Ids.of("description-status:draft"));
        status.setName("Draft");
        status.setInternalStatus(DescriptionStatus.Draft);
        description.setStatus(status);
        PropertyDefinitionModel properties = new PropertyDefinitionModel();
        properties.setFieldSets(answers.build());
        description.setProperties(properties);
        this.plan.getDescriptions().add(description);

        if (this.plan.getDescriptionTemplates().stream().noneMatch(x -> template.getGroupId().equals(x.getDescriptionTemplateGroupId()))) {
            PlanDescriptionTemplateModel link = new PlanDescriptionTemplateModel();
            link.setDescriptionTemplateGroupId(template.getGroupId());
            link.setSectionId(DefinitionBuilder.DESCRIPTIONS_SECTION);
            this.plan.getDescriptionTemplates().add(link);
        }
        return this;
    }

    /** A plan-level reference, as picked in a blueprint reference field (grants, funders, projects, researchers). */
    public PlanBuilder reference(ReferenceModel reference) {
        PlanReferenceModel planReference = new PlanReferenceModel();
        planReference.setId(Ids.of("plan-reference:" + reference.getId()));
        planReference.setReference(reference);
        this.plan.getReferences().add(planReference);
        return this;
    }

    /** A contact entered in the blueprint's contact preset field. */
    public PlanBuilder contact(String firstName, String lastName, String email) {
        PlanContactModel contact = new PlanContactModel();
        contact.setFirstName(firstName);
        contact.setLastName(lastName);
        contact.setEmail(email);
        this.plan.getProperties().getContacts().add(contact);
        return this;
    }

    public PlanBuilder member(String name, String email, PlanUserRole role) {
        PlanUserModel planUser = new PlanUserModel();
        planUser.setUser(user(name, email));
        planUser.setRole(role);
        this.plan.getUsers().add(planUser);
        return this;
    }

    public PlanBuilder tags(String... tags) {
        this.plan.getDescriptions().getLast().setTags(new ArrayList<>(List.of(tags)));
        return this;
    }

    public PlanModel build() {
        return this.plan;
    }

    private static UserModel user(String name, String email) {
        UserModel user = new UserModel();
        user.setId(Ids.of("user:" + email));
        user.setName(name);
        UserContactInfoModel contact = new UserContactInfoModel();
        contact.setId(Ids.of("user-contact:" + email));
        contact.setOrdinal(0);
        contact.setType(ContactInfoType.Email);
        contact.setValue(email);
        user.setContacts(new ArrayList<>(List.of(contact)));
        return user;
    }
}
