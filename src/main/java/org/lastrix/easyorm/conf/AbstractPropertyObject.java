package org.lastrix.easyorm.conf;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

@Data
@NoArgsConstructor
@EqualsAndHashCode
@ToString
abstract class AbstractPropertyObject
{
	@JsonProperty( "property" )
	private Map<String, String> properties;
}
