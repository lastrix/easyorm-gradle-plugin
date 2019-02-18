package org.lastrix.easyorm.queryLanguage.object;

import org.jetbrains.annotations.NotNull;

public enum BinaryKind
{
	EQUAL( "==" ),
	NOT_EQUAL( "!=" ),
	LESS_THAN( "<" ),
	GREATER_THAN( ">" ),
	LESS_EQUAL( "<=" ),
	GREATER_EQUAL( ">=" );
	private final String operator;

	BinaryKind( String operator )
	{
		this.operator = operator;
	}

	public String getOperator()
	{
		return operator;
	}

	@NotNull
	public static BinaryKind findByOperator( @NotNull String operator )
	{
		for( BinaryKind kind : values() )
		{
			if( kind.getOperator().equals( operator ) )
				return kind;
		}
		throw new IllegalArgumentException( "No Kind for operator: " + operator );
	}
}
