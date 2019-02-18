package org.lastrix.easyorm.generator.postgresql;

import org.lastrix.easyorm.generator.GeneratorUtils;
import org.lastrix.easyorm.generator.hibernate.AbstractSqlDialect;
import org.lastrix.easyorm.unit.dbm.Entity;
import org.lastrix.easyorm.unit.dbm.ForeignKeyConstraint;
import org.lastrix.easyorm.unit.java.EntityField;
import org.hibernate.boot.jaxb.cfg.spi.JaxbCfgConfigPropertyType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmConfigParameterType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmGeneratorSpecificationType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmSimpleIdType;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public final class PostgreSqlDialect extends AbstractSqlDialect
{
	private static final String DEFAULT_DDL_SETTING = "validate";

	@Override
	public void populateCfgProperties( List<JaxbCfgConfigPropertyType> list )
	{
		list.add( GeneratorUtils.createProperty( AvailableSettings.HBM2DDL_AUTO, DEFAULT_DDL_SETTING ) );
	}

	@Override
	public String getName()
	{
		return "postgre";
	}

	@NotNull
	@Override
	public JaxbHbmSimpleIdType id( @NotNull EntityField field )
	{
		JaxbHbmSimpleIdType idCfg = new JaxbHbmSimpleIdType();
		idCfg.setName( field.getName() );
		idCfg.setColumnAttribute( column( field.getColumn() ) );
		JaxbHbmGeneratorSpecificationType generator = new JaxbHbmGeneratorSpecificationType();
		generator.setClazz( SequenceStyleGenerator.class.getTypeName() );
		fillGeneratorParams( generator.getConfigParameters(), field );
		idCfg.setGenerator( generator );
		return idCfg;
	}

	@NotNull
	@Override
	protected String toNotation( @NotNull String value )
	{
		StringBuilder sb = new StringBuilder();
		int length = value.length();
		boolean underscore = false;
		for( int i = 0; i < length; i++ )
		{
			char c = value.charAt( i );
			if( Character.isUpperCase( c ) )
			{
				if( i != 0 && !underscore )
					sb.append( '_' );
				sb.append( Character.toLowerCase( c ) );
			}
			else
				sb.append( c );

			underscore = c == '_';
		}
		return sb.toString();
	}

	@NotNull
	@Override
	public String getForeignKeyConstraintName( @NotNull ForeignKeyConstraint constraint )
	{
		if( constraint.getSource() == null )
			throw new IllegalStateException();

		Entity sourceTable = constraint.getSource().getEntity();
		return String.format( "fk_%s_%s", entity( sourceTable ), column( constraint.getSource() ) );
	}

	private void fillGeneratorParams( Collection<JaxbHbmConfigParameterType> params, EntityField field )
	{
		JaxbHbmConfigParameterType optimizer = new JaxbHbmConfigParameterType();
		optimizer.setName( "optimizer" );
		optimizer.setValue( "none" );
		params.add( optimizer );

		JaxbHbmConfigParameterType incrementSize = new JaxbHbmConfigParameterType();
		incrementSize.setName( "increment_size" );
		incrementSize.setValue( "1" );
		params.add( incrementSize );

		JaxbHbmConfigParameterType sequenceName = new JaxbHbmConfigParameterType();
		sequenceName.setName( "sequence_name" );
		Entity table = field.getColumn().getEntity();
		sequenceName.setValue( table.getSchema() + ".sq_" + entity( table ) );
		params.add( sequenceName );
	}
}
