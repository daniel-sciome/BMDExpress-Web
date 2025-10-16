package com.sciome.bmdexpressweb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sciome.bmdexpressweb.service.BmdResultsService;
import com.sciome.bmdexpressweb.service.CategoryAnalysisAsyncService;
import com.sciome.bmdexpressweb.service.ProjectService;
import com.sciome.bmdexpress2.mvp.model.stat.BMDResult;
import com.sciome.bmdexpress2.shared.CategoryAnalysisEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for CategoryAnalysisController
 *
 * Tests category analysis job submission, status checking, and result retrieval.
 */
@WebMvcTest(CategoryAnalysisController.class)
class CategoryAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private BmdResultsService bmdResultsService;

    @MockBean
    private CategoryAnalysisAsyncService analysisService;

    private BMDResult mockBmdResult;
    private String testProjectId;
    private String testAnalysisId;

    @BeforeEach
    void setUp() {
        testProjectId = "test-project-123";
        testAnalysisId = "analysis-456";

        mockBmdResult = new BMDResult();
        mockBmdResult.setName("BMD Analysis 1");
    }

    @Test
    void testSubmitCategoryAnalysis_Success() throws Exception {
        // Arrange
        when(projectService.projectExists(testProjectId)).thenReturn(true);
        when(bmdResultsService.findBmdResult(testProjectId, "BMD Analysis 1"))
                .thenReturn(mockBmdResult);
        when(analysisService.runCategoryAnalysisAsync(
                eq(mockBmdResult),
                eq(CategoryAnalysisEnum.GO),
                any()))
                .thenReturn(CompletableFuture.completedFuture(testAnalysisId));

        Map<String, Object> request = new HashMap<>();
        request.put("projectId", testProjectId);
        request.put("bmdResultName", "BMD Analysis 1");
        request.put("analysisType", "GO");
        request.put("parameters", new HashMap<>());

        // Act & Assert
        mockMvc.perform(post("/api/category-analysis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.analysisId").value(testAnalysisId))
                .andExpect(jsonPath("$.projectId").value(testProjectId))
                .andExpect(jsonPath("$.status").value("RUNNING"))
                .andExpect(jsonPath("$.resultLocation").value("/api/category-analysis/" + testAnalysisId));

        verify(projectService, times(1)).projectExists(testProjectId);
        verify(bmdResultsService, times(1)).findBmdResult(testProjectId, "BMD Analysis 1");
        verify(analysisService, times(1)).runCategoryAnalysisAsync(
                eq(mockBmdResult),
                eq(CategoryAnalysisEnum.GO),
                any());
    }

    @Test
    void testSubmitCategoryAnalysis_ProjectNotFound() throws Exception {
        // Arrange
        when(projectService.projectExists("invalid-id")).thenReturn(false);

        Map<String, Object> request = new HashMap<>();
        request.put("projectId", "invalid-id");
        request.put("bmdResultName", "BMD Analysis 1");
        request.put("analysisType", "GO");
        request.put("parameters", new HashMap<>());

        // Act & Assert
        mockMvc.perform(post("/api/category-analysis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(projectService, times(1)).projectExists("invalid-id");
        verify(bmdResultsService, never()).findBmdResult(any(), any());
        verify(analysisService, never()).runCategoryAnalysisAsync(any(), any(), any());
    }

    @Test
    void testSubmitCategoryAnalysis_BmdResultNotFound() throws Exception {
        // Arrange
        when(projectService.projectExists(testProjectId)).thenReturn(true);
        when(bmdResultsService.findBmdResult(testProjectId, "NonExistent"))
                .thenThrow(new IllegalArgumentException("BMDResult not found: NonExistent"));

        Map<String, Object> request = new HashMap<>();
        request.put("projectId", testProjectId);
        request.put("bmdResultName", "NonExistent");
        request.put("analysisType", "GO");
        request.put("parameters", new HashMap<>());

        // Act & Assert
        mockMvc.perform(post("/api/category-analysis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(projectService, times(1)).projectExists(testProjectId);
        verify(bmdResultsService, times(1)).findBmdResult(testProjectId, "NonExistent");
    }

    @Test
    void testGetCategoryAnalysis_Running() throws Exception {
        // Arrange
        CategoryAnalysisAsyncService.AnalysisJobResult jobResult =
                new CategoryAnalysisAsyncService.AnalysisJobResult(testAnalysisId);
        jobResult.setStatus("RUNNING");
        jobResult.setSubmittedAt(LocalDateTime.now());

        when(analysisService.getAnalysisResult(testAnalysisId)).thenReturn(jobResult);

        // Act & Assert
        mockMvc.perform(get("/api/category-analysis/{analysisId}", testAnalysisId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysisId").value(testAnalysisId))
                .andExpect(jsonPath("$.status").value("RUNNING"))
                .andExpect(jsonPath("$.submittedAt").exists());

        verify(analysisService, times(1)).getAnalysisResult(testAnalysisId);
    }

    @Test
    void testGetCategoryAnalysis_NotFound() throws Exception {
        // Arrange
        when(analysisService.getAnalysisResult("invalid-id"))
                .thenThrow(new IllegalArgumentException("Analysis not found: invalid-id"));

        // Act & Assert
        mockMvc.perform(get("/api/category-analysis/{analysisId}", "invalid-id"))
                .andExpect(status().isNotFound());

        verify(analysisService, times(1)).getAnalysisResult("invalid-id");
    }

    @Test
    void testGetCategoryAnalysis_Completed() throws Exception {
        // Arrange
        CategoryAnalysisAsyncService.AnalysisJobResult jobResult =
                new CategoryAnalysisAsyncService.AnalysisJobResult(testAnalysisId);
        jobResult.setStatus("COMPLETED");
        jobResult.setSubmittedAt(LocalDateTime.now());
        jobResult.setCompletedAt(LocalDateTime.now());
        // Note: Would need to mock CategoryAnalysisResults for full test

        when(analysisService.getAnalysisResult(testAnalysisId)).thenReturn(jobResult);

        // Act & Assert
        mockMvc.perform(get("/api/category-analysis/{analysisId}", testAnalysisId))
                .andExpect(status().isOk());

        verify(analysisService, times(1)).getAnalysisResult(testAnalysisId);
    }

    @Test
    void testGetCategoryAnalysis_Failed() throws Exception {
        // Arrange
        CategoryAnalysisAsyncService.AnalysisJobResult jobResult =
                new CategoryAnalysisAsyncService.AnalysisJobResult(testAnalysisId);
        jobResult.setStatus("FAILED");
        jobResult.setSubmittedAt(LocalDateTime.now());
        jobResult.setCompletedAt(LocalDateTime.now());
        jobResult.setErrorMessage("Analysis failed due to invalid parameters");

        when(analysisService.getAnalysisResult(testAnalysisId)).thenReturn(jobResult);

        // Act & Assert
        mockMvc.perform(get("/api/category-analysis/{analysisId}", testAnalysisId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysisId").value(testAnalysisId))
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.errorMessage").value("Analysis failed due to invalid parameters"));

        verify(analysisService, times(1)).getAnalysisResult(testAnalysisId);
    }
}
