package org.lastrix.easyorm.generator.hibernate;

import org.lastrix.easyorm.unit.dbm.CascadeAction;
import org.lastrix.easyorm.unit.dbm.Column;
import org.lastrix.easyorm.unit.dbm.Entity;
import org.lastrix.easyorm.unit.dbm.ForeignKeyConstraint;
import org.lastrix.easyorm.unit.java.EntityField;
import org.hibernate.boot.jaxb.cfg.spi.JaxbCfgConfigPropertyType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmSimpleIdType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Dialect
{
	@NotNull
	default String schema( @NotNull String source )
	{
		return source;
	}

	void populateCfgProperties( List<JaxbCfgConfigPropertyType> list );

	String getName();

	@NotNull
	String cascadeAction( @NotNull CascadeAction action );

	@NotNull
	default String column( @NotNull Column column )
	{
		return column( column.getName() );
	}

	@NotNull
	String column( @NotNull String name );

	@NotNull
	String getForeignKeyConstraintName( @NotNull ForeignKeyConstraint constraint );

	@NotNull
	String sqlType( @NotNull EntityField field );

	@NotNull
	default String entity( @NotNull Entity table )
	{
		return entity( table.getName() );
	}

	@NotNull
	String entity( @NotNull String name );

	@NotNull
	JaxbHbmSimpleIdType id( @NotNull EntityField field );

	@NotNull
	default String aggregateFunction( @NotNull String name )
	{
		return name;
	}
}
