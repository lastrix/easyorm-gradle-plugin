package org.lastrix.easyorm.queryLanguage.object;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode( of = {"fieldName", "join"} )
@ToString( of = {"fieldName", "join"} )
public final class FieldRef implements Expression
{
	@NotNull
	private final String fieldName;
	@Nullable
	private final Join join;
}
