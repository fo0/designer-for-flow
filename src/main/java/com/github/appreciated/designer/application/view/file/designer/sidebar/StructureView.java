package com.github.appreciated.designer.application.view.file.designer.sidebar;

import com.github.appreciated.designer.application.model.file.ProjectFileModel;
import com.github.appreciated.designer.application.view.BaseView;
import com.github.appreciated.designer.component.DesignerComponentWrapper;
import com.github.appreciated.designer.helper.ComponentContainerHelper;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.treegrid.TreeGrid;
import lombok.extern.log4j.Log4j2;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public class StructureView extends BaseView {
    private static final long serialVersionUID = -878124261779595119L;

    // Data
    private final TreeGrid<Component> grid;

    public StructureView(final ProjectFileModel projectFileModel) {
        super("structure");

        grid = new TreeGrid<>();
        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_NO_BORDER);
        grid.addHierarchyColumn(component -> component.getClass().getSimpleName());
        grid.setSizeFull();
        grid.setRowsDraggable(true);
        grid.setDropMode(GridDropMode.ON_TOP_OR_BETWEEN);
        grid.setDragFilter(component -> component != projectFileModel.getInformation().getComponent());
        grid.addDragStartListener(dragStartEvent -> {
            Component draggedItem = dragStartEvent.getDraggedItems().stream().findFirst().get().getParent().get();
            projectFileModel.setCurrentDragItem(draggedItem);
            projectFileModel.getEventService().getDesignerComponentDragEventPublisher().publish(draggedItem, true);
        });
        grid.addDragEndListener(event -> {
            projectFileModel.removeCurrentDragItem();
            projectFileModel.getEventService().getDesignerComponentDragEventPublisher().publish(null, false);
        });
        //  Actual Parent Layout
        //      |
        //  DesignerComponentWrapper
        //      |
        //  Component

        grid.addDropListener(dropEvent -> {
            final GridDropLocation name = dropEvent.getDropLocation();
            if (dropEvent.getDropTargetItem().isPresent() && projectFileModel.getCurrentDragItem() != dropEvent.getDropTargetItem().get()) {
                final Component dropLocation = dropEvent.getDropTargetItem().get();
                switch (name) {
                    case BELOW:
                    case ABOVE:
                        projectFileModel.getEventService()
                                .getDesignerComponentDropEventPublisher()
                                .publish(projectFileModel.getCurrentDragItem(), dropLocation.getParent().get());
                        break;
                    case ON_TOP:
                        List<Component> containedChildren = dropLocation.getChildren().collect(Collectors.toList());
                        projectFileModel.getEventService()
                                .getDesignerComponentDropEventPublisher()
                                .publish(projectFileModel.getCurrentDragItem(), containedChildren.get(containedChildren.size() - 1));
                        break;
                    default:
                        break;
                }
            } else {
                Notification.show(getTranslation("elements.cannot.be.dropped.on.themself"));
            }
        });
        grid.addItemClickListener(event -> projectFileModel.getEventService().getFocusedEventPublisher().publish(event.getItem()));
        add(grid);
        projectFileModel.getEventService().getStructureChangedEventListener().addEventConsumer(event -> {
            if (event.getComponent() == projectFileModel.getInformation().getComponent()) {
                updateStructure(event.getComponent());
            }
        });
        projectFileModel.getEventService().getFocusedEventListener().addEventConsumer(elementFocusedEvent -> {
            uiAccess(() -> {
                grid.select(elementFocusedEvent.getFocus());
            });
        });
        updateStructure(projectFileModel.getInformation().getComponent());
    }

    private void updateStructure(Component designRootComponent) {
        if (designRootComponent != null) {
            uiAccess(() -> {
                grid.setItems(Collections.singletonList(unpack(designRootComponent)), component -> {
                    if (ComponentContainerHelper.isComponentContainer(component)) {
                        return unpack(ComponentContainerHelper.getChildren(component)).collect(Collectors.toList());
                    } else {
                        return Collections.emptyList();
                    }
                });
                grid.expandRecursively(Stream.of(unpack(designRootComponent)), 30);
                grid.getDataProvider().refreshAll();
            });
        } else {
            grid.setItems(Collections.emptyList());
        }
    }

    private Component unpack(Component designRootComponent) {
        if (designRootComponent instanceof DesignerComponentWrapper) {
            return ((DesignerComponentWrapper) designRootComponent).getActualComponent();
        } else {
            return designRootComponent;
        }
    }

    private Stream<Component> unpack(Stream<Component> children) {
        return children.map(this::unpack);
    }
}