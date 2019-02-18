package org.lastrix.easyorm.unit.dbm;

import org.lastrix.easyorm.unit.java.EntityField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode( of = {"name", "entity"} )
@ToString( of = "name" )
public final class Column
{
	public Column( String name, Entity entity, EntityField field )
	{
		this.name = name;
		this.entity = entity;
		this.field = field;
	}

	private final String name;
	private final Entity entity;
	private final EntityField field;
}
