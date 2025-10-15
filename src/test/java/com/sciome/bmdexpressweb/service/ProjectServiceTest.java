package com.sciome.bmdexpressweb.service;

import com.sciome.bmdexpress2.mvp.model.BMDProject;
import com.sciome.bmdexpress2.mvp.model.stat.BMDResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProjectService
 */
class ProjectServiceTest {

    private ProjectService service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        service = new ProjectService();
    }

    /**
     * Helper method to create a mock BMDProject for testing
     */
    private BMDProject createMockProject(String name) {
        BMDProject project = new BMDProject();
        project.setName(name);

        // Create mock BMD results
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

    /**
     * Helper method to serialize a BMDProject to an InputStream
     */
    private InputStream serializeProject(BMDProject project) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(project);
        oos.close();
        return new ByteArrayInputStream(baos.toByteArray());
    }

    @Test
    void testLoadProject_Success() throws Exception {
        // Arrange
        BMDProject mockProject = createMockProject("Test Project");
        InputStream inputStream = serializeProject(mockProject);

        // Act
        String projectId = service.loadProject(inputStream, "test.bm2");

        // Assert
        assertNotNull(projectId);
        assertFalse(projectId.isEmpty());
        assertTrue(service.projectExists(projectId));
    }

    @Test
    void testLoadProject_GeneratesUniqueIds() throws Exception {
        // Arrange
        BMDProject project1 = createMockProject("Project 1");
        BMDProject project2 = createMockProject("Project 2");

        // Act
        String projectId1 = service.loadProject(serializeProject(project1), "test1.bm2");
        String projectId2 = service.loadProject(serializeProject(project2), "test2.bm2");

        // Assert
        assertNotEquals(projectId1, projectId2);
    }

    @Test
    void testGetProject_Success() throws Exception {
        // Arrange
        BMDProject mockProject = createMockProject("Test Project");
        String projectId = service.loadProject(serializeProject(mockProject), "test.bm2");

        // Act
        BMDProject retrieved = service.getProject(projectId);

        // Assert
        assertNotNull(retrieved);
        assertEquals("Test Project", retrieved.getName());
    }

    @Test
    void testGetProject_NotFound() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            service.getProject("non-existent-id");
        });
    }

    @Test
    void testGetProjectHolder_Success() throws Exception {
        // Arrange
        BMDProject mockProject = createMockProject("Test Project");
        String projectId = service.loadProject(serializeProject(mockProject), "test.bm2");

        // Act
        ProjectService.ProjectHolder holder = service.getProjectHolder(projectId);

        // Assert
        assertNotNull(holder);
        assertEquals(projectId, holder.getProjectId());
        assertEquals("test.bm2", holder.getOriginalFilename());
        assertNotNull(holder.getUploadedAt());
        assertNotNull(holder.getProject());
    }

    @Test
    void testGetProjectHolder_NotFound() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            service.getProjectHolder("non-existent-id");
        });
    }

    @Test
    void testProjectExists_True() throws Exception {
        // Arrange
        BMDProject mockProject = createMockProject("Test Project");
        String projectId = service.loadProject(serializeProject(mockProject), "test.bm2");

        // Act
        boolean exists = service.projectExists(projectId);

        // Assert
        assertTrue(exists);
    }

    @Test
    void testProjectExists_False() {
        // Act
        boolean exists = service.projectExists("non-existent-id");

        // Assert
        assertFalse(exists);
    }

    @Test
    void testGetAllProjectIds_Empty() {
        // Act
        List<String> ids = service.getAllProjectIds();

        // Assert
        assertNotNull(ids);
        assertTrue(ids.isEmpty());
    }

    @Test
    void testGetAllProjectIds_Multiple() throws Exception {
        // Arrange
        BMDProject project1 = createMockProject("Project 1");
        BMDProject project2 = createMockProject("Project 2");
        String id1 = service.loadProject(serializeProject(project1), "test1.bm2");
        String id2 = service.loadProject(serializeProject(project2), "test2.bm2");

        // Act
        List<String> ids = service.getAllProjectIds();

        // Assert
        assertNotNull(ids);
        assertEquals(2, ids.size());
        assertTrue(ids.contains(id1));
        assertTrue(ids.contains(id2));
    }

    @Test
    void testDeleteProject_Success() throws Exception {
        // Arrange
        BMDProject mockProject = createMockProject("Test Project");
        String projectId = service.loadProject(serializeProject(mockProject), "test.bm2");

        // Act
        service.deleteProject(projectId);

        // Assert
        assertFalse(service.projectExists(projectId));
    }

    @Test
    void testDeleteProject_NonExistent() {
        // Act - should not throw exception
        assertDoesNotThrow(() -> {
            service.deleteProject("non-existent-id");
        });
    }

    @Test
    void testLoadProject_InvalidStream() {
        // Arrange
        InputStream invalidStream = new ByteArrayInputStream("not a valid serialized object".getBytes());

        // Act & Assert
        assertThrows(Exception.class, () -> {
            service.loadProject(invalidStream, "invalid.bm2");
        });
    }

    @Test
    void testMultipleOperations_Integration() throws Exception {
        // Arrange
        BMDProject project1 = createMockProject("Project 1");
        BMDProject project2 = createMockProject("Project 2");

        // Act - Load multiple projects
        String id1 = service.loadProject(serializeProject(project1), "test1.bm2");
        String id2 = service.loadProject(serializeProject(project2), "test2.bm2");

        // Assert - Both exist
        assertTrue(service.projectExists(id1));
        assertTrue(service.projectExists(id2));
        assertEquals(2, service.getAllProjectIds().size());

        // Act - Delete one
        service.deleteProject(id1);

        // Assert - Only one remains
        assertFalse(service.projectExists(id1));
        assertTrue(service.projectExists(id2));
        assertEquals(1, service.getAllProjectIds().size());

        // Assert - Can still access remaining project
        BMDProject retrieved = service.getProject(id2);
        assertEquals("Project 2", retrieved.getName());
    }
}
