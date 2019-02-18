package org.lastrix.easyorm.queryLanguage.object;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode( of = "name" )
@ToString( of = "name" )
public final class ByAliasRef implements Expression
{
	@NotNull
	private final String name;
	private final boolean create;
	private Object target;
}
