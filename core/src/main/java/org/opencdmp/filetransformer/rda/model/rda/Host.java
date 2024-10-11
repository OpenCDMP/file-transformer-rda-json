
package org.opencdmp.filetransformer.rda.model.rda;

import com.fasterxml.jackson.annotation.*;
import gr.cite.tools.exception.MyApplicationException;

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The Dataset Distribution Host Schema
 * <p>
 * To provide information on quality of service provided by infrastructure (e.g. repository) where data is stored.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "availability",
    "backup_frequency",
    "backup_type",
    "certified_with",
    "description",
    "geo_location",
    "pid_system",
    "storage_type",
    "support_versioning",
    "title",
    "url"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Host implements Serializable
{

    /**
     * The Dataset Distribution Host Availability Schema
     * <p>
     * Availability
     * 
     */
    @JsonProperty("availability")
    @JsonPropertyDescription("Availability")
    private String availability;
    /**
     * The Dataset Distribution Host Backup Frequency Schema
     * <p>
     * Backup Frequency
     * 
     */
    @JsonProperty("backup_frequency")
    @JsonPropertyDescription("Backup Frequency")
    private String backupFrequency;
    /**
     * The Dataset Distribution Host Backup Type Schema
     * <p>
     * Backup Type
     * 
     */
    @JsonProperty("backup_type")
    @JsonPropertyDescription("Backup Type")
    private String backupType;
    /**
     * The Dataset Distribution Host Certification Type Schema
     * <p>
     * Repository certified to a recognised standard. Allowed values: din31644, dini-zertifikat, dsa, iso16363, iso16919, trac, wds, coretrustseal
     * 
     */
    @JsonProperty("certified_with")
    @JsonPropertyDescription("Repository certified to a recognised standard. Allowed values: din31644, dini-zertifikat, dsa, iso16363, iso16919, trac, wds, coretrustseal")
    private CertifiedWith certifiedWith;
    /**
     * The Dataset Distribution Host Description Schema
     * <p>
     * Description
     * 
     */
    @JsonProperty("description")
    @JsonPropertyDescription("Description")
    private String description;
    /**
     * The Dataset Distribution Host Geographical Location Schema
     * <p>
     * Physical location of the data expressed using ISO 3166-1 country code.
     * 
     */
    @JsonProperty("geo_location")
    @JsonPropertyDescription("Physical location of the data expressed using ISO 3166-1 country code.")
    private GeoLocation geoLocation;
    /**
     * The Dataset Distribution Host PID System Schema
     * <p>
     * PID system(s). Allowed values: ark, arxiv, bibcode, doi, ean13, eissn, handle, igsn, isbn, issn, istc, lissn, lsid, pmid, purl, upc, url, urn, other
     * 
     */
    @JsonProperty("pid_system")
    @JsonPropertyDescription("PID system(s). Allowed values: ark, arxiv, bibcode, doi, ean13, eissn, handle, igsn, isbn, issn, istc, lissn, lsid, pmid, purl, upc, url, urn, other")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<PidSystem> pidSystem = null;
    /**
     * The Dataset Distribution Host Storage Type Schema
     * <p>
     * The type of storage required
     * 
     */
    @JsonProperty("storage_type")
    @JsonPropertyDescription("The type of storage required")
    private String storageType;
    /**
     * The Dataset Distribution Host Support Versioning Schema
     * <p>
     * If host supports versioning. Allowed values: yes, no, unknown
     * 
     */
    @JsonProperty("support_versioning")
    @JsonPropertyDescription("If host supports versioning. Allowed values: yes, no, unknown")
    private SupportVersioning supportVersioning;
    /**
     * The Dataset Distribution Host Title Schema
     * <p>
     * Title
     * (Required)
     * 
     */
    @JsonProperty("title")
    @JsonPropertyDescription("Title")
    private String title;
    /**
     * The Dataset Distribution Host Title Schema
     * <p>
     * The URL of the system hosting a distribution of a dataset
     * (Required)
     * 
     */
    @JsonProperty("url")
    @JsonPropertyDescription("The URL of the system hosting a distribution of a dataset")
    private URI url;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = 8564338806797654115L;

    /**
     * The Dataset Distribution Host Availability Schema
     * <p>
     * Availability
     * 
     */
    @JsonProperty("availability")
    public String getAvailability() {
        return availability;
    }

    /**
     * The Dataset Distribution Host Availability Schema
     * <p>
     * Availability
     * 
     */
    @JsonProperty("availability")
    public void setAvailability(String availability) {
        this.availability = availability;
    }

    /**
     * The Dataset Distribution Host Backup Frequency Schema
     * <p>
     * Backup Frequency
     * 
     */
    @JsonProperty("backup_frequency")
    public String getBackupFrequency() {
        return backupFrequency;
    }

    /**
     * The Dataset Distribution Host Backup Frequency Schema
     * <p>
     * Backup Frequency
     * 
     */
    @JsonProperty("backup_frequency")
    public void setBackupFrequency(String backupFrequency) {
        this.backupFrequency = backupFrequency;
    }

    /**
     * The Dataset Distribution Host Backup Type Schema
     * <p>
     * Backup Type
     * 
     */
    @JsonProperty("backup_type")
    public String getBackupType() {
        return backupType;
    }

    /**
     * The Dataset Distribution Host Backup Type Schema
     * <p>
     * Backup Type
     * 
     */
    @JsonProperty("backup_type")
    public void setBackupType(String backupType) {
        this.backupType = backupType;
    }

    /**
     * The Dataset Distribution Host Certification Type Schema
     * <p>
     * Repository certified to a recognised standard. Allowed values: din31644, dini-zertifikat, dsa, iso16363, iso16919, trac, wds, coretrustseal
     * 
     */
    @JsonProperty("certified_with")
    public CertifiedWith getCertifiedWith() {
        return certifiedWith;
    }

    /**
     * The Dataset Distribution Host Certification Type Schema
     * <p>
     * Repository certified to a recognised standard. Allowed values: din31644, dini-zertifikat, dsa, iso16363, iso16919, trac, wds, coretrustseal
     * 
     */
    @JsonProperty("certified_with")
    public void setCertifiedWith(CertifiedWith certifiedWith) {
        this.certifiedWith = certifiedWith;
    }

    /**
     * The Dataset Distribution Host Description Schema
     * <p>
     * Description
     * 
     */
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    /**
     * The Dataset Distribution Host Description Schema
     * <p>
     * Description
     * 
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The Dataset Distribution Host Geographical Location Schema
     * <p>
     * Physical location of the data expressed using ISO 3166-1 country code.
     * 
     */
    @JsonProperty("geo_location")
    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    /**
     * The Dataset Distribution Host Geographical Location Schema
     * <p>
     * Physical location of the data expressed using ISO 3166-1 country code.
     * 
     */
    @JsonProperty("geo_location")
    public void setGeoLocation(GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    /**
     * The Dataset Distribution Host PID System Schema
     * <p>
     * PID system(s). Allowed values: ark, arxiv, bibcode, doi, ean13, eissn, handle, igsn, isbn, issn, istc, lissn, lsid, pmid, purl, upc, url, urn, other
     * 
     */
    @JsonProperty("pid_system")
    public List<PidSystem> getPidSystem() {
        return pidSystem;
    }

    /**
     * The Dataset Distribution Host PID System Schema
     * <p>
     * PID system(s). Allowed values: ark, arxiv, bibcode, doi, ean13, eissn, handle, igsn, isbn, issn, istc, lissn, lsid, pmid, purl, upc, url, urn, other
     * 
     */
    @JsonProperty("pid_system")
    public void setPidSystem(List<PidSystem> pidSystem) {
        this.pidSystem = pidSystem;
    }

    /**
     * The Dataset Distribution Host Storage Type Schema
     * <p>
     * The type of storage required
     * 
     */
    @JsonProperty("storage_type")
    public String getStorageType() {
        return storageType;
    }

    /**
     * The Dataset Distribution Host Storage Type Schema
     * <p>
     * The type of storage required
     * 
     */
    @JsonProperty("storage_type")
    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    /**
     * The Dataset Distribution Host Support Versioning Schema
     * <p>
     * If host supports versioning. Allowed values: yes, no, unknown
     * 
     */
    @JsonProperty("support_versioning")
    public SupportVersioning getSupportVersioning() {
        return supportVersioning;
    }

    /**
     * The Dataset Distribution Host Support Versioning Schema
     * <p>
     * If host supports versioning. Allowed values: yes, no, unknown
     * 
     */
    @JsonProperty("support_versioning")
    public void setSupportVersioning(SupportVersioning supportVersioning) {
        this.supportVersioning = supportVersioning;
    }

    /**
     * The Dataset Distribution Host Title Schema
     * <p>
     * Title
     * (Required)
     * 
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     * The Dataset Distribution Host Title Schema
     * <p>
     * Title
     * (Required)
     * 
     */
    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * The Dataset Distribution Host Title Schema
     * <p>
     * The URL of the system hosting a distribution of a dataset
     * (Required)
     * 
     */
    @JsonProperty("url")
    public URI getUrl() {
        return url;
    }

    /**
     * The Dataset Distribution Host Title Schema
     * <p>
     * The URL of the system hosting a distribution of a dataset
     * (Required)
     * 
     */
    @JsonProperty("url")
    public void setUrl(URI url) {
        this.url = url;
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

    public enum CertifiedWith {

        DIN_31644("din31644"),
        DINI_ZERTIFIKAT("dini-zertifikat"),
        DSA("dsa"),
        ISO_16363("iso16363"),
        ISO_16919("iso16919"),
        TRAC("trac"),
        WDS("wds"),
        CORETRUSTSEAL("coretrustseal");
        private final String value;
        private final static Map<String, CertifiedWith> CONSTANTS = new HashMap<String, CertifiedWith>();

        static {
            for (CertifiedWith c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        CertifiedWith(String value) {
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
        public static CertifiedWith fromValue(String value) {
            CertifiedWith constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new MyApplicationException(value);
            } else {
                return constant;
            }
        }

    }

    public enum GeoLocation {

        AD("AD"),
        AE("AE"),
        AF("AF"),
        AG("AG"),
        AI("AI"),
        AL("AL"),
        AM("AM"),
        AO("AO"),
        AQ("AQ"),
        AR("AR"),
        AS("AS"),
        AT("AT"),
        AU("AU"),
        AW("AW"),
        AX("AX"),
        AZ("AZ"),
        BA("BA"),
        BB("BB"),
        BD("BD"),
        BE("BE"),
        BF("BF"),
        BG("BG"),
        BH("BH"),
        BI("BI"),
        BJ("BJ"),
        BL("BL"),
        BM("BM"),
        BN("BN"),
        BO("BO"),
        BQ("BQ"),
        BR("BR"),
        BS("BS"),
        BT("BT"),
        BV("BV"),
        BW("BW"),
        BY("BY"),
        BZ("BZ"),
        CA("CA"),
        CC("CC"),
        CD("CD"),
        CF("CF"),
        CG("CG"),
        CH("CH"),
        CI("CI"),
        CK("CK"),
        CL("CL"),
        CM("CM"),
        CN("CN"),
        CO("CO"),
        CR("CR"),
        CU("CU"),
        CV("CV"),
        CW("CW"),
        CX("CX"),
        CY("CY"),
        CZ("CZ"),
        DE("DE"),
        DJ("DJ"),
        DK("DK"),
        DM("DM"),
        DO("DO"),
        DZ("DZ"),
        EC("EC"),
        EE("EE"),
        EG("EG"),
        EH("EH"),
        ER("ER"),
        ES("ES"),
        ET("ET"),
        FI("FI"),
        FJ("FJ"),
        FK("FK"),
        FM("FM"),
        FO("FO"),
        FR("FR"),
        GA("GA"),
        GB("GB"),
        GD("GD"),
        GE("GE"),
        GF("GF"),
        GG("GG"),
        GH("GH"),
        GI("GI"),
        GL("GL"),
        GM("GM"),
        GN("GN"),
        GP("GP"),
        GQ("GQ"),
        GR("GR"),
        GS("GS"),
        GT("GT"),
        GU("GU"),
        GW("GW"),
        GY("GY"),
        HK("HK"),
        HM("HM"),
        HN("HN"),
        HR("HR"),
        HT("HT"),
        HU("HU"),
        ID("ID"),
        IE("IE"),
        IL("IL"),
        IM("IM"),
        IN("IN"),
        IO("IO"),
        IQ("IQ"),
        IR("IR"),
        IS("IS"),
        IT("IT"),
        JE("JE"),
        JM("JM"),
        JO("JO"),
        JP("JP"),
        KE("KE"),
        KG("KG"),
        KH("KH"),
        KI("KI"),
        KM("KM"),
        KN("KN"),
        KP("KP"),
        KR("KR"),
        KW("KW"),
        KY("KY"),
        KZ("KZ"),
        LA("LA"),
        LB("LB"),
        LC("LC"),
        LI("LI"),
        LK("LK"),
        LR("LR"),
        LS("LS"),
        LT("LT"),
        LU("LU"),
        LV("LV"),
        LY("LY"),
        MA("MA"),
        MC("MC"),
        MD("MD"),
        ME("ME"),
        MF("MF"),
        MG("MG"),
        MH("MH"),
        MK("MK"),
        ML("ML"),
        MM("MM"),
        MN("MN"),
        MO("MO"),
        MP("MP"),
        MQ("MQ"),
        MR("MR"),
        MS("MS"),
        MT("MT"),
        MU("MU"),
        MV("MV"),
        MW("MW"),
        MX("MX"),
        MY("MY"),
        MZ("MZ"),
        NA("NA"),
        NC("NC"),
        NE("NE"),
        NF("NF"),
        NG("NG"),
        NI("NI"),
        NL("NL"),
        NO("NO"),
        NP("NP"),
        NR("NR"),
        NU("NU"),
        NZ("NZ"),
        OM("OM"),
        PA("PA"),
        PE("PE"),
        PF("PF"),
        PG("PG"),
        PH("PH"),
        PK("PK"),
        PL("PL"),
        PM("PM"),
        PN("PN"),
        PR("PR"),
        PS("PS"),
        PT("PT"),
        PW("PW"),
        PY("PY"),
        QA("QA"),
        RE("RE"),
        RO("RO"),
        RS("RS"),
        RU("RU"),
        RW("RW"),
        SA("SA"),
        SB("SB"),
        SC("SC"),
        SD("SD"),
        SE("SE"),
        SG("SG"),
        SH("SH"),
        SI("SI"),
        SJ("SJ"),
        SK("SK"),
        SL("SL"),
        SM("SM"),
        SN("SN"),
        SO("SO"),
        SR("SR"),
        SS("SS"),
        ST("ST"),
        SV("SV"),
        SX("SX"),
        SY("SY"),
        SZ("SZ"),
        TC("TC"),
        TD("TD"),
        TF("TF"),
        TG("TG"),
        TH("TH"),
        TJ("TJ"),
        TK("TK"),
        TL("TL"),
        TM("TM"),
        TN("TN"),
        TO("TO"),
        TR("TR"),
        TT("TT"),
        TV("TV"),
        TW("TW"),
        TZ("TZ"),
        UA("UA"),
        UG("UG"),
        UM("UM"),
        US("US"),
        UY("UY"),
        UZ("UZ"),
        VA("VA"),
        VC("VC"),
        VE("VE"),
        VG("VG"),
        VI("VI"),
        VN("VN"),
        VU("VU"),
        WF("WF"),
        WS("WS"),
        YE("YE"),
        YT("YT"),
        ZA("ZA"),
        ZM("ZM"),
        ZW("ZW");
        private final String value;
        private final static Map<String, GeoLocation> CONSTANTS = new HashMap<String, GeoLocation>();

        static {
            for (GeoLocation c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        GeoLocation(String value) {
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
        public static GeoLocation fromValue(String value) {
            GeoLocation constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new MyApplicationException(value);
            } else {
                return constant;
            }
        }

    }

    public enum SupportVersioning {

        YES("yes"),
        NO("no"),
        UNKNOWN("unknown");
        private final String value;
        private final static Map<String, SupportVersioning> CONSTANTS = new HashMap<String, SupportVersioning>();

        static {
            for (SupportVersioning c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        SupportVersioning(String value) {
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
        public static SupportVersioning fromValue(String value) {
            SupportVersioning constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new MyApplicationException(value);
            } else {
                return constant;
            }
        }

    }

}
