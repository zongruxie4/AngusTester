package cloud.xcan.sdf.core.angustester.application.query.task.impl;

import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNull;

import cloud.xcan.sdf.core.angustester.application.query.task.TaskFavouriteQuery;
import cloud.xcan.sdf.core.angustester.domain.task.favorite.TaskFavouriteP;
import cloud.xcan.sdf.core.angustester.domain.task.favorite.TaskFavouriteRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import javax.annotation.Resource;
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