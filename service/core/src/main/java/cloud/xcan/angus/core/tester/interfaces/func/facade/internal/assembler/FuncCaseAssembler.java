package cloud.xcan.angus.core.tester.interfaces.func.facade.internal.assembler;

import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.EXPORT_CASE_LIST;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getTenantId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.arrayToLists;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static cloud.xcan.angus.spec.utils.ObjectUtils.stringSafe;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;

import cloud.xcan.angus.api.commonlink.TesterConstant;
import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.func.cases.CaseStepView;
import cloud.xcan.angus.core.tester.domain.func.cases.CaseTestResult;
import cloud.xcan.angus.core.tester.domain.func.cases.CaseTestStep;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCase;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.domain.func.review.record.FuncReviewCaseRecord;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.FuncCaseAddDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.FuncCaseFindDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.FuncCaseReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.FuncCaseResultModifyDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.FuncCaseReviewDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.FuncCaseSearchDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.FuncCaseUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.FuncCaseDetailVo;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.FuncCaseExportListVo;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.FuncCaseInfoVo;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.FuncCaseListVo;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.FuncCaseReviewVo;
import cloud.xcan.angus.core.tester.interfaces.task.facade.internal.assembler.TaskAssembler;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.TaskInfoVo;
import cloud.xcan.angus.core.utils.SpringAppDirUtils;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.remote.vo.IdAndNameVo;
import cloud.xcan.angus.spec.experimental.Assert;
import cloud.xcan.angus.spec.locale.MessageHolder;
import cloud.xcan.angus.spec.utils.FileUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.SimpleColumnWidthStyleStrategy;
import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.InputStreamResource;

public class FuncCaseAssembler {

  public static FuncCase addDtoToDomain(FuncCaseAddDto dto) {
    return new FuncCase()
        .setName(dto.getName())
        //.setCode(dto.getCode())
        .setVersion(1)
        .setPlanId(dto.getPlanId())
        .setModuleId(dto.getModuleId())
        .setSoftwareVersion(dto.getSoftwareVersion())
        .setPriority(nullSafe(dto.getPriority(), Priority.DEFAULT))
        .setDeadlineDate(dto.getDeadlineDate())
        .setOverdue(false)
        //.setEvalWorkloadMethod(dto.getEvalWorkloadMethod())
        .setEvalWorkload(dto.getEvalWorkload())
        .setActualWorkload(dto.getEvalWorkload())
        .setPrecondition(dto.getPrecondition())
        .setStepView(nullSafe(dto.getStepView(), CaseStepView.DEFAULT))
        .setSteps(dto.getSteps())
        .setDescription(dto.getDescription())
        //.setReviewStatus(dto.getReviewStatus())
        //.setReviewRemark(dto.getReviewRemark())
        .setReviewNum(0)
        .setReviewFailNum(0)
        .setTesterId(dto.getTesterId())
        .setDeveloperId(dto.getDeveloperId())
        .setTestNum(0)
        .setTestFailNum(0)
        .setTestResult(CaseTestResult.PENDING)
        .setAttachments(dto.getAttachments())
        .setTagIds(dto.getTagIds())
        .setRefTaskIds(dto.getRefTaskIds())
        .setRefCaseIds(dto.getRefCaseIds())
        .setPlanDeleted(false)
        .setDeleted(false);
  }

  public static FuncCase updateDtoToDomain(FuncCaseUpdateDto dto) {
    return new FuncCase()
        .setId(dto.getId())
        .setName(dto.getName())
        //.setCode(dto.getCode())
        // No modification allowed, only movement allowed
        //.setPlanId(dto.getPlanId())
        .setModuleId(dto.getModuleId())
        .setSoftwareVersion(dto.getSoftwareVersion())
        //.setCaseType(dto.getCaseType())
        .setPriority(dto.getPriority())
        .setDeadlineDate(dto.getDeadlineDate())
        //.setEvalWorkloadMethod(dto.getEvalWorkloadMethod())
        .setEvalWorkload(dto.getEvalWorkload())
        .setActualWorkload(dto.getActualWorkload())
        .setPrecondition(dto.getPrecondition())
        .setStepView(dto.getStepView())
        .setSteps(dto.getSteps())
        .setDescription(dto.getDescription())
        //.setReviewStatus(dto.getReviewStatus())
        //.setReviewRemark(dto.getReviewRemark())
        //.setReviewNum(0)
        .setTesterId(dto.getTesterId())
        .setDeveloperId(dto.getDeveloperId())
        //.setTestResult(dto.getTestResult())
        .setTagIds(dto.getTagIds())
        .setAttachments(dto.getAttachments())
        .setRefTaskIds(dto.getRefTaskIds())
        .setRefCaseIds(dto.getRefCaseIds());
  }

