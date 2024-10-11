
package org.opencdmp.filetransformer.rda.model.rda;

import com.fasterxml.jackson.annotation.*;
import gr.cite.tools.exception.MyApplicationException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * The Funder ID Schema
 * <p>
 * Funder ID of the associated project
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "identifier",
    "type"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class FunderId implements Serializable
{

    /**
     * The Funder ID Value Schema
     * <p>
     * Funder ID, recommended to use CrossRef Funder Registry. See: https://www.crossref.org/services/funder-registry/
     * (Required)
     * 
     */
    @JsonProperty("identifier")
    @JsonPropertyDescription("Funder ID, recommended to use CrossRef Funder Registry. See: https://www.crossref.org/services/funder-registry/")
    private String identifier;
    /**
     * The Funder ID Type Schema
     * <p>
     * Identifier type. Allowed values: fundref, url, other
     * (Required)
     * 
     */
    @JsonProperty("type")
    @JsonPropertyDescription("Identifier type. Allowed values: fundref, url, other")
    private Type type;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = 1783349151334366078L;

    /**
     * The Funder ID Value Schema
     * <p>
     * Funder ID, recommended to use CrossRef Funder Registry. See: https://www.crossref.org/services/funder-registry/
     * (Required)
     * 
     */
    @JsonProperty("identifier")
    public String getIdentifier() {
        return identifier;
    }

    /**
     * The Funder ID Value Schema
     * <p>
     * Funder ID, recommended to use CrossRef Funder Registry. See: https://www.crossref.org/services/funder-registry/
     * (Required)
     * 
     */
    @JsonProperty("identifier")
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * The Funder ID Type Schema
     * <p>
     * Identifier type. Allowed values: fundref, url, other
     * (Required)
     * 
     */
    @JsonProperty("type")
    public Type getType() {
        return type;
    }

    /**
     * The Funder ID Type Schema
     * <p>
     * Identifier type. Allowed values: fundref, url, other
     * (Required)
     * 
     */
    @JsonProperty("type")
    public void setType(Type type) {
        this.type = type;
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

    public enum Type {

        FUNDREF("fundref"),
        URL("url"),
        OTHER("other");
        private final String value;
        private final static Map<String, Type> CONSTANTS = new HashMap<String, Type>();

        static {
            for (Type c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        Type(String value) {
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
        public static Type fromValue(String value) {
            Type constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new MyApplicationException(value);
            } else {
                return constant;
            }
        }

    }

}
