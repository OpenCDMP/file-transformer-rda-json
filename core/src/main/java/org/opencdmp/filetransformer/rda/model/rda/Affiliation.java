package org.opencdmp.filetransformer.rda.model.rda;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;


/**
 * The Affiliation Schema
 * <p>
 *
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "affiliation_id",
        "name"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Affiliation implements Serializable
{

    /**
     * The DMP Affiliation Value Schema
     * <p>
     * Identifier for a DMP
     * (Required)
     *
     */
    @JsonProperty("affiliation_id")
    @JsonPropertyDescription("Identifier for an affiliation")
    private AffiliationId affiliation_id;
    /**
     * The Name of an affiliation
     * <p>
     *
     * (Required)
     *
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name of an affiliation")
    private String name;

    /**
     * The DMP Affiliation Value Schema
     * <p>
     * Identifier for a DMP
     * (Required)
     *
     */
    @JsonProperty("affiliation_id")
    public AffiliationId getAffiliationId() {
        return affiliation_id;
    }

    /**
     * The DMP Affiliation Value Schema
     * <p>
     * Identifier for a DMP
     * (Required)
     *
     */
    @JsonProperty("affiliation_id")
    public void setAffiliationId(AffiliationId affiliation_id) {
        this.affiliation_id = affiliation_id;
    }

    /**
     * The Name of an affiliation
     * <p>
     *
     * (Required)
     *
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * The Name of an affiliation
     * <p>
     *
     * (Required)
     *
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }


}