  public static FuncCase replaceDtoToDomain(FuncCaseReplaceDto dto) {
    FuncCase funcCase = new FuncCase().setId(dto.getId())
        .setName(dto.getName())
        //.setCode(dto.getCode())
        .setVersion(isNull(dto.getId()) ? 1 : null)
        // No modification allowed, only movement allowed
        .setPlanId(isNull(dto.getId()) ? dto.getPlanId() : null)
        .setModuleId(dto.getModuleId())
        .setSoftwareVersion(dto.getSoftwareVersion())
        .setPriority(nullSafe(dto.getPriority(), Priority.DEFAULT))
        .setDeadlineDate(dto.getDeadlineDate())
        .setOverdue(isNull(dto.getId()) ? false : null)
        //.setEvalWorkloadMethod(dto.getEvalWorkloadMethod())
        .setEvalWorkload(dto.getEvalWorkload())
        .setActualWorkload(dto.getActualWorkload())
        .setPrecondition(dto.getPrecondition())
        .setStepView(nullSafe(dto.getStepView(), CaseStepView.DEFAULT))
        .setSteps(dto.getSteps())
        .setDescription(dto.getDescription())
        //.setReviewStatus(dto.getReviewStatus())
        //.setReviewRemark(dto.getReviewRemark())
        .setTesterId(dto.getTesterId())
        .setDeveloperId(dto.getDeveloperId())
        //.setTestResult(dto.getTestResult())
        .setTagIds(dto.getTagIds())
        .setAttachments(dto.getAttachments())
        .setRefTaskIds(dto.getRefTaskIds())
        .setRefCaseIds(dto.getRefCaseIds());
    if (isNull(dto.getId())) { // add
      funcCase.setReviewNum(0)
          .setReviewFailNum(0)
          .setTestResult(CaseTestResult.PENDING)
          .setTestNum(0)
          .setTestFailNum(0)
          .setPlanDeleted(false)
          .setDeleted(false);
    }
    return funcCase;
  }

  public static FuncCase resultModifyDtoToDomain(FuncCaseResultModifyDto dto) {
    return new FuncCase().setId(dto.getId())
        .setTestResult(dto.getTestResult())
        .setEvalWorkload(dto.getEvalWorkload())
        .setActualWorkload(dto.getActualWorkload())
        .setTestRemark(dto.getTestRemark());
  }

  public static FuncCase reviewDtoToDomain(FuncCaseReviewDto dto) {
    return new FuncCase().setId(dto.getId())
        .setReviewStatus(dto.getReviewStatus())
        .setReviewRemark(dto.getReviewRemark());
  }

