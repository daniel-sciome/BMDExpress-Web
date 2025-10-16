package com.sciome.bmdexpressweb.controller;

import com.sciome.bmdexpressweb.dto.CategoryAnalysisTableView;
import com.sciome.bmdexpressweb.dto.ErrorResponse;
import com.sciome.bmdexpressweb.dto.ProjectUploadResponse;
import com.sciome.bmdexpressweb.service.BmdResultsService;
import com.sciome.bmdexpressweb.service.CategoryResultsService;
import com.sciome.bmdexpressweb.service.ProjectService;
import com.sciome.bmdexpress2.mvp.model.BMDProject;
import com.sciome.bmdexpress2.mvp.model.stat.BMDResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for BMDExpress project management
 *
 * Provides endpoints for uploading, loading, and managing .bm2 project files.
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    @Value("${bmdexpress.projects.dir:data/projects}")
    private String projectFilesDir;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private BmdResultsService bmdResultsService;

    @Autowired
    private CategoryResultsService categoryResultsService;

    /**
     * Upload a .bm2 project file
     *
     * POST /api/projects
     * Content-Type: multipart/form-data
     * Body: file=<.bm2 file>
     *
     * @param file The .bm2 file
     * @return Project upload response with project ID
     */
    @PostMapping
    public ResponseEntity<ProjectUploadResponse> uploadProject(
            @RequestParam("file") MultipartFile file) throws Exception {

        try {
            logger.info("Uploading project file: {}", file.getOriginalFilename());

            String projectId = projectService.loadProject(
                    file.getInputStream(),
                    file.getOriginalFilename());

            ProjectService.ProjectHolder holder = projectService.getProjectHolder(projectId);
            BMDProject project = holder.getProject();

            List<String> bmdResultNames = project.getbMDResult().stream()
                    .map(r -> r.getName())
                    .collect(Collectors.toList());

            List<String> categoryResultNames = project.getCategoryAnalysisResults().stream()
                    .map(r -> r.getName())
                    .collect(Collectors.toList());

            ProjectUploadResponse response = new ProjectUploadResponse(
                    projectId,
                    project.getName(),
                    holder.getUploadedAt(),
                    bmdResultNames,
                    categoryResultNames,
                    project.getDoseResponseExperiments().size()
            );

            logger.info("Project uploaded successfully: {} (ID: {})", file.getOriginalFilename(), projectId);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Failed to upload project", e);
            throw new RuntimeException("Failed to upload project: " + e.getMessage(), e);
        }
    }

    /**
     * Get project metadata
     *
     * GET /api/projects/{projectId}
     *
     * @param projectId The project ID
     * @return Project metadata
     */
    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectUploadResponse> getProject(@PathVariable String projectId) throws Exception {
        ProjectService.ProjectHolder holder = projectService.getProjectHolder(projectId);
        BMDProject project = holder.getProject();

        List<String> bmdResultNames = project.getbMDResult().stream()
                .map(r -> r.getName())
                .collect(Collectors.toList());

        List<String> categoryResultNames = project.getCategoryAnalysisResults().stream()
                .map(r -> r.getName())
                .collect(Collectors.toList());

        ProjectUploadResponse response = new ProjectUploadResponse(
                projectId,
                project.getName(),
                holder.getUploadedAt(),
                bmdResultNames,
                categoryResultNames,
                project.getDoseResponseExperiments().size()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get list of BMD result names in a project
     *
     * GET /api/projects/{projectId}/bmd-results
     *
     * @param projectId The project ID
     * @return List of BMD result names
     */
    @GetMapping("/{projectId}/bmd-results")
    public ResponseEntity<List<String>> getBmdResults(@PathVariable String projectId) {
        List<String> bmdResultNames = bmdResultsService.getBmdResultNames(projectId);
        return ResponseEntity.ok(bmdResultNames);
    }

    /**
     * Get a specific BMD result from a project
     *
     * GET /api/projects/{projectId}/bmd-results/{resultName}
     *
     * @param projectId The project ID
     * @param resultName The BMD result name
     * @return The BMD result
     */
    @GetMapping("/{projectId}/bmd-results/{resultName}")
    public ResponseEntity<?> getBmdResult(
            @PathVariable String projectId,
            @PathVariable String resultName) {

        try {
            BMDResult bmdResult = bmdResultsService.findBmdResult(projectId, resultName);

            // Ensure row data and column headers are generated for JSON serialization
            try {
                bmdResult.getColumnHeader(); // This populates the transient columnHeader field
                bmdResult.generateRowData(); // This populates the row data
            } catch (NullPointerException e) {
                // Some BMDResult objects may not have complete data for row generation
                // This is okay - we'll just return the object as-is
                logger.debug("Unable to generate row data for BMDResult: {}", resultName);
            }

            return ResponseEntity.ok(bmdResult);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get list of category analysis result names in a project
     *
     * GET /api/projects/{projectId}/category-results
     *
     * @param projectId The project ID
     * @return List of category analysis result names
     */
    @GetMapping("/{projectId}/category-results")
    public ResponseEntity<List<String>> getCategoryResults(@PathVariable String projectId) {
        List<String> categoryResultNames = categoryResultsService.getCategoryResultNames(projectId);
        return ResponseEntity.ok(categoryResultNames);
    }

    /**
     * Get the full BMDProject object
     *
     * GET /api/projects/{projectId}/full
     *
     * @param projectId The project ID
     * @return The complete BMDProject object
     */
    @GetMapping("/{projectId}/full")
    public ResponseEntity<BMDProject> getFullProject(@PathVariable String projectId) {
        try {
            ProjectService.ProjectHolder holder = projectService.getProjectHolder(projectId);
            BMDProject project = holder.getProject();
            return ResponseEntity.ok(project);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get a specific category analysis result from a project
     *
     * GET /api/projects/{projectId}/category-results/{resultName}
     *
     * @param projectId The project ID
     * @param resultName The category result name
     * @return The category analysis results
     */
    @GetMapping("/{projectId}/category-results/{resultName}")
    public ResponseEntity<?> getCategoryResult(
            @PathVariable String projectId,
            @PathVariable String resultName) {

        try {
            ProjectService.ProjectHolder holder = projectService.getProjectHolder(projectId);
            BMDProject project = holder.getProject();

            // Find the matching category result
            var categoryResult = project.getCategoryAnalysisResults().stream()
                    .filter(r -> r.getName().equals(resultName))
                    .findFirst()
                    .orElse(null);

            if (categoryResult == null) {
                return ResponseEntity.notFound().build();
            }

            // Ensure row data and column headers are generated for JSON serialization
            List<String> columnHeader = categoryResult.getColumnHeader(); // This populates the transient columnHeader field
            categoryResult.generateRowData(); // This populates the row data

            logger.debug("Column header size: {}", columnHeader != null ? columnHeader.size() : "null");
            logger.debug("Column header: {}", columnHeader);
            logger.debug("Category results size: {}", categoryResult.getCategoryAnalsyisResults() != null ? categoryResult.getCategoryAnalsyisResults().size() : "null");

            // Convert to table view with explicit columnHeader field
            List<Map<String, Object>> rowData = new java.util.ArrayList<>();
            if (categoryResult.getCategoryAnalsyisResults() != null) {
                for (var result : categoryResult.getCategoryAnalsyisResults()) {
                    Map<String, Object> rowMap = new java.util.HashMap<>();
                    rowMap.put("row", result.getRow()); // CategoryAnalysisResult has getRow() method
                    rowData.add(rowMap);
                }
            }

            CategoryAnalysisTableView tableView = new CategoryAnalysisTableView(
                categoryResult.getName(),
                columnHeader,
                rowData
            );

            return ResponseEntity.ok(tableView);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete a project
     *
     * DELETE /api/projects/{projectId}
     *
     * @param projectId The project ID
     * @return No content
     */
    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable String projectId) {

        if (!projectService.projectExists(projectId)) {
            return ResponseEntity.notFound().build();
        }

        projectService.deleteProject(projectId);
        logger.info("Project deleted: {}", projectId);

        return ResponseEntity.noContent().build();
    }

    /**
     * List available .bm2 files in the server directory
     *
     * GET /api/projects/available-files
     *
     * @return List of available .bm2 filenames
     */
    @GetMapping("/available-files")
    public ResponseEntity<?> listAvailableFiles() {
        try {
            Path projectDir = Paths.get(projectFilesDir);

            if (!Files.exists(projectDir)) {
                logger.warn("Project directory does not exist: {}", projectFilesDir);
                return ResponseEntity.ok(List.of());
            }

            List<String> bm2Files = Files.list(projectDir)
                    .filter(path -> path.toString().endsWith(".bm2"))
                    .map(path -> path.getFileName().toString())
                    .sorted()
                    .collect(Collectors.toList());

            logger.info("Found {} .bm2 files in {}", bm2Files.size(), projectFilesDir);

            return ResponseEntity.ok(bm2Files);

        } catch (IOException e) {
            logger.error("Failed to list project files", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to list files: " + e.getMessage()));
        }
    }

    /**
     * Load a .bm2 project file from server filesystem
     *
     * POST /api/projects/load-from-file
     *
     * @param request Map with "filename" key
     * @return Project upload response with project ID
     */
    @PostMapping("/load-from-file")
    public ResponseEntity<?> loadProjectFromFile(@RequestBody Map<String, String> request) {

        try {
            String filename = request.get("filename");
            if (filename == null || filename.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse(400, "Bad Request", "No filename provided", "/api/projects/load-from-file"));
            }

            // Security: prevent directory traversal
            if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(403, "Forbidden", "Invalid filename", "/api/projects/load-from-file"));
            }

            String filepath = projectFilesDir + "/" + filename;
            File file = new File(filepath);

            if (!file.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("File not found: " + filename));
            }

            if (!file.canRead()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Cannot read file: " + filename));
            }

            logger.info("Loading project from file: {}", filepath);

            String projectId = projectService.loadProject(
                    new FileInputStream(file),
                    file.getName());

            ProjectService.ProjectHolder holder = projectService.getProjectHolder(projectId);
            BMDProject project = holder.getProject();

            List<String> bmdResultNames = project.getbMDResult().stream()
                    .map(r -> r.getName())
                    .collect(Collectors.toList());

            List<String> categoryResultNames = project.getCategoryAnalysisResults().stream()
                    .map(r -> r.getName())
                    .collect(Collectors.toList());

            ProjectUploadResponse response = new ProjectUploadResponse(
                    projectId,
                    project.getName(),
                    holder.getUploadedAt(),
                    bmdResultNames,
                    categoryResultNames,
                    project.getDoseResponseExperiments().size()
            );

            logger.info("Project loaded successfully: {} (ID: {})", filename, projectId);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IOException e) {
            logger.error("Failed to load project from file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to read file: " + e.getMessage()));
        } catch (ClassNotFoundException e) {
            logger.error("Failed to deserialize project", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid project file format"));
        } catch (Exception e) {
            logger.error("Unexpected error loading project", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to load project: " + e.getMessage()));
        }
    }
}
