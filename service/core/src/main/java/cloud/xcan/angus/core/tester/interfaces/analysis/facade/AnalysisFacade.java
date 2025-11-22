package cloud.xcan.angus.core.tester.interfaces.analysis.facade;

import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.AnalysisAddDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.AnalysisFindDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.AnalysisReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.AnalysisSearchDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.AnalysisUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.vo.AnalysisDetailVo;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.vo.AnalysisListVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collection;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface AnalysisFacade {

  IdKey<Long, Object> add(AnalysisAddDto dto);

  void update(AnalysisUpdateDto dto);

  IdKey<Long, Object> replace(AnalysisReplaceDto dto);

  void refresh(Long id);

  void delete(Collection<Long> ids);

  AnalysisDetailVo detail(Long id);

  PageResult<AnalysisListVo> list(AnalysisFindDto dto);

  PageResult<AnalysisListVo> search(AnalysisSearchDto dto);

  ResponseEntity<Resource> overviewExport(Long id, HttpServletResponse response);

}
