package cloud.xcan.angus.core.tester.application.query.test.impl;

import static cloud.xcan.angus.api.commonlink.TesterApisMessage.TRASH_NO_BACK_PERMISSION;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.TRASH_NO_BACK_PERMISSION_CODE;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.TRASH_NO_CLEAR_PERMISSION;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.TRASH_NO_CLEAR_PERMISSION_CODE;
import static cloud.xcan.angus.core.tester.application.query.common.impl.CommonQueryImpl.isAdmin;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static java.util.Objects.isNull;

import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.exception.BizException;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.test.FuncTrashQuery;
import cloud.xcan.angus.core.tester.domain.test.trash.FuncTrash;
import cloud.xcan.angus.core.tester.domain.test.trash.FuncTrashRepo;
import cloud.xcan.angus.core.tester.domain.test.trash.FuncTrashSearchRepo;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Implementation of FuncTrashQuery for managing functional test trash queries and operations.
 * <p>
 * This class provides functionality for querying and managing functional test trash items, which
 * represent deleted resources that can be restored or permanently cleared. It handles trash
 * retrieval, permission validation, and user information enrichment.
 * <p>
 * Key features include:
 * <ul>
 *   <li>Trash item counting and listing with search capabilities</li>
 *   <li>Permission-based access control for trash operations</li>
 *   <li>User information enrichment for audit trails</li>
 *   <li>Business operation validation (restore/clear)</li>
 *   <li>Admin privilege management</li>
 * </ul>
 * <p>
 * The implementation uses BizTemplate pattern for consistent business logic execution and
 * includes performance optimizations for bulk operations and permission checks.
 * <p>
 * Supports both individual trash operations and bulk operations with proper error handling
 * and permission validation.
 */
@Biz
public class FuncTrashQueryImpl implements FuncTrashQuery {

  @Resource
  private FuncTrashRepo funcTrashRepo;
  @Resource
  private FuncTrashSearchRepo funcTrashSearchRepo;
  @Resource
  private UserManager userManager;

  /**
   * Counts trash items with optional project filtering.
   * <p>
   * Provides total count of trash items, either globally or within a specific project.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param projectId optional project ID for filtering trash items
   * @return total count of trash items matching the criteria
   */
  @Override
  public Long count(Long projectId) {
    return new BizTemplate<Long>() {

      @Override
      protected Long process() {
        // Return global count if no project specified, otherwise project-specific count
        return isNull(projectId) ? funcTrashRepo.count()
            : funcTrashRepo.countByProjectId(projectId);
      }
    }.execute();
  }

  /**
   * Retrieves a paginated list of trash items with search capabilities.
   * <p>
   * Supports both regular search and full-text search with comprehensive filtering. Enriches
   * results with user information for both creation and deletion operations.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param spec           the search specification with criteria and filters
   * @param pageable       pagination parameters (page, size, sort)
   * @param fullTextSearch whether to use full-text search capabilities
   * @param match          full-text search match parameters
   * @return Page of FuncTrash objects with enriched user information
   */
  @Override
  public Page<FuncTrash> list(GenericSpecification<FuncTrash> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<FuncTrash>>() {

      @Override
      protected Page<FuncTrash> process() {
        // Execute search based on search type
        Page<FuncTrash> page = fullTextSearch
            ? funcTrashSearchRepo.find(spec.getCriteria(), pageable, FuncTrash.class, match)
            : funcTrashRepo.findAll(spec, pageable);

        // Enrich trash items with user information if results exist
        if (!page.isEmpty()) {
          // Enrich user information for both creation and deletion operations
          userManager.setUserNameAndAvatar(page.getContent(), "createdBy", "createdByName",
              "createdByAvatar");
          userManager.setUserNameAndAvatar(page.getContent(), "deletedBy", "deletedByName",
              "deletedByAvatar");
        }
        return page;
      }
    }.execute();
  }

  /**
   * Finds a trash item with business operation permission validation.
   * <p>
   * Validates that the current user has permission to perform the specified business operation
   * (restore or clear) on the trash item. Admin users have full access.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param id  the trash item ID to find
   * @param biz the business operation type ("BACK" for restore, "CLEAR" for permanent deletion)
   * @return FuncTrash object with validated permissions
   * @throws ResourceNotFound if the trash item is not found
   * @throws BizException     if the user lacks permission for the specified operation
   */
  @Override
  public FuncTrash findMyTrashForBiz(Long id, String biz) {
    // Retrieve trash item and validate existence
    FuncTrash trashDb = funcTrashRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "FuncTrash"));

    // Validate permissions for non-admin users
    if (!isAdmin() && ObjectUtils.notEqual(trashDb.getDeletedBy(), getUserId())) {
      // Check specific business operation permissions
      if ("BACK".equals(biz)) {
        throw BizException.of(TRASH_NO_BACK_PERMISSION_CODE, TRASH_NO_BACK_PERMISSION);
      } else if ("CLEAR".equals(biz)) {
        throw BizException.of(TRASH_NO_CLEAR_PERMISSION_CODE, TRASH_NO_CLEAR_PERMISSION);
      }
    }
    return trashDb;
  }
}
