package org.lastrix.easyorm.unit.java;

import org.lastrix.easyorm.unit.dbm.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode( of = "className" )
@ToString( of = "className" )
public final class EntityClass
{
	public EntityClass( @NotNull String className, @Nullable Map<String, String> properties )
	{
		this.className = className;
		this.properties = properties;
		fields = new ArrayList<>();
	}

	@NotNull
	private final String className;
	private final List<EntityField> fields;
	@Nullable
	private final Map<String, String> properties;
	private Entity entity;
	private List<EntityField> equals;
	private List<EntityField> compareTo;
	private List<EntityField> toString;
	private List<EntityConstructor> constructors;

	public void addField( @NotNull EntityField field )
	{
		if( findField( field.getName() ) != null )
			throw new IllegalArgumentException( "Duplicate field: " + field );
		fields.add( field );
	}

	@Nullable
	public EntityField findField( @NotNull String name )
	{
		return fields
				.stream()
				.filter( e -> e.getName().equals( name ) )
				.findFirst()
				.orElse( null );
	}
}
