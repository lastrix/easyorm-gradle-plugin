package org.lastrix.easyorm.generator.hibernate;

import org.lastrix.easyorm.unit.dbm.CascadeAction;
import org.lastrix.easyorm.unit.java.EntityField;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings( {"MethodMayBeStatic", "WeakerAccess"} )
public abstract class AbstractSqlDialect implements Dialect
{
	@NotNull
	@Override
	public String cascadeAction( @NotNull CascadeAction action )
	{
		switch( action )
		{
			case SET_NULL:
				return "SET NULL";

			case NO_ACTION:
				return "NO ACTION";

			case CASCADE:
			case RESTRICT:
				return action.name().toUpperCase();

			default:
				throw new UnsupportedOperationException( action.name() );
		}
	}

	@NotNull
	@Override
	public final String column( @NotNull String name )
	{
		return toNotation( name );
	}

	@NotNull
	@Override
	public final String entity( @NotNull String name )
	{
		return toNotation( name );
	}

	@NotNull
	@Override
	public final String sqlType( @NotNull EntityField field )
	{
		if( field.getMappedField() != null )
			return sqlType( field.getMappedField() );

		if( StringUtils.isBlank( field.getTypeName() ) )
			throw new IllegalArgumentException( "Field type is blank" );

		switch( field.getTypeName() )
		{
			case "java.lang.Short":
				return getShortDataType();

			case "java.lang.Integer":
				return getIntegerDataType();

			case "java.lang.Long":
				return getLongDataType();

			case "java.lang.Number":
				return getNumericDataType();

			case "java.time.Instant":
				return getInstantDataType();

			case "java.lang.Boolean":
				return getBooleanDataType();

			case "java.lang.String":
				return getStringDataType( field );

			default:
				throw new UnsupportedOperationException( "Unable to handle: " + field.getTypeName() );
		}
	}

	protected String getStringDataType( @NotNull EntityField field )
	{
		if( field.getLength() == -1 )
			return "VARCHAR";
		if( field.getLength() == 0 )
			throw new IllegalStateException( "Length must be set for field: " + field.getLength() );
		return "VARCHAR(" + field.getLength() + ')';
	}

	@NotNull
	protected String getBooleanDataType()
	{
		return "BOOLEAN";
	}

	@NotNull
	protected String getInstantDataType()
	{
		return "TIMESTAMP WITH TIME ZONE";
	}

	@NotNull
	protected String getNumericDataType()
	{
		return "NUMERIC";
	}

	@NotNull
	protected String getLongDataType()
	{
		return "BIGINT";
	}

	@NotNull
	protected String getIntegerDataType()
	{
		return "INTEGER";
	}

	@NotNull
	protected String getShortDataType()
	{
		return "SMALLINT";
	}

	@NotNull
	protected abstract String toNotation( @NotNull String value );
}
