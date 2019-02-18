package org.lastrix.easyorm.generator.postgresql;

import org.lastrix.easyorm.generator.AbstractInstallScriptGenerator;
import org.lastrix.easyorm.generator.GeneratorUtils;
import org.lastrix.easyorm.unit.Unit;
import org.lastrix.easyorm.unit.dbm.expr.EntityJoin;
import org.lastrix.easyorm.unit.dbm.expr.Expression;
import org.lastrix.easyorm.unit.dbm.expr.FieldJoin;
import org.lastrix.easyorm.unit.dbm.*;
import org.lastrix.easyorm.unit.java.Mapping;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.stream.Collectors;

public class PostgreInstallScript extends AbstractInstallScriptGenerator
{
	public PostgreInstallScript( Unit unit, File buildDir )
	{
		super( unit, buildDir, new PostgreSqlDialect() );
	}

	private final PostgreSqlExpressionCompiler eCompiler = new PostgreSqlExpressionCompiler();

	@Override
	protected void createFirst()
	{
		append( String.format(
				"CREATE SCHEMA %s;\nALTER SCHEMA %s OWNER TO %s;\n\n",
				getSchema(),
				getSchema(),
				getOwner()
		) );
	}

	@Override
	protected void compileLast()
	{
		Entity variable = getUnit().findEntity( "Variable" );
		if( variable == null )
			throw new IllegalStateException();

		append( String.format(
				"INSERT INTO %s (id,version,readonly,key,value) VALUES (DEFAULT,DEFAULT, TRUE, 'global.db.version', '%s');\n",
				getEntityName( variable ),
				getUnit().getVersion()
		) );
	}

	@Override
	protected void createTable( Table table )
	{
		if( getUnit().findClass( table.getEntityClass().getClassName() ) != null )
		{
			String sequenceName = getSequenceName( table );
			append( String.format(
					"CREATE SEQUENCE %s INCREMENT BY 1 MINVALUE 0 MAXVALUE %s START WITH 1 CACHE 1 CYCLE OWNED BY NONE;\nALTER SEQUENCE %s OWNER TO %s;\n\n",
					sequenceName,
					getMaxValueForTable( table ),
					sequenceName,
					getOwner()
			) );
		}

		String tableName = getEntityName( table );

		append( String.format( "CREATE TABLE %s(\n", tableName ) );

		table.getColumns()
				.stream()
				.filter( e -> e.getField().getMapping() != Mapping.MANY_TO_MANY && e.getField().getMapping() != Mapping.ONE_TO_MANY )
				.forEach( this :: generateColumnScript );

		String internalConstraints = table.getConstraints()
				.stream()
				.filter( e -> !( e instanceof ForeignKeyConstraint) )
				.map( this :: generateNonFkConstraintScripts )
				.collect( Collectors.joining( ", " + System.lineSeparator() ) );
		append( internalConstraints ).append( System.lineSeparator() );

		append( ");\n" );

		append( String.format(
				"ALTER TABLE %s  OWNER TO %s;\n\n",
				tableName,
				getOwner()
		) );

		if( table.getIndices() != null )
			table.getIndices().stream().filter( e -> e.getType() == IndexType.HINT )
					.forEach( this :: generateBTreeIndex );

		append( System.lineSeparator() ).append( System.lineSeparator() );
	}

	private void generateColumnScript( Column column )
	{
		append( "\t" )
				.append( column( column ) ).append( ' ' )
				.append( sqlType( column.getField() ) );

		if( !column.getField().isNullable() )
			append( " NOT NULL" );

		if( column.getName().equals( "id" ) )
			append( " DEFAULT nextval('" ).append( getSequenceName( column.getEntity() ) ).append( "'::REGCLASS)" );
		else if( !StringUtils.isBlank( column.getField().getDefaultValue() ) )
		{
			append( " DEFAULT " );
			appendDefaultValue( column.getField() );
		}

		append( GeneratorUtils.COMMA_WITH_NEWLINE );
	}

