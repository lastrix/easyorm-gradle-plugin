package org.lastrix.easyorm.conf;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode( of = "name", callSuper = true )
@ToString( of = "name" )
public final class ConfigField extends AbstractPropertyObject
{
	@JsonProperty( required = true )
	private String name;
	private String type;
	private int length;
	private boolean nullable;
	private String defaultValue;
	@JsonProperty( "many-to-one" )
	private ConfigManyToAny manyToOne;
	@JsonProperty( "many-to-many" )
	private ConfigManyToAny manyToMany;
	@JsonProperty( "one-to-many" )
	private ConfigOneToMany oneToMany;
}
