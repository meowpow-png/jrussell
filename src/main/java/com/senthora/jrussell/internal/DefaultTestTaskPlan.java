package com.senthora.jrussell.internal;

import com.senthora.jrussell.api.TestTask;
import com.senthora.jrussell.api.TestTaskPlan;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * Default implementation of {@link TestTaskPlan}.
 *
 * @implNote
 * This class should not assign indices to tasks.
 * That is the responsibility of task runners running the plan.
 * It should also not normalize identity or reorder tasks.
 */
public record DefaultTestTaskPlan(List<TestTask> tasks) implements TestTaskPlan {

    /**
     * @implNote
     * If an already immutable list is passed,
     * it will generally not be copied by {@code List.copyOf}.
     */
    public DefaultTestTaskPlan {
        tasks = List.copyOf(tasks);
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
