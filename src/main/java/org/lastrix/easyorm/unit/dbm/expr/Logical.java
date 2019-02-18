package org.lastrix.easyorm.unit.dbm.expr;

import org.lastrix.easyorm.queryLanguage.object.LogicalKind;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode( of = "kind" )
@ToString( of = "kind" )
public final class Logical implements Expression
{
	@NotNull
	private final LogicalKind kind;
	@NotNull
	private final List<Expression> items;
}
