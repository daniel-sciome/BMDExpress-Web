# Testing Strategy

## Testing Pyramid

```
                    ┌───────────────┐
                    │  E2E Tests    │  (5%)
                    │  (Selenium)   │
                    └───────────────┘
                 ┌─────────────────────┐
                 │ Integration Tests   │  (20%)
                 │  (Testcontainers)   │
                 └─────────────────────┘
          ┌──────────────────────────────────┐
          │        Unit Tests                │  (75%)
          │   (JUnit, Mockito)               │
          └──────────────────────────────────┘
```

## Unit Testing

**Scope:** Individual classes and methods

**Tools:**
- JUnit 5
- Mockito for mocking
- AssertJ for fluent assertions

**Example:**
```java
@ExtendWith(MockitoExtension.class)
class BMDAnalysisServiceTest {

    @Mock
    private BMDSTool bmdsTool;

    @Mock
    private BMDResultRepository resultRepository;

    @InjectMocks
    private BMDAnalysisService analysisService;

    @Test
    void testExecuteBMDAnalysis() {
        // Given
        DoseResponseExperiment experiment = createTestExperiment();
        ModelInputParameters params = createTestParams();

        // When
        BMDResult result = analysisService.executeBMDAnalysis(
            experiment, params, modelSelectionParams, models
        );

        // Then
        assertThat(result.getProbeStatResults()).hasSize(10);
        assertThat(result.getBmdMethod()).isEqualTo(BMDMethod.BMDS);
    }
}
```

## Integration Testing

**Scope:** Component interactions with real infrastructure

**Tools:**
- Spring Boot Test
- Testcontainers (PostgreSQL, Redis)
- MockMvc for API testing

**Example:**
```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
class AnalysisControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379);

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testSubmitBMDAnalysis() {
        // Given
        BMDAnalysisRequest request = new BMDAnalysisRequest();
        request.setProjectId(1L);
        request.setExperimentId(1L);

        // When
        ResponseEntity<JobSubmissionResponse> response = restTemplate
            .postForEntity("/api/v1/analysis/bmd", request, JobSubmissionResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody().getJobId()).isNotNull();
    }
}
```

## End-to-End Testing

**Scope:** Complete user workflows

**Tools:**
- Selenium WebDriver
- Vaadin TestBench

**Test Scenarios:**
1. User registration and login
2. Create project
3. Import expression data
4. Run ANOVA prefilter
5. Run BMD analysis
6. View results
7. Export results

## Performance Testing

**Tools:**
- Apache JMeter
- Gatling

**Scenarios:**
- 100 concurrent users browsing
- 50 concurrent analysis jobs
- Large file upload (100MB expression data)

**Success Criteria:**
- 95th percentile response time <5s
- Job throughput: 20 jobs/minute
- No memory leaks under sustained load
