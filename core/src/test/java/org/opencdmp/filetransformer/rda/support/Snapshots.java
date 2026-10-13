package org.opencdmp.filetransformer.rda.support;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Approved-output files under src/test/resources/snapshots.
 * <p>
 * A missing snapshot is written and the test fails, so new output is always looked at once.
 * Run with {@code -Dsnapshots.update=true} to accept intended changes; the diff then shows up in git.
 */
public final class Snapshots {

    private static final Path ROOT = Path.of("src", "test", "resources", "snapshots");
    private static final boolean UPDATE = Boolean.getBoolean("snapshots.update");
    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private Snapshots() {
    }

    public static void assertMatches(String fileName, String actual) {
        Path file = ROOT.resolve(fileName);
        String normalized = normalizeLineEndings(actual);
        try {
            if (UPDATE || !Files.exists(file)) {
                Files.createDirectories(file.getParent());
                Files.writeString(file, normalized, StandardCharsets.UTF_8);
                if (!UPDATE) fail("Snapshot " + file + " did not exist and has been written. Review it and run again.");
                return;
            }
            String expected = normalizeLineEndings(Files.readString(file, StandardCharsets.UTF_8));
            assertThat(normalized)
                    .as("Snapshot %s differs. If the change is intended, run with -Dsnapshots.update=true and review the git diff.", file)
                    .isEqualTo(expected);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * For the outcome a scenario did not have (a failure file when it succeeded, or the other way round):
     * a leftover file from an earlier outcome is removed in update mode and fails the test otherwise.
     */
    public static void assertAbsent(String... fileNames) {
        for (String fileName : fileNames) {
            Path file = ROOT.resolve(fileName);
            if (!Files.exists(file)) continue;
            try {
                if (UPDATE) {
                    Files.delete(file);
                } else {
                    fail("Snapshot " + file + " is left over from an earlier outcome. Run with -Dsnapshots.update=true to remove it.");
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    /** Pretty JSON with object keys sorted, so key order never shows up as a change. Array order is kept. */
    public static String canonicalJson(String json, boolean dropNulls) {
        return canonicalJson(MAPPER.readTree(json), dropNulls);
    }

    public static String canonicalJson(Object value, boolean dropNulls) {
        JsonNode tree = value instanceof JsonNode node ? node : MAPPER.valueToTree(value);
        return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(sorted(tree, dropNulls)) + "\n";
    }

    private static Object sorted(JsonNode node, boolean dropNulls) {
        if (node.isObject()) {
            Map<String, Object> map = new TreeMap<>();
            for (Map.Entry<String, JsonNode> entry : node.properties()) {
                if (dropNulls && entry.getValue().isNull()) continue;
                map.put(entry.getKey(), sorted(entry.getValue(), dropNulls));
            }
            return map;
        }
        if (node.isArray()) {
            List<Object> list = new ArrayList<>();
            for (JsonNode element : node) list.add(sorted(element, dropNulls));
            return list;
        }
        return node;
    }

    private static String normalizeLineEndings(String text) {
        String unix = text.replace("\r\n", "\n");
        return unix.endsWith("\n") ? unix : unix + "\n";
    }
}
