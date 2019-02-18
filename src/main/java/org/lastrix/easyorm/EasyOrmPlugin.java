package org.lastrix.easyorm;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.jetbrains.annotations.NotNull;
import org.lastrix.easyorm.conf.Config;
import org.lastrix.easyorm.generator.hibernate.HibernateConfigurationCompiler;
import org.lastrix.easyorm.generator.hsqldb.HsqlDbDialect;
import org.lastrix.easyorm.generator.hsqldb.HsqlDbInstallScript;
import org.lastrix.easyorm.generator.java.JavaGenerator;
import org.lastrix.easyorm.generator.mssql.MsSqlDialect;
import org.lastrix.easyorm.generator.mssql.MsSqlInstallScript;
import org.lastrix.easyorm.generator.mssql.MsSqlUninstallScript;
import org.lastrix.easyorm.generator.postgresql.PostgreInstallScript;
import org.lastrix.easyorm.generator.postgresql.PostgreSqlDialect;
import org.lastrix.easyorm.generator.postgresql.PostgreUninstallScript;
import org.lastrix.easyorm.unit.Compiler;
import org.lastrix.easyorm.unit.Unit;

import java.io.File;

public class EasyOrmPlugin implements Plugin<Project> {
    private static final String EXTENSION_NAME = "EasyOrm";
    private static final String DIR_GENERATED_SRC = "generated-src";
    private static final String DIR_GENERATED_RESOURCES = "generated-resources";

    @Override
    public void apply(@NotNull Project target) {
        target.getExtensions().add(EXTENSION_NAME, EasyOrmExtension.class);
        target.afterEvaluate(EasyOrmPlugin::afterEvaluate);
    }

    private static void afterEvaluate(Project project) {
        EasyOrmExtension extension =
                (EasyOrmExtension) project
                        .getExtensions()
                        .getByName(EXTENSION_NAME);

        assertFileExist(extension.model);
        assertFileExist(extension.javaTemplate);

        createTask(project, extension);
    }

    private static void assertFileExist(File file) {
        if (file == null || !file.exists() || file.isDirectory())
            throw new IllegalArgumentException("File does not exist or directory: " + file);
    }

    private static void createTask(Project project, EasyOrmExtension extension) {
        File buildDir = project.getBuildDir();
        File generatedSourceDir = javaDir(extension, buildDir);
        ensureFolderExists(generatedSourceDir);
        File generatedResourcesDir = resourcesDir(extension, buildDir);
        ensureFolderExists(generatedResourcesDir);

        Task task = project.task("easyOrm");
        task.setDescription("Generated database model files");
        task.getInputs().file(extension.model);
        task.getInputs().file(extension.javaTemplate);
        task.getOutputs().file(generatedSourceDir);
        project.getTasks().findByName("compileJava").dependsOn(task);
        project.getTasks().findByName("processResources").dependsOn(task);

        task.doFirst(t -> {
            Config config = Config.readFrom(extension.model);
            Unit unit = new Compiler(config).compile();
            new JavaGenerator(unit, generatedSourceDir, extension.javaTemplate).generate();
            if (extension.postgre) {
                new HibernateConfigurationCompiler(unit, new PostgreSqlDialect(), generatedResourcesDir).compile();
                new PostgreInstallScript(unit, generatedResourcesDir).generate();
                new PostgreUninstallScript(unit, generatedResourcesDir).generate();
            }

            if (extension.mssql) {
                new HibernateConfigurationCompiler(unit, new MsSqlDialect(), generatedResourcesDir).compile();
                new MsSqlInstallScript(unit, generatedResourcesDir).generate();
                new MsSqlUninstallScript(unit, generatedResourcesDir).generate();
            }

            if (extension.hsqldb) {
                new HibernateConfigurationCompiler(unit, new HsqlDbDialect(), generatedResourcesDir).compile();
                new HsqlDbInstallScript(unit, generatedResourcesDir).generate();
            }
        });
    }

    private static File javaDir(EasyOrmExtension extension, File buildDir) {
        if (extension.javaOutput == null)
            return new File(buildDir, DIR_GENERATED_SRC);
        return extension.javaOutput;
    }

    private static File resourcesDir(EasyOrmExtension extension, File buildDir) {
        if (extension.resourcesOutput == null)
            return new File(buildDir, DIR_GENERATED_RESOURCES);
        return extension.resourcesOutput;
    }

    private static void ensureFolderExists(File folder) {
        if (!folder.exists() && !folder.mkdirs() || !folder.isDirectory())
            throw new IllegalStateException("Unable to mkdirs: " + folder);
    }
}
