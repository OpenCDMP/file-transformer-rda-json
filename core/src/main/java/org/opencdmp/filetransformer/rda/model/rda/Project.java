
package org.opencdmp.filetransformer.rda.model.rda;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The DMP Project Items Schema
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "description",
    "end",
    "funding",
    "start",
    "title"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Project implements Serializable
{

    /**
     * The DMP Project Description Schema
     * <p>
     * Project description
     * 
     */
    @JsonProperty("description")
    @JsonPropertyDescription("Project description")
    private String description;
    /**
     * The DMP Project End Date Schema
     * <p>
     * Project end date
     * (Required)
     * 
     */
    @JsonProperty("end")
    @JsonPropertyDescription("Project end date")
    private String end;
    /**
     * The DMP Project Funding Schema
     * <p>
     * Funding related with a project
     * 
     */
    @JsonProperty("funding")
    @JsonPropertyDescription("Funding related with a project")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Funding> funding = null;
    /**
     * The DMP Project Start Date Schema
     * <p>
     * Project start date
     * (Required)
     * 
     */
    @JsonProperty("start")
    @JsonPropertyDescription("Project start date")
    private String start;
    /**
     * The DMP Project Title Schema
     * <p>
     * Project title
     * (Required)
     * 
     */
    @JsonProperty("title")
    @JsonPropertyDescription("Project title")
    private String title;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = 1437619307195890472L;

    /**
     * The DMP Project Description Schema
     * <p>
     * Project description
     * 
     */
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    /**
     * The DMP Project Description Schema
     * <p>
     * Project description
     * 
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The DMP Project End Date Schema
     * <p>
     * Project end date
     * (Required)
     * 
     */
    @JsonProperty("end")
    public String getEnd() {
        return end;
    }

    /**
     * The DMP Project End Date Schema
     * <p>
     * Project end date
     * (Required)
     * 
     */
    @JsonProperty("end")
    public void setEnd(String end) {
        this.end = end;
    }

    /**
     * The DMP Project Funding Schema
     * <p>
     * Funding related with a project
     * 
     */
    @JsonProperty("funding")
    public List<Funding> getFunding() {
        return funding;
    }

    /**
     * The DMP Project Funding Schema
     * <p>
     * Funding related with a project
     * 
     */
    @JsonProperty("funding")
    public void setFunding(List<Funding> funding) {
        this.funding = funding;
    }

    /**
     * The DMP Project Start Date Schema
     * <p>
     * Project start date
     * (Required)
     * 
     */
    @JsonProperty("start")
    public String getStart() {
        return start;
    }

    /**
     * The DMP Project Start Date Schema
     * <p>
     * Project start date
     * (Required)
     * 
     */
    @JsonProperty("start")
    public void setStart(String start) {
        this.start = start;
    }

    /**
     * The DMP Project Title Schema
     * <p>
     * Project title
     * (Required)
     * 
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     * The DMP Project Title Schema
     * <p>
     * Project title
     * (Required)
     * 
     */
    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
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
