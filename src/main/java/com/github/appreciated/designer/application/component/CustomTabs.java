package com.github.appreciated.designer.application.component;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.shared.Registration;

import java.util.Arrays;

public class CustomTabs extends Tabs {

    private final Tab addTab;
    private Runnable addListener;

    public CustomTabs() {
        setAutoselect(false);
        IconButton addButton = new IconButton(VaadinIcon.PLUS.create());
        addButton.getStyle()
                .set("padding", "0px")
                .set("font-size", "20px")
                .set("color", "black");
        addTab = new Tab(addButton);
        addTab.getStyle()
                .set("--lumo-primary-color", "transparent")
                .set("--lumo-contrast-60pct", "transparent");
        getStyle().set("padding-right", "0px");
        super.add(addTab);
    }

    @Override
    public void add(Tab... tabs) {
        Arrays.stream(tabs)
                .peek(tab1 -> tab1.getElement().getStyle().set("padding", "0px"))
                .forEach(currentTab -> addComponentAtIndex(getComponentCount() - 1, currentTab));
    }


    @Override
    public Registration addSelectedChangeListener(ComponentEventListener<SelectedChangeEvent> listener) {
        return super.addSelectedChangeListener(event -> {
            if (event.getSelectedTab() == addTab) {
                this.addListener.run();
                if (event.getPreviousTab() != null && getChildren().anyMatch(component -> component == event.getPreviousTab())) {
                    setSelectedTab(event.getPreviousTab());
                } else {
                    addTab.setSelected(false);
                }
            } else {
                listener.onComponentEvent(event);
            }
        });
    }

    public void addAddListener(Runnable addListener) {
        this.addListener = addListener;
    }

    public Tab getAddTab() {
        return addTab;
    }
}
