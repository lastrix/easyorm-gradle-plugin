package org.lastrix.easyorm.generator;

import org.lastrix.easyorm.generator.hibernate.Dialect;
import org.lastrix.easyorm.unit.Unit;
import org.lastrix.easyorm.unit.dbm.*;
import org.lastrix.easyorm.unit.java.EntityField;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractScriptGenerator
{
	protected AbstractScriptGenerator( Unit unit, File buildDir, Dialect dialect )
	{
		this.unit = unit;
		this.dialect = dialect;
		this.buildDir = buildDir;
	}

	private final Dialect dialect;
	private final Unit unit;
	private final File buildDir;
	private final StringBuilder stringBuilder = new StringBuilder();

	public final void generate()
	{
		File outFolder = new File( buildDir, "res" + File.separatorChar + "hibernate" + File.separatorChar + dialect.getName() );
		if( !outFolder.exists() && !outFolder.mkdirs() || !outFolder.isDirectory() )
			throw new IllegalStateException( "Unable mkdirs: " + outFolder );

		File targetFile = new File( outFolder, getScriptName() );
		if( targetFile.exists() && !targetFile.delete() || targetFile.isDirectory() )
			throw new IllegalStateException( "Unable to remove file: " + targetFile );

		compileScript();
		writeToFile( targetFile, stringBuilder );
	}

	protected final Dialect getDialect()
	{
		return dialect;
	}

	protected final Unit getUnit()
	{
		return unit;
	}

	public StringBuilder getStringBuilder()
	{
		return stringBuilder;
	}

	public List<View> getViews()
	{
		return unit.getViews();
	}

	protected final List<Table> getTables()
	{
		return unit.getTables();
	}

	protected final String getOwner()
	{
		return unit.getOwner();
	}

	protected final String getSchema()
	{
		return unit.getName();
	}

	protected final StringBuilder append( String value )
	{
		return stringBuilder.append( value );
	}

	@NotNull
	protected final String column( @NotNull Column column )
	{
		return dialect.column( column );
	}

	@NotNull
	protected final String entity( @NotNull Entity entity )
	{
		return dialect.entity( entity );
	}

	@NotNull
	protected final String cascadeAction( @NotNull CascadeAction action )
	{
		return dialect.cascadeAction( action );
	}

	@NotNull
	protected final String sqlType( @NotNull EntityField field )
	{
		return dialect.sqlType( field );
	}

	@NotNull
	protected final String getEntityName( Entity entity )
	{
		return entity.getSchema() + '.' + dialect.entity( entity );
	}

	protected final String joinColumns( Collection<Column> columns, CharSequence delimiter )
	{
		return columns.stream().map( dialect:: column ).collect( Collectors.joining( delimiter ) );
	}

	protected void appendDefaultValue( EntityField field )
	{
		assert field.getDefaultValue() != null;
		switch( field.getTypeName() )
		{
			case "java.lang.Boolean":
				append( String.valueOf( Boolean.parseBoolean( field.getDefaultValue() ) ) );
				break;

			case "java.lang.Short":
			case "java.lang.Integer":
			case "java.lang.Long":
				append( String.valueOf( Long.parseLong( field.getDefaultValue() ) ) );
				break;

			case "java.time.Instant":
				if( !"current".equals( field.getDefaultValue() ) )
					throw new IllegalArgumentException( "Not allowed default value for instant: " + field.getDefaultValue() );
				append( "CURRENT_TIMESTAMP" );
				break;

			default:
				throw new IllegalStateException( "Unable to use default value '" + field.getDefaultValue() + "' for type: " + field.getTypeName() );
		}
	}


	protected static String getMaxValueForTable( Table table )
	{
		Column id = table.findColumn( "id" );
		if( id == null )
			throw new IllegalStateException( "No id field for table: " + table );

		switch( id.getField().getTypeName() )
		{
			case "java.lang.Short":
				return String.valueOf( Short.MAX_VALUE );

			case "java.lang.Integer":
				return String.valueOf( Integer.MAX_VALUE );

			case "java.lang.Long":
				return String.valueOf( Long.MAX_VALUE );

			default:
				throw new UnsupportedOperationException( id.getField().getTypeName() );

		}
	}

	private static void writeToFile( File file, CharSequence source )
	{
		try
		{
			FileUtils.writeStringToFile( file, GeneratorUtils.fixNewLines( source ), "UTF-8" );
		} catch( IOException e )
		{
			throw new IllegalStateException( "Unable to write to file: " + file, e );
		}
	}

	@NotNull
	protected abstract String getScriptName();

	protected abstract void compileScript();
}
