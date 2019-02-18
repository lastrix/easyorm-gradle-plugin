package org.lastrix.easyorm.unit;

import org.lastrix.easyorm.queryLanguage.object.Join;
import org.lastrix.easyorm.queryLanguage.object.JoinEntity;
import org.lastrix.easyorm.queryLanguage.object.JoinField;
import org.lastrix.easyorm.unit.dbm.Entity;
import org.lastrix.easyorm.unit.dbm.Table;
import org.lastrix.easyorm.unit.dbm.View;
import org.lastrix.easyorm.unit.dbm.expr.EntityJoin;
import org.lastrix.easyorm.unit.dbm.expr.Expression;
import org.lastrix.easyorm.unit.dbm.expr.FieldJoin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class Resolver
{
	Resolver( Unit unit )
	{
		this.unit = unit;
	}

	private final Unit unit;
	private View view;

	@Nullable
	public Entity resolveEntityByAlias( @NotNull String alias )
	{
		if( view.getFrom() != null && view.getFrom().getAlias().equalsIgnoreCase( alias ) )
			return view.getFrom().getEntity();

		for( Expression expression : view.getJoins() )
		{
			if( expression instanceof FieldJoin && ( (FieldJoin)expression ).getAlias().equalsIgnoreCase( alias ) )
				return ( (FieldJoin)expression ).getTarget();
			else if( expression instanceof EntityJoin && ( (EntityJoin)expression ).getAlias().equalsIgnoreCase( alias ) )
				return ( (EntityJoin)expression ).getEntity();
		}

		return null;
	}


	@Nullable
	public Entity resolveEntityByJoin( @NotNull Join join )
	{
		if( join instanceof JoinEntity )
			return resolveEntityByName( ( (JoinEntity)join ).getEntity() );

		if( join instanceof JoinField )
			return resolveEntityByAlias( join.getAlias() );

		throw new UnsupportedOperationException( join.getClass().getTypeName() );
	}

	@Nullable
	public Entity resolveEntityByName( @NotNull String entity )
	{
		if( view.getFrom() != null && view.getFrom().getEntity().getName().equals( entity ) )
			return view.getFrom().getEntity();

		for( Expression expression : view.getJoins() )
		{
			if( expression instanceof FieldJoin )
			{
				FieldJoin join = (FieldJoin)expression;
				Entity e = join.getSourceEntity();
				if( e.getName().equals( entity ) )
					return e;
			}
			else if( expression instanceof EntityJoin )
			{
				EntityJoin join = (EntityJoin)expression;
				Entity e = join.getEntity();
				if( e.getName().equals( entity ) )
					return e;
			}
			else
				throw new UnsupportedOperationException( expression.getClass().getTypeName() );
		}

		for( Table table : unit.getTables() )
			if( table.getName().equals( entity ) )
				return table;

		for( View e : unit.getViews() )
			if( e.getName().equals( entity ) )
				return e;

		return null;
	}

	public void setView( View view )
	{
		this.view = view;
	}
}
