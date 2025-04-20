package cloud.xcan.angus.core.tester.application.query.func.impl;

import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.func.FuncPlanQuery;
import cloud.xcan.angus.core.tester.application.query.func.FuncPlanSearch;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlan;
import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlanSearchRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class FuncPlanSearchImpl implements FuncPlanSearch {

  @Resource
  private FuncPlanSearchRepo funcPlanSearchRepo;

  @Resource
  private FuncPlanQuery funcPlanQuery;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private UserManager userManager;

  @Override
  public Page<FuncPlan> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<FuncPlan> clz, String... matches) {
    return new BizTemplate<Page<FuncPlan>>() {
      @Override
      protected void checkParams() {
        // Check the project permission
        projectMemberQuery.checkMember(criteria);
      }

      @Override
      protected Page<FuncPlan> process() {
        criteria.add(SearchCriteria.equal("deleted", false));

        // Set authorization conditions when you are not an administrator or only query yourself
        // funcPlanQuery.checkAndSetAuthObjectIdCriteria(criteria); -> All project members are visible

        Page<FuncPlan> page = funcPlanSearchRepo.find(criteria, pageable, clz, matches);
        if (page.hasContent()) {
          Set<Long> planIds = page.getContent().stream().map(FuncPlan::getId)
              .collect(Collectors.toSet());
          funcPlanQuery.setCaseNum(page.getContent(), planIds);
          funcPlanQuery.setProgress(page.getContent(), planIds);
          funcPlanQuery.setMembers(page.getContent(), planIds);
          // Set user name and avatar
          userManager.setUserNameAndAvatar(page.getContent(), "ownerId", "ownerName",
              "ownerAvatar");
        }
        return page;
      }
    }.execute();
  }

}
