package cloud.xcan.angus.core.tester.interfaces.report.facade.internal;

import static cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.angus.core.tester.interfaces.report.facade.internal.assembler.ReportAssembler.toReportDetailVo;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.spring.condition.NotCommunityEditionCondition;
import cloud.xcan.angus.core.tester.application.cmd.report.ReportCmd;
import cloud.xcan.angus.core.tester.application.query.issue.TaskQuery;
import cloud.xcan.angus.core.tester.application.query.report.ReportQuery;
import cloud.xcan.angus.core.tester.application.query.report.ReportSearch;
import cloud.xcan.angus.core.tester.application.query.test.FuncCaseQuery;
import cloud.xcan.angus.core.tester.domain.issue.TaskInfo;
import cloud.xcan.angus.core.tester.domain.report.Report;
import cloud.xcan.angus.core.tester.domain.report.ReportInfo;
import cloud.xcan.angus.core.tester.domain.setting.ContentFilterSetting;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.ReportFacade;
import cloud.xcan.angus.core.tester.interfaces.report.facade.dto.ReportAddDto;
import cloud.xcan.angus.core.tester.interfaces.report.facade.dto.ReportFindDto;
import cloud.xcan.angus.core.tester.interfaces.report.facade.dto.ReportReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.report.facade.dto.ReportSearchDto;
import cloud.xcan.angus.core.tester.interfaces.report.facade.dto.ReportUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.report.facade.internal.assembler.ReportAssembler;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.ReportDetailVo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.ReportListVo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.content.FuncCaseContentVo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.content.TaskContentVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.Collection;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Conditional(NotCommunityEditionCondition.class)
@Component
public class ReportFacadeImpl implements ReportFacade {

  @Resource
  private ReportCmd reportCmd;

  @Resource
  private ReportQuery reportQuery;

  @Resource
  private ReportSearch reportSearch;

  @Resource
  private TaskQuery taskQuery;

  @Resource
  private FuncCaseQuery funcCaseQuery;

  @Override
  public IdKey<Long, Object> add(ReportAddDto dto) {
    return reportCmd.add(ReportAssembler.addDtoToDomain(dto));
  }

  @Override
  public void update(ReportUpdateDto dto) {
    reportCmd.update(ReportAssembler.updateDtoToDomain(dto));
  }

  @Override
  public IdKey<Long, Object> replace(ReportReplaceDto dto) {
    return reportCmd.replace(ReportAssembler.replaceDtoToDomain(dto));
  }

  @Override
  public void generateNow(Long id) {
    reportCmd.generateNow(id);
  }

  @Override
  public void delete(Collection<Long> ids) {
    reportCmd.delete(ids);
  }

  @Override
  public String shareToken(Long id) {
    return reportQuery.shareToken(id);
  }

  @Override
  public Object content(Long id) {
    return reportQuery.content(id);
  }

  @Override
  public Object content(Long id, String token, Long recordId) {
    return reportQuery.content(id, token, recordId);
  }

  @Override
  public TaskContentVo taskContent(Long taskId) {
    TaskContentVo vo = new TaskContentVo();
    TaskInfo taskInfo = taskQuery.checkAndFindInfo(taskId);
    vo.setReport(toReportDetailVo(taskId, taskInfo));
    ContentFilterSetting filter = new ContentFilterSetting();
    filter.setTargetType(CombinedTargetType.TASK).setTargetId(taskId);
    vo.setContent(reportQuery.assembleTaskContent(filter));
    return vo;
  }

  @Override
  public FuncCaseContentVo funcCaseContent(Long caseId) {
    FuncCaseContentVo vo = new FuncCaseContentVo();
    FuncCaseInfo caseInfo = funcCaseQuery.checkAndFindInfo(caseId);
    vo.setReport(toReportDetailVo(caseId, caseInfo));
    ContentFilterSetting filter = new ContentFilterSetting();
    filter.setTargetType(CombinedTargetType.FUNC_CASE).setTargetId(caseId);
    vo.setContent(reportQuery.assembleFuncCaseContent(filter));
    return vo;
  }

  @NameJoin
  @Override
  public ReportDetailVo detail(Long id) {
    Report report = reportQuery.detail(id);
    return ReportAssembler.toDetailVo(report);
  }

  @NameJoin
  @Override
  public PageResult<ReportListVo> list(ReportFindDto dto) {
    Page<ReportInfo> page = reportQuery.find(ReportAssembler.getSpecification(dto), dto.tranPage());
    return buildVoPageResult(page, ReportAssembler::toListVo);
  }

  @NameJoin
  @Override
  public PageResult<ReportListVo> search(ReportSearchDto dto) {
    Page<ReportInfo> page = reportSearch.search(ReportAssembler.getSearchCriteria(dto),
        dto.tranPage(), ReportInfo.class, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, ReportAssembler::toListVo);
  }
}
