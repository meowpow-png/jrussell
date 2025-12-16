---
title: "JRussell Docs — Decorators"
---

## Introduction

Task decoration is the creation of a new task by wrapping the behavior of an existing task.

Decorators are reusable, standalone components that allow you to adjust how and under what conditions a task runs without changing the task’s internal logic. This makes it possible to apply cross-cutting execution concerns such as timing, constraints, or conditional execution while keeping tasks focused on their core responsibility.

---

## Capabilities

Decorators are intended to control whether, when, and under what conditions a task executes. JRussell provides built-in task decorators with standard capabilities that wrap and control execution, such as:

- Delaying execution or enforcing time limits
- Injecting failures or short-circuiting execution
- Forwarding return values and propagating exceptions

Provided decorators do not:

- Inspect results or analyze failures
- React to task execution outcomes
- Keep track of task execution history
- Modify task logic or perform execution optimization

These framework-level guarantees, they do not apply to user decorator implementations, which do not have to follow these rules. JRussel is a **library, not a sandbox**. Users are encouraged to create their own decorator implementations which may:

- Keep local state (including flags or counters)
- Short-circuit execution based on that state
- Implement custom execution policies
- Break determinism or safety if they choose

### Transformation

JRussell task decoration is are purely functional transformation.

Decorating a task produces a new task value and never modifies the original task. Decoration changes how a task is executed inside a runner, not the task’s internal logic. For example, a delay decorator introduces a delay *around* task execution, not *inside* the task itself.

### Failure Injection

Decorators may cause failures (for example, failing after a delay or when execution exceeds a time limit). However, built-in decorators do not interpret, transform, or recover from failures produced by tasks. Failure handling and reporting are the responsibility of execution runners.

### Short-Circuiting

JRussell provides a decorator abstraction that allows decorators to prevent task execution entirely by choosing not to invoke the wrapped task. This is commonly referred to as _short-circuiting_.

JRussell does not assign any special meaning to short-circuited execution. When a task is prevented from running, it is reported like any other execution failure. The runner makes no distinction, and results do not expose a separate outcome or state.

---

## Decorator API

<!--
- Decorators are applied **eagerly**
- Decorator order is **significant and user-defined**
- No automatic reordering or normalization
-->





### Creating Decorators

Users may use built-in decorators or implement their own. JRussell does not distinguish between built-in and custom decorators. A decorator is recognized as such only if it implements the `TestTaskDecorator` interface.

Decorators typically invoke the wrapped task as part of execution, but may also delay, interrupt, or prevent execution entirely. While JRussell does not enforce decorator behavior, decorators are expected to operate on the provided task rather than replace it arbitrarily. For more information on up-to-date implementation constraints read class javadoc.

To see interesting ways to implement decorators read [[#Usage Examples]].

### Applying Decorators

Decorators are applied one at a time when building tasks:

```java
TestTasks.runnable(...).withDelay(...).withTimeout(...).build();
```

When applying decorators it's important to understand that actual decoration happens when you call the methods; at **task construction time**, not at execution time. This means that a decorator is applied **before** a task is given to a runner. There is no deferred or lazy resolution. Once a task is decorated, that decoration is fixed for the lifetime of that task value.

Decorators can also be applied directly with object references:

```java
var delayDecorator = new DelayDecorator(...);
var timeoutDecorator = new TimeoutDecorator(...);

TestTasks.runnable(...)
		.withDecorator(delayDecorator)
		.withDecorator(timeoutDecorator)
		.build();
```

This approach is useful when you want to reuse decorators across tests or scenarios, since the decorator API does not expose which decorators were applied during task construction. Applying decorators this way is functionally equivalent to the inline configuration approach; the two differ only in how decorators are provided, not in how they are applied or executed.

It is also important to note that decorators apply only to the task being built. Applying a decorator does not affect other tasks or previously built task instances.

### Composing Decorators

Decorators wrap tasks **in the order provided by the user**. Conceptually, it looks like:

- First decorator applied → closest to the task
- Last decorator applied → outermost wrapper

For example, consider a task that completes in 100 ms, should execute with a delay of 500 ms, and a timeout of 300 ms. Here are two possible ways this task can be configured:

- **Delay → Timeout**  `[Timeout(Delay(Task))]`
  The timeout starts immediately and includes the delay. The timeout expires before the task starts, and execution fails.

- **Timeout → Delay** `[Delay(Timeout(Task))]`
  The delay happens first. The timeout starts after the delay and applies only to task execution. The task completes successfully.

Changing the order of decorators changes execution outcome, even with the same task and configuration. This means order affects behavior, and users are responsible for choosing a meaningful order.

JRussell does not reorder, merge, or normalize decorators. It does not detect incompatible decorators, collapse multiple decorators into one, or resolve composition conflicts. It also does not impose any rules or restrictions on how decorators are composed; users may apply multiple decorators to a task or combine them differently per task.

### Reusing Decorators

Users are allowed to reuse decorators without limitations. However, JRussell provides no guarantees about reuse safety. Decorators may be stateless or stateful, and any state they carry is owned entirely by the decorator implementation.

If a decorator maintains internal state, that state applies to every task it is reused with and must be safe for the intended execution mode. JRussell does not clone decorators, coordinate access to state, or enforce single-use behavior.

As with tasks, responsibility for thread safety, and reuse behavior lies with the author.

---

## Provided Decorators

### DelayTaskDecorator

This decorator applies a **fixed time delay** before a task begins execution. It is useful for modeling simple timing relationships between tasks, especially in concurrent tests, without introducing scheduling logic or coordination at the runner level.

The delay affects only *when* the task starts executing; it does not change task behavior. If the task is interrupted during the delay, execution is aborted and the task is not invoked.

### TimeoutTaskDecorator

This decorator enforces a **strict execution time limit** on individual tasks. It is primarily intended for concurrent execution, where the runner cannot reliably enforce per-task timeouts on its own.

If a task does not complete within the configured timeout, execution fails and the task is cancelled. Timeout enforcement is *best-effort* and relies on *cooperative interruption*; tasks that do not respond to interruption may continue running in the background.

The timeout applies to a single task invocation only. It does not represent a global timeout for the entire test run. When combined with other decorators, the order of decoration determines whether delays or other execution behavior count toward the timeout.

### ShortCircuitTaskDecorator

**Example 1: Global kill switch**

```java
AtomicBoolean disabled = new AtomicBoolean(false);

var decorator = new ShortCircuitTaskDecorator(disabled::get);
```

Flip the flag → all decorated tasks stop executing.

**Example 2: Execute only N times**

```java
AtomicInteger remaining = new AtomicInteger(3);

var decorator = new ShortCircuitTaskDecorator(
    () -> remaining.getAndDecrement() <= 0
);
```

After 3 executions → everything short-circuits.

**Example 3: Time-based cutoff**

```java
Instant deadline = Instant.now().plusSeconds(5);

var decorator = new ShortCircuitTaskDecorator(
    () -> Instant.now().isAfter(deadline)
);
```

Tasks stop executing after the deadline.

---

## Usage Examples