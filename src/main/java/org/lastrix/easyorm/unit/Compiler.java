package org.lastrix.easyorm.unit;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lastrix.easyorm.conf.*;
import org.lastrix.easyorm.unit.dbm.*;
import org.lastrix.easyorm.unit.java.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class Compiler
{
	static final String NO_CLASS_ENTITY = "NOT-A-CLASS";

	public Compiler( Config config )
	{
		this.config = config;
	}

	private final Config config;
	private Unit unit;

	@NotNull
	public Unit compile()
	{
		unit = new Unit( config.getName(), config.getBasePackage(), config.getOwner(), config.getVersion() );
		config.getEntities().forEach( this :: register );
		config.getEntities().forEach( this :: processMappings );
		config.getEntities().forEach( this :: processMappingsLast );
		new ViewCompiler( config, unit ).compile();
		return unit;
	}

	private void register( ConfigEntity entity )
	{
		EntityClass entityClass = new EntityClass( getEntityClassName( entity.getName() ), entity.getProperties() );
		Table table = new Table( unit.getName(), entity.getName(), entityClass );
		entityClass.setEntity( table );
		unit.addClass( entityClass );
		unit.addTable( table );

		if( StringUtils.isBlank( entity.getId() ) )
			throw new IllegalArgumentException( "ID type is not set for " + entity.getName() );
		Column id = createSimpleField( entityClass, table, entity.getId(), "id", null );
		table.addConstraint( PrimaryKeyConstraint.build( table, id ) );
		if( !StringUtils.isBlank( entity.getVersion() ) )
			createSimpleField( entityClass, table, entity.getVersion(), "version", "0" );

		for( ConfigField field : entity.getFields() )
		{
			EntityField entityField = new EntityField( entityClass, field.getName(), field.getProperties() );
			entityClass.addField( entityField );
			entityField.setTypeName( field.getType() );
			entityField.setDefaultValue( field.getDefaultValue() );
			entityField.setNullable( field.isNullable() );
			entityField.setLength( field.getLength() );

			Column column = new Column( getColumnName( field ), table, entityField );
			table.addColumn( column );
			entityField.setColumn( column );
		}

		if( entity.getEquals() != null )
			entityClass.setEquals( collectFields( entityClass, entity.getEquals() ) );
		if( entity.getToString() != null )
			entityClass.setToString( collectFields( entityClass, entity.getToString() ) );
		if( entity.getCompareTo() != null )
			entityClass.setCompareTo( collectFields( entityClass, entity.getCompareTo() ) );

		if( entity.getConstructors() != null )
			entityClass.setConstructors( buildConstructors( entityClass, entity.getConstructors() ) );

		if( entity.getIndex() != null )
			processIndexes( table, entityClass, entity.getIndex() );
	}

	private static void processIndexes( Table table, EntityClass entityClass, Iterable<ConfigIndex> list )
	{
		for( ConfigIndex index : list )
			processIndex( table, entityClass, index );
	}

	private static void processIndex( Table table, EntityClass entityClass, ConfigIndex index )
	{
		if( index.getFields() == null || index.getFields().isEmpty() )
			throw new IllegalStateException( "Empty index for: " + entityClass.getClassName() );

		List<Column> columns = collectFields( entityClass, index.getFields() )
				.stream()
				.map( EntityField :: getColumn )
				.collect( Collectors.toList() );

		if( index.getType() == IndexType.UNIQUE )
			table.addConstraint( UniqueConstraint.create( table, columns ) );
		else
			table.addIndex( Index.create( index.getType(), table, columns ) );
	}

	private static List<EntityConstructor> buildConstructors(EntityClass entityClass, Collection<ConfigConstructor> constructors )
	{
		if( constructors.isEmpty() )
			return Collections.emptyList();
		List<EntityConstructor> list = new ArrayList<>();
		for( ConfigConstructor constructor : constructors )
		{
			if( constructor.getFields() == null || constructor.getFields().isEmpty() )
				throw new IllegalStateException( "Empty constructor for: " + entityClass.getClassName() );
			list.add( new EntityConstructor( entityClass, collectFields( entityClass, constructor.getFields() ) ) );
		}
		return list;
	}

	@NotNull
	private static List<EntityField> collectFields( @NotNull EntityClass entityClass, @NotNull Iterable<String> list )
	{
		List<EntityField> fields = new ArrayList<>();
		for( String name : list )
		{
			EntityField field = entityClass.findField( name );
			if( field == null )
				throw new IllegalStateException( "No field for name '" + name + "' in class: " + entityClass.getClassName() );
			fields.add( field );
		}
		return fields;
	}

	private void processMappings( ConfigEntity entity )
	{
		Table table = unit.findTable( entity.getName() );
		if( table == null )
			throw new IllegalStateException();
		EntityClass entityClass = table.getEntityClass();

		for( ConfigField field : entity.getFields() )
		{
			EntityField entityField = entityClass.findField( field.getName() );
			if( entityField == null )
				throw new IllegalStateException();
			processMapping( entityField, field );
		}
	}

	private void processMappingsLast( ConfigEntity entity )
	{
		Table table = unit.findTable( entity.getName() );
		if( table == null )
			throw new IllegalStateException();
		EntityClass entityClass = table.getEntityClass();

		for( ConfigField field : entity.getFields() )
		{
			EntityField entityField = entityClass.findField( field.getName() );
			if( entityField == null )
				throw new IllegalStateException();
			if( field.getOneToMany() != null )
				processOneToManyMapping( entityField, field );
		}
	}

	private void processMapping( EntityField entityField, ConfigField field )
	{
		int total = ( field.getManyToOne() == null ? 0 : 1 )
				+ ( field.getManyToMany() == null ? 0 : 1 )
				+ ( field.getOneToMany() == null ? 0 : 1 );
		if( total > 1 )
			throw new IllegalStateException( "Too many mappings for field: " + entityField.getFullName() );

		if( field.getManyToMany() != null )
			processManyToManyMapping( entityField, field );
		else if( field.getManyToOne() != null )
			processManyToOneMapping( entityField, field );
	}

	private void processOneToManyMapping( EntityField entityField, ConfigField field )
	{
		ConfigOneToMany mapping = field.getOneToMany();

		Table target = unit.findTable( mapping.getRef() );
		if( target == null )
			throw new IllegalStateException( "Unable to find ref: " + mapping.getRef() );

		entityField.setMapping( Mapping.ONE_TO_MANY );
		entityField.setMappingType( MappingType.SINGLE );
		entityField.setTypeName( field.getType() );
		entityField.setTypeParameters( Collections.singletonList( target.getEntityClass().getClassName() ) );
		if( StringUtils.isBlank( field.getType() ) )
			entityField.setMappingType( MappingType.SET );
		else
			entityField.setMappingType( MappingType.resolveMappingType( field.getType() ) );

		EntityField mappedField = target.getEntityClass().getFields()
				.stream()
				.filter( e -> e.getTypeName().equals( entityField.getEntityClass().getClassName() ) )
				.findFirst()
				.orElseThrow( () -> new IllegalStateException( "Unable to find one-to-many mapped field for: " + entityField.getFullName() ) );
		entityField.setMappedField( mappedField );
	}

	private void processManyToManyMapping( EntityField entityField, ConfigField field )
	{
		ConfigManyToAny mapping = field.getManyToMany();
		Table target = unit.findTable( mapping.getRef() );
		if( target == null )
			throw new IllegalStateException( "Unable to find ref: " + mapping.getRef() );

		entityField.setMapping( Mapping.MANY_TO_MANY );
		entityField.setInverse( mapping.isInverse() );
		entityField.setMappingType( MappingType.resolveMappingType( field.getType() ) );
		entityField.setTypeName( field.getType() );
		entityField.setTypeParameters( Collections.singletonList( target.getEntityClass().getClassName() ) );
		entityField.setMappedField( target.getEntityClass().findField( "id" ) );

		Table sourceTable = (Table)entityField.getEntityClass().getEntity();
		String mTableName = mapping.isInverse() ? createManyToManyTableName( target, sourceTable ) : createManyToManyTableName( sourceTable, target );
		Table mTable = unit.findTable( mTableName );
		if( mTable == null )
		{
			mTable = mapping.isInverse()
					? createManyToManyTable( target, sourceTable )
					: createManyToManyTable( sourceTable, target );
			unit.addTable( mTable );
		}
		entityField.setMediator( mTable );
		Column sourceColumn = sourceTable.findColumn( "id" );
		EntityField targetField = mTable.getEntityClass().findField( sourceTable.getName() );
		if( targetField == null )
			throw new IllegalStateException();
		Column targetColumn = targetField.getColumn();

		// lazy to rename
		if( sourceColumn == null || targetColumn == null )
			throw new IllegalStateException();
		mTable.addConstraint( ForeignKeyConstraint.create( targetColumn, sourceColumn, mapping ) );
	}

	private Table createManyToManyTable( Table source, Table target )
	{
		EntityClass mEntity = new EntityClass( NO_CLASS_ENTITY, null );
		Table mTable = new Table( unit.getName(), createManyToManyTableName( source, target ), mEntity );

		EntityField ourMappedField = source.findColumn( "id" ).getField();
		EntityField ourField = new EntityField( mEntity, source.getName(), ourMappedField.getProperties() );
		ourField.setMappedField( ourMappedField );
		Column ourColumn = new Column( ourField.getName() + "Id", mTable, ourField );
		mEntity.addField( ourField );
		mTable.addColumn( ourColumn );
		ourField.setColumn( ourColumn );

		EntityField dstMappedField = target.findColumn( "id" ).getField();
		EntityField dstField = new EntityField( mEntity, target.getName(), dstMappedField.getProperties() );
		dstField.setMappedField( dstMappedField );
		Column dstColumn = new Column( dstField.getName() + "Id", mTable, dstField );
		mEntity.addField( dstField );
		mTable.addColumn( dstColumn );
		dstField.setColumn( dstColumn );

		mTable.addConstraint( PrimaryKeyConstraint.build( mTable, ourColumn, dstColumn ) );
		return mTable;
	}

	private static String createManyToManyTableName( Table source, Table target )
	{
		return "__mm_" + source.getName() + "_to_" + target.getName();
	}

	private void processManyToOneMapping( EntityField entityField, ConfigField field )
	{
		ConfigManyToAny mapping = field.getManyToOne();
		Table target = unit.findTable( mapping.getRef() );
		if( target == null )
			throw new IllegalStateException( "Unable to find ref: " + mapping.getRef() );

		entityField.setMapping( Mapping.MANY_TO_ONE );
		entityField.setMappingType( MappingType.SINGLE );
		entityField.setTypeName( target.getEntityClass().getClassName() );
		EntityField targetField = target.getEntityClass().findField( "id" );
		assert targetField != null;
		entityField.setMappedField( targetField );

		( (Table)entityField.getEntityClass().getEntity() )
				.addConstraint( ForeignKeyConstraint.create( entityField.getColumn(), targetField.getColumn(), mapping ) );
	}

	private static Column createSimpleField( EntityClass entityClass, Table table, String typeName, String name, @Nullable String defaultValue )
	{
		EntityField field = new EntityField( entityClass, name, null );
		field.setTypeName( typeName );
		field.setDefaultValue( defaultValue );
		Column column = new Column( name, table, field );
		field.setColumn( column );
		entityClass.addField( field );
		table.addColumn( column );
		return column;
	}

	private static String getColumnName( ConfigField field )
	{
		if( field.getManyToOne() != null )
			return field.getName() + "Id";

		return field.getName();
	}

	private String getEntityClassName( String entity )
	{
		return config.getBasePackage() + '.' + entity + "Entity";
	}
}
