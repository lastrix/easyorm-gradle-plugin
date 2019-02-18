package org.lastrix.easyorm.queryLanguage.object;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode
@ToString
public final class StringRef implements Expression
{
	public StringRef( String value )
	{
		this.value = value.substring( 1, value.length() - 1 );
	}

	private final String value;
}
