package cloud.xcan.sdf.core.angustester.interfaces.report.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.dto.ReportAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.dto.ReportFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.dto.ReportReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.dto.ReportSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.dto.ReportUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.ReportDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.ReportListVo;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.content.FuncCaseContentVo;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.content.TaskContentVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;

public interface ReportFacade {

  IdKey<Long, Object> add(ReportAddDto dto);

  void update(ReportUpdateDto dto);

  IdKey<Long, Object> replace(ReportReplaceDto dto);

  void generateNow(Long id);

  void delete(Collection<Long> ids);

  String shareToken(Long id);

  Object content(Long id);

  Object content(Long id, String token, Long recordId);

  TaskContentVo taskContent(Long taskId);

  FuncCaseContentVo funcCaseContent(Long caseId);

  ReportDetailVo detail(Long id);

  PageResult<ReportListVo> list(ReportFindDto dto);

  PageResult<ReportListVo> search(ReportSearchDto dto);

}
