package com.senthora.jrussell.api;

import java.time.Duration;
import java.time.Instant;

/**
 * Represents an immutable snapshot of a task execution attempt.
 * <h3>API Guarantees</h3>
 * <ul>
 *     <li>All values are immutable after creation.</li>
 *     <li>All values describe a single execution attempt snapshot.</li>
 *     <li>This interface has no side effects; accessing values never triggers execution.</li>
 * </ul>
 *
 * @param <T> the return type of task execution result
 */
public interface TestTaskResult<T> {

    /**
     * Returns the {@code TestTask} associated with this result.
     *
     * @apiNote
     * The returned task instance may be associated with multiple results.
     *
     * @return test task instance; never {@code null}
     */
    TestTask task();

    /**
     * Returns the exact moment when execution of
     * the task associated with this result began.
     * <p>
     * If task execution never began, the returned
     * value will be equal to end time. This moment
     * is always before or equal to the end time.
     *
     * @return the moment execution began; never {@code null}
     *
     * @see #endTime()
     * @see #started()
     */
    Instant startTime();

    /**
     * Returns the exact moment when execution of
     * the task associated with this result ended.
     * <p>
     * If task execution never began, the returned
     * value will be equal to start time. This moment
     * is always after or equal to the start time.
     *
     * @return the moment execution ended; never {@code null}
     *
     * @see #startTime()
     * @see #started()
     */
    Instant endTime();

    /**
     * Returns the exception thrown during the
     * execution of the task associated with this result.
     *
     * @apiNote
     * The exception may originate from the task or
     * from execution infrastructure and may have
     * been transformed by wrapping operations.
     *
     * @return exception thrown during task execution,
     *         or {@code null} if no exception was thrown
     *
     * @see #success()
     */
    Throwable error();

    /**
     * Returns the value produced by the execution
     * of the task associated with this result.
     * <p>
     * The result may be {@code null}, which does not
     * necessarily indicate failure; the task may have
     * returned {@code null} or have a {@code Void} return type.
     *
     * @see #success()
     * @see #error()
     */
    T result();

    /**
     * Returns the attempted execution duration
     * of the task associated with this result
     * <p>
     * This is a convenience method that simply
     * returns the duration between {@link #startTime()}
     * and {@link #endTime()}.
     *
     * @return execution duration, or zero if task execution
     *         never began; never {@code null}
     */
    Duration duration();

    /**
     * Returns whether execution of the task associated
     * with this result completed without throwing an exception.
     *
     * @apiNote
     * A return value of {@code false} does not necessarily
     * mean that execution started. Tasks that were short-circuited
     * or otherwise prevented from running may still be reported as failures.
     */
    boolean success();

    /**
     * Returns whether execution of the task
     * associated with this result started.
     * <p>
     * This is a convenience method that compares
     * {@link #startTime()} and {@link #endTime()}
     * to determine whether execution began.
     *
     * @apiNote
     * A return value of {@code false} does not imply that the
     * task succeeded or failed, only that execution did not begin.
     */
    boolean started();
}
