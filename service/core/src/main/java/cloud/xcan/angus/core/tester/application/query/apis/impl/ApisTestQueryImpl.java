package cloud.xcan.angus.core.tester.application.query.apis.impl;

import static cloud.xcan.angus.core.tester.application.converter.ApisTestConverter.assembleApisTestCount;
import static cloud.xcan.angus.core.tester.application.converter.ApisTestConverter.assembleTestApisCount;
import static cloud.xcan.angus.core.utils.CoreUtils.getCommonDeletedResourcesStatsFilter;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.apis.ApisTestQuery;
import cloud.xcan.angus.core.tester.domain.apis.ApisBaseInfo;
import cloud.xcan.angus.core.tester.domain.apis.ApisBaseInfoRepo;
import cloud.xcan.angus.core.tester.domain.kanban.TestApisCount;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.model.services.ApisTestCount;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

/**
 * Implementation of API test query operations for testing statistics and management.
 * 
 * <p>This class provides comprehensive functionality for querying and analyzing
 * API testing data, including test type validation, test statistics, and result counting.</p>
 * 
 * <p>It handles test type management, service and project test statistics,
 * and comprehensive test result analysis with proper data aggregation.</p>
 * 
 * <p>Key features include:
 * <ul>
 *   <li>Enabled test type discovery for APIs</li>
 *   <li>Service-level test API counting and statistics</li>
 *   <li>Project-level test API counting and statistics</li>
 *   <li>Test result counting and analysis</li>
 *   <li>Creator-based filtering and date range analysis</li>
 *   <li>Functional, performance, and stability test support</li>
 *   <li>Comprehensive test data aggregation</li>
 * </ul></p>
 * 
 * @author XiaoLong Liu
 */
@Biz
public class ApisTestQueryImpl implements ApisTestQuery {

  @Resource
  private ApisBaseInfoRepo apisBaseInfoRepo;

  @Resource
  private UserManager userManager;

  /**
   * Finds enabled test types for an API.
   * 
   * <p>This method determines which test types are enabled for a specific API
   * based on the API's test configuration flags.</p>
   * 
   * @param apisId the API ID to find enabled test types for
   * @return list of enabled test types for the API
   */
  @Override
  public List<TestType> findEnabledTestTypes(Long apisId) {
    return new BizTemplate<List<TestType>>() {
      ApisBaseInfo apisDb;

      @Override
      protected void checkParams() {
        apisDb = apisBaseInfoRepo.findById(apisId)
            .orElseThrow(() -> ResourceNotFound.of(apisId, "Apis"));
      }

      @Override
      protected List<TestType> process() {
        // Determine enabled test types based on API configuration
        List<TestType> enabledTestTypes = new ArrayList<>();
        if (nonNull(apisDb.getTestFunc()) && apisDb.getTestFunc()) {
          enabledTestTypes.add(TestType.FUNCTIONAL);
        }
        if (nonNull(apisDb.getTestPerf()) && apisDb.getTestPerf()) {
          enabledTestTypes.add(TestType.PERFORMANCE);
        }
        if (nonNull(apisDb.getTestStability()) && apisDb.getTestStability()) {
          enabledTestTypes.add(TestType.STABILITY);
        }
        return enabledTestTypes;
      }
    }.execute();
  }

  /**
   * Counts test APIs for a specific service with filtering criteria.
   * 
   * <p>This method calculates test API statistics for a service based on
   * creator type, creator ID, and date range filters.</p>
   * 
   * @param serviceId the service ID to count test APIs for
   * @param creatorObjectType the type of creator object for filtering
   * @param creatorObjectId the creator object ID for filtering
   * @param createdDateStart the start date for filtering
   * @param createdDateEnd the end date for filtering
   * @return comprehensive test API count statistics for the service
   */
  @Override
  public ApisTestCount countServiceTestApis(Long serviceId, AuthObjectType creatorObjectType,
      Long creatorObjectId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd) {
    return new BizTemplate<ApisTestCount>() {

      @Override
      protected ApisTestCount process() {
        // Initialize test count statistics
        ApisTestCount count = new ApisTestCount();

        // Retrieve APIs for the service with filtering criteria
        List<ApisBaseInfo> apis = getServiceApisBaseInfos(serviceId, creatorObjectType,
            creatorObjectId, createdDateStart, createdDateEnd);
        if (isNotEmpty(apis)) {
          // Assemble comprehensive test count statistics
          assembleApisTestCount(count, apis);
        }
        return count;
      }
    }.execute();
  }

