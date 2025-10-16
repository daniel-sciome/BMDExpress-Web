package com.sciome.bmdexpressweb.controller;

import com.sciome.bmdexpressweb.dto.CategoryAnalysisRequest;
import com.sciome.bmdexpressweb.dto.CategoryAnalysisResponse;
import com.sciome.bmdexpressweb.service.BmdResultsService;
import com.sciome.bmdexpressweb.service.CategoryAnalysisAsyncService;
import com.sciome.bmdexpressweb.service.ProjectService;
import com.sciome.bmdexpress2.mvp.model.stat.BMDResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
}
