package org.lastrix.easyorm.unit.dbm.expr;

import org.lastrix.easyorm.unit.dbm.Entity;
import org.lastrix.easyorm.unit.java.EntityField;
import org.jetbrains.annotations.Nullable;

public interface Expression
{
	@Nullable
	default Entity asEntity()
	{
		return null;
	}

	@Nullable
	default EntityField asField()
	{
		return null;
	}

	@Nullable
	default String getAggregateType()
	{
		return null;
	}
}
