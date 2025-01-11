package cloud.xcan.sdf.core.angustester.interfaces.report.facade.internal.assembler;

import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserFullname;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.sdf.core.angustester.domain.report.Report;
import cloud.xcan.sdf.core.angustester.domain.report.ReportCategory;
import cloud.xcan.sdf.core.angustester.domain.report.ReportInfo;
import cloud.xcan.sdf.core.angustester.domain.report.ReportStatus;
import cloud.xcan.sdf.core.angustester.domain.report.ReportTemplate;
import cloud.xcan.sdf.core.angustester.domain.task.TaskInfo;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.dto.ReportAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.dto.ReportFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.dto.ReportReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.dto.ReportSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.dto.ReportUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.ReportDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.ReportListVo;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder;
import java.time.LocalDateTime;
import java.util.Set;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;

public class ReportAssembler {

  public static Report addDtoToDomain(ReportAddDto dto) {
    return new Report().setProjectId(dto.getProjectId())
        .setAuthFlag(nullSafe(dto.getAuthFlag(), false))
        .setName(dto.getName())
        .setVersion(dto.getVersion())
        .setDescription(dto.getDescription())
        .setCategory(dto.getCategory())
        .setTemplate(dto.getTemplate())
        .setStatus(ReportStatus.PENDING)
        .setShareToken(RandomStringUtils.randomAlphanumeric(32))
        .setCreateTimeSetting(dto.getCreateTimeSetting())
        .setBasicInfoSetting(dto.getBasicInfoSetting())
        .setContentSetting(dto.getContentSetting());
  }

  public static Report updateDtoToDomain(ReportUpdateDto dto) {
    return new Report().setId(dto.getId())
        //.setProjectId(dto.getProjectId())
        //.setAuthFlag(nonNull(dto.getId()) ? null : nullSafe(dto.getAuthFlag(), false))
        .setName(dto.getName())
        .setVersion(dto.getVersion())
        .setDescription(dto.getDescription())
        //.setCategory(dto.getCategory())
        //.setTemplate(dto.getTemplate())
        //.setStatus(nonNull(dto.getId()) ? null : ReportStatus.PENDING)
        .setCreateTimeSetting(dto.getCreateTimeSetting())
        .setBasicInfoSetting(dto.getBasicInfoSetting())
        .setContentSetting(dto.getContentSetting());
  }

  public static Report replaceDtoToDomain(ReportReplaceDto dto) {
    return new Report().setId(dto.getId())
        .setProjectId(dto.getProjectId())
        .setAuthFlag(nonNull(dto.getId()) ? null : nullSafe(dto.getAuthFlag(), false))
        .setName(dto.getName())
        .setVersion(dto.getVersion())
        .setDescription(dto.getDescription())
        .setCategory(nonNull(dto.getId()) ? null : dto.getCategory())
        .setTemplate(nonNull(dto.getId()) ? null : dto.getTemplate())
        .setStatus(nonNull(dto.getId()) ? null : ReportStatus.PENDING)
        .setShareToken(nonNull(dto.getId()) ? null : RandomStringUtils.randomAlphanumeric(32))
        .setCreateTimeSetting(dto.getCreateTimeSetting())
        .setBasicInfoSetting(dto.getBasicInfoSetting())
        .setContentSetting(dto.getContentSetting());
  }

  public static @NotNull ReportDetailVo toReportDetailVo(Long taskId, TaskInfo taskInfo) {
    ReportDetailVo report = new ReportDetailVo();
    report.setName(taskInfo.getName())
        .setCategory(ReportCategory.TASK).setTemplate(ReportTemplate.TASK)
        .setDescription(ReportTemplate.TASK.getMessage())
        .setTargetType(CombinedTargetType.TASK)
        .setTargetId(taskId).setTargetName(taskInfo.getName())
        .setCreatedBy(getUserId()).setCreatedByName(getUserFullname())
        .setCreatedDate(LocalDateTime.now());
    return report;
  }

  public static @NotNull ReportDetailVo toReportDetailVo(Long caseId, FuncCaseInfo caseInfo) {
    ReportDetailVo report = new ReportDetailVo();
    report.setName(caseInfo.getName())
        .setCategory(ReportCategory.FUNCTIONAL).setTemplate(ReportTemplate.FUNC_TESTING_CASE)
        .setDescription(ReportTemplate.FUNC_TESTING_CASE.getMessage())
        .setTargetType(CombinedTargetType.FUNC_CASE)
        .setTargetId(caseId).setTargetName(caseInfo.getName())
        .setCreatedBy(getUserId()).setCreatedByName(getUserFullname())
        .setCreatedDate(LocalDateTime.now());
    return report;
  }

  public static ReportDetailVo toDetailVo(Report report) {
    return new ReportDetailVo().setId(report.getId())
        .setProjectId(report.getProjectId())
        .setAuthFlag(report.getAuthFlag())
        .setName(report.getName())
        .setVersion(report.getVersion())
        .setDescription(report.getDescription())
        .setCategory(report.getCategory())
        .setTemplate(report.getTemplate())
        .setStatus(report.getStatus())
        .setFailureMessage(report.getFailureMessage())
        .setCreatedAt(report.getCreatedAt())
        .setNextGenerationDate(report.getNextGenerationDate())
        .setTargetId(report.getTargetId())
        .setTargetType(report.getTargetType())
        .setTargetName(report.getTargetName())
        .setCreateTimeSetting(report.getCreateTimeSetting())
        .setBasicInfoSetting(report.getBasicInfoSetting())
        .setContentSetting(report.getContentSetting())
        .setTenantId(report.getTenantId())
        .setCreatedBy(report.getCreatedBy())
        .setCreatedDate(report.getCreatedDate())
        .setLastModifiedBy(report.getLastModifiedBy())
        .setLastModifiedDate(report.getLastModifiedDate());
  }

  public static ReportListVo toListVo(ReportInfo report) {
    return new ReportListVo().setId(report.getId())
        .setProjectId(report.getProjectId())
        .setAuthFlag(report.getAuthFlag())
        .setName(report.getName())
        .setVersion(report.getVersion())
        .setDescription(report.getDescription())
        .setCategory(report.getCategory())
        .setTemplate(report.getTemplate())
        .setStatus(report.getStatus())
        .setFailureMessage(report.getFailureMessage())
        .setCreatedAt(report.getCreatedAt())
        .setNextGenerationDate(report.getNextGenerationDate())
        .setTargetId(report.getTargetId())
        .setTargetType(report.getTargetType())
        .setTargetName(report.getTargetName())
        .setCurrentAuths(report.getCurrentAuths())
        .setTenantId(report.getTenantId())
        .setCreatedBy(report.getCreatedBy())
        .setCreatedDate(report.getCreatedDate())
        .setLastModifiedBy(report.getLastModifiedBy())
        .setLastModifiedDate(report.getLastModifiedDate());
  }

  public static GenericSpecification<ReportInfo> getSpecification(ReportFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate")
        .orderByFields("id", "name", "category", "template", "nextGenerationDate",
            "createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate")
        .matchSearchFields("name", "description")
        .build();
    return new GenericSpecification<>(filters);
  }

  public static Set<SearchCriteria> getSearchCriteria(ReportSearchDto dto) {
    // Build the final filters
    return new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate")
        .orderByFields("id", "name", "category", "template", "nextGenerationDate",
            "createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate")
        .matchSearchFields("name", "description")
        .build();
  }
}
