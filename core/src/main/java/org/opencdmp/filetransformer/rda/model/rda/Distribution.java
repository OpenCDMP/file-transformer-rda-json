
package org.opencdmp.filetransformer.rda.model.rda;

import com.fasterxml.jackson.annotation.*;
import gr.cite.tools.exception.MyApplicationException;

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The Dataset Distribution Items Schema
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "access_url",
    "available_until",
    "byte_size",
    "data_access",
    "description",
    "download_url",
    "format",
    "host",
    "license",
    "title",
    "additional_properties"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Distribution implements Serializable
{

    /**
     * The Dataset Distribution Access URL Schema
     * <p>
     * A URL of the resource that gives access to a distribution of the dataset. e.g. landing page.
     * 
     */
    @JsonProperty("access_url")
    @JsonPropertyDescription("A URL of the resource that gives access to a distribution of the dataset. e.g. landing page.")
    private String accessUrl;
    /**
     * The Dataset Distribution Available Until Schema
     * <p>
     * Indicates how long this distribution will be / should be available.
     * 
     */
    @JsonProperty("available_until")
    @JsonPropertyDescription("Indicates how long this distribution will be / should be available.")
    private String availableUntil;
    /**
     * The Dataset Distribution Byte Size Schema
     * <p>
     * Size in bytes.
     * 
     */
    @JsonProperty("byte_size")
    @JsonPropertyDescription("Size in bytes.")
    private Integer byteSize;
    /**
     * The Dataset Distribution Data Access Schema
     * <p>
     * Indicates access mode for data. Allowed values: open, shared, closed
     * (Required)
     * 
     */
    @JsonProperty("data_access")
    @JsonPropertyDescription("Indicates access mode for data. Allowed values: open, shared, closed")
    private DataAccess dataAccess;
    /**
     * The Dataset Distribution Description Schema
     * <p>
     * Description is a property in both Dataset and Distribution, in compliance with W3C DCAT. In some cases these might be identical, but in most cases the Dataset represents a more abstract concept, while the distribution can point to a specific file.
     * 
     */
    @JsonProperty("description")
    @JsonPropertyDescription("Description is a property in both Dataset and Distribution, in compliance with W3C DCAT. In some cases these might be identical, but in most cases the Dataset represents a more abstract concept, while the distribution can point to a specific file.")
    private String description;
    /**
     * The Dataset Distribution Download URL Schema
     * <p>
     * The URL of the downloadable file in a given format. E.g. CSV file or RDF file.
     * 
     */
    @JsonProperty("download_url")
    @JsonPropertyDescription("The URL of the downloadable file in a given format. E.g. CSV file or RDF file.")
    private URI downloadUrl;
    /**
     * The Dataset Distribution Format Schema
     * <p>
     * Format according to: https://www.iana.org/assignments/media-types/media-types.xhtml if appropriate, otherwise use the common name for this format.
     * 
     */
    @JsonProperty("format")
    @JsonPropertyDescription("Format according to: https://www.iana.org/assignments/media-types/media-types.xhtml if appropriate, otherwise use the common name for this format.")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> format = null;
    /**
     * The Dataset Distribution Host Schema
     * <p>
     * To provide information on quality of service provided by infrastructure (e.g. repository) where data is stored.
     * 
     */
    @JsonProperty("host")
    @JsonPropertyDescription("To provide information on quality of service provided by infrastructure (e.g. repository) where data is stored.")
    private Host host;
    /**
     * The Dataset Distribution License(s) Schema
     * <p>
     * To list all licenses applied to a specific distribution of data.
     * 
     */
    @JsonProperty("license")
    @JsonPropertyDescription("To list all licenses applied to a specific distribution of data.")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<License> license = null;
    /**
     * The Dataset Distribution Title Schema
     * <p>
     * Title is a property in both Dataset and Distribution, in compliance with W3C DCAT. In some cases these might be identical, but in most cases the Dataset represents a more abstract concept, while the distribution can point to a specific file.
     * (Required)
     * 
     */
    @JsonProperty("title")
    @JsonPropertyDescription("Title is a property in both Dataset and Distribution, in compliance with W3C DCAT. In some cases these might be identical, but in most cases the Dataset represents a more abstract concept, while the distribution can point to a specific file.")
    private String title;
    @JsonProperty("additional_properties")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = -6018365280419917902L;

    /**
     * The Dataset Distribution Access URL Schema
     * <p>
     * A URL of the resource that gives access to a distribution of the dataset. e.g. landing page.
     * 
     */
    @JsonProperty("access_url")
    public String getAccessUrl() {
        return accessUrl;
    }

    /**
     * The Dataset Distribution Access URL Schema
     * <p>
     * A URL of the resource that gives access to a distribution of the dataset. e.g. landing page.
     * 
     */
    @JsonProperty("access_url")
    public void setAccessUrl(String accessUrl) {
        this.accessUrl = accessUrl;
    }

    /**
     * The Dataset Distribution Available Until Schema
     * <p>
     * Indicates how long this distribution will be / should be available.
     * 
     */
    @JsonProperty("available_until")
    public String getAvailableUntil() {
        return availableUntil;
    }

    /**
     * The Dataset Distribution Available Until Schema
     * <p>
     * Indicates how long this distribution will be / should be available.
     * 
     */
    @JsonProperty("available_until")
    public void setAvailableUntil(String availableUntil) {
        this.availableUntil = availableUntil;
    }

    /**
     * The Dataset Distribution Byte Size Schema
     * <p>
     * Size in bytes.
     * 
     */
    @JsonProperty("byte_size")
    public Integer getByteSize() {
        return byteSize;
    }

    /**
     * The Dataset Distribution Byte Size Schema
     * <p>
     * Size in bytes.
     * 
     */
    @JsonProperty("byte_size")
    public void setByteSize(Integer byteSize) {
        this.byteSize = byteSize;
    }

    /**
     * The Dataset Distribution Data Access Schema
     * <p>
     * Indicates access mode for data. Allowed values: open, shared, closed
     * (Required)
     * 
     */
    @JsonProperty("data_access")
    public DataAccess getDataAccess() {
        return dataAccess;
    }

    /**
     * The Dataset Distribution Data Access Schema
     * <p>
     * Indicates access mode for data. Allowed values: open, shared, closed
     * (Required)
     * 
     */
    @JsonProperty("data_access")
    public void setDataAccess(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    /**
     * The Dataset Distribution Description Schema
     * <p>
     * Description is a property in both Dataset and Distribution, in compliance with W3C DCAT. In some cases these might be identical, but in most cases the Dataset represents a more abstract concept, while the distribution can point to a specific file.
     * 
     */
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    /**
     * The Dataset Distribution Description Schema
     * <p>
     * Description is a property in both Dataset and Distribution, in compliance with W3C DCAT. In some cases these might be identical, but in most cases the Dataset represents a more abstract concept, while the distribution can point to a specific file.
     * 
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The Dataset Distribution Download URL Schema
     * <p>
     * The URL of the downloadable file in a given format. E.g. CSV file or RDF file.
     * 
     */
    @JsonProperty("download_url")
    public URI getDownloadUrl() {
        return downloadUrl;
    }

    /**
     * The Dataset Distribution Download URL Schema
     * <p>
     * The URL of the downloadable file in a given format. E.g. CSV file or RDF file.
     * 
     */
    @JsonProperty("download_url")
    public void setDownloadUrl(URI downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    /**
     * The Dataset Distribution Format Schema
     * <p>
     * Format according to: https://www.iana.org/assignments/media-types/media-types.xhtml if appropriate, otherwise use the common name for this format.
     * 
     */
    @JsonProperty("format")
    public List<String> getFormat() {
        return format;
    }

    /**
     * The Dataset Distribution Format Schema
     * <p>
     * Format according to: https://www.iana.org/assignments/media-types/media-types.xhtml if appropriate, otherwise use the common name for this format.
     * 
     */
    @JsonProperty("format")
    public void setFormat(List<String> format) {
        this.format = format;
    }

    /**
     * The Dataset Distribution Host Schema
     * <p>
     * To provide information on quality of service provided by infrastructure (e.g. repository) where data is stored.
     * 
     */
    @JsonProperty("host")
    public Host getHost() {
        return host;
    }

    /**
     * The Dataset Distribution Host Schema
     * <p>
     * To provide information on quality of service provided by infrastructure (e.g. repository) where data is stored.
     * 
     */
    @JsonProperty("host")
    public void setHost(Host host) {
        this.host = host;
    }

    /**
     * The Dataset Distribution License(s) Schema
     * <p>
     * To list all licenses applied to a specific distribution of data.
     * 
     */
    @JsonProperty("license")
    public List<License> getLicense() {
        return license;
    }

    /**
     * The Dataset Distribution License(s) Schema
     * <p>
     * To list all licenses applied to a specific distribution of data.
     * 
     */
    @JsonProperty("license")
    public void setLicense(List<License> license) {
        this.license = license;
    }

    /**
     * The Dataset Distribution Title Schema
     * <p>
     * Title is a property in both Dataset and Distribution, in compliance with W3C DCAT. In some cases these might be identical, but in most cases the Dataset represents a more abstract concept, while the distribution can point to a specific file.
     * (Required)
     * 
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     * The Dataset Distribution Title Schema
     * <p>
     * Title is a property in both Dataset and Distribution, in compliance with W3C DCAT. In some cases these might be identical, but in most cases the Dataset represents a more abstract concept, while the distribution can point to a specific file.
     * (Required)
     * 
     */
    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty("additional_properties")
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonProperty("additional_properties")
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public enum DataAccess {

        OPEN("open"),
        SHARED("shared"),
        CLOSED("closed");
        private final String value;
        private final static Map<String, DataAccess> CONSTANTS = new HashMap<String, DataAccess>();

        static {
            for (DataAccess c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        DataAccess(String value) {
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
        public static DataAccess fromValue(String value) {
            DataAccess constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new MyApplicationException(value);
            } else {
                return constant;
            }
        }

    }

}
