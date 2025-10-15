package com.sciome.bmdexpressweb.service;

import com.sciome.bmdexpress2.mvp.model.BMDProject;
import com.sciome.bmdexpress2.mvp.model.stat.BMDResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BmdResultsService
 *
 * Tests finding and querying BMD results within projects.
 */
class BmdResultsServiceTest {

    private BmdResultsService service;
    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectService = mock(ProjectService.class);
        service = new BmdResultsService(projectService);
    }

    /**
     * Helper method to create a mock BMDProject with BMD results
     */
    private BMDProject createProjectWithBmdResults() {
        BMDProject project = new BMDProject();
        project.setName("Test Project");

        List<BMDResult> bmdResults = new ArrayList<>();

        BMDResult result1 = new BMDResult();
        result1.setName("BMD Analysis 1");
        bmdResults.add(result1);

        BMDResult result2 = new BMDResult();
        result2.setName("BMD Analysis 2");
        bmdResults.add(result2);

        project.setbMDResult(bmdResults);

        return project;
    }

    @Test
    void testFindBmdResult_Success() {
        // Arrange
        BMDProject project = createProjectWithBmdResults();
        when(projectService.getProject("test-id")).thenReturn(project);

        // Act
        BMDResult result = service.findBmdResult("test-id", "BMD Analysis 1");

        // Assert
        assertNotNull(result);
        assertEquals("BMD Analysis 1", result.getName());
        verify(projectService, times(1)).getProject("test-id");
    }

    @Test
    void testFindBmdResult_CaseInsensitive() {
        // Arrange
        BMDProject project = createProjectWithBmdResults();
        when(projectService.getProject("test-id")).thenReturn(project);

        // Act
        BMDResult result = service.findBmdResult("test-id", "bmd analysis 1");

        // Assert
        assertNotNull(result);
        assertEquals("BMD Analysis 1", result.getName());
    }

    @Test
    void testFindBmdResult_NotFound() {
        // Arrange
        BMDProject project = createProjectWithBmdResults();
        when(projectService.getProject("test-id")).thenReturn(project);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            service.findBmdResult("test-id", "Non-existent Result");
        });
    }

    @Test
    void testGetBmdResultNames_Success() {
        // Arrange
        BMDProject project = createProjectWithBmdResults();
        when(projectService.getProject("test-id")).thenReturn(project);

        // Act
        List<String> names = service.getBmdResultNames("test-id");

        // Assert
        assertNotNull(names);
        assertEquals(2, names.size());
        assertTrue(names.contains("BMD Analysis 1"));
        assertTrue(names.contains("BMD Analysis 2"));
    }

    @Test
    void testGetBmdResultNames_EmptyProject() {
        // Arrange
        BMDProject project = new BMDProject();
        project.setbMDResult(new ArrayList<>());
        when(projectService.getProject("test-id")).thenReturn(project);

        // Act
        List<String> names = service.getBmdResultNames("test-id");

        // Assert
        assertNotNull(names);
        assertTrue(names.isEmpty());
    }

    @Test
    void testProjectNotFound_ThrowsException() {
        // Arrange
        when(projectService.getProject("invalid-id"))
                .thenThrow(new IllegalArgumentException("Project not found: invalid-id"));

        // Act & Assert - Should propagate the exception
        assertThrows(IllegalArgumentException.class, () -> {
            service.getBmdResultNames("invalid-id");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            service.findBmdResult("invalid-id", "Some Result");
        });
    }
}
