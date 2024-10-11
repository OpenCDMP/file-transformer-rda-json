
package org.opencdmp.filetransformer.rda.model.rda;

import com.fasterxml.jackson.annotation.*;
import gr.cite.tools.exception.MyApplicationException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * The Dataset Metadata Standard ID Schema
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "identifier",
    "type"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetadataStandardId implements Serializable
{

    /**
     * The Dataset Metadata Standard Identifier Value Schema
     * <p>
     * Identifier for the metadata standard used.
     * (Required)
     * 
     */
    @JsonProperty("identifier")
    @JsonPropertyDescription("Identifier for the metadata standard used.")
    private String identifier;
    /**
     * The Dataset Metadata Standard Identifier Type Schema
     * <p>
     * Identifier type. Allowed values: url, other
     * (Required)
     * 
     */
    @JsonProperty("type")
    @JsonPropertyDescription("Identifier type. Allowed values: url, other")
    private Type type;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = -7641042701935397947L;

    /**
     * The Dataset Metadata Standard Identifier Value Schema
     * <p>
     * Identifier for the metadata standard used.
     * (Required)
     * 
     */
    @JsonProperty("identifier")
    public String getIdentifier() {
        return identifier;
    }

    /**
     * The Dataset Metadata Standard Identifier Value Schema
     * <p>
     * Identifier for the metadata standard used.
     * (Required)
     * 
     */
    @JsonProperty("identifier")
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * The Dataset Metadata Standard Identifier Type Schema
     * <p>
     * Identifier type. Allowed values: url, other
     * (Required)
     * 
     */
    @JsonProperty("type")
    public Type getType() {
        return type;
    }

    /**
     * The Dataset Metadata Standard Identifier Type Schema
     * <p>
     * Identifier type. Allowed values: url, other
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
