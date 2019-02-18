package org.lastrix.easyorm.queryLanguage.object;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public final class ViewField
{
	@NotNull
	private final String name;
	@NotNull
	private final Expression expression;
}
