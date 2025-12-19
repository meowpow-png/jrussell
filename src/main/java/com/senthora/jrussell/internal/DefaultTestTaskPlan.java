package com.senthora.jrussell.internal;

import com.senthora.jrussell.api.TestTask;
import com.senthora.jrussell.api.TestTaskPlan;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;

/**
 * Default implementation of {@link TestTaskPlan}.
 *
 * @implNote
 * This class should not assign indices to tasks.
 * That is the responsibility of task runners running the plan.
 * It should also not normalize identity or reorder tasks.
 */
public final class DefaultTestTaskPlan implements TestTaskPlan {

    @Unmodifiable
    private final List<TestTask> tasks;

    public DefaultTestTaskPlan(Collection<? extends TestTask> tasks) {
        this.tasks = List.copyOf(tasks);
    }

    @Override
    @Unmodifiable
    public List<TestTask> tasks() {
        return tasks;
    }

    @Override
    public int size() {
        return tasks.size();
    }

    @Override
    public boolean isEmpty() {
        return tasks.isEmpty();
    }
}
