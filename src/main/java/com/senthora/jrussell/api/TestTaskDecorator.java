package com.senthora.jrussell.api;

import java.util.concurrent.Callable;

/**
 * A task decorator wraps a task to control how it is executed.
 * <p>
 * It exists to let execution behavior be composed explicitly at build time,
 * keeping tasks simple and execution control outside of task code.
 * <p>
 * <h3>API Guarantees</h3>
 * <ul>
 *     <li>The original task is not <i>executed</i> during decoration.</li>
 *     <li>The original task is not <i>mutated</i> during decoration.</li>
 * </ul>
 * <p>
 * Callers must not assume when or how often a task executes, whether execution
 * is synchronous, or which thread executes the task. Nothing about execution
 * should be assumed beyond “it happens when the returned callable is invoked.”
 * <p>
 * <h3>Implementation Guidelines</h3>
 * <ul>
 *     <li>Decorators should control whether, when, or how the supplied task runs,
 *     rather than ignoring it and performing unrelated work.</li>
 *     <li>Decorators should tolerate composition, as they may be
 *     applied in arbitrary user-defined order.</li>
 *     <li>Decorators should not rely on runner-level scheduling,
 *     lifecycle, or coordination guarantees.</li>
 * </ul>
 */
@FunctionalInterface
public interface TestTaskDecorator {

    /**
     * Returns a decorated form of the supplied task.
     *
     * @param task the task to decorate
     * @param <T> the task result type
     * @return callable representing the decorated task
     * @throws NullPointerException if {@code task} is {@code null}
     */
    <T> Callable<T> decorate(Callable<T> task);
}
