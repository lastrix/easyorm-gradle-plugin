package org.lastrix.easyorm.queryLanguage.object;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode
@ToString
public final class NumberRef implements Expression
{
	public NumberRef( String value )
	{
		this.value = Long.parseLong( value );
	}

	private final long value;
}
