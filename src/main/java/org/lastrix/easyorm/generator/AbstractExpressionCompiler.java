package org.lastrix.easyorm.generator;

import org.lastrix.easyorm.generator.hibernate.Dialect;
import org.lastrix.easyorm.unit.dbm.Column;
import org.lastrix.easyorm.unit.dbm.Entity;
import org.jetbrains.annotations.NotNull;
import org.lastrix.easyorm.unit.dbm.expr.*;
import org.lastrix.easyorm.unit.dbm.expr.object.*;

public abstract class AbstractExpressionCompiler
{
	protected AbstractExpressionCompiler( Dialect dialect )
	{
		this.dialect = dialect;
	}

	private final Dialect dialect;
	private StringBuilder sb;

	@NotNull
	protected final String column( @NotNull Column column )
	{
		return dialect.column( column );
	}

	@NotNull
	protected final String entity( @NotNull Entity table )
	{
		return dialect.entity( table );
	}

	@NotNull
	protected final String aggregateFunction( @NotNull String name )
	{
		return dialect.aggregateFunction( name );
	}

	protected final StringBuilder append( Object obj )
	{
		return sb.append( obj );
	}

	protected final StringBuilder append( String str )
	{
		return sb.append( str );
	}

	protected final StringBuilder append( CharSequence value )
	{
		return sb.append( value );
	}

	protected final StringBuilder append( int i )
	{
		return sb.append( i );
	}

	protected final StringBuilder append( long lng )
	{
		return sb.append( lng );
	}

	protected final StringBuilder append( float f )
	{
		return sb.append( f );
	}

	protected final StringBuilder append( double d )
	{
		return sb.append( d );
	}

	protected final String entityName( Entity entity )
	{
		return entity.getSchema() + '.' + dialect.entity( entity );
	}

	@NotNull
	public final String compile( @NotNull Expression expression )
	{
		sb = new StringBuilder();
		compileRecursive( expression );
		return sb.toString();
	}

	protected final void compileRecursive( @NotNull Expression expression )
	{
		if( expression instanceof RefObject)
			compileObject( (RefObject)expression );
		else if( expression instanceof Binary)
			compileBinary( (Binary)expression );
		else if( expression instanceof Call)
			compileCall( (Call)expression );
		else if( expression instanceof Logical )
			compileLogical( (Logical)expression );
		else if( expression instanceof Not )
			compileNot( (Not)expression );
		else if( expression instanceof Paren )
			compileParen( (Paren)expression );
		else if( expression instanceof Ternary )
			compileTernary( (Ternary)expression );
		else if( expression instanceof EntityJoin)
			compileEntityJoin( (EntityJoin)expression );
		else if( expression instanceof FieldJoin)
			compileFieldJoin( (FieldJoin)expression );
		else
			throw new UnsupportedOperationException( expression.getClass().getTypeName() );
	}


	private void compileObject( @NotNull RefObject object )
	{
		if( object instanceof BooleanObject)
			compileBoolean( (BooleanObject)object );
		else if( object instanceof EntityObject)
			compileEntity( (EntityObject)object );
		else if( object instanceof FieldObject)
			compileField( (FieldObject)object );
		else if( object instanceof NumberObject)
			compileNumber( (NumberObject)object );
		else if( object instanceof StringObject )
			compileString( (StringObject)object );
	}

	protected abstract void compileBinary( @NotNull Binary binary );

	protected abstract void compileCall( @NotNull Call call );

	protected abstract void compileLogical( @NotNull Logical logical );

	protected abstract void compileNot( @NotNull Not not );

	protected abstract void compileParen( @NotNull Paren paren );

	protected abstract void compileTernary( @NotNull Ternary ternary );

	protected abstract void compileEntityJoin( EntityJoin join );

	protected abstract void compileFieldJoin( FieldJoin join );

	protected abstract void compileBoolean( @NotNull BooleanObject object );

	protected abstract void compileEntity( @NotNull EntityObject object );

	protected abstract void compileField( @NotNull FieldObject object );

	protected abstract void compileNumber( @NotNull NumberObject object );

	protected abstract void compileString( @NotNull StringObject object );
}