  public static FuncCaseDetailVo toDetailVo(FuncCase case0) {
    return new FuncCaseDetailVo().setId(case0.getId())
        .setName(case0.getName())
        .setCode(case0.getCode())
        .setVersion(case0.getVersion())
        .setProjectId(case0.getProjectId())
        .setPlanId(case0.getPlanId())
        .setPlanAuth(case0.getPlanAuth())
        .setModuleId(case0.getModuleId())
        .setSoftwareVersion(case0.getSoftwareVersion())
        .setPriority(case0.getPriority())
        .setDeadlineDate(case0.getDeadlineDate())
        .setOverdue(case0.getOverdue())
        .setEvalWorkloadMethod(case0.getEvalWorkloadMethod())
        .setEvalWorkload(case0.getEvalWorkload())
        .setActualWorkload(case0.getActualWorkload())
        .setPrecondition(case0.getPrecondition())
        .setStepView(nullSafe(case0.getStepView(), CaseStepView.DEFAULT))
        .setSteps(case0.getSteps())
        .setDescription(case0.getDescription())
        .setReview(case0.getReview())
        .setReviewerId(case0.getReviewerId())
        .setReviewDate(case0.getReviewDate())
        .setReviewStatus(case0.getReviewStatus())
        .setReviewRemark(case0.getReviewRemark())
        .setReviewNum(case0.getReviewNum())
        .setReviewFailNum(case0.getReviewFailNum())
        .setTesterId(case0.getTesterId())
        .setDeveloperId(case0.getDeveloperId())
        .setUnplanned(case0.getUnplanned())
        .setTestNum(case0.getTestNum())
        .setTestFailNum(case0.getTestFailNum())
        .setTestResult(case0.getTestResult())
        .setTestRemark(case0.getTestRemark())
        .setTestResultHandleDate(case0.getTestResultHandleDate())
        .setAttachments(case0.getAttachments())
        .setTags(isNotEmpty(case0.getTagTargets()) ? case0.getTagTargets().stream()
            .map(o -> new IdAndNameVo().setId(o.getTagId()).setName(o.getTagName()))
            .collect(Collectors.toList()) : Collections.emptyList())
        .setRefTaskInfos(isNotEmpty(case0.getAssocTasks()) ? case0.getAssocTasks().stream()
            .map(TaskAssembler::toInfoVo).collect(Collectors.toList()) : emptyList())
        .setRefCaseInfos(isNotEmpty(case0.getAssocCases()) ? case0.getAssocCases().stream()
            .map(FuncCaseAssembler::toInfoVo).collect(Collectors.toList()) : emptyList())
        .setProgress(case0.getProgress())
        .setFavourite(case0.getFavourite())
        .setFollow(case0.getFollow())
        .setCommentNum(case0.getCommentNum())
        .setActivityNum(case0.getActivityNum())
        .setTenantId(case0.getTenantId())
        .setCreatedBy(case0.getCreatedBy())
        .setCreatedDate(case0.getCreatedDate())
        .setAvatar(case0.getAvatar())
        .setLastModifiedBy(case0.getLastModifiedBy())
        .setLastModifiedDate(case0.getLastModifiedDate());
  }

  public static FuncCaseListVo toListVo(FuncCaseInfo case0) {
    return new FuncCaseListVo().setId(case0.getId())
        .setName(case0.getName())
        .setCode(case0.getCode())
        .setVersion(case0.getVersion())
        .setPlanId(case0.getPlanId())
        .setPlanAuth(case0.getPlanAuth())
        .setModuleId(case0.getModuleId())
        .setSoftwareVersion(case0.getSoftwareVersion())
        .setPriority(case0.getPriority())
        .setDeadlineDate(case0.getDeadlineDate())
        .setOverdue(case0.getOverdue())
        .setEvalWorkloadMethod(case0.getEvalWorkloadMethod())
        .setEvalWorkload(case0.getEvalWorkload())
        .setActualWorkload(case0.getActualWorkload())
        .setPrecondition(case0.getPrecondition())
        .setSteps(case0.getSteps())
        .setRefTaskInfos(isNotEmpty(case0.getAssocTasks()) ? case0.getAssocTasks().stream()
            .map(TaskAssembler::toInfoVo).collect(Collectors.toList()) : emptyList())
        .setRefCaseInfos(isNotEmpty(case0.getAssocCases()) ? case0.getAssocCases().stream()
            .map(FuncCaseAssembler::toInfoVo).collect(Collectors.toList()) : emptyList())
        .setDescription(case0.getDescription())
        .setReview(case0.getReview())
        .setReviewerId(case0.getReviewerId())
        .setReviewDate(case0.getReviewDate())
        .setReviewStatus(case0.getReviewStatus())
        .setReviewRemark(case0.getReviewRemark())
        .setReviewNum(case0.getReviewNum())
        .setTesterId(case0.getTesterId())
        .setTesterName(case0.getTesterName())
        .setTesterAvatar(case0.getTesterAvatar())
        .setDeveloperId(case0.getDeveloperId())
        .setUnplanned(case0.getUnplanned())
        .setTestNum(case0.getTestNum())
        .setTestFailNum(case0.getTestFailNum())
        .setReviewFailNum(case0.getReviewFailNum())
        .setTestResult(case0.getTestResult())
        .setTestRemark(case0.getTestRemark())
        .setTestResultHandleDate(case0.getTestResultHandleDate())
        .setTags(isNotEmpty(case0.getTagTargets()) ? case0.getTagTargets().stream()
            .map(o -> new IdAndNameVo().setId(o.getTagId()).setName(o.getTagName()))
            .collect(Collectors.toList()) : Collections.emptyList())
        .setProgress(case0.getProgress())
        .setFavourite(case0.getFavourite())
        .setFollow(case0.getFollow())
        .setTenantId(case0.getTenantId())
        .setCreatedBy(case0.getCreatedBy())
        .setCreatedByName(case0.getCreatedByName())
        .setCreatedDate(case0.getCreatedDate())
        .setAvatar(case0.getAvatar())
        .setLastModifiedBy(case0.getLastModifiedBy())
        .setLastModifiedDate(case0.getLastModifiedDate());
  }

