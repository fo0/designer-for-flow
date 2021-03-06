package com.github.appreciated.designer.application.events.save;

import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Component
@UIScope
public class SaveRequiredEventListener implements ApplicationListener<SaveRequiredEvent> {
    private final List<Consumer<SaveRequiredEvent>> consumerList = new ArrayList<>();

    @Override
    public void onApplicationEvent(SaveRequiredEvent event) {
        consumerList.forEach(elementFocusedEventConsumer -> elementFocusedEventConsumer.accept(event));
    }

    public void addEventConsumer(Consumer<SaveRequiredEvent> eventConsumer) {
        this.consumerList.add(eventConsumer);
    }

}