package com.senthora.jrussell;

import org.junit.jupiter.api.Tag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a test class as an architecture test.
 * <p>
 * Architecture tests verify structural and dependency rules
 * rather than runtime behavior. They are typically used
 * to enforce package boundaries, layering constraints,
 * and other architectural invariants.
 * <p>
 * This annotation is intended to make architectural
 * tests easy to identify, group, and selectively
 * execute in build tools and IDEs.
 */
@Tag("arch")
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ArchTest {}
