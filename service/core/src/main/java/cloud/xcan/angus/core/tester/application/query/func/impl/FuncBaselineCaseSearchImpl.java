package cloud.xcan.angus.core.tester.application.query.func.impl;

import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.CriteriaUtils;
import cloud.xcan.angus.core.tester.application.query.func.FuncBaselineCaseSearch;
import cloud.xcan.angus.core.tester.application.query.func.FuncPlanAuthQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.tag.TagQuery;
import cloud.xcan.angus.core.tester.application.query.task.TaskFuncCaseQuery;
import cloud.xcan.angus.core.tester.domain.func.baseline.FuncBaselineCaseInfo;
import cloud.xcan.angus.core.tester.domain.func.baseline.FuncBaselineCaseInfoSearchRepo;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCase;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class FuncBaselineCaseSearchImpl implements FuncBaselineCaseSearch {

  @Resource
  private FuncBaselineCaseInfoSearchRepo funcBaselineCaseInfoSearchRepo;

  @Resource
  private FuncCaseRepo funcCaseRepo;

  @Resource
  private TaskFuncCaseQuery taskFuncCaseQuery;

  @Resource
  private FuncPlanAuthQuery funcPlanAuthQuery;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private TagQuery tagQuery;

  @Resource
  private UserManager userManager;

  @Override
  public Page<FuncBaselineCaseInfo> search(Long baselineId, boolean export,
      Set<SearchCriteria> criteria, PageRequest pageable, Class<FuncBaselineCaseInfo> clz,
      String... matches) {
    return new BizTemplate<Page<FuncBaselineCaseInfo>>() {
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
      protected Page<FuncBaselineCaseInfo> process() {
        criteria.add(SearchCriteria.equal("baselineId", baselineId));

        // Assemble mainClass table
        Page<FuncBaselineCaseInfo> page = funcBaselineCaseInfoSearchRepo.find(criteria, pageable,
            FuncBaselineCaseInfo.class, matches);

        if (page.hasContent()) {
          // Set user name and avatar
          userManager.setUserNameAndAvatar(page.getContent(), "createdBy");
          // Set tester name and avatar
          userManager.setUserNameAndAvatar(page.getContent(),
              "testerId", "testerName", "testerAvatar");
          // Set tags
          tagQuery.setTags(page.getContent());

          if (export) {
            List<Long> caseIds = page.getContent().stream()
                .map(FuncBaselineCaseInfo::getCaseId).collect(Collectors.toList());
            Map<Long, FuncCase> caseMap = funcCaseRepo.findAllById(caseIds).stream()
                .collect(Collectors.toMap(FuncCase::getId, x -> x));
            for (FuncBaselineCaseInfo caseInfo : page.getContent()) {
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
