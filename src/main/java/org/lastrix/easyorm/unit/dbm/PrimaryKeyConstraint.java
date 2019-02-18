package org.lastrix.easyorm.unit.dbm;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public final class PrimaryKeyConstraint implements Constraint
{
	private Table table;
	private List<Column> column;

	public static Constraint build( Table table, Column... columns )
	{
		if( ArrayUtils.isEmpty( columns ) )
			throw new IllegalArgumentException( "No PK columns" );

		PrimaryKeyConstraint constraint = new PrimaryKeyConstraint();
		constraint.setTable( table );
		constraint.setColumn( Arrays.asList( columns ) );
		return constraint;
	}
}
