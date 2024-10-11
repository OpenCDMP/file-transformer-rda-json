
package org.opencdmp.filetransformer.rda.model.rda;

import com.fasterxml.jackson.annotation.*;
import gr.cite.tools.exception.MyApplicationException;

import java.io.Serializable;
import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The DMP Schema
 * <p>
 * 
 * 
 */
@JsonIgnoreProperties(value = { "schema" }, ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "contact",
    "contributor",
    "cost",
    "created",
    "dataset",
    "description",
    "dmp_id",
    "ethical_issues_description",
    "ethical_issues_exist",
    "ethical_issues_report",
    "language",
    "modified",
    "project",
    "title",
    "additional_properties"
})
public class Dmp implements Serializable
{

    /**
     * The DMP Contact Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("contact")
    private Contact contact;
    /**
     * The Contributor Schema
     * <p>
     * 
     * 
     */
    @JsonProperty("contributor")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Contributor> contributor = null;
    /**
     * The Cost Schema
     * <p>
     * 
     * 
     */
    @JsonProperty("cost")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Cost> cost = null;
    /**
     * The DMP Creation Schema
     * <p>
     * 
     * 
     */
    @JsonProperty("created")
    @JsonPropertyDescription("")
    private Instant created;
    /**
     * The Dataset Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("dataset")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Dataset> dataset = null;
    /**
     * The DMP Description Schema
     * <p>
     * To provide any free-form text information on a DMP
     * 
     */
    @JsonProperty("description")
    @JsonPropertyDescription("To provide any free-form text information on a DMP")
    private String description;
    /**
     * The DMP Identifier Schema
     * <p>
     * Identifier for the DMP itself
     * (Required)
     * 
     */
    @JsonProperty("dmp_id")
    @JsonPropertyDescription("Identifier for the DMP itself")
    private DmpId dmpId;
    /**
     * The DMP Ethical Issues Description Schema
     * <p>
     * To describe ethical issues directly in a DMP
     * 
     */
    @JsonProperty("ethical_issues_description")
    @JsonPropertyDescription("To describe ethical issues directly in a DMP")
    private String ethicalIssuesDescription;
    /**
     * The DMP Ethical Issues Exist Schema
     * <p>
     * To indicate whether there are ethical issues related to data that this DMP describes. Allowed values: yes, no, unknown
     * (Required)
     * 
     */
    @JsonProperty("ethical_issues_exist")
    @JsonPropertyDescription("To indicate whether there are ethical issues related to data that this DMP describes. Allowed values: yes, no, unknown")
    private EthicalIssuesExist ethicalIssuesExist;
    /**
     * The DMP Ethical Issues Report Schema
     * <p>
     * To indicate where a protocol from a meeting with an ethical commitee can be found
     * 
     */
    @JsonProperty("ethical_issues_report")
    @JsonPropertyDescription("To indicate where a protocol from a meeting with an ethical commitee can be found")
    private URI ethicalIssuesReport;
    /**
     * The DMP Language Schema
     * <p>
     * Language of the DMP expressed using ISO 639-3.
     * (Required)
     * 
     */
    @JsonProperty("language")
    @JsonPropertyDescription("Language of the DMP expressed using ISO 639-3.")
    private Language language;
    /**
     * The DMP Modification Schema
     * <p>
     * Must be set each time DMP is modified. Indicates DMP version.
     * (Required)
     * 
     */
    @JsonProperty("modified")
    @JsonPropertyDescription("Must be set each time DMP is modified. Indicates DMP version.")
    private Instant modified;
    /**
     * The DMP Project Schema
     * <p>
     * Project related to a DMP
     * 
     */
    @JsonProperty("project")
    @JsonPropertyDescription("Project related to a DMP")
    private List<Project> project = null;
    /**
     * The DMP Title Schema
     * <p>
     * Title of a DMP
     * (Required)
     * 
     */
    @JsonProperty("title")
    @JsonPropertyDescription("Title of a DMP")
    private String title;
    @JsonProperty("additional_properties")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = 4599713332472772292L;

    /**
     * The DMP Contact Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("contact")
    public Contact getContact() {
        return contact;
    }

    /**
     * The DMP Contact Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("contact")
    public void setContact(Contact contact) {
        this.contact = contact;
    }

    /**
     * The Contributor Schema
     * <p>
     * 
     * 
     */
    @JsonProperty("contributor")
    public List<Contributor> getContributor() {
        return contributor;
    }

    /**
     * The Contributor Schema
     * <p>
     * 
     * 
     */
    @JsonProperty("contributor")
    public void setContributor(List<Contributor> contributor) {
        this.contributor = contributor;
    }

    /**
     * The Cost Schema
     * <p>
     * 
     * 
     */
    @JsonProperty("cost")
    public List<Cost> getCost() {
        return cost;
    }

    /**
     * The Cost Schema
     * <p>
     * 
     * 
     */
    @JsonProperty("cost")
    public void setCost(List<Cost> cost) {
        this.cost = cost;
    }

    /**
     * The DMP Creation Schema
     * <p>
     * 
     * 
     */
    @JsonProperty("created")
    public Instant getCreated() {
        return created;
    }

