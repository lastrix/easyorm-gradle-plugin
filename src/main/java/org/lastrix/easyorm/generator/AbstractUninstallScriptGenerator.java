package org.lastrix.easyorm.generator;

import org.lastrix.easyorm.generator.hibernate.Dialect;
import org.lastrix.easyorm.unit.Unit;
import org.lastrix.easyorm.unit.dbm.ForeignKeyConstraint;
import org.lastrix.easyorm.unit.dbm.Table;
import org.lastrix.easyorm.unit.dbm.View;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public abstract class AbstractUninstallScriptGenerator extends AbstractScriptGenerator
{
	protected AbstractUninstallScriptGenerator( Unit unit, File buildDir, Dialect dialect )
	{
		super( unit, buildDir, dialect );
	}

	@NotNull
	@Override
	protected final String getScriptName()
	{
		return GeneratorUtils.UNINSTALL_SCRIPT;
	}

	@Override
	protected final void compileScript()
	{
		dropFirst();
		getViews().forEach( this :: dropView );
		getTables().forEach( this :: dropForeignKeys );
		getTables().forEach( this :: dropTable );
		dropLast();
	}


	protected abstract void dropFirst();

	protected abstract void dropView( View view );

	protected void dropForeignKeys( Table table )
	{
		table.getConstraints().stream().filter( e -> e instanceof ForeignKeyConstraint )
				.map( e -> (ForeignKeyConstraint)e )
				.forEach( this :: dropForeignKey );
	}

	protected void dropForeignKey( ForeignKeyConstraint constraint )
	{

	}

	protected abstract void dropTable( Table table );

	protected abstract void dropLast();
}
