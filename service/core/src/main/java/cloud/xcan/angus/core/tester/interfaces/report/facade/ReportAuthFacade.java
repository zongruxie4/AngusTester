package cloud.xcan.angus.core.tester.interfaces.report.facade;

import cloud.xcan.angus.core.tester.domain.report.auth.ReportPermission;
import cloud.xcan.angus.core.tester.interfaces.report.facade.dto.auth.ReportAuthAddDto;
import cloud.xcan.angus.core.tester.interfaces.report.facade.dto.auth.ReportAuthFindDto;
import cloud.xcan.angus.core.tester.interfaces.report.facade.dto.auth.ReportAuthReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.auth.ReportAuthCurrentVo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.auth.ReportAuthVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public interface ReportAuthFacade {

  IdKey<Long, Object> add(Long reportId, ReportAuthAddDto dto);

  void replace(Long id, ReportAuthReplaceDto dto);

  void enabled(Long reportId, Boolean enabled);

  Boolean status(Long reportId);

  void delete(Long id);

  List<ReportPermission> userAuth(Long reportId, Long userId, Boolean admin);

  ReportAuthCurrentVo currentUserAuth(Long reportId, Boolean admin);

  Map<Long, ReportAuthCurrentVo> currentUserAuths(HashSet<Long> reportIds, Boolean admin);

  void authCheck(Long reportId, ReportPermission permission, Long userId);

  PageResult<ReportAuthVo> list(ReportAuthFindDto dto);

}
