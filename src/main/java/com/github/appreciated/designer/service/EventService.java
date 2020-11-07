package com.github.appreciated.designer.service;

import com.github.appreciated.designer.events.designer.dnd.DesignerComponentDragEventListener;
import com.github.appreciated.designer.events.designer.dnd.DesignerComponentDragEventPublisher;
import com.github.appreciated.designer.events.designer.dnd.DesignerComponentDropEventListener;
import com.github.appreciated.designer.events.designer.dnd.DesignerComponentDropEventPublisher;
import com.github.appreciated.designer.events.designer.focus.ElementFocusedEventListener;
import com.github.appreciated.designer.events.designer.focus.ElementFocusedEventPublisher;
import com.github.appreciated.designer.events.designer.save.SaveRequiredEventListener;
import com.github.appreciated.designer.events.designer.save.SaveRequiredEventPublisher;
import com.github.appreciated.designer.events.designer.structure.StructureChangedEventListener;
import com.github.appreciated.designer.events.designer.structure.StructureChangedEventPublisher;
import com.github.appreciated.designer.events.designer.theme.ThemeChangedEventListener;
import com.github.appreciated.designer.events.designer.theme.ThemeChangedEventPublisher;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@UIScope
public class EventService {

    private final ElementFocusedEventPublisher focusedEventPublisher;
    private final StructureChangedEventPublisher structureChangedEventPublisher;
    private final ThemeChangedEventPublisher themeChangedEventPublisher;
    private final ElementFocusedEventListener focusedEventListener;
    private final StructureChangedEventListener structureChangedEventListener;
    private final ThemeChangedEventListener themeChangedEventListener;
    private final DesignerComponentDropEventListener designerComponentDropListener;
    private final DesignerComponentDropEventPublisher designerComponentDropEventPublisher;
    private final DesignerComponentDragEventListener designerComponentDragListener;
    private final DesignerComponentDragEventPublisher designerComponentDragEventPublisher;
    private final SaveRequiredEventListener saveRequiredEventListener;
    private final SaveRequiredEventPublisher saveRequiredEventPublisher;

    public EventService(@Autowired ElementFocusedEventPublisher focusedEventPublisher,
                        @Autowired ElementFocusedEventListener focusedEventListener,
                        @Autowired StructureChangedEventPublisher structureChangedEventPublisher,
                        @Autowired StructureChangedEventListener structureChangedEventListener,
                        @Autowired SaveRequiredEventPublisher saveRequiredEventPublisher,
                        @Autowired SaveRequiredEventListener saveRequiredEventListener,
                        @Autowired ThemeChangedEventPublisher themeChangedEventPublisher,
                        @Autowired ThemeChangedEventListener themeChangedEventListener,
                        @Autowired DesignerComponentDropEventListener designerComponentDropListener,
                        @Autowired DesignerComponentDropEventPublisher designerComponentDropEventPublisher,
                        @Autowired DesignerComponentDragEventListener designerComponentDragListener,
                        @Autowired DesignerComponentDragEventPublisher designerComponentDragEventPublisher
    ) {
        this.focusedEventPublisher = focusedEventPublisher;
        this.structureChangedEventPublisher = structureChangedEventPublisher;
        this.themeChangedEventPublisher = themeChangedEventPublisher;
        this.focusedEventListener = focusedEventListener;
        this.structureChangedEventListener = structureChangedEventListener;
        this.saveRequiredEventPublisher = saveRequiredEventPublisher;
        this.saveRequiredEventListener = saveRequiredEventListener;
        this.themeChangedEventListener = themeChangedEventListener;
        this.designerComponentDropListener = designerComponentDropListener;
        this.designerComponentDropEventPublisher = designerComponentDropEventPublisher;
        this.designerComponentDragListener = designerComponentDragListener;
        this.designerComponentDragEventPublisher = designerComponentDragEventPublisher;
    }

    public ThemeChangedEventListener getThemeChangedEventListener() {
        return themeChangedEventListener;
    }

    public ThemeChangedEventPublisher getThemeChangedEventPublisher() {
        return themeChangedEventPublisher;
    }

    public ElementFocusedEventPublisher getFocusedEventPublisher() {
        return focusedEventPublisher;
    }

    public StructureChangedEventPublisher getStructureChangedEventPublisher() {
        return structureChangedEventPublisher;
    }

    public ElementFocusedEventListener getFocusedEventListener() {
        return focusedEventListener;
    }

    public StructureChangedEventListener getStructureChangedEventListener() {
        return structureChangedEventListener;
    }

    public DesignerComponentDropEventListener getDesignerComponentDropListener() {
        return designerComponentDropListener;
    }

    public DesignerComponentDropEventPublisher getDesignerComponentDropEventPublisher() {
        return designerComponentDropEventPublisher;
    }

    public DesignerComponentDragEventListener getDesignerComponentDragListener() {
        return designerComponentDragListener;
    }

    public DesignerComponentDragEventPublisher getDesignerComponentDragEventPublisher() {
        return designerComponentDragEventPublisher;
    }

    public SaveRequiredEventListener getSaveRequiredEventListener() {
        return saveRequiredEventListener;
    }

    public SaveRequiredEventPublisher getSaveRequiredEventPublisher() {
        return saveRequiredEventPublisher;
    }
}
