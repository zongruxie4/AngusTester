package cloud.xcan.angus.core.tester.application.query.issue.impl;

import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.issue.TaskFavouriteQuery;
import cloud.xcan.angus.core.tester.domain.issue.favorite.TaskFavouriteP;
import cloud.xcan.angus.core.tester.domain.issue.favorite.TaskFavouriteRepo;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * <p>
 * Implementation of TaskFavouriteQuery for task favorite management and query operations.
 * </p>
 * <p>
 * Provides methods for querying user's favorite tasks with pagination and search capabilities.
 * </p>
 */
@Biz
public class TaskFavouriteQueryImpl implements TaskFavouriteQuery {

  @Resource
  private TaskFavouriteRepo taskFavouriteRepo;

  /**
   * <p>
   * List user's favorite tasks with pagination and optional name filtering.
   * </p>
   * <p>
   * Retrieves paginated favorite tasks for the current user within a project.
   * Supports optional name-based filtering for search functionality.
   * </p>
   * @param projectId Project ID
   * @param name Optional name filter for search
   * @param pageable Pagination information
   * @return Page of favorite tasks
   */
  @Override
  public Page<TaskFavouriteP> list(Long projectId, String name, PageRequest pageable) {
    return new BizTemplate<Page<TaskFavouriteP>>() {

      @Override
      protected Page<TaskFavouriteP> process() {
        return isEmpty(name) ? taskFavouriteRepo.search(projectId, getUserId(), pageable)
            : taskFavouriteRepo.searchByMatch(projectId, getUserId(), name, pageable);
      }
    }.execute();
  }

  /**
   * <p>
   * Count user's favorite tasks.
   * </p>
   * <p>
   * Returns the total count of favorite tasks for the current user.
   * If projectId is provided, counts only favorites within that project.
   * </p>
   * @param projectId Optional project ID to filter by
   * @return Count of favorite tasks
   */
  @Override
  public Long count(Long projectId) {
    return new BizTemplate<Long>() {

      @Override
      protected Long process() {
        return isNull(projectId) ? taskFavouriteRepo.countByCreatedBy(getUserId())
            : taskFavouriteRepo.countByProjectIdAndCreatedBy(projectId, getUserId());
      }
    }.execute();
  }
}
