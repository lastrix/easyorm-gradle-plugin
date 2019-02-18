package org.lastrix.easyorm.queryLanguage.object;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public final class LogicalListExpression implements Expression
{
	private final LogicalKind kind;
	private final List<Expression> items = new ArrayList<>();

	public void add( Expression expression )
	{
		items.add( expression );
	}

}
