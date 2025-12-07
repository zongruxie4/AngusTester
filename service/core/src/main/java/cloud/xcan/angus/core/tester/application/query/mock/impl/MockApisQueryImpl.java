package cloud.xcan.angus.core.tester.application.query.mock.impl;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceExisted;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.findFirstValue;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MOCK_APIS_ASSOC_APIS_EXISTED_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MOCK_APIS_NAME_EXISTED_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MOCK_APIS_OPERATION_EXISTED_T;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_OPENAPI_SUMMARY_LENGTH;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.stringSafe;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import cloud.xcan.angus.api.commonlink.setting.quota.QuotaResource;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.repository.summary.SummaryQueryRegister;
import cloud.xcan.angus.core.tester.application.query.apis.ApisQuery;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockApisQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceAuthQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceQuery;
import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.apis.ApisBaseInfo;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApis;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApisOperationP;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApisRepo;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApisSearchRepo;
import cloud.xcan.angus.core.tester.domain.mock.apis.response.MockApisResponse;
import cloud.xcan.angus.core.tester.domain.mock.apis.response.MockApisResponseRepo;
import cloud.xcan.angus.core.tester.domain.mock.service.MockService;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceInfo;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.spec.http.PathMatchers;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Implementation of MockApisQuery for managing Mock API operations and data retrieval.
 * <p>
 * This class provides comprehensive functionality for querying and managing Mock APIs, which
 * simulate real API endpoints for testing and development purposes. It handles API retrieval,
 * validation, quota management, and comprehensive data enrichment.
 * <p>
 * Key features include:
 * <ul>
 *   <li>Mock API detail retrieval with comprehensive data enrichment</li>
 *   <li>Paginated API listing with search capabilities and authorization checks</li>
 *   <li>Service-specific API filtering and analysis</li>
 *   <li>Full-text search support for API content</li>
 *   <li>Comprehensive validation for name and operation uniqueness</li>
 *   <li>Quota management and resource validation</li>
 *   <li>Associated API validation and consistency checking</li>
 *   <li>Safe clone name generation for API duplication</li>
 * </ul>
 * <p>
 * The implementation uses the BizTemplate pattern for consistent business logic handling
 * and proper error management across all operations. It also supports summary query
 * registration for analytics and reporting purposes.
 */
@Biz
@SummaryQueryRegister(name = "MockApis", table = "mock_apis",
    groupByColumns = {"created_date", "method", "source"},
    aggregateColumns = {"id", "request_num", "pushback_num", "simulate_error_num",
        "success_num", "exception_num"})
public class MockApisQueryImpl implements MockApisQuery {

  @Resource
  private MockApisRepo mockApisRepo;
  @Resource
  private MockApisResponseRepo apisResponseRepo;
  @Resource
  private MockApisSearchRepo mockApisSearchRepo;
  @Resource
  private CommonQuery commonQuery;
  @Resource
  private ApisQuery apisQuery;
  @Resource
  private MockServiceQuery mockServiceQuery;
  @Resource
  private MockServiceAuthQuery mockServiceAuthQuery;

  /**
   * Retrieves detailed information for a specific Mock API with comprehensive data enrichment.
   * <p>
   * Fetches complete API details including associated service information, response data, and
   * consistency validation with the original API definition.
   * <p>
   * The method performs authorization validation and enriches the API data with service
   * information, response configurations, and consistency checks.
   *
   * @param id the Mock API ID to retrieve details for
   * @return MockApis object with complete enriched details
   * @throws ResourceNotFound if the Mock API is not found
   */
  @Override
  public MockApis detail(Long id) {
    return new BizTemplate<MockApis>() {
      MockApis mockApisDb;
      MockServiceInfo mockServiceDb;
      ApisBaseInfo apisDb;

      @Override
      protected void checkParams() {
        // Validate that the Mock API exists and retrieve it
        mockApisDb = checkAndFind(id);

        // Validate user authorization for viewing the Mock API
        mockServiceAuthQuery.checkViewAuth(getUserId(), mockApisDb.getMockServiceId());

        // Validate that the associated mock service exists
        mockServiceDb = mockServiceQuery.checkAndFindInfo(mockApisDb.getMockServiceId());

        // Validate that the associated API exists if specified
        if (nonNull(mockApisDb.getAssocApisId())) {
          apisDb = apisQuery.findBaseByIdIn(mockApisDb.getAssocApisId());
        }
      }

      @Override
      protected MockApis process() {
        // Enrich Mock API with service information
        setMockServiceInfo(mockApisDb, mockServiceDb);

        // Check and set inconsistent operation information if associated API exists
        if (nonNull(mockApisDb.getAssocApisId())) {
          setInconsistentOperationInfo(mockApisDb, apisDb);
        }

        // Enrich Mock API with response configurations
        mockApisDb.setResponses(apisResponseRepo.findAllByMockApisId(id));

        // Set associated API deletion status for UI display
        mockApisDb.setAssocApisDeleted(isNull(mockApisDb.getAssocApisId()) || nonNull(apisDb));
        return mockApisDb;
      }
    }.execute();
  }

