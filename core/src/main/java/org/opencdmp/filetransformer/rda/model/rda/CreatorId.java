
package org.opencdmp.filetransformer.rda.model.rda;

import com.fasterxml.jackson.annotation.*;
import gr.cite.tools.exception.MyApplicationException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * The Contact ID Schema
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
public class CreatorId implements Serializable
{

    /**
     * The DMP Creator Identifier Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("identifier")
    private String identifier;
    /**
     * The DMP Creator Identifier Type Schema
     * <p>
     * Identifier type. Suggested Values: orcid, isni, openid, other
     * (Required)
     * 
     */
    @JsonProperty("type")
    @JsonPropertyDescription("Identifier type. Allowed values: orcid, isni, openid, other")
    private String type;

    /**
     * The DMP Contact Identifier Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("identifier")
    public String getIdentifier() {
        return identifier;
    }

    /**
     * The DMP Contact Identifier Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("identifier")
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * The DMP Contact Identifier Type Schema
     * <p>
     * Identifier type. Allowed values: orcid, isni, openid, other
     * (Required)
     * 
     */
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    /**
     * The DMP Contact Identifier Type Schema
     * <p>
     * Identifier type. Allowed values: orcid, isni, openid, other
     * (Required)
     * 
     */
    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }


}
