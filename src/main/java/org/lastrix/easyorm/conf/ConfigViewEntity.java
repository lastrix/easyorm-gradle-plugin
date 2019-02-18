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
public final class ConfigViewEntity extends AbstractPropertyObject
{
	@JsonProperty( required = true )
	private String name;
	@JsonProperty( required = true )
	private boolean entity;
	@JsonProperty( required = true )
	private List<String> expr;
	@Nullable
	private List<ConfigField> fields;
}
