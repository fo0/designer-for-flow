package com.github.appreciated.designer.template.java.generator.interfaces;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.Expression;
import com.vaadin.flow.component.Component;

import java.util.List;
import java.util.stream.Stream;

public interface ComponentJavaGenerator<T> {
    boolean canParse(Component propertyParent);

    boolean requiresGeneration(T propertyParent);

    Stream<Expression> generate(CompilationUnit compilationUnit, T propertyParent, Expression nameExpr);

    List<String> generatedProperties();
}
