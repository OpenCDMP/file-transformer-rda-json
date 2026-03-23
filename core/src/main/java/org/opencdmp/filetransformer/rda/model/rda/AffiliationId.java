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
public class AffiliationId implements Serializable
{

    /**
     * The DMP Affiliation Identifier Schema
     * <p>
     *
     * (Required)
     *
     */
    @JsonProperty("identifier")
    private String identifier;
    /**
     * The DMP Affiliation Identifier Schema
     * <p>
     *
     * (Required)
     *
     */
    @JsonProperty("type")
    private String type;
    /**
     * The DMP Affiliation Type Schema
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
     * Identifier type. Allowed values: orcid, isni, openid, other
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
     * Identifier type. Allowed values: orcid, isni, openid, other
     * (Required)
     *
     */
    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }



}

