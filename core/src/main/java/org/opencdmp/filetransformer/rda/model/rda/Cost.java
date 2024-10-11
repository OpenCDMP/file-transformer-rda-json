
package org.opencdmp.filetransformer.rda.model.rda;

import com.fasterxml.jackson.annotation.*;
import gr.cite.tools.exception.MyApplicationException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * The Cost Items Schema
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "currency_code",
    "description",
    "title",
    "value"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Cost implements Serializable
{

    /**
     * The Cost Currency Code Schema
     * <p>
     * Allowed values defined by ISO 4217
     * 
     */
    @JsonProperty("currency_code")
    @JsonPropertyDescription("Allowed values defined by ISO 4217")
    private CurrencyCode currencyCode;
    /**
     * The Cost Description Schema
     * <p>
     * Cost(s) Description
     * 
     */
    @JsonProperty("description")
    @JsonPropertyDescription("Cost(s) Description")
    private String description;
    /**
     * The Cost Title Schema
     * <p>
     * Title
     * (Required)
     * 
     */
    @JsonProperty("title")
    @JsonPropertyDescription("Title")
    private String title;
    /**
     * The Cost Value Schema
     * <p>
     * Value
     * 
     */
    @JsonProperty("value")
    @JsonPropertyDescription("Value")
    private Double value;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = -322637784848035165L;

    /**
     * The Cost Currency Code Schema
     * <p>
     * Allowed values defined by ISO 4217
     * 
     */
    @JsonProperty("currency_code")
    public CurrencyCode getCurrencyCode() {
        return currencyCode;
    }

    /**
     * The Cost Currency Code Schema
     * <p>
     * Allowed values defined by ISO 4217
     * 
     */
    @JsonProperty("currency_code")
    public void setCurrencyCode(CurrencyCode currencyCode) {
        this.currencyCode = currencyCode;
    }

    /**
     * The Cost Description Schema
     * <p>
     * Cost(s) Description
     * 
     */
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    /**
     * The Cost Description Schema
     * <p>
     * Cost(s) Description
     * 
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The Cost Title Schema
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
     * The Cost Title Schema
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
     * The Cost Value Schema
     * <p>
     * Value
     * 
     */
    @JsonProperty("value")
    public Double getValue() {
        return value;
    }

    /**
     * The Cost Value Schema
     * <p>
     * Value
     * 
     */
    @JsonProperty("value")
    public void setValue(Double value) {
        this.value = value;
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

    public enum CurrencyCode {

        AED("AED"),
        AFN("AFN"),
        ALL("ALL"),
        AMD("AMD"),
        ANG("ANG"),
        AOA("AOA"),
        ARS("ARS"),
        AUD("AUD"),
        AWG("AWG"),
        AZN("AZN"),
        BAM("BAM"),
        BBD("BBD"),
        BDT("BDT"),
        BGN("BGN"),
        BHD("BHD"),
        BIF("BIF"),
        BMD("BMD"),
        BND("BND"),
        BOB("BOB"),
        BRL("BRL"),
        BSD("BSD"),
        BTN("BTN"),
        BWP("BWP"),
        BYN("BYN"),
        BZD("BZD"),
        CAD("CAD"),
        CDF("CDF"),
        CHF("CHF"),
        CLP("CLP"),
        CNY("CNY"),
        COP("COP"),
        CRC("CRC"),
        CUC("CUC"),
        CUP("CUP"),
        CVE("CVE"),
        CZK("CZK"),
        DJF("DJF"),
        DKK("DKK"),
        DOP("DOP"),
        DZD("DZD"),
        EGP("EGP"),
        ERN("ERN"),
        ETB("ETB"),
        EUR("EUR"),
        FJD("FJD"),
        FKP("FKP"),
        GBP("GBP"),
        GEL("GEL"),
        GGP("GGP"),
        GHS("GHS"),
        GIP("GIP"),
        GMD("GMD"),
        GNF("GNF"),
        GTQ("GTQ"),
        GYD("GYD"),
        HKD("HKD"),
        HNL("HNL"),
        HRK("HRK"),
        HTG("HTG"),
        HUF("HUF"),
        IDR("IDR"),
        ILS("ILS"),
        IMP("IMP"),
        INR("INR"),
        IQD("IQD"),
        IRR("IRR"),
        ISK("ISK"),
        JEP("JEP"),
        JMD("JMD"),
        JOD("JOD"),
        JPY("JPY"),
        KES("KES"),
        KGS("KGS"),
        KHR("KHR"),
        KMF("KMF"),
        KPW("KPW"),
        KRW("KRW"),
        KWD("KWD"),
        KYD("KYD"),
        KZT("KZT"),
        LAK("LAK"),
        LBP("LBP"),
        LKR("LKR"),
        LRD("LRD"),
        LSL("LSL"),
        LYD("LYD"),
        MAD("MAD"),
        MDL("MDL"),
        MGA("MGA"),
        MKD("MKD"),
        MMK("MMK"),
        MNT("MNT"),
        MOP("MOP"),
        MRU("MRU"),
        MUR("MUR"),
        MVR("MVR"),
        MWK("MWK"),
        MXN("MXN"),
        MYR("MYR"),
        MZN("MZN"),
        NAD("NAD"),
        NGN("NGN"),
        NIO("NIO"),
        NOK("NOK"),
        NPR("NPR"),
        NZD("NZD"),
        OMR("OMR"),
        PAB("PAB"),
        PEN("PEN"),
        PGK("PGK"),
        PHP("PHP"),
        PKR("PKR"),
        PLN("PLN"),
        PYG("PYG"),
        QAR("QAR"),
        RON("RON"),
        RSD("RSD"),
        RUB("RUB"),
        RWF("RWF"),
        SAR("SAR"),
        SBD("SBD"),
        SCR("SCR"),
        SDG("SDG"),
        SEK("SEK"),
        SGD("SGD"),
        SHP("SHP"),
        SLL("SLL"),
        SOS("SOS"),
        SPL("SPL*"),
        SRD("SRD"),
        STN("STN"),
        SVC("SVC"),
        SYP("SYP"),
        SZL("SZL"),
        THB("THB"),
        TJS("TJS"),
        TMT("TMT"),
        TND("TND"),
        TOP("TOP"),
        TRY("TRY"),
        TTD("TTD"),
        TVD("TVD"),
        TWD("TWD"),
        TZS("TZS"),
        UAH("UAH"),
        UGX("UGX"),
        USD("USD"),
        UYU("UYU"),
        UZS("UZS"),
        VEF("VEF"),
        VND("VND"),
        VUV("VUV"),
        WST("WST"),
        XAF("XAF"),
        XCD("XCD"),
        XDR("XDR"),
        XOF("XOF"),
        XPF("XPF"),
        YER("YER"),
        ZAR("ZAR"),
        ZMW("ZMW"),
        ZWD("ZWD");
        private final String value;
        private final static Map<String, CurrencyCode> CONSTANTS = new HashMap<String, CurrencyCode>();

        static {
            for (CurrencyCode c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        CurrencyCode(String value) {
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
        public static CurrencyCode fromValue(String value) {
            CurrencyCode constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new MyApplicationException(value);
            } else {
                return constant;
            }
        }

    }

}
