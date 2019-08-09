package org.lastrix.easyorm.generator.mssql;

import org.lastrix.easyorm.generator.AbstractUninstallScriptGenerator;
import org.lastrix.easyorm.unit.Unit;
import org.lastrix.easyorm.unit.dbm.ForeignKeyConstraint;
import org.lastrix.easyorm.unit.dbm.Table;
import org.lastrix.easyorm.unit.dbm.View;

import java.io.File;

public final class MsSqlUninstallScript extends AbstractUninstallScriptGenerator {
    public MsSqlUninstallScript(Unit unit, File buildDir) {
        super(unit, buildDir, new MsSqlDialect());
    }

    @Override
    protected void dropFirst() {
        // do nothing
    }

    @Override
    protected void dropView(View view) {
        String name = getEntityName(view);
        append(String.format("IF (SCHEMA_ID('%s') IS NOT NULL)\n\tBEGIN\n\n", getSchema()))
                .append(String.format(
                        "\t\tIF OBJECT_ID('%s', 'V') IS NOT NULL\n" +
                                "\t\t\tDROP VIEW %s;\n\n",
                        name, name))
                .append("\tEND\nGO\n");
    }

    @Override
    protected void dropForeignKey(ForeignKeyConstraint constraint) {
        String constraintName = getDialect().getForeignKeyConstraintName(constraint);
        String name = getEntityName(constraint.getSource().getEntity());

        append(String.format("IF (SCHEMA_ID('%s') IS NOT NULL)\n\tBEGIN\n\n", getSchema()))
                .append(String.format(
                        "IF OBJECT_ID('%s.%s', 'F') IS NOT NULL\n\t\t\tALTER TABLE %s DROP CONSTRAINT %s;\n\n",
                        getSchema(), constraintName,
                        name, constraintName
                ))
                .append("\tEND\nGO\n");
    }

    @Override
    protected void dropTable(Table table) {
        String name = getEntityName(table);
        append(String.format("IF (SCHEMA_ID('%s') IS NOT NULL)\n\tBEGIN\n\n", getSchema()))
                .append(
                        String.format(
                                "\t\tIF OBJECT_ID('%s', 'U') IS NOT NULL\n" +
                                        "\t\t\tDROP TABLE %s;\n\n",
                                name, name))
                .append("\tEND\nGO\n");
    }

    @Override
    protected void dropLast() {
        // do nothing
    }
}
