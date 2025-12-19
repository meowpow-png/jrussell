package com.senthora.jrussell.api;

import com.senthora.jrussell.internal.DefaultTestTaskPlan;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * This interface represents a fixed set of {@link TestTask}
 * instances that are planned to be executed by {@link TestTaskRunner}.
 * <p>
 * It answers only what should be executed, independent of how or when,
 * serving as a declarative description of the work to be performed.
 * <h3>API Guarantees</h3>
 * <ul>
 *     <li>The plan is always immutable. Once created, it cannot be modified.
 *     It preserves task order and exposes tasks in the order they were added.</li>
 *     <li>The plan is safe to reuse. It may be executed multiple times.
 *     Each execution is independent and produces a new result set.</li>
 *     <li>The plan does not inspect, modify, decorate, or validate
 *     task behavior. Tasks are always stored and exposed as-is</li>
 * </ul>
 * <h3>Design Notes</h3>
 * <ul>
 *     <li>Intentionally does not interact with threads, track execution
 *     progress or handle failures or cancellations. If it ever starts caring
 *     about these concerns it stops being a plan and starts being a runner.</li>
 *     <li>Intended to be compatible with all task runners. It should not
 *     retain references to runners or past execution.</li>
 *     <li>Empty plans are explicitly allowed. Whether “nothing to run”
 *     is an error is an execution concern, not a planning concern.</li>
 * </ul>
 *
 * @apiNote
 * Callers should not assume task thread safety,
 * deterministic execution timing, failure handling
 * or result ordering across concurrent execution.
 */
public interface TestTaskPlan {

    /**
     * Creates a new {@code TestTaskPlan}
     * that contains the given collection of tasks
     * <p>
     * The collection is allowed to be empty, but the preferred
     * way of creating an empty plan is via {@link #empty()}.
     *
     * @param tasks collection of planned tasks
     * @return new plan instance; never {@code null}
     * @throws NullPointerException if {@code tasks} is {@code null}
     *         or if any element inside the collection is {@code null}.
     */
    static TestTaskPlan of(Collection<? extends TestTask> tasks) {
        Objects.requireNonNull(tasks, "tasks must not be null");
        return new DefaultTestTaskPlan(tasks);
    }

    /**
     * Creates a new {@code TestTaskPlan} that contains no tasks.
     * <p>
     * This is the preferred way of creating an empty plan
     * instead of calling {@link #of(Collection)} with an empty list.
     *
     * @return new plan instance; never {@code null}
     */
    static TestTaskPlan empty() {
        return new DefaultTestTaskPlan(List.of());
    }

    /**
     * Returns the list of tasks for this plan.
     * <p>
     * The returned list is immutable and contains
     * tasks ordered in the same way they were added.
     *
     * @return list of tasks; never {@code null}.
     */
    List<TestTask> tasks();

    /**
     * Returns the count of tasks in this plan.
     */
    int size();

    /**
     * Returns {@code true} if this plan contains no tasks.
     */
    boolean isEmpty();
}
