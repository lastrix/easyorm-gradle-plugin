package org.lastrix.easyorm.unit.dbm.expr;

import org.lastrix.easyorm.unit.dbm.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode( of = "alias" )
@ToString( of = "alias" )
public class EntityJoin implements Expression
{
	private final boolean left;
	@NotNull
	private final String alias;
	@NotNull
	private final Entity entity;
	@Nullable
	private final Expression expression;

	@Nullable
	@Override
	public Entity asEntity()
	{
		return entity;
	}
}
