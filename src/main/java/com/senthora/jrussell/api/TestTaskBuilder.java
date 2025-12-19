package com.senthora.jrussell.api;

import java.time.Duration;
import java.util.function.BooleanSupplier;

/**
 * This interface represents a single-use task definition in progress.
 * <p>
 * It incrementally defines how a {@link TestTask} executes before
 * it is run. The builder represents the definition process, not the
 * task itself. Chaining methods wrap the underlying behavior, accumulating
 * execution effects in call order and applying them when the task is built.
 * <p>
 * The builder provides no way to observe, inspect, or modify the
 * internal task or decorator state. Callers cannot determine which
 * decorators were applied or how they are represented.
 * <h3>API Guarantees</h3>
 * <ul>
 *     <li>All chaining methods contribute to a single task definition.</li>
 *     <li>The task is guaranteed not to be executed during building.</li>
 *     <li>Decorators apply only to the task produced by this builder.</li>
 *     <li>Decorators are applied in the order they are added.</li>
 *     <li>Decorators are eagerly applied to the underlying task.</li>
 *     <li>Each builder instance may be used to build at most one task.</li>
 * </ul>
 * <h3>Usage Guidelines</h3>
 * <ul>
 *     <li>Avoid sharing builders across threads. Builders are mutable
 *     during construction and make no guarantees about thread safety.</li>
 *     <li>Reuse decorators only if their implementation is safe to reuse.
 *     Decorator instances may carry state and are not copied by the builder.</li>
 *     <li>Use convenience methods for standard behavior. When using custom decorator
 *     implementations or planning to reuse decorators, prefer {@link #with(TestTaskDecorator)}.</li>
 * </ul>
 *
 * @param <T> result type produced by the built task
 *
 * @see TestTasks
 */
public interface TestTaskBuilder<T> {

    /**
     * Adds the given decorator to this task definition.
     *
     * @apiNote
     * The provided decorator is not defensively
     * copied and may carry shared state.
     *
     * @param decorator decorator to apply to the task
     * @return instance of this builder
     *
     * @throws NullPointerException if {@code decorator} is {@code null}
     * @throws IllegalStateException if the task has already been built
     */
    TestTaskBuilder<T> with(TestTaskDecorator decorator);

    /**
     * Adds a fixed delay before this task begins execution.
     * <p>
     * The delay is applied when the task is executed
     * and does not affect the task's internal logic.
     *
     * @param delay duration to wait before task execution
     * @return instance of this builder
     *
     * @throws NullPointerException if {@code delay} is {@code null}
     * @throws IllegalArgumentException if {@code delay} represents a negative duration
     * @throws IllegalStateException if the task has already been built
     */
    TestTaskBuilder<T> withDelay(Duration delay);

    /**
     * Adds an execution time limit to this task.
     * <p>
     * The timeout applies to a single task invocation
     * and is enforced when the task is executed.
     *
     * @param timeout maximum allowed execution duration
     * @return instance of this builder
     *
     * @throws NullPointerException if {@code timeout} is {@code null}
     * @throws IllegalArgumentException if {@code timeout} represents a negative duration
     * @throws IllegalStateException if the task has already been built
     */
    TestTaskBuilder<T> withTimeout(Duration timeout);

    /**
     * Adds a conditional execution guard to this task.
     * <p>
     * When the provided condition evaluates to {@code true},
     * task execution is prevented for that invocation.
     *
     * @param condition condition that determines whether execution is skipped
     * @return instance of this builder
     *
     * @throws NullPointerException if {@code condition} is {@code null}
     * @throws IllegalStateException if the task has already been built
     */
    TestTaskBuilder<T> withShortCircuit(BooleanSupplier condition);

    /**
     * Builds and returns a new {@link TestTask} decorated
     * with behaviors defined during the building process.
     * <p>
     * This method may be called at most once. Calling it more
     * than once results in an {@link IllegalStateException}.
     * <p>
     * Building a task only composes it; no execution
     * or validation occurs. The returned task is fully
     * defined and cannot be modified through this API.
     *
     * @return a new decorated task instance
     * @throws IllegalStateException if this builder has already been used
     */
    TestTask build();
}
