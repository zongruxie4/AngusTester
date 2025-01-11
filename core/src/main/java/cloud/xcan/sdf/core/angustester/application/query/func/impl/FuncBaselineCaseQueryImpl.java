package cloud.xcan.sdf.core.angustester.application.query.func.impl;

import static cloud.xcan.sdf.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.sdf.api.manager.UserManager;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.comment.CommentQuery;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncBaselineCaseQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectMemberQuery;
import cloud.xcan.sdf.core.angustester.application.query.tag.TagQuery;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskFuncCaseQuery;
import cloud.xcan.sdf.core.angustester.domain.comment.CommentTargetType;
import cloud.xcan.sdf.core.angustester.domain.func.baseline.FuncBaselineCase;
import cloud.xcan.sdf.core.angustester.domain.func.baseline.FuncBaselineCaseInfo;
import cloud.xcan.sdf.core.angustester.domain.func.baseline.FuncBaselineCaseInfoRepo;
import cloud.xcan.sdf.core.angustester.domain.func.baseline.FuncBaselineCaseRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class FuncBaselineCaseQueryImpl implements FuncBaselineCaseQuery {

  @Resource
  private FuncBaselineCaseRepo funcBaselineCaseRepo;

  @Resource
  private FuncBaselineCaseInfoRepo funcBaselineCaseInfoRepo;

  @Resource
  private TaskFuncCaseQuery taskFuncCaseQuery;

  @Resource
  private TagQuery tagQuery;

  @Resource
  private CommentQuery commentQuery;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private UserManager userManager;

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
        if (isEmpty(casesDb)){
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

  @Override
  public Page<FuncBaselineCaseInfo> list(Long baselineId,
      GenericSpecification<FuncBaselineCaseInfo> spec, PageRequest pageable) {
    return new BizTemplate<Page<FuncBaselineCaseInfo>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(spec.getCriterias());
      }

      @Override
      protected Page<FuncBaselineCaseInfo> process() {
        spec.getCriterias().add(SearchCriteria.equal("baselineId", baselineId));

        // Assemble mainClass table
        Page<FuncBaselineCaseInfo> page = funcBaselineCaseInfoRepo.findAll(spec, pageable);

        if (page.hasContent()) {
          // Set user name and avatar
          userManager.setUserNameAndAvatar(page.getContent(), "createdBy");
          // Set tester name and avatar
          userManager.setUserNameAndAvatar(page.getContent(), "testerId", "testerName",
              "testerAvatar");

          // Set tags
          tagQuery.setTags(page.getContent());
        }
        return page;
      }
    }.execute();
  }

  @Override
  public List<FuncBaselineCase> checkAndFind(Long caseId) {
    List<FuncBaselineCase> baselineCases = funcBaselineCaseRepo.findByCaseId(caseId);
    assertResourceNotFound(baselineCases, caseId, "FuncBaselineCase");
    return baselineCases;
  }

}
