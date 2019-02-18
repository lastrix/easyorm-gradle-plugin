package org.lastrix.easyorm.unit.java;

import org.lastrix.easyorm.unit.dbm.Column;
import org.lastrix.easyorm.unit.dbm.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode( of = {"entityClass", "name"} )
@ToString( of = "name" )
public final class EntityField
{
	public EntityField( @NotNull EntityClass entityClass, @NotNull String name, @Nullable Map<String, String> properties )
	{
		this.entityClass = entityClass;
		this.name = name;
		this.properties = properties;
	}

	@NotNull
	private final EntityClass entityClass;
	@NotNull
	private final String name;
	@Nullable
	private final Map<String, String> properties;
	private String typeName;
	private List<String> typeParameters;
	private Column column;
	private boolean nullable;
	@Nullable
	private String defaultValue;
	private int length;
	@Nullable
	private EntityField mappedField;
	private Table mediator;
	@Nullable
	private Mapping mapping;
	private boolean inverse;
	@Nullable
	private MappingType mappingType;

	public String getFullName()
	{
		return getEntityClass().getClassName() + '#' + getName();
	}
}
