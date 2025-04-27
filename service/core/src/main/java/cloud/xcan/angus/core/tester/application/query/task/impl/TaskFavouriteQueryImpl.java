package cloud.xcan.angus.core.tester.application.query.task.impl;

import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;

import cloud.xcan.angus.core.tester.application.query.task.TaskFavouriteQuery;
import cloud.xcan.angus.core.tester.domain.task.favorite.TaskFavouriteP;
import cloud.xcan.angus.core.tester.domain.task.favorite.TaskFavouriteRepo;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class TaskFavouriteQueryImpl implements TaskFavouriteQuery {

  @Resource
  private TaskFavouriteRepo taskFavouriteRepo;

  @Override
  public Page<TaskFavouriteP> search(Long projectId, String name, PageRequest pageable) {
    return new BizTemplate<Page<TaskFavouriteP>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<TaskFavouriteP> process() {
        return isEmpty(name) ? taskFavouriteRepo.search(projectId, getUserId(), pageable)
            : taskFavouriteRepo.searchByMatch(projectId, getUserId(), name, pageable);
      }
    }.execute();
  }

  @Override
  public Long count(Long projectId) {
    return new BizTemplate<Long>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Long process() {
        return isNull(projectId) ? taskFavouriteRepo.countByCreatedBy(getUserId())
            : taskFavouriteRepo.countByProjectIdAndCreatedBy(projectId, getUserId());
      }
    }.execute();
  }
}
