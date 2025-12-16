---
title: "JRussell Design"
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