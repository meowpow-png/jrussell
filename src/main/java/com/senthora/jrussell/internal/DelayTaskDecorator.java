package com.senthora.jrussell.task;

import com.senthora.jrussell.api.TestTaskDecorator;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Applies a time delay before task execution.
 * <p>
 * This decorator is intended to directly delay task execution,
 * making it easier to model simple timing relationships between
 * tasks in concurrent tests without introducing complex
 * scheduling or runner-level coordination.
 * <p>
 * <h3>Internal Guarantees</h3>
 * <ul>
 *     <li>Delays task execution without altering task logic.</li>
 *     <li>Applies the delay directly to tasks, independent of runner scheduling.</li>
 *     <li>Interruption during delay aborts execution before the task is invoked.</li>
 * </ul>
 * <p>
 * <h3>Operational Notes</h3>
 * <ul>
 *     <li>The delay is implemented as a blocking pause on the task execution
 *     thread and therefore counts as part of the task’s execution time.</li>
 *     <li>When used alongside other decorators, their order determines
 *     how the delay interacts with timeouts and failure behavior.</li>
 *     <li>The delay is not a scheduling mechanism; it does not
 *     defer task submission or execution by the runner.</li>
 *     <li>The delay does not provide precise timing guarantees;
 *     actual start time may vary due to thread scheduling and system load.</li>
 * </ul>
 * <p>
 * <h3>Usage Guidelines</h3>
 * <ul>
 *     <li>Use this decorator to start a task later than others,
 *     for example to simulate delayed startup in concurrent execution.</li>
 *     <li>Use this decorator to defer task execution until
 *     after other setup or background activity has completed.</li>
 * </ul>
 *
 * @implNote
 * A zero-duration delay is treated as a no-op and does not wrap the task.
 */
@SuppressWarnings("ClassCanBeRecord")
public final class DelayTaskDecorator implements TestTaskDecorator {

    private final Duration delay;

    DelayTaskDecorator(Duration delay) {
        Objects.requireNonNull(delay, "delay must not be null");
        if (delay.isNegative()) {
            throw new IllegalArgumentException("Delay must be non-negative");
        }
        this.delay = delay;
    }

    /**
     * @return the original task if configured delay is zero; otherwise,
     *         a callable that applies the delay before invoking the task
     */
    @Override
    public <T> Callable<T> decorate(Callable<T> task) {
        Objects.requireNonNull(task, "task must not be null");
        if (delay.isZero()) {
            return task;
        }
        return () -> {
            try {
                Thread.sleep(delay.toMillis());
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw e;
            }
            return task.call();
        };
    }
}
