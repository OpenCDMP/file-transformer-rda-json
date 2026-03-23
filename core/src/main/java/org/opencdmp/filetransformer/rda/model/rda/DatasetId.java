
package org.opencdmp.filetransformer.rda.model.rda;

import com.fasterxml.jackson.annotation.*;
import gr.cite.tools.exception.MyApplicationException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * The Dataset ID Schema
 * <p>
 * Dataset ID
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "identifier",
    "type"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasetId implements Serializable
{

    /**
     * The Dataset Identifier Schema
     * <p>
     * Identifier for a dataset
     * (Required)
     * 
     */
    @JsonProperty("identifier")
    @JsonPropertyDescription("Identifier for a dataset")
    private String identifier;
    /**
     * The Dataset Identifier Type Schema
     * <p>
     * Dataset identifier type. Suggested Values: orcid, isni, openid, other
     * (Required)
     * 
     */
    @JsonProperty("type")
    @JsonPropertyDescription("Dataset identifier type. Suggested Values: orcid, isni, openid, other")
    private String type;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = -6295164005851378031L;

    public DatasetId() {
    }

    public DatasetId(String identifier, String type) {
        this.identifier = identifier;
        this.type = type;
    }

    /**
     * The Dataset Identifier Schema
     * <p>
     * Identifier for a dataset
     * (Required)
     * 
     */
    @JsonProperty("identifier")
    public String getIdentifier() {
        return identifier;
    }

    /**
     * The Dataset Identifier Schema
     * <p>
     * Identifier for a dataset
     * (Required)
     * 
     */
    @JsonProperty("identifier")
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * The Dataset Identifier Type Schema
     * <p>
     * Dataset identifier type. Suggested Values: orcid, isni, openid, other
     * (Required)
     * 
     */
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    /**
     * The Dataset Identifier Type Schema
     * <p>
     * Dataset identifier type. Suggested Values: orcid, isni, openid, other
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
