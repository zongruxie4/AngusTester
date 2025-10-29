package cloud.xcan.angus.core.tester.application.query.func.impl;

import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.func.FuncCaseFollowQuery;
import cloud.xcan.angus.core.tester.domain.test.follow.FuncCaseFollowP;
import cloud.xcan.angus.core.tester.domain.test.follow.FuncCaseFollowRepo;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Implementation of FuncCaseFollowQuery for managing case follow queries.
 * <p>
 * Provides methods to list and count followed cases for the current user.
 */
@Biz
public class FuncCaseFollowQueryImpl implements FuncCaseFollowQuery {

  @Resource
  private FuncCaseFollowRepo funcCaseFollowRepo;

  /**
   * Lists followed cases for a project and user, with optional name filter.
   * <p>
   * Supports pagination and fuzzy search by name.
   *
   * @param projectId project ID
   * @param name case name filter
   * @param pageable pagination
   * @return paginated result of FuncCaseFollowP
   */
  @Override
  public Page<FuncCaseFollowP> list(Long projectId, String name, PageRequest pageable) {
    return new BizTemplate<Page<FuncCaseFollowP>>() {

      @Override
      protected Page<FuncCaseFollowP> process() {
        return isEmpty(name) ? funcCaseFollowRepo.search(projectId, getUserId(), pageable)
            : funcCaseFollowRepo.searchByMatch(projectId, getUserId(), name, pageable);
      }
    }.execute();
  }

  /**
   * Counts the number of followed cases for a project and user.
   *
   * @param projectId project ID (nullable)
   * @return count of followed cases
   */
  @Override
  public Long count(Long projectId) {
    return new BizTemplate<Long>() {

      @Override
      protected Long process() {
        return isNull(projectId) ? funcCaseFollowRepo.countByCreatedBy(getUserId())
            : funcCaseFollowRepo.countByProjectIdAndCreatedBy(projectId, getUserId());
      }
    }.execute();
  }

}
