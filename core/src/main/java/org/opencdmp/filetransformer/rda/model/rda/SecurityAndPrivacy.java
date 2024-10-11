
package org.opencdmp.filetransformer.rda.model.rda;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * The Dataset Security & Policy Items Schema
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "description",
    "title",
    "additional_properties"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class SecurityAndPrivacy implements Serializable
{

    /**
     * The Dataset Security & Policy Description Schema
     * <p>
     * Description
     * 
     */
    @JsonProperty("description")
    @JsonPropertyDescription("Description")
    private String description;
    /**
     * The Dataset Security & Policy Title Schema
     * <p>
     * Title
     * (Required)
     * 
     */
    @JsonProperty("title")
    @JsonPropertyDescription("Title")
    private String title;
    @JsonProperty("additional_properties")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = 7863747935827682977L;

    /**
     * The Dataset Security & Policy Description Schema
     * <p>
     * Description
     * 
     */
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    /**
     * The Dataset Security & Policy Description Schema
     * <p>
     * Description
     * 
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The Dataset Security & Policy Title Schema
     * <p>
     * Title
     * (Required)
     * 
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     * The Dataset Security & Policy Title Schema
     * <p>
     * Title
     * (Required)
     * 
     */
    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
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
