
package org.opencdmp.filetransformer.rda.model.rda;

import com.fasterxml.jackson.annotation.*;
import gr.cite.tools.exception.MyApplicationException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * The Dataset ID Schema
 * <p>
 * Dataset ID
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "identifier",
    "type"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasetId implements Serializable
{

    /**
     * The Dataset Identifier Schema
     * <p>
     * Identifier for a dataset
     * (Required)
     * 
     */
    @JsonProperty("identifier")
    @JsonPropertyDescription("Identifier for a dataset")
    private String identifier;
    /**
     * The Dataset Identifier Type Schema
     * <p>
     * Dataset identifier type. Allowed values: handle, doi, ark, url, other
     * (Required)
     * 
     */
    @JsonProperty("type")
    @JsonPropertyDescription("Dataset identifier type. Allowed values: handle, doi, ark, url, other")
    private Type type;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = -6295164005851378031L;

    public DatasetId() {
    }

    public DatasetId(String identifier, Type type) {
        this.identifier = identifier;
        this.type = type;
    }

    /**
     * The Dataset Identifier Schema
     * <p>
     * Identifier for a dataset
     * (Required)
     * 
     */
    @JsonProperty("identifier")
    public String getIdentifier() {
        return identifier;
    }

    /**
     * The Dataset Identifier Schema
     * <p>
     * Identifier for a dataset
     * (Required)
     * 
     */
    @JsonProperty("identifier")
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * The Dataset Identifier Type Schema
     * <p>
     * Dataset identifier type. Allowed values: handle, doi, ark, url, other
     * (Required)
     * 
     */
    @JsonProperty("type")
    public Type getType() {
        return type;
    }

    /**
     * The Dataset Identifier Type Schema
     * <p>
     * Dataset identifier type. Allowed values: handle, doi, ark, url, other
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

        HANDLE("handle"),
        DOI("doi"),
        ARK("ark"),
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
