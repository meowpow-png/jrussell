package com.senthora.jrussell.api;

import com.senthora.jrussell.internal.TaskDefinition;

/**
 * This interface represents a named unit of work,
 * exposing a stable identity that allows tasks
 * to be referenced and compared without
 * depending on execution details.
 *
 * @implNote
 * This interface forms a sealed hierarchy that
 * restricts task implementations to an internal type,
 * ensuring that task execution remains library-controlled
 * and cannot be invoked outside task runner.
 *
 * @see TestTasks
 * @see TestTaskResult
 */
@UsesInternal
public sealed interface TestTask permits TaskDefinition {

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
