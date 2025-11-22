package cloud.xcan.angus.core.tester.interfaces.report.facade.internal.assembler;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.report.auth.ReportAuth;
import cloud.xcan.angus.core.tester.domain.report.auth.ReportAuthCurrent;
import cloud.xcan.angus.core.tester.domain.report.auth.ReportPermission;
import cloud.xcan.angus.core.tester.interfaces.report.facade.dto.auth.ReportAuthAddDto;
import cloud.xcan.angus.core.tester.interfaces.report.facade.dto.auth.ReportAuthFindDto;
import cloud.xcan.angus.core.tester.interfaces.report.facade.dto.auth.ReportAuthReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.auth.ReportAuthCurrentVo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.auth.ReportAuthDeptDetailVo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.auth.ReportAuthGroupDetailVo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.auth.ReportAuthUserDetailVo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.auth.ReportAuthVo;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.springframework.data.jpa.domain.Specification;

public class ReportAuthAssembler {

  public static ReportAuth addDtoToDomain(Long reportId, ReportAuthAddDto dto) {
    Set<ReportPermission> permissions = new HashSet<>();
    if (ObjectUtils.isNotEmpty(dto.getPermissions())) {
      permissions.addAll(dto.getPermissions());
    }
    return new ReportAuth().setReportId(reportId)
        .setCreator(false)
        .setAuthObjectType(dto.getAuthObjectType())
        .setAuthObjectId(dto.getAuthObjectId())
        .setAuths(new ArrayList<>(permissions));
  }

  public static ReportAuth replaceDtoToDomain(Long reportId, ReportAuthReplaceDto dto) {
    return new ReportAuth().setId(reportId).setAuths(new ArrayList<>(dto.getPermissions()));
  }

  public static ReportAuthVo toDetailVo(ReportAuth auth) {
    ReportAuthVo authVo;
    switch (auth.getAuthObjectType()) {
      case USER:
        authVo = new ReportAuthUserDetailVo();
        break;
      case GROUP:
        authVo = new ReportAuthGroupDetailVo();
        break;
      case DEPT:
        authVo = new ReportAuthDeptDetailVo();
        break;
      default:
        throw ResourceNotFound.of(auth.getAuthObjectType().getMessage());
    }
    authVo.setId(auth.getId());
    authVo.setPermissions(auth.getAuths());
    authVo.setAuthObjectType(auth.getAuthObjectType());
    authVo.setAuthObjectId(auth.getAuthObjectId());
    authVo.setCreator(auth.getCreator());
    authVo.setReportId(auth.getReportId());
    return authVo;
  }

  public static ReportAuthCurrentVo toAuthCurrentVo(ReportAuthCurrent authCurrent) {
    return new ReportAuthCurrentVo()
        .setReportAuth(authCurrent.isReportAuth())
        .setPermissions(authCurrent.getPermissions());
  }

  public static Specification<ReportAuth> getSpecification(ReportAuthFindDto dto) {
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate")
        .inAndNotFields("id", "reportId")
        .orderByFields("id", "reportId", "createdDate")
        .build();
    return new GenericSpecification<>(filters);
  }

}




