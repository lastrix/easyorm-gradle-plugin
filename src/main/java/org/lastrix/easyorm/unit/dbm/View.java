package org.lastrix.easyorm.unit.dbm;

import org.lastrix.easyorm.unit.dbm.expr.EntityJoin;
import org.lastrix.easyorm.unit.dbm.expr.Expression;
import org.lastrix.easyorm.unit.java.EntityClass;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode( of = {"schema", "name"} )
@ToString( of = {"schema", "name"} )
public final class View implements Entity
{
	private final String schema;
	private final String name;
	private final EntityClass entityClass;
	private final List<Column> columns = new ArrayList<>();

	private EntityJoin from;
	private final List<Expression> joins = new ArrayList<>();
	private final Map<Column, Expression> columnSources = new HashMap<>();
	private Expression where;
	private final List<Column> groupBy = new ArrayList<>();
}
