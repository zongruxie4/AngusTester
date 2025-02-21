package cloud.xcan.sdf.core.angustester.application.query.func.impl;

import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.isUserAction;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.manager.UserManager;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncCaseQuery;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncCaseSearch;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncPlanAuthQuery;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncPlanQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectMemberQuery;
import cloud.xcan.sdf.core.angustester.application.query.tag.TagQuery;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskFuncCaseQuery;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCase;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseInfoSearchRepo;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.jpa.criteria.CriteriaUtils;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class FuncCaseSearchImpl implements FuncCaseSearch {

  @Resource
  private FuncCaseRepo funcCaseRepo;

  @Resource
  private FuncCaseInfoSearchRepo funcCaseInfoSearchRepo;

  @Resource
  private FuncCaseQuery funcCaseQuery;

  @Resource
  private FuncPlanQuery funcPlanQuery;

  @Resource
  private FuncPlanAuthQuery funcPlanAuthQuery;

  @Resource
  private TaskFuncCaseQuery taskFuncCaseQuery;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private TagQuery tagQuery;

  @Resource
  private UserManager userManager;

  @Override
  public Page<FuncCaseInfo> search(boolean exportFlag, Set<SearchCriteria> criteria,
      Pageable pageable, Class<FuncCaseInfo> clz, String... matches) {
    return new BizTemplate<Page<FuncCaseInfo>>() {
      @Override
      protected void checkParams() {
        // Check the project permission
        projectMemberQuery.checkMember(criteria);

        String planId = CriteriaUtils.findFirstValue(criteria, "planId");
        // ProtocolAssert.assertNotEmpty(planId, "Plan id is required");
        if (nonNull(planId) && exportFlag) {
          funcPlanAuthQuery.checkExportCaseAuth(getUserId(), Long.parseLong(planId));
        }
      }

      @Override
      protected Page<FuncCaseInfo> process() {
        criteria.add(SearchCriteria.equal("deletedFlag", false));
        criteria.add(SearchCriteria.equal("planDeletedFlag", false));

        // Set authorization conditions when you are not an administrator or only query yourself
        funcPlanQuery.checkAndSetAuthObjectIdCriteria(criteria);

        // Assemble mainClass table
        Page<FuncCaseInfo> page = funcCaseInfoSearchRepo.find(criteria, pageable,
            FuncCaseInfo.class, matches);

        if (page.hasContent()) {
          if (isUserAction()) {
            // Set favourite state
            funcCaseQuery.setFavourite(page.getContent());
            // Set follow state
            funcCaseQuery.setFollow(page.getContent());
          }
          // Set user name and avatar
          userManager.setUserNameAndAvatar(page.getContent(), "createdBy");
          // Set tester name and avatar
          userManager.setUserNameAndAvatar(page.getContent(), "testerId", "testerName",
              "testerAvatar");

          // Set tags
          tagQuery.setTags(page.getContent());

          if (exportFlag) {
            List<Long> caseIds = page.getContent().stream().map(FuncCaseInfo::getId)
                .collect(Collectors.toList());
            Map<Long, FuncCase> caseMap = funcCaseRepo.findAllById(caseIds).stream()
                .collect(Collectors.toMap(FuncCase::getId, x -> x));
            for (FuncCaseInfo caseInfo : page.getContent()) {
              caseInfo.setPrecondition(caseMap.get(caseInfo.getId()).getPrecondition());
              caseInfo.setSteps(caseMap.get(caseInfo.getId()).getSteps());
            }
            // Set reference tasks and cases
            taskFuncCaseQuery.setAssocForCase(page.getContent());
          }
        }
        return page;
      }
    }.execute();
  }

}
