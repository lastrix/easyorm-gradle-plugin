package org.lastrix.easyorm.generator.mssql;

import org.lastrix.easyorm.generator.AbstractSqlExpressionCompiler;
import org.lastrix.easyorm.unit.dbm.expr.Binary;
import org.lastrix.easyorm.unit.dbm.expr.Call;
import org.lastrix.easyorm.unit.dbm.expr.object.BooleanObject;
import org.lastrix.easyorm.unit.dbm.expr.object.FieldObject;
import org.lastrix.easyorm.unit.dbm.expr.object.StringObject;
import org.jetbrains.annotations.NotNull;

public final class MsSqlExpressionCompiler extends AbstractSqlExpressionCompiler
{
	private boolean insideExec;

	public MsSqlExpressionCompiler()
	{
		super( new MsSqlDialect() );
	}

	public boolean isInsideExec()
	{
		return insideExec;
	}

	public void setInsideExec( boolean insideExec )
	{
		this.insideExec = insideExec;
	}

	@Override
	protected void compileString( @NotNull StringObject object )
	{
		if( insideExec )
			append( " ''" ).append( object.getValue() ).append( "'' " );
		else
			super.compileString( object );
	}

	@Override
	protected void compileBoolean( @NotNull BooleanObject object )
	{
		append( object.isValue() ? 1 : 0 );
	}

	private boolean insideBinary;
	private boolean insideCall;

	@Override
	protected void compileBinary( @NotNull Binary binary )
	{
		insideBinary = true;
		super.compileBinary( binary );
		insideBinary = false;
	}

	@Override
	protected void compileField( @NotNull FieldObject object )
	{
		super.compileField( object );
		if( !insideCall && !insideBinary && Boolean.class.getTypeName().equals( object.getField().getTypeName() ) )
			append( " = 1" );
	}

	@Override
	protected void compileCall( @NotNull Call call )
	{
		insideCall = true;
		super.compileCall( call );
		insideCall = false;
	}
}
