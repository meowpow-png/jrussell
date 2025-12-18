package com.senthora.jrussell;

import org.junit.jupiter.api.Tag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a test class as a unit test.
 * <p>
 * Unit tests verify the behavior of individual components
 * in isolation. They are typically focused on a single
 * class or small unit of logic and avoid external
 * dependencies or integration concerns.
 * <p>
 * This annotation is intended to make unit tests
 * easy to identify, group, and selectively
 * execute in build tools and IDEs.
 */
@Tag("unit")
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UnitTest {}
