package cloud.xcan.angus.core.tester.application.query.apis.impl;

import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static java.util.Objects.isNull;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.apis.ApisFavouriteQuery;
import cloud.xcan.angus.core.tester.domain.apis.favourite.ApisFavouriteP;
import cloud.xcan.angus.core.tester.domain.apis.favourite.ApisFavouriteRepo;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Implementation of API favourite query operations for bookmark management.
 * 
 * <p>This class provides functionality for querying and managing
 * API favourites, including favourite listing, counting, and search.</p>
 * 
 * <p>It handles user-specific favourite management with project-based
 * filtering and name-based search capabilities.</p>
 * 
 * <p>Key features include:
 * <ul>
 *   <li>API favourite listing with pagination</li>
 *   <li>Name-based search for favourites</li>
 *   <li>Project-based favourite counting</li>
 *   <li>User-specific favourite filtering</li>
 * </ul></p>
 * 
 * @author XiaoLong Liu
 */
@Biz
public class ApisFavouriteQueryImpl implements ApisFavouriteQuery {

  @Resource
  private ApisFavouriteRepo apisFavouriteRepo;

  /**
   * Lists API favourites with optional project and name filtering.
   * 
   * <p>This method retrieves API favourites for the current user with support
   * for project-based filtering and name-based search.</p>
   * 
   * @param projectId the project ID to filter favourites by (can be null for all projects)
   * @param apisName the API name to search for (can be null for all APIs)
   * @param pageable the pagination and sorting parameters
   * @return a page of API favourites for the current user
   */
  @Override
  public Page<ApisFavouriteP> list(Long projectId, String apisName, PageRequest pageable) {
    return new BizTemplate<Page<ApisFavouriteP>>() {

      @Override
      protected Page<ApisFavouriteP> process() {
        // Execute search with or without name filtering
        return isEmpty(apisName) ? apisFavouriteRepo.search(projectId, getUserId(), pageable)
            : apisFavouriteRepo.searchByMatch(projectId, getUserId(), apisName, pageable);
      }
    }.execute();
  }

  /**
   * Counts API favourites for the current user with optional project filtering.
   * 
   * <p>This method returns the total number of API favourites for the current user,
   * optionally filtered by project.</p>
   * 
   * @param projectId the project ID to filter count by (can be null for all projects)
   * @return the total number of API favourites for the current user
   */
  @Override
  public Long count(Long projectId) {
    return new BizTemplate<Long>() {

      @Override
      protected Long process() {
        // Count favourites with or without project filtering
        return isNull(projectId) ? apisFavouriteRepo.countByCreatedBy(getUserId())
            : apisFavouriteRepo.countByProjectIdAndCreatedBy(projectId, getUserId());
      }
    }.execute();
  }

}




