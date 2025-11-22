package cloud.xcan.angus.core.tester.interfaces.report.facade.internal;

import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.spring.condition.NotCommunityEditionCondition;
import cloud.xcan.angus.core.tester.application.cmd.report.ReportAuthCmd;
import cloud.xcan.angus.core.tester.application.query.report.ReportAuthQuery;
import cloud.xcan.angus.core.tester.domain.report.auth.ReportAuth;
import cloud.xcan.angus.core.tester.domain.report.auth.ReportAuthCurrent;
import cloud.xcan.angus.core.tester.domain.report.auth.ReportPermission;
import cloud.xcan.angus.core.tester.interfaces.report.facade.ReportAuthFacade;
import cloud.xcan.angus.core.tester.interfaces.report.facade.dto.auth.ReportAuthAddDto;
import cloud.xcan.angus.core.tester.interfaces.report.facade.dto.auth.ReportAuthFindDto;
import cloud.xcan.angus.core.tester.interfaces.report.facade.dto.auth.ReportAuthReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.report.facade.internal.assembler.ReportAuthAssembler;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.auth.ReportAuthCurrentVo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.auth.ReportAuthVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import jakarta.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
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
  public void enabled(Long reportId, Boolean enabled) {
    reportAuthCmd.enabled(reportId, enabled);
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
  public List<ReportPermission> userAuth(Long reportId, Long userId, Boolean admin) {
    return reportAuthQuery.userAuth(reportId, userId, admin);
  }

  @Override
  public ReportAuthCurrentVo currentUserAuth(Long reportId, Boolean admin) {
    ReportAuthCurrent authCurrent = reportAuthQuery.currentUserAuth(reportId, admin);
    return ReportAuthAssembler.toAuthCurrentVo(authCurrent);
  }

  @Override
  public Map<Long, ReportAuthCurrentVo> currentUserAuths(HashSet<Long> reportIds,
      Boolean admin) {
    Map<Long, ReportAuthCurrent> authCurrentsMap = reportAuthQuery
        .currentUserAuths(reportIds, admin);
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
