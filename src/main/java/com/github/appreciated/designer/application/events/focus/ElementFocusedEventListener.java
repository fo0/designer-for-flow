package com.github.appreciated.designer.application.events.focus;

import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Component
@UIScope
public class ElementFocusedEventListener implements ApplicationListener<ElementFocusedEvent> {
    private final List<Consumer<ElementFocusedEvent>> consumerList = new ArrayList<>();

    @Override
    public void onApplicationEvent(ElementFocusedEvent event) {
        consumerList.forEach(elementFocusedEventConsumer -> elementFocusedEventConsumer.accept(event));
    }

    public void addEventConsumer(Consumer<ElementFocusedEvent> eventConsumer) {
        this.consumerList.add(eventConsumer);
    }
}