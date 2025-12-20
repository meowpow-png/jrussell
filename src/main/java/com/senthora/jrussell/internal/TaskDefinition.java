package com.senthora.jrussell.internal;

import com.senthora.jrussell.api.TestTask;

import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Default implementation of {@link TestTask}.
 *
 * @implNote
 * Should not manage lifecycle, contain hooks,
 * or be aware of execution policies such
 * as timing,threading, or execution count.
 *
 * @see TestTaskBuilderImpl
 */
public final class TaskDefinition<T> implements TestTask {

    private final String id;
    private final Callable<T> executable;

    TaskDefinition(String id, Callable<T> executable) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.executable = Objects.requireNonNull(executable, "executable must not be null");
    }

    public String id() {
        return id;
    }

    /**
     * @implNote
     * This method is intentionally package-private.
     * Callers should not bypass runners, which are
     * the only legitimate executors of tasks.
     */
    Callable<T> executable() {
        return executable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TaskDefinition<?> other)) {
            return false;
        }
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Task: '%s'".formatted(id());
    }
}
