package cloud.xcan.angus.core.tester.interfaces.test.facade.internal;


import static cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.angus.core.tester.interfaces.test.facade.internal.assembler.FuncCaseAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.test.facade.internal.assembler.FuncCaseAssembler.toCaseListExportResource;
import static cloud.xcan.angus.core.tester.interfaces.test.facade.internal.assembler.FuncCaseAssembler.toDetailVo;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;
import static cloud.xcan.angus.core.utils.ServletUtils.buildDownloadResourceResponseEntity;
import static cloud.xcan.angus.remote.ApiConstant.RLimit.MAX_REPORT_ROWS;
import static cloud.xcan.angus.remote.CommonMessage.EXPORT_ROW_OVERT_LIMIT_CODE;
import static cloud.xcan.angus.remote.CommonMessage.EXPORT_ROW_OVERT_LIMIT_T;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.core.biz.BizAssert;
import cloud.xcan.angus.core.biz.JoinSupplier;
import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.project.TagTargetCmd;
import cloud.xcan.angus.core.tester.application.cmd.test.FuncCaseCmd;
import cloud.xcan.angus.core.tester.application.query.issue.TaskQuery;
import cloud.xcan.angus.core.tester.application.query.test.FuncCaseQuery;
import cloud.xcan.angus.core.tester.application.query.test.FuncReviewCaseRecordQuery;
import cloud.xcan.angus.core.tester.domain.issue.TaskInfo;
import cloud.xcan.angus.core.tester.domain.issue.TaskType;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCase;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.domain.test.review.record.FuncReviewCaseRecord;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.internal.assembler.TaskAssembler;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.vo.TaskInfoVo;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.SoftwareVersionRefReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.FuncCaseFacade;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.FuncCaseAddDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.FuncCaseAttachmentReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.FuncCaseFindDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.FuncCaseImportDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.FuncCaseReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.FuncCaseResultModifyDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.FuncCaseReviewDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.FuncCaseTagReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.FuncCaseTesterReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.FuncCaseUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.FuncCaseWorkloadReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.internal.assembler.FuncCaseAssembler;
import cloud.xcan.angus.core.tester.interfaces.test.facade.vo.FuncCaseDetailVo;
import cloud.xcan.angus.core.tester.interfaces.test.facade.vo.FuncCaseExportListVo;
import cloud.xcan.angus.core.tester.interfaces.test.facade.vo.FuncCaseListVo;
import cloud.xcan.angus.core.tester.interfaces.test.facade.vo.FuncCaseReviewVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class FuncCaseFacadeImpl implements FuncCaseFacade {

  @Resource
  private FuncCaseCmd funcCaseCmd;

  @Resource
  private FuncCaseQuery funcCaseQuery;

  @Resource
  private TaskQuery taskQuery;

  @Resource
  private FuncReviewCaseRecordQuery funcReviewCaseRecordQuery;

  @Resource
  private TagTargetCmd funcCaseTagTargetCmd;

  @Resource
  private JoinSupplier joinSupplier;

  @Override
  public List<IdKey<Long, Object>> add(List<FuncCaseAddDto> dto) {
    List<FuncCase> cases = dto.stream().map(FuncCaseAssembler::addDtoToDomain).toList();
    return funcCaseCmd.add(cases);
  }

  @Override
  public void update(List<FuncCaseUpdateDto> dto) {
    List<FuncCase> funcCases = dto.stream()
        .map(FuncCaseAssembler::updateDtoToDomain).toList();
    funcCaseCmd.update(funcCases);
  }

  @Override
  public List<IdKey<Long, Object>> replace(List<FuncCaseReplaceDto> dto) {
    List<FuncCase> cases = dto.stream()
        .map(FuncCaseAssembler::replaceDtoToDomain).toList();
    return funcCaseCmd.replace(cases);
  }

  @Override
  public void rename(Long id, String name) {
    funcCaseCmd.rename(id, name);
  }

  @Override
  public List<IdKey<Long, Object>> clone(Set<Long> ids) {
    return funcCaseCmd.clone(ids);
  }

  @Override
  public void move(Set<Long> ids, Long targetPlanId) {
    funcCaseCmd.move(ids, targetPlanId);
  }

  @Override
  public void replaceTester(Long id, FuncCaseTesterReplaceDto dto) {
    funcCaseCmd.replaceTester(id, dto.getTesterId());
  }

  @Override
  public void replaceTag(Long id, FuncCaseTagReplaceDto dto) {
    funcCaseTagTargetCmd.replaceCaseTags(id, Objects.isNull(dto) ? null : dto.getTagIds());
  }

  @Override
  public void replaceDeadline(Long id, LocalDateTime deadlineDate) {
    funcCaseCmd.replaceDeadline(id, deadlineDate);
  }

  @Override
  public void replacePriority(Long id, Priority priority) {
    funcCaseCmd.replacePriority(id, priority);
  }

  @Override
  public void replaceSoftwareVersion(Long id, SoftwareVersionRefReplaceDto dto) {
    funcCaseCmd.replaceSoftwareVersion(id, Objects.isNull(dto) ? null : dto.getSoftwareVersion());
  }

  @Override
  public void replaceEvalWorkload(Long id, FuncCaseWorkloadReplaceDto dto) {
    funcCaseCmd.replaceEvalWorkload(id, Objects.isNull(dto) ? null : dto.getWorkload());
  }

  @Override
  public void replaceActualWorkload(Long id, FuncCaseWorkloadReplaceDto dto) {
    funcCaseCmd.replaceActualWorkload(id, Objects.isNull(dto) ? null : dto.getWorkload());
  }

  @Override
  public void replaceAttachment(Long id, FuncCaseAttachmentReplaceDto dto) {
    funcCaseCmd.replaceAttachment(id, Objects.isNull(dto) ? null : dto.getAttachments());
  }

  @Override
  public void resultReplace(List<FuncCaseResultModifyDto> dto) {
    List<FuncCase> cases = dto.stream()
        .map(FuncCaseAssembler::resultModifyDtoToDomain).toList();
    funcCaseCmd.resultModify(cases, true);
  }

  @Override
  public void resultUpdate(List<FuncCaseResultModifyDto> dto) {
    List<FuncCase> cases = dto.stream()
        .map(FuncCaseAssembler::resultModifyDtoToDomain).toList();
    funcCaseCmd.resultModify(cases, false);
  }

  @Override
  public void resultReset(HashSet<Long> ids) {
    funcCaseCmd.resultReset(ids);
  }

  @Override
  public void retest(HashSet<Long> ids) {
    funcCaseCmd.retest(ids);
  }

  @Override
  public void review(List<FuncCaseReviewDto> dto) {
    List<FuncCase> cases = dto.stream()
        .map(FuncCaseAssembler::reviewDtoToDomain).toList();
    funcCaseCmd.review(cases);
  }

  @Override
  public void reviewReset(HashSet<Long> ids) {
    funcCaseCmd.reviewReset(ids);
  }

  @Override
  public void taskAssocAdd(Long id, HashSet<Long> assocTaskIds) {
    funcCaseCmd.taskAssocAdd(id, assocTaskIds);
  }

  @Override
  public void taskAssocCancel(Long id, HashSet<Long> assocTaskIds) {
    funcCaseCmd.taskAssocCancel(id, assocTaskIds);
  }

  @Override
  public void caseAssocAdd(Long id, HashSet<Long> assocCaseIds) {
    funcCaseCmd.caseAssocAdd(id, assocCaseIds);
  }

  @Override
  public void caseAssocCancel(Long id, HashSet<Long> assocCaseIds) {
    funcCaseCmd.caseAssocCancel(id, assocCaseIds);
  }

  @Override
  public List<IdKey<Long, Object>> imports(FuncCaseImportDto dto) {
    return funcCaseCmd.imports(dto.getPlanId(), dto.getStrategyWhenDuplicated(), dto.getFile());
  }

  @Override
  public List<IdKey<Long, Object>> importExample(Long projectId) {
    return funcCaseCmd.importExample(projectId);
  }

  @Override
  public void delete(Collection<Long> ids) {
    funcCaseCmd.delete(ids);
  }

  @Override
  public List<TaskInfoVo> notAssociatedTask(Long id, Long moduleId, @Nullable TaskType taskType) {
    List<TaskInfo> caseInfos = taskQuery.notAssociatedTaskInCase(id, moduleId, taskType);
    return isEmpty(caseInfos) ? null : caseInfos.stream().map(TaskAssembler::toInfoVo)
        .toList();
  }

  @NameJoin
  @Override
  public List<FuncCaseListVo> notAssociatedCase(Long id, Long moduleId) {
    List<FuncCaseInfo> caseInfos = funcCaseQuery.notAssociatedCaseInCase(id, moduleId);
    return isEmpty(caseInfos) ? null : caseInfos.stream().map(FuncCaseAssembler::toListVo)
        .toList();
  }

  @NameJoin
  @Override
  public FuncCaseDetailVo detail(Long id) {
    return toDetailVo(funcCaseQuery.detail(id));
  }

  @NameJoin
  @Override
  public List<FuncCaseReviewVo> reviewList(Long id) {
    List<FuncReviewCaseRecord> caseReviews = funcReviewCaseRecordQuery.caseReview(id);
    return isEmpty(caseReviews) ? Collections.emptyList()
        : caseReviews.stream().map(FuncCaseAssembler::toReviewListVo).toList();
  }

  @NameJoin
  @Override
  public PageResult<FuncCaseListVo> list(boolean export, FuncCaseFindDto dto) {
    Page<FuncCaseInfo> page = funcCaseQuery.list(export, getSpecification(dto), dto.tranPage(),
        dto.fullTextSearch, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, FuncCaseAssembler::toListVo);
  }

  /**
   * Note: The default maximum number of exported records, which needs to be exported in batches
   * according to conditions(created_date) when exceeding the limit.
   */
  @Override
  public ResponseEntity<org.springframework.core.io.Resource> export(
      FuncCaseFindDto dto, HttpServletResponse response) {
    List<FuncCaseExportListVo> data = getExportFuncCaseData(dto);
    String fileName = "CaseListExport-" + System.currentTimeMillis() + ".xlsx";
    return buildDownloadResourceResponseEntity(-1, APPLICATION_OCTET_STREAM, fileName,
        0, toCaseListExportResource(data, fileName));
  }

  @NotNull
  private List<FuncCaseExportListVo> getExportFuncCaseData(FuncCaseFindDto dto) {
    dto.setPageSize(200);
    PageResult<FuncCaseListVo> page = joinSupplier.execute(() -> list(true, dto));
    // 500 reads per time, with a maximum support for exporting MAX_REPORT_ROWS.
    BizAssert.assertTrue(page.getTotal() <= MAX_REPORT_ROWS,
        EXPORT_ROW_OVERT_LIMIT_CODE, EXPORT_ROW_OVERT_LIMIT_T, new Object[]{MAX_REPORT_ROWS});
    List<FuncCaseExportListVo> data = page.getList().stream().map(FuncCaseAssembler::toListVo)
        .collect(Collectors.toList());
    while (page.getList().size() >= 200) {
      dto.setPageNo(dto.getPageNo() + 1);
      page = list(true, dto);
      if (!page.isEmpty()) {
        data.addAll(page.getList().stream().map(FuncCaseAssembler::toListVo).toList());
      }
    }
    return data;
  }
}
