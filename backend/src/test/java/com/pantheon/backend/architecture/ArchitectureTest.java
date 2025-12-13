package com.pantheon.backend.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;


import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packages = "com.pantheon.backend", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

    /**
     * Rule 1: Layered Architecture
     * Matches your folders: 'web', 'service', 'repositories', 'integration'
     */
    @ArchTest
    static final ArchRule layer_dependencies_are_respected = layeredArchitecture()
            .consideringOnlyDependenciesInAnyPackage("com.pantheon.backend..")
            .layer("Service").definedBy("..service..")
            .layer("Persistence").definedBy("..repository..")
            .layer("Integration").definedBy("..integration..")
            .whereLayer("Persistence").mayOnlyBeAccessedByLayers("Service", "Integration")
            .whereLayer("Integration").mayOnlyBeAccessedByLayers("Service");

    @ArchTest
    static final ArchRule services_should_be_named_correctly = classes()
            .that().resideInAPackage("..service..")
            .should().haveSimpleNameEndingWith("Service");

    @ArchTest
    static final ArchRule clients_should_be_named_correctly = classes()
            .that().resideInAPackage("..client..")
            .should().haveSimpleNameEndingWith("Client");

    @ArchTest
    static final ArchRule no_cycles = slices()
            .matching("com.pantheon.backend.(*)..")
            .should().beFreeOfCycles();
}
