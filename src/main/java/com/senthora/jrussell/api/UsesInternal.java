package com.senthora.jrussell.api;

import java.lang.annotation.*;

/**
 * Marks an API class that intentionally
 * references internal implementation types.
 * <p>
 * This annotation exists to make API-to-internal dependencies
 * explicit and reviewable. As a rule, API code should not reference
 * internal packages. Classes annotated with {@code @UsesInternal}
 * are deliberate exceptions to that rule and are expected to
 * have a stable, explicitly documented architectural
 * reason for crossing the boundary.</p>
 * <p>
 * This annotation does not relax visibility rules or
 * expose internal types to consumers. It is used solely
 * for architectural documentation and enforcement by tests.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@interface UsesInternal {}
