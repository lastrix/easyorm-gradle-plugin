package org.lastrix.easyorm.unit.dbm;

import org.lastrix.easyorm.unit.java.EntityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Entity extends Comparable<Entity>
{
	@Nullable
	default Column findColumn( @NotNull String name )
	{
		return getColumns()
				.stream()
				.filter( e -> e.getName().equals( name ) )
				.findFirst()
				.orElse( null );
	}

	@NotNull
	String getSchema();

	@NotNull
	String getName();

	@NotNull
	EntityClass getEntityClass();

	@NotNull
	List<Column> getColumns();

	@Override
	default int compareTo( @NotNull Entity o )
	{
		return getName().compareTo( o.getName() );
	}
}
