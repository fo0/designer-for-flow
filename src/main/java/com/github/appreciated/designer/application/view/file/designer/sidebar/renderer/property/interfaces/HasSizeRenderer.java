package com.github.appreciated.designer.application.view.file.designer.sidebar.renderer.property.interfaces;

import com.github.appreciated.designer.application.component.SmallIconButton;
import com.github.appreciated.designer.application.component.properties.PropertyTextField;
import com.github.appreciated.designer.application.view.file.designer.sidebar.renderer.AbstractComponentPropertyRenderer;
import com.github.appreciated.designer.application.view.file.designer.sidebar.renderer.RenderPair;
import com.github.appreciated.designer.regex.CssRegex;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.RegexpValidator;

import java.util.stream.Stream;

public class HasSizeRenderer extends AbstractComponentPropertyRenderer<HasSize> {


    @Override
    public boolean canRender(Component propertyParent) {
        return propertyParent instanceof HasSize;
    }

    @Override
    public Stream<RenderPair> render(HasSize component) {
        Component parent = ((Component) component).getParent().orElse(null);
        if (parent != null) {
            Binder<HasSize> componentBinder = new Binder<>();
            componentBinder.setBean((HasSize) parent);

            PropertyTextField heightComponent = new PropertyTextField();
            heightComponent.setSuffixComponent(
                    new SmallIconButton(VaadinIcon.ARROWS_LONG_V.create(),
                            buttonClickEvent -> heightComponent.setValue("100%"))
            );
            heightComponent.setClearButtonVisible(true);
            componentBinder.forField(heightComponent)
                    .withValidator(new RegexpValidator("Not a valid css length", CssRegex.getLengthRegex()))
                    .bind(HasSize::getHeight, HasSize::setHeight);

            componentBinder.setBean((HasSize) parent);
            PropertyTextField widthComponent = new PropertyTextField();
            widthComponent.setClearButtonVisible(true);
            widthComponent.setSuffixComponent(
                    new SmallIconButton(VaadinIcon.ARROWS_LONG_H.create(),
                            buttonClickEvent -> widthComponent.setValue("100%"))
            );
            componentBinder.forField(widthComponent)
                    .withValidator(new RegexpValidator("Not a valid css length", CssRegex.getLengthRegex()))
                    .bind(HasSize::getWidth, HasSize::setWidth);
            return Stream.of(
                    new RenderPair("height", heightComponent),
                    new RenderPair("width", widthComponent)
            );
        } else {
            return Stream.empty();
        }
    }

    @Override
    public Stream<String> rendersCssStyle() {
        return Stream.of("height", "width");
    }

}
