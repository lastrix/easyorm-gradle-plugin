package org.lastrix.easyorm.conf;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public final class ConfigConstructor
{
	private List<String> fields;
}
