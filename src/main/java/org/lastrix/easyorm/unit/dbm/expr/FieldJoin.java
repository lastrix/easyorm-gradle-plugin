package org.lastrix.easyorm.unit.dbm.expr;

import org.lastrix.easyorm.unit.dbm.Column;
import org.lastrix.easyorm.unit.dbm.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode( of = "alias" )
@ToString( of = "alias" )
public class FieldJoin implements Expression
{
	private final boolean left;
	private final String alias;
	private final String sourceAlias;
	private final Entity sourceEntity;
	private final Column sourceColumn;
	private final Entity target;

	@NotNull
	@Override
	public Entity asEntity()
	{
		return target;
	}
}
