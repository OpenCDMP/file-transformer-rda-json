
package org.opencdmp.filetransformer.rda.model.rda;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The DMP Creator Schema
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "affiliation",
    "creator_id",
    "mbox",
    "name"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Creator implements Serializable
{

    /**
     * The Affiliation Schema
     * <p>
     *
     *
     *
     */
    @JsonProperty("affiliation")
    private List<Affiliation> affiliation;
    /**
     * The Creator ID Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("creator_id")
    private List<CreatorId> creatorId;
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
    /**
     * The Affiliation Schema
     * <p>
     *
     *
     *
     */
    @JsonProperty("affiliation")
    public List<Affiliation> getAffiliation() {
        return affiliation;
    }

    /**
     * The Affiliation Schema
     * <p>
     *
     *
     *
     */
    @JsonProperty("affiliation")
    public void setAffiliation(List<Affiliation> affiliation) {
        this.affiliation = affiliation;
    }

    /**
     * The Creator ID Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("creator_id")
    public List<CreatorId> getCreatorId() {
        return creatorId;
    }

    /**
     * The Creator ID Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("creator_id")
    public void setCreatorId(List<CreatorId> creatorId) {
        this.creatorId = creatorId;
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


}
