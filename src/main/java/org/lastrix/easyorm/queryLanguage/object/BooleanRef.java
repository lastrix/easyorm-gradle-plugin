package org.lastrix.easyorm.queryLanguage.object;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode
@ToString
public final class BooleanRef implements Expression
{
	public BooleanRef( String value )
	{
		this.value = Boolean.parseBoolean( value );
	}

	private final boolean value;
}