  /**
   * Counts test APIs for a specific project with filtering criteria.
   * 
   * <p>This method calculates test API statistics for a project based on
   * creator type, creator ID, and date range filters.</p>
   * 
   * @param projectId the project ID to count test APIs for
   * @param creatorObjectType the type of creator object for filtering
   * @param creatorObjectId the creator object ID for filtering
   * @param createdDateStart the start date for filtering
   * @param createdDateEnd the end date for filtering
   * @return comprehensive test API count statistics for the project
   */
  @Override
  public ApisTestCount countProjectTestApis(Long projectId, AuthObjectType creatorObjectType,
      Long creatorObjectId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd) {
    return new BizTemplate<ApisTestCount>() {

      @Override
      protected ApisTestCount process() {
        ApisTestCount count = new ApisTestCount();

        List<ApisBaseInfo> apis = getProjectApisBaseInfos(projectId, creatorObjectType,
            creatorObjectId, createdDateStart, createdDateEnd);
        if (isNotEmpty(apis)) {
          assembleApisTestCount(count, apis);
        }
        return count;
      }
    }.execute();
  }

  /**
   * Counts test results for APIs in a project with filtering criteria.
   * 
   * <p>This method calculates test result statistics for APIs in a project based on
   * creator type, creator ID, and date range filters.</p>
   * 
   * @param projectId the project ID to count test results for
   * @param creatorObjectType the type of creator object for filtering
   * @param creatorObjectId the creator object ID for filtering
   * @param createdDateStart the start date for filtering
   * @param createdDateEnd the end date for filtering
   * @return comprehensive test result count statistics for the project
   */
  @Override
  public TestApisCount countTestResult(Long projectId, AuthObjectType creatorObjectType,
      Long creatorObjectId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd) {
    return new BizTemplate<TestApisCount>() {

      @Override
      protected TestApisCount process() {
        TestApisCount count = new TestApisCount();

        List<ApisBaseInfo> apis = getProjectApisBaseInfos(projectId, creatorObjectType,
            creatorObjectId, createdDateStart, createdDateEnd);
        if (isNotEmpty(apis)) {
          assembleTestApisCount(count, apis);
        }
        return count;
      }
    }.execute();
  }

  /**
   * Retrieves API base information for a service with filtering criteria.
   * 
   * <p>This method fetches API base information for a specific service
   * with optional filtering by creator and date range.</p>
   * 
   * @param serviceId the service ID to get APIs for
   * @param creatorObjectType the type of creator object for filtering
   * @param creatorObjectId the creator object ID for filtering
   * @param createdDateStart the start date for filtering
   * @param createdDateEnd the end date for filtering
   * @return list of API base information for the service
   */
  private List<ApisBaseInfo> getServiceApisBaseInfos(Long serviceId,
      AuthObjectType creatorObjectType, Long creatorObjectId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd) {
    Set<Long> createdBys = null;
    if (nonNull(creatorObjectType) && nonNull(creatorObjectId)) {
      createdBys = userManager.getUserIdByOrgType0(creatorObjectType, creatorObjectId);
    }

    Set<SearchCriteria> allFilters = getCommonDeletedResourcesStatsFilter(null,
        createdDateStart, createdDateEnd, createdBys);
    allFilters.add(SearchCriteria.equal("serviceId", serviceId));

    return apisBaseInfoRepo.findAllByFilters(allFilters, Sort.by(Order.desc("id")));
  }

  /**
   * Retrieves API base information for a project with filtering criteria.
   * 
   * <p>This method fetches API base information for a specific project
   * with optional filtering by creator and date range.</p>
   * 
   * @param projectId the project ID to get APIs for
   * @param creatorObjectType the type of creator object for filtering
   * @param creatorObjectId the creator object ID for filtering
   * @param createdDateStart the start date for filtering
   * @param createdDateEnd the end date for filtering
   * @return list of API base information for the project
   */
  private List<ApisBaseInfo> getProjectApisBaseInfos(Long projectId,
      AuthObjectType creatorObjectType, Long creatorObjectId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd) {
    Set<Long> createdBys = null;
    if (nonNull(creatorObjectType) && nonNull(creatorObjectId)) {
      createdBys = userManager.getUserIdByOrgType0(creatorObjectType, creatorObjectId);
    }

    Set<SearchCriteria> allFilters = getCommonDeletedResourcesStatsFilter(projectId,
        createdDateStart, createdDateEnd, createdBys);
    return apisBaseInfoRepo.findAllByFilters(allFilters, Sort.by(Order.desc("id")));
  }

}
