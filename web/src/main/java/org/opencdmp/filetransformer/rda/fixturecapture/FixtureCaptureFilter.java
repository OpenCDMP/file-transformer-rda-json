package org.opencdmp.filetransformer.rda.fixturecapture;

import gr.cite.tools.logging.LoggerService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Writes each POST body under /api/file-transformer/ to the capture directory, named after the time and the
 * endpoint (e.g. 20260925-143012-001-export-plan.json). The request itself is passed through untouched.
 * <p>
 * Captured plans hold real answers and user details: anonymise them before adding one to the test fixtures.
 */
public class FixtureCaptureFilter extends OncePerRequestFilter {
	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(FixtureCaptureFilter.class));
	private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

	private final FixtureCaptureProperties properties;
	private final AtomicInteger counter = new AtomicInteger();

	public FixtureCaptureFilter(FixtureCaptureProperties properties) {
		this.properties = properties;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		if (!"POST".equalsIgnoreCase(request.getMethod())) {
			filterChain.doFilter(request, response);
			return;
		}
		ContentCachingRequestWrapper wrapper = new ContentCachingRequestWrapper(request, this.properties.getMaxBodyBytes());
		try {
			filterChain.doFilter(wrapper, response);
		} finally {
			this.save(wrapper);
		}
	}

	private void save(ContentCachingRequestWrapper request) {
		byte[] body = request.getContentAsByteArray();
		if (body.length == 0) return;
		String endpoint = request.getRequestURI().replaceFirst(".*/api/file-transformer/?", "").replaceAll("[^A-Za-z0-9]+", "-");
		String name = String.format("%s-%03d-%s.json", LocalDateTime.now().format(TIMESTAMP), this.counter.incrementAndGet() % 1000, endpoint.isBlank() ? "request" : endpoint);
		try {
			Path directory = Path.of(this.properties.getDirectory());
			Files.createDirectories(directory);
			Path file = directory.resolve(name);
			Files.write(file, body);
			logger.warn("fixture capture: saved request body to " + file.toAbsolutePath());
		} catch (IOException e) {
			logger.error("fixture capture: could not save " + name + ": " + e.getMessage(), e);
		}
	}
}
