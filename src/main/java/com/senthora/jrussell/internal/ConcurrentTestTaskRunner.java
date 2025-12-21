package com.senthora.jrussell.internal;

import com.senthora.jrussell.api.RunnerExecutionPolicy;
import com.senthora.jrussell.api.TestTask;
import com.senthora.jrussell.api.TestTaskResult;
import com.senthora.jrussell.api.RunnerExecutionException;

import java.util.*;
import java.util.concurrent.*;

/**
 * Task runner implementation that executes tasks concurrently using multiple threads.
 * <h3>Internal Guarantees</h3>
 * <ul>
 *     <li>All tasks are submitted to the underlying executor eagerly at the start of execution.
 *     Task submission is not deferred or conditional on the completion of other tasks.</li>
 *     <li>When execution terminates early, results include only tasks that completed
 *     before termination. Cancelled or incomplete tasks do not produce results.</li>
 *     <li>If the executing thread is interrupted while awaiting task completion,
 *     execution aborts immediately and a runner-level exception is thrown.</li>
 * </ul>
 * <h3>Operational Notes</h3>
 * <ul>
 *     <li>Task start order, completion order and timing are nondeterministic
 *     and may vary between runs, even with the same execution plan.</li>
 *     <li>Under fail-fast execution policy, failure is detected only after
 *     a task completes. Tasks that have already been submitted or started
 *     may still execute partially or fully before cancellation is attempted</li>
 *     <li>When cancellation occurs, remaining tasks are cancelled using
 *     thread interruption. Tasks that do not respond to interruption
 *     may continue running in the background.</li>
 *     <li>Task execution is backed by a fixed-size thread pool sized
 *     to the minimum of the number of tasks and available processors.</li>
 * </ul>
 */
public final class ConcurrentTestTaskRunner extends AbstractTestTaskRunner {

    public ConcurrentTestTaskRunner(RunnerExecutionPolicy executionPolicy) {
        super(executionPolicy);
    }

    @Override
    List<TestTaskResult<?>> execute(List<TaskDefinition<?>> tasks) {
        if (tasks.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        TestTaskResult<?>[] results = new TestTaskResult[tasks.size()];
        try (ExecutorService executor = newExecutorService(tasks)) {
            CompletionService<TestTaskResult<?>> completion = new ExecutorCompletionService<>(executor);

            Map<Future<TestTaskResult<?>>, TestTask> futures = new HashMap<>(tasks.size());
            Map<Future<TestTaskResult<?>>, Integer> futureIndex = new HashMap<>(tasks.size());

            for (int i = 0; i < tasks.size(); i++) {
                TaskDefinition<?> task = tasks.get(i);
                try {
                    Future<TestTaskResult<?>> future = completion.submit(() -> execute(task));
                    futures.put(future, task);
                    futureIndex.put(future, i);
                }
                catch (RejectedExecutionException e) {
                    var message = "Task submission was rejected by executor [" + task + ']';
                    throw new RunnerExecutionException(message, e);
                }
            }
            Set<Future<TestTaskResult<?>>> completedFutures = new LinkedHashSet<>();
            for (int i = 0; i < futures.size(); i++) {
                try {
                    Future<TestTaskResult<?>> future = completion.take();
                    completedFutures.add(future);

                    TestTaskResult<?> result = future.get();
                    Integer index = futureIndex.get(future);
                    if (index == null) {
                        var message = "Unable to retrieve future index for task '%s'";
                        throw new RunnerExecutionException(message.formatted(result.task().id()));
                    }
                    results[index] = result;

                    if (!result.success() && executionPolicy == RunnerExecutionPolicy.FAIL_FAST) {
                        cancelRemainingTasks(futures.keySet());
                        break;
                    }
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    var message = executionInterruptedMessage(completedFutures, futures);
                    throw new RunnerExecutionException(message, e);
                }
                catch (ExecutionException e) {
                    var message = "Invariant violation: task execution leaked an exception";
                    throw new RunnerExecutionException(message, e.getCause());
                }
                catch (RuntimeException e) {
                    var message = "Runner infrastructure failure during execution";
                    throw new RunnerExecutionException(message, e);
                }
            }
        }
        return Arrays.stream(results)
                .filter(Objects::nonNull)
                .toList();
    }

    private static String executionInterruptedMessage(
            Set<Future<TestTaskResult<?>>> completedFutures,
            Map<Future<TestTaskResult<?>>, TestTask> allFutures
    ){
        var newLine = System.lineSeparator();
        var message = "Test execution interrupted while awaiting task completion.";
        if (completedFutures.isEmpty()) {
            return message;
        }
        StringBuilder sb = new StringBuilder(message + newLine + "Completed tasks:");
        for (Future<TestTaskResult<?>> future : completedFutures) {
            sb.append(newLine).append(allFutures.get(future).id());
        }
        return sb.toString();
    }

    private static ExecutorService newExecutorService(List<TaskDefinition<?>> tasks) {
        int poolSize = Math.min(tasks.size(), Runtime.getRuntime().availableProcessors());
        return Executors.newFixedThreadPool(poolSize);
    }

    private static void cancelRemainingTasks(Collection<Future<TestTaskResult<?>>> futures) {
        futures.forEach(f -> f.cancel(true));
    }
}
