package cloud.xcan.angus.core.tester.application.query.mock.impl;

import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.mock.MockApisQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockApisResponseQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceAuthQuery;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApis;
import cloud.xcan.angus.core.tester.domain.mock.apis.response.MockApisResponse;
import cloud.xcan.angus.core.tester.domain.mock.apis.response.MockApisResponseRepo;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Implementation of MockApisResponseQuery for managing Mock API response operations.
 * <p>
 * This class provides comprehensive functionality for querying and managing Mock API responses,
 * which define the response configurations and data for mock API endpoints.
 * It handles response retrieval, validation, and uniqueness checking.
 * <p>
 * Key features include:
 * <ul>
 *   <li>Mock API response retrieval with authorization validation</li>
 *   <li>Response name uniqueness validation for creation operations</li>
 *   <li>Batch response validation and conflict detection</li>
 *   <li>Authorization checks for response access</li>
 *   <li>Efficient duplicate detection and error handling</li>
 * </ul>
 * <p>
 * The implementation uses the BizTemplate pattern for consistent business logic handling
 * and proper error management across all operations.
 */
@Biz
public class MockApisResponseQueryImpl implements MockApisResponseQuery {

  @Resource
  private MockApisResponseRepo mockApisResponseRepo;
  @Resource
  private MockApisQuery mockApisQuery;
  @Resource
  private MockServiceAuthQuery mockServiceAuthQuery;

  /**
   * Retrieves all response configurations for a specific Mock API.
   * <p>
   * Fetches complete response configurations including response data, headers,
   * and status codes for comprehensive API response management.
   * <p>
   * The method performs authorization validation to ensure the current user has
   * permission to view responses for the specified Mock API.
   *
   * @param apisId the Mock API ID to retrieve responses for
   * @return List of MockApisResponse objects with complete response configurations
   * @throws ResourceNotFound if the Mock API is not found
   */
  @Override
  public List<MockApisResponse> findAllByApisId(Long apisId) {
    return new BizTemplate<List<MockApisResponse>>() {
      @Override
      protected void checkParams() {
        // Validate that the Mock API exists and retrieve it
        MockApis mockApis = mockApisQuery.checkAndFind(apisId);

        // Validate user authorization for viewing responses from the Mock API
        mockServiceAuthQuery.checkViewAuth(getUserId(), mockApis.getMockServiceId());
      }

      @Override
      protected List<MockApisResponse> process() {
        // Retrieve all response configurations for the specified Mock API
        return mockApisResponseRepo.findAllByMockApisId(apisId);
      }
    }.execute();
  }

  /**
   * Validates that Mock API response names do not already exist within their respective APIs.
   * <p>
   * Performs comprehensive validation to ensure response name uniqueness both within
   * the provided response list and against existing responses in the database.
   * <p>
   * The method handles batch validation efficiently by grouping responses by API
   * and performing duplicate detection at multiple levels.
   *
   * @param apisResponses0 list of Mock API responses to validate names for
   * @throws ResourceExisted if any response name already exists or conflicts
   */
  @Override
  public void checkAddResponseNameExists(List<MockApisResponse> apisResponses0) {
    if (isNotEmpty(apisResponses0)) {
      // Group responses by Mock API ID for efficient batch processing
      Map<Long, List<MockApisResponse>> apisResponsesMap = apisResponses0.stream()
          .collect(Collectors.groupingBy(MockApisResponse::getMockApisId));

      for (Entry<Long, List<MockApisResponse>> entry : apisResponsesMap.entrySet()) {
        // Check for duplicate names within the provided response list
        List<MockApisResponse> mockApisResponses = entry.getValue().stream()
            .filter(ObjectUtils.duplicateByKey(MockApisResponse::getName))
            .toList();
        if (isNotEmpty(mockApisResponses)) {
          throw ResourceExisted.of(mockApisResponses.get(0).getName(), "MockApisResponse");
        }

        // Check for conflicts with existing responses in the database
        List<String> names = entry.getValue().stream()
            .map(MockApisResponse::getName).toList();
        List<String> existedNames = mockApisResponseRepo
            .findNamesByMockApisIdAndNameIn(entry.getKey(), names);
        if (isNotEmpty(existedNames)) {
          throw ResourceExisted.of(existedNames.get(0), "MockApisResponse");
        }
      }
    }
  }

}
