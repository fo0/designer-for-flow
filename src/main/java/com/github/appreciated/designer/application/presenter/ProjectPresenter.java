package com.github.appreciated.designer.application.presenter;

import com.github.appreciated.designer.Shortcuts;
import com.github.appreciated.designer.application.model.ProjectModel;
import com.github.appreciated.designer.application.presenter.file.ProjectFilePresenter;
import com.github.appreciated.designer.application.view.ErrorPageView;
import com.github.appreciated.designer.application.view.ProjectView;
import com.github.appreciated.designer.dialog.AddNewDesignTabDialog;
import com.github.appreciated.designer.model.DesignCompilerInformation;
import com.github.appreciated.designer.service.EventService;
import com.github.appreciated.designer.service.ExceptionService;
import com.github.appreciated.designer.service.ProjectService;
import com.github.appreciated.mvp.Presenter;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.*;
import com.vaadin.flow.shared.ui.Transport;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

@Route("project")
@StyleSheet("./styles/theme.css")
@Push(transport = Transport.LONG_POLLING)
public class ProjectPresenter extends Presenter<ProjectModel, ProjectView> implements HasUrlParameter<String> {

    private AddNewDesignTabDialog files;
    private String projectPath;
    private EventService eventService;
    private ProjectService projectService;

    public ProjectPresenter(@Autowired EventService eventService, @Autowired ProjectService projectService, @Autowired ExceptionService exceptionService) {
        UI.getCurrent().getSession().setErrorHandler(event -> {
            if (projectService.getConfig().getDeveloperMode()) {
                event.getThrowable().printStackTrace();
            }
            exceptionService.setError(event.getThrowable());
            UI.getCurrent().navigate(ErrorPageView.class);
        });
        this.eventService = eventService;
        this.projectService = projectService;

        getContent().getTabs().addSelectedChangeListener(selectedChangeEvent -> {
            getContent().getContent().setSelected(getContent().getTabs().getSelectedIndex());
        });

        getContent().getDial().addClickListener(event -> {
            files = new AddNewDesignTabDialog(projectService.getProject().getSourceFolder(), this::addTab);
            files.open();
        });
    }

    public void addTab(File file) {
        DesignCompilerInformation info = projectService.create(file);
        ProjectFilePresenter presenter = new ProjectFilePresenter(info, eventService);
        getContent().addTab(info.getDesign().getName(), tab -> {
            getContent().getContent().remove(presenter);
            getContent().getTabs().remove(tab);
        });
        getContent().getContent().add(presenter);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        new Shortcuts(attachEvent.getUI(), projectService, eventService);
        if (projectService.getConfig().getDeveloperMode()) {
            File defaultFile = new File("C:\\Users\\Johannes\\IdeaProjects\\designer-test-project\\src\\main\\java\\com\\github\\appreciated\\designer\\view\\TestDesign.java");
            if (defaultFile.exists()) {
                addTab(defaultFile);
            }
        }
    }


    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String parameter) {
        Location location = beforeEvent.getLocation();
        QueryParameters queryParameters = location.getQueryParameters();
        Map<String, List<String>> parametersMap = queryParameters.getParameters();
        projectPath = URLDecoder.decode(parametersMap.get("path").get(0));
        projectService.initProject(new File(projectPath));
    }

}
