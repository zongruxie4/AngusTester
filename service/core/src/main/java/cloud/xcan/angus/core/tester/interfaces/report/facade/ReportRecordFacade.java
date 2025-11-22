package cloud.xcan.angus.core.tester.interfaces.report.facade;

import cloud.xcan.angus.core.tester.interfaces.report.facade.dto.record.ReportRecordFindDto;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.content.ApisTestingContentVo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.content.FuncCaseContentVo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.content.FuncPlanContentVo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.content.ProjectProgressContentVo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.content.ReportContentVo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.content.ScenarioTestingContentVo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.content.ServicesTestingContentVo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.content.TaskContentVo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.content.TaskSprintContentVo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.record.ReportRecordDetailVo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.record.ReportRecordListVo;
import cloud.xcan.angus.remote.PageResult;
import java.util.Collection;

public interface ReportRecordFacade {

  void delete(Collection<Long> ids);

  ReportRecordDetailVo detail(Long id);

  PageResult<ReportRecordListVo> list(ReportRecordFindDto dto);

  ReportContentVo reportContent(Long reportId, Long recordId);

  ProjectProgressContentVo projectProgressContent(Long reportId, Long recordId);

  TaskSprintContentVo taskSprintContent(Long reportId, Long recordId);

  TaskContentVo taskContent(Long reportId, Long recordId);

  FuncPlanContentVo funcPlanContent(Long reportId, Long recordId);

  FuncCaseContentVo funcCaseContent(Long reportId, Long recordId);

  ServicesTestingContentVo servicesTestingContent(Long reportId, Long recordId);

  ApisTestingContentVo apisTestingContent(Long reportId, Long recordId);

  ScenarioTestingContentVo scenarioTestingContent(Long reportId, Long recordId);

}
