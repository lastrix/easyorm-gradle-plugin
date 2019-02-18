package org.lastrix.easyorm.unit.dbm;

import org.lastrix.easyorm.conf.ConfigManyToAny;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public final class ForeignKeyConstraint implements Constraint
{
	private Column source;
	private Column target;
	private CascadeAction update;
	private CascadeAction delete;

	public static ForeignKeyConstraint create( Column source, Column target, ConfigManyToAny mapping )
	{
		ForeignKeyConstraint constraint = new ForeignKeyConstraint();
		constraint.setDelete( mapping.getDelete() );
		constraint.setUpdate( mapping.getUpdate() );
		constraint.setSource( source );
		constraint.setTarget( target );
		return constraint;
	}
}
