package org.opencdmp.filetransformer.rda.support;

import java.util.function.Supplier;

/**
 * The result of a transformer call, or the exception it threw. Known crashes are pinned like any other output,
 * so fixing one shows up as a snapshot change. Line numbers are left out on purpose: they move with every refactor.
 */
public record Outcome<T>(T value, String failure) {

    public static <T> Outcome<T> of(Supplier<T> call) {
        try {
            return new Outcome<>(call.get(), null);
        } catch (RuntimeException e) {
            Throwable cause = e;
            while (cause.getCause() != null && cause.getCause() != cause) cause = cause.getCause();
            return new Outcome<>(null, "fails with " + cause.getClass().getSimpleName() + ": " + cause.getMessage() + "\n");
        }
    }

    public boolean failed() {
        return this.failure != null;
    }
}
