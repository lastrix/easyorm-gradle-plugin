package org.lastrix.easyorm.unit.dbm.expr.object;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode
@ToString
public final class BooleanObject implements RefObject
{
	public static final BooleanObject TRUE = new BooleanObject( true );
	public static final BooleanObject FALSE = new BooleanObject( false );

	private BooleanObject( boolean value )
	{
		this.value = value;
	}

	private final boolean value;
}
