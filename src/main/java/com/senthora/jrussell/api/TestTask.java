package com.senthora.jrussell.api;

/**
 * This interface represents a named unit of work,
 * exposing a stable identity that allows tasks
 * to be referenced and compared without
 * depending on execution details.
 *
 * @see TestTasks
 * @see TestTaskResult
 */
public interface TestTask {

    /**
     * Returns the identifier associated with this task.
     * <p>
     * <b>Idempotence:</b><br>
     * Repeated calls return the same value.
     * <p>
     * <b>Equality:</b><br>
     * Two task instances with the same id represent the same logical task.
     */
    String id();
}
