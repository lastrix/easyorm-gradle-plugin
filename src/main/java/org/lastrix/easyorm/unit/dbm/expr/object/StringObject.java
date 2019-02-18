package org.lastrix.easyorm.unit.dbm.expr.object;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public final class StringObject implements RefObject
{
	private final String value;
}