  public static FuncCaseReviewVo toReviewListVo(FuncReviewCaseRecord reviewCase) {
    return new FuncCaseReviewVo()
        .setId(reviewCase.getId())
        .setPlanId(reviewCase.getPlanId())
        //.setPlanName(reviewCase.getPlanName())
        .setReviewCaseId(reviewCase.getReviewCaseId())
        .setReviewId(reviewCase.getReviewId())
        //.setReviewName(reviewCase.getReviewName())
        .setCaseId(reviewCase.getCaseId())
        .setReviewedCase(reviewCase.getReviewedCase())
        //.setLatestCase(reviewCase.getLatestCase())
        .setReviewerId(reviewCase.getReviewerId())
        //.setReviewerName(reviewCase.getReviewerName())
        .setReviewDate(reviewCase.getReviewDate())
        .setReviewStatus(reviewCase.getReviewStatus())
        .setReviewRemark(reviewCase.getReviewRemark())
        .setCreatedBy(reviewCase.getCreatedBy())
        .setCreatedByName(reviewCase.getCreatedByName())
        .setAvatar(reviewCase.getAvatar())
        .setCreatedDate(reviewCase.getCreatedDate());
  }

  public static FuncCaseInfoVo toInfoVo(FuncCaseInfo case0) {
    return new FuncCaseInfoVo()
        .setId(case0.getId())
        .setCode(case0.getCode())
        .setName(case0.getName())
        .setVersion(case0.getVersion())
        .setSoftwareVersion(case0.getSoftwareVersion())
        .setOverdue(case0.getOverdue())
        .setReview(case0.getReview())
        .setPriority(case0.getPriority())
        .setEvalWorkload(case0.getEvalWorkload())
        .setDeadlineDate(case0.getDeadlineDate())
        .setDescription(case0.getDescription())
        .setTesterId(case0.getTesterId())
        .setTesterName(case0.getTesterName())
        .setTesterAvatar(case0.getTesterAvatar())
        .setUnplanned(case0.getUnplanned())
        .setReviewStatus(case0.getReviewStatus())
        .setReviewRemark(case0.getReviewRemark())
        .setTestResult(case0.getTestResult())
        .setTestRemark(case0.getTestRemark());
  }

