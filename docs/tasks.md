---
title: "JRussell Docs — Tasks"
---

<!--
- A task is a **wrapper around `Runnable` or `Callable`**
- Tasks are **opaque** to the framework
- Tasks are **reusable** and may be executed multiple times
- Tasks may do anything internally; JRussell does not introspect
- Tasks have **identity**: name + index ID
- Tasks are responsible for their own thread safety if reused
-->

Tasks are opaque. JRussell does not introspect behavior, state, or intent:
No lifecycle management, no hooks into app state, no global registries.

JRussell is not a reliability system:
Failure is expected, captured, and surfaced—not hidden.

## Understanding Tasks

In JRussell, a task represents a reusable unit of work, not a single-use execution.

When you create a task, you give it an identity which stays with the task every time it runs. This makes tasks easy to reuse, and compare across test runs. You can run the same task multiple times and each run produces its own result, all tied back to the same task.

JRussell does not look inside tasks or try to manage their behavior. It simply runs them, measures what happened, and reports the outcome.

**What you can expect:**

- Every individual task can be executed multiple times.
- Tasks can be reused across different tests and scenarios.
- Every execution produces a new, independent result.
- Results clearly reference the task that produced them.
- Tasks are never inspected or modified before or after execution.
- All execution threads are internally owned and managed.
- Failures inside tasks are always captured and reported, never swallowed.

**What you are responsible for:**

- Making sure tasks are safe to run more than once.
- Ensuring thread safety when tasks may run concurrently.
- Responding to interruption if your task supports cancellation.
- Cleaning up any resources used by a task.

---

## Authoring Tasks

### Thread Safety

When JRussell runs tasks concurrently, it may execute multiple tasks at the same time on different threads. JRussell does not coordinate access to task internals, shared state, or external resources. As a result, thread safety is the responsibility of task authors.

If the **same task instance** is executed concurrently:

- Any mutable state inside the task must be safe to access from multiple threads.
- Instance fields must be immutable, synchronized, or otherwise protected.
- Tasks should not assume that executions happen one at a time.

If tasks access **shared resources**, such as static fields, singletons, files or databases, in-memory caches or external services then access to those resources must be coordinated by the user. JRussell does not serialize access or provide locking.

When tasks run concurrently execution order is not guaranteed, timing is not predictable and side effects may interleave. For these reasons tasks must not rely on:

- Another task having already run
- A specific execution order
- Thread-local assumptions unless explicitly managed

JRussell may **interrupt threads** to signal **cancellation or timeouts**. Tasks should always tolerate interruptions if they support cancellation and avoid leaving shared state in an inconsistent state when interrupted. JRussell will never attempt to forcibly stop threads or roll back partial work.

For example, imagine a developer writing a task to test production logic that processes a batch of inputs and produces a summarized result, such as record validation, data transformation, or collecting assertion failures. The task is expected to behave the same way on every run, based only on that run’s inputs.

The developer then writes a simple test task that does the following:

- Keeps an internal collection to accumulate results
- Appends to this collection as it processes input
- Returns the accumulated result at the end

Since the test task defines reusable execution logic the developer decides to reuse the task instance across test runs. Because the task holds onto the same accumulator:

- Data from the first run is still present
- The second run appends on top of it
- The result now includes entries from _previous executions_

This kind of bug can lead to:

- Tests that pass or fail depending on execution order
- Failures that disappear when run in isolation
- Incorrect confidence in system behavior

From the developer’s point of view, JRussell appears to be working correctly. The task ran, returned a result, and no concurrency was involved. The real issue was **state leakage caused by task reuse**.

---

## Building Tasks

### Task Identity

### Task Decoration

---

<!--
- Two execution modes: **sequential** and **concurrent**
- Sequential execution is **blocking**
- Concurrent execution is **non-blocking**, but result collection is blocking
- No streaming of results; execution is **deterministic**
- JRussell **owns all execution threads**
- Execution is **best-effort**
-->

## Running Tasks

### Execution Mode

### Tracking Time

<!--
- Execution time is measured using **`System.nanoTime`**
- Timeouts **interrupt the executing thread** and **cancel the future**
- Cancellation is **cooperative and best-effort**
- JRussell does not forcefully terminate tasks
-->

### Handling Failures

<!--
- Failures originating inside tasks are **captured in task results**
- Framework-level failures (timeouts, interruptions) are **propagated**
- Clear distinction between task failures and execution failures
-->

### Canceling Tasks

---

## Reading Results

<!--
- Every execution produces a **fully materialized result**
- Results are complete and available only after execution finishes
- Results include:
    - Task identity
    - Execution duration
    - Captured failure (if any)
    - Return value (if applicable)
-->

### Basic Metadata

### Execution Time

### Captured Failure

### Result Value

---

## Usage Examples

---