  /**
   * Retrieves Mock API information for OpenAPI integration with flexible filtering.
   * <p>
   * Supports both comprehensive API retrieval and specific API matching based on HTTP method and
   * endpoint patterns. Used primarily for OpenAPI integration and external service consumption.
   * <p>
   * The method handles path matching using pattern matchers and enriches API data with response
   * configurations for complete API representation.
   *
   * @param mockServiceId the mock service ID for filtering APIs
   * @param method        the HTTP method to filter by (optional)
   * @param endpoint      the endpoint path to filter by (optional)
   * @return List of MockApis objects with response configurations, or null if not found
   */
  @Override
  public List<MockApis> info0(Long mockServiceId, String method, String endpoint) {
    return new BizTemplate<List<MockApis>>() {
      @Override
      protected void checkParams() {
        // Note: No authorization check required for OpenAPI integration
      }

      @Override
      protected List<MockApis> process() {
        // Retrieve all operations for the mock service
        List<MockApisOperationP> operations = mockApisRepo
            .findAllOperationByMockServiceId(mockServiceId);
        if (isNotEmpty(operations)) {
          if (isEmpty(method) || isEmpty(endpoint)) {
            // Retrieve all Mock APIs for the service with response configurations
            List<MockApis> mockApis = mockApisRepo.findByMockServiceId(mockServiceId);
            if (isNotEmpty(mockApis)) {
              // Batch retrieve and group response configurations for efficiency
              Map<Long, List<MockApisResponse>> mockApisResponsesMap = apisResponseRepo
                  .findByMockServiceId(mockServiceId).stream()
                  .collect(Collectors.groupingBy(MockApisResponse::getMockApisId));
              if (isNotEmpty(mockApisResponsesMap)) {
                for (MockApis mockApi : mockApis) {
                  mockApi.setResponses(mockApisResponsesMap.get(mockApi.getId()));
                }
              }
            }
            return mockApis;
          } else {
            // Find specific Mock API by method and endpoint pattern matching
            Optional<MockApisOperationP> operation = operations.stream()
                .filter(x -> method.equalsIgnoreCase(x.getMethod())
                    && PathMatchers.getPathMatcher().match(x.getEndpoint(), endpoint))
                .findFirst();
            if (operation.isPresent()) {
              MockApis mockApis = checkAndFind(operation.get().getId());
              List<MockApisResponse> mockApisResponses = apisResponseRepo
                  .findAllByMockApisId(operation.get().getId());
              mockApis.setResponses(mockApisResponses);
              return List.of(mockApis);
            }
          }
        }
        return null;
      }
    }.execute();
  }

