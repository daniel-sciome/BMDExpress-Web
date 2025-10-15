package com.sciome.bmdexpressweb.controller;

import com.sciome.bmdexpressweb.service.BmdResultsService;
import com.sciome.bmdexpressweb.service.CategoryResultsService;
import com.sciome.bmdexpressweb.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test suite for GlobalExceptionHandler
 *
 * Tests that exceptions thrown by controllers are properly caught
 * and converted to appropriate HTTP responses with error details.
 */
@WebMvcTest(ProjectController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private BmdResultsService bmdResultsService;

    @MockBean
    private CategoryResultsService categoryResultsService;

    @Test
    void testHandleRuntimeException_ReturnsInternalServerError() throws Exception {
        // Arrange - Service throws RuntimeException
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.bm2",
                "application/octet-stream",
                new byte[]{1, 2, 3}
        );

        when(projectService.loadProject(any(), anyString()))
                .thenThrow(new RuntimeException("Failed to deserialize project"));

        // Act & Assert - Should return 500 with error details
        mockMvc.perform(multipart("/api/projects")
                        .file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Failed to upload project: Failed to deserialize project"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/projects"));
    }

    @Test
    void testHandleIllegalArgumentException_ReturnsBadRequest() throws Exception {
        // Arrange - Service throws IllegalArgumentException for invalid parameter
        when(projectService.getProjectHolder(anyString()))
                .thenThrow(new IllegalArgumentException("Invalid project ID format: invalid-id"));

        // Act & Assert - Should return 400 with error details
        mockMvc.perform(get("/api/projects/{projectId}", "invalid-id"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid project ID format: invalid-id"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/projects/invalid-id"));
    }

    @Test
    void testHandleIllegalArgumentException_ReturnsNotFound() throws Exception {
        // Arrange - Service throws IllegalArgumentException with "not found" message
        when(projectService.getProjectHolder(anyString()))
                .thenThrow(new IllegalArgumentException("Project not found: missing-id"));

        // Act & Assert - Should return 404 when message contains "not found"
        mockMvc.perform(get("/api/projects/{projectId}", "missing-id"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Project not found: missing-id"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/projects/missing-id"));
    }

    @Test
    void testHandleIOException_ReturnsInternalServerError() throws Exception {
        // Arrange - Service throws IOException
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.bm2",
                "application/octet-stream",
                new byte[]{1, 2, 3}
        );

        when(projectService.loadProject(any(), anyString()))
                .thenThrow(new IOException("Failed to read file"));

        // Act & Assert - Should return 500 with error details
        mockMvc.perform(multipart("/api/projects")
                        .file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message", containsString("Failed to read file")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testHandleClassNotFoundException_ReturnsInternalServerError() throws Exception {
        // Arrange - Service throws ClassNotFoundException (invalid .bm2 format)
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.bm2",
                "application/octet-stream",
                new byte[]{1, 2, 3}
        );

        when(projectService.loadProject(any(), anyString()))
                .thenThrow(new ClassNotFoundException("com.sciome.bmdexpress2.mvp.model.BMDProject"));

        // Act & Assert - Should return 500 with error details
        mockMvc.perform(multipart("/api/projects")
                        .file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message", containsString("BMDProject")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testHandleGenericException_ReturnsInternalServerError() throws Exception {
        // Arrange - Service throws generic Exception
        when(bmdResultsService.getBmdResultNames(anyString()))
                .thenThrow(new RuntimeException("Unexpected error occurred"));

        // Act & Assert - Should return 500 with error details
        mockMvc.perform(get("/api/projects/{projectId}/bmd-results", "test-id"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Unexpected error occurred"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/projects/test-id/bmd-results"));
    }

    @Test
    void testHandleMethodArgumentNotValid_ReturnsBadRequest() throws Exception {
        // This test covers Spring's validation exceptions
        // We don't have @Valid annotations yet, but the handler should be ready for them

        String invalidJson = "{\"filename\": null}";

        // Act & Assert - Missing required field should return 400
        mockMvc.perform(post("/api/projects/load-from-file")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
