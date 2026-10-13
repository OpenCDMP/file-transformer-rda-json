package org.opencdmp.filetransformer.rda.fixturecapture;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fixture-capture")
public class FixtureCaptureProperties {
	private boolean enabled;
	private String directory;
	private int maxBodyBytes = 50 * 1024 * 1024;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public int getMaxBodyBytes() {
		return maxBodyBytes;
	}

	public void setMaxBodyBytes(int maxBodyBytes) {
		this.maxBodyBytes = maxBodyBytes;
	}
}
