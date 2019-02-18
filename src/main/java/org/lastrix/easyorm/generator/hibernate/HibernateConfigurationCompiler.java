package org.lastrix.easyorm.generator.hibernate;

import org.lastrix.easyorm.unit.Unit;
import org.apache.commons.io.FileUtils;
import org.hibernate.boot.jaxb.cfg.spi.JaxbCfgConfigPropertyType;
import org.hibernate.boot.jaxb.cfg.spi.JaxbCfgHibernateConfiguration;
import org.hibernate.boot.jaxb.cfg.spi.JaxbCfgHibernateConfiguration.JaxbCfgSessionFactory;
import org.hibernate.boot.jaxb.cfg.spi.JaxbCfgMappingReferenceType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmHibernateMapping;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class HibernateConfigurationCompiler
{

	private static final String DOCTYPE_MAPPING = System.lineSeparator() +
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<!DOCTYPE hibernate-mapping PUBLIC \"-//Hibernate/Hibernate Mapping DTD 3.0//EN\" " +
			"\"http://www.hibernate.org/dtd/hibernate-mapping-3.0.dtd\">";

	private static final String DOCTYPE_CFG = System.lineSeparator() +
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<!DOCTYPE hibernate-configuration PUBLIC \"-//Hibernate/Hibernate Configuration DTD 3.0//EN\" " +
			"\"http://www.hibernate.org/dtd/hibernate-configuration-3.0.dtd\">";

	public HibernateConfigurationCompiler( Unit unit, Dialect dialect, File buildDir )
	{
		this.unit = unit;
		this.dialect = dialect;
		outDir = new File( buildDir, "res" + File.separatorChar + "hibernate" + File.separatorChar + dialect.getName() );
		hbmDir = new File( outDir, "hbm" );
		if( !hbmDir.exists() && !hbmDir.mkdirs() || !hbmDir.isDirectory() )
			throw new IllegalStateException( "Unable to mkdirs: " + hbmDir );
	}

	private final Unit unit;
	private final Dialect dialect;
	private final File outDir;
	private final File hbmDir;

	public void compile()
	{
		List<JaxbHbmHibernateMapping> mappings =
				unit.getClasses()
						.stream()
						.map( e -> new EntityMappingBuilder( e, dialect ).build() )
						.collect( Collectors.toList() );

		Collection<String> resources = storeMappings( mappings );

		JaxbCfgHibernateConfiguration cfg = new JaxbCfgHibernateConfiguration();
		JaxbCfgSessionFactory factory = new JaxbCfgSessionFactory();
		List<JaxbCfgConfigPropertyType> property = factory.getProperty();
		dialect.populateCfgProperties( property );

		List<JaxbCfgMappingReferenceType> mapping = factory.getMapping();
		for( String resource : resources )
			mapping.add( createMapping( resource ) );
		cfg.setSessionFactory( factory );

		storeHibernateCfg( cfg );
	}

	private static JaxbCfgMappingReferenceType createMapping( String resource )
	{
		JaxbCfgMappingReferenceType type = new JaxbCfgMappingReferenceType();
		type.setResource( resource );
		return type;
	}

	private void storeHibernateCfg( JaxbCfgHibernateConfiguration cfg )
	{
		Marshaller marshaller = createMarshallerFor( JaxbCfgHibernateConfiguration.class );
		setMarshallerProperty( marshaller, "com.sun.xml.internal.bind.xmlHeaders", DOCTYPE_CFG );
		String name = outDir.getName() + ".cfg.xml";
		File outputFile = new File( outDir, name );
		writeToFile( marshaller, cfg, outputFile );
	}

	private Collection<String> storeMappings( Iterable<JaxbHbmHibernateMapping> mappings )
	{
		Marshaller marshaller = createMarshallerFor( JaxbHbmHibernateMapping.class );
		setMarshallerProperty( marshaller, "com.sun.xml.internal.bind.xmlHeaders", DOCTYPE_MAPPING );
		Collection<String> list = new ArrayList<>();
		for( JaxbHbmHibernateMapping mapping : mappings )
			writeMapping( marshaller, mapping, list );
		return list;
	}

	private static void setMarshallerProperty( Marshaller marshaller, String name, String value )
	{
		try
		{
			marshaller.setProperty( name, value );
		} catch( PropertyException e )
		{
			throw new IllegalStateException( "Unable to set property: " + name, e );
		}
	}

	private void writeMapping( Marshaller marshaller, JaxbHbmHibernateMapping mapping, Collection<String> list )
	{
		String className = mapping.getClazz().get( 0 ).getName();
		className = className.substring( className.lastIndexOf( '.' ) + 1 );
		String name = className + ".hbm.xml";
		list.add( "res/hibernate/" + outDir.getName() + "/hbm/" + name );
		File outputFile = new File( hbmDir, name );
		writeToFile( marshaller, mapping, outputFile );
	}

	private static void writeToFile( Marshaller marshaller, Object object, File outputFile )
	{
		byte[] data = null;
		try( ByteArrayOutputStream os = new ByteArrayOutputStream() )
		{
			marshaller.marshal( object, os );
			data = os.toByteArray();
		} catch( Exception e )
		{
			throw new IllegalStateException( "Unable to write mapping to file: " + outputFile, e );
		}
		try
		{
			String content = new String( data, "UTF-8" );
			while( Character.isWhitespace( content.charAt( 0 ) ) )
				content = content.substring( 1 );
			FileUtils.writeStringToFile( outputFile, content.replaceAll( " xmlns=\".*\"", "" ) );
		} catch( IOException e )
		{
			throw new IllegalStateException( "Unable to patch file: " + outputFile, e );
		}
	}

	private static Marshaller createMarshallerFor( Class<?>... classes )
	{
		JAXBContext context;
		try
		{
			context = JAXBContext.newInstance( classes );
		} catch( JAXBException e )
		{
			throw new IllegalStateException( "Unable to create context instance for jaxb", e );
		}

		try
		{
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, true );
			marshaller.setProperty( Marshaller.JAXB_FRAGMENT, true );
			return marshaller;
		} catch( JAXBException e )
		{
			throw new IllegalStateException( "Unable to create JAXB marshaller", e );
		}
	}
}
