package org.lastrix.easyorm.unit;

import org.lastrix.easyorm.queryLanguage.AqlUtil;
import org.lastrix.easyorm.conf.Config;
import org.lastrix.easyorm.conf.ConfigField;
import org.lastrix.easyorm.conf.ConfigOneToMany;
import org.lastrix.easyorm.conf.ConfigViewEntity;
import org.lastrix.easyorm.unit.dbm.Column;
import org.lastrix.easyorm.unit.dbm.Entity;
import org.lastrix.easyorm.unit.dbm.View;
import org.lastrix.easyorm.unit.dbm.expr.EntityJoin;
import org.lastrix.easyorm.unit.dbm.expr.Expression;
import org.lastrix.easyorm.unit.dbm.expr.FieldJoin;
import org.lastrix.easyorm.unit.java.EntityClass;
import org.lastrix.easyorm.unit.java.EntityField;
import org.lastrix.easyorm.unit.java.Mapping;
import org.lastrix.easyorm.unit.java.MappingType;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.lastrix.easyorm.queryLanguage.object.*;

import java.util.Collections;

public class ViewCompiler
{
	public ViewCompiler( Config config, Unit unit )
	{
		this.config = config;
		this.unit = unit;
		resolver = new Resolver( unit );
		eCompiler = new ExpressionCompiler( unit, resolver );
	}

	private final Config config;
	private final Unit unit;
	private View view;
	private final Resolver resolver;
	private final ExpressionCompiler eCompiler;

	public void compile()
	{
		for( ConfigViewEntity configViewEntity : config.getViewEntities() )
		{
			try
			{
				compileView( configViewEntity );
			} catch( Exception e )
			{
				throw new IllegalStateException( "Unable to compile view: " + configViewEntity.getName(), e );
			}
		}
	}

	private void compileView( ConfigViewEntity entity )
	{
		EntityClass mEntity = new EntityClass( getViewClassName( entity ), entity.getProperties() );
		if( entity.isEntity() )
			unit.addClass( mEntity );

		String text = StringUtils.join( entity.getExpr(), "" );
		ViewTemplate template = AqlUtil.parse( entity.getName(), text );

		view = new View( config.getName(), entity.getName(), mEntity );
		resolver.setView( view );
		mEntity.setEntity( view );
		unit.addView( view );
		view.setFrom( (EntityJoin)compileJoin( template.getFrom() ) );
		for( Join join : template.getJoins() )
			view.getJoins().add( compileJoin( join ) );

		template.getFields().forEach( this :: compileViewField );

		if( entity.getFields() != null )
			entity.getFields().forEach( this :: compileOneToManyField );

		for( ViewField field : template.getGroupBy() )
		{
			EntityField groupField = mEntity.findField( field.getName() );
			if( groupField == null )
				throw new IllegalStateException( "Unable to find groupBy field: " + field.getName() );
			view.getGroupBy().add( groupField.getColumn() );
		}

		if( template.getWhere() != null )
			view.setWhere( eCompiler.compileExpression( template.getWhere() ) );
	}

	private void compileOneToManyField( ConfigField cf )
	{
		EntityField field = new EntityField( view.getEntityClass(), cf.getName(), cf.getProperties() );
		view.getEntityClass().addField( field );
		field.setTypeName( cf.getType() );

		ConfigOneToMany otm = cf.getOneToMany();
		if( otm == null )
			throw new IllegalStateException( "Fields for view must be One-To-Many" );

		Entity entity = unit.findEntity( otm.getRef() );
		if( entity == null )
			throw new IllegalStateException( "Unable to resolve entity: " + otm.getRef() );

		EntityField mappedField = entity.getEntityClass().findField( otm.getField() );
		if( mappedField == null )
			throw new IllegalStateException( "No field '" + otm.getField() + "' for entity: " + entity.getName() );

		field.setTypeParameters( Collections.singletonList( entity.getEntityClass().getClassName() ) );
		field.setMappedField( mappedField );
		field.setMapping( Mapping.ONE_TO_MANY );
		field.setMappingType( MappingType.resolveMappingType( cf.getType() ) );
	}

	private void compileViewField( ViewField vf )
	{
		Expression compiled = eCompiler.compileExpression( vf.getExpression() );

		EntityField field = compiled.asField();
		if( field != null )
		{
			compileViewFieldFromSource( vf, compiled, field, false );
			return;
		}

		Entity entity = compiled.asEntity();
		if( entity != null )
		{
			Column id = entity.findColumn( "id" );
			if( id == null )
				throw new IllegalStateException();
			compileViewFieldFromSource( vf, compiled, id.getField(), true );
			return;
		}

		String aggregateType = compiled.getAggregateType();
		if( aggregateType != null )
			createAggregateField( vf, compiled, aggregateType );
	}

