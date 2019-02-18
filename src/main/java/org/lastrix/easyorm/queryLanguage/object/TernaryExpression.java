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
public final class TernaryExpression implements Expression
{
	@NotNull
	private final Expression condition;
	@NotNull
	private final Expression left;
	@NotNull
	private final Expression right;
}
