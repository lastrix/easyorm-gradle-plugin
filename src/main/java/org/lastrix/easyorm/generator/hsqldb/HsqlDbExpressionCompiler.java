package org.lastrix.easyorm.generator.hsqldb;

import org.lastrix.easyorm.generator.AbstractSqlExpressionCompiler;
import org.lastrix.easyorm.unit.dbm.expr.EntityJoin;
import org.lastrix.easyorm.unit.dbm.expr.FieldJoin;
import org.lastrix.easyorm.unit.java.EntityField;
import org.lastrix.easyorm.unit.java.Mapping;

public final class HsqlDbExpressionCompiler extends AbstractSqlExpressionCompiler
{
	public HsqlDbExpressionCompiler()
	{
		super( new HsqlDbDialect() );
	}

	@Override
	protected void compileEntityJoin( EntityJoin join )
	{
		append( entity( join.getEntity() ) )
				.append( ' ' )
				.append( join.getAlias() );

		if( join.getExpression() != null )
		{
			append( " ON " );
			compileRecursive( join.getExpression() );
		}
	}

	@Override
	protected void compileFieldJoin( FieldJoin join )
	{
		if( join.getSourceColumn().getField().getMapping() == Mapping.ONE_TO_MANY )
		{
			EntityField mappedField = join.getSourceColumn().getField().getMappedField();
			if( mappedField == null )
				throw new IllegalStateException( "Mapped field required" );

			append( entity( join.getTarget() ) ).append( ' ' ).append( join.getAlias() )
					.append( " ON " )
					.append( join.getAlias() ).append( '.' ).append( column( mappedField.getColumn() ) )
					.append( " = " ).append( join.getSourceAlias() ).append( ".id" );
		}
		else
			append( entity( join.getTarget() ) ).append( ' ' ).append( join.getAlias() )
					.append( " ON " )
					.append( join.getAlias() ).append( ".id = " )
					.append( join.getSourceAlias() ).append( '.' ).append( column( join.getSourceColumn() ) );
	}
}
