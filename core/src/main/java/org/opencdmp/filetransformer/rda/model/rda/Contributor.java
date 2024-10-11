
package org.opencdmp.filetransformer.rda.model.rda;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * The Contributor Items Schema
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "contributor_id",
    "mbox",
    "name",
    "role"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Contributor implements Serializable
{

    /**
     * The Contributor_id Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("contributor_id")
    private ContributorId contributorId;
    /**
     * The Contributor Mailbox Schema
     * <p>
     * Contributor Mail address
     * 
     */
    @JsonProperty("mbox")
    @JsonPropertyDescription("Contributor Mail address")
    private String mbox;
    /**
     * The Name Schema
     * <p>
     * Name of the contributor
     * (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name of the contributor")
    private String name;
    /**
     * The Role Schema
     * <p>
     * Type of contributor
     * (Required)
     * 
     */
    @JsonProperty("role")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("Type of contributor")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Set<String> role = null;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = 3452606902359513114L;

    /**
     * The Contributor_id Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("contributor_id")
    public ContributorId getContributorId() {
        return contributorId;
    }

    /**
     * The Contributor_id Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("contributor_id")
    public void setContributorId(ContributorId contributorId) {
        this.contributorId = contributorId;
    }

    /**
     * The Contributor Mailbox Schema
     * <p>
     * Contributor Mail address
     * 
     */
    @JsonProperty("mbox")
    public String getMbox() {
        return mbox;
    }

    /**
     * The Contributor Mailbox Schema
     * <p>
     * Contributor Mail address
     * 
     */
    @JsonProperty("mbox")
    public void setMbox(String mbox) {
        this.mbox = mbox;
    }

    /**
     * The Name Schema
     * <p>
     * Name of the contributor
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
     * Name of the contributor
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Role Schema
     * <p>
     * Type of contributor
     * (Required)
     * 
     */
    @JsonProperty("role")
    public Set<String> getRole() {
        return role;
    }

    /**
     * The Role Schema
     * <p>
     * Type of contributor
     * (Required)
     * 
     */
    @JsonProperty("role")
    public void setRole(Set<String> role) {
        this.role = role;
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
