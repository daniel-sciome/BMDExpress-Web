package com.sciome.bmdexpressweb.service;

import com.sciome.bmdexpress2.mvp.model.BMDProject;
import com.sciome.bmdexpress2.mvp.model.category.CategoryAnalysisResults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CategoryResultsService
 *
 * Tests finding and querying category analysis results within projects.
 */
class CategoryResultsServiceTest {

    private CategoryResultsService service;
    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectService = mock(ProjectService.class);
        service = new CategoryResultsService(projectService);
    }

    /**
     * Helper method to create a mock BMDProject with category analysis results
     */
    private BMDProject createProjectWithCategoryResults() {
        BMDProject project = new BMDProject();
        project.setName("Test Project");

        List<CategoryAnalysisResults> categoryResults = new ArrayList<>();

        CategoryAnalysisResults result1 = new CategoryAnalysisResults();
        result1.setName("Pathway Analysis 1");
        categoryResults.add(result1);

        CategoryAnalysisResults result2 = new CategoryAnalysisResults();
        result2.setName("GO Analysis 1");
        categoryResults.add(result2);

        project.setCategoryAnalysisResults(categoryResults);

        return project;
    }

    @Test
    void testFindCategoryResult_Success() {
        // Arrange
        BMDProject project = createProjectWithCategoryResults();
        when(projectService.getProject("test-id")).thenReturn(project);

        // Act
        CategoryAnalysisResults result = service.findCategoryResult("test-id", "Pathway Analysis 1");

        // Assert
        assertNotNull(result);
        assertEquals("Pathway Analysis 1", result.getName());
        verify(projectService, times(1)).getProject("test-id");
    }

    @Test
    void testFindCategoryResult_CaseInsensitive() {
        // Arrange
        BMDProject project = createProjectWithCategoryResults();
        when(projectService.getProject("test-id")).thenReturn(project);

        // Act
        CategoryAnalysisResults result = service.findCategoryResult("test-id", "pathway analysis 1");

        // Assert
        assertNotNull(result);
        assertEquals("Pathway Analysis 1", result.getName());
    }

    @Test
    void testFindCategoryResult_NotFound() {
        // Arrange
        BMDProject project = createProjectWithCategoryResults();
        when(projectService.getProject("test-id")).thenReturn(project);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            service.findCategoryResult("test-id", "Non-existent Result");
        });
    }

    @Test
    void testGetCategoryResultNames_Success() {
        // Arrange
        BMDProject project = createProjectWithCategoryResults();
        when(projectService.getProject("test-id")).thenReturn(project);

        // Act
        List<String> names = service.getCategoryResultNames("test-id");

        // Assert
        assertNotNull(names);
        assertEquals(2, names.size());
        assertTrue(names.contains("Pathway Analysis 1"));
        assertTrue(names.contains("GO Analysis 1"));
    }

    @Test
    void testGetCategoryResultNames_EmptyProject() {
        // Arrange
        BMDProject project = new BMDProject();
        project.setCategoryAnalysisResults(new ArrayList<>());
        when(projectService.getProject("test-id")).thenReturn(project);

        // Act
        List<String> names = service.getCategoryResultNames("test-id");

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
            service.getCategoryResultNames("invalid-id");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            service.findCategoryResult("invalid-id", "Some Result");
        });
    }
}
