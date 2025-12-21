package com.senthora.jrussell.internal;

import com.senthora.jrussell.api.RunnerExecutionException;
import com.senthora.jrussell.api.RunnerExecutionPolicy;
import com.senthora.jrussell.api.TestTaskResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Task runner implementation that executes tasks
 * synchronously on the calling thread.
 * <h3>Internal Guarantees</h3>
 * <ul>
 *     <li>Tasks are executed in the order defined by execution plan.</li>
 *     <li>All tasks are executed synchronously on the calling thread.</li>
 *     <li>Each task begins execution only after the previous task
 *     has fully completed; no execution overlap or interleaving occurs.</li>
 *     <li>Under fail-fast execution policy, execution stops immediately
 *     after the first unsuccessful task and no further tasks are started.</li>
 *     <li>If interruption is observed during execution, the run aborts
 *     immediately and interruption is reported as a runner-level failure.</li>
 * </ul>
 * <h3>Operational Notes</h3>
 * <ul>
 *     <li>The runner does not cancel or interrupt tasks.</li>
 *     <li>The runner does not offload execution to other threads.</li>
 * </ul>
 *
 * @apiNote
 * Fail-fast execution stops subsequent tasks from
 * starting but does not interrupt currently executing task.
 * Interruption during execution is reported as task failure.
 */
public final class SynchronousTaskRunner extends AbstractTestTaskRunner {

    public SynchronousTaskRunner(RunnerExecutionPolicy executionPolicy) {
        super(executionPolicy);
    }

    @Override
    List<TestTaskResult<?>> execute(List<TaskDefinition<?>> tasks) {
        List<TestTaskResult<?>> results = new ArrayList<>(tasks.size());
        for (TaskDefinition<?> task : tasks) {
            try {
                TestTaskResult<?> result = execute(task);
                results.add(result);
                if (!result.success() && executionPolicy == RunnerExecutionPolicy.FAIL_FAST) {
                    break;
                }
            }
            catch (RuntimeException e) {
                if (Thread.currentThread().isInterrupted()) {
                    Thread.currentThread().interrupt();
                }
                var message = "Runner infrastructure failure during execution";
                throw new RunnerExecutionException(message, e);
            }
        }
        return results;
    }
}
