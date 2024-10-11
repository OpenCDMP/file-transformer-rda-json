
package org.opencdmp.filetransformer.rda.model.rda;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import gr.cite.tools.exception.MyApplicationException;

import java.util.HashMap;
import java.util.Map;

public enum PidSystem {

    ARK("ark"),
    ARXIV("arxiv"),
    BIBCODE("bibcode"),
    DOI("doi"),
    EAN_13("ean13"),
    EISSN("eissn"),
    HANDLE("handle"),
    IGSN("igsn"),
    ISBN("isbn"),
    ISSN("issn"),
    ISTC("istc"),
    LISSN("lissn"),
    LSID("lsid"),
    PMID("pmid"),
    PURL("purl"),
    UPC("upc"),
    URL("url"),
    URN("urn"),
    OTHER("other");
    private final String value;
    private final static Map<String, PidSystem> CONSTANTS = new HashMap<String, PidSystem>();

    static {
        for (PidSystem c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    PidSystem(String value) {
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
    public static PidSystem fromValue(String value) {
        PidSystem constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new MyApplicationException(value);
        } else {
            return constant;
        }
    }

}
