package org.lastrix.easyorm.unit.dbm;

import org.lastrix.easyorm.unit.java.EntityClass;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode( of = "name" )
@ToString( of = "name" )
public final class Table implements Entity
{
	public Table( String schema, String name, EntityClass entityClass )
	{
		this.schema = schema;
		this.name = name;
		this.entityClass = entityClass;
	}

	private final String schema;
	private final String name;
	private final EntityClass entityClass;
	private final List<Column> columns = new ArrayList<>();
	private List<Constraint> constraints;
	private List<Index> indices;

	public void addColumn( @NotNull Column column )
	{
		if( findColumn( column.getName() ) != null )
			throw new IllegalArgumentException( "Duplicate column '" + column.getName() + "' for table '" + getName() + '\'' );

		columns.add( column );
	}

	public void addConstraint( @NotNull Constraint constraint )
	{
		if( constraints != null && constraints.contains( constraint ) )
			throw new IllegalArgumentException( "Duplicate constraint for table '" + getName() + "': " + constraint );

		if( constraints == null )
			constraints = new ArrayList<>();

		constraints.add( constraint );
	}

	public void addIndex( @NotNull Index index )
	{
		if( indices == null )
			indices = new ArrayList<>();

		indices.add( index );
	}
}
