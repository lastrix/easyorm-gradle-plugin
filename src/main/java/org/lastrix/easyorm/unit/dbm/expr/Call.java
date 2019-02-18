package org.lastrix.easyorm.unit.dbm.expr;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode( of = "functionName" )
@ToString
public final class Call implements Expression
{
	@NotNull
	private final String functionName;
	@NotNull
	private final List<Expression> parameters;

	@Nullable
	@Override
	public String getAggregateType()
	{
		String v = functionName.toLowerCase();
		if( "count".equalsIgnoreCase( v ) )
			return Long.class.getTypeName();

		if( isAggregateFunction( v ) )
			return Number.class.getTypeName();

		if( "concat".equals( v ) )
			return String.class.getTypeName();

		return null;
	}

	public static boolean isAggregateFunction( String v )
	{
		return Arrays.stream( AGGREGATES ).anyMatch( v:: equals );
	}

	private static final String[] AGGREGATES = {"avg", "min", "max", "sum", "count"};
}
