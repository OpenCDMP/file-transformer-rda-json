
package org.opencdmp.filetransformer.rda.model.rda;

import com.fasterxml.jackson.annotation.*;
import gr.cite.tools.exception.MyApplicationException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * The Host Identifier Schema
 * <p>
 * Identifier for the DMP itself
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "identifier",
    "type"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class HostId implements Serializable
{

    /**
     * The Host Identifier Value Schema
     * <p>
     * Identifier for a DMP
     * (Required)
     * 
     */
    @JsonProperty("identifier")
    @JsonPropertyDescription("To indicate the specific value of an identifier for a host")
    private String identifier;
    /**
     * The DMP Identifier Type Schema
     * <p>
     * The DMP Identifier Type. Allowed values: handle, doi, ark, url, other
     * (Required)
     * 
     */
    @JsonProperty("type")
    @JsonPropertyDescription("To specify a type of an identifier for a host. Suggested Values: url")
    private String type;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = -6059908070202476841L;

    /**
     * The DMP Identifier Value Schema
     * <p>
     * Identifier for a DMP
     * (Required)
     * 
     */
    @JsonProperty("identifier")
    public String getIdentifier() {
        return identifier;
    }

    /**
     * The DMP Identifier Value Schema
     * <p>
     * Identifier for a DMP
     * (Required)
     * 
     */
    @JsonProperty("identifier")
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * The DMP Identifier Type Schema
     * <p>
     * The DMP Identifier Type. Suggested Values: url
     * (Required)
     * 
     */
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    /**
     * The DMP Identifier Type Schema
     * <p>
     * The DMP Identifier Type. Suggested Values: url
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
