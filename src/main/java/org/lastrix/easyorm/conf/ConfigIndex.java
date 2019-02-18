package org.lastrix.easyorm.conf;

import org.lastrix.easyorm.unit.dbm.IndexType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public final class ConfigIndex
{
	private IndexType type = IndexType.HINT;
	@JsonProperty( required = true )
	private List<String> fields;

}
