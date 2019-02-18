package org.lastrix.easyorm.unit.dbm.expr.object;

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
@EqualsAndHashCode( of = {"sourceAlias", "field"} )
@ToString( of = {"sourceAlias", "field"} )
public final class FieldObject implements RefObject
{
	@Nullable
	private final String sourceAlias;
	@NotNull
	private final EntityField field;

	@Nullable
	@Override
	public Entity asEntity()
	{
		if( field.getMappedField() == null )
			return null;

		return field.getMappedField().getEntityClass().getEntity();
	}

	@NotNull
	@Override
	public EntityField asField()
	{
		return field;
	}
}
