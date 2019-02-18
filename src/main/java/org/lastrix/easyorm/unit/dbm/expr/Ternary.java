package org.lastrix.easyorm.unit.dbm.expr;


import org.lastrix.easyorm.unit.dbm.Entity;
import org.lastrix.easyorm.unit.java.EntityField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public final class Ternary implements Expression
{
	@NotNull
	private final Expression condition;
	@NotNull
	private final Expression left;
	@NotNull
	private final Expression right;

	@Nullable
	@Override
	public Entity asEntity()
	{
		Entity le = left.asEntity();
		Entity re = right.asEntity();
		if( !Objects.equals( le, re ) )
			throw new IllegalStateException( "Not equal entites: " + le + " <-> " + re );
		return le;
	}

	@Nullable
	@Override
	public EntityField asField()
	{
		EntityField lf = left.asField();
		EntityField rf = right.asField();
		// TODO: check type
		return lf;
	}
}
