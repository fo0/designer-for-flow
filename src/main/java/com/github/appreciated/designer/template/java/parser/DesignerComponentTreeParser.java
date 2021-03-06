package com.github.appreciated.designer.template.java.parser;


import com.github.appreciated.designer.application.component.DesignerComponentWrapper;
import com.github.appreciated.designer.application.model.DesignCompilerInformation;
import com.github.appreciated.designer.application.model.project.Project;
import com.github.appreciated.designer.helper.ComponentContainerHelper;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.SourceRoot;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.tabs.Tab;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.appreciated.designer.helper.ComponentContainerHelper.*;

public class DesignerComponentTreeParser {

    private final File file;
    private Project project;
    private final ComponentTreeParser generator;

    public DesignerComponentTreeParser(File file, Project project) throws ParseException, ClassNotFoundException {
        this.file = file;
        this.project = project;
        SourceRoot sourceRoot = new SourceRoot(project.getProjectRoot().toPath());
        CompilationUnit compilationUnit = sourceRoot.parse(CodeGenerationUtils.packageToPath(file.getParent()), file.getName());
        generator = new ComponentTreeParser(compilationUnit, project);
    }

    public static Component wrap(Component component, DesignCompilerInformation information) {
        if (isComponentContainer(component, information) && !isProjectComponent(component, information)) {
            List<Component> children = ComponentContainerHelper.getChildren(component).collect(Collectors.toList());
            ComponentContainerHelper.removeAll(component);
            children.forEach(child -> {
                if (child instanceof AccordionPanel || child instanceof Tab) {
                    addComponent(component, child);
                } else {
                    if (component instanceof Scroller) {
                        // Workaround for Scroller.
                        ((Scroller) component).setContent(null);
                    }
                    Component wrapped = wrap(child, information);
                    addComponent(component, wrapped);
                }
            });
            return new DesignerComponentWrapper(component, isProjectComponent(component, information));
        }
        return new DesignerComponentWrapper(component, isProjectComponent(component, information));
    }

    public DesignCompilerInformation getRootDesignCompilerInformation() {
        DesignCompilerInformation info = new DesignCompilerInformation();
        info.setDesign(file);
        info.setProject(project);
        info.setCompilationMetaInformation(generator.getCompilationMetaInformation());
        info.setClassName(generator.getClassName());
        Component parsedComponent = generator.getComponent();
        DesignerComponentNormalizer.normalize(parsedComponent, info);
        info.setComponent(wrap(parsedComponent, info));
        return info;
    }

}