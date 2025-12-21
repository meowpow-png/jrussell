package com.senthora.jrussell.internal;

import com.senthora.jrussell.api.TestTaskResult;

import java.time.*;
import java.util.Objects;

/**
 * Default implementation of {@link TestTaskResult}.
 */
public record DefaultTestTaskResult<T>(
        TaskDefinition<T> task,
        Instant startTime,
        Instant endTime,
        Throwable error,
        T result
) implements TestTaskResult<T> {

    public DefaultTestTaskResult {
        Objects.requireNonNull(task, "task must not be null");
        Objects.requireNonNull(startTime, "start time must not be null");
        Objects.requireNonNull(endTime, "end time must not be null");
        if (startTime.getEpochSecond() < 0) {
            throw new IllegalArgumentException("Start time must not represent negative time");
        }
        if (endTime.getEpochSecond() < 0) {
            throw new IllegalArgumentException("End time must not represent negative time");
        }
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Start time must not be after end time");
        }
    }

    @Override
    public Duration duration() {
        return Duration.between(startTime, endTime);
    }

    @Override
    public boolean success() {
        return error == null;
    }

    @Override
    public boolean started() {
        return startTime.isBefore(endTime);
    }
}