  /**
   * Retrieves a paginated list of Mock APIs with comprehensive filtering and enrichment.
   * <p>
   * Supports both regular search and full-text search with service-specific filtering. Provides
   * enriched API information including service details for comprehensive analysis.
   * <p>
   * The method performs authorization validation to ensure the current user has permission to view
   * APIs for the specified mock service.
   *
   * @param spec           the search specification with criteria and filters
   * @param pageable       pagination parameters (page, size, sort)
   * @param fullTextSearch whether to use full-text search capabilities
   * @param match          array of field names to include in full-text search
   * @return Page of MockApis objects with enriched service information
   */
  @Override
  public Page<MockApis> list(GenericSpecification<MockApis> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<MockApis>>() {
      MockServiceInfo mockServiceDb;

      @Override
      protected void checkParams() {
        // Extract mock service ID from search criteria for authorization validation
        String mockServiceId = findFirstValue(spec.getCriteria(), "mockServiceId");
        mockServiceAuthQuery.checkViewAuth(getUserId(), Long.parseLong(mockServiceId));

        // Validate that the associated mock service exists
        mockServiceDb = mockServiceQuery.checkAndFindInfo(Long.parseLong(mockServiceId));
      }

      @Override
      protected Page<MockApis> process() {
        // Execute search based on whether full-text search is enabled
        Page<MockApis> page = fullTextSearch
            ? mockApisSearchRepo.find(spec.getCriteria(), pageable, MockApis.class, match)
            : mockApisRepo.findAll(spec, pageable);
        if (page.isEmpty()) {
          return page;
        }

        // Enrich each Mock API with service information
        for (MockApis mockApis : page.getContent()) {
          setMockServiceInfo(mockApis, mockServiceDb);
        }
        return page;
      }
    }.execute();
  }

