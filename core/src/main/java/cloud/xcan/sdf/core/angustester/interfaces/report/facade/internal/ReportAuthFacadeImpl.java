package cloud.xcan.sdf.core.angustester.interfaces.report.facade.internal;

import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.application.cmd.report.ReportAuthCmd;
import cloud.xcan.sdf.core.angustester.application.query.report.ReportAuthQuery;
import cloud.xcan.sdf.core.angustester.domain.report.auth.ReportAuth;
import cloud.xcan.sdf.core.angustester.domain.report.auth.ReportAuthCurrent;
import cloud.xcan.sdf.core.angustester.domain.report.auth.ReportPermission;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.ReportAuthFacade;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.dto.auth.ReportAuthAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.dto.auth.ReportAuthFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.dto.auth.ReportAuthReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.internal.assembler.ReportAuthAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.auth.ReportAuthCurrentVo;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.auth.ReportAuthVo;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.core.spring.condition.NotCommunityEditionCondition;
import cloud.xcan.sdf.spec.experimental.IdKey;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Conditional(NotCommunityEditionCondition.class)
@Component
public class ReportAuthFacadeImpl implements ReportAuthFacade {

  @Resource
  private ReportAuthCmd reportAuthCmd;

  @Resource
  private ReportAuthQuery reportAuthQuery;

  @Override
  public IdKey<Long, Object> add(Long reportId, ReportAuthAddDto dto) {
    return reportAuthCmd.add(ReportAuthAssembler.addDtoToDomain(reportId, dto));
  }

  @Override
  public void replace(Long reportId, ReportAuthReplaceDto dto) {
    reportAuthCmd.replace(ReportAuthAssembler.replaceDtoToDomain(reportId, dto));
  }

  @Override
  public void enabled(Long reportId, Boolean enabledFlag) {
    reportAuthCmd.enabled(reportId, enabledFlag);
  }

  @Override
  public Boolean status(Long reportId) {
    return reportAuthQuery.status(reportId);
  }

  @Override
  public void delete(Long id) {
    reportAuthCmd.delete(id);
  }

  @Override
  public List<ReportPermission> userAuth(Long reportId, Long userId, Boolean adminFlag) {
    return reportAuthQuery.userAuth(reportId, userId, adminFlag);
  }

  @Override
  public ReportAuthCurrentVo currentUserAuth(Long reportId, Boolean adminFlag) {
    ReportAuthCurrent authCurrent = reportAuthQuery.currentUserAuth(reportId, adminFlag);
    return ReportAuthAssembler.toAuthCurrentVo(authCurrent);
  }

  @Override
  public Map<Long, ReportAuthCurrentVo> currentUserAuths(HashSet<Long> reportIds,
      Boolean adminFlag) {
    Map<Long, ReportAuthCurrent> authCurrentsMap = reportAuthQuery
        .currentUserAuths(reportIds, adminFlag);
    return ObjectUtils.isEmpty(authCurrentsMap) ? null : authCurrentsMap.entrySet()
        .stream().collect(Collectors.toMap(Entry::getKey,
            x -> ReportAuthAssembler.toAuthCurrentVo(x.getValue())));
  }

  @Override
  public void authCheck(Long reportId, ReportPermission permission, Long userId) {
    reportAuthQuery.check(reportId, permission, userId);
  }

  @NameJoin
  @Override
  public PageResult<ReportAuthVo> list(ReportAuthFindDto dto) {
    List<String> reportIds = dto.getFilterInValue("reportId");
    if (dto.getReportId() != null) {
      reportIds.add(String.valueOf(dto.getReportId()));
    }
    Page<ReportAuth> dirAuthPage = reportAuthQuery.find(
        ReportAuthAssembler.getSpecification(dto), reportIds, dto.tranPage());
    return buildVoPageResult(dirAuthPage, ReportAuthAssembler::toDetailVo);
  }

}
