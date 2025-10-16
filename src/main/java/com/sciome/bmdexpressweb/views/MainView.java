package com.sciome.bmdexpressweb.views;

import com.sciome.bmdexpressweb.dto.ProjectUploadResponse;
import com.sciome.bmdexpressweb.mvp.presenter.mainstage.ProjectNavigationPresenter;
import com.sciome.bmdexpressweb.service.BmdExpressApiService;
import com.sciome.bmdexpressweb.shared.eventbus.BMDExpressEventBus;
import com.sciome.bmdexpressweb.views.dataview.CategoryAnalysisDataView;
import com.sciome.bmdexpressweb.views.mainstage.ProjectNavigationView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.util.Map;

/**
 * MainView - Clean Vaadin implementation with menu bar and modal upload dialog.
 * Primary view for BMDExpress web application using MVP architecture.
 */
@Route("")
public class MainView extends VerticalLayout {

    private final BmdExpressApiService apiService;
    private final BMDExpressEventBus eventBus;

    // UI Components
    private H1 projectNameLabel;
    private Span currentSelectionLabel;
    private Span actionStatusLabel;
    private SplitLayout contentArea;
    private MenuBar menuBar;

    // MVP Components
    private ProjectNavigationView projectNavigationView;
    private ProjectNavigationPresenter projectNavigationPresenter;
    private VerticalLayout dataViewArea;
    private CategoryAnalysisDataView categoryAnalysisDataView;

    // Upload dialog
    private Dialog uploadDialog;

    // Current project ID for fetching category results
    private String currentProjectId;

    @Autowired
    public MainView(BmdExpressApiService apiService, BMDExpressEventBus eventBus) {
        this.apiService = apiService;
        this.eventBus = eventBus;

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        initializeMenuBar();
        initializeHeader();
        initializeMVPComponents();
        initializeContentArea();
        initializeUploadDialog();
    }

    /**
     * Initializes the MVP components (view and presenter)
     */
    private void initializeMVPComponents() {
        // Create view
        projectNavigationView = new ProjectNavigationView();

        // Create presenter (wires up EventBus subscriptions)
        projectNavigationPresenter = new ProjectNavigationPresenter(projectNavigationView, eventBus);

        // Create category analysis data view
        categoryAnalysisDataView = new CategoryAnalysisDataView();

        // Wire up selection listener
        projectNavigationView.addSelectionListener(this::handleCategorySelection);

        System.out.println("MainView: MVP components initialized");
    }

    /**
     * Creates the menu bar (File, Analysis, Help menus)
     */
    private void initializeMenuBar() {
        menuBar = new MenuBar();
        menuBar.setWidthFull();

        // File Menu
        MenuItem fileMenu = menuBar.addItem("File");
        SubMenu fileSubMenu = fileMenu.getSubMenu();
        fileSubMenu.addItem("Open Project", e -> showUploadDialog());
        fileSubMenu.addItem("Close Project", e -> closeProject());
        fileSubMenu.addItem("Save Project", e -> saveProject());
        fileSubMenu.addItem("Save As...", e -> saveProjectAs());
        fileSubMenu.addItem("Export as JSON", e -> exportAsJSON());
        fileSubMenu.addItem("Exit", e -> exitApplication());

        // Analysis Menu (placeholder for now)
        MenuItem analysisMenu = menuBar.addItem("Analysis");
        SubMenu analysisSubMenu = analysisMenu.getSubMenu();
        analysisSubMenu.addItem("One-way ANOVA", e -> performOneWayANOVA());
        analysisSubMenu.addItem("Williams Trend", e -> performWilliamsTrend());
        analysisSubMenu.addItem("BMD Analysis", e -> performBMDAnalysis());
        analysisSubMenu.addItem("GO Analysis", e -> performGOAnalysis());
        analysisSubMenu.addItem("Pathway Analysis", e -> performPathwayAnalysis());

        // Help Menu
        MenuItem helpMenu = menuBar.addItem("Help");
        SubMenu helpSubMenu = helpMenu.getSubMenu();
        helpSubMenu.addItem("Tutorial", e -> openTutorial());
        helpSubMenu.addItem("About", e -> showAbout());
        helpSubMenu.addItem("License", e -> showLicense());

        add(menuBar);
    }

    /**
     * Creates the header section with project name and status labels
     */
    private void initializeHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setPadding(true);
        header.setSpacing(true);
        header.getStyle().set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

        // Project name
        projectNameLabel = new H1("BMDExpress Web");
        projectNameLabel.getStyle().set("margin", "0");
        projectNameLabel.getStyle().set("font-size", "1.5rem");

