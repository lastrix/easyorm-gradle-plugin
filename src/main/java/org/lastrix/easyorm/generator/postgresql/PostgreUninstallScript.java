package org.lastrix.easyorm.generator.postgresql;

import org.lastrix.easyorm.generator.AbstractUninstallScriptGenerator;
import org.lastrix.easyorm.unit.Unit;
import org.lastrix.easyorm.unit.dbm.Table;
import org.lastrix.easyorm.unit.dbm.View;

import java.io.File;

public final class PostgreUninstallScript extends AbstractUninstallScriptGenerator
{
	public PostgreUninstallScript( Unit unit, File buildDir )
	{
		super( unit, buildDir, new PostgreSqlDialect() );
	}

	@Override
	protected void dropFirst()
	{
		// do nothing
	}

	@Override
	protected void dropView( View view )
	{
		append( String.format( "DROP VIEW IF EXISTS %s CASCADE;\n", getEntityName( view ) ) );
	}

	@Override
	protected void dropForeignKeys( Table table )
	{
		// nothing to do
	}

	@Override
	protected void dropTable( Table table )
	{
		append( String.format( "DROP TABLE IF EXISTS %s CASCADE;\n", getEntityName( table ) ) );
		append( String.format( "DROP SEQUENCE IF EXISTS %s CASCADE;\n", table.getSchema() + ".sq_" + entity( table ) ) );
	}

	@Override
	protected void dropLast()
	{
		append( "\nDROP SCHEMA IF EXISTS " ).append( getSchema() ).append( " CASCADE;\n" );
	}
}
