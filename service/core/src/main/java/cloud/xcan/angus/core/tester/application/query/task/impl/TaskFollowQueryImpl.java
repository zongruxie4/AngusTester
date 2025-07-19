package cloud.xcan.angus.core.tester.application.query.task.impl;

import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.task.TaskFollowQuery;
import cloud.xcan.angus.core.tester.domain.task.follow.TaskFollowP;
import cloud.xcan.angus.core.tester.domain.task.follow.TaskFollowRepo;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class TaskFollowQueryImpl implements TaskFollowQuery {

  @Resource
  private TaskFollowRepo taskFollowRepo;

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
