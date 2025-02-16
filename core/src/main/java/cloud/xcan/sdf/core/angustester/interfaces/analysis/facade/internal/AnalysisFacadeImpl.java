package cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.internal;

import static cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;
import static cloud.xcan.sdf.core.utils.ServletUtils.buildDownloadResourceResponseEntity;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.application.cmd.analysis.AnalysisCmd;
import cloud.xcan.sdf.core.angustester.application.query.analysis.AnalysisQuery;
import cloud.xcan.sdf.core.angustester.application.query.analysis.AnalysisSearch;
import cloud.xcan.sdf.core.angustester.domain.analysis.Analysis;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.AnalysisFacade;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.AnalysisAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.AnalysisFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.AnalysisReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.AnalysisSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.AnalysisUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.internal.assembler.AnalysisAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.vo.AnalysisDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.vo.AnalysisListVo;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.core.spring.condition.NotCommunityEditionCondition;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
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
    Analysis analysis = AnalysisAssembler.toAddDomain(dto);
    return analysisCmd.add(analysis);
  }

  @Override
  public void update(AnalysisUpdateDto dto) {
    Analysis analysis = AnalysisAssembler.toUpdateDomain(dto);
    analysisCmd.update(analysis);
  }

  @Override
  public IdKey<Long, Object> replace(AnalysisReplaceDto dto) {
    Analysis analysis = AnalysisAssembler.toReplaceDomain(dto);
    return analysisCmd.replace(analysis);
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
    Analysis analysis = analysisQuery.detail(id);
    return AnalysisAssembler.toDetail(analysis);
  }

  @NameJoin
  @Override
  public PageResult<AnalysisListVo> list(AnalysisFindDto dto) {
    Page<Analysis> page = analysisQuery.find(AnalysisAssembler.getSpecification(dto),
        dto.tranPage());
    return buildVoPageResult(page, AnalysisAssembler::toListVo);
  }

  @NameJoin
  @Override
  public PageResult<AnalysisListVo> search(AnalysisSearchDto dto) {
    Page<Analysis> page = analysisSearch.search(AnalysisAssembler.getSearchCriteria(dto),
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
