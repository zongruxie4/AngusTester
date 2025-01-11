package cloud.xcan.sdf.core.angustester.interfaces.report.facade.internal;

import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.application.cmd.report.ReportRecordCmd;
import cloud.xcan.sdf.core.angustester.application.query.report.ReportQuery;
import cloud.xcan.sdf.core.angustester.application.query.report.ReportRecordQuery;
import cloud.xcan.sdf.core.angustester.domain.report.ReportTemplate;
import cloud.xcan.sdf.core.angustester.domain.report.record.ReportRecord;
import cloud.xcan.sdf.core.angustester.domain.report.record.ReportRecordInfo;
import cloud.xcan.sdf.core.angustester.domain.report.record.content.ApisTestingContent;
import cloud.xcan.sdf.core.angustester.domain.report.record.content.FuncCaseContent;
import cloud.xcan.sdf.core.angustester.domain.report.record.content.FuncPlanContent;
import cloud.xcan.sdf.core.angustester.domain.report.record.content.ProjectProgressContent;
import cloud.xcan.sdf.core.angustester.domain.report.record.content.ScenarioTestingContent;
import cloud.xcan.sdf.core.angustester.domain.report.record.content.ServicesTestingContent;
import cloud.xcan.sdf.core.angustester.domain.report.record.content.TaskContent;
import cloud.xcan.sdf.core.angustester.domain.report.record.content.TaskSprintContent;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.ReportFacade;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.ReportRecordFacade;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.dto.record.ReportRecordFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.internal.assembler.ReportRecordAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.ReportDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.content.ApisTestingContentVo;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.content.FuncCaseContentVo;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.content.FuncPlanContentVo;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.content.ProjectProgressContentVo;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.content.ReportContentVo;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.content.ScenarioTestingContentVo;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.content.ServicesTestingContentVo;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.content.TaskContentVo;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.content.TaskSprintContentVo;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.record.ReportRecordDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.record.ReportRecordListVo;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.core.biz.ProtocolAssert;
import cloud.xcan.sdf.core.spring.condition.NotPrivateEditionCondition;
import java.util.Collection;
import javax.annotation.Resource;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Conditional(NotPrivateEditionCondition.class)
@Component
public class ReportRecordFacadeImpl implements ReportRecordFacade {

  @Resource
  private ReportQuery reportQuery;

  @Resource
  private ReportRecordCmd reportRecordCmd;

  @Resource
  private ReportRecordQuery reportRecordQuery;

  @Resource
  private ReportFacade reportFacade;

  @Override
  public void delete(Collection<Long> ids) {
    reportRecordCmd.delete(ids);
  }

  @NameJoin
  @Override
  public ReportRecordDetailVo detail(Long id) {
    ReportRecord record = reportRecordQuery.detail(id);
    return ReportRecordAssembler.toDetailVo(record);
  }

  @NameJoin
  @Override
  public PageResult<ReportRecordListVo> list(ReportRecordFindDto dto) {
    Page<ReportRecordInfo> page = reportRecordQuery.find(
        ReportRecordAssembler.getSpecification(dto), dto.tranPage());
    return buildVoPageResult(page, ReportRecordAssembler::toListVo);
  }

  @Override
  public ReportContentVo reportContent(Long reportId, Long recordId) {
    ReportContentVo vo = new ReportContentVo();
    ReportDetailVo reportDetailVo = reportFacade.detail(reportId);
    vo.setReport(reportDetailVo);
    ProtocolAssert.assertTrue(!reportDetailVo.getTemplate().isExecutionReport(),
        "Cannot support querying execution type report");
    ReportRecord record = reportRecordQuery.findContent(reportId, recordId,
        reportDetailVo.getTemplate());
    vo.setContent(record.getContent());
    return vo;
  }

  @Override
  public ProjectProgressContentVo projectProgressContent(Long reportId, Long recordId) {
    ProjectProgressContentVo vo = new ProjectProgressContentVo();
    vo.setReport(reportFacade.detail(reportId));
    ReportRecord record = reportRecordQuery.findContent(reportId, recordId,
        ReportTemplate.PROJECT_PROGRESS);
    vo.setContent((ProjectProgressContent) record.getContent());
    return vo;
  }

  @Override
  public TaskSprintContentVo taskSprintContent(Long reportId, Long recordId) {
    TaskSprintContentVo vo = new TaskSprintContentVo();
    vo.setReport(reportFacade.detail(reportId));
    ReportRecord record = reportRecordQuery.findContent(reportId, recordId,
        ReportTemplate.TASK_SPRINT);
    vo.setContent((TaskSprintContent) record.getContent());
    return vo;
  }

  @Override
  public TaskContentVo taskContent(Long reportId, Long recordId) {
    TaskContentVo vo = new TaskContentVo();
    vo.setReport(reportFacade.detail(reportId));
    ReportRecord record = reportRecordQuery.findContent(reportId, recordId,
        ReportTemplate.TASK);
    vo.setContent((TaskContent) record.getContent());
    return vo;
  }

  @Override
  public FuncPlanContentVo funcPlanContent(Long reportId, Long recordId) {
    FuncPlanContentVo vo = new FuncPlanContentVo();
    vo.setReport(reportFacade.detail(reportId));
    ReportRecord record = reportRecordQuery.findContent(reportId, recordId,
        ReportTemplate.FUNC_TESTING_PLAN);
    vo.setContent((FuncPlanContent) record.getContent());
    return vo;
  }

  @Override
  public FuncCaseContentVo funcCaseContent(Long reportId, Long recordId) {
    FuncCaseContentVo vo = new FuncCaseContentVo();
    vo.setReport(reportFacade.detail(reportId));
    ReportRecord record = reportRecordQuery.findContent(reportId, recordId,
        ReportTemplate.FUNC_TESTING_CASE);
    vo.setContent((FuncCaseContent) record.getContent());
    return vo;
  }

  @Override
  public ServicesTestingContentVo servicesTestingContent(Long reportId, Long recordId) {
    ServicesTestingContentVo vo = new ServicesTestingContentVo();
    vo.setReport(reportFacade.detail(reportId));
    ReportRecord record = reportRecordQuery.findContent(reportId, recordId,
        ReportTemplate.SERVICES_TESTING_RESULT);
    vo.setContent((ServicesTestingContent) record.getContent());
    return vo;
  }

  @Override
  public ApisTestingContentVo apisTestingContent(Long reportId, Long recordId) {
    ApisTestingContentVo vo = new ApisTestingContentVo();
    vo.setReport(reportFacade.detail(reportId));
    ReportRecord record = reportRecordQuery.findContent(reportId, recordId,
        ReportTemplate.APIS_TESTING_RESULT);
    vo.setContent((ApisTestingContent) record.getContent());
    return vo;
  }

  @Override
  public ScenarioTestingContentVo scenarioTestingContent(Long reportId, Long recordId) {
    ScenarioTestingContentVo vo = new ScenarioTestingContentVo();
    vo.setReport(reportFacade.detail(reportId));
    ReportRecord record = reportRecordQuery.findContent(reportId, recordId,
        ReportTemplate.SCENARIO_TESTING_RESULT);
    vo.setContent((ScenarioTestingContent) record.getContent());
    return vo;
  }

}
