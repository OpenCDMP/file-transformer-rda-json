
package org.opencdmp.filetransformer.rda.model.rda;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The Dataset Technical Resource Items Schema
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "description",
    "name",
    "technical_resource_id",
    "additional_properties"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class TechnicalResource implements Serializable
{

    /**
     * The Dataset Technical Resource Description Schema
     * <p>
     * Description of the technical resource
     * 
     */
    @JsonProperty("description")
    @JsonPropertyDescription("Description of the technical resource")
    private String description;
    /**
     * The Dataset Technical Resource Name Schema
     * <p>
     * Name of the technical resource
     * (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name of the technical resource")
    private String name;

    @JsonProperty("technical_resource_id")
    @JsonPropertyDescription("Identifier of a technical resource")
    private List<TechnicalResourceId> technicalResourceId;

    @JsonProperty("additional_properties")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = -7451757227129483110L;

    /**
     * The Dataset Technical Resource Description Schema
     * <p>
     * Description of the technical resource
     * 
     */
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    /**
     * The Dataset Technical Resource Description Schema
     * <p>
     * Description of the technical resource
     * 
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The Dataset Technical Resource Name Schema
     * <p>
     * Name of the technical resource
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * The Dataset Technical Resource Name Schema
     * <p>
     * Name of the technical resource
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("technical_resource_id")
    public List<TechnicalResourceId> getTechnicalResourceId() {
        return technicalResourceId;
    }

    @JsonProperty("technical_resource_id")
    public void setTechnicalResourceId(List<TechnicalResourceId> technicalResourceId) {
        this.technicalResourceId = technicalResourceId;
    }

    @JsonProperty("additional_properties")
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonProperty("additional_properties")
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
