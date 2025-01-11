package cloud.xcan.sdf.core.angustester.interfaces.report.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.domain.report.auth.ReportPermission;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.dto.auth.ReportAuthAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.dto.auth.ReportAuthFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.dto.auth.ReportAuthReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.auth.ReportAuthCurrentVo;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.auth.ReportAuthVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public interface ReportAuthFacade {

  IdKey<Long, Object> add(Long reportId, ReportAuthAddDto dto);

  void replace(Long id, ReportAuthReplaceDto dto);

  void enabled(Long reportId, Boolean enabledFlag);

  Boolean status(Long reportId);

  void delete(Long id);

  List<ReportPermission> userAuth(Long reportId, Long userId, Boolean adminFlag);

  ReportAuthCurrentVo currentUserAuth(Long reportId, Boolean adminFlag);

  Map<Long, ReportAuthCurrentVo> currentUserAuths(HashSet<Long> reportIds, Boolean adminFlag);

  void authCheck(Long reportId, ReportPermission permission, Long userId);

  PageResult<ReportAuthVo> list(ReportAuthFindDto dto);

}
