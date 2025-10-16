package com.sciome.bmdexpressweb.service;

import com.sciome.bmdexpress2.mvp.model.category.CategoryAnalysisResults;
import com.sciome.bmdexpress2.mvp.model.stat.BMDResult;
import com.sciome.bmdexpress2.shared.CategoryAnalysisEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Async service for running category analyses
 * Wraps the core CategoryAnalysisService from BMDExpress
 */
@Service
public class CategoryAnalysisAsyncService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryAnalysisAsyncService.class);

    // In-memory result store
    private final Map<String, AnalysisJobResult> analysisResults = new ConcurrentHashMap<>();

    /**
     * Run category analysis asynchronously
     *
     * @param bmdResult The BMDResult to analyze
     * @param analysisType Type of category analysis (GO, PATHWAY, DEFINED, GENE_LEVEL)
     * @param parameters Analysis parameters
     * @return CompletableFuture with analysis ID
     */
    @Async
    public CompletableFuture<String> runCategoryAnalysisAsync(
            BMDResult bmdResult,
            CategoryAnalysisEnum analysisType,
            Map<String, Object> parameters) {

        String analysisId = UUID.randomUUID().toString();

        // Store initial status
        AnalysisJobResult job = new AnalysisJobResult(analysisId);
        job.setStatus("RUNNING");
        job.setSubmittedAt(LocalDateTime.now());
        analysisResults.put(analysisId, job);

        try {
            logger.info("Starting category analysis: {} for BMDResult: {}", analysisType, bmdResult.getName());

            // TODO: Implement actual analysis
            // For now, just store as completed
            job.setStatus("COMPLETED");
            job.setCompletedAt(LocalDateTime.now());

            logger.info("Completed category analysis: {}", analysisId);

            return CompletableFuture.completedFuture(analysisId);

        } catch (Exception e) {
            logger.error("Category analysis failed: {}", analysisId, e);
            job.setStatus("FAILED");
            job.setErrorMessage(e.getMessage());
            job.setCompletedAt(LocalDateTime.now());
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Get analysis job status and results
     */
    public AnalysisJobResult getAnalysisResult(String analysisId) {
        AnalysisJobResult result = analysisResults.get(analysisId);
        if (result == null) {
            throw new IllegalArgumentException("Analysis not found: " + analysisId);
        }
        return result;
    }

    /**
     * Result holder for async analysis jobs
     */
    public static class AnalysisJobResult {
        private final String analysisId;
        private String status; // PENDING, RUNNING, COMPLETED, FAILED
        private LocalDateTime submittedAt;
        private LocalDateTime completedAt;
        private CategoryAnalysisResults results;
        private String errorMessage;

        public AnalysisJobResult(String analysisId) {
            this.analysisId = analysisId;
            this.status = "PENDING";
        }

        // Getters and setters
        public String getAnalysisId() {
            return analysisId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public LocalDateTime getSubmittedAt() {
            return submittedAt;
        }

        public void setSubmittedAt(LocalDateTime submittedAt) {
            this.submittedAt = submittedAt;
        }

        public LocalDateTime getCompletedAt() {
            return completedAt;
        }

        public void setCompletedAt(LocalDateTime completedAt) {
            this.completedAt = completedAt;
        }

        public CategoryAnalysisResults getResults() {
            return results;
        }

        public void setResults(CategoryAnalysisResults results) {
            this.results = results;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}
