package com.github.appreciated.designer.application.view.file.designer.template;

import com.github.appreciated.designer.application.model.file.ProjectFileModel;
import com.github.appreciated.designer.application.view.BaseView;
import com.github.appreciated.designer.component.DesignerComponent;
import com.github.appreciated.designer.component.designer.DesignerComponentLabel;
import com.github.appreciated.designer.service.ComponentService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.util.List;
import java.util.Map;

public class DesignerComponentsView extends BaseView {
    private static final long serialVersionUID = 3391173526041012926L;

    private final TextField search;
    private final Accordion accordion;

    private final Map<String, VerticalLayout> componentsCategories;
    private final Map<String, List<DesignerComponentLabel>> components;
    private ProjectFileModel projectFileModel;


    public DesignerComponentsView(final ProjectFileModel projectFileModel) {
        super("components");
        this.projectFileModel = projectFileModel;

        componentsCategories = Maps.newHashMap();
        components = Maps.newHashMap();

        accordion = new Accordion();

        ComponentService service = new ComponentService();
        search = new TextField();
        search.setPlaceholder(getTranslation("search"));
        search.setWidthFull();
        search.setValueChangeMode(ValueChangeMode.EAGER);
        search.setPrefixComponent(VaadinIcon.SEARCH.create());
        search.setClearButtonVisible(true);
        search.addValueChangeListener(event -> initAccordions(event.getValue()));
        add(search);
        components.put(getTranslation("vaadin.components"), Lists.newArrayList());
        components.put(getTranslation("html.components"), Lists.newArrayList());
        service.getAllComponents().forEach(this::initDragAndDrop);
        components.keySet().forEach(key -> {
            AccordionPanel accordionPanel = new AccordionPanel();
            accordionPanel.setSummaryText(key);
            VerticalLayout wrapper = new VerticalLayout();
            wrapper.setPadding(false);
            accordionPanel.addContent(wrapper);
            componentsCategories.put(key, wrapper);
            accordion.add(accordionPanel);
        });
        initAccordions(null);
        add(accordion);
        accordion.setSizeFull();
    }

    private void initAccordions(String name) {
        componentsCategories.forEach((s, verticalLayout) -> verticalLayout.removeAll());
        components.forEach((key, value) -> value.stream()
                .filter(designerComponentLabel -> name == null || designerComponentLabel.getName().toLowerCase().contains(name.toLowerCase()))
                .forEach(designerComponentLabel -> componentsCategories.get(key).add(designerComponentLabel)));
    }

    private void initDragAndDrop(DesignerComponent component) {
        DesignerComponentLabel label = new DesignerComponentLabel(component);
        DragSource<DesignerComponentLabel> source = DragSource.create(label);
        source.addDragStartListener(e -> {
            projectFileModel.setCurrentDragItem(e.getComponent());
            projectFileModel.getEventService().getDesignerComponentDragEventPublisher().publish(e.getComponent(), true);
        });
        source.addDragEndListener(e -> {
            projectFileModel.removeCurrentDragItem();
            projectFileModel.getEventService().getDesignerComponentDragEventPublisher().publish(e.getComponent(), false);
        });
        if (component.getTagName().startsWith("<vaadin")) {
            components.get(getTranslation("vaadin.components")).add(label);
        } else {
            components.get(getTranslation("html.components")).add(label);
        }
    }
}