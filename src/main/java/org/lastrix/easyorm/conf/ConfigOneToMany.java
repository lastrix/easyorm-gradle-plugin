package org.lastrix.easyorm.conf;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode( of = "ref" )
@ToString( of = "ref" )
public final class ConfigOneToMany
{
	@JsonProperty( required = true )
	private String ref;
	private String field;
}