	private void createAggregateField( ViewField vf, Expression compiled, String aggregateType )
	{
		EntityField field = new EntityField( view.getEntityClass(), vf.getName(), null );
		view.getEntityClass().addField( field );
		Column column = new Column( vf.getName(), view, field );
		field.setColumn( column );
		view.getColumns().add( column );
		field.setTypeName( aggregateType );
		view.getColumnSources().put( column, compiled );
	}

	private void compileViewFieldFromSource( ViewField vf, Expression compiled, EntityField source, boolean direct )
	{
		EntityField field = new EntityField( view.getEntityClass(), vf.getName(), null );
		view.getEntityClass().addField( field );
		Column column = new Column( source.getMappedField() == null && !direct ? vf.getName() : vf.getName() + "Id", view, field );
		field.setColumn( column );
		view.getColumns().add( column );

		if( direct )
		{
			field.setTypeName( source.getEntityClass().getClassName() );
			field.setMappedField( source.getEntityClass().findField( "id" ) );
			field.setMappingType( MappingType.SINGLE );
			field.setMapping( Mapping.MANY_TO_ONE );
		}
		else if( source.getMappedField() == null )
		{
			field.setTypeName( source.getTypeName() );
			if( String.class.getTypeName().equals( field.getTypeName() ) )
				field.setLength( -1 );
		}
		else
		{
			field.setMappedField( source );
			field.setTypeName( source.getTypeName() );
			field.setTypeParameters( source.getTypeParameters() );
			field.setMapping( Mapping.MANY_TO_ONE );
			field.setMappingType( MappingType.SINGLE );
		}
		view.getColumnSources().put( column, compiled );
	}

	@NotNull
	private Expression compileJoin( Join join )
	{
		if( join instanceof JoinEntity)
			return compileJoinEntity( (JoinEntity)join );

		if( join instanceof JoinField)
			return compileJoinField( (JoinField)join );

		throw new UnsupportedOperationException( join.getClass().getTypeName() );
	}

	private Expression compileJoinField( JoinField join )
	{
		FieldRef ref = join.getField();
		Join sourceJoin = join.getField().getJoin();
		if( sourceJoin == null )
			throw new IllegalStateException();
		String joinAlias = sourceJoin.getAlias();
		Entity joinEntity = resolveEntity( sourceJoin );

		Column joinColumn = joinEntity.getColumns().stream()
				.filter( e -> e.getField().getName().equals( ref.getFieldName() ) )
				.findFirst()
				.orElseThrow( () ->
						              new IllegalStateException( "No field: " + join.getField().getJoin().getAlias() + '.' + ref.getFieldName() ) );

		EntityField joinField = joinColumn.getField();
		String entityName = joinField.getMapping() == Mapping.ONE_TO_MANY || joinField.getMapping() == Mapping.MANY_TO_MANY
				? joinField.getTypeParameters().get( 0 )
				: joinField.getTypeName();

		EntityClass entity = unit.findClass( entityName );
		if( entity == null )
			throw new IllegalStateException( "No class: " + entityName );

		return new FieldJoin( join.isLeft(), join.getAlias(), joinAlias, joinEntity, joinColumn, entity.getEntity() );
	}

	@NotNull
	private Entity resolveEntity( @NotNull Join join )
	{

		if( join instanceof JoinEntity )
			return resolveEntityImpl( ( (JoinEntity)join ).getEntity() );

		if( join instanceof JoinField )
			return resolveJoinByAlias( join.getAlias() );

		throw new UnsupportedOperationException( join.getClass().getTypeName() );
	}

	private Entity resolveJoinByAlias( String alias )
	{
		Entity entity = resolver.resolveEntityByAlias( alias );
		if( entity == null )
			throw new IllegalArgumentException( "No join for alias: " + alias );
		return entity;
	}

	private Entity resolveEntityImpl( String entity )
	{
		Entity result = resolver.resolveEntityByName( entity );
		if( result == null )
			throw new IllegalArgumentException( "No entity for name: " + entity );
		return result;
	}

	private Expression compileJoinEntity( JoinEntity join )
	{
		return new EntityJoin(
				join.isLeft(),
				join.getAlias(),
				resolveEntityImpl( join.getEntity() ),
				join.getExpression() == null ? null : eCompiler.compileExpression( join.getExpression() ) );
	}

	@NotNull
	private String getViewClassName( ConfigViewEntity entity )
	{
		return entity.isEntity() ? getEntityClassName( entity.getName() ) : Compiler.NO_CLASS_ENTITY;
	}

	private String getEntityClassName( String entity )
	{
		return config.getBasePackage() + '.' + entity + "Entity";
	}
}
