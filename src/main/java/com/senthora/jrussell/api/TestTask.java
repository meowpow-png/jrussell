package com.senthora.jrussell.api;

import com.senthora.jrussell.internal.TaskDefinition;

/**
 * This interface represents a named unit of work,
 * exposing a stable identity that allows tasks
 * to be referenced and compared without
 * depending on execution details.
 *
 * @implNote
 * This sealed interface restricts task
 * implementations to library-provided types.
 *
 * @see TestTasks
 * @see TestTaskResult
 */
@UsesInternal
public sealed interface TestTask permits TaskDefinition {

    /**
     * Returns the identifier associated with this task.
     * <p>
     * Repeated calls return the same value.
     * Two task instances with the same id represent the same logical task.
     *
     * @return id of this task; never {@code null} or empty
     */
    String id();
}
