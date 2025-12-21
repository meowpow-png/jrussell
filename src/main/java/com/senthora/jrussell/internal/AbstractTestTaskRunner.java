package com.senthora.jrussell.internal;

import com.senthora.jrussell.api.*;

import java.time.Instant;
import java.util.*;

/**
 * Base class that provides common execution
 * behavior for all task runner implementations.
 * <h3>Operational Notes</h3>
 * <ul>
 *     <li>Task execution strategy is delegated to implementations.</li>
 *     <li>Execution policy is fixed at construction and applies to all tasks in a run.</li>
 *     <li>Results are returned only after execution completes normally
 *     or terminates under fail-fast execution policy.</li>
 *     <li>Execution time is measured using wall-clock time.</li>
 * </ul>
 * <h3>Implementation Contract</h3>
 * <ul>
 *     <li>Must consistently enforce execution policy.</li>
 *     <li>Must preserve plan submission order in returned results.</li>
 *     <li>Must never retain execution state between runs.</li>
 *     <li>Each task must be executed at most once per run.</li>
 *     <li>Task-originated exceptions must never escape execution and
 *     must be captured as task results.</li>
 * </ul>
 */
public non-sealed abstract class AbstractTestTaskRunner implements TestTaskRunner {

    final RunnerExecutionPolicy executionPolicy;

    AbstractTestTaskRunner(RunnerExecutionPolicy executionPolicy) {
        this.executionPolicy = Objects.requireNonNull(executionPolicy);
    }

    @Override
    public final List<TestTaskResult<?>> run(TestTaskPlan plan) {
        return execute(TaskResolver.resolveAll(plan.tasks()));
    }

    @Override
    public final TestTaskResult<?> run(TestTask task) {
        return run(TestTaskPlan.of(List.of(task))).getFirst();
    }

    @Override
    public final List<TestTaskResult<?>> run(Collection<? extends TestTask> tasks) {
        return run(TestTaskPlan.of(tasks));
    }

    abstract List<TestTaskResult<?>> execute(List<TaskDefinition<?>> tasks);

    /**
     * Executes the given task synchronously and returns execution results.
     * <p>
     * Any exception thrown by the task is captured in the returned
     * result and never propagated. Other exceptions that are propagated
     * indicates a runner-level failure and should abort execution.
     *
     * @implNote
     * Must never allow task-originated exceptions to escape.
     * All task failures are intended to be represented as result data.
     *
     * @param <T> return value of task execution
     */
    static <T> TestTaskResult<T> execute(TaskDefinition<T> task) {
        Instant start = Instant.now();
        T value = null;
        Throwable error = null;
        try {
            value = task.executable().call();
        }
        catch (Throwable t) {
            error = t;
        }
        Instant end = Instant.now();
        return new DefaultTestTaskResult<>(task, start, end, error, value);
    }

    private static final class TaskResolver {

        private TaskResolver() {}

        private static TaskDefinition<?> resolve(TestTask task) {
            Objects.requireNonNull(task, "task must not be null");
            return (TaskDefinition<?>) task;
        }

        private static List<TaskDefinition<?>> resolveAll(Collection<? extends TestTask> tasks) {
            Objects.requireNonNull(tasks, "tasks must not be null");
            if (tasks.isEmpty()) {
                return List.of();
            }
            List<TaskDefinition<?>> resolved = new ArrayList<>(tasks.size());
            for (TestTask task : tasks) {
                resolved.add(resolve(task));
            }
            return resolved;
        }
    }
}
