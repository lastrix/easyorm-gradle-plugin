package org.lastrix.easyorm.unit.dbm.expr.object;

import org.lastrix.easyorm.unit.dbm.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode( of = "name" )
@ToString( of = "name" )
public final class EntityObject implements RefObject
{
	@NotNull
	private final String name;
	private final Entity entity;

	@NotNull
	@Override
	public Entity asEntity()
	{
		return entity;
	}
}
