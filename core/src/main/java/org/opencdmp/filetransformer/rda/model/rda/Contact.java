
package org.opencdmp.filetransformer.rda.model.rda;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * The DMP Contact Schema
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "contact_id",
    "mbox",
    "name"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Contact implements Serializable
{

    /**
     * The Contact ID Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("contact_id")
    private ContactId contactId;
    /**
     * The Mailbox Schema
     * <p>
     * Contact Person's E-mail address
     * (Required)
     * 
     */
    @JsonProperty("mbox")
    @JsonPropertyDescription("Contact Person's E-mail address")
    private String mbox;
    /**
     * The Name Schema
     * <p>
     * Name of the contact person
     * (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name of the contact person")
    private String name;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = -2062619884605400321L;

    /**
     * The Contact ID Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("contact_id")
    public ContactId getContactId() {
        return contactId;
    }

    /**
     * The Contact ID Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("contact_id")
    public void setContactId(ContactId contactId) {
        this.contactId = contactId;
    }

    /**
     * The Mailbox Schema
     * <p>
     * Contact Person's E-mail address
     * (Required)
     * 
     */
    @JsonProperty("mbox")
    public String getMbox() {
        return mbox;
    }

    /**
     * The Mailbox Schema
     * <p>
     * Contact Person's E-mail address
     * (Required)
     * 
     */
    @JsonProperty("mbox")
    public void setMbox(String mbox) {
        this.mbox = mbox;
    }

    /**
     * The Name Schema
     * <p>
     * Name of the contact person
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * The Name Schema
     * <p>
     * Name of the contact person
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("additional_properties")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonProperty("additional_properties")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
