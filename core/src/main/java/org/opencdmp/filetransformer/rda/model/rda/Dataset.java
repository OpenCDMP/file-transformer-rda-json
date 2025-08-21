
package org.opencdmp.filetransformer.rda.model.rda;

import com.fasterxml.jackson.annotation.*;
import gr.cite.tools.exception.MyApplicationException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The Dataset Items Schema
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "data_quality_assurance",
    "dataset_id",
    "description",
    "distribution",
    "issued",
    "keyword",
    "language",
    "metadata",
    "personal_data",
    "preservation_statement",
    "security_and_privacy",
    "sensitive_data",
    "technical_resource",
    "title",
    "type",
    "additional_properties"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Dataset implements Serializable
{

    /**
     * The Data Quality Assurance Schema
     * <p>
     * Data Quality Assurance
     * 
     */
    @JsonProperty("data_quality_assurance")
    @JsonPropertyDescription("Data Quality Assurance")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> dataQualityAssurance = null;
    /**
     * The Dataset ID Schema
     * <p>
     * Dataset ID
     * (Required)
     * 
     */
    @JsonProperty("dataset_id")
    @JsonPropertyDescription("Dataset ID")
    private DatasetId datasetId;
    /**
     * The Dataset Description Schema
     * <p>
     * Description is a property in both Dataset and Distribution, in compliance with W3C DCAT. In some cases these might be identical, but in most cases the Dataset represents a more abstract concept, while the distribution can point to a specific file.
     * 
     */
    @JsonProperty("description")
    @JsonPropertyDescription("Description is a property in both Dataset and Distribution, in compliance with W3C DCAT. In some cases these might be identical, but in most cases the Dataset represents a more abstract concept, while the distribution can point to a specific file.")
    private String description;
    /**
     * The Dataset Distribution Schema
     * <p>
     * To provide technical information on a specific instance of data.
     * 
     */
    @JsonProperty("distribution")
    @JsonPropertyDescription("To provide technical information on a specific instance of data.")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Distribution> distribution = null;
    /**
     * The Dataset is Reused Schema
     * <p>
     * Indication if the dataset is reused, i.e., not produced in project(s) covered by this DMP.
     *
     */
    @JsonProperty("is_reused")
    @JsonPropertyDescription("Indication if the dataset is reused")
    private Boolean isReused;
    /**
     * The Dataset Date of Issue Schema
     * <p>
     * Date of Issue
     * 
     */
    @JsonProperty("issued")
    @JsonPropertyDescription("Date of Issue")
    private String issued;
    /**
     * The Dataset Keyword(s) Schema
     * <p>
     * Keywords
     * 
     */
    @JsonProperty("keyword")
    @JsonPropertyDescription("Keywords")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> keyword = null;
    /**
     * The Dataset Language Schema
     * <p>
     * Language of the dataset expressed using ISO 639-3.
     * 
     */
    @JsonProperty("language")
    @JsonPropertyDescription("Language of the dataset expressed using ISO 639-3.")
    private Language language;
    /**
     * The Dataset Metadata Schema
     * <p>
     * To describe metadata standards used.
     * 
     */
    @JsonProperty("metadata")
    @JsonPropertyDescription("To describe metadata standards used.")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Metadatum> metadata = null;
    /**
     * The Dataset Personal Data Schema
     * <p>
     * If any personal data is contained. Allowed values: yes, no, unknown
     * (Required)
     * 
     */
    @JsonProperty("personal_data")
    @JsonPropertyDescription("If any personal data is contained. Allowed values: yes, no, unknown")
    private PersonalData personalData;
    /**
     * The Dataset Preservation Statement Schema
     * <p>
     * Preservation Statement
     * 
     */
    @JsonProperty("preservation_statement")
    @JsonPropertyDescription("Preservation Statement")
    private String preservationStatement;
    /**
     * The Dataset Security and Policy Schema
     * <p>
     * To list all issues and requirements related to security and privacy
     * 
     */
    @JsonProperty("security_and_privacy")
    @JsonPropertyDescription("To list all issues and requirements related to security and privacy")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<SecurityAndPrivacy> securityAndPrivacy = null;
    /**
     * The Dataset Sensitive Data Schema
     * <p>
     * If any sensitive data is contained. Allowed values: yes, no, unknown
     * (Required)
     * 
     */
    @JsonProperty("sensitive_data")
    @JsonPropertyDescription("If any sensitive data is contained. Allowed values: yes, no, unknown")
    private SensitiveData sensitiveData;
    /**
     * The Dataset Technical Resource Schema
     * <p>
     * To list all technical resources needed to implement a DMP
     * 
     */
    @JsonProperty("technical_resource")
    @JsonPropertyDescription("To list all technical resources needed to implement a DMP")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<TechnicalResource> technicalResource = null;
    /**
     * The Dataset Title Schema
     * <p>
     * Title is a property in both Dataset and Distribution, in compliance with W3C DCAT. In some cases these might be identical, but in most cases the Dataset represents a more abstract concept, while the distribution can point to a specific file.
     * (Required)
     * 
     */
    @JsonProperty("title")
    @JsonPropertyDescription("Title is a property in both Dataset and Distribution, in compliance with W3C DCAT. In some cases these might be identical, but in most cases the Dataset represents a more abstract concept, while the distribution can point to a specific file.")
    private String title;
    /**
     * The Dataset Type Schema
     * <p>
     * If appropriate, type according to: DataCite and/or COAR dictionary. Otherwise use the common name for the type, e.g. raw data, software, survey, etc. https://schema.datacite.org/meta/kernel-4.1/doc/DataCite-MetadataKernel_v4.1.pdf http://vocabularies.coar-repositories.org/pubby/resource_type.html
     * 
     */
    @JsonProperty("type")
    @JsonPropertyDescription("If appropriate, type according to: DataCite and/or COAR dictionary. Otherwise use the common name for the type, e.g. raw data, software, survey, etc. https://schema.datacite.org/meta/kernel-4.1/doc/DataCite-MetadataKernel_v4.1.pdf http://vocabularies.coar-repositories.org/pubby/resource_type.html")
    private String type;
    @JsonProperty("additional_properties")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = -6931119120629009399L;

    /**
     * The Data Quality Assurance Schema
     * <p>
     * Data Quality Assurance
     * 
     */
    @JsonProperty("data_quality_assurance")
    public List<String> getDataQualityAssurance() {
        return dataQualityAssurance;
    }

    /**
     * The Data Quality Assurance Schema
     * <p>
     * Data Quality Assurance
     * 
     */
    @JsonProperty("data_quality_assurance")
    public void setDataQualityAssurance(List<String> dataQualityAssurance) {
        this.dataQualityAssurance = dataQualityAssurance;
    }

    /**
     * The Dataset ID Schema
     * <p>
     * Dataset ID
     * (Required)
     * 
     */
    @JsonProperty("dataset_id")
    public DatasetId getDatasetId() {
        return datasetId;
    }

    /**
     * The Dataset ID Schema
     * <p>
     * Dataset ID
     * (Required)
     * 
     */
    @JsonProperty("dataset_id")
    public void setDatasetId(DatasetId datasetId) {
        this.datasetId = datasetId;
    }

    /**
     * The Dataset Description Schema
     * <p>
     * Description is a property in both Dataset and Distribution, in compliance with W3C DCAT. In some cases these might be identical, but in most cases the Dataset represents a more abstract concept, while the distribution can point to a specific file.
     * 
     */
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    /**
     * The Dataset Description Schema
     * <p>
     * Description is a property in both Dataset and Distribution, in compliance with W3C DCAT. In some cases these might be identical, but in most cases the Dataset represents a more abstract concept, while the distribution can point to a specific file.
     * 
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The Dataset Distribution Schema
     * <p>
     * To provide technical information on a specific instance of data.
     * 
     */
    @JsonProperty("distribution")
    public List<Distribution> getDistribution() {
        return distribution;
    }

    /**
     * The Dataset Distribution Schema
     * <p>
     * To provide technical information on a specific instance of data.
     * 
     */
    @JsonProperty("distribution")
    public void setDistribution(List<Distribution> distribution) {
        this.distribution = distribution;
    }

    /**
     * The Dataset is Reused Schema
     * <p>
     * Indication if the dataset is reused, i.e., not produced in project(s) covered by this DMP.
     *
     */
    @JsonProperty("is_reused")
    public Boolean getIsReused() {
        return isReused;
    }

    /**
     * The Dataset is Reused Schema
     * <p>
     * Indication if the dataset is reused, i.e., not produced in project(s) covered by this DMP.
     *
     */
    @JsonProperty("is_reused")
    public void setIsReused(Boolean isReused) {
        this.isReused = isReused;
    }

    /**
     * The Dataset Date of Issue Schema
     * <p>
     * Date of Issue
     * 
     */
    @JsonProperty("issued")
    public String getIssued() {
        return issued;
    }

    /**
     * The Dataset Date of Issue Schema
     * <p>
     * Date of Issue
     * 
     */
    @JsonProperty("issued")
    public void setIssued(String issued) {
        this.issued = issued;
    }

    /**
     * The Dataset Keyword(s) Schema
     * <p>
     * Keywords
     * 
     */
    @JsonProperty("keyword")
    public List<String> getKeyword() {
        return keyword;
    }

    /**
     * The Dataset Keyword(s) Schema
     * <p>
     * Keywords
     * 
     */
    @JsonProperty("keyword")
    public void setKeyword(List<String> keyword) {
        this.keyword = keyword;
    }

    /**
     * The Dataset Language Schema
     * <p>
     * Language of the dataset expressed using ISO 639-3.
     * 
     */
    @JsonProperty("language")
    public Language getLanguage() {
        return language;
    }

    /**
     * The Dataset Language Schema
     * <p>
     * Language of the dataset expressed using ISO 639-3.
     * 
     */
    @JsonProperty("language")
    public void setLanguage(Language language) {
        this.language = language;
    }

    /**
     * The Dataset Metadata Schema
     * <p>
     * To describe metadata standards used.
     * 
     */
    @JsonProperty("metadata")
    public List<Metadatum> getMetadata() {
        return metadata;
    }

    /**
     * The Dataset Metadata Schema
     * <p>
     * To describe metadata standards used.
     * 
     */
    @JsonProperty("metadata")
    public void setMetadata(List<Metadatum> metadata) {
        this.metadata = metadata;
    }

    /**
     * The Dataset Personal Data Schema
     * <p>
     * If any personal data is contained. Allowed values: yes, no, unknown
     * (Required)
     * 
     */
    @JsonProperty("personal_data")
    public PersonalData getPersonalData() {
        return personalData;
    }

    /**
     * The Dataset Personal Data Schema
     * <p>
     * If any personal data is contained. Allowed values: yes, no, unknown
     * (Required)
     * 
     */
    @JsonProperty("personal_data")
    public void setPersonalData(PersonalData personalData) {
        this.personalData = personalData;
    }

    /**
     * The Dataset Preservation Statement Schema
     * <p>
     * Preservation Statement
     * 
     */
    @JsonProperty("preservation_statement")
    public String getPreservationStatement() {
        return preservationStatement;
    }

    /**
     * The Dataset Preservation Statement Schema
     * <p>
     * Preservation Statement
     * 
     */
    @JsonProperty("preservation_statement")
    public void setPreservationStatement(String preservationStatement) {
        this.preservationStatement = preservationStatement;
    }

    /**
     * The Dataset Security and Policy Schema
     * <p>
     * To list all issues and requirements related to security and privacy
     * 
     */
    @JsonProperty("security_and_privacy")
    public List<SecurityAndPrivacy> getSecurityAndPrivacy() {
        return securityAndPrivacy;
    }

    /**
     * The Dataset Security and Policy Schema
     * <p>
     * To list all issues and requirements related to security and privacy
     * 
     */
    @JsonProperty("security_and_privacy")
    public void setSecurityAndPrivacy(List<SecurityAndPrivacy> securityAndPrivacy) {
        this.securityAndPrivacy = securityAndPrivacy;
    }

    /**
     * The Dataset Sensitive Data Schema
     * <p>
     * If any sensitive data is contained. Allowed values: yes, no, unknown
     * (Required)
     * 
     */
    @JsonProperty("sensitive_data")
    public SensitiveData getSensitiveData() {
        return sensitiveData;
    }

    /**
     * The Dataset Sensitive Data Schema
     * <p>
     * If any sensitive data is contained. Allowed values: yes, no, unknown
     * (Required)
     * 
     */
    @JsonProperty("sensitive_data")
    public void setSensitiveData(SensitiveData sensitiveData) {
        this.sensitiveData = sensitiveData;
    }

    /**
     * The Dataset Technical Resource Schema
     * <p>
     * To list all technical resources needed to implement a DMP
     * 
     */
    @JsonProperty("technical_resource")
    public List<TechnicalResource> getTechnicalResource() {
        return technicalResource;
    }

    /**
     * The Dataset Technical Resource Schema
     * <p>
     * To list all technical resources needed to implement a DMP
     * 
     */
    @JsonProperty("technical_resource")
    public void setTechnicalResource(List<TechnicalResource> technicalResource) {
        this.technicalResource = technicalResource;
    }

    /**
     * The Dataset Title Schema
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
     * The Dataset Title Schema
     * <p>
     * Title is a property in both Dataset and Distribution, in compliance with W3C DCAT. In some cases these might be identical, but in most cases the Dataset represents a more abstract concept, while the distribution can point to a specific file.
     * (Required)
     * 
     */
    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * The Dataset Type Schema
     * <p>
     * If appropriate, type according to: DataCite and/or COAR dictionary. Otherwise use the common name for the type, e.g. raw data, software, survey, etc. https://schema.datacite.org/meta/kernel-4.1/doc/DataCite-MetadataKernel_v4.1.pdf http://vocabularies.coar-repositories.org/pubby/resource_type.html
     * 
     */
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    /**
     * The Dataset Type Schema
     * <p>
     * If appropriate, type according to: DataCite and/or COAR dictionary. Otherwise use the common name for the type, e.g. raw data, software, survey, etc. https://schema.datacite.org/meta/kernel-4.1/doc/DataCite-MetadataKernel_v4.1.pdf http://vocabularies.coar-repositories.org/pubby/resource_type.html
     * 
     */
    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("additional_properties")
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonProperty("additional_properties")
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public enum PersonalData {

        YES("yes"),
        NO("no"),
        UNKNOWN("unknown");
        private final String value;
        private final static Map<String, PersonalData> CONSTANTS = new HashMap<String, PersonalData>();

        static {
            for (PersonalData c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        PersonalData(String value) {
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
        public static PersonalData fromValue(String value) {
            PersonalData constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new MyApplicationException(value);
            } else {
                return constant;
            }
        }

    }

    public enum SensitiveData {

        YES("yes"),
        NO("no"),
        UNKNOWN("unknown");
        private final String value;
        private final static Map<String, SensitiveData> CONSTANTS = new HashMap<String, SensitiveData>();

        static {
            for (SensitiveData c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        SensitiveData(String value) {
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
        public static SensitiveData fromValue(String value) {
            SensitiveData constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new MyApplicationException(value);
            } else {
                return constant;
            }
        }

    }

}
