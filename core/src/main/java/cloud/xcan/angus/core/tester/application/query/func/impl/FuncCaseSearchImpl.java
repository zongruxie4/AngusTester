package cloud.xcan.angus.core.tester.application.query.func.impl;

import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isUserAction;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.CriteriaUtils;
import cloud.xcan.angus.core.tester.application.query.func.FuncCaseQuery;
import cloud.xcan.angus.core.tester.application.query.func.FuncCaseSearch;
import cloud.xcan.angus.core.tester.application.query.func.FuncPlanAuthQuery;
import cloud.xcan.angus.core.tester.application.query.func.FuncPlanQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.tag.TagQuery;
import cloud.xcan.angus.core.tester.application.query.task.TaskFuncCaseQuery;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCase;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseInfoSearchRepo;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
  public Page<FuncCaseInfo> search(boolean export, Set<SearchCriteria> criteria,
      Pageable pageable, Class<FuncCaseInfo> clz, String... matches) {
    return new BizTemplate<Page<FuncCaseInfo>>() {
      @Override
      protected void checkParams() {
        // Check the project permission
        projectMemberQuery.checkMember(criteria);

        String planId = CriteriaUtils.findFirstValue(criteria, "planId");
        // ProtocolAssert.assertNotEmpty(planId, "Plan id is required");
        if (nonNull(planId) && export) {
          funcPlanAuthQuery.checkExportCaseAuth(getUserId(), Long.parseLong(planId));
        }
      }

      @Override
      protected Page<FuncCaseInfo> process() {
        criteria.add(SearchCriteria.equal("deleted", false));
        criteria.add(SearchCriteria.equal("planDeleted", false));

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
          // Set progress
          funcCaseQuery.setCaseInfoProgress(page.getContent());

          if (export) {
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
