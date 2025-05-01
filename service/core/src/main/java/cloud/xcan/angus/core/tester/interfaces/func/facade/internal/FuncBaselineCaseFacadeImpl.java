package cloud.xcan.angus.core.tester.interfaces.func.facade.internal;

import static cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.angus.core.tester.interfaces.func.facade.internal.assembler.FuncBaselineCaseAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.func.facade.internal.assembler.FuncBaselineCaseAssembler.toDetailVo;
import static cloud.xcan.angus.core.tester.interfaces.func.facade.internal.assembler.FuncCaseAssembler.getSearchCriteria;
import static cloud.xcan.angus.core.tester.interfaces.func.facade.internal.assembler.FuncCaseAssembler.toCaseListExportResource;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;
import static cloud.xcan.angus.core.utils.CoreUtils.copyProperties;
import static cloud.xcan.angus.core.utils.ServletUtils.buildDownloadResourceResponseEntity;
import static cloud.xcan.angus.remote.ApiConstant.RLimit.MAX_REPORT_ROWS;
import static cloud.xcan.angus.remote.CommonMessage.EXPORT_ROW_OVERT_LIMIT_CODE;
import static cloud.xcan.angus.remote.CommonMessage.EXPORT_ROW_OVERT_LIMIT_T;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

import cloud.xcan.angus.core.biz.BizAssert;
import cloud.xcan.angus.core.biz.JoinSupplier;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.cmd.func.FuncBaselineCaseCmd;
import cloud.xcan.angus.core.tester.application.query.func.FuncBaselineCaseQuery;
import cloud.xcan.angus.core.tester.application.query.func.FuncBaselineCaseSearch;
import cloud.xcan.angus.core.tester.application.query.func.FuncBaselineQuery;
import cloud.xcan.angus.core.tester.application.query.func.FuncCaseQuery;
import cloud.xcan.angus.core.tester.application.query.func.FuncCaseSearch;
import cloud.xcan.angus.core.tester.domain.func.baseline.FuncBaseline;
import cloud.xcan.angus.core.tester.domain.func.baseline.FuncBaselineCase;
import cloud.xcan.angus.core.tester.domain.func.baseline.FuncBaselineCaseInfo;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCase;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.interfaces.func.facade.FuncBaselineCaseFacade;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.FuncCaseFindDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.FuncCaseSearchDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.internal.assembler.FuncBaselineCaseAssembler;
import cloud.xcan.angus.core.tester.interfaces.func.facade.internal.assembler.FuncCaseAssembler;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.FuncCaseDetailVo;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.FuncCaseExportListVo;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.FuncCaseListVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class FuncBaselineCaseFacadeImpl implements FuncBaselineCaseFacade {

  @Resource
  private FuncBaselineCaseCmd funcBaselineCaseCmd;

  @Resource
  private FuncBaselineCaseQuery funcBaselineCaseQuery;

  @Resource
  private FuncBaselineCaseSearch funcBaselineCaseSearch;

  @Resource
  private FuncCaseQuery funcCaseQuery;

  @Resource
  private FuncCaseSearch funcCaseSearch;

  @Resource
  private FuncBaselineQuery funcBaselineQuery;

  @Resource
  private JoinSupplier joinSupplier;

  @Override
  public void add(Long baselineId, HashSet<Long> caseIds) {
    funcBaselineCaseCmd.add(baselineId, caseIds);
  }

  @Override
  public void delete(Long baselineId, HashSet<Long> caseIds) {
    funcBaselineCaseCmd.delete(baselineId, caseIds);
  }

  @Override
  public FuncCaseDetailVo detail(Long baselineId, Long caseId) {
    FuncBaseline baselineDb = funcBaselineQuery.checkAndFind(baselineId);
    List<FuncBaselineCase> baselineCases = funcBaselineCaseQuery.detail(caseId);

    FuncCaseDetailVo currentBaselineCaseVo = null;
    Map<Integer, FuncCaseDetailVo> allVersionCaseVos = new HashMap<>();
    if (nonNull(baselineCases)) {
      List<FuncCaseDetailVo> allBaselineCaseVos = baselineCases.stream()
          .map(FuncBaselineCaseAssembler::toDetailVo).collect(Collectors.toList());
      allVersionCaseVos.putAll(allBaselineCaseVos.stream()
          .collect(Collectors.toMap(FuncCaseDetailVo::getVersion, x -> x)));
      if (baselineDb.getEstablished()) {
        currentBaselineCaseVo = toDetailVo(baselineCases.stream()
            .filter(x -> Objects.equals(baselineId, x.getBaselineId())).findFirst().orElse(null));
      }
    }

    FuncCase detail = funcCaseQuery.detail(caseId);
    FuncCaseDetailVo latestBaselineCaseVo = FuncCaseAssembler.toDetailVo(detail);
    if (isNull(currentBaselineCaseVo)) {
      currentBaselineCaseVo = latestBaselineCaseVo;
    } else {
      allVersionCaseVos.put(latestBaselineCaseVo.getVersion(),
          copyProperties(latestBaselineCaseVo, new FuncCaseDetailVo()));
    }

    // Fix :: Could not write JSON: Infinite recursion (StackOverflowError);
    // nested exception is com.fasterxml.jackson.databind.JsonMappingException: Infinite recursion (StackOverflowError)
    // (through reference chain: java.util.HashMap["1"]->cloud.xcan.angus.core.tester.interfaces.func.facade.vo.baseline
    allVersionCaseVos.put(currentBaselineCaseVo.getVersion(),
        copyProperties(currentBaselineCaseVo, new FuncCaseDetailVo()));
    currentBaselineCaseVo.setAllVersionCaseVos(allVersionCaseVos);

    return currentBaselineCaseVo;
  }

  @Override
  public PageResult<FuncCaseListVo> list(Long baselineId, FuncCaseFindDto dto) {
    FuncBaseline baselineDb = funcBaselineQuery.checkAndFind(baselineId);
    if (isEmpty(baselineDb.getCaseIds())) {
      return PageResult.empty();
    }
    if (baselineDb.getEstablished()) {
      Page<FuncBaselineCaseInfo> page = funcBaselineCaseQuery.list(baselineId,
          getSpecification(dto), dto.tranPage());
      return buildVoPageResult(page, FuncBaselineCaseAssembler::toListVo);
    }
    GenericSpecification<FuncCaseInfo> spc = FuncCaseAssembler.getSpecification(dto);
    spc.getCriteria().add(SearchCriteria.in("id", baselineDb.getCaseIds()));
    Page<FuncCaseInfo> page = funcCaseQuery.list(spc, dto.tranPage());
    return buildVoPageResult(page, FuncCaseAssembler::toListVo);
  }

  @Override
  public PageResult<FuncCaseListVo> search(Long baselineId, boolean export,
      FuncCaseSearchDto dto) {
    FuncBaseline baselineDb = funcBaselineQuery.checkAndFind(baselineId);
    if (isEmpty(baselineDb.getCaseIds())) {
      return PageResult.empty();
    }
    if (baselineDb.getEstablished()) {
      Page<FuncBaselineCaseInfo> page = funcBaselineCaseSearch.search(baselineId,
          export, getSearchCriteria(dto), dto.tranPage(), FuncBaselineCaseInfo.class,
          getMatchSearchFields(dto.getClass()));
      return buildVoPageResult(page, FuncBaselineCaseAssembler::toListVo);
    }
    Set<SearchCriteria> criteria = getSearchCriteria(dto);
    criteria.add(SearchCriteria.in("id", baselineDb.getCaseIds()));
    Page<FuncCaseInfo> page = funcCaseSearch.search(export, criteria, dto.tranPage(),
        FuncCaseInfo.class, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, FuncCaseAssembler::toListVo);
  }

  @Override
  public ResponseEntity<org.springframework.core.io.Resource> export(
      Long baselineId, FuncCaseSearchDto dto, HttpServletResponse response) {
    List<FuncCaseExportListVo> data = getExportFuncCaseData(baselineId, dto);
    String fileName = "BaselineCaseListExport-" + System.currentTimeMillis() + ".xlsx";
    return buildDownloadResourceResponseEntity(-1, APPLICATION_OCTET_STREAM, fileName,
        0, toCaseListExportResource(data, fileName));
  }

  @NotNull
  private List<FuncCaseExportListVo> getExportFuncCaseData(Long baselineId, FuncCaseSearchDto dto) {
    dto.setPageSize(200);
    PageResult<FuncCaseListVo> page = joinSupplier.execute(
        () -> search(baselineId, true, dto));
    // 500 reads per time, with a maximum support for exporting MAX_REPORT_ROWS.
    BizAssert.assertTrue(page.getTotal() <= MAX_REPORT_ROWS,
        EXPORT_ROW_OVERT_LIMIT_CODE, EXPORT_ROW_OVERT_LIMIT_T, new Object[]{MAX_REPORT_ROWS});
    List<FuncCaseExportListVo> data = page.getList().stream().map(FuncCaseAssembler::toListVo)
        .collect(Collectors.toList());
    while (page.getList().size() >= 200) {
      dto.setPageNo(dto.getPageNo() + 1);
      page = search(baselineId, true, dto);
      if (!page.isEmpty()) {
        data.addAll(page.getList().stream().map(FuncCaseAssembler::toListVo).toList());
      }
    }
    return data;
  }
}
