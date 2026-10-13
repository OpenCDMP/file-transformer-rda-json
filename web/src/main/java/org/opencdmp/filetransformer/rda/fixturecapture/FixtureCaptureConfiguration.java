package org.opencdmp.filetransformer.rda.fixturecapture;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Development aid: saves the body of every export and import request so it can become a test fixture
 * (core/src/test/resources/fixtures). Off unless fixture-capture.enabled is true.
 */
@Configuration
@EnableConfigurationProperties(FixtureCaptureProperties.class)
@ConditionalOnProperty(prefix = "fixture-capture", name = "enabled", havingValue = "true")
public class FixtureCaptureConfiguration {

	@Bean
	public FilterRegistrationBean<FixtureCaptureFilter> fixtureCaptureFilter(FixtureCaptureProperties properties) {
		FilterRegistrationBean<FixtureCaptureFilter> registration = new FilterRegistrationBean<>(new FixtureCaptureFilter(properties));
		registration.addUrlPatterns("/api/file-transformer/*");
		return registration;
	}
}
