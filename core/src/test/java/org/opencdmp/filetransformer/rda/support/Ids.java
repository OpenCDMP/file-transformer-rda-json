package org.opencdmp.filetransformer.rda.support;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** Stable UUIDs derived from readable names, so snapshots do not change between runs. */
public final class Ids {

    private Ids() {
    }

    public static UUID of(String name) {
        return UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8));
    }
}
