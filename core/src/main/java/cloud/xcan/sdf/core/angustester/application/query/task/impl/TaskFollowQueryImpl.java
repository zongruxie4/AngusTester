package cloud.xcan.sdf.core.angustester.application.query.task.impl;

import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNull;

import cloud.xcan.sdf.core.angustester.application.query.task.TaskFollowQuery;
import cloud.xcan.sdf.core.angustester.domain.task.follow.TaskFollowP;
import cloud.xcan.sdf.core.angustester.domain.task.follow.TaskFollowRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class TaskFollowQueryImpl implements TaskFollowQuery {

  @Resource
  private TaskFollowRepo taskFollowRepo;

  @Override
  public Page<TaskFollowP> search(Long projectId, String name, PageRequest pageable) {
    return new BizTemplate<Page<TaskFollowP>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<TaskFollowP> process() {
        return isEmpty(name) ? taskFollowRepo.search(projectId, getUserId(), pageable)
            : taskFollowRepo.searchByMatch(projectId, getUserId(), name, pageable);
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
        return isNull(projectId) ? taskFollowRepo.countByCreatedBy(getUserId())
            : taskFollowRepo.countByProjectIdAndCreatedBy(projectId, getUserId());
      }
    }.execute();
  }

}