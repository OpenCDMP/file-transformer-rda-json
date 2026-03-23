
package org.opencdmp.filetransformer.rda.model.rda;

import com.fasterxml.jackson.annotation.*;
import gr.cite.tools.exception.MyApplicationException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * The Funding Grant ID Schema
 * <p>
 * Grant ID of the associated project
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "identifier",
    "type"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class GrantId implements Serializable
{

    /**
     * The Funding Grant ID Value Schema
     * <p>
     * Grant ID
     * (Required)
     * 
     */
    @JsonProperty("identifier")
    @JsonPropertyDescription("Grant ID")
    private String identifier;
    /**
     * The Funding Grant ID Type Schema
     * <p>
     * Identifier type. Suggested Values: doi, url
     * (Required)
     * 
     */
    @JsonProperty("type")
    @JsonPropertyDescription("Identifier type. Allowed values: url, other")
    private String type;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = -7738072672837592065L;

    /**
     * The Funding Grant ID Value Schema
     * <p>
     * Grant ID
     * (Required)
     * 
     */
    @JsonProperty("identifier")
    public String getIdentifier() {
        return identifier;
    }

    /**
     * The Funding Grant ID Value Schema
     * <p>
     * Grant ID
     * (Required)
     * 
     */
    @JsonProperty("identifier")
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * The Funding Grant ID Type Schema
     * <p>
     * Identifier type. Suggested Values: doi, url
     * (Required)
     * 
     */
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    /**
     * The Funding Grant ID Type Schema
     * <p>
     * Identifier type. Suggested Values: doi, url
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
