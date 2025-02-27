package cloud.xcan.sdf.core.angustester.interfaces.func.facade.internal;

import static cloud.xcan.sdf.api.ApiConstant.RLimit.MAX_REPORT_ROWS;
import static cloud.xcan.sdf.api.CommonMessage.EXPORT_ROW_OVERT_LIMIT_CODE;
import static cloud.xcan.sdf.api.CommonMessage.EXPORT_ROW_OVERT_LIMIT_T;
import static cloud.xcan.sdf.core.angustester.interfaces.func.facade.internal.assembler.FuncCaseAssembler.getSearchCriteria;
import static cloud.xcan.sdf.core.angustester.interfaces.func.facade.internal.assembler.FuncCaseAssembler.toCaseListExportResource;
import static cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;
import static cloud.xcan.sdf.core.utils.ServletUtils.buildDownloadResourceResponseEntity;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.enums.Priority;
import cloud.xcan.sdf.core.angustester.application.cmd.func.FuncCaseCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.tag.TagTargetCmd;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncCaseQuery;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncCaseSearch;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncReviewCaseRecordQuery;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskQuery;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCase;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.sdf.core.angustester.domain.func.review.record.FuncReviewCaseRecord;
import cloud.xcan.sdf.core.angustester.domain.task.TaskInfo;
import cloud.xcan.sdf.core.angustester.domain.task.TaskType;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.FuncCaseFacade;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseAttachmentReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseImportDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseResultModifyDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseReviewDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseTagReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseTesterReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseWorkloadReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.internal.assembler.FuncCaseAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.FuncCaseDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.FuncCaseExportListVo;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.FuncCaseListVo;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.FuncCaseReviewVo;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.internal.assembler.TaskAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.TaskInfoVo;
import cloud.xcan.sdf.core.angustester.interfaces.version.facade.dto.SoftwareVersionRefReplaceDto;
import cloud.xcan.sdf.core.biz.BizAssert;
import cloud.xcan.sdf.core.biz.JoinSupplier;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
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
  private FuncCaseSearch funcCaseSearch;

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
    List<FuncCase> cases = dto.stream().map(FuncCaseAssembler::addDtoToDomain)
        .collect(Collectors.toList());
    return funcCaseCmd.add(cases);
  }

  @Override
  public void update(List<FuncCaseUpdateDto> dto) {
    List<FuncCase> funcCases = dto.stream()
        .map(FuncCaseAssembler::updateDtoToDomain).collect(Collectors.toList());
    funcCaseCmd.update(funcCases);
  }

  @Override
  public List<IdKey<Long, Object>> replace(List<FuncCaseReplaceDto> dto) {
    List<FuncCase> cases = dto.stream()
        .map(FuncCaseAssembler::replaceDtoToDomain).collect(Collectors.toList());
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
    funcCaseCmd.replaceSoftwareVersion(id,  Objects.isNull(dto) ? null : dto.getSoftwareVersion());
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
        .map(FuncCaseAssembler::resultModifyDtoToDomain).collect(Collectors.toList());
    funcCaseCmd.resultModify(cases, true);
  }

  @Override
  public void resultUpdate(List<FuncCaseResultModifyDto> dto) {
    List<FuncCase> cases = dto.stream()
        .map(FuncCaseAssembler::resultModifyDtoToDomain).collect(Collectors.toList());
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
        .map(FuncCaseAssembler::reviewDtoToDomain).collect(Collectors.toList());
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
        .collect(Collectors.toList());
  }

  @NameJoin
  @Override
  public List<FuncCaseListVo> notAssociatedCase(Long id, Long moduleId) {
    List<FuncCaseInfo> caseInfos = funcCaseQuery.notAssociatedCaseInCase(id, moduleId);
    return isEmpty(caseInfos) ? null : caseInfos.stream().map(FuncCaseAssembler::toListVo)
        .collect(Collectors.toList());
  }

  @NameJoin
  @Override
  public FuncCaseDetailVo detail(Long id) {
    FuncCase detail = funcCaseQuery.detail(id);
    return FuncCaseAssembler.toDetailVo(detail);
  }

  @NameJoin
  @Override
  public List<FuncCaseReviewVo> reviewList(Long id) {
    List<FuncReviewCaseRecord> caseReviews = funcReviewCaseRecordQuery.caseReview(id);
    return isEmpty(caseReviews) ? Collections.emptyList()
        : caseReviews.stream().map(FuncCaseAssembler::toReviewListVo).collect(Collectors.toList());
  }

  @NameJoin
  @Override
  public PageResult<FuncCaseListVo> list(FuncCaseFindDto dto) {
    Page<FuncCaseInfo> page = funcCaseQuery
        .list(FuncCaseAssembler.getSpecification(dto), dto.tranPage());
    return buildVoPageResult(page, FuncCaseAssembler::toListVo);
  }

  @NameJoin
  @Override
  public PageResult<FuncCaseListVo> search(boolean exportFlag, FuncCaseSearchDto dto) {
    Page<FuncCaseInfo> page = funcCaseSearch.search(exportFlag, getSearchCriteria(dto),
        dto.tranPage(), FuncCaseInfo.class, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, FuncCaseAssembler::toListVo);
  }

  /**
   * Note: The default maximum number of exported records, which needs to be exported in batches
   * according to conditions(created_date) when exceeding the limit.
   */
  @Override
  public ResponseEntity<org.springframework.core.io.Resource> export(
      FuncCaseSearchDto dto, HttpServletResponse response) {
    List<FuncCaseExportListVo> data = getExportFuncCaseData(dto);
    String fileName = "CaseListExport-" + System.currentTimeMillis() + ".xlsx";
    return buildDownloadResourceResponseEntity(-1, APPLICATION_OCTET_STREAM, fileName,
        0, toCaseListExportResource(data, fileName));
  }

  @NotNull
  private List<FuncCaseExportListVo> getExportFuncCaseData(FuncCaseSearchDto dto) {
    dto.setPageSize(200);
    PageResult<FuncCaseListVo> page = joinSupplier.execute(() -> search(true, dto));
    // 500 reads per time, with a maximum support for exporting MAX_REPORT_ROWS.
    BizAssert.assertTrue(page.getTotal() <= MAX_REPORT_ROWS,
        EXPORT_ROW_OVERT_LIMIT_CODE, EXPORT_ROW_OVERT_LIMIT_T, new Object[]{MAX_REPORT_ROWS});
    List<FuncCaseExportListVo> data = page.getList().stream().map(FuncCaseAssembler::toListVo)
        .collect(Collectors.toList());
    while (page.getList().size() >= 200) {
      dto.setPageNo(dto.getPageNo() + 1);
      page = search(true, dto);
      if (!page.isEmpty()) {
        data.addAll(page.getList().stream().map(FuncCaseAssembler::toListVo)
            .collect(Collectors.toList()));
      }
    }
    return data;
  }
}
