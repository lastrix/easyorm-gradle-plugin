package org.lastrix.easyorm.generator.hibernate;

import org.lastrix.easyorm.unit.dbm.Column;
import org.lastrix.easyorm.unit.dbm.View;
import org.lastrix.easyorm.unit.java.EntityClass;
import org.lastrix.easyorm.unit.java.EntityField;
import org.lastrix.easyorm.unit.java.Mapping;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.boot.jaxb.hbm.spi.*;

import java.util.List;

final class EntityMappingBuilder
{
	EntityMappingBuilder( EntityClass clazz, Dialect dialect )
	{
		this.clazz = clazz;
		this.dialect = dialect;
	}

	private final EntityClass clazz;
	private final Dialect dialect;
	private JaxbHbmRootEntityType entity;

	JaxbHbmHibernateMapping build()
	{
		buildImpl();

		JaxbHbmHibernateMapping hbm = new JaxbHbmHibernateMapping();
		hbm.getClazz().add( entity );
		return hbm;
	}

	private void buildImpl()
	{
		entity = new JaxbHbmRootEntityType();
		entity = new JaxbHbmRootEntityType();
		entity.setName( clazz.getClassName() );
		entity.setTable( dialect.entity( clazz.getEntity() ) );
		String schema = dialect.schema( clazz.getEntity().getSchema() );
		if( !StringUtils.isBlank( schema ) )
			entity.setSchema( schema );
		clazz.getFields().forEach( this :: buildField );
	}

	private void buildField( EntityField field )
	{
		String name = field.getName();
		if( "id".equals( name ) )
		{
			if( field.getEntityClass().getEntity() instanceof View )
			{
				JaxbHbmSimpleIdType value = new JaxbHbmSimpleIdType();
				value.setName( "id" );
				value.setColumnAttribute( "id" );
				entity.setId( value );
			}
			else
				entity.setId( dialect.id( field ) );
		}
		else if( "version".equals( name ) )
			buildVersionField( field );
		else if( field.getMappedField() == null )
			buildSimpleField( field );
		else if( field.getMapping() == Mapping.ONE_TO_MANY )
			buildOneToManyField( field );
		else if( field.getMapping() == Mapping.MANY_TO_ONE )
			buildManyToOneField( field );
		else if( field.getMapping() == Mapping.MANY_TO_MANY )
			buildManyToManyField( field );
		else
			throw new IllegalStateException();
	}

	private void buildVersionField( EntityField field )
	{
		JaxbHbmVersionAttributeType version = new JaxbHbmVersionAttributeType();
		version.setType( field.getTypeName() );
		version.setName( field.getName() );
		version.setColumnAttribute( dialect.column( field.getColumn() ) );
		entity.setVersion( version );
	}

	private void buildSimpleField( EntityField field )
	{
		JaxbHbmBasicAttributeType property = new JaxbHbmBasicAttributeType();
		property.setName( field.getName() );
		property.setTypeAttribute( field.getTypeName() );
		if( !field.isNullable() )
			property.setNotNull( true );
		JaxbHbmColumnType columnType = new JaxbHbmColumnType();
		columnType.setName( dialect.column( field.getColumn() ) );
		columnType.setSqlType( dialect.sqlType( field ) );
		if( field.getLength() > 0 )
			columnType.setLength( field.getLength() );
		property.getColumnOrFormula().add( columnType );
		entity.getAttributes().add( property );
	}

	private void buildOneToManyField( EntityField field )
	{
		if( field.getMappedField() == null )
			throw new IllegalStateException();

		JaxbHbmSetType st = new JaxbHbmSetType();
		st.setName( field.getName() );
		// always inverse
		st.setInverse( true );
		st.setFetch( JaxbHbmFetchStyleWithSubselectEnum.JOIN );
		JaxbHbmKeyType key = new JaxbHbmKeyType();
		key.setColumnAttribute( dialect.column( field.getMappedField().getColumn() ) );
		JaxbHbmOneToManyCollectionElementType otm = new JaxbHbmOneToManyCollectionElementType();
		otm.setClazz( field.getMappedField().getEntityClass().getClassName() );
		st.setOneToMany( otm );
		st.setKey( key );
		st.setLazy( JaxbHbmLazyWithExtraEnum.FALSE );
		String schema = dialect.schema( clazz.getEntity().getSchema() );
		if( !StringUtils.isBlank( schema ) )
			st.setSchema( schema );
		entity.getAttributes().add( st );
	}

	private void buildManyToOneField( EntityField field )
	{
		JaxbHbmManyToOneType ft = new JaxbHbmManyToOneType();
		ft.setName( field.getName() );
		ft.setClazz( field.getTypeName() );
		if( !field.isNullable() )
			ft.setNotNull( true );
		if( field.getColumn() == null )
			throw new IllegalStateException();
		ft.setColumnAttribute( dialect.column( field.getColumn() ) );
		ft.setLazy( JaxbHbmLazyWithNoProxyEnum.FALSE );
		entity.getAttributes().add( ft );
	}

	private void buildManyToManyField( EntityField field )
	{
		if( field.getMappedField() == null )
			throw new IllegalStateException();

		JaxbHbmSetType st = new JaxbHbmSetType();
		st.setName( field.getName() );
		if( field.isInverse() )
			st.setInverse( true );

		List<Column> columns = field.getMediator().getColumns();
		Column keyColumn = columns.get( field.isInverse() ? 1 : 0 );
		Column inverseColumn = columns.get( field.isInverse() ? 0 : 1 );

		JaxbHbmKeyType key = new JaxbHbmKeyType();
		key.setColumnAttribute( dialect.column( keyColumn ) );
		JaxbHbmManyToManyCollectionElementType mtm = new JaxbHbmManyToManyCollectionElementType();
		mtm.setClazz( field.getMappedField().getEntityClass().getClassName() );

		mtm.setColumnAttribute( dialect.column( inverseColumn ) );
		st.setManyToMany( mtm );
		st.setKey( key );
		st.setTable( dialect.entity( inverseColumn.getEntity() ) );
		st.setLazy( JaxbHbmLazyWithExtraEnum.FALSE );
		String schema = dialect.schema( clazz.getEntity().getSchema() );
		if( !StringUtils.isBlank( schema ) )
			st.setSchema( schema );
		entity.getAttributes().add( st );
	}
}
