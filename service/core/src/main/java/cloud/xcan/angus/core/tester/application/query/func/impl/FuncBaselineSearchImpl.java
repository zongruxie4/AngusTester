package cloud.xcan.angus.core.tester.application.query.func.impl;

import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.core.tester.application.query.func.FuncBaselineSearch;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.domain.func.baseline.FuncBaselineInfo;
import cloud.xcan.angus.core.tester.domain.func.baseline.FuncBaselineInfoSearchRepo;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import java.util.Set;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class FuncBaselineSearchImpl implements FuncBaselineSearch {

  @Resource
  private FuncBaselineInfoSearchRepo funcBaselineInfoSearchRepo;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Override
  public Page<FuncBaselineInfo> search(Set<SearchCriteria> criteria, PageRequest pageable,
      Class<FuncBaselineInfo> clz, String... matches) {
    return new BizTemplate<Page<FuncBaselineInfo>>() {
      @Override
      protected void checkParams() {
        // Check the project permission
        projectMemberQuery.checkMember(criteria);
      }

      @Override
      protected Page<FuncBaselineInfo> process() {
        return funcBaselineInfoSearchRepo.find(criteria, pageable, clz, matches);
      }
    }.execute();
  }
}
