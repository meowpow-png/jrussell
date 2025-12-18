package com.senthora.jrussell.internal;

import com.senthora.jrussell.api.TestTask;
import com.senthora.jrussell.api.TestTaskBuilder;
import com.senthora.jrussell.api.TestTaskDecorator;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.BooleanSupplier;

public final class TestTaskBuilderImpl<T> implements TestTaskBuilder<T> {

    private final String id;
    private final Callable<T> task;
    private final List<TestTaskDecorator> decorators;

    private boolean built = false;

    public TestTaskBuilderImpl(String id, Callable<T> task) {
        Objects.requireNonNull(id, "task id must not be null");
        var normalized = id.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("task id must not be blank");
        }
        this.id = normalized;
        this.task = Objects.requireNonNull(task, "task must not be null");
        this.decorators = new ArrayList<>();
    }

    @Override
    public TestTaskBuilderImpl<T> with(TestTaskDecorator decorator) {
        assertTaskNotBuilt();
        Objects.requireNonNull(decorator, "decorator must not be null");
        return add(decorator);
    }

    @Override
    public TestTaskBuilderImpl<T> withDelay(Duration delay) {
        assertTaskNotBuilt();
        Objects.requireNonNull(delay, "delay must not be null");
        return add(new DelayTaskDecorator(delay));
    }

    @Override
    public TestTaskBuilderImpl<T> withTimeout(Duration timeout) {
        assertTaskNotBuilt();
        Objects.requireNonNull(timeout, "timeout must not be null");
        return add(new TimeoutTaskDecorator(timeout));
    }

    @Override
    public TestTaskBuilderImpl<T> withShortCircuit(BooleanSupplier condition) {
        assertTaskNotBuilt();
        Objects.requireNonNull(condition, "condition must not be null");
        return add(new ShortCircuitTaskDecorator(condition));
    }

    @Override
    public TestTask build() {
        assertTaskNotBuilt();
        Callable<T> composed = task;
        for (TestTaskDecorator decorator : decorators) {
            composed = decorator.decorate(composed);
        }
        built = true;
        return new TaskDefinition<>(id, composed);
    }

    private void assertTaskNotBuilt() {
        if (built) {
            throw new AssertionError("Builder has already been used to build a task");
        }
    }

    private TestTaskBuilderImpl<T> add(TestTaskDecorator decorator) {
        decorators.add(decorator);
        return this;
    }
}
