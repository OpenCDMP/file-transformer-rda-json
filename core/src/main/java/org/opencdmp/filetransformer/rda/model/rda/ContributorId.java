
package org.opencdmp.filetransformer.rda.model.rda;

import com.fasterxml.jackson.annotation.*;
import gr.cite.tools.exception.MyApplicationException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * The Contributor_id Schema
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "identifier",
    "type"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContributorId implements Serializable
{

    /**
     * The Contributor Identifier Schema
     * <p>
     * Identifier for a contact person
     * (Required)
     * 
     */
    @JsonProperty("identifier")
    @JsonPropertyDescription("Identifier for a contact person")
    private String identifier;
    /**
     * The Contributor Identifier Type Schema
     * <p>
     * Identifier type. Suggested Values: orcid, isni, openid
     * (Required)
     * 
     */
    @JsonProperty("type")
    @JsonPropertyDescription("Identifier type. Suggested Values: orcid, isni, openid")
    private String type;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = 3089650417960767482L;

    /**
     * The Contributor Identifier Schema
     * <p>
     * Identifier for a contact person
     * (Required)
     * 
     */
    @JsonProperty("identifier")
    public String getIdentifier() {
        return identifier;
    }

    /**
     * The Contributor Identifier Schema
     * <p>
     * Identifier for a contact person
     * (Required)
     * 
     */
    @JsonProperty("identifier")
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * The Contributor Identifier Type Schema
     * <p>
     * Identifier type. Suggested Values: orcid, isni, openid.
     * (Required)
     * 
     */
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    /**
     * The Contributor Identifier Type Schema
     * <p>
     * Identifier type. Suggested Values: orcid, isni, openid.
     * (Required)
     * 
     */
    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
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
