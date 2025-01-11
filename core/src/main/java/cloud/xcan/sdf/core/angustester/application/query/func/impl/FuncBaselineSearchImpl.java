package cloud.xcan.sdf.core.angustester.application.query.func.impl;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncBaselineSearch;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectMemberQuery;
import cloud.xcan.sdf.core.angustester.domain.func.baseline.FuncBaselineInfo;
import cloud.xcan.sdf.core.angustester.domain.func.baseline.FuncBaselineInfoSearchRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import java.util.Set;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class FuncBaselineSearchImpl implements FuncBaselineSearch {

  @Resource
  private FuncBaselineInfoSearchRepo funcBaselineInfoSearchRepo;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Override
  public Page<FuncBaselineInfo> search(Set<SearchCriteria> criterias, PageRequest pageable,
      Class<FuncBaselineInfo> clz, String... matches) {
    return new BizTemplate<Page<FuncBaselineInfo>>() {
      @Override
      protected void checkParams() {
        // Check the project permission
        projectMemberQuery.checkMember(criterias);
      }

      @Override
      protected Page<FuncBaselineInfo> process() {
        return funcBaselineInfoSearchRepo.find(criterias, pageable, clz, matches);
      }
    }.execute();
  }
}
