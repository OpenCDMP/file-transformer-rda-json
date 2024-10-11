
package org.opencdmp.filetransformer.rda.model.rda;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;


/**
 * The Dataset Distribution License Items
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "license_ref",
    "start_date"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class License implements Serializable
{

    /**
     * The Dataset Distribution License Reference Schema
     * <p>
     * Link to license document.
     * (Required)
     * 
     */
    @JsonProperty("license_ref")
    @JsonPropertyDescription("Link to license document.")
    private URI licenseRef;
    /**
     * The Dataset Distribution License Start Date Schema
     * <p>
     * Starting date of license. If date is set in the future, it indicates embargo period.
     * (Required)
     * 
     */
    @JsonProperty("start_date")
    @JsonPropertyDescription("Starting date of license. If date is set in the future, it indicates embargo period.")
    private String startDate;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = 4148207295817559010L;

    /**
     * The Dataset Distribution License Reference Schema
     * <p>
     * Link to license document.
     * (Required)
     * 
     */
    @JsonProperty("license_ref")
    public URI getLicenseRef() {
        return licenseRef;
    }

    /**
     * The Dataset Distribution License Reference Schema
     * <p>
     * Link to license document.
     * (Required)
     * 
     */
    @JsonProperty("license_ref")
    public void setLicenseRef(URI licenseRef) {
        this.licenseRef = licenseRef;
    }

    /**
     * The Dataset Distribution License Start Date Schema
     * <p>
     * Starting date of license. If date is set in the future, it indicates embargo period.
     * (Required)
     * 
     */
    @JsonProperty("start_date")
    public String getStartDate() {
        return startDate;
    }

    /**
     * The Dataset Distribution License Start Date Schema
     * <p>
     * Starting date of license. If date is set in the future, it indicates embargo period.
     * (Required)
     * 
     */
    @JsonProperty("start_date")
    public void setStartDate(String startDate) {
        this.startDate = startDate;
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
