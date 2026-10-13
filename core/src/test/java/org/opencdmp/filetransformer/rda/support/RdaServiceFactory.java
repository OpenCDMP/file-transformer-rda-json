package org.opencdmp.filetransformer.rda.support;

import org.opencdmp.filetransformer.rda.convention.ConventionServiceImpl;
import org.opencdmp.filetransformer.rda.service.descriptiontemplatesearcher.TemplateFieldSearcherServiceImpl;
import org.opencdmp.filetransformer.rda.service.json.JsonHandlingService;
import org.opencdmp.filetransformer.rda.service.planblueprintsearcher.PlanBlueprintFieldSearcherServiceImpl;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaFileTransformerService;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaFileTransformerServiceConfiguration;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaFileTransformerServiceProperties;
import org.opencdmp.filetransformer.rda.service.storage.FileStorageService;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.PropertySourcesPlaceholdersResolver;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Builds the real {@link RdaFileTransformerService} without a Spring context, bound to the same
 * rda-file-transformer.yml the web module ships, so tests see production reference codes and language map.
 * Environment variables are deliberately not consulted: placeholders resolve to their defaults only.
 */
public final class RdaServiceFactory {

    private static final Path CONFIG = Path.of("web", "src", "main", "resources", "config", "rda-file-transformer.yml");

    private RdaServiceFactory() {
    }

    public static RdaFileTransformerService create() {
        RdaFileTransformerServiceProperties properties = loadProperties();
        return new RdaFileTransformerService(
                new InMemoryStorage(),
                new JsonHandlingService(),
                new RdaFileTransformerServiceConfiguration(properties),
                new TemplateFieldSearcherServiceImpl(),
                new PlanBlueprintFieldSearcherServiceImpl(),
                new ConventionServiceImpl());
    }

    private static RdaFileTransformerServiceProperties loadProperties() {
        Path config = Stream.of(Path.of("..").resolve(CONFIG), CONFIG)
                .filter(Files::exists)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Cannot find " + CONFIG + " from " + Path.of("").toAbsolutePath()));
        try {
            List<PropertySource<?>> sources = new YamlPropertySourceLoader().load("rda-file-transformer", new FileSystemResource(config));
            Binder binder = new Binder(ConfigurationPropertySources.from(sources), new PropertySourcesPlaceholdersResolver(sources));
            return binder.bind("rda-file-transformer", RdaFileTransformerServiceProperties.class)
                    .orElseThrow(() -> new IllegalStateException("No rda-file-transformer section in " + config));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Only used when useSharedStorage is on, which the default config leaves off. */
    private static final class InMemoryStorage implements FileStorageService {
        private final java.util.Map<String, byte[]> files = new java.util.HashMap<>();

        @Override
        public String storeFile(byte[] data) {
            String ref = "file-" + this.files.size();
            this.files.put(ref, data);
            return ref;
        }

        @Override
        public byte[] readFile(String fileRef) {
            return this.files.get(fileRef);
        }
    }
}
