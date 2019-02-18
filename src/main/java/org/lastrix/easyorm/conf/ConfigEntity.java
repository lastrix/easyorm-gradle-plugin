package org.lastrix.easyorm.conf;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode( of = "name", callSuper = true )
@ToString( of = "name" )
public final class ConfigEntity extends AbstractPropertyObject
{
	@JsonProperty( required = true )
	private String name;
	@JsonProperty( required = true )
	private String id;
	@Nullable
	private String version;
	@JsonProperty( required = true )
	private List<ConfigField> fields;
	private List<ConfigIndex> index;
	private List<String> equals;
	private List<String> toString;
	private List<String> compareTo;
	private List<ConfigConstructor> constructors;
}
