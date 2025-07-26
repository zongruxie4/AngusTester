package cloud.xcan.angus.core.tester.application.query.apis.impl;

import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static java.util.Objects.isNull;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.apis.ApisFollowQuery;
import cloud.xcan.angus.core.tester.domain.apis.follow.ApisFollowP;
import cloud.xcan.angus.core.tester.domain.apis.follow.ApisFollowRepo;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Implementation of API follow query operations for subscription management.
 * 
 * <p>This class provides functionality for querying and managing
 * API follows, including follow listing, counting, and search.</p>
 * 
 * <p>It handles user-specific follow management with project-based
 * filtering and name-based search capabilities.</p>
 * 
 * <p>Key features include:
 * <ul>
 *   <li>API follow listing with pagination</li>
 *   <li>Name-based search for follows</li>
 *   <li>Project-based follow counting</li>
 *   <li>User-specific follow filtering</li>
 * </ul></p>
 * 
 * @author XiaoLong Liu
 */
@Biz
public class ApisFollowQueryImpl implements ApisFollowQuery {

  @Resource
  private ApisFollowRepo apisFollowRepo;

  /**
   * Lists API follows with optional project and name filtering.
   * 
   * <p>This method retrieves API follows for the current user with support
   * for project-based filtering and name-based search.</p>
   * 
   * @param projectId the project ID to filter follows by (can be null for all projects)
   * @param name the API name to search for (can be null for all APIs)
   * @param pageable the pagination and sorting parameters
   * @return a page of API follows for the current user
   */
  @Override
  public Page<ApisFollowP> list(Long projectId, String name, PageRequest pageable) {
    return new BizTemplate<Page<ApisFollowP>>() {

      @Override
      protected Page<ApisFollowP> process() {
        // Execute search with or without name filtering
        return isEmpty(name) ? apisFollowRepo.search(projectId, getUserId(), pageable)
            : apisFollowRepo.searchByMatch(projectId, getUserId(), name, pageable);
      }
    }.execute();
  }

  /**
   * Counts API follows for the current user with optional project filtering.
   * 
   * <p>This method returns the total number of API follows for the current user,
   * optionally filtered by project.</p>
   * 
   * @param projectId the project ID to filter count by (can be null for all projects)
   * @return the total number of API follows for the current user
   */
  @Override
  public Long count(Long projectId) {
    return new BizTemplate<Long>() {

      @Override
      protected Long process() {
        // Count follows with or without project filtering
        return isNull(projectId) ? apisFollowRepo.countByCreatedBy(getUserId())
            : apisFollowRepo.countByProjectIdAndCreatedBy(projectId, getUserId());
      }
    }.execute();
  }
}




