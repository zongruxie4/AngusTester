package cloud.xcan.sdf.core.angustester.application.query.func.impl;

import cloud.xcan.sdf.api.manager.UserManager;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncPlanQuery;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncPlanSearch;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectMemberQuery;
import cloud.xcan.sdf.core.angustester.domain.func.plan.FuncPlan;
import cloud.xcan.sdf.core.angustester.domain.func.plan.FuncPlanSearchRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
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
  public Page<FuncPlan> search(Set<SearchCriteria> criterias, Pageable pageable,
      Class<FuncPlan> clz, String... matches) {
    return new BizTemplate<Page<FuncPlan>>() {
      @Override
      protected void checkParams() {
        // Check the project permission
        projectMemberQuery.checkMember(criterias);
      }

      @Override
      protected Page<FuncPlan> process() {
        criterias.add(SearchCriteria.equal("deletedFlag", false));

        // Set authorization conditions when you are not an administrator or only query yourself
        // funcPlanQuery.checkAndSetAuthObjectIdCriteria(criterias); -> All project members are visible

        Page<FuncPlan> page = funcPlanSearchRepo.find(criterias, pageable, clz, matches);
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
