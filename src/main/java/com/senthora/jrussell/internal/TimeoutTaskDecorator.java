package com.senthora.jrussell.internal;

import com.senthora.jrussell.api.TestTaskDecorator;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * Applies a strict execution time limit to individual tasks.
 * <p>
 * This decorator is intended for use when tasks are executed concurrently,
 * as the runner cannot independently monitor and enforce timeouts
 * for multiple tasks executing in parallel.
 * <p>
 * <h3>Internal Guarantees</h3>
 * <ul>
 *     <li>Executes each decorated task in its own dedicated executor.</li>
 *     <li>Enforces execution timeout, independent of runner scheduling.</li>
 *     <li>Cancels the underlying task when a timeout or interruption occurs.</li>
 *     <li>Preserves and propagates thread interruption, task-thrown exceptions and system errors.</li>
 * </ul>
 * <p>
 * <h3>Operational Notes</h3>
 * <ul>
 *     <li>The timeout applies to a single task invocation only and does not
 *     represent a global or aggregate timeout for the entire runner execution.</li>
 *     <li>Timeout enforcement is best-effort and may be affected by thread scheduling,
 *     or executor startup latency; it should not be assumed to be precise to the millisecond.</li>
 *     <li>The timeout countdown begins when the task is submitted for execution,
 *     not necessarily when the task first begins running.</li>
 * </ul>
 * <p>
 * <h3>Usage Guidelines</h3>
 * <ul>
 *     <li>Ensure decorated tasks terminate promptly when interrupted;
 *     timeout enforcement relies on cooperative interruption, and tasks
 *     that ignore interruption may continue executing until completion.</li>
 *     <li>Interrupted tasks do not need to restore the interrupt flag,
 *     as they run on dedicated, non-reused threads with no upstream
 *     observation of interruption state.</li>
 * </ul>
 *
 * @implNote
 * This decorator allocates a new thread per decorated task, which may
 * increase thread creation overhead when applied to a large number of tasks.
 */
@SuppressWarnings("ClassCanBeRecord")
public final class TimeoutTaskDecorator implements TestTaskDecorator {

    private final Duration timeout;

    TimeoutTaskDecorator(Duration timeout) {
        Objects.requireNonNull(timeout, "timeout must not be null");
        if (timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("Timeout must be positive");
        }
        this.timeout = timeout;
    }

    @Override
    public <T> Callable<T> decorate(Callable<T> task) {
        Objects.requireNonNull(task, "task must not be null");
        return () -> {
            try (ExecutorService executor = namedSingleThreadExecutor()) {
                return executeFutureTask(executor, task);
            }
        };
    }

    private ExecutorService namedSingleThreadExecutor() {
        return Executors.newSingleThreadExecutor(r ->  new Thread(r, "timeout-task"));
    }

    private <T> T executeFutureTask(ExecutorService executor, Callable<T> task) throws Exception {
        Future<T> future = executor.submit(task);
        try {
            return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        }
        catch (TimeoutException e) {
            future.cancel(true);
            throw e;
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            future.cancel(true);
            throw e;
        }
        catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception ex) {
                throw ex;
            }
            if (cause instanceof Error err) {
                throw err;
            }
            throw new RuntimeException(cause);
        }
    }
}
