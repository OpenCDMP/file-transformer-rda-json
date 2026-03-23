package org.opencdmp.filetransformer.rda.model.rda;

import com.fasterxml.jackson.annotation.*;
import gr.cite.tools.exception.MyApplicationException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * The Alternate DMP Identifier Schema
 * <p>
 * Alternate Identifier for the DMP itself
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "identifier",
        "type"
})
public class AlternateIdentifier implements Serializable
{

    /**
     * The DMP Alternate Identifier Value Schema
     * <p>
     * Identifier for a DMP
     * (Required)
     *
     */
    @JsonProperty("identifier")
    @JsonPropertyDescription("Identifier for a DMP for an Affiliation")
    private String identifier;
    /**
     * The DMP Identifier Type Schema
     * <p>
     * The DMP Identifier Type. Suggested Values: ror, grid, isni
     * (Required)
     *
     */
    @JsonProperty("type")
    @JsonPropertyDescription("The DMP Identifier Type For an Affiliation. Suggested Values: ror, grid, isni")
    private String type;

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
     * The DMP Identifier Value Schema For an Affiliation
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
     * The DMP Identifier Type For an Affiliation. Suggested Values: ror, grid, isni"
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
     * The DMP Identifier Type For an Affiliation. Allowed values: handle, doi, ark, url, other
     * (Required)
     *
     */
    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }


}
