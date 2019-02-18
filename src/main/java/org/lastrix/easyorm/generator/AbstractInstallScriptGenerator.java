package org.lastrix.easyorm.generator;

import org.lastrix.easyorm.generator.hibernate.Dialect;
import org.lastrix.easyorm.unit.Unit;
import org.lastrix.easyorm.unit.dbm.Table;
import org.lastrix.easyorm.unit.dbm.View;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public abstract class AbstractInstallScriptGenerator extends AbstractScriptGenerator
{
	protected AbstractInstallScriptGenerator( Unit unit, File buildDir, Dialect dialect )
	{
		super( unit, buildDir, dialect );
	}

	@NotNull
	@Override
	protected final String getScriptName()
	{
		return GeneratorUtils.INSTALL_SCRIPT;
	}

	@Override
	protected final void compileScript()
	{
		createFirst();

		getTables().forEach( this :: createTable );

		getTables().forEach( this :: createForeignKeys );

		getViews().forEach( this :: createView );

		compileLast();
	}

	protected abstract void createFirst();

	protected abstract void createTable( Table table );

	protected abstract void createForeignKeys( Table table );

	protected abstract void createView( View view );

	protected abstract void compileLast();
}
