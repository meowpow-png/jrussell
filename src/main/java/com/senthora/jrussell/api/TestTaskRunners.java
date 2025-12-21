package com.senthora.jrussell.api;

import com.senthora.jrussell.internal.ConcurrentTestTaskRunner;
import com.senthora.jrussell.internal.SynchronousTaskRunner;

/**
 * Factory for creating {@link TestTaskRunner} instances
 * with different execution modes and execution policies.
 *
 * @implNote
 * Connects the public API to internal runner implementations.
 */
@UsesInternal
public interface TestTaskRunners {

    /**
     * Creates a task runner that executes tasks synchronously
     * on the calling thread using the specified execution policy.
     *
     * @param policy the execution policy to apply
     * @return new synchronous task runner
     *
     * @see #synchronous()
     */
    static TestTaskRunner synchronous(RunnerExecutionPolicy policy) {
        return new SynchronousTaskRunner(policy);
    }

    /**
     * Creates a task runner that executes tasks synchronously
     * on the calling thread using the default execution policy.
     *
     * @return new synchronous task runner
     *
     * @see #synchronous(RunnerExecutionPolicy)
     */
    static TestTaskRunner synchronous() {
        return synchronous(RunnerExecutionPolicy.STANDARD);
    }


    /**
     * Creates a task runner that executes tasks
     * concurrently using the specified execution policy.
     *
     * @param policy the execution policy to apply
     * @return new concurrent task runner
     *
     * @see #concurrent()
     */
    static TestTaskRunner concurrent(RunnerExecutionPolicy policy) {
        return new ConcurrentTestTaskRunner(policy);
    }

    /**
     * Creates a task runner that executes tasks
     * concurrently using the default execution policy.
     *
     * @return new concurrent task runner
     *
     * @see #concurrent(RunnerExecutionPolicy)
     */
    static TestTaskRunner concurrent() {
        return concurrent(RunnerExecutionPolicy.STANDARD);
    }
}
