# Key Components

## Domain Layer

### BMDProject Entity

```java
@Entity
@Table(name = "bmd_projects")
public class BMDProject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    private String name;
    private String description;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DoseResponseExperiment> experiments;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<PrefilterResults> prefilterResults;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<BMDResult> bmdResults;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<CategoryAnalysisResults> categoryResults;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

**Key Relationships:**
- **One-to-Many** with all analysis result types
- **Many-to-One** with User for multi-tenancy
- **Cascade operations** for child entities

### DoseResponseExperiment Entity

```java
@Entity
@Table(name = "dose_response_experiments")
public class DoseResponseExperiment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private BMDProject project;

    private String name;

    @OneToMany(mappedBy = "experiment", cascade = CascadeType.ALL)
    private List<Treatment> treatments;

    @OneToMany(mappedBy = "experiment", cascade = CascadeType.ALL)
    private List<ProbeResponse> probeResponses;

    @ManyToOne
    @JoinColumn(name = "chip_info_id")
    private ChipInfo chip;

    @Enumerated(EnumType.STRING)
    private LogTransformationEnum logTransformation;

    // Large data stored in object storage
    @Column(name = "expression_matrix_s3_key")
    private String expressionMatrixS3Key;
}
```

**Design Decision:** Store large expression matrices in MinIO/S3, reference via key in database.

### BMDResult Entity

```java
@Entity
@Table(name = "bmd_results")
public class BMDResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private BMDProject project;

    @ManyToOne
    @JoinColumn(name = "experiment_id")
    private DoseResponseExperiment experiment;

    @ManyToOne
    @JoinColumn(name = "prefilter_id")
    private PrefilterResults prefilterResults;

    private String name;

    @Enumerated(EnumType.STRING)
    private BMDMethod bmdMethod; // BMDS, TOXICR_LAPLACE, TOXICR_MCMC, GCURVEP

    @OneToMany(mappedBy = "bmdResult", cascade = CascadeType.ALL)
    private List<ProbeStatResult> probeStatResults;

    @Column(name = "analysis_info", columnDefinition = "jsonb")
    @Type(JsonBinaryType.class)
    private AnalysisInfo analysisInfo;

    // Large result set stored in object storage
    @Column(name = "results_s3_key")
    private String resultsS3Key;
}
```

**Design Decision:** Use JSONB column for analysis parameters, S3 for large result sets.

## Service Layer

### BMDAnalysisService

**Interface:**
```java
public interface BMDAnalysisService {

    /**
     * Execute BMD analysis asynchronously
     * @return Job ID for tracking progress
     */
    String submitBMDAnalysis(
        Long projectId,
        Long dataId,
        BMDAnalysisRequest request,
        User user
    );

    /**
     * Execute BMD analysis synchronously (for testing)
     */
    BMDResult executeBMDAnalysis(
        IStatModelProcessable data,
        ModelInputParameters inputParams,
        ModelSelectionParameters modelSelectionParams,
        List<StatModel> modelsToRun
    );

    /**
     * Get analysis progress
     */
    JobProgress getAnalysisProgress(String jobId);
}
```

**Implementation Approach:**
1. Ported directly from desktop `BMDAnalysisService`
2. Added async wrapper methods
3. Progress callbacks publish Spring Events
4. Results saved to database + object storage

### JobService

**Interface:**
```java
public interface JobService {

    /**
     * Submit a job to the queue
     */
    String submitJob(JobRequest request, User user);

    /**
     * Get job status
     */
    JobStatus getJobStatus(String jobId);

    /**
     * Cancel a running job
     */
    void cancelJob(String jobId);

    /**
     * Get all jobs for a user
     */
    List<JobStatus> getUserJobs(Long userId);

    /**
     * Publish progress update
     */
    void publishProgress(String jobId, double progress, String message);
}
```

**Job Queue Implementation:**
- **Redis-based queue** using Spring Data Redis
- **Job entity** persisted in PostgreSQL
- **Status updates** published via WebSocket
- **Retry logic** for transient failures

## REST API Layer

### AnalysisController

```java
@RestController
@RequestMapping("/api/v1/analysis")
public class AnalysisController {

    @PostMapping("/bmd")
    public ResponseEntity<JobSubmissionResponse> submitBMDAnalysis(
        @RequestBody BMDAnalysisRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        String jobId = analysisService.submitBMDAnalysis(
            request.getProjectId(),
            request.getDataId(),
            request,
            getCurrentUser(userDetails)
        );
        return ResponseEntity.accepted()
            .body(new JobSubmissionResponse(jobId));
    }

