package org.lastrix.easyorm.generator.mssql;

import org.lastrix.easyorm.generator.GeneratorUtils;
import org.lastrix.easyorm.generator.hibernate.AbstractSqlDialect;
import org.lastrix.easyorm.unit.dbm.CascadeAction;
import org.lastrix.easyorm.unit.dbm.Entity;
import org.lastrix.easyorm.unit.dbm.ForeignKeyConstraint;
import org.lastrix.easyorm.unit.java.EntityField;
import org.hibernate.boot.jaxb.cfg.spi.JaxbCfgConfigPropertyType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmGeneratorSpecificationType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmSimpleIdType;
import org.hibernate.cfg.AvailableSettings;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MsSqlDialect extends AbstractSqlDialect
{
	private static final Map<String, String> KEYWORD_REMAP = new HashMap<>();

	static
	{
		KEYWORD_REMAP.put( "key", "_Key" );
		KEYWORD_REMAP.put( "index", "_Index" );
		KEYWORD_REMAP.put( "function", "_Function" );
		KEYWORD_REMAP.put( "user", "_User" );
	}

	private static final String DEFAULT_DDL_SETTING = "validate";

	@Override
	public void populateCfgProperties( List<JaxbCfgConfigPropertyType> list )
	{
		list.add( GeneratorUtils.createProperty( AvailableSettings.HBM2DDL_AUTO, DEFAULT_DDL_SETTING ) );
	}

	@NotNull
	@Override
	public String cascadeAction( @NotNull CascadeAction action )
	{
		switch( action )
		{
			case SET_NULL:
				return "SET NULL";

			case NO_ACTION:
			case RESTRICT:
				return "NO ACTION";

			case CASCADE:
				return action.name().toUpperCase();

			default:
				throw new UnsupportedOperationException( action.name() );
		}
	}

	@Override
	public String getName()
	{
		return "mssql";
	}

	@NotNull
	@Override
	public JaxbHbmSimpleIdType id( @NotNull EntityField field )
	{
		JaxbHbmSimpleIdType idCfg = new JaxbHbmSimpleIdType();
		idCfg.setName( field.getName() );
		idCfg.setColumnAttribute( column( field.getColumn() ) );
		JaxbHbmGeneratorSpecificationType generator = new JaxbHbmGeneratorSpecificationType();
		generator.setClazz( "identity" );
		idCfg.setGenerator( generator );
		return idCfg;
	}

	@NotNull
	@Override
	protected String toNotation( @NotNull String value )
	{
		String remappedKeyword = KEYWORD_REMAP.get( value.toLowerCase() );
		if( remappedKeyword != null )
			return remappedKeyword;

		// already in CamelCaseNotation
		if( Character.isLowerCase( value.charAt( 0 ) ) )
			return Character.toUpperCase( value.charAt( 0 ) ) + value.substring( 1 );
		return value;
	}

	@Override
	protected String getStringDataType( @NotNull EntityField field )
	{
		if( field.getLength() == -1 )
			return "TEXT";
		if( field.getLength() == 0 )
			throw new IllegalStateException( "Length must be set for field: " + field.getLength() );
		return "VARCHAR(" + field.getLength() + ')';
	}

	@NotNull
	@Override
	protected String getInstantDataType()
	{
		return "DATETIME";
	}

	@NotNull
	@Override
	protected String getBooleanDataType()
	{
		return "BIT";
	}

	@NotNull
	@Override
	protected String getNumericDataType()
	{
		return "BIGINT";
	}

	@NotNull
	@Override
	public String getForeignKeyConstraintName( ForeignKeyConstraint constraint )
	{
		if( constraint.getSource() == null )
			throw new IllegalStateException();

		Entity sourceTable = constraint.getSource().getEntity();
		return String.format( "FK_%s_%s", entity( sourceTable ), column( constraint.getSource() ) );
	}

	@NotNull
	@Override
	public String aggregateFunction( @NotNull String name )
	{
		return "COUNT".equalsIgnoreCase( name ) ? "COUNT_BIG" : name;
	}
}
