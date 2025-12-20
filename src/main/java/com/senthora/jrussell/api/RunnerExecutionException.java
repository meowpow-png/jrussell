package com.senthora.jrussell.api;

/**
 * Indicates a runner-level failure that prevents task execution from completing.
 *
 * @implNote
 * Kept non-final for possible future use within the library.
 */
public class RunnerExecutionException extends RuntimeException {

    /**
     * Constructs a new runner execution exception
     * with the given message and cause.
     *
     * @param message a description of the runner-level failure
     * @param cause the underlying cause of the failure ({@code null} value
     *              is permitted, and indicates that the cause is unknown)
     */
    public RunnerExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new runner execution exception with the given message.
     *
     * @param message a description of the runner-level failure
     */
    public RunnerExecutionException(String message) {
        super(message);
    }

}
