package com.sciome.bmdexpressweb.controller;

import com.sciome.bmdexpressweb.dto.CategoryAnalysisRequest;
import com.sciome.bmdexpressweb.dto.CategoryAnalysisResponse;
import com.sciome.bmdexpressweb.service.BmdResultsService;
import com.sciome.bmdexpressweb.service.CategoryAnalysisAsyncService;
import com.sciome.bmdexpressweb.service.ProjectService;
import com.sciome.bmdexpress2.mvp.model.category.CategoryAnalysisResults;
import com.sciome.bmdexpress2.mvp.model.stat.BMDResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

/**
 * REST Controller for category analysis operations
 */
@RestController
@RequestMapping("/api/category-analysis")
public class CategoryAnalysisController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryAnalysisController.class);

    @Autowired
    private ProjectService projectService;

    @Autowired
    private BmdResultsService bmdResultsService;

    @Autowired
    private CategoryAnalysisAsyncService analysisService;

    /**
     * Submit a category analysis job
     *
     * POST /api/category-analysis
     *
     * @param request Category analysis request
     * @return Analysis job response with analysis ID
     */
    @PostMapping
    public ResponseEntity<CategoryAnalysisResponse> submitCategoryAnalysis(
            @RequestBody CategoryAnalysisRequest request) {

        try {
            logger.info("Submitting category analysis: type={}, project={}, bmdResult={}",
                    request.getAnalysisType(), request.getProjectId(), request.getBmdResultName());

            // Validate project exists
            if (!projectService.projectExists(request.getProjectId())) {
                return ResponseEntity.notFound().build();
            }

            // Find the BMD result
            BMDResult bmdResult = bmdResultsService.findBmdResult(
                    request.getProjectId(),
                    request.getBmdResultName());

            // Submit async analysis
            CompletableFuture<String> future = analysisService.runCategoryAnalysisAsync(
                    bmdResult,
                    request.getAnalysisType(),
                    request.getParameters());

            // Wait for analysis ID (this completes immediately, actual analysis runs in background)
            String analysisId = future.join();

            CategoryAnalysisResponse response = new CategoryAnalysisResponse(
                    analysisId,
                    request.getProjectId(),
                    "RUNNING");

            response.setResultLocation("/api/category-analysis/" + analysisId);

            logger.info("Category analysis submitted: {}", analysisId);

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid request", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Failed to submit category analysis", e);
            throw new RuntimeException("Failed to submit category analysis: " + e.getMessage(), e);
        }
    }

    /**
     * Get category analysis status and results
     *
     * GET /api/category-analysis/{analysisId}
     *
     * @param analysisId The analysis ID
     * @return Analysis status and results
     */
    @GetMapping("/{analysisId}")
    public ResponseEntity<?> getCategoryAnalysis(@PathVariable String analysisId) {

        try {
            CategoryAnalysisAsyncService.AnalysisJobResult job =
                    analysisService.getAnalysisResult(analysisId);

            if ("COMPLETED".equals(job.getStatus())) {
                // Return the full results
                return ResponseEntity.ok(job.getResults());
            } else {
                // Return status only
                CategoryAnalysisResponse response = new CategoryAnalysisResponse(
                        job.getAnalysisId(),
                        null,
                        job.getStatus());
                response.setSubmittedAt(job.getSubmittedAt());
                response.setCompletedAt(job.getCompletedAt());
                response.setErrorMessage(job.getErrorMessage());

                return ResponseEntity.ok(response);
            }

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Export category analysis results as JSON
     *
     * GET /api/category-analysis/{analysisId}/export?format=json
     *
     * STUB: Returns results in JSON format. In a full implementation,
     * this would format the CategoryAnalysisResults for download.
     *
     * @param analysisId The analysis ID
     * @return Category analysis results as JSON
     */
    @GetMapping(value = "/{analysisId}/export", params = "format=json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CategoryAnalysisResults> exportCategoryAnalysisJson(
            @PathVariable String analysisId) {

        try {
            CategoryAnalysisAsyncService.AnalysisJobResult job =
                    analysisService.getAnalysisResult(analysisId);

            if (!"COMPLETED".equals(job.getStatus())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            // STUB: In a full implementation, this would return the actual results
            // See /tmp/server/controller/CategoryAnalysisController.java lines 135-154
            logger.warn("Export JSON is stubbed - no actual results to export");

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=category_analysis_" + analysisId + ".json")
                    .body(job.getResults());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Export category analysis results as TSV
     *
     * GET /api/category-analysis/{analysisId}/export?format=tsv
     *
     * STUB: Would generate TSV format output from CategoryAnalysisResults.
     * In a full implementation, this would:
     * 1. Get the column headers from results.getColumnHeader()
     * 2. Get all rows from results.getCategoryAnalysisResults()
     * 3. Format as tab-separated values
     *
     * @param analysisId The analysis ID
     * @param format The format parameter (should be "tsv")
     * @return Category analysis results as TSV
     */
    @GetMapping(value = "/{analysisId}/export", params = "format=tsv", produces = "text/tab-separated-values")
    public ResponseEntity<String> exportCategoryAnalysisTsv(
            @PathVariable String analysisId,
            @RequestParam(defaultValue = "tsv") String format) {

        try {
            CategoryAnalysisAsyncService.AnalysisJobResult job =
                    analysisService.getAnalysisResult(analysisId);

            if (!"COMPLETED".equals(job.getStatus())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            CategoryAnalysisResults results = job.getResults();

            // STUB: Generate placeholder TSV
            // See /tmp/server/controller/CategoryAnalysisController.java lines 164-201 for full implementation
            StringBuilder tsv = new StringBuilder();
            tsv.append("# Category analysis results export is stubbed\n");
            tsv.append("# No actual results available\n");
            tsv.append("ID\tName\tDescription\n");

            logger.warn("Export TSV is stubbed - returning placeholder data");

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=category_analysis_" + analysisId + ".tsv")
                    .body(tsv.toString());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