	private String generateNonFkConstraintScripts( Constraint constraint )
	{
		if( constraint instanceof PrimaryKeyConstraint )
		{
			PrimaryKeyConstraint pk = (PrimaryKeyConstraint)constraint;
			String columns = joinColumns( pk.getColumn(), ", " );
			return "CONSTRAINT pk_" + entity( pk.getTable() ) + " PRIMARY KEY (" + columns + ')';
		}
		else if( constraint instanceof UniqueConstraint )
		{
			UniqueConstraint uq = (UniqueConstraint)constraint;
			String columns = joinColumns( uq.getColumns(), ", " );
			String columnsKey = joinColumns( uq.getColumns(), "_" );
			return "CONSTRAINT uq_" + entity( uq.getTable() ) + '_' + columnsKey + " UNIQUE (" + columns + ')';
		}
		else
			throw new UnsupportedOperationException( constraint.getClass().getTypeName() );
	}

	private void generateBTreeIndex( Index index )
	{
		String columns = index.getColumns()
				.stream()
				.map( this :: mapBTreeColumn )
				.collect( Collectors.joining( GeneratorUtils.COMMA_WITH_NEWLINE ) );

		append( String.format(
				"CREATE INDEX idx_btree_%s_%s ON %s\n\t USING BTREE\n\t(\n%s\n\t);\n",
				entity( index.getTable() ), joinColumns( index.getColumns(), "_" ),
				getEntityName( index.getTable() ),
				columns
		) );
	}

	private String mapBTreeColumn( Column column )
	{
		return "\t\t" + column( column ) + " ASC NULLS LAST";
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
		Entity sourceTable = constraint.getSource().getEntity();
		String constraintName = getDialect().getForeignKeyConstraintName( constraint );

		append( String.format(
				"ALTER TABLE %s ADD CONSTRAINT %s FOREIGN KEY ( %s )\n REFERENCES %s ( %s )\n ON DELETE %s ON UPDATE %s;\n\n",
				getEntityName( sourceTable ),
				constraintName,
				column( constraint.getSource() ),
				getEntityName( constraint.getTarget().getEntity() ),
				column( constraint.getTarget() ),
				cascadeAction( constraint.getDelete() ),
				cascadeAction( constraint.getUpdate() )
		) );
	}

	private String getSequenceName( Entity entity )
	{
		return entity.getSchema() + ".sq_" + entity( entity );
	}

	@Override
	protected void createView( View view )
	{
		append( "CREATE VIEW " ).append( getEntityName( view ) ).append( " AS\n" );
		append( "SELECT\n" );
		new ViewColumnWriter( view ).write();
		append( "FROM " );
		new ViewFromWriter( view ).write();

		if( view.getWhere() != null )
		{
			append( "WHERE " ).append( eCompiler.compile( view.getWhere() ) ).append( '\n' );
		}

		if( !view.getGroupBy().isEmpty() )
		{
			append( "GROUP BY\n" ).append(
					view.getGroupBy()
							.stream()
							.map( e -> {
								Expression expression = view.getColumnSources().get( e );
								if( expression == null )
									throw new IllegalStateException( "No expression for groupBy column: " + e.getName() );
								return expression;
							} )
							.map( eCompiler:: compile )
							.collect( Collectors.joining( GeneratorUtils.COMMA_WITH_NEWLINE ) )
			).append( '\n' );
		}

		append( ";\n\n" );
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
					.collect( Collectors.joining( GeneratorUtils.COMMA_WITH_NEWLINE ) );
			append( select ).append( System.lineSeparator() );
		}

		private String asSelectColumn( Column column )
		{
			Expression expression = view.getColumnSources().get( column );
			if( expression == null )
				throw new IllegalStateException( "No source for column: " + column );


			return '\t' + eCompiler.compile( expression ) + " AS " + column( column );
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

			append( eCompiler.compile( view.getFrom() ) ).append( '\n' );

			for( Expression expression : view.getJoins() )
			{
				if( isLeft( expression ) )
					append( "LEFT " );
				append( "JOIN " ).append( eCompiler.compile( expression ) ).append( '\n' );
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
