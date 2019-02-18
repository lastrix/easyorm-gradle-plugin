package org.lastrix.easyorm.unit.java;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@EqualsAndHashCode
@ToString
public final class EntityConstructor
{
	public EntityConstructor( EntityClass entityClass, List<EntityField> fields )
	{
		this.entityClass = entityClass;
		//noinspection AssignmentOrReturnOfFieldWithMutableType
		this.fields = fields;
	}

	private final EntityClass entityClass;
	private final List<EntityField> fields;
}
