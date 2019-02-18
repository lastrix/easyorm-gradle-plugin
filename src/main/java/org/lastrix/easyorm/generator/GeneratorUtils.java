package org.lastrix.easyorm.generator;

import org.hibernate.boot.jaxb.cfg.spi.JaxbCfgConfigPropertyType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GeneratorUtils
{
	public static final String INSTALL_SCRIPT = "install.sql";
	private static final Pattern NEW_LINE = Pattern.compile( "\r?\n" );
	public static final String UNINSTALL_SCRIPT = "uninstall.sql";
	public static final String COMMA_WITH_NEWLINE = ',' + System.lineSeparator();

	private GeneratorUtils()
	{
	}

	public static JaxbCfgConfigPropertyType createProperty( String key, String value )
	{
		JaxbCfgConfigPropertyType propertyType = new JaxbCfgConfigPropertyType();
		propertyType.setName( key );
		propertyType.setValue( value );
		return propertyType;
	}

	public static String fixNewLines( CharSequence source )
	{
		return NEW_LINE.matcher( source ).replaceAll( Matcher.quoteReplacement( System.lineSeparator() ) );
	}

}
