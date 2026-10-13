package org.opencdmp.filetransformer.rda.support;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** A readable list of differences between two JSON documents, one line per changed path. */
public final class JsonDiff {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private JsonDiff() {
    }

    public static String between(String before, String after) {
        List<String> lines = new ArrayList<>();
        diff("$", MAPPER.readTree(before), MAPPER.readTree(after), lines);
        return lines.isEmpty() ? "(identical)\n" : String.join("\n", lines) + "\n";
    }

    private static void diff(String path, JsonNode before, JsonNode after, List<String> lines) {
        if (before == null || before.isMissingNode()) {
            lines.add(path + ": added " + compact(after));
        } else if (after == null || after.isMissingNode()) {
            lines.add(path + ": removed " + compact(before));
        } else if (before.isObject() && after.isObject()) {
            Set<String> keys = new TreeSet<>(before.propertyNames());
            keys.addAll(after.propertyNames());
            for (String key : keys) diff(path + "." + key, before.path(key), after.path(key), lines);
        } else if (before.isArray() && after.isArray()) {
            int size = Math.max(before.size(), after.size());
            for (int i = 0; i < size; i++) diff(path + "[" + i + "]", before.path(i), after.path(i), lines);
        } else if (!before.equals(after)) {
            lines.add(path + ": " + compact(before) + " -> " + compact(after));
        }
    }

    private static String compact(JsonNode node) {
        String text = MAPPER.writeValueAsString(node);
        return text.length() > 160 ? text.substring(0, 157) + "..." : text;
    }
}
