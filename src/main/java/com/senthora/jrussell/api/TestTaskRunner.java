package com.senthora.jrussell.api;

import com.senthora.jrussell.internal.AbstractTestTaskRunner;

import java.util.Collection;
import java.util.List;

/**
 * This interface represents a stateless engine for executing
 * test tasks and returning ordered execution results.
 * <p>
 * It exists to separate <em>what</em> should be executed from
 * <em>how</em> it is executed, providing predictable failure
 * handling and result ordering independent of execution strategy.
 * <h3>API Guarantees</h3>
 * <ul>
 *     <li>Task specific failures are always captured in {@link TestTaskResult}.</li>
 *     <li>Tasks are executed at most once per run and each
 *     task that completes produces exactly one result.</li>
 *     <li>Runners do not modify execution plans or tasks.</li>
 *     <li>Infrastructure failures abort current and future task execution
 *     and are propagated as runner-level exceptions.</li>
 *     <li>When fail-fast execution is enabled, results may be partial
 *     and will include only tasks that completed before cancellation.</li>
 *     <li>Result ordering always matches task order defined by execution plan.</li>
 * </ul>
 *
 * @apiNote
 * Callers should not assume deterministic execution timing
 * or specific task interruption semantics. Task cancellation
 * is attempted but not guaranteed; in-flight tasks may still complete.
 *
 * @implNote
 * This sealed interface restricts class
 * implementations to library-provided types.
 */
@UsesInternal
public sealed interface TestTaskRunner permits AbstractTestTaskRunner {

    /**
     * Executes the given plan and returns execution results.
     * <p>
     * This method blocks until execution completes or
     * terminates under the configured execution policy.
     *
     * @param plan the execution plan defining tasks to run
     * @return ordered list of execution results; never {@code null}
     * @throws RunnerExecutionException if task execution cannot be
     *         completed due to a runner-level failure
     *
     * @see #run(TestTask)
     */
    List<TestTaskResult<?>> run(TestTaskPlan plan);

    /**
     * Executes a single task and returns its execution result.
     * <p>
     * This is a convenience method for executing exactly one task.
     *
     * @param task the task to execute
     * @return execution result for the given task; never {@code null}
     * @throws RunnerExecutionException if task execution cannot be
     *         completed due to a runner-level failure
     *
     * @see #run(TestTaskPlan)
     */
    TestTaskResult<?> run(TestTask task);

    /**
     * Executes the given collection of tasks and returns execution results.
     * <p>
     * This is a convenience method for executing multiple
     * individual tasks. For repeated execution, tasks
     * should be grouped into {@link TestTaskPlan}.
     *
     * @param tasks the tasks to execute
     * @return ordered list of execution results; never {@code null}
     * @throws RunnerExecutionException if task execution cannot be
     *         completed due to a runner-level failure
     *
     * @see #run(TestTaskPlan)
     */
    List<TestTaskResult<?>> run(Collection<? extends TestTask> tasks);
}
