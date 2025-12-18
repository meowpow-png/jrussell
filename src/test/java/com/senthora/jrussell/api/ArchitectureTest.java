package com.senthora.jrussell.api;

import com.senthora.jrussell.ArchTest;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@ArchTest
class ArchitectureTest {

    private static final String ROOT_PACKAGE = "com.senthora.jrussell";

    private static final ArchRule API_DOES_NOT_ACCESS_INTERNAL =
            ArchRuleDefinition.noClasses()
                    .that().resideInAPackage("..api..")
                    .and().areNotAnnotatedWith(UsesInternal.class)
                    .should().dependOnClassesThat()
                    .resideInAPackage("..internal..");

    private static final ArchRule API_DOES_NOT_USE_ANNOTATIONS =
            ArchRuleDefinition.noClasses()
                    .that().resideInAPackage("..api..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("org.jetbrains.annotations..");

    @Test
    @DisplayName("Should fail when not permitted API classes depend on internals")
    void should_Fail_when_NotPermittedApiClassesDependOnInternals() {
        API_DOES_NOT_ACCESS_INTERNAL.check(importedClasses());
    }

    @Test
    @DisplayName("Should fail when API classes use Jetbrains annotations")
    void should_Fail_when_ApiClassesUseJetbrainsAnnotations() {
        API_DOES_NOT_USE_ANNOTATIONS.check(importedClasses());
    }

    private static JavaClasses importedClasses() {
        return new ClassFileImporter().importPackages(ROOT_PACKAGE);
    }
}
