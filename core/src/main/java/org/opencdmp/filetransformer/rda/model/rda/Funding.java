
package org.opencdmp.filetransformer.rda.model.rda;

import com.fasterxml.jackson.annotation.*;
import gr.cite.tools.exception.MyApplicationException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * The DMP Project Funding Items Schema
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "funder_id",
    "funding_status",
    "grant_id"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Funding implements Serializable
{

    /**
     * The Funder ID Schema
     * <p>
     * Funder ID of the associated project
     * (Required)
     * 
     */
    @JsonProperty("funder_id")
    @JsonPropertyDescription("Funder ID of the associated project")
    private FunderId funderId;
    /**
     * The Funding Status Schema
     * <p>
     * To express different phases of project lifecycle. Allowed values: planned, applied, granted, rejected
     * 
     */
    @JsonProperty("funding_status")
    @JsonPropertyDescription("To express different phases of project lifecycle. Allowed values: planned, applied, granted, rejected")
    private FundingStatus fundingStatus;
    /**
     * The Funding Grant ID Schema
     * <p>
     * Grant ID of the associated project
     * (Required)
     * 
     */
    @JsonProperty("grant_id")
    @JsonPropertyDescription("Grant ID of the associated project")
    private GrantId grantId;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = 8962229321225336165L;

    /**
     * The Funder ID Schema
     * <p>
     * Funder ID of the associated project
     * (Required)
     * 
     */
    @JsonProperty("funder_id")
    public FunderId getFunderId() {
        return funderId;
    }

    /**
     * The Funder ID Schema
     * <p>
     * Funder ID of the associated project
     * (Required)
     * 
     */
    @JsonProperty("funder_id")
    public void setFunderId(FunderId funderId) {
        this.funderId = funderId;
    }

    /**
     * The Funding Status Schema
     * <p>
     * To express different phases of project lifecycle. Allowed values: planned, applied, granted, rejected
     * 
     */
    @JsonProperty("funding_status")
    public FundingStatus getFundingStatus() {
        return fundingStatus;
    }

    /**
     * The Funding Status Schema
     * <p>
     * To express different phases of project lifecycle. Allowed values: planned, applied, granted, rejected
     * 
     */
    @JsonProperty("funding_status")
    public void setFundingStatus(FundingStatus fundingStatus) {
        this.fundingStatus = fundingStatus;
    }

    /**
     * The Funding Grant ID Schema
     * <p>
     * Grant ID of the associated project
     * (Required)
     * 
     */
    @JsonProperty("grant_id")
    public GrantId getGrantId() {
        return grantId;
    }

    /**
     * The Funding Grant ID Schema
     * <p>
     * Grant ID of the associated project
     * (Required)
     * 
     */
    @JsonProperty("grant_id")
    public void setGrantId(GrantId grantId) {
        this.grantId = grantId;
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

    public enum FundingStatus {

        PLANNED("planned"),
        APPLIED("applied"),
        GRANTED("granted"),
        REJECTED("rejected");
        private final String value;
        private final static Map<String, FundingStatus> CONSTANTS = new HashMap<String, FundingStatus>();

        static {
            for (FundingStatus c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        FundingStatus(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static FundingStatus fromValue(String value) {
            FundingStatus constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new MyApplicationException(value);
            } else {
                return constant;
            }
        }

    }

}
