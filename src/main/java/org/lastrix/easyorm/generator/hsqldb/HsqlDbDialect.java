package org.lastrix.easyorm.generator.hsqldb;

import org.lastrix.easyorm.generator.GeneratorUtils;
import org.lastrix.easyorm.generator.hibernate.AbstractSqlDialect;
import org.lastrix.easyorm.unit.dbm.Entity;
import org.lastrix.easyorm.unit.dbm.ForeignKeyConstraint;
import org.lastrix.easyorm.unit.java.EntityField;
import org.hibernate.boot.jaxb.cfg.spi.JaxbCfgConfigPropertyType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmGeneratorSpecificationType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmSimpleIdType;
import org.hibernate.cfg.AvailableSettings;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class HsqlDbDialect extends AbstractSqlDialect
{
	private static final String INSTALL_SCRIPT_PATH = "res/hibernate/hsqldb/install.sql";
	private static final String DEFAULT_DDL_SETTING = "validate";
	private static final String DEFAULT_DB_ACTION = "create";
	private static final String DEFAULT_DB_SOURCE = "script";

	@NotNull
	@Override
	public String schema( @NotNull String source )
	{
		// NO SCHEMA ALLOWED FOR HSQLDB
		return "";
	}

	@Override
	public void populateCfgProperties( List<JaxbCfgConfigPropertyType> list )
	{
		list.add( GeneratorUtils.createProperty( AvailableSettings.HBM2DDL_AUTO, DEFAULT_DDL_SETTING ) );
		list.add( GeneratorUtils.createProperty( AvailableSettings.HBM2DDL_DATABASE_ACTION, DEFAULT_DB_ACTION ) );
		list.add( GeneratorUtils.createProperty( AvailableSettings.HBM2DDL_CREATE_SOURCE, DEFAULT_DB_SOURCE ) );
		list.add( GeneratorUtils.createProperty( AvailableSettings.HBM2DDL_CREATE_SCRIPT_SOURCE, INSTALL_SCRIPT_PATH ) );
	}

	@Override
	public String getName()
	{
		return "hsqldb";
	}

	@NotNull
	@Override
	public JaxbHbmSimpleIdType id( @NotNull EntityField field )
	{
		JaxbHbmSimpleIdType idCfg = new JaxbHbmSimpleIdType();
		idCfg.setName( field.getName() );
		idCfg.setColumnAttribute( column( field.getColumn() ) );
		JaxbHbmGeneratorSpecificationType generator = new JaxbHbmGeneratorSpecificationType();
		generator.setClazz( "increment" );
		idCfg.setGenerator( generator );
		return idCfg;
	}

	@NotNull
	@Override
	protected String toNotation( @NotNull String value )
	{
		if( "count".equalsIgnoreCase( value ) )
			return "Z_COUNT";

		StringBuilder sb = new StringBuilder();
		int length = value.length();
		boolean underscore = false;
		for( int i = 0; i < length; i++ )
		{
			char c = value.charAt( i );
			if( Character.isUpperCase( c ) && i != 0 && !underscore )
				sb.append( '_' );

			sb.append( Character.toUpperCase( c ) );
			underscore = c == '_';
		}
		String result = sb.toString();
		if( result.charAt( 0 ) == '_' )
			return 'Z' + result;
		return result;
	}

	@Override
	protected String getStringDataType( @NotNull EntityField field )
	{
		if( field.getLength() == -1 )
			return "LONGVARCHAR";
		if( field.getLength() == 0 )
			throw new IllegalStateException( "Length must be set for field: " + field.getLength() );
		return "VARCHAR(" + field.getLength() + ')';
	}

	@NotNull
	@Override
	public String getForeignKeyConstraintName( @NotNull ForeignKeyConstraint constraint )
	{
		if( constraint.getSource() == null )
			throw new IllegalStateException();

		Entity sourceTable = constraint.getSource().getEntity();
		return String.format( "FK_%s_%s", entity( sourceTable ), column( constraint.getSource() ) );
	}
}
