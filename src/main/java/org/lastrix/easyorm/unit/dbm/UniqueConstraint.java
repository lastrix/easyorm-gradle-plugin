package org.lastrix.easyorm.unit.dbm;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
@EqualsAndHashCode
@ToString
public final class UniqueConstraint implements Constraint
{
	public static Constraint create( Table table, List<Column> columns )
	{
		return new UniqueConstraint( table, columns );
	}

	private UniqueConstraint( @NotNull Table table, @NotNull List<Column> columns )
	{
		this.table = table;
		this.columns = columns;
	}

	private final Table table;
	private final List<Column> columns;
}