        // Status area
        VerticalLayout statusArea = new VerticalLayout();
        statusArea.setSpacing(false);
        statusArea.setPadding(false);

        currentSelectionLabel = new Span("No dataset selected");
        currentSelectionLabel.getStyle().set("font-size", "0.875rem");
        currentSelectionLabel.getStyle().set("color", "var(--lumo-secondary-text-color)");

        actionStatusLabel = new Span("");
        actionStatusLabel.getStyle().set("font-size", "0.875rem");
        actionStatusLabel.getStyle().set("color", "var(--lumo-success-color)");

        statusArea.add(currentSelectionLabel, actionStatusLabel);

        header.add(projectNameLabel, statusArea);
        add(header);
    }

    /**
     * Creates the main content area with split layout (navigation tree on left, data view on right)
     */
    private void initializeContentArea() {
        // Create SplitLayout for tree and data view
        contentArea = new SplitLayout();
        contentArea.setSizeFull();
        contentArea.setOrientation(SplitLayout.Orientation.HORIZONTAL);
        contentArea.setSplitterPosition(25); // 25% for tree, 75% for data view

        // Left side: Project navigation tree
        projectNavigationView.setWidth("100%");
        projectNavigationView.setHeight("100%");
        contentArea.addToPrimary(projectNavigationView);

        // Right side: Data view area (placeholder for now)
        dataViewArea = new VerticalLayout();
        dataViewArea.setSizeFull();
        dataViewArea.setPadding(true);
        dataViewArea.setSpacing(true);
        dataViewArea.setAlignItems(Alignment.CENTER);
        dataViewArea.setJustifyContentMode(JustifyContentMode.CENTER);

        H3 welcomeMessage = new H3("Welcome to BMDExpress Web");
        Paragraph instructions = new Paragraph(
            "Use File > Open Project to upload a BMDExpress project file (.bm2) and begin analysis."
        );

        dataViewArea.add(welcomeMessage, instructions);
        contentArea.addToSecondary(dataViewArea);

        add(contentArea);
        expand(contentArea); // Makes contentArea take all available space
    }

    /**
     * Creates the modal upload dialog (shown when File > Open Project is clicked)
     */
    private void initializeUploadDialog() {
        uploadDialog = new Dialog();
        uploadDialog.setCloseOnEsc(true);
        uploadDialog.setCloseOnOutsideClick(false);
        uploadDialog.setWidth("500px");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);

        H3 title = new H3("Open BMDExpress Project");
        title.getStyle().set("margin-top", "0");

        Paragraph description = new Paragraph(
            "Select a BMDExpress project file (.bm2) to upload. " +
            "The project will be loaded and displayed in the tree view."
        );
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        // Upload component with memory buffer (no drop zone)
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes(".bm2");
        upload.setMaxFiles(1);
        upload.setMaxFileSize(100 * 1024 * 1024); // 100MB max
        upload.setDropAllowed(false); // Remove drop zone
        upload.setUploadButton(new Button("Choose File"));

        upload.addSucceededListener(event -> {
            try {
                String fileName = event.getFileName();
                InputStream inputStream = buffer.getInputStream();

                // Upload file to server via REST API
                uploadProjectFile(fileName, inputStream);

                // Close dialog
                uploadDialog.close();

                // Show success notification
                showSuccessNotification("Project uploaded successfully: " + fileName);
                updateProjectLabel(fileName);

            } catch (Exception e) {
                showErrorNotification("Failed to upload project: " + e.getMessage());
                e.printStackTrace();
            }
        });

        upload.addFileRejectedListener(event -> {
            showErrorNotification("File rejected: " + event.getErrorMessage());
        });

        // Buttons
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setWidthFull();
        buttons.setJustifyContentMode(JustifyContentMode.END);

        Button cancelButton = new Button("Cancel", e -> uploadDialog.close());

        buttons.add(cancelButton);

        dialogLayout.add(title, description, upload, buttons);
        uploadDialog.add(dialogLayout);
    }

    /**
     * Shows the upload dialog
     */
    private void showUploadDialog() {
        uploadDialog.open();
    }

    /**
     * Uploads the project file to the bmdexpress-server via REST API
     */
    private void uploadProjectFile(String fileName, InputStream inputStream) {
        try {
            // Save to temp file first
            java.io.File tempFile = java.io.File.createTempFile("bmd_upload_", ".bm2");
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile)) {
                inputStream.transferTo(fos);
            }

            // Upload to backend API using API service
            ProjectUploadResponse response = apiService.uploadProject(tempFile);
            tempFile.delete();

            if (response != null && response.getProjectId() != null) {
                System.out.println("Project uploaded successfully: " + response.getProjectId());
                System.out.println("Project name: " + response.getName());
                System.out.println("Category results: " + response.getCategoryResultNames());

                // Save current project ID
                currentProjectId = response.getProjectId();

                // Update action status
                updateActionStatus("Project loaded: " + response.getName());

                // Populate project navigation tree via presenter
                projectNavigationPresenter.populateTreeFromResponse(response);

            } else {
                throw new RuntimeException("Invalid response from API");
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload project to server: " + e.getMessage(), e);
        }
    }

    // ===========================
    // Menu Action Handlers
    // ===========================

    private void closeProject() {
        // TODO: Implement close project logic
        showInfoNotification("Close project - not yet implemented");
    }

    private void saveProject() {
        // TODO: Implement save project logic
        showInfoNotification("Save project - not yet implemented");
    }

    private void saveProjectAs() {
        // TODO: Implement save as logic
        showInfoNotification("Save project as - not yet implemented");
    }

    private void exportAsJSON() {
        // TODO: Implement JSON export logic
        showInfoNotification("Export as JSON - not yet implemented");
    }

    private void exitApplication() {
        // TODO: Implement exit logic (maybe just close browser tab?)
        showInfoNotification("Exit - close browser tab");
    }

    private void performOneWayANOVA() {
        showInfoNotification("One-way ANOVA - not yet implemented");
    }

    private void performWilliamsTrend() {
        showInfoNotification("Williams Trend - not yet implemented");
    }

    private void performBMDAnalysis() {
        showInfoNotification("BMD Analysis - not yet implemented");
    }

    private void performGOAnalysis() {
        showInfoNotification("GO Analysis - not yet implemented");
    }

    private void performPathwayAnalysis() {
        showInfoNotification("Pathway Analysis - not yet implemented");
    }

    private void openTutorial() {
        getUI().ifPresent(ui -> ui.getPage().open("https://github.com/auerbachs/BMDExpress-3", "_blank"));
    }

    private void showAbout() {
        Dialog aboutDialog = new Dialog();
        VerticalLayout layout = new VerticalLayout();
        layout.add(new H3("BMDExpress Web"));
        layout.add(new Paragraph("Version 3.7.0"));
        layout.add(new Paragraph("A web-based interface for BMDExpress analysis"));
        Button closeButton = new Button("Close", e -> aboutDialog.close());
        layout.add(closeButton);
        aboutDialog.add(layout);
        aboutDialog.open();
    }

    private void showLicense() {
        showInfoNotification("License dialog - not yet implemented");
    }

    // ===========================
    // UI Update Methods
    // ===========================

    /**
     * Updates the project name label
     */
    public void updateProjectLabel(String projectName) {
        projectNameLabel.setText("BMDExpress Web - " + projectName);
    }

    /**
     * Updates the current selection label
     */
    public void updateSelectionLabel(String selection) {
        currentSelectionLabel.setText(selection);
    }

    /**
     * Updates the action status label (temporarily, auto-hides after 5 seconds)
     */
    public void updateActionStatus(String status) {
        actionStatusLabel.setText(status);

        // Auto-hide after 5 seconds
        getUI().ifPresent(ui -> {
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    ui.access(() -> actionStatusLabel.setText(""));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    // ===========================
    // Notification Helpers
    // ===========================

    private void showSuccessNotification(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showErrorNotification(String message) {
        Notification notification = Notification.show(message, 5000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void showInfoNotification(String message) {
        Notification.show(message, 3000, Notification.Position.BOTTOM_START);
    }

    // ===========================
    // Category Selection Handler
    // ===========================

    /**
     * Handles category result selection from the navigation tree
     */
    private void handleCategorySelection(String categoryResultName) {
        if (currentProjectId == null) {
            showErrorNotification("No project loaded");
            return;
        }

        try {
            System.out.println("Loading category result: " + categoryResultName + " from project: " + currentProjectId);

            // Fetch category result data from backend
            Map<String, Object> categoryResult = apiService.getCategoryResult(currentProjectId, categoryResultName);

            if (categoryResult == null) {
                showErrorNotification("Failed to load category result: " + categoryResultName);
                return;
            }

            // Update the data view area with category analysis view
            dataViewArea.removeAll();
            dataViewArea.add(categoryAnalysisDataView);
            dataViewArea.setAlignItems(Alignment.STRETCH);
            dataViewArea.setJustifyContentMode(JustifyContentMode.START);

            // Load data into the view
            categoryAnalysisDataView.loadCategoryData(categoryResult);

            // Update selection label
            updateSelectionLabel("Category Analysis: " + categoryResultName);

            System.out.println("Category result loaded successfully");

        } catch (Exception e) {
            showErrorNotification("Failed to load category result: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

