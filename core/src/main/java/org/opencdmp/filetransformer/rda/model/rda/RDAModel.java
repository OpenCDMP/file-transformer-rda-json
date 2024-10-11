
package org.opencdmp.filetransformer.rda.model.rda;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;


/**
 * RDA DMP Common Standard Schema
 * <p>
 * JSON Schema for the RDA DMP Common Standard
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "dmp"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class RDAModel implements Serializable
{

    /**
     * The DMP Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("dmp")
    private Dmp dmp;
    private final static long serialVersionUID = 7331666133368350998L;

    /**
     * The DMP Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("dmp")
    public Dmp getDmp() {
        return dmp;
    }

    /**
     * The DMP Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("dmp")
    public void setDmp(Dmp dmp) {
        this.dmp = dmp;
    }

}
