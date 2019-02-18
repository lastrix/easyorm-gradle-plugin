package org.lastrix.easyorm.unit;

import org.lastrix.easyorm.unit.dbm.Entity;
import org.lastrix.easyorm.unit.dbm.Table;
import org.lastrix.easyorm.unit.dbm.View;
import org.lastrix.easyorm.unit.java.EntityClass;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode( of = "name" )
@ToString( of = "name" )
public final class Unit
{
	public Unit( @NotNull String name, @NotNull String basePackage, String owner, int version )
	{
		this.name = name;
		this.basePackage = basePackage;
		this.owner = owner;
		this.version = version;
		classes = new ArrayList<>();
		tables = new ArrayList<>();
		views = new ArrayList<>();
	}

	private final String name;
	private final String basePackage;
	private final String owner;
	private final int version;
	private List<EntityClass> classes;
	private List<Table> tables;
	private List<View> views;

	public void addClass( @NotNull EntityClass entityClass )
	{
		if( findClass( entityClass.getClassName() ) != null )
			throw new IllegalArgumentException( "Duplicate entityClass: " + entityClass.getClassName() );
		classes.add( entityClass );
	}

	public void addTable( @NotNull Table table )
	{
		if( findTable( table.getName() ) != null
				|| findView( table.getName() ) != null )
			throw new IllegalArgumentException( "Duplicate entity: " + table.getName() );
		tables.add( table );
	}

	public void addView( @NotNull View view )
	{
		if( findTable( view.getName() ) != null
				|| findView( view.getName() ) != null )
			throw new IllegalArgumentException( "Duplicate entity: " + view.getName() );
		views.add( view );
	}

	@Nullable
	public EntityClass findClass( @NotNull String className )
	{
		return classes
				.stream()
				.filter( e -> e.getClassName().equals( className ) )
				.findFirst()
				.orElse( null );
	}

	@Nullable
	public Entity findEntity( String name )
	{
		Table table = findTable( name );
		if( table == null )
			return findView( name );
		return table;
	}

	@Nullable
	public Table findTable( @NotNull String name )
	{
		return tables
				.stream()
				.filter( e -> e.getName().equals( name ) )
				.findFirst()
				.orElse( null );
	}

	@Nullable
	public View findView( @NotNull String name )
	{
		return views
				.stream()
				.filter( e -> e.getName().equals( name ) )
				.findFirst()
				.orElse( null );
	}
}
