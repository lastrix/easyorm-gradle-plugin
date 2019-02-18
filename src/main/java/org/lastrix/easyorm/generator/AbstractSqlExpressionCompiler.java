package org.lastrix.easyorm.generator;

import org.lastrix.easyorm.generator.hibernate.Dialect;
import org.lastrix.easyorm.unit.dbm.expr.*;
import org.lastrix.easyorm.unit.dbm.expr.object.*;
import org.lastrix.easyorm.unit.java.EntityField;
import org.lastrix.easyorm.unit.java.Mapping;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractSqlExpressionCompiler extends AbstractExpressionCompiler
{
	protected AbstractSqlExpressionCompiler( Dialect dialect )
	{
		super( dialect );
	}

	@Override
	protected void compileBinary( @NotNull Binary binary )
	{
		compileRecursive( binary.getLeft() );
		switch( binary.getKind() )
		{
			case EQUAL:
				append( " = " );
				break;

			case NOT_EQUAL:
				append( " <> " );
				break;

			case LESS_THAN:
			case GREATER_THAN:
			case LESS_EQUAL:
			case GREATER_EQUAL:
				append( " " ).append( binary.getKind().getOperator() ).append( ' ' );
				break;

			default:
				throw new UnsupportedOperationException( binary.getKind().getOperator() );
		}
		compileRecursive( binary.getRight() );
	}

	@Override
	protected void compileCall( @NotNull Call call )
	{
		if( Call.isAggregateFunction( call.getFunctionName() ) )
		{
			checkSingleParameter( call );
			append( aggregateFunction( call.getFunctionName().toUpperCase() ) ).append( "( " );
			compileRecursive( call.getParameters().get( 0 ) );
			append( " ) " );
		}
		else if( "concat".equalsIgnoreCase( call.getFunctionName() ) )
		{
			append( "concat( " );
			appendExpressions( ", ", call.getParameters() );
			append( " ) " );
		}
		else if( "isNull".equalsIgnoreCase( call.getFunctionName() ) )
		{
			checkSingleParameter( call );
			compileRecursive( call.getParameters().get( 0 ) );
			append( " IS NULL " );
		}
		else if( "isNotNull".equalsIgnoreCase( call.getFunctionName() ) )
		{
			checkSingleParameter( call );
			compileRecursive( call.getParameters().get( 0 ) );
			append( " IS NOT NULL " );
		}
		else
			throw new UnsupportedOperationException( call.getFunctionName() );
	}

	@Override
	protected void compileLogical( @NotNull Logical logical )
	{
		String delimiter;
		switch( logical.getKind() )
		{
			case AND:
				delimiter = " AND ";
				break;

			case OR:
				delimiter = " OR ";
				break;

			default:
				throw new UnsupportedOperationException( logical.getKind().name() );
		}

		appendExpressions( delimiter, logical.getItems() );
	}

	@Override
	protected void compileNot( @NotNull Not not )
	{
		append( " NOT ( " );
		compileRecursive( not.getExpression() );
		append( " ) " );
	}

	@Override
	protected void compileParen( @NotNull Paren paren )
	{
		append( " ( " );
		compileRecursive( paren.getExpression() );
		append( " ) " );
	}

	@Override
	protected void compileTernary( @NotNull Ternary ternary )
	{
		append( " ( CASE WHEN " );
		compileRecursive( ternary.getCondition() );
		append( " THEN " );
		compileRecursive( ternary.getLeft() );
		append( " ELSE " );
		compileRecursive( ternary.getRight() );
		append( " END ) " );
	}

	@Override
	protected void compileEntityJoin( EntityJoin join )
	{
		append( entityName( join.getEntity() ) )
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

			append( entityName( join.getTarget() ) ).append( ' ' ).append( join.getAlias() )
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

	@Override
	protected void compileBoolean( @NotNull BooleanObject object )
	{
		append( String.valueOf( object.isValue() ).toUpperCase() );
	}

	@Override
	protected void compileEntity( @NotNull EntityObject object )
	{
		append( object.getName() ).append( ".id" );
	}

	@Override
	protected void compileField( @NotNull FieldObject object )
	{
		String alias = object.getSourceAlias();
		if( alias == null )
			throw new IllegalStateException();
		append( alias ).append( '.' ).append( column( object.getField().getColumn() ) );
	}

	@Override
	protected void compileNumber( @NotNull NumberObject object )
	{
		append( object.getValue() );
	}

	@Override
	protected void compileString( @NotNull StringObject object )
	{
		append( " '" ).append( object.getValue() ).append( "' " );
	}


	protected static void checkSingleParameter( Call call )
	{
		if( call.getParameters().size() != 1 )
			throw new IllegalArgumentException( "Aggregate functions require single parameter" );
	}

	protected final void appendExpressions( String delimiter, Iterable<Expression> list )
	{
		boolean first = true;
		for( Expression expression : list )
		{
			if( first )
				first = false;
			else
				append( delimiter );

			compileRecursive( expression );
		}
	}
}
