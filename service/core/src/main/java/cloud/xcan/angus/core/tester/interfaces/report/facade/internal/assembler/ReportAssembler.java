package cloud.xcan.angus.core.tester.interfaces.report.facade.internal.assembler;

import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserFullName;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.issue.TaskInfo;
import cloud.xcan.angus.core.tester.domain.report.Report;
import cloud.xcan.angus.core.tester.domain.report.ReportCategory;
import cloud.xcan.angus.core.tester.domain.report.ReportInfo;
import cloud.xcan.angus.core.tester.domain.report.ReportStatus;
import cloud.xcan.angus.core.tester.domain.report.ReportTemplate;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.dto.ReportAddDto;
import cloud.xcan.angus.core.tester.interfaces.report.facade.dto.ReportFindDto;
import cloud.xcan.angus.core.tester.interfaces.report.facade.dto.ReportReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.report.facade.dto.ReportSearchDto;
import cloud.xcan.angus.core.tester.interfaces.report.facade.dto.ReportUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.ReportDetailVo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.ReportListVo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.time.LocalDateTime;
import java.util.Set;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;

public class ReportAssembler {

  public static Report addDtoToDomain(ReportAddDto dto) {
    return new Report().setProjectId(dto.getProjectId())
        .setAuth(nullSafe(dto.getAuth(), false))
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
        //.setAuth(nonNull(dto.getId()) ? null : nullSafe(dto.getAuth(), false))
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
        .setAuth(nonNull(dto.getId()) ? null : nullSafe(dto.getAuth(), false))
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
        .setCreatedBy(getUserId()).setCreator(getUserFullName())
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
        .setCreatedBy(getUserId()).setCreator(getUserFullName())
        .setCreatedDate(LocalDateTime.now());
    return report;
  }

  public static ReportDetailVo toDetailVo(Report report) {
    return new ReportDetailVo().setId(report.getId())
        .setProjectId(report.getProjectId())
        .setAuth(report.getAuth())
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
        .setModifiedBy(report.getModifiedBy())
        .setModifiedDate(report.getModifiedDate());
  }

  public static ReportListVo toListVo(ReportInfo report) {
    return new ReportListVo().setId(report.getId())
        .setProjectId(report.getProjectId())
        .setAuth(report.getAuth())
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
        .setModifiedBy(report.getModifiedBy())
        .setModifiedDate(report.getModifiedDate());
  }

  public static GenericSpecification<ReportInfo> getSpecification(ReportFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate")
        .orderByFields("id", "name", "category", "template", "nextGenerationDate",
            "createdBy", "createdDate", "modifiedBy", "modifiedDate")
        .matchSearchFields("name", "description")
        .build();
    return new GenericSpecification<>(filters);
  }

  public static Set<SearchCriteria> getSearchCriteria(ReportSearchDto dto) {
    // Build the final filters
    return new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate")
        .orderByFields("id", "name", "category", "template", "nextGenerationDate",
            "createdBy", "createdDate", "modifiedBy", "modifiedDate")
        .matchSearchFields("name", "description")
        .build();
  }
}
