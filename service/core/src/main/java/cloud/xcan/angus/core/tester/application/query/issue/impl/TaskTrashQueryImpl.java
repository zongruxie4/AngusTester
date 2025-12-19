package cloud.xcan.angus.core.tester.application.query.issue.impl;

import static cloud.xcan.angus.api.commonlink.TesterApisMessage.TRASH_NO_BACK_PERMISSION;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.TRASH_NO_BACK_PERMISSION_CODE;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.TRASH_NO_CLEAR_PERMISSION;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.TRASH_NO_CLEAR_PERMISSION_CODE;
import static cloud.xcan.angus.core.tester.application.query.common.impl.CommonQueryImpl.isAdmin;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static java.util.Objects.isNull;

import cloud.xcan.angus.api.manager.UserManager;
import org.springframework.stereotype.Service;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.exception.BizException;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.issue.TaskTrashQuery;
import cloud.xcan.angus.core.tester.domain.issue.trash.TaskTrash;
import cloud.xcan.angus.core.tester.domain.issue.trash.TaskTrashRepo;
import cloud.xcan.angus.core.tester.domain.issue.trash.TaskTrashSearchRepo;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * <p>
 * Implementation of TaskTrashQuery for task trash management and query operations.
 * </p>
 * <p>
 * Provides comprehensive trash management services including counting, listing, and permission
 * validation for deleted tasks. Supports full-text search, pagination, and user-specific access
 * control for trash operations.
 * </p>
 */
@Service
public class TaskTrashQueryImpl implements TaskTrashQuery {

  /**
   * Business operation constants for trash management
   */
  private static final String BIZ_OPERATION_BACK = "BACK";
  private static final String BIZ_OPERATION_CLEAR = "CLEAR";

  @Resource
  private TaskTrashRepo taskTrashRepo;
  @Resource
  private TaskTrashSearchRepo taskTrashSearchRepo;
  @Resource
  private UserManager userManager;

  /**
   * <p>
   * Count the number of tasks in trash.
   * </p>
   * <p>
   * Returns the total count of deleted tasks, optionally filtered by project ID. If projectId is
   * null, returns count for all projects.
   * </p>
   *
   * @param projectId Optional project ID to filter by
   * @return Number of tasks in trash
   */
  @Override
  public Long count(Long projectId) {
    return new BizTemplate<Long>() {
      @Override
      protected Long process() {
        return isNull(projectId) ? taskTrashRepo.count()
            : taskTrashRepo.countByProjectId(projectId);
      }
    }.execute();
  }

  /**
   * <p>
   * Get paginated list of tasks in trash with filtering and search capabilities.
   * </p>
   * <p>
   * Retrieves deleted tasks based on specification criteria with support for full-text search. Sets
   * user names and avatars for both creators and deleters of the tasks.
   * </p>
   *
   * @param spec           Generic specification for filtering
   * @param pageable       Pagination parameters
   * @param fullTextSearch Whether to use full-text search
   * @param match          Search match patterns
   * @return Paginated results of deleted tasks with user information
   */
  @Override
  public Page<TaskTrash> list(GenericSpecification<TaskTrash> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<TaskTrash>>() {

      @Override
      protected Page<TaskTrash> process() {
        // Execute search based on full-text search flag
        Page<TaskTrash> page = fullTextSearch
            ? taskTrashSearchRepo.find(spec.getCriteria(), pageable, TaskTrash.class, match)
            : taskTrashRepo.findAll(spec, pageable);

        // Set user information for non-empty results
        if (!page.isEmpty()) {
          // Set creator information (name and avatar)
          userManager.setUserNameAndAvatar(page.getContent(), "createdBy", "creator",
              "creatorAvatar");
          // Set deleter information (name and avatar)
          userManager.setUserNameAndAvatar(page.getContent(), "deletedBy", "deletedByName",
              "deletedByAvatar");
        }
        return page;
      }
    }.execute();
  }

  /**
   * <p>
   * Find a specific task in trash with business operation permission validation.
   * </p>
   * <p>
   * Retrieves a deleted task and validates user permissions for specific business operations (BACK
   * or CLEAR). Non-admin users can only access tasks they deleted themselves.
   * </p>
   *
   * @param id  Task trash ID
   * @param biz Business operation type ("BACK" or "CLEAR")
   * @return Task trash entity if found and user has permission
   * @throws ResourceNotFound if the task is not found
   * @throws BizException     if user lacks permission for the operation
   */
  @Override
  public TaskTrash findMyTrashForBiz(Long id, String biz) {
    // Retrieve the task from trash
    TaskTrash trashDb = taskTrashRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "TaskTrash"));

    // Check permissions for non-admin users
    if (!isAdmin() && ObjectUtils.notEqual(trashDb.getDeletedBy(), getUserId())) {
      // Validate specific business operation permissions
      switch (biz) {
        case BIZ_OPERATION_BACK:
          throw BizException.of(TRASH_NO_BACK_PERMISSION_CODE, TRASH_NO_BACK_PERMISSION);
        case BIZ_OPERATION_CLEAR:
          throw BizException.of(TRASH_NO_CLEAR_PERMISSION_CODE, TRASH_NO_CLEAR_PERMISSION);
        default:
          // For unknown business operations, allow access (admin users can always access)
          break;
      }
    }

    return trashDb;
  }
}
