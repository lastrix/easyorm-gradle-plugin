package org.lastrix.easyorm.unit.dbm.expr;

import org.lastrix.easyorm.queryLanguage.object.BinaryKind;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode( of = "kind" )
@ToString( of = "kind" )
public final class Binary implements Expression
{
	@NotNull
	private final BinaryKind kind;
	@NotNull
	private final Expression left;
	@NotNull
	private final Expression right;
}
