package com.sciome.bmdexpressweb.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Project REST API using real .bm2 files
 *
 * These tests verify the full stack:
 * - File upload and deserialization
 * - Service layer processing
 * - Controller response generation
 * - JSON serialization
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "bmdexpress.projects.dir=data/projects",
        "spring.servlet.multipart.max-file-size=100MB",
        "spring.servlet.multipart.max-request-size=100MB"
})
class ProjectApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private List<String> projectIdsToCleanup = new ArrayList<>();

    @AfterEach
    void cleanup() {
        // Delete any projects created during tests
        for (String projectId : projectIdsToCleanup) {
            restTemplate.delete("/api/projects/" + projectId);
        }
        projectIdsToCleanup.clear();
    }

    @Test
    void testUploadRealBm2File_FullFlow() throws Exception {
        // Arrange
        File bm2File = new File("data/projects/P3MP-Parham.bm2");
        assertThat(bm2File).exists();

        FileSystemResource fileResource = new FileSystemResource(bm2File);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/projects",
                requestEntity,
                String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        JsonNode responseJson = objectMapper.readTree(response.getBody());
        assertThat(responseJson.has("projectId")).isTrue();
        assertThat(responseJson.has("name")).isTrue();
        assertThat(responseJson.has("bmdResultNames")).isTrue();
        assertThat(responseJson.has("categoryResultNames")).isTrue();

        String projectId = responseJson.get("projectId").asText();
        projectIdsToCleanup.add(projectId);

        // Verify project has actual data
        assertThat(responseJson.get("projectId").asText()).isNotEmpty();
        assertThat(responseJson.get("name").asText()).isNotEmpty();

        System.out.println("✓ Uploaded real .bm2 file:");
        System.out.println("  Project ID: " + projectId);
        System.out.println("  Project Name: " + responseJson.get("name").asText());
        System.out.println("  BMD Results: " + responseJson.get("bmdResultNames").size());
        System.out.println("  Category Results: " + responseJson.get("categoryResultNames").size());
    }

    @Test
    void testGetBmdResults_WithRealData() throws Exception {
        // Arrange - Upload real file first
        String projectId = uploadRealBm2File();

        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/projects/" + projectId + "/bmd-results",
                String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode bmdResults = objectMapper.readTree(response.getBody());
        assertThat(bmdResults.isArray()).isTrue();

        if (bmdResults.size() > 0) {
            System.out.println("✓ Found " + bmdResults.size() + " BMD results:");
            for (JsonNode result : bmdResults) {
                System.out.println("  - " + result.asText());
            }
        } else {
            System.out.println("⚠ No BMD results in this project");
        }
    }

    @Test
    void testGetSpecificBmdResult_WithRealData() throws Exception {
        // Arrange - Upload real file first
        String projectId = uploadRealBm2File();

        // Get list of BMD results
        ResponseEntity<String> listResponse = restTemplate.getForEntity(
                "/api/projects/" + projectId + "/bmd-results",
                String.class
        );
        JsonNode bmdResults = objectMapper.readTree(listResponse.getBody());

        if (bmdResults.size() == 0) {
            System.out.println("⚠ Skipping test - no BMD results in project");
            return;
        }

        String firstResultName = bmdResults.get(0).asText();

        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/projects/" + projectId + "/bmd-results/" + firstResultName,
                String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode bmdResult = objectMapper.readTree(response.getBody());
        assertThat(bmdResult.has("name")).isTrue();
        assertThat(bmdResult.get("name").asText()).isEqualTo(firstResultName);

        System.out.println("✓ Retrieved BMD result: " + firstResultName);
        System.out.println("  Result has fields: " + bmdResult.fieldNames());
    }

    @Test
    void testGetCategoryResults_WithRealData() throws Exception {
        // Arrange - Upload real file first
        String projectId = uploadRealBm2File();

        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/projects/" + projectId + "/category-results",
                String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode categoryResults = objectMapper.readTree(response.getBody());
        assertThat(categoryResults.isArray()).isTrue();

        if (categoryResults.size() > 0) {
            System.out.println("✓ Found " + categoryResults.size() + " category results:");
            for (JsonNode result : categoryResults) {
                System.out.println("  - " + result.asText());
            }
        } else {
            System.out.println("⚠ No category results in this project");
        }
    }

    @Test
    void testGetSpecificCategoryResult_WithRealData() throws Exception {
        // Arrange - Upload real file first
        String projectId = uploadRealBm2File();

        // Get list of category results
        ResponseEntity<String> listResponse = restTemplate.getForEntity(
                "/api/projects/" + projectId + "/category-results",
                String.class
        );
        JsonNode categoryResults = objectMapper.readTree(listResponse.getBody());

        if (categoryResults.size() == 0) {
            System.out.println("⚠ Skipping test - no category results in project");
            return;
        }

        String firstResultName = categoryResults.get(0).asText();

        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/projects/" + projectId + "/category-results/" + firstResultName,
                String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode categoryResult = objectMapper.readTree(response.getBody());
        assertThat(categoryResult.has("name")).isTrue();
        assertThat(categoryResult.get("name").asText()).isEqualTo(firstResultName);

        System.out.println("✓ Retrieved category result: " + firstResultName);
        System.out.println("  Result has fields: " + categoryResult.fieldNames());
    }

    @Test
    void testGetFullProject_WithRealData() throws Exception {
        // Arrange - Upload real file first
        String projectId = uploadRealBm2File();

        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/projects/" + projectId + "/full",
                String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode project = objectMapper.readTree(response.getBody());
        assertThat(project.has("name")).isTrue();
        assertThat(project.has("bMDResult")).isTrue();
        assertThat(project.has("categoryAnalysisResults")).isTrue();

        System.out.println("✓ Retrieved full project:");
        System.out.println("  Name: " + project.get("name").asText());
        System.out.println("  BMD Results: " + project.get("bMDResult").size());
        System.out.println("  Category Results: " + project.get("categoryAnalysisResults").size());
    }

    /**
     * Helper method to upload real .bm2 file and return project ID
     */
    private String uploadRealBm2File() throws Exception {
        File bm2File = new File("data/projects/P3MP-Parham.bm2");
        assertThat(bm2File).exists();

        FileSystemResource fileResource = new FileSystemResource(bm2File);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/projects",
                requestEntity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        JsonNode responseJson = objectMapper.readTree(response.getBody());
        String projectId = responseJson.get("projectId").asText();
        projectIdsToCleanup.add(projectId);

        return projectId;
    }
}
