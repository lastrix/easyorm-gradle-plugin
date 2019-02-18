package org.lastrix.easyorm.queryLanguage;

import org.lastrix.easyorm.queryLanguage.object.ViewTemplate;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;

public final class AqlUtil
{
	private AqlUtil()
	{
	}

	public static ViewTemplate parse( String name, String code )
	{
		try
		{
			CharStream stream = CharStreams.fromString( code, name );

//			ANTLRErrorListener listener = new ReflangANTLRErrorListener( name == null ? "(null)" : name );
			QueryLanguageLexer lexer = new QueryLanguageLexer( stream );
//			lexer.addErrorListener( listener );
			CommonTokenStream tokenStream = new CommonTokenStream( lexer );
			QueryLanguageParser parser = new QueryLanguageParser( tokenStream );
			parser.setBuildParseTree( false );
//			parser.addErrorListener( listener );
			parser.getInterpreter().setPredictionMode( PredictionMode.SLL );

			return parser.startStmt().result;
		} catch( Exception e )
		{
			throw new IllegalStateException( "Unable to parse source code: " + name, e );
		}
	}
}
