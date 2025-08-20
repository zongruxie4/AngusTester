package cloud.xcan.angus.core.tester.application.query.apis.impl;

import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.APIS_UNARCHIVED_NO_PERMISSION_CODE;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.APIS_UNARCHIVED_NO_PERMISSION_T;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizAssert;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.repository.summary.SummaryQueryRegister;
import cloud.xcan.angus.core.tester.application.converter.ApisConverter;
import cloud.xcan.angus.core.tester.application.query.apis.ApisUnarchivedQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.domain.apis.unarchived.ApisUnarchived;
import cloud.xcan.angus.core.tester.domain.apis.unarchived.ApisUnarchivedListRepo;
import cloud.xcan.angus.core.tester.domain.apis.unarchived.ApisUnarchivedRepo;
import cloud.xcan.angus.core.tester.domain.apis.unarchived.ApisUnarchivedSearchRepo;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Implementation of unarchived API query operations for personal API management.
 * 
 * <p>This class provides comprehensive functionality for querying and managing
 * unarchived APIs, including personal API storage, pagination, search, and validation.</p>
 * 
 * <p>It handles personal API lifecycle management, project association,
 * and user-specific API organization with proper permission validation.</p>
 * 
 * <p>Key features include:
 * <ul>
 *   <li>Unarchived API detail and list queries with pagination</li>
 *   <li>Full-text search capabilities for API content</li>
 *   <li>Personal API count and statistics</li>
 *   <li>Project member permission validation</li>
 *   <li>User-specific API filtering and access control</li>
 *   <li>Summary query registration for reporting</li>
 *   <li>Update permission validation</li>
 * </ul></p>
 */
@Biz
@Slf4j
@SummaryQueryRegister(name = "ApisUnarchived", table = "apis_unarchived",
    groupByColumns = {"created_date", "method"})
public class ApisUnarchivedQueryImpl implements ApisUnarchivedQuery {

  @Resource
  private ApisUnarchivedRepo apisUnarchivedRepo;
  @Resource
  private ApisUnarchivedListRepo apisUnarchivedListRepo;
  @Resource
  private ApisUnarchivedSearchRepo apisUnarchivedSearchRepo;
  @Resource
  private ProjectMemberQuery projectMemberQuery;

  /**
   * Retrieves detailed unarchived API information.
   * 
   * <p>This method fetches detailed information for an unarchived API,
   * ensuring the API belongs to the current user.</p>
   * 
   * @param id the unarchived API ID to retrieve details for
   * @return the detailed unarchived API information
   * @throws ResourceNotFound if the API is not found or doesn't belong to current user
   */
  @Override
  public ApisUnarchived detail(Long id) {
    return new BizTemplate<ApisUnarchived>() {

      @Override
      protected ApisUnarchived process() {
        // Retrieve unarchived API with ownership validation
        return checkAndFind(id);
      }
    }.execute();
  }

  /**
   * Counts unarchived APIs for the current user with optional project filtering.
   * 
   * <p>This method returns the total number of unarchived APIs owned by the current user,
   * optionally filtered by project ID.</p>
   * 
   * @param projectId the project ID to filter count by (can be null for all projects)
   * @return the total number of unarchived APIs for the current user
   */
  @Override
  public Long count(Long projectId) {
    return new BizTemplate<Long>() {

      @Override
      protected Long process() {
        // Count unarchived APIs with or without project filtering for current user
        return isNull(projectId) ? apisUnarchivedRepo.countByCreatedBy(getUserId())
            : apisUnarchivedRepo.countByProjectIdAndCreatedBy(projectId, getUserId());
      }
    }.execute();
  }

  /**
   * Lists unarchived APIs with pagination, filtering, and optional full-text search.
   * 
   * <p>This method retrieves unarchived APIs owned by the current user based on
   * specification criteria with support for pagination and optional full-text search.</p>
   * 
   * <p>The method validates project member permissions and automatically filters
   * results to show only APIs owned by the current user.</p>
   * 
   * @param spec the specification for filtering unarchived APIs
   * @param pageable the pagination and sorting parameters
   * @param fullTextSearch whether to use full-text search
   * @param match the full-text search match fields
   * @return a page of unarchived APIs owned by the current user
   */
  @Override
  public Page<ApisUnarchived> list(GenericSpecification<ApisUnarchived> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<ApisUnarchived>>() {
      @Override
      protected void checkParams() {
        // Verify user has project member permissions
        projectMemberQuery.checkMember(spec.getCriteria());
      }

      @Override
      protected Page<ApisUnarchived> process() {
        // Add current user filter to ensure only user's APIs are returned
        Set<SearchCriteria> criteria = spec.getCriteria();
        criteria.add(SearchCriteria.equal("createdBy", getUserId()));
        
        // Execute search with full-text or standard search
        return fullTextSearch
            ? apisUnarchivedSearchRepo.find(criteria, pageable, ApisUnarchived.class,
            ApisConverter::objectArrToApisUnarchived, match)
            : apisUnarchivedListRepo.find(criteria, pageable, ApisUnarchived.class,
                ApisConverter::objectArrToApisUnarchived, null);
      }
    }.execute();
  }

  @Override
  public ApisUnarchived checkAndFind(Long id) {
    return apisUnarchivedRepo.findByCreatedByAndId(getUserId(), id)
        .orElseThrow(() -> ResourceNotFound.of(id, "ApisUnarchived"));
  }

  /**
   * Allow only self modification of not archived apis.
   */
  @Override
  public void checkUpdateApiPermission(List<ApisUnarchived> apis) {
    List<Long> existIds = apisUnarchivedRepo.findApisUnarchiveCreateBy(
        apis.stream().map(ApisUnarchived::getId).collect(Collectors.toSet()), getUserId());

    List<Long> requestIds = apis.stream().map(ApisUnarchived::getId).toList();
    requestIds.removeAll(existIds);
    BizAssert.assertTrue(isEmpty(requestIds), APIS_UNARCHIVED_NO_PERMISSION_CODE,
        APIS_UNARCHIVED_NO_PERMISSION_T, new Object[]{requestIds});
  }

}
