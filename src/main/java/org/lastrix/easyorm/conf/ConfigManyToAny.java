package org.lastrix.easyorm.conf;

import org.lastrix.easyorm.unit.dbm.CascadeAction;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode( of = "ref" )
@ToString( of = "ref" )
public final class ConfigManyToAny
{
	@JsonProperty( required = true )
	private String ref;
	private boolean inverse;
	private CascadeAction update = CascadeAction.NO_ACTION;
	private CascadeAction delete = CascadeAction.NO_ACTION;
}
