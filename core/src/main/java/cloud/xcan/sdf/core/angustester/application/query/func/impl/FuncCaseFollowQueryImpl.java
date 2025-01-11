package cloud.xcan.sdf.core.angustester.application.query.func.impl;

import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

import cloud.xcan.sdf.core.angustester.application.query.func.FuncCaseFollowQuery;
import cloud.xcan.sdf.core.angustester.domain.func.follow.FuncCaseFollowP;
import cloud.xcan.sdf.core.angustester.domain.func.follow.FuncCaseFollowRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class FuncCaseFollowQueryImpl implements FuncCaseFollowQuery {

  @Resource
  private FuncCaseFollowRepo funcCaseFollowRepo;

  @Override
  public Page<FuncCaseFollowP> search(Long projectId, String name, PageRequest pageable) {
    return new BizTemplate<Page<FuncCaseFollowP>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<FuncCaseFollowP> process() {
        return isEmpty(name) ? funcCaseFollowRepo.search(projectId, getUserId(), pageable)
            : funcCaseFollowRepo.searchByMatch(projectId, getUserId(), name, pageable);
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
        return isNull(projectId) ? funcCaseFollowRepo.countByCreatedBy(getUserId())
            : funcCaseFollowRepo.countByProjectIdAndCreatedBy(projectId, getUserId());
      }
    }.execute();
  }

}
