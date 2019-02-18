package org.lastrix.easyorm.unit;

import org.jetbrains.annotations.NotNull;
import org.lastrix.easyorm.queryLanguage.object.*;
import org.lastrix.easyorm.unit.dbm.Entity;
import org.lastrix.easyorm.unit.dbm.expr.Expression;
import org.lastrix.easyorm.unit.dbm.expr.*;
import org.lastrix.easyorm.unit.dbm.expr.object.*;
import org.lastrix.easyorm.unit.java.EntityField;

import java.util.stream.Collectors;

final class ExpressionCompiler {
    ExpressionCompiler(Unit unit, Resolver resolver) {
        this.unit = unit;
        this.resolver = resolver;
    }

    private final Unit unit;
    private final Resolver resolver;

    public Expression compileExpression(org.lastrix.easyorm.queryLanguage.object.Expression e) {
        return compile(e);
    }

    private Expression compile(@NotNull org.lastrix.easyorm.queryLanguage.object.Expression e) {
        if (e instanceof BinaryExpression)
            return new Binary(((BinaryExpression) e).getKind(), compile(((BinaryExpression) e).getLeft()), compile(((BinaryExpression) e).getRight()));

        if (e instanceof BooleanRef)
            return ((BooleanRef) e).isValue()
                    ? BooleanObject.TRUE
                    : BooleanObject.FALSE;

        if (e instanceof CallExpression)
            return new Call(
                    ((CallExpression) e).getFunctionName(),
                    ((CallExpression) e).getParameters().stream().map(this::compile).collect(Collectors.toList()));

        if (e instanceof EntityRef)
            return compileEntityRef((EntityRef) e);

        if (e instanceof FieldRef)
            return compileFieldRef((FieldRef) e);

        if (e instanceof LogicalListExpression)
            return new Logical(
                    ((LogicalListExpression) e).getKind(),
                    ((LogicalListExpression) e).getItems().stream().map(this::compile).collect(Collectors.toList()));

        if (e instanceof NotExpression)
            return new Not(compile(((NotExpression) e).getExpression()));

        if (e instanceof NumberRef)
            return new NumberObject(((NumberRef) e).getValue());

        if (e instanceof ParenExpression)
            return new Paren(compile(((ParenExpression) e).getExpression()));

        if (e instanceof StringRef)
            return new StringObject(((StringRef) e).getValue());

        if (e instanceof TernaryExpression)
            return new Ternary(
                    compile(((TernaryExpression) e).getCondition()),
                    compile(((TernaryExpression) e).getLeft()),
                    compile(((TernaryExpression) e).getRight())
            );

        throw new UnsupportedOperationException(e.getClass().getTypeName());
    }

    private Expression compileFieldRef(FieldRef ref) {
        if (ref.getJoin() == null)
            throw new IllegalArgumentException("No join for field: " + ref.getFieldName());

        Entity entity = resolver.resolveEntityByJoin(ref.getJoin());
        if (entity == null)
            throw new IllegalArgumentException("No entity found for field: " + ref.getFieldName());

        EntityField field = entity.getEntityClass().findField(ref.getFieldName());
        if (field == null)
            throw new IllegalArgumentException("No field '" + ref.getFieldName() + "' in entity: " + entity.getName());

        return new FieldObject(ref.getJoin().getAlias(), field);
    }

    private Expression compileEntityRef(EntityRef ref) {
        if (ref.getName().equals("t") && resolver.resolveEntityByAlias(ref.getName()).getName().equals("PatternMatch")) {
            int k = 0;
        }
        Entity entity = resolver.resolveEntityByAlias(ref.getName());
        if (entity == null)
            entity = resolver.resolveEntityByName(ref.getName());

        if (entity == null)
            throw new IllegalArgumentException("No entity for: " + ref.getName());
        return new EntityObject(ref.getName(), entity);
    }
}
