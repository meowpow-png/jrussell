---
title: "JRussell Docs — Tasks"
---

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

### Task Reusability

In JRussell, tasks are designed to be **reusable**. JRussell may execute the **same task instance** multiple times during a test run. Task instances are not recreated or reset between executions, so any state they hold persists across runs.

When authoring reusable tasks:

- Prefer **local variables** inside the task body for execution-specific data.
- Treat instance fields as **immutable configuration**, not execution state.
- If mutable state is required, ensure it is **explicitly reset** on each execution.

It is important to note that task reusability is independent of concurrency. Even when tasks are executed one at a time on a single thread, reuse-related bugs can occur if state from a previous execution is unintentionally retained.

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

### Interruption Handling

Tasks executed by JRussell may be **interrupted** during execution. Interruption is used to signal events such as timeouts or explicit cancellation requests, and can occur at any point while a task is running.

Interruption is **cooperative and best-effort**. An interrupt is a signal delivered to the executing thread; it does not forcibly stop execution or roll back partial work. If a task ignores interruption, it may continue running even after a timeout or cancellation has been requested.

Because of this, **task code is responsible for responding appropriately to interruption**. Tasks that support cancellation should periodically check their interruption status and exit promptly when interrupted. Tasks that block on interruptible operations should handle interruption in a way that leaves the system in a consistent state.

Interruption may occur **mid-execution**, after some work has already been performed. Any side effects that occur before interruption are not automatically undone. No attempt is made to clean up partial work left behind by interrupted tasks.

Interruption handling is **independent of concurrency**. Tasks may be interrupted even when executed sequentially on a single thread, for example due to a timeout. As a result, tasks should not assume that interruption only occurs during concurrent execution.

Correct handling of interruption is essential for predictable behavior when timeouts or cancellation are used. Tasks that cooperate with interruption terminate more promptly and reduce the risk of leaving inconsistent or unexpected state behind.

---

## Building Tasks

### Task Identity

Every task in JRussell has an **identity**. Task identity is a stable label that stays with a task across all executions and is used to associate results with the task that produced them.

Task identity has the following properties:

- Identity does not change across executions
- Identity is independent of task behavior
- Tasks sharing an identity represent the same logical task

JRussell does not validate, normalize, or enforce uniqueness of explicitly provided identifiers. Responsibility for choosing meaningful and non-colliding identifiers lies with the caller.

When building a task, you begin by choosing how the task should be identified. You may assign an explicit identifier or allow one to be generated automatically.

```java
IdentitySelector explicitId = TestTasks.named("my-task-id");
IdentitySelector autoGeneratedId = TestTasks.unnamed();
```

Selecting an identity does **not** create a task. Instead, it produces an intermediate object that represents the chosen identity and serves as a factory for building tasks associated with it.

From an identity selector, you define the executable logic for the task:

```java
TestTasks.named("response-health-check").callable(() -> {  
    int responseTimeMs = getResponseTimeInMillis();  
    return responseTimeMs < 500 ? "HEALTHY" : "DEGRADED";  
});
```

Multiple tasks may be built from the same identity selector:

```java
IdentitySelector selector = TestTasks.named("simple-count-down");  
TestTask slowCountDown = selector.runnable(this::slowCountDown).build();  
TestTask fastCountDown = selector.runnable(this::fastCountDown).build();
```

When this is done, all resulting tasks intentionally share the same task identity.

Task identity is established once during task definition and cannot be modified afterward. Once an identity is chosen, all subsequent task configuration builds upon that identity.

### Builder API

After choosing a task identity and providing executable logic, task behavior is composed step by step using the builder API. Each method call contributes additional execution behavior to the task definition, but nothing is executed or applied eagerly.

Builder methods are fluent and return the same builder instance, allowing configuration to be expressed as a single chained definition.

```java
TestTasks.named("example-task")
    .callable(this::runCheck)
    .withTimeout(Duration.ofSeconds(2))
    .withDelay(Duration.ofMillis(100))
    // additional configuration
```

Calling `build()` finalizes the task definition.

```java
TestTask task = TestTasks.named("once")
    .runnable(this::execute)
    .build();
```

A builder instance may be used to build **at most one task**. After a task has been built, the builder cannot be modified or reused. Any attempt to do so will fail with an exception.

```java
var builder = TestTasks.unnamed().runnable(this::execute);
builder.build();  // succeeds
builder.build();  // throws exception
```

To define a new task or a variation of an existing one, create a new builder.

Builders start with only the executable logic provided by the user. No additional execution behavior is applied implicitly. All behavior beyond basic execution is introduced explicitly through builder method calls.

Builder methods record execution decorators internally, which are applied when the task is built.
For detailed information about decorators see **[[decorators|JRussell Docs — Decorators]]**.

---

## Running Tasks

<!--
- Two execution modes: **sequential** and **concurrent**
- Sequential execution is **blocking**
- Concurrent execution is **non-blocking**, but result collection is blocking
- No streaming of results; execution is **deterministic**
- JRussell **owns all execution threads**
- Execution is **best-effort**
-->

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