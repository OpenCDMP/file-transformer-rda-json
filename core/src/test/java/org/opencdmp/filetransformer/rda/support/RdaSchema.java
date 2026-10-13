package org.opencdmp.filetransformer.rda.support;

import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * The official RDA DMP Common Standard 1.2 JSON schema (RDA-DMP-Common/RDA-DMP-Common-Standard,
 * examples/JSON/JSON-schema/1.2/maDMP-schema-1.2.json), copied unmodified into src/test/resources/schema.
 */
public final class RdaSchema {

    private static final Schema SCHEMA = load();

    private RdaSchema() {
    }

    /** One line per violation, "location: message", sorted so the report is stable. */
    public static String violations(String json) {
        List<Error> errors = SCHEMA.validate(json, InputFormat.JSON);
        if (errors.isEmpty()) return "(no violations)\n";
        return String.join("\n", errors.stream()
                .map(error -> error.getInstanceLocation() + ": " + error.getMessage())
                .sorted()
                .distinct()
                .toList()) + "\n";
    }

    private static Schema load() {
        try (InputStream stream = RdaSchema.class.getResourceAsStream("/schema/maDMP-schema-1.2.json")) {
            if (stream == null) throw new IllegalStateException("schema/maDMP-schema-1.2.json is missing from the test resources");
            return SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12).getSchema(stream, InputFormat.JSON);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
