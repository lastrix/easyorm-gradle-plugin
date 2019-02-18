package org.lastrix.easyorm.unit.dbm.expr;

import org.lastrix.easyorm.unit.dbm.Entity;
import org.lastrix.easyorm.unit.java.EntityField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public final class Paren implements Expression
{
	@NotNull
	private final Expression expression;

	@Nullable
	@Override
	public Entity asEntity()
	{
		return expression.asEntity();
	}

	@Nullable
	@Override
	public EntityField asField()
	{
		return expression.asField();
	}

	@Nullable
	@Override
	public String getAggregateType()
	{
		return expression.getAggregateType();
	}
}