  /**
   * Validates that a Mock API exists and retrieves it.
   * <p>
   * Performs existence validation and throws ResourceNotFound if the API is not found. Used as a
   * helper method for other operations that require API validation.
   *
   * @param id the Mock API ID to validate and retrieve
   * @return MockApis object if found
   * @throws ResourceNotFound if the Mock API is not found
   */
  @Override
  public MockApis checkAndFind(Long id) {
    return mockApisRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "MockApis"));
  }

  /**
   * Validates that multiple Mock APIs exist and retrieves them.
   * <p>
   * Performs batch existence validation and throws ResourceNotFound if any API is not found. Used
   * for bulk operations that require multiple API validations.
   *
   * @param reqApisIds collection of Mock API IDs to validate and retrieve
   * @return List of MockApis objects for all found APIs
   * @throws ResourceNotFound if any Mock API is not found
   */
  @Override
  public List<MockApis> checkAndFind(Collection<Long> reqApisIds) {
    List<MockApis> services = mockApisRepo.findAllById(reqApisIds);
    Set<Long> serviceIds = services.stream().map(MockApis::getId).collect(Collectors.toSet());
    reqApisIds.removeAll(serviceIds);
    assertResourceNotFound(isEmpty(reqApisIds), reqApisIds, "MockApis");
    return services;
  }

  /**
   * Validates that Mock API names do not already exist within a service.
   * <p>
   * Checks for name uniqueness within the specified mock service to prevent duplicate API names
   * during creation operations.
   *
   * @param serviceId the mock service ID for scope validation
   * @param apis      list of Mock APIs to validate names for
   * @throws ResourceExisted if any API name already exists
   */
  @Override
  public void checkAddNameExists(Long serviceId, List<MockApis> apis) {
    List<String> existedNames = mockApisRepo.findSummariesByMockServiceIdAndSummaryIn(
        serviceId, apis.stream().map(MockApis::getName).collect(Collectors.toSet()));
    assertResourceExisted(existedNames.isEmpty(), existedNames, "MockApis");
  }

  /**
   * Validates that Mock API names do not conflict during update operations.
   * <p>
   * Checks for name uniqueness within the specified mock service, excluding the current API being
   * updated to allow name preservation.
   *
   * @param serviceId the mock service ID for scope validation
   * @param apis      list of Mock APIs to validate names for
   * @throws ResourceExisted if any API name conflicts with existing APIs
   */
  @Override
  public void checkUpdateNameExists(Long serviceId, List<MockApis> apis) {
    // Note: Update name is optional - skip validation if no names provided
    Set<String> reqNames = apis.stream().map(MockApis::getName).collect(Collectors.toSet());
    if (isEmpty(reqNames)) {
      return;
    }
    List<MockApis> existedApis = mockApisRepo
        .findAllByMockServiceIdAndSummaryIn(serviceId, reqNames);
    if (isNotEmpty(existedApis)) {
      for (MockApis existedApi : existedApis) {
        for (MockApis api : apis) {
          assertResourceExisted(!Objects.equals(existedApi.getName(), api.getName())
                  || existedApi.getId().equals(api.getId()), MOCK_APIS_NAME_EXISTED_T,
              new Object[]{api.getName()});
        }
      }
    }
  }

  /**
   * Validates that Mock API operations (method + endpoint) do not already exist.
   * <p>
   * Checks for operation uniqueness within the specified mock service to prevent duplicate API
   * operations during creation operations.
   *
   * @param serviceId the mock service ID for scope validation
   * @param apis      list of Mock APIs to validate operations for
   * @throws ResourceExisted if any API operation already exists
   */
  @Override
  public void checkAddOperationExists(Long serviceId, List<MockApis> apis) {
    // Note: Update method and endpoint is required for operation validation
    List<MockApis> existedApis = mockApisRepo.findByMockServiceIdAndEndpointIn(
        serviceId, apis.stream().map(MockApis::getEndpoint).collect(Collectors.toSet()));
    if (isNotEmpty(existedApis)) {
      for (MockApis existedApi : existedApis) {
        for (MockApis api : apis) {
          assertResourceExisted(!existedApi.sameIdentityAs(api),
              MOCK_APIS_OPERATION_EXISTED_T, new Object[]{api.getMethod(), api.getEndpoint()});
        }
      }
    }
  }

  /**
   * Validates that Mock API operations do not conflict during update operations.
   * <p>
   * Checks for operation uniqueness within the specified mock service, excluding the current API
   * being updated to allow operation preservation.
   *
   * @param serviceId the mock service ID for scope validation
   * @param apis      list of Mock APIs to validate operations for
   * @throws ResourceExisted if any API operation conflicts with existing APIs
   */
  @Override
  public void checkUpdateOperationExists(Long serviceId, List<MockApis> apis) {
    List<MockApis> existedApis = mockApisRepo.findByMockServiceIdAndEndpointIn(
        serviceId, apis.stream().map(MockApis::getEndpoint).collect(Collectors.toSet()));
    if (isNotEmpty(existedApis)) {
      for (MockApis existedApi : existedApis) {
        for (MockApis api : apis) {
          assertResourceExisted(!existedApi.sameIdentityAs(api)
                  || existedApi.getId().equals(api.getId()), MOCK_APIS_OPERATION_EXISTED_T,
              new Object[]{api.getMethod(), api.getEndpoint()});
        }
      }
    }
  }

  /**
   * Validates that the mock service has sufficient quota for additional APIs.
   * <p>
   * Checks the current API count against the service quota and validates that adding the specified
   * number of APIs would not exceed the limit.
   *
   * @param mockServiceDb the mock service to check quota for
   * @param incr          the number of APIs to be added
   * @throws QuotaExceeded if the quota limit would be exceeded
   */
  @Override
  public void checkServiceApisQuota(MockService mockServiceDb, int incr) {
    long count = mockApisRepo.countAllByMockServiceId(mockServiceDb.getId());
    commonQuery.checkTenantQuota(QuotaResource.AngusTesterMockServiceApis,
        Set.of(mockServiceDb.getId()), incr + count);
  }

  /**
   * Validates that an API is not already associated with another Mock API.
   * <p>
   * Ensures that APIs can only be associated with one Mock API to prevent conflicts and maintain
   * data integrity.
   *
   * @param apisDb the API to validate association for
   * @throws ResourceExisted if the API is already associated with another Mock API
   */
  @Override
  public void checkAssocApisExists(Apis apisDb) {
    ProtocolAssert.assertTrue(!mockApisRepo.existsByAssocApisId(apisDb.getId()),
        MOCK_APIS_ASSOC_APIS_EXISTED_T, new Object[]{apisDb.getName()});
  }

  /**
   * Validates that an API base info is not already associated with another Mock API.
   * <p>
   * Ensures that APIs can only be associated with one Mock API to prevent conflicts and maintain
   * data integrity.
   *
   * @param apisDb the API base info to validate association for
   * @throws ResourceExisted if the API is already associated with another Mock API
   */
  @Override
  public void checkAssocApisExists(ApisBaseInfo apisDb) {
    ProtocolAssert.assertTrue(!mockApisRepo.existsByAssocApisId(apisDb.getId()),
        MOCK_APIS_ASSOC_APIS_EXISTED_T, new Object[]{apisDb.getName()});
  }

  /**
   * Validates that multiple APIs are not already associated with other Mock APIs.
   * <p>
   * Performs batch validation to ensure that all specified APIs can be associated with Mock APIs
   * without conflicts.
   *
   * @param mockApis list of Mock APIs to validate associations for
   * @throws ResourceExisted  if any API is already associated with another Mock API
   * @throws ResourceNotFound if any specified API does not exist
   */
  @Override
  public void checkAssocApissExists(List<MockApis> mockApis) {
    List<Long> apisIds = mockApis.stream().filter(x -> nonNull(x.getAssocApisId()))
        .map(MockApis::getAssocApisId).toList();
    if (isNotEmpty(apisIds)) {
      List<ApisBaseInfo> apis = apisQuery.checkAndFindBaseInfos(apisIds);
      if (isNotEmpty(apis)) {
        for (ApisBaseInfo api : apis) {
          ProtocolAssert.assertTrue(!mockApisRepo.existsByAssocApisId(api.getId()),
              MOCK_APIS_ASSOC_APIS_EXISTED_T, new Object[]{api.getName()});
        }
      } else {
        throw ResourceNotFound.of(apisIds.get(0), "Apis");
      }
    }
  }

  /**
   * Generates a safe clone name for Mock API duplication.
   * <p>
   * Creates a unique name for cloned APIs by appending "-Copy" and ensuring the name does not
   * exceed maximum length constraints while maintaining uniqueness.
   * <p>
   * The method also generates a unique endpoint for the cloned API to prevent operation conflicts.
   *
   * @param apis the Mock API to generate a clone name for
   */
  @Override
  public void setSafeCloneName(MockApis apis) {
    String saltName = randomAlphanumeric(3);
    String clonedName = mockApisRepo.existsByMockServiceIdAndSummary(apis.getMockServiceId(),
        apis.getName() + "-Copy") ? apis.getName() + "-Copy." + saltName : apis.getName() + "-Copy";
    clonedName = clonedName.length() > MAX_OPENAPI_SUMMARY_LENGTH ? clonedName.substring(0,
        MAX_OPENAPI_SUMMARY_LENGTH - 3) + saltName : clonedName;
    apis.setSummary(clonedName);
    apis.setEndpoint(stringSafe(apis.getEndpoint()) + "-Copy." + saltName);
  }

  /**
   * Enriches Mock API with service information for display and analysis.
   * <p>
   * Sets service-related fields including name, domain URL, and host URL to provide complete
   * context for the Mock API.
   *
   * @param mockApisDb    the Mock API to enrich with service information
   * @param mockServiceDb the service information to apply
   */
  @Override
  public void setMockServiceInfo(MockApis mockApisDb, MockServiceInfo mockServiceDb) {
    mockApisDb.setMockServiceName(mockServiceDb.getName())
        .setMockServiceDomainUrl(mockServiceDb.getServiceDomainUrl())
        .setMockServiceHostUrl(mockServiceDb.getServiceHostUrl());
  }

  /**
   * Checks and sets inconsistent operation information between Mock API and associated API.
   * <p>
   * Compares the Mock API's method and endpoint with the associated API's definition and flags
   * inconsistencies for UI display and user awareness.
   *
   * @param mockApisDb the Mock API to check for inconsistencies
   * @param apisDb     the associated API to compare against
   */
  private void setInconsistentOperationInfo(MockApis mockApisDb, ApisBaseInfo apisDb) {
    if (!Objects.equals(apisDb.getEndpoint(), mockApisDb.getEndpoint()) ||
        !Objects.equals(apisDb.getMethod(), mockApisDb.getMethod())) {
      mockApisDb.setInconsistentOperation(true)
          .setApisEndpoint(apisDb.getEndpoint()).setApisMethod(apisDb.getMethod());
    } else {
      mockApisDb.setInconsistentOperation(false);
    }
  }
}