    @PostMapping("/prefilter/anova")
    public ResponseEntity<JobSubmissionResponse> submitANOVA(
        @RequestBody ANOVARequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        // Similar pattern
    }

    @GetMapping("/{analysisId}/results")
    public ResponseEntity<BMDResultDTO> getResults(
        @PathVariable Long analysisId
    ) {
        // Return analysis results
    }
}
```

**API Design Principles:**
- **Async by default**: Return job ID immediately
- **RESTful resources**: `/api/v1/{resource}/{id}`
- **Hypermedia**: Include links to related resources
- **Versioning**: `/v1/` prefix for API evolution

## Vaadin UI Layer

### MainLayout

```java
@Route("")
@PageTitle("BMDExpress Web")
public class MainLayout extends AppLayout {

    private final ProjectService projectService;

    public MainLayout(ProjectService projectService) {
        this.projectService = projectService;
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("BMDExpress Web");
        logo.addClassName("logo");

        Button logout = new Button("Logout", e -> logout());

        HorizontalLayout header = new HorizontalLayout(logo, logout);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        addToNavbar(header);
    }

    private void createDrawer() {
        RouterLink projectsLink = new RouterLink("Projects", ProjectListView.class);
        RouterLink dataLink = new RouterLink("Data Import", DataImportView.class);
        RouterLink analysisLink = new RouterLink("Analysis", AnalysisView.class);

        addToDrawer(new VerticalLayout(
            projectsLink,
            dataLink,
            analysisLink
        ));
    }
}
```

### BMDAnalysisView

```java
@Route(value = "analysis/bmd", layout = MainLayout.class)
@PageTitle("BMD Analysis")
public class BMDAnalysisView extends VerticalLayout {

    private final BMDAnalysisService analysisService;
    private final JobService jobService;

    private ComboBox<IStatModelProcessable> dataSelector;
    private CheckBoxGroup<StatModel> modelSelector;
    private NumberField bmrField;
    private ComboBox<BMDMethod> methodSelector;
    private Button runButton;

    public BMDAnalysisView(
        BMDAnalysisService analysisService,
        JobService jobService
    ) {
        this.analysisService = analysisService;
        this.jobService = jobService;

        createUI();
    }

    private void createUI() {
        H2 title = new H2("BMD Analysis");

        // Data selection
        dataSelector = new ComboBox<>("Select Data");
        dataSelector.setItems(getAvailableData());
        dataSelector.setItemLabelGenerator(IStatModelProcessable::getName);

        // Model selection
        modelSelector = new CheckBoxGroup<>("Models to Run");
        modelSelector.setItems(StatModel.values());

        // BMR configuration
        bmrField = new NumberField("Benchmark Response (BMR)");
        bmrField.setValue(1.349);
        bmrField.setStep(0.1);

        // Method selection
        methodSelector = new ComboBox<>("Method");
        methodSelector.setItems(BMDMethod.values());
        methodSelector.setValue(BMDMethod.BMDS);

        // Run button
        runButton = new Button("Run Analysis", e -> runAnalysis());
        runButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Layout
        FormLayout form = new FormLayout();
        form.add(dataSelector, modelSelector, bmrField, methodSelector);

        add(title, form, runButton);
    }

    private void runAnalysis() {
        BMDAnalysisRequest request = new BMDAnalysisRequest();
        request.setDataId(dataSelector.getValue().getId());
        request.setModels(modelSelector.getSelectedItems());
        request.setBmr(bmrField.getValue());
        request.setMethod(methodSelector.getValue());

        String jobId = analysisService.submitBMDAnalysis(
            getCurrentProjectId(),
            request.getDataId(),
            request,
            getCurrentUser()
        );

        // Show progress dialog
        AnalysisProgressDialog dialog = new AnalysisProgressDialog(jobId, jobService);
        dialog.open();
    }
}
```

### Real-Time Progress Updates

```java
@Push
@Route(value = "analysis/progress", layout = MainLayout.class)
public class AnalysisProgressView extends VerticalLayout {

    private final JobService jobService;
    private final UI ui;

    private ProgressBar progressBar;
    private Span statusLabel;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public AnalysisProgressView(JobService jobService) {
        this.jobService = jobService;
        this.ui = UI.getCurrent();

        createUI();
        subscribeToProgress();
    }

    @EventListener
    public void onJobProgress(JobProgressEvent event) {
        // Update UI from background thread
        ui.access(() -> {
            progressBar.setValue(event.getProgress());
            statusLabel.setText(event.getMessage());
        });
    }
}
```

**Key Feature:** Use Vaadin `@Push` annotation for server-to-client updates via WebSocket.