    /**
     * The DMP Creation Schema
     * <p>
     * 
     * 
     */
    @JsonProperty("created")
    public void setCreated(Instant created) {
        this.created = created;
    }

    /**
     * The Dataset Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("dataset")
    public List<Dataset> getDataset() {
        return dataset;
    }

    /**
     * The Dataset Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("dataset")
    public void setDataset(List<Dataset> dataset) {
        this.dataset = dataset;
    }

    /**
     * The DMP Description Schema
     * <p>
     * To provide any free-form text information on a DMP
     * 
     */
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    /**
     * The DMP Description Schema
     * <p>
     * To provide any free-form text information on a DMP
     * 
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The DMP Identifier Schema
     * <p>
     * Identifier for the DMP itself
     * (Required)
     * 
     */
    @JsonProperty("dmp_id")
    public DmpId getDmpId() {
        return dmpId;
    }

    /**
     * The DMP Identifier Schema
     * <p>
     * Identifier for the DMP itself
     * (Required)
     * 
     */
    @JsonProperty("dmp_id")
    public void setDmpId(DmpId dmpId) {
        this.dmpId = dmpId;
    }

    /**
     * The DMP Ethical Issues Description Schema
     * <p>
     * To describe ethical issues directly in a DMP
     * 
     */
    @JsonProperty("ethical_issues_description")
    public String getEthicalIssuesDescription() {
        return ethicalIssuesDescription;
    }

    /**
     * The DMP Ethical Issues Description Schema
     * <p>
     * To describe ethical issues directly in a DMP
     * 
     */
    @JsonProperty("ethical_issues_description")
    public void setEthicalIssuesDescription(String ethicalIssuesDescription) {
        this.ethicalIssuesDescription = ethicalIssuesDescription;
    }

    /**
     * The DMP Ethical Issues Exist Schema
     * <p>
     * To indicate whether there are ethical issues related to data that this DMP describes. Allowed values: yes, no, unknown
     * (Required)
     * 
     */
    @JsonProperty("ethical_issues_exist")
    public EthicalIssuesExist getEthicalIssuesExist() {
        return ethicalIssuesExist;
    }

    /**
     * The DMP Ethical Issues Exist Schema
     * <p>
     * To indicate whether there are ethical issues related to data that this DMP describes. Allowed values: yes, no, unknown
     * (Required)
     * 
     */
    @JsonProperty("ethical_issues_exist")
    public void setEthicalIssuesExist(EthicalIssuesExist ethicalIssuesExist) {
        this.ethicalIssuesExist = ethicalIssuesExist;
    }

    /**
     * The DMP Ethical Issues Report Schema
     * <p>
     * To indicate where a protocol from a meeting with an ethical commitee can be found
     * 
     */
    @JsonProperty("ethical_issues_report")
    public URI getEthicalIssuesReport() {
        return ethicalIssuesReport;
    }

    /**
     * The DMP Ethical Issues Report Schema
     * <p>
     * To indicate where a protocol from a meeting with an ethical commitee can be found
     * 
     */
    @JsonProperty("ethical_issues_report")
    public void setEthicalIssuesReport(URI ethicalIssuesReport) {
        this.ethicalIssuesReport = ethicalIssuesReport;
    }

    /**
     * The DMP Language Schema
     * <p>
     * Language of the DMP expressed using ISO 639-3.
     * (Required)
     * 
     */
    @JsonProperty("language")
    public Language getLanguage() {
        return language;
    }

    /**
     * The DMP Language Schema
     * <p>
     * Language of the DMP expressed using ISO 639-3.
     * (Required)
     * 
     */
    @JsonProperty("language")
    public void setLanguage(Language language) {
        this.language = language;
    }

    /**
     * The DMP Modification Schema
     * <p>
     * Must be set each time DMP is modified. Indicates DMP version.
     * (Required)
     * 
     */
    @JsonProperty("modified")
    public Instant getModified() {
        return modified;
    }

    /**
     * The DMP Modification Schema
     * <p>
     * Must be set each time DMP is modified. Indicates DMP version.
     * (Required)
     * 
     */
    @JsonProperty("modified")
    public void setModified(Instant modified) {
        this.modified = modified;
    }

    /**
     * The DMP Project Schema
     * <p>
     * Project related to a DMP
     * 
     */
    @JsonProperty("project")
    public List<Project> getProject() {
        return project;
    }

    /**
     * The DMP Project Schema
     * <p>
     * Project related to a DMP
     * 
     */
    @JsonProperty("project")
    public void setProject(List<Project> project) {
        this.project = project;
    }

    /**
     * The DMP Title Schema
     * <p>
     * Title of a DMP
     * (Required)
     * 
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     * The DMP Title Schema
     * <p>
     * Title of a DMP
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

    public enum EthicalIssuesExist {

        YES("yes"),
        NO("no"),
        UNKNOWN("unknown");
        private final String value;
        private final static Map<String, EthicalIssuesExist> CONSTANTS = new HashMap<String, EthicalIssuesExist>();

        static {
            for (EthicalIssuesExist c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        EthicalIssuesExist(String value) {
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
        public static EthicalIssuesExist fromValue(String value) {
            EthicalIssuesExist constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new MyApplicationException(value);
            } else {
                return constant;
            }
        }

    }

}
