package cloud.xcan.sdf.core.angustester.interfaces.report.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.dto.record.ReportRecordFindDto;
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
