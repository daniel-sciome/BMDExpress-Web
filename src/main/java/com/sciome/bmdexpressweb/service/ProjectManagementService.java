package com.sciome.bmdexpressweb.service;

import com.sciome.bmdexpress2.mvp.model.BMDProject;
import com.sciome.bmdexpress2.mvp.model.stat.BMDResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for managing BMDExpress projects (.bm2 files)
 * Provides in-memory storage and deserialization of .bm2 project files
 */
@Service
public class ProjectManagementService {

    private static final Logger log = LoggerFactory.getLogger(ProjectManagementService.class);

    // In-memory project store
    // Maps project ID (UUID) -> ProjectHolder (project + metadata)
    private final Map<String, ProjectHolder> projects = new ConcurrentHashMap<>();

    /**
     * Load a .bm2 project file from an InputStream and store it in memory
     *
     * @param inputStream .bm2 file content
     * @param filename Original filename
     * @return Project ID (UUID)
     * @throws IOException if deserialization fails
     * @throws ClassNotFoundException if BMDProject class not found
     */
    public String loadProject(InputStream inputStream, String filename)
            throws IOException, ClassNotFoundException {

        log.info("Loading project from file: {}", filename);

        BufferedInputStream bis = new BufferedInputStream(inputStream, 1024 * 2000);
        ObjectInputStream ois = new ObjectInputStream(bis);

        BMDProject project = (BMDProject) ois.readObject();
        ois.close();

        String projectId = UUID.randomUUID().toString();
        ProjectHolder holder = new ProjectHolder(projectId, project, filename, LocalDateTime.now());

        projects.put(projectId, holder);

        log.info("Project loaded successfully: {} (ID: {})", filename, projectId);

        return projectId;
    }

    /**
     * Get a project by ID
     *
     * @param projectId The project ID
     * @return The BMDProject
     * @throws IllegalArgumentException if project not found
     */
    public BMDProject getProject(String projectId) {
        ProjectHolder holder = projects.get(projectId);
        if (holder == null) {
            throw new IllegalArgumentException("Project not found: " + projectId);
        }
        return holder.getProject();
    }

    /**
     * Get project holder (with metadata)
     *
     * @param projectId The project ID
     * @return ProjectHolder with project and metadata
     * @throws IllegalArgumentException if project not found
     */
    public ProjectHolder getProjectHolder(String projectId) {
        ProjectHolder holder = projects.get(projectId);
        if (holder == null) {
            throw new IllegalArgumentException("Project not found: " + projectId);
        }
        return holder;
    }

    /**
     * Find a BMDResult by name within a project
     *
     * @param projectId The project ID
     * @param bmdResultName The BMDResult name
     * @return The BMDResult
     * @throws IllegalArgumentException if project or result not found
     */
    public BMDResult findBmdResult(String projectId, String bmdResultName) {
        BMDProject project = getProject(projectId);

        return project.getbMDResult().stream()
                .filter(result -> result.getName().equalsIgnoreCase(bmdResultName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "BMDResult not found: " + bmdResultName + " in project " + projectId));
    }

    /**
     * Get list of all BMDResult names in a project
     *
     * @param projectId The project ID
     * @return List of BMDResult names
     */
    public List<String> getBmdResultNames(String projectId) {
        BMDProject project = getProject(projectId);
        return project.getbMDResult().stream()
                .map(BMDResult::getName)
                .collect(Collectors.toList());
    }

    /**
     * Check if project exists
     *
     * @param projectId The project ID
     * @return true if project exists
     */
    public boolean projectExists(String projectId) {
        return projects.containsKey(projectId);
    }

    /**
     * Get all loaded project IDs
     *
     * @return List of project IDs
     */
    public List<String> getAllProjectIds() {
        return projects.keySet().stream()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Remove a project from memory
     *
     * @param projectId The project ID
     */
    public void deleteProject(String projectId) {
        ProjectHolder holder = projects.remove(projectId);
        if (holder != null) {
            log.info("Project deleted: {} (ID: {})", holder.getOriginalFilename(), projectId);
        }
    }

    /**
     * Holder class for project + metadata
     * Stores the BMDProject along with upload metadata
     */
    public static class ProjectHolder {
        private final String projectId;
        private final BMDProject project;
        private final String originalFilename;
        private final LocalDateTime uploadedAt;

        public ProjectHolder(String projectId, BMDProject project,
                           String originalFilename, LocalDateTime uploadedAt) {
            this.projectId = projectId;
            this.project = project;
            this.originalFilename = originalFilename;
            this.uploadedAt = uploadedAt;
        }

        public String getProjectId() {
            return projectId;
        }

        public BMDProject getProject() {
            return project;
        }

        public String getOriginalFilename() {
            return originalFilename;
        }

        public LocalDateTime getUploadedAt() {
            return uploadedAt;
        }
    }
}
