package org.lastrix.easyorm.queryLanguage.object;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode( of = {"alias"} )
@ToString( of = {"alias"} )
public final class JoinEntity implements Join
{
	private final boolean left;
	private final String entity;
	private final String alias;
	@Nullable
	private Expression expression;
}
