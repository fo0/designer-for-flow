package com.github.appreciated.designer.application.model;

import com.github.appreciated.designer.application.model.project.Project;
import com.github.appreciated.designer.helper.FieldNameHelper;
import com.github.appreciated.designer.theme.css.Theme;
import com.vaadin.flow.component.Component;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Configurable;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Configurable
@NoArgsConstructor
public class DesignCompilerInformation {

    private Project project;
    private File design;
    private Component component;
    private Theme theme;

    private String className;
    private Map<Component, CompilationMetaInformation> compilationMetaInformation = new HashMap<>();
    private List<String> components;

    public void setProject(Project project) {
        this.project = project;

        if (project.hasThemeFile()) {
            this.theme = new Theme(project);
        }
        components = FieldNameHelper.getComponents(project).collect(Collectors.toList());
    }

    public CompilationMetaInformation getCompilationMetaInformation(Component component) {
        if (hasCompilationMetaInformation(component)) {
            return compilationMetaInformation.get(component);
        } else {
            return null;
        }
    }

    public boolean hasCompilationMetaInformation(Component component) {
        return compilationMetaInformation.containsKey(component);
    }

    public CompilationMetaInformation getOrCreateCompilationMetaInformation(Component component) {
        if (!hasCompilationMetaInformation(component)) {
            compilationMetaInformation.put(component, new CompilationMetaInformation());
        }
        return compilationMetaInformation.get(component);
    }


    public boolean isVariableNameValid(String s, Component component) {
        return FieldNameHelper.isFieldNameValid(s, component, components.stream(), compilationMetaInformation);
    }
}