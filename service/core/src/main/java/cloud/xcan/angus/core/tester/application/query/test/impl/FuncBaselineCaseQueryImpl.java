package cloud.xcan.angus.core.tester.application.query.test.impl;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.CriteriaUtils;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.comment.CommentQuery;
import cloud.xcan.angus.core.tester.application.query.issue.TaskFuncCaseQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.project.TagQuery;
import cloud.xcan.angus.core.tester.application.query.test.FuncBaselineCaseQuery;
import cloud.xcan.angus.core.tester.application.query.test.FuncPlanAuthQuery;
import cloud.xcan.angus.core.tester.domain.comment.CommentTargetType;
import cloud.xcan.angus.core.tester.domain.test.baseline.FuncBaselineCase;
import cloud.xcan.angus.core.tester.domain.test.baseline.FuncBaselineCaseInfo;
import cloud.xcan.angus.core.tester.domain.test.baseline.FuncBaselineCaseInfoRepo;
import cloud.xcan.angus.core.tester.domain.test.baseline.FuncBaselineCaseInfoSearchRepo;
import cloud.xcan.angus.core.tester.domain.test.baseline.FuncBaselineCaseRepo;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCase;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Implementation of FuncBaselineCaseQuery for managing baseline case queries.
 * <p>
 * Provides methods to retrieve, list, and check baseline cases, including tag, comment, and user
 * info enrichment.
 * <p>
 * Handles permission checks and export logic for baseline case information.
 */
@Biz
public class FuncBaselineCaseQueryImpl implements FuncBaselineCaseQuery {

  @Resource
  private FuncBaselineCaseRepo funcBaselineCaseRepo;
  @Resource
  private FuncBaselineCaseInfoRepo funcBaselineCaseInfoRepo;
  @Resource
  private FuncBaselineCaseInfoSearchRepo funcBaselineCaseInfoSearchRepo;
  @Resource
  private TaskFuncCaseQuery taskFuncCaseQuery;
  @Resource
  private FuncPlanAuthQuery funcPlanAuthQuery;
  @Resource
  private FuncCaseRepo funcCaseRepo;
  @Resource
  private TagQuery tagQuery;
  @Resource
  private CommentQuery commentQuery;
  @Resource
  private ProjectMemberQuery projectMemberQuery;
  @Resource
  private UserManager userManager;

  /**
   * Retrieves detailed information for a baseline case by caseId.
   * <p>
   * Enriches the result with tags, associated tasks, user info, and comment count.
   *
   * @param caseId the case ID
   * @return list of FuncBaselineCase, or null if not found
   */
  @Override
  public List<FuncBaselineCase> detail(Long caseId) {
    return new BizTemplate<List<FuncBaselineCase>>() {
      List<FuncBaselineCase> casesDb;

      @Override
      protected void checkParams() {
        casesDb = funcBaselineCaseRepo.findByCaseId(caseId);
      }

      @Override
      protected List<FuncBaselineCase> process() {
        if (isEmpty(casesDb)) {
          return null;
        }
        // Set tags
        tagQuery.setTags(casesDb);
        // Set reference tasks and cases
        taskFuncCaseQuery.setAssocForCase(casesDb);
        // Set user name and avatar
        userManager.setUserNameAndAvatar(casesDb, "createdBy");
        // Set comment num
        for (FuncBaselineCase caseDb : casesDb) {
          int commentNum = commentQuery.getCommentNum(caseDb.getCaseId(),
              CommentTargetType.FUNC_CASE.getValue());
          caseDb.setCommentNum(commentNum);
        }
        return casesDb;
      }
    }.execute();
  }

  /**
   * Lists baseline case info with optional export and full-text search.
   * <p>
   * Checks project member permission and export authorization, enriches with user and tag info, and
   * optionally includes steps and preconditions for export.
   *
   * @param export         whether to export extra info
   * @param baselineId     the baseline ID
   * @param spec           search specification
   * @param pageable       pagination
   * @param fullTextSearch whether to use full-text search
   * @param match          search match terms
   * @return paginated result of FuncBaselineCaseInfo
   */
  @Override
  public Page<FuncBaselineCaseInfo> list(boolean export, Long baselineId,
      GenericSpecification<FuncBaselineCaseInfo> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<FuncBaselineCaseInfo>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(spec.getCriteria());

        String planId = CriteriaUtils.findFirstValue(spec.getCriteria(), "planId");
        // ProtocolAssert.assertNotEmpty(planId, "Plan id is required");
        if (nonNull(planId) && export) {
          funcPlanAuthQuery.checkExportCaseAuth(getUserId(), Long.parseLong(planId));
        }
      }

      @Override
      protected Page<FuncBaselineCaseInfo> process() {
        spec.getCriteria().add(SearchCriteria.equal("baselineId", baselineId));

        Set<String> tagIds = CriteriaUtils.findAllIdInAndEqualValues(spec.getCriteria(),
            "tagId", true);
        if (isNotEmpty(tagIds)) {
          Set<Long> caseIds = tagQuery.getTargetIdsById(
              tagIds.stream().map(Long::parseLong).collect(Collectors.toSet()));
          if (isNotEmpty(caseIds)){
            spec.getCriteria().add(SearchCriteria.in("caseId", caseIds));
          }
        }

        // Assemble mainClass table
        Page<FuncBaselineCaseInfo> page = fullTextSearch
            ? funcBaselineCaseInfoSearchRepo.find(spec.getCriteria(), pageable,
            FuncBaselineCaseInfo.class, match)
            : funcBaselineCaseInfoRepo.findAll(spec, pageable);

        if (page.hasContent()) {
          // Set user name and avatar
          userManager.setUserNameAndAvatar(page.getContent(), "createdBy");
          // Set tester name and avatar
          userManager.setUserNameAndAvatar(page.getContent(), "testerId", "testerName",
              "testerAvatar");
          // Set tags
          tagQuery.setTags(page.getContent());

          if (export) {
            List<Long> caseIds = page.getContent().stream()
                .map(FuncBaselineCaseInfo::getCaseId).toList();
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

  /**
   * Checks and finds baseline cases by caseId, throws if not found.
   *
   * @param caseId the case ID
   * @return list of FuncBaselineCase
   */
  @Override
  public List<FuncBaselineCase> checkAndFind(Long caseId) {
    List<FuncBaselineCase> baselineCases = funcBaselineCaseRepo.findByCaseId(caseId);
    assertResourceNotFound(baselineCases, caseId, "FuncBaselineCase");
    return baselineCases;
  }

}
