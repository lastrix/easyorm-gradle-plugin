package org.lastrix.easyorm.generator.java;

import org.lastrix.easyorm.generator.GeneratorUtils;
import org.lastrix.easyorm.unit.Unit;
import org.lastrix.easyorm.unit.java.EntityClass;
import org.lastrix.easyorm.unit.java.EntityConstructor;
import org.lastrix.easyorm.unit.java.EntityField;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JavaGenerator
{
	public JavaGenerator( Unit unit, File buildDir, File templateFile )
	{
		this.unit = unit;
		this.buildDir = buildDir;
		try
		{
			template = FileUtils.readFileToString( templateFile, "UTF-8" );
		} catch( IOException e )
		{
			throw new IllegalStateException( "Unable to read file: " + templateFile, e );
		}
	}

	private final Unit unit;
	private final File buildDir;
	private final String template;


	public void generate()
	{
		unit.getClasses().forEach( this :: generateClass );

		String manifestClass = "package " + unit.getBasePackage() + ";\n" +
				'\n' +
				"public final class DatabaseInfo\n" +
				"{\n" +
				"\tprivate DatabaseInfo()\n" +
				"\t{\n" +
				"\t}\n" +
				'\n' +
				"\tpublic static final int VERSION = " + unit.getVersion() + ";\n" +
				'}';

		try
		{
			File targetDir = new File( buildDir, unit.getBasePackage().replace( '.', File.separatorChar ) );
			FileUtils.writeStringToFile( new File( targetDir, "DatabaseInfo.java" ), manifestClass );
		} catch( IOException e )
		{
			throw new IllegalStateException( "Unable to write manifest class", e );
		}
	}

	private void generateClass( EntityClass entityClass )
	{
		EntityClassCompiler compiler = new EntityClassCompiler( entityClass );
		String sourceCode = compiler.compile( template );

		File folder = new File( buildDir, compiler.getPackageName().replace( '.', File.separatorChar ) );
		if( !folder.exists() && !folder.mkdirs() || !folder.isDirectory() )
			throw new IllegalStateException( "Unable to create folder: " + folder );

		File output = new File( folder, compiler.getName() + ".java" );
		try
		{
			FileUtils.writeStringToFile( output, sourceCode, "UTF-8" );
		} catch( IOException e )
		{
			throw new IllegalStateException( "Unable to write to file: " + output, e );
		}
	}

	private static final class EntityClassCompiler
	{
		private EntityClassCompiler( EntityClass clazz )
		{
			this.clazz = clazz;
			String className = clazz.getClassName();
			int idx = className.lastIndexOf( '.' );
			if( idx == -1 )
				throw new IllegalStateException();
			packageName = className.substring( 0, idx );
			name = className.substring( idx + 1 );
		}

		private final EntityClass clazz;
		private final String packageName;
		private final String name;

		String getPackageName()
		{
			return packageName;
		}

		String getName()
		{
			return name;
		}

		String compile( String template )
		{
			String source = template;
			source = TAG_PACKAGE_NAME.matcher( source ).replaceAll( Matcher.quoteReplacement( packageName ) );
			source = TAG_CLASS_NAME.matcher( source ).replaceAll( Matcher.quoteReplacement( name ) );
			source = TAG_EQUALS.matcher( source ).replaceAll( Matcher.quoteReplacement( buildEquals() ) );
			source = TAG_TO_STRING.matcher( source ).replaceAll( Matcher.quoteReplacement( buildToString() ) );
			source = TAG_CONSTANTS.matcher( source ).replaceAll( Matcher.quoteReplacement( buildConstants() ) );
			source = TAG_CONSTRUCTORS.matcher( source ).replaceAll( Matcher.quoteReplacement( buildConstructors() ) );
			source = TAG_FIELDS.matcher( source ).replaceAll( Matcher.quoteReplacement( buildFields() ) );
			source = TAG_COMPARE_TO.matcher( source ).replaceAll( Matcher.quoteReplacement( buildCompareTo() ) );
			return GeneratorUtils.fixNewLines( source );
		}

		private String buildEquals()
		{
			if( clazz.getEquals() == null || clazz.getEquals().isEmpty() )
				return "";

			return "of = {" + fields2String( clazz.getEquals() ) + '}';
		}

		private String buildToString()
		{
			if( clazz.getToString() == null || clazz.getToString().isEmpty() )
				return "";

			return "of = {" + fields2String( clazz.getToString() ) + '}';
		}

		private String buildConstants()
		{
			List<EntityField> fields = clazz.getFields().stream().filter( e -> e.getLength() > 0 ).collect( Collectors.toList() );
			if( fields.isEmpty() )
				return "";

			Collection<String> constants = new ArrayList<>();
			for( EntityField field : fields )
				constants.add( "\tpublic static final int LENGTH_" + field.getName().toUpperCase() + " = " + field.getLength() + ';' );
			return StringUtils.join( constants, System.lineSeparator() );
		}

		private String buildConstructors()
		{
			if( clazz.getConstructors() == null || clazz.getConstructors().isEmpty() )
				return "";

			String delimiter = System.lineSeparator() + System.lineSeparator();
			return clazz.getConstructors().stream()
					.map( this :: mapConstructor ).collect( Collectors.joining( delimiter ) );
		}

		private String buildFields()
		{
			Collection<String> list = clazz
					.getFields()
					.stream()
					.map( this :: mapField )
					.collect( Collectors.toList() );

			return StringUtils.join( list, System.lineSeparator() );
		}

		private String buildCompareTo()
		{
			if( clazz.getCompareTo() == null || clazz.getCompareTo().isEmpty() )
				return "\t\treturn getId().compareTo(o.getId());";

			StringBuilder sb = new StringBuilder();
			sb.append( "\t\tint result;" ).append( System.lineSeparator() );

			for( EntityField field : clazz.getCompareTo() )
			{
				String getter = "get" + Character.toUpperCase( field.getName().charAt( 0 ) ) + field.getName().substring( 1 );

				sb.append( "\t\tresult = " )
						.append( getter ).append( "().compareTo(" )
						.append( "o." ).append( getter ).append( "());" ).append( System.lineSeparator() )
						.append( "\t\tif (result != 0) return result;" ).append( System.lineSeparator() );
			}
			sb.append( "\t\treturn result;" ).append( System.lineSeparator() );

			return sb.toString();
		}

		private String mapConstructor( EntityConstructor constructor )
		{
			StringBuilder sb = new StringBuilder();
			sb.append( "\tpublic " )
					.append( name )
					.append( '(' )
					.append( constructor.getFields().stream().map( this :: mapArgument ).collect( Collectors.joining( ", " ) ) )
					.append( ')' )
					.append( System.lineSeparator() )
					.append( "\t{" )
					.append( System.lineSeparator() );

			constructor
					.getFields()
					.forEach( e -> mapAssignment( e, sb ) );

			sb.append( "\t}" );
			return sb.toString();
		}

		private static void mapAssignment( EntityField field, StringBuilder sb )
		{
			sb
					.append( "\t\tthis." )
					.append( field.getName() )
					.append( " = " )
					.append( field.getName() )
					.append( ';' )
					.append( System.lineSeparator() );
		}

		private String mapArgument( EntityField field )
		{
			return ( field.isNullable() ? "@Nullable " : "" )
					+ createTypeString( field.getTypeName(), field.getTypeParameters() )
					+ ' '
					+ field.getName();
		}

		private String mapField( EntityField field )
		{
			StringBuilder sb = new StringBuilder();
			if( field.isNullable() )
				sb.append( "\t@Nullable" ).append( System.lineSeparator() );

			String annotations = null;
			if( field.getProperties() != null )
				annotations = field.getProperties().get( "annotations" );

			if( annotations != null )
				Arrays.stream( annotations.split( "\n" ) )
						.forEach( a -> {
							if( !StringUtils.isBlank( a ) )
								sb.append( '\t' ).append( a.trim() ).append( '\n' );
						} );

			sb.append( "\tprivate " )
					.append( createTypeString( field.getTypeName(), field.getTypeParameters() ) )
					.append( ' ' )
					.append( field.getName() )
					.append( ';' );

			return sb.toString();
		}

		private String createTypeString( String typeName, @Nullable Collection<String> typeParameters )
		{
			if( typeParameters == null )
				return toPrettyType( typeName );

			return toPrettyType( typeName ) + '<' + typeParameters.stream().map( this :: toPrettyType ).collect( Collectors.joining( "," ) ) + '>';
		}

		private String toPrettyType( String typeName )
		{
			if( StringUtils.isBlank( typeName ) )
				throw new IllegalArgumentException( "Type must not be null" );

			if( isNamespace( typeName, NS_JAVA_LANG ) )
				return typeName.substring( NS_JAVA_LANG.length() + 1 );

			if( isNamespace( typeName, NS_JAVA_TIME ) )
				return typeName.substring( NS_JAVA_TIME.length() + 1 );

			if( isNamespace( typeName, NS_JAVA_UTIL ) )
				return typeName.substring( NS_JAVA_UTIL.length() + 1 );

			if( isNamespace( typeName, packageName ) )
				return typeName.substring( packageName.length() + 1 );

			return typeName;
		}

		private static boolean isNamespace( String name, CharSequence namespace )
		{
			return name.indexOf( '.', namespace.length() + 1 ) == -1;
		}

		private static String fields2String( Collection<EntityField> fields )
		{
			return fields
					.stream()
					.map( e -> '"' + e.getName() + '"' )
					.collect( Collectors.joining( "," ) );
		}
	}

	private static final String NS_JAVA_LANG = "java.lang";
	private static final String NS_JAVA_TIME = "java.time";
	private static final String NS_JAVA_UTIL = "java.util";
	private static final Pattern TAG_PACKAGE_NAME = Pattern.compile( "%packageName%", Pattern.LITERAL );
	private static final Pattern TAG_EQUALS = Pattern.compile( "%equals%", Pattern.LITERAL );
	private static final Pattern TAG_TO_STRING = Pattern.compile( "%toString%", Pattern.LITERAL );
	private static final Pattern TAG_CLASS_NAME = Pattern.compile( "%className%", Pattern.LITERAL );
	private static final Pattern TAG_CONSTANTS = Pattern.compile( "%constants%", Pattern.LITERAL );
	private static final Pattern TAG_CONSTRUCTORS = Pattern.compile( "%constructors%", Pattern.LITERAL );
	private static final Pattern TAG_FIELDS = Pattern.compile( "%fields%", Pattern.LITERAL );
	private static final Pattern TAG_COMPARE_TO = Pattern.compile( "%compareToBody%", Pattern.LITERAL );
}
