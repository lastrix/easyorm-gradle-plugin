package org.lastrix.easyorm.unit.java;

import java.util.List;
import java.util.Set;

public enum MappingType
{
	SINGLE,
	SET,
	LIST;

	public static MappingType resolveMappingType( String typeName )
	{
		if( Set.class.getTypeName().equals( typeName ) )
			return SET;
		if( List.class.getTypeName().equals( typeName ) )
			return LIST;

		throw new IllegalArgumentException( "Unable to resolve: " + typeName );
	}
}
