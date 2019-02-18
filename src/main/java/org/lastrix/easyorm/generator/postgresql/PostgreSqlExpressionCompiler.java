package org.lastrix.easyorm.generator.postgresql;

import org.lastrix.easyorm.generator.AbstractSqlExpressionCompiler;

public final class PostgreSqlExpressionCompiler extends AbstractSqlExpressionCompiler
{
	public PostgreSqlExpressionCompiler()
	{
		super( new PostgreSqlDialect() );
	}
}
