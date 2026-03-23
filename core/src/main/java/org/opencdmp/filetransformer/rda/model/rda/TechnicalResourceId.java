package org.opencdmp.filetransformer.rda.model.rda;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "identifier",
        "type"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class TechnicalResourceId implements Serializable
{

    /**
     * The DMP Technical Resource Identifier Schema
     * <p>
     *
     * (Required)
     *
     */
    @JsonProperty("identifier")
    private String identifier;
    /**
     * The DMP Affiliation Identifier Type Schema
     * <p>
     * 
     * (Required)
     *
     */
    @JsonProperty("type")
    @JsonPropertyDescription("Identifier type.")
    private String type;

    /**
     * The DMP Affiliation Identifier Schema
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
     * The DMP Affiliation Identifier Schema
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
     * The DMP Affiliation Identifier Type Schema
     * <p>
     * Identifier type.  Suggested Values: doi, url, other
     * (Required)
     *
     */
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    /**
     * The DMP Affiliation Identifier Type Schema
     * <p>
     * Identifier type.  Suggested Values: doi, url, other
     * (Required)
     *
     */
    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }


}

