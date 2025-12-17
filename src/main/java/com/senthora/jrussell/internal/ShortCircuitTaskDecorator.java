package com.senthora.jrussell.task;

import com.senthora.jrussell.api.TestTaskDecorator;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.BooleanSupplier;

/**
 * Prevents task execution before the task body is entered.
 * <p>
 * This decorator is intended to be used as a simple execution-time
 * gate that conditionally prevents a task from being executed.
 * <p>
 * <h3>Internal Guarantees</h3>
 * <ul>
 *     <li>The short-circuit condition is evaluated at execution time.</li>
 *     <li>The task body is never entered when short-circuited.</li>
 *     <li>The task is not invoked when the condition evaluates to {@code true}.</li>
 *     <li>The decorator maintains no internal state.</li>
 * </ul>
 * <p>
 * <h3>Usage Guidelines</h3>
 * <ul>
 *     <li>Ensure the supplied condition is fast and side-effect free
 *     when used in performance-sensitive or concurrent execution.</li>
 * </ul>
 */
@SuppressWarnings("ClassCanBeRecord")
public final class ShortCircuitTaskDecorator implements TestTaskDecorator {

    private final BooleanSupplier condition;

    ShortCircuitTaskDecorator(BooleanSupplier condition) {
        this.condition = Objects.requireNonNull(condition, "condition must not be null");
    }

    @Override
    public <T> Callable<T> decorate(Callable<T> task) {
        Objects.requireNonNull(task, "task must not be null");
        return () -> {
            if (condition.getAsBoolean()) {
                throw new RuntimeException("Task execution was short-circuited");
            }
            return task.call();
        };
    }
}
