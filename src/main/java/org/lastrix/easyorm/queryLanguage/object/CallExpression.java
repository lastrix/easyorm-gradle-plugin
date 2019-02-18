package org.lastrix.easyorm.queryLanguage.object;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode( of = "functionName" )
@ToString
public final class CallExpression implements Expression
{
	private final String functionName;
	private final List<Expression> parameters = new ArrayList<>();

	public void addParameter( Expression parameter )
	{
		if( parameter == null )
			throw new IllegalArgumentException();
		parameters.add( parameter );
	}
}
