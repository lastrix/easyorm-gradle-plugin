grammar QueryLanguage;

@header
{
package org.lastrix.easyorm.queryLanguage;

import org.lastrix.easyorm.queryLanguage.object.*;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
}

startStmt returns [ ViewTemplate result ]
    @init { $result = new ViewTemplate(); }
    :   fromPart[$result]
        fieldsPart[$result]
        wherePart[$result]?
        groupPart[$result]?
    ;

fieldsPart[ ViewTemplate view ]
    :   FIELDS LBRACE
        fieldPart[$view]+
        RBRACE
    ;

fromPart[ ViewTemplate view ]
    :   FROM LBRACE
        fromEntityNoExpr[$view] SEMI
        (joinTableOrField[$view] SEMI)*
        RBRACE
    ;

wherePart[ ViewTemplate view ]
    :   WHERE LBRACE expr[$view] RBRACE
        { $view.setWhere($expr.result); }
    ;

groupPart[ ViewTemplate view ]
    :   GROUP LBRACK
        alias { $view.addGroupBy($alias.text); }
        (COMMA alias { $view.addGroupBy($alias.text); } )*
        RBRACK
    ;

fieldPart[ ViewTemplate view ]
    :   identifier
        LBRACE expr[$view] RBRACE
        { $view.addField($identifier.text, $expr.result); }
    ;


fromEntityNoExpr[ ViewTemplate view ]
    :   identifier alias
        { $view.setFromTable( $view.rootEntityRef($identifier.text), $alias.text ); }
    ;

joinTableOrField[ ViewTemplate view ]
    :   LEFT?
        (
            joinEntity[$view, $LEFT != null] | joinField[$view, $LEFT != null]
        )
    ;

joinEntity[ ViewTemplate view, boolean left ]
    :   entity[$view] alias
        { JoinEntity e = $view.addJoinEntity($left, $entity.result, $alias.text); }
        LBRACE expr[$view] RBRACE
        { e.setExpression($expr.result); }
    ;

joinField[ ViewTemplate view, boolean left ]
    :   field[$view] alias
        { $view.addJoinField($left, $field.result, $alias.text); }
    ;

expr[ ViewTemplate view ] returns [ Expression result ]
    :   ternaryExpr[$view] { $result = $ternaryExpr.result; }
    ;

ternaryExpr[ ViewTemplate view ] returns [ Expression result ]
    :   cond=orExpr[$view] { $result = $cond.result; }
    (
        INTERR
        left=expr[$view]
        COLON
        right =expr[$view]
        { $result = new TernaryExpression($cond.result, $left.result, $right.result); }
    )?
    ;

orExpr[ ViewTemplate view ] returns [ Expression result ]
    :   andExpr[$view]
        { $result = $andExpr.result;}
        (
            { LogicalListExpression e = new LogicalListExpression(LogicalKind.OR); }
            { e.add($andExpr.result); }
            ( OR andExpr[$view] { e.add($andExpr.result); } )+
            { $result = e;}
        )?
    ;

andExpr[ ViewTemplate view ] returns [ Expression result ]
    :   equalityExpr[$view]
        { $result = $equalityExpr.result;}
        (
            { LogicalListExpression e = new LogicalListExpression(LogicalKind.AND); }
            { e.add($equalityExpr.result); }
            (AND equalityExpr[$view] { e.add($equalityExpr.result); } )+
            { $result = e;}
        )?
    ;

equalityExpr[ ViewTemplate view ] returns [ Expression result ]
    :   left=comparisonExpr[$view]
        { $result = $left.result; }
        (
            op=(EQUALS|NOT_EQUAL)
            right=comparisonExpr[$view]
            { $result = BinaryExpression.create($op.text, $left.result, $right.result); }
        )?
    ;

comparisonExpr[ ViewTemplate view ] returns [ Expression result ]
    :   left = unary[$view]
        { $result = $left.result; }
        (
            op=(LT|GT|LE|GE)
            right = unary[$view]
            { $result = BinaryExpression.create($op.text, $left.result, $right.result); }
        )?
    ;


unary[ ViewTemplate view ] returns [ Expression result ]
    :   functionCall[$view] { $result = $functionCall.result; }
    |   { boolean negation = false; }
        (NOT { negation = true;})?
        atom[$view]
        { $result = negation ? new NotExpression($atom.result) : $atom.result; }
    |   LPAREN expr[$view] RPAREN
        { $result = new ParenExpression($expr.result); }
    ;

atom[ ViewTemplate view ] returns [ Expression result ]
    :   field[$view] { $result = $field.result; }
    |   entity[$view] { $result = $entity.result; }
    |   StringLiteral { $result = new StringRef($StringLiteral.text); }
    |   NumberLiteral { $result = new NumberRef($NumberLiteral.text); }
    |   BooleanLiteral { $result = new BooleanRef($BooleanLiteral.text); }
    ;

functionCall[ ViewTemplate view ] returns [ Expression result ]
    :   identifier
        {
            CallExpression e = new CallExpression($identifier.text);
            $result = e;
        }
        LPAREN
            expr[$view]
            { e.addParameter($expr.result); }
            (COMMA expr[$view] { e.addParameter($expr.result); } )*
        RPAREN
    ;

entity[ ViewTemplate view ] returns [ EntityRef result ]
    :   identifier
        { $result = $view.entityRef($identifier.text);}
    ;

alias
    :   identifier
    ;

field[ ViewTemplate view ] returns [ FieldRef result ]
    :   source=identifier DOT name=identifier
        { $result = $view.fieldRef($source.text, $name.text); }
    ;

identifier
    :   IDENTIFIER
    |   FIELDS
    |   FROM
    |   WHERE
    |   GROUP
    |   LEFT
    ;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

FIELDS: 'FIELDS';
FROM: 'FROM';
WHERE: 'WHERE';
GROUP: 'GROUP';
LEFT: 'LEFT';

LBRACE: '{';
RBRACE: '}';
LBRACK: '[';
RBRACK: ']';
LPAREN: '(';
RPAREN: ')';
COMMA: ',';
SEMI: ';';
COLON: ':';
INTERR: '?';
GT              : '>';
LT              : '<';
LE              : '<=';
GE              : '>=';

AND: '&&';
OR: '||';
NOT_EQUAL: '!=';
NOT: '!';
DOT: '.';
EQUALS: '==';

IDENTIFIER: [_A-Za-z][_A-Za-z0-9]*;

NumberLiteral
    :   '0' |
        NonZeroDigit Digit*
    ;

fragment
Digit
    :   '0'
    |   NonZeroDigit
    ;


fragment
NonZeroDigit
    :   [1-9]
    ;


BooleanLiteral
    :   'true' | 'TRUE'
    |   'false' | 'FALSE'
    ;

StringLiteral
    :   '\'' StringCharacters? '\''
    ;

fragment
StringCharacters
    :   StringCharacter+
    ;

fragment
StringCharacter
    :   ~['\\]
    ;

WS  :  [ \t\r\n\u000C]+ -> skip
    ;
