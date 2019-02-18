package org.lastrix.easyorm.unit.dbm;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
@EqualsAndHashCode
@ToString
public final class Index
{
	public static Index create( IndexType type, @NotNull Table table, List<Column> columns )
	{
		return new Index( type, table, columns );
	}

	private Index( @NotNull IndexType type, @NotNull Table table, @NotNull List<Column> columns )
	{
		this.type = type;
		this.table = table;
		this.columns = columns;
	}

	private final IndexType type;
	private final Table table;
	private final List<Column> columns;
}
