package com.sciome.bmdexpressweb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sciome.bmdexpressweb.dto.ProjectUploadResponse;
import com.sciome.bmdexpressweb.service.BmdResultsService;
import com.sciome.bmdexpressweb.service.CategoryResultsService;
import com.sciome.bmdexpressweb.service.ProjectService;
import com.sciome.bmdexpress2.mvp.model.BMDProject;
import com.sciome.bmdexpress2.mvp.model.stat.BMDResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for ProjectController
 */
@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private BmdResultsService bmdResultsService;

    @MockBean
    private CategoryResultsService categoryResultsService;

    private BMDProject mockProject;
    private ProjectService.ProjectHolder mockHolder;
    private String testProjectId;

    @BeforeEach
    void setUp() {
        testProjectId = "test-project-id-123";

        // Create mock BMDProject
        mockProject = new BMDProject();
        mockProject.setName("Test Project");

        // Create mock BMD results
        List<BMDResult> bmdResults = new ArrayList<>();
        BMDResult result1 = new BMDResult();
        result1.setName("BMD Analysis 1");
        bmdResults.add(result1);

        BMDResult result2 = new BMDResult();
        result2.setName("BMD Analysis 2");
        bmdResults.add(result2);

        mockProject.setbMDResult(bmdResults);

        // Create mock category results (empty for now)
        mockProject.setCategoryAnalysisResults(new ArrayList<>());

        // Create mock project holder
        mockHolder = new ProjectService.ProjectHolder(
                testProjectId,
                mockProject,
                "test.bm2",
                LocalDateTime.now()
        );
    }

    /**
     * Helper method to serialize a BMDProject
     */
    private byte[] serializeProject(BMDProject project) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(project);
        oos.close();
        return baos.toByteArray();
    }

    @Test
    void testUploadProject_Success() throws Exception {
        // Arrange
        byte[] fileContent = serializeProject(mockProject);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.bm2",
                "application/octet-stream",
                fileContent
        );

        when(projectService.loadProject(any(), anyString())).thenReturn(testProjectId);
        when(projectService.getProjectHolder(testProjectId)).thenReturn(mockHolder);

        // Act & Assert
        mockMvc.perform(multipart("/api/projects")
                        .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectId").value(testProjectId))
                .andExpect(jsonPath("$.name").value("Test Project"))
                .andExpect(jsonPath("$.bmdResultNames", hasSize(2)))
                .andExpect(jsonPath("$.bmdResultNames[0]").value("BMD Analysis 1"))
                .andExpect(jsonPath("$.bmdResultNames[1]").value("BMD Analysis 2"))
                .andExpect(jsonPath("$.categoryResultNames", hasSize(0)));

        verify(projectService, times(1)).loadProject(any(), eq("test.bm2"));
        verify(projectService, times(1)).getProjectHolder(testProjectId);
    }

    // TODO: Add test for upload failure once GlobalExceptionHandler is implemented
    // Currently the controller throws RuntimeException which isn't caught without a global handler

    @Test
    void testGetProject_Success() throws Exception {
        // Arrange
        when(projectService.getProjectHolder(testProjectId)).thenReturn(mockHolder);

        // Act & Assert
        mockMvc.perform(get("/api/projects/{projectId}", testProjectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(testProjectId))
                .andExpect(jsonPath("$.name").value("Test Project"))
                .andExpect(jsonPath("$.bmdResultNames", hasSize(2)));

        verify(projectService, times(1)).getProjectHolder(testProjectId);
    }

    @Test
    void testGetProject_NotFound() throws Exception {
        // Arrange
        when(projectService.getProjectHolder(anyString()))
                .thenThrow(new IllegalArgumentException("Project not found"));

        // Act & Assert
        mockMvc.perform(get("/api/projects/{projectId}", "non-existent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetBmdResults_Success() throws Exception {
        // Arrange
        List<String> resultNames = List.of("BMD Analysis 1", "BMD Analysis 2");
        when(bmdResultsService.getBmdResultNames(testProjectId)).thenReturn(resultNames);

        // Act & Assert
        mockMvc.perform(get("/api/projects/{projectId}/bmd-results", testProjectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]").value("BMD Analysis 1"))
                .andExpect(jsonPath("$[1]").value("BMD Analysis 2"));

        verify(bmdResultsService, times(1)).getBmdResultNames(testProjectId);
    }

    @Test
    void testGetBmdResults_NotFound() throws Exception {
        // Arrange
        when(bmdResultsService.getBmdResultNames(anyString()))
                .thenThrow(new IllegalArgumentException("Project not found"));

        // Act & Assert
        mockMvc.perform(get("/api/projects/{projectId}/bmd-results", "non-existent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetFullProject_Success() throws Exception {
        // Arrange
        when(projectService.getProjectHolder(testProjectId)).thenReturn(mockHolder);

        // Act & Assert
        mockMvc.perform(get("/api/projects/{projectId}/full", testProjectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Project"));

        verify(projectService, times(1)).getProjectHolder(testProjectId);
    }

    @Test
    void testGetFullProject_NotFound() throws Exception {
        // Arrange
        when(projectService.getProjectHolder(anyString()))
                .thenThrow(new IllegalArgumentException("Project not found"));

        // Act & Assert
        mockMvc.perform(get("/api/projects/{projectId}/full", "non-existent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteProject_Success() throws Exception {
        // Arrange
        when(projectService.projectExists(testProjectId)).thenReturn(true);
        doNothing().when(projectService).deleteProject(testProjectId);

        // Act & Assert
        mockMvc.perform(delete("/api/projects/{projectId}", testProjectId))
                .andExpect(status().isNoContent());

        verify(projectService, times(1)).projectExists(testProjectId);
        verify(projectService, times(1)).deleteProject(testProjectId);
    }

    @Test
    void testDeleteProject_NotFound() throws Exception {
        // Arrange
        when(projectService.projectExists(anyString())).thenReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/api/projects/{projectId}", "non-existent-id"))
                .andExpect(status().isNotFound());

        verify(projectService, times(1)).projectExists("non-existent-id");
        verify(projectService, never()).deleteProject(anyString());
    }

    @Test
    void testListAvailableFiles_Success() throws Exception {
        // Note: This test would require actual filesystem interaction
        // In a real scenario, you'd mock the filesystem or use a test directory
        // For now, we'll test the empty case

        // Act & Assert
        mockMvc.perform(get("/api/projects/available-files"))
                .andExpect(status().isOk());
    }

    @Test
    void testLoadProjectFromFile_InvalidFilename() throws Exception {
        // Arrange - filename with directory traversal
        String requestBody = objectMapper.writeValueAsString(
                java.util.Map.of("filename", "../etc/passwd")
        );

        // Act & Assert
        mockMvc.perform(post("/api/projects/load-from-file")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Invalid filename"));
    }

    @Test
    void testLoadProjectFromFile_EmptyFilename() throws Exception {
        // Arrange
        String requestBody = objectMapper.writeValueAsString(
                java.util.Map.of("filename", "")
        );

        // Act & Assert
        mockMvc.perform(post("/api/projects/load-from-file")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No filename provided"));
    }

    @Test
    void testLoadProjectFromFile_NoFilename() throws Exception {
        // Arrange
        String requestBody = "{}";

        // Act & Assert
        mockMvc.perform(post("/api/projects/load-from-file")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No filename provided"));
    }

    @Test
    void testUploadProject_NoFile() throws Exception {
        // Act & Assert
        mockMvc.perform(multipart("/api/projects"))
                .andExpect(status().isBadRequest());
    }
}
