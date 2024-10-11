package org.opencdmp.filetransformer.rda.service.rdafiletransformer;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "rda-file-transformer")
public class RdaFileTransformerServiceProperties {
	private String transformerId;
	private boolean useSharedStorage;
	private String organizationReferenceCode;
	private String grantReferenceCode;
	private String funderReferenceCode;
	private String researcherReferenceCode;
	private String licenceReferenceCode;
	private String projectReferenceCode;
	private String datasetReferenceCode;
	private String publicationReferenceCode;
	private String researcherMalCode;
	private String projectStartDateCode;
	private String projectEndDateCode;
	private String licenseStartDateCode;

	private Map<String, String> languageMap;


	public String getTransformerId() {
		return transformerId;
	}

	public void setTransformerId(String transformerId) {
		this.transformerId = transformerId;
	}

	public boolean isUseSharedStorage() {
		return useSharedStorage;
	}

	public void setUseSharedStorage(boolean useSharedStorage) {
		this.useSharedStorage = useSharedStorage;
	}

	public String getOrganizationReferenceCode() {
		return organizationReferenceCode;
	}

	public void setOrganizationReferenceCode(String organizationReferenceCode) {
		this.organizationReferenceCode = organizationReferenceCode;
	}

	public String getGrantReferenceCode() {
		return grantReferenceCode;
	}

	public void setGrantReferenceCode(String grantReferenceCode) {
		this.grantReferenceCode = grantReferenceCode;
	}

	public String getFunderReferenceCode() {
		return funderReferenceCode;
	}

	public void setFunderReferenceCode(String funderReferenceCode) {
		this.funderReferenceCode = funderReferenceCode;
	}

	public String getResearcherReferenceCode() {
		return researcherReferenceCode;
	}

	public void setResearcherReferenceCode(String researcherReferenceCode) {
		this.researcherReferenceCode = researcherReferenceCode;
	}

	public String getProjectReferenceCode() {
		return projectReferenceCode;
	}

	public void setProjectReferenceCode(String projectReferenceCode) {
		this.projectReferenceCode = projectReferenceCode;
	}

	public String getLicenceReferenceCode() {
		return licenceReferenceCode;
	}

	public void setLicenceReferenceCode(String licenceReferenceCode) {
		this.licenceReferenceCode = licenceReferenceCode;
	}

	public String getDatasetReferenceCode() {
		return datasetReferenceCode;
	}

	public void setDatasetReferenceCode(String datasetReferenceCode) {
		this.datasetReferenceCode = datasetReferenceCode;
	}

	public String getPublicationReferenceCode() {
		return publicationReferenceCode;
	}

	public void setPublicationReferenceCode(String publicationReferenceCode) {
		this.publicationReferenceCode = publicationReferenceCode;
	}

	public String getResearcherMalCode() {
		return researcherMalCode;
	}

	public String getProjectStartDateCode() {
		return projectStartDateCode;
	}

	public void setProjectStartDateCode(String projectStartDateCode) {
		this.projectStartDateCode = projectStartDateCode;
	}

	public String getProjectEndDateCode() {
		return projectEndDateCode;
	}

	public void setProjectEndDateCode(String projectEndDateCode) {
		this.projectEndDateCode = projectEndDateCode;
	}

	public void setResearcherMalCode(String researcherMalCode) {
		this.researcherMalCode = researcherMalCode;
	}

	public Map<String, String> getLanguageMap() {
		return languageMap;
	}

	public void setLanguageMap(Map<String, String> languageMap) {
		this.languageMap = languageMap;
	}

	public String getLicenseStartDateCode() {
		return licenseStartDateCode;
	}

	public void setLicenseStartDateCode(String licenseStartDateCode) {
		this.licenseStartDateCode = licenseStartDateCode;
	}
}
