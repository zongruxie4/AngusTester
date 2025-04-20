package cloud.xcan.angus.core.tester.application.query.apis.impl;

import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static java.util.Objects.isNull;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.apis.ApisFollowQuery;
import cloud.xcan.angus.core.tester.domain.apis.follow.ApisFollowP;
import cloud.xcan.angus.core.tester.domain.apis.follow.ApisFollowRepo;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class ApisFollowQueryImpl implements ApisFollowQuery {

  @Resource
  private ApisFollowRepo apisFollowRepo;

  @Override
  public Page<ApisFollowP> search(Long projectId, String name, PageRequest pageable) {
    return new BizTemplate<Page<ApisFollowP>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<ApisFollowP> process() {
        return isEmpty(name) ? apisFollowRepo.search(projectId, getUserId(), pageable)
            : apisFollowRepo.searchByMatch(projectId, getUserId(), name, pageable);
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
        return isNull(projectId) ? apisFollowRepo.countByCreatedBy(getUserId())
            : apisFollowRepo.countByProjectIdAndCreatedBy(projectId, getUserId());
      }
    }.execute();
  }
}




