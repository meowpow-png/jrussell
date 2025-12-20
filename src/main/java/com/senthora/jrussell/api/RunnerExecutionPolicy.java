package com.senthora.jrussell.api;

/**
 * Defines how task execution should respond to failures
 * encountered during a run executed by {@link TestTaskRunner}.
 */
public enum RunnerExecutionPolicy {

    /**
     * Execution policy where task execution proceeds
     * without early termination when individual tasks fail.
     *
     * @apiNote
     * This is the default execution policy.
     */
    STANDARD,

    /**
     * Execution policy where task execution terminates
     * early when a failure is encountered.
     */
    FAIL_FAST
}
