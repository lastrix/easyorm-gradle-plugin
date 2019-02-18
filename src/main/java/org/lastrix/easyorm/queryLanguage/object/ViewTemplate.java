package org.lastrix.easyorm.queryLanguage.object;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode( exclude = {"fields", "where", "from", "joins", "groupBy"} )
@ToString( exclude = {"fields", "where", "from", "joins", "groupBy"} )
public final class ViewTemplate
{
	private final List<ViewField> fields = new ArrayList<>();
	private Expression where;
	private JoinEntity from;
	private final List<Join> joins = new ArrayList<>();
	private final List<ViewField> groupBy = new ArrayList<>();


	public void addField( String alias, Expression expression )
	{
		if( getFieldByName( alias ) != null )
			throw new IllegalArgumentException( "Field redeclaration: " + alias );

		fields.add( new ViewField( alias, expression ) );
	}

	public void setFromTable( @NotNull EntityRef entity, @NotNull String alias )
	{
		from = new JoinEntity( false, entity.getName(), alias );
	}

	public JoinEntity addJoinEntity( boolean left, @NotNull EntityRef entity, @NotNull String alias )
	{
		JoinEntity e = new JoinEntity( left, entity.getName(), alias );
		joins.add( e );
		return e;
	}

	public void addJoinField( boolean left, @NotNull FieldRef field, @NotNull String alias )
	{
		joins.add( new JoinField( left, field, alias ) );
	}

	public void addGroupBy( @NotNull String alias )
	{
		ViewField field = getFieldByName( alias );
		if( field == null )
			throw new IllegalArgumentException( "No field for alias: " + alias );
		groupBy.add( field );
	}

	public boolean hasAlias( @NotNull String name )
	{
		return getFieldByName( name ) != null || getJoinByAlias( name ) != null;
	}

	@Nullable
	public Join getJoinByAlias( @NotNull String name )
	{
		if( from.getAlias().equalsIgnoreCase( name ) )
			return from;

		return joins.stream()
				.filter( e -> e.getAlias().equalsIgnoreCase( name ) )
				.findFirst()
				.orElse( null );
	}

	@Nullable
	public ViewField getFieldByName( @NotNull String name )
	{
		return fields.stream()
				.filter( e -> e.getName().equalsIgnoreCase( name ) )
				.findFirst()
				.orElse( null );
	}

	public EntityRef rootEntityRef( @NotNull String name )
	{
		return new EntityRef( name );
	}

	@NotNull
	public EntityRef entityRef( @NotNull String name )
	{
		return new EntityRef( name );
	}

	@NotNull
	public FieldRef fieldRef( @NotNull String source, @NotNull String name )
	{
		Join join = getJoinByAlias( source );
		if( join == null )
			throw new IllegalArgumentException( "No source by alias: " + source );
		return new FieldRef( name, join );
	}
}
