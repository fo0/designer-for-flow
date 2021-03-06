package com.github.appreciated.designer.template.java.parser;

import com.github.appreciated.designer.application.model.CompilationMetaInformation;
import com.github.appreciated.designer.application.model.project.Project;
import com.github.appreciated.designer.application.service.ComponentService;
import com.github.appreciated.designer.helper.DesignerFileHelper;
import com.github.appreciated.designer.helper.FieldNameHelper;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.binder.HasItems;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ComponentTreeParser {

    private final ClassOrInterfaceDeclaration classDefinition;
    private final ComponentService service;
    private final CompilationUnit compilationUnit;
    private final Project project;
    private final Map<Component, CompilationMetaInformation> compilationMetaInformation = new HashMap<>();
    private final List<String> components;
    Component rootComponent;
    Map<String, Component> fieldMap = new HashMap<>();
    private String className;

    public ComponentTreeParser(CompilationUnit compilationUnit, Project project) throws ParseException, ClassNotFoundException {
        this.compilationUnit = compilationUnit;
        this.project = project;
        service = new ComponentService();
        components = FieldNameHelper.getComponents(project).collect(Collectors.toList());
        if (compilationUnit.getPrimaryType().isPresent()) {
            TypeDeclaration<?> definition = compilationUnit.getPrimaryType().get();
            if (definition instanceof ClassOrInterfaceDeclaration) {
                classDefinition = (ClassOrInterfaceDeclaration) definition;
            } else {
                throw new ParseException("Nope!");
            }
        } else {
            throw new ParseException("Nope!");
        }
        instantiateRootComponent();
        classDefinition.getFields().forEach(this::instantiateField);
        // If the design does not have a constructor there is nothing left to do
        if (classDefinition.getConstructors().size() > 0) {
            for (Statement statement : classDefinition.getConstructors().get(0).getBody().getStatements()) {
                processBodyStatement(statement);
            }
        }
    }

    public Object invokeMethodOnComponent(MethodCallExpr expression, Component component) throws ClassNotFoundException {
        List<Object> list = new ArrayList<>();
        for (Expression expression1 : expression.getArguments()) {
            Object o = resolveParameter(expression1, component);
            list.add(o);
        }
        Object[] args = list.toArray();

        Method[] foundMethods = Arrays.stream(component.getClass().getMethods())
                .filter(method1 -> method1.getName().equals(expression.getNameAsString()))
                .filter(method1 -> method1.getParameterCount() == args.length || method1.isVarArgs())
                .toArray(Method[]::new);

        boolean found = false;
        ArrayList<Throwable> errors = new ArrayList<>();
        Object result = null;
        for (Method foundMethod : foundMethods) {
            try {
                if (args.length == 1 && !foundMethod.isVarArgs()) {
                    result = foundMethod.invoke(component, args[0]);
                    found = true;
                } else {
                    if (args[0].getClass() == String.class) {
                        result = foundMethod.invoke(component, new Object[]{Arrays.stream(args).toArray(String[]::new)});
                        if (component instanceof HasItems && foundMethod.getName().equals("setItems")) {
                            CompilationMetaInformation info = new CompilationMetaInformation();
                            info.setPropertyReplacement("items", Arrays.stream(Arrays.stream(args).toArray(String[]::new)).collect(Collectors.toList()));
                            compilationMetaInformation.put(component, info);
                        }
                    } else {
                        Class clazz = args[0] instanceof Component ? Component.class : args[0].getClass();
                        result = foundMethod.invoke(component, new Object[]{Arrays.stream(args).toArray(value -> (Object[]) Array.newInstance(clazz, value))});
                    }
                    found = true;
                }
            } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException | ArrayStoreException e) {
                // These are only relevant if no fitting method was found so we save them for later
                errors.add(e);
            }
        }
        if (!found) {
            errors.forEach(Throwable::printStackTrace);
            throw new UnsupportedOperationException("No fitting Method found for:\"" + expression.getNameAsString() + "\"");
        }
        return result;
    }

    Object resolveParameter(Expression arg, Component component) throws ClassNotFoundException {
        if (arg instanceof StringLiteralExpr) {
            return ((StringLiteralExpr) arg).asString();
        } else if (arg instanceof BooleanLiteralExpr) {
            return ((BooleanLiteralExpr) arg).getValue();
        } else if (arg instanceof IntegerLiteralExpr) {
            return ((IntegerLiteralExpr) arg).asNumber().intValue();
        } else if (arg instanceof DoubleLiteralExpr) {
            return ((DoubleLiteralExpr) arg).asDouble();
        } else if (arg instanceof NameExpr) {
            return fieldMap.get(((NameExpr) arg).getNameAsString());
        } else if (arg instanceof FieldAccessExpr) {
            return resolveFieldAccess((FieldAccessExpr) arg);
        } else if (arg instanceof ObjectCreationExpr) {
            return resolveObjectCreation((ObjectCreationExpr) arg);
        } else if (arg instanceof MethodCallExpr) {
            if (((MethodCallExpr) arg).getNameAsString().equals("getTranslation")) {
                StringLiteralExpr argumentExpression = (StringLiteralExpr) ((MethodCallExpr) arg).getArguments().getFirst().get();
                CompilationMetaInformation info = new CompilationMetaInformation();
                info.setPropertyReplacement("text", argumentExpression.asString());
                compilationMetaInformation.put(component, info);
                return project.getTranslationForKey(argumentExpression.getValue());
            }
            return invokeMethodOnComponent((MethodCallExpr) arg, rootComponent);
        } else {
            throw new UnsupportedOperationException(arg.getClass().getSimpleName() + " is not supported");
        }
    }

    private Object resolveObjectCreation(ObjectCreationExpr arg) {
        return instantiateClassWithName(arg.getTypeAsString(), arg.getArguments().stream().map(expression -> {
            try {
                return resolveParameter(expression, null);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }).toArray());
    }

    private Object resolveFieldAccess(FieldAccessExpr arg) throws ClassNotFoundException {
        if (fieldMap.containsKey(arg.getNameAsString())) {
            accessFieldOfObject(fieldMap.get(arg.getNameAsString()), arg);
        } else {
            // Enums
            String[] scope = resolveScope(arg).toArray(String[]::new);
            String resolved = resolveName(scope[0]).get();
            Class classname = Class.forName(resolved + "$" + scope[1]);
            return Enum.valueOf(classname, scope[2]);
        }
        return null;
    }

    private Object accessFieldOfObject(Object clazz, FieldAccessExpr arg) {
        String[] access = resolveScope(arg).skip(1).toArray(String[]::new);
        return null;
    }

    Stream<String> resolveScope(FieldAccessExpr expr) {
        if (expr.getScope() instanceof NameExpr) {
            return Stream.of(((NameExpr) expr.getScope()).getNameAsString(), expr.getNameAsString());
        } else {
            return Stream.concat(resolveScope((FieldAccessExpr) expr.getScope()), Stream.of(expr.getNameAsString()));
        }
    }

    private void instantiateRootComponent() {
        className = classDefinition.getNameAsString();
        String typeName = classDefinition.getExtendedTypes().get(0).getNameAsString();
        rootComponent = (Component) instantiateClassWithName(typeName);
    }

    private Object instantiateClassWithName(String typeName, Object... args) {
        try {
            Class<?> className = Class.forName(resolveName(typeName).get());
            if (args.length == 0) {
                return className.getDeclaredConstructor().newInstance();
            } else {
                Class[] argClasses = Arrays.stream(args).map(o -> o.getClass())
                        .map(aClass -> aClass == Integer.class ? int.class : aClass)
                        .toArray(Class[]::new);
                return className.getDeclaredConstructor(argClasses).newInstance(args);
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | NoSuchElementException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Optional<String> resolveName(String typeName) {
        // workaround for subclasses like FormLayout.ResponsiveStep
        String classTypeName = typeName.contains(".") ? typeName.split("\\.")[0] : typeName;
        Optional<ImportDeclaration> declaration = compilationUnit.getImports()
                .stream()
                .filter(importDeclaration -> importDeclaration.getName().asString().endsWith(classTypeName))
                .findFirst();
        if (declaration.isPresent()) {
            if (typeName.contains(".")) {
                String name = declaration.get().getNameAsString();
                name = name.substring(0, name.indexOf(typeName.split("\\.")[0]));
                return Optional.of(name + typeName.replace(".", "$"));
            } else {
                return declaration.map(importDeclaration -> importDeclaration.getName().asString());
            }
        } else {
            return compilationUnit.getImports()
                    .stream()
                    .filter(importDeclaration -> importDeclaration.getNameAsString().startsWith("com.vaadin"))
                    .filter(ImportDeclaration::isAsterisk)
                    .filter(importDeclaration -> {
                        try {
                            // Since we don't know if there are multiple imports with asterisks which is the correct one
                            // the correct one needs to be found by bruteforce
                            Class.forName(importDeclaration.getNameAsString() + "." + typeName);
                            return true;
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                            return false;
                        }
                    })
                    .map(importDeclaration -> importDeclaration.getNameAsString() + "." + typeName)
                    .findFirst();
        }
    }

    private void instantiateField(FieldDeclaration field) {
        String fieldName = field.getVariables().get(0).getNameAsString();
        Optional<String> fieldType = resolveName(field.getElementType().asString());
        boolean isClassAvailable = false;
        try {
            if (fieldType.isPresent()) {
                Class.forName(fieldType.get());
                isClassAvailable = true;
            }
        } catch (ClassNotFoundException e) {
        }
        if (fieldType.isPresent() && isClassAvailable) {
            Component component = (Component) instantiateClassWithName(field.getElementType().asString());
            fieldMap.put(fieldName, component);
            if (FieldNameHelper.isFieldNameValid(fieldName, component, components.stream(), compilationMetaInformation)) {
                getOrCreateCompilationMetaInformation(component).setVariableName(fieldName);
            }
        } else if (isFieldProjectComponent(field)) {
            Component component = (Component) instantiateProjectComponentClassWithName(field);
            fieldMap.put(fieldName, component);
            if (FieldNameHelper.isFieldNameValid(fieldName, component, components.stream(), compilationMetaInformation)) {
                getOrCreateCompilationMetaInformation(component).setVariableName(fieldName);
            }
        } else {
            throw new UnsupportedOperationException("Could not instantiate field with name: " + fieldName);
        }
    }

    private CompilationMetaInformation getOrCreateCompilationMetaInformation(Component component) {
        if (!compilationMetaInformation.containsKey(component)) {
            compilationMetaInformation.put(component, new CompilationMetaInformation());
        }
        return compilationMetaInformation.get(component);
    }

    private Object instantiateProjectComponentClassWithName(FieldDeclaration declaration) {
        try {
            CompilationUnit unit = getDesignerFileForField(declaration).get();
            Component component = new ComponentTreeParser(getDesignerFileForField(declaration).get(), project).getComponent();
            CompilationMetaInformation info = getOrCreateCompilationMetaInformation(component);
            info.setProjectComponent(true);
            info.setPackage(unit.getPackageDeclaration().get().getName().asString());
            info.setClassName(unit.getPrimaryTypeName().get());
            return component;
        } catch (ParseException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean isFieldProjectComponent(FieldDeclaration declaration) {
        return getDesignerFileForField(declaration).isPresent();
    }

    private Optional<CompilationUnit> getDesignerFileForField(FieldDeclaration declaration) {
        Collection<CompilationUnit> files = service.findDesignerClassFilesInPackage(project.getSourceFolder())
                .map(file -> DesignerFileHelper.getCompilationUnitFromDesignerFile(project.getSourceFolder().toPath(), file))
                .collect(Collectors.toList());
        for (CompilationUnit compilationUnit1 : files) {
            String projectClassName = compilationUnit1.getPrimaryType().get().getName().asString();
            String fieldClassName = declaration.getElementType().asString();
            if (projectClassName.equals(fieldClassName)) {
                return Optional.of(compilationUnit1);
            }
        }
        return Optional.empty();
    }

    private void processBodyStatement(Statement statement) throws ClassNotFoundException {
        if (statement instanceof ExpressionStmt) {
            Expression expression = ((ExpressionStmt) statement).getExpression();
            if (expression instanceof MethodCallExpr) {
                MethodCallExpr methodCallExpr = (MethodCallExpr) expression;
                Component relatedComponent = resolveComponent(methodCallExpr);
                invokeMethodOnComponent(methodCallExpr, relatedComponent);
            } else {
                throw new UnsupportedOperationException("Currently only MethodCallExpr are supported");
            }
        } else {
            throw new UnsupportedOperationException("Currently only ExpressionStmt are supported");
        }
    }

    private Component resolveComponent(MethodCallExpr methodCallExpr) {
        Expression variableExpression = methodCallExpr.getScope().get();
        if (variableExpression instanceof NameExpr) {
            String name = methodCallExpr.getScope().get().asNameExpr().getNameAsString();
            return fieldMap.get(name);
        } else if (variableExpression instanceof ThisExpr) {
            return rootComponent;
        } else {
            throw new UnsupportedOperationException("Currently only NameExpr and ThisExpr are supported");
        }
    }

    public Component getComponent() {
        return rootComponent;
    }

    public String getClassName() {
        return className;
    }

    public Map<Component, CompilationMetaInformation> getCompilationMetaInformation() {
        return compilationMetaInformation;
    }
}
