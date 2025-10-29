package cloud.xcan.angus.core.tester.application.query.issue.impl;

import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.issue.TaskFollowQuery;
import cloud.xcan.angus.core.tester.domain.issue.follow.TaskFollowP;
import cloud.xcan.angus.core.tester.domain.issue.follow.TaskFollowRepo;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * <p>
 * Implementation of TaskFollowQuery for task follow management and query operations.
 * </p>
 * <p>
 * Provides methods for querying user's followed tasks with pagination and search capabilities.
 * </p>
 */
@Biz
public class TaskFollowQueryImpl implements TaskFollowQuery {

  @Resource
  private TaskFollowRepo taskFollowRepo;

  /**
   * <p>
   * List user's followed tasks with pagination and optional name filtering.
   * </p>
   * <p>
   * Retrieves paginated followed tasks for the current user within a project.
   * Supports optional name-based filtering for search functionality.
   * </p>
   * @param projectId Project ID
   * @param name Optional name filter for search
   * @param pageable Pagination information
   * @return Page of followed tasks
   */
  @Override
  public Page<TaskFollowP> list(Long projectId, String name, PageRequest pageable) {
    return new BizTemplate<Page<TaskFollowP>>() {

      @Override
      protected Page<TaskFollowP> process() {
        return isEmpty(name) ? taskFollowRepo.search(projectId, getUserId(), pageable)
            : taskFollowRepo.searchByMatch(projectId, getUserId(), name, pageable);
      }
    }.execute();
  }

  /**
   * <p>
   * Count user's followed tasks.
   * </p>
   * <p>
   * Returns the total count of followed tasks for the current user.
   * If projectId is provided, counts only followed tasks within that project.
   * </p>
   * @param projectId Optional project ID to filter by
   * @return Count of followed tasks
   */
  @Override
  public Long count(Long projectId) {
    return new BizTemplate<Long>() {

      @Override
      protected Long process() {
        return isNull(projectId) ? taskFollowRepo.countByCreatedBy(getUserId())
            : taskFollowRepo.countByProjectIdAndCreatedBy(projectId, getUserId());
      }
    }.execute();
  }

}
