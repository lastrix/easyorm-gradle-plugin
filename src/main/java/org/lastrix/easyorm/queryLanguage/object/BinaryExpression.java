package org.lastrix.easyorm.queryLanguage.object;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public final class BinaryExpression implements Expression
{
	public static BinaryExpression create( @NotNull String op, @NotNull Expression left, @NotNull Expression right )
	{
		return new BinaryExpression( BinaryKind.findByOperator( op ), left, right );
	}

	@NotNull
	private final BinaryKind kind;
	@NotNull
	private final Expression left;
	@NotNull
	private final Expression right;
}
