package org.opencdmp.filetransformer.rda.service.rdafiletransformer;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({RdaFileTransformerServiceProperties.class})
public class RdaFileTransformerServiceConfiguration {
	private final RdaFileTransformerServiceProperties rdaFileTransformerServiceProperties;

	public RdaFileTransformerServiceConfiguration(RdaFileTransformerServiceProperties rdaFileTransformerServiceProperties) {
		this.rdaFileTransformerServiceProperties = rdaFileTransformerServiceProperties;
	}

	public RdaFileTransformerServiceProperties getRdaFileTransformerServiceProperties() {
		return rdaFileTransformerServiceProperties;
	}
}