  public static FuncCaseExportListVo toListVo(FuncCaseListVo listVo) {
    FuncCaseExportListVo vo = new FuncCaseExportListVo()
        .setPlanName(listVo.getPlanName())
        .setModuleName(listVo.getModuleName())
        .setName(listVo.getName())
        .setCode(listVo.getCode())
        .setPriority(listVo.getPriority())
        .setDeadlineDate(listVo.getDeadlineDate())
        .setOverdue(listVo.getOverdue())
        .setEvalWorkloadMethod(listVo.getEvalWorkloadMethod())
        .setEvalWorkload(listVo.getEvalWorkload())
        .setActualWorkload(listVo.getActualWorkload())
        .setPrecondition(listVo.getPrecondition())
        .setDescription(listVo.getDescription())
        .setReviewerName(listVo.getReviewerName())
        .setReviewDate(listVo.getReviewDate())
        .setReviewStatus(listVo.getReviewStatus())
        .setReviewRemark(listVo.getReviewRemark())
        .setReviewNum(listVo.getReviewNum())
        .setTesterName(listVo.getTesterName())
        .setDeveloperName(listVo.getDeveloperName())
        .setUnplanned(listVo.getUnplanned())
        .setTestNum(listVo.getTestNum())
        .setTestFailNum(listVo.getTestFailNum())
        .setReviewFailNum(listVo.getReviewFailNum())
        .setTestResult(listVo.getTestResult())
        .setTestRemark(listVo.getTestRemark())
        .setTestResultHandleDate(listVo.getTestResultHandleDate())
        .setRefTags(isNotEmpty(listVo.getTags()) ? listVo.getTags().stream()
            .map(IdAndNameVo::getName).collect(Collectors.toList()) : Collections.emptyList())
        .setCreatedByName(listVo.getCreatedByName())
        .setCreatedDate(listVo.getCreatedDate())
        .setLastModifiedByName(listVo.getLastModifiedByName())
        .setLastModifiedDate(listVo.getLastModifiedDate());
    if (isNotEmpty(listVo.getRefTaskInfos())) {
      vo.setRefTasks(listVo.getRefTaskInfos().stream().map(TaskInfoVo::getName)
          .collect(Collectors.toList()));
    }
    if (isNotEmpty(listVo.getRefCaseInfos())) {
      vo.setRefCases(listVo.getRefCaseInfos().stream().map(FuncCaseInfoVo::getName)
          .collect(Collectors.toList()));
    }
    if (isNotEmpty(listVo.getSteps())) {
      StringBuilder stringBuilder = new StringBuilder();
      for (CaseTestStep step : listVo.getSteps()) {
        if (isNotEmpty(step.getExpectedResult())) {
          stringBuilder.append(stringSafe(step.getStep())).append("##")
              .append(step.getExpectedResult());
        } else {
          stringBuilder.append(step.getStep());
        }
        //stringBuilder.append("@@\n");
        stringBuilder.append("@@\n");
      }
      vo.setSteps(stringBuilder.toString());
    }
    return vo;
  }

  @SneakyThrows
  @NotNull
  public static InputStreamResource toCaseListExportResource(List<FuncCaseExportListVo> counts,
      String fileName) {
    String filePath = new SpringAppDirUtils().getTmpDir() + TesterConstant.EXPORT_SUMMARY_DIR
        + getTenantId() + File.separator + fileName;
    File file = new File(filePath);
    FileUtils.forceMkdirParent(file);
    String headerMessage = MessageHolder.message(EXPORT_CASE_LIST);
    Assert.assertNotEmpty(headerMessage, "CaseListExport message not configured");
    EasyExcel.write(file, FuncCaseExportListVo.class)
        .head(arrayToLists(headerMessage.split(","))).sheet()
        //.head(ApisCaseListVo.class).sheet()
        .registerWriteHandler(new SimpleColumnWidthStyleStrategy(25))
        .doWrite(() -> counts);
    return new InputStreamResource(new FileInputStream(filePath));
  }

  public static GenericSpecification<FuncCaseInfo> getSpecification(FuncCaseFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("deadlineDate", "createdDate", "lastModifiedDate", "reviewDate",
            "testResultHandleDate", "reviewNum", "testNum", "testFailNum")
        .inAndNotFields("id", "tagId", "testResult", "testerId", "developerId")
        .orderByFields("id", "createdDate", "lastModifiedDate", "priority",
            "deadlineDate", "reviewStatus", "reviewNum", "testerId", "developerId", "reviewerId",
            "testNum", "testFailNum", "testResult", "reviewDate", "testResultHandleDate")
        .matchSearchFields("name", "description", "code")
        .build();
    return new GenericSpecification<>(filters);
  }

  public static Set<SearchCriteria> getSearchCriteria(FuncCaseSearchDto dto) {
    // Build the final filters
    return new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("deadlineDate", "createdDate", "lastModifiedDate",
            "reviewDate", "testResultHandleDate", "reviewNum", "testNum", "testFailNum")
        .inAndNotFields("id", "tagId", "testResult", "testerId", "developerId")
        .orderByFields("id", "createdDate", "lastModifiedDate", "priority",
            "deadlineDate", "reviewStatus", "reviewNum", "testerId", "developerId", "reviewerId",
            "testNum", "testFailNum", "testResult", "reviewDate", "testResultHandleDate")
        .matchSearchFields("name", "description", "code")
        .build();
  }

}
