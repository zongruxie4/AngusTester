package cloud.xcan.angus.core.tester.application.query.func.impl;

import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.func.FuncCaseFavouriteQuery;
import cloud.xcan.angus.core.tester.domain.func.favourite.FuncCaseFavouriteP;
import cloud.xcan.angus.core.tester.domain.func.favourite.FuncCaseFavouriteRepo;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Implementation of FuncCaseFavouriteQuery for managing case favourite queries.
 * <p>
 * Provides methods to list and count favourite cases for the current user.
 */
@Biz
public class FuncCaseFavouriteQueryImpl implements FuncCaseFavouriteQuery {

  @Resource
  private FuncCaseFavouriteRepo funcCaseFavouriteRepo;

  /**
   * Lists favourite cases for a project and user, with optional name filter.
   * <p>
   * Supports pagination and fuzzy search by name.
   *
   * @param projectId project ID
   * @param name case name filter
   * @param pageable pagination
   * @return paginated result of FuncCaseFavouriteP
   */
  @Override
  public Page<FuncCaseFavouriteP> list(Long projectId, String name, PageRequest pageable) {
    return new BizTemplate<Page<FuncCaseFavouriteP>>() {

      @Override
      protected Page<FuncCaseFavouriteP> process() {
        return isEmpty(name) ? funcCaseFavouriteRepo.search(projectId, getUserId(), pageable)
            : funcCaseFavouriteRepo.searchByMatch(projectId, getUserId(), name, pageable);
      }
    }.execute();
  }

  /**
   * Counts the number of favourite cases for a project and user.
   *
   * @param projectId project ID (nullable)
   * @return count of favourite cases
   */
  @Override
  public Long count(Long projectId) {
    return new BizTemplate<Long>() {

      @Override
      protected Long process() {
        return isNull(projectId) ? funcCaseFavouriteRepo.countByCreatedBy(getUserId())
            : funcCaseFavouriteRepo.countByProjectIdAndCreatedBy(projectId, getUserId());
      }
    }.execute();
  }

}

