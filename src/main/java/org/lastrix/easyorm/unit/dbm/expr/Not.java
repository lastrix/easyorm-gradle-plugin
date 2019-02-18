package org.lastrix.easyorm.unit.dbm.expr;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public final class Not implements Expression
{
	private final Expression expression;
}
