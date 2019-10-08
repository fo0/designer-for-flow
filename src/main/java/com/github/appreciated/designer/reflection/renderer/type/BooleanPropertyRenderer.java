package com.github.appreciated.designer.reflection.renderer.type;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

public class BooleanPropertyRenderer extends AbstractPropertyRenderer {
    @Override
    public boolean canRender(Component propertyParent, String propertyName, PropertyDescriptor propertyDescriptor) {
        return propertyDescriptor.getPropertyType() == Boolean.class || propertyDescriptor.getPropertyType().getName().equals("boolean");
    }

    public Component render(String propertyName, PropertyDescriptor propertyDescriptor, Component propertyParent) {
        Checkbox checkbox = new Checkbox();
        try {
            Boolean property = (Boolean) propertyDescriptor.getReadMethod().invoke(propertyParent);
            setValueButNull(checkbox, property);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        checkbox.addValueChangeListener(checkboxBooleanComponentValueChangeEvent -> applyValue(propertyParent, propertyDescriptor, checkbox.getValue()));
        return checkbox;
    }
}
