package org.lastrix.easyorm.generator.hsqldb;

import org.lastrix.easyorm.generator.AbstractInstallScriptGenerator;
import org.lastrix.easyorm.unit.Unit;
import org.lastrix.easyorm.unit.dbm.expr.EntityJoin;
import org.lastrix.easyorm.unit.dbm.expr.Expression;
import org.lastrix.easyorm.unit.dbm.expr.FieldJoin;
import org.lastrix.easyorm.unit.dbm.*;
import org.lastrix.easyorm.unit.java.Mapping;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.stream.Collectors;

public final class HsqlDbInstallScript extends AbstractInstallScriptGenerator
{
	public HsqlDbInstallScript( Unit unit, File buildDir )
	{
		super( unit, buildDir, new HsqlDbDialect() );
	}

	@Override
	protected void createFirst()
	{
		// nothing to do
	}

	@Override
	protected void compileLast()
	{
		// nothing to do
	}

	@Override
	protected void createTable( Table table )
	{
		String tableName = entity( table );

		append( String.format( "CREATE TABLE %s( ", tableName ) );

		table.getColumns()
				.stream()
				.filter( e -> e.getField().getMapping() != Mapping.MANY_TO_MANY && e.getField().getMapping() != Mapping.ONE_TO_MANY )
				.forEach( this :: generateColumnScript );

		String internalConstraints = table.getConstraints()
				.stream()
				.filter( e -> !( e instanceof ForeignKeyConstraint) )
				.map( this :: generateNonFkConstraintScripts )
				.collect( Collectors.joining( ", " ) );
		append( internalConstraints );

		append( " );\n" );

		// TODO: indexes
//		if( table.getIndices() != null )
//			table.getIndices().stream().filter( e -> e.getType() == IndexType.HINT )
//					.forEach( this :: generateBTreeIndex );

		append( System.lineSeparator() ).append( System.lineSeparator() );
	}

	private void generateColumnScript( Column column )
	{
		append( "\t" )
				.append( column( column ) ).append( ' ' )
				.append( sqlType( column.getField() ) );

		if( !StringUtils.isBlank( column.getField().getDefaultValue() ) )
		{
			append( " DEFAULT " );
			appendDefaultValue( column.getField() );
		}

		if( !column.getField().isNullable() )
			append( " NOT NULL" );

		append( ", " );
	}

	private String generateNonFkConstraintScripts( Constraint constraint )
	{
		if( constraint instanceof PrimaryKeyConstraint)
		{
			PrimaryKeyConstraint pk = (PrimaryKeyConstraint)constraint;
			String columns = joinColumns( pk.getColumn(), ", " );
			return "CONSTRAINT PK_" + entity( pk.getTable() ) + " PRIMARY KEY (" + columns + ')';
		}
		else if( constraint instanceof UniqueConstraint )
		{
			UniqueConstraint uq = (UniqueConstraint)constraint;
			String columns = joinColumns( uq.getColumns(), ", " );
			String columnsKey = joinColumns( uq.getColumns(), "_" );
			return "CONSTRAINT UQ_" + entity( uq.getTable() ) + '_' + columnsKey + " UNIQUE (" + columns + ')';
		}
		else
			throw new UnsupportedOperationException( constraint.getClass().getTypeName() );
	}

	@Override
	protected void createForeignKeys( Table table )
	{
		table.getConstraints().stream().filter( e -> e instanceof ForeignKeyConstraint )
				.map( e -> (ForeignKeyConstraint)e )
				.forEach( this :: generateForeignKeyScript );
	}

	private void generateForeignKeyScript( ForeignKeyConstraint constraint )
	{
		if( constraint.getSource() == null )
			throw new IllegalStateException();

		Entity sourceTable = constraint.getSource().getEntity();
		String constraintName = String.format( "FK_%s_%s", entity( sourceTable ), column( constraint.getSource() ) );

		append( String.format(
				"ALTER TABLE %s ADD CONSTRAINT %s FOREIGN KEY ( %s ) REFERENCES %s ( %s ) ON DELETE %s ON UPDATE %s;\n",
				entity( sourceTable ),
				constraintName,
				column( constraint.getSource() ),
				entity( constraint.getTarget().getEntity() ),
				column( constraint.getTarget() ),
				cascadeAction( constraint.getDelete() ),
				cascadeAction( constraint.getUpdate() )
		) );
	}

	private final HsqlDbExpressionCompiler eCompiler = new HsqlDbExpressionCompiler();

	@Override
	protected void createView( View view )
	{
		append( "CREATE VIEW " ).append( entity( view ) ).append( " AS SELECT " );
		new ViewColumnWriter( view ).write();
		append( "FROM " );
		new ViewFromWriter( view ).write();

		if( view.getWhere() != null )
		{
			append( "WHERE " ).append( eCompiler.compile( view.getWhere() ) ).append( ' ' );
		}

		if( !view.getGroupBy().isEmpty() )
		{
			append( "GROUP BY " ).append(
					view.getGroupBy()
							.stream()
							.map( e -> {
								Expression expression = view.getColumnSources().get( e );
								if( expression == null )
									throw new IllegalStateException( "No expression for groupBy column: " + e.getName() );
								return expression;
							} )
							.map( eCompiler:: compile )
							.collect( Collectors.joining( ", " ) )
			);
		}

		append( ";\n" );
	}

	private final class ViewColumnWriter
	{
		private ViewColumnWriter( View view )
		{
			this.view = view;
		}

		private final View view;

		void write()
		{
			String select = view.getColumns().stream()
					.map( this :: asSelectColumn )
					.collect( Collectors.joining( ", " ) );
			append( select ).append( "  " );
		}

		private String asSelectColumn( Column column )
		{
			Expression expression = view.getColumnSources().get( column );
			if( expression == null )
				throw new IllegalStateException( "No source for column: " + column );


			return "  " + eCompiler.compile( expression ) + " AS " + column( column );
		}
	}

	private final class ViewFromWriter
	{
		private ViewFromWriter( View view )
		{
			this.view = view;
		}

		private final View view;

		void write()
		{
			if( view.getFrom() == null )
				throw new IllegalStateException( "No sources for: " + view.getName() );

			append( eCompiler.compile( view.getFrom() ) ).append( "  " );

			for( Expression expression : view.getJoins() )
			{
				if( isLeft( expression ) )
					append( "LEFT " );
				append( "JOIN " ).append( eCompiler.compile( expression ) ).append( "  " );
			}
		}

		private boolean isLeft( Expression expression )
		{
			if( expression instanceof FieldJoin )
				return ( (FieldJoin)expression ).isLeft();

			return expression instanceof EntityJoin && ( (EntityJoin)expression ).isLeft();
		}
	}

}
