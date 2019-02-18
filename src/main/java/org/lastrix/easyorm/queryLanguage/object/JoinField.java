package org.lastrix.easyorm.queryLanguage.object;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode( of = "alias" )
@ToString( of = "alias" )
public final class JoinField implements Join
{
	private final boolean left;
	@NotNull
	private final FieldRef field;
	@NotNull
	private final String alias;
}
