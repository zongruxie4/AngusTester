package cloud.xcan.angus.core.tester.interfaces.analysis.facade.internal;

import static cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.angus.core.tester.interfaces.analysis.facade.internal.assembler.AnalysisAssembler.getSearchCriteria;
import static cloud.xcan.angus.core.tester.interfaces.analysis.facade.internal.assembler.AnalysisAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.analysis.facade.internal.assembler.AnalysisAssembler.toAddDomain;
import static cloud.xcan.angus.core.tester.interfaces.analysis.facade.internal.assembler.AnalysisAssembler.toDetail;
import static cloud.xcan.angus.core.tester.interfaces.analysis.facade.internal.assembler.AnalysisAssembler.toReplaceDomain;
import static cloud.xcan.angus.core.tester.interfaces.analysis.facade.internal.assembler.AnalysisAssembler.toUpdateDomain;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;
import static cloud.xcan.angus.core.utils.ServletUtils.buildDownloadResourceResponseEntity;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.spring.condition.NotCommunityEditionCondition;
import cloud.xcan.angus.core.tester.application.cmd.analysis.AnalysisCmd;
import cloud.xcan.angus.core.tester.application.query.analysis.AnalysisQuery;
import cloud.xcan.angus.core.tester.application.query.analysis.AnalysisSearch;
import cloud.xcan.angus.core.tester.domain.analysis.Analysis;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.AnalysisFacade;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.AnalysisAddDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.AnalysisFindDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.AnalysisReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.AnalysisSearchDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.AnalysisUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.internal.assembler.AnalysisAssembler;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.vo.AnalysisDetailVo;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.vo.AnalysisListVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Conditional(NotCommunityEditionCondition.class)
@Component
public class AnalysisFacadeImpl implements AnalysisFacade {

  @Resource
  private AnalysisCmd analysisCmd;

  @Resource
  private AnalysisQuery analysisQuery;

  @Resource
  private AnalysisSearch analysisSearch;

  @Override
  public IdKey<Long, Object> add(AnalysisAddDto dto) {
    return analysisCmd.add(toAddDomain(dto));
  }

  @Override
  public void update(AnalysisUpdateDto dto) {
    analysisCmd.update(toUpdateDomain(dto));
  }

  @Override
  public IdKey<Long, Object> replace(AnalysisReplaceDto dto) {
    return analysisCmd.replace(toReplaceDomain(dto));
  }

  @Override
  public void refresh(Long id) {
    analysisCmd.refresh(id);
  }

  @Override
  public void delete(Collection<Long> ids) {
    analysisCmd.delete(ids);
  }

  @NameJoin
  @Override
  public AnalysisDetailVo detail(Long id) {
    return toDetail(analysisQuery.detail(id));
  }

  @NameJoin
  @Override
  public PageResult<AnalysisListVo> list(AnalysisFindDto dto) {
    Page<Analysis> page = analysisQuery.find(getSpecification(dto), dto.tranPage());
    return buildVoPageResult(page, AnalysisAssembler::toListVo);
  }

  @NameJoin
  @Override
  public PageResult<AnalysisListVo> search(AnalysisSearchDto dto) {
    Page<Analysis> page = analysisSearch.search(getSearchCriteria(dto),
        dto.tranPage(), Analysis.class, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, AnalysisAssembler::toListVo);
  }


  @SneakyThrows
  @Override
  public ResponseEntity<org.springframework.core.io.Resource> overviewExport(Long id,
      HttpServletResponse response) {
    File analysis = analysisQuery.overviewExport(id);
    return buildDownloadResourceResponseEntity(-1, APPLICATION_OCTET_STREAM,
        analysis.getName(), 0, new InputStreamResource(new FileInputStream(analysis)));
  }
}
