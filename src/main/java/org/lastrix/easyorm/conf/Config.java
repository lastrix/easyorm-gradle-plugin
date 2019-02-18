package org.lastrix.easyorm.conf;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode( of = "name" )
@ToString( of = "name" )
public final class Config
{
	@JsonProperty( required = true )
	private String name;
	@JsonProperty( required = true )
	private String owner;
	@JsonProperty( required = true )
	private Integer version;
	@JsonProperty( required = true )
	private String basePackage;
	private List<ConfigEntity> entities;
	@JsonProperty( "view-entities" )
	private List<ConfigViewEntity> viewEntities;

	public void save( File file )
	{
		try
		{
			MAPPER.writeValue( file, this );
		} catch( IOException e )
		{
			throw new IllegalStateException( "Unable to write to file: " + file, e );
		}
	}

	public static Config readFrom( File file )
	{
		try
		{
			return MAPPER.readValue( file, Config.class );
		} catch( IOException e )
		{
			throw new IllegalStateException( "Unable to read from file: " + file, e );
		}
	}

	private static final ObjectMapper MAPPER = new ObjectMapper();
}
