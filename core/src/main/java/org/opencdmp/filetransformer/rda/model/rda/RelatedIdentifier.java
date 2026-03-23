
package org.opencdmp.filetransformer.rda.model.rda;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The DMP Project Items Schema
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "identifier",
    "metadata_scheme",
    "relation_type",
    "resource_type",
    "scheme_type",
    "scheme_uri",
    "type"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class RelatedIdentifier implements Serializable
{

    @JsonProperty("identifier")
    @JsonPropertyDescription("Value of the identifier")
    private String identifier;

    @JsonProperty("metadata_scheme")
    @JsonPropertyDescription("Name of the related metadata schema (if applicable)")
    private String metadataScheme;

    @JsonProperty("relation_type")
    @JsonPropertyDescription("Type of relation between the resource and the related resource")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String relationType;

    @JsonProperty("resource_type")
    @JsonPropertyDescription("Project start date")
    private String resourceType;

    @JsonProperty("scheme_type")
    @JsonPropertyDescription("Type of the related metadata scheme linked with scheme URI ")
    private String schemeType;

    @JsonProperty("scheme_uri")
    @JsonPropertyDescription("Link to the scheme of the identifier")
    private URI schemeUri;

    @JsonProperty("type")
    @JsonPropertyDescription("Type of the identifier")
    private String type;

    @JsonProperty("identifier")
    public String getIdentifier() {
        return identifier;
    }

    @JsonProperty("identifier")
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @JsonProperty("metadata_scheme")
    public String getMetadataScheme() {
        return metadataScheme;
    }

    @JsonProperty("metadata_scheme")
    public void setMetadataScheme(String metadataScheme) {
        this.metadataScheme = metadataScheme;
    }

    @JsonProperty("relation_type")
    public String getRelationType() {
        return relationType;
    }

    @JsonProperty("relation_type")
    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    @JsonProperty("resource_type")
    public String getResourceType() {
        return resourceType;
    }

    @JsonProperty("resource_type")
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    @JsonProperty("scheme_type")
    public String getSchemeType() {
        return schemeType;
    }

    @JsonProperty("scheme_type")
    public void setSchemeType(String schemeType) {
        this.schemeType = schemeType;
    }

    @JsonProperty("scheme_uri")
    public URI getSchemeUri() {
        return schemeUri;
    }

    @JsonProperty("scheme_uri")
    public void setSchemeUri(URI schemeUri) {
        this.schemeUri = schemeUri;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }


}
