package org.opencdmp.filetransformer.rda.model.rda;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "identifier",
        "type"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectId implements Serializable
{

    /**
     * The DMP Project Identifier Schema
     * <p>
     *
     * (Required)
     *
     */
    @JsonProperty("identifier")
    private String identifier;
    /**
     * The DMP Project Identifier Schema
     * <p>
     *  Suggested Values: doi, raid, url
     * (Required)
     *
     */
    @JsonProperty("type")
    private String type;

    /**
     * The DMP Project Type Schema
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
     * The DMP Project Identifier Schema
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
     * The DMP Project Identifier Type Schema
     * <p>
     * Suggested Values: doi, raid, url
     * (Required)
     *
     */
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    /**
     * The DMP Project Identifier Type Schema
     * <p>
     * Suggested Values: doi, raid, url
     * (Required)
     *
     */
    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }



}

