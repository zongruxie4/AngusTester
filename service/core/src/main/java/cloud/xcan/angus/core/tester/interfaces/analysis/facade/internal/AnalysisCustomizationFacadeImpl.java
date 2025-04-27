package cloud.xcan.angus.core.tester.interfaces.analysis.facade.internal;

import cloud.xcan.angus.api.gm.analysis.dto.CustomizationSummaryDto;
import cloud.xcan.angus.api.gm.analysis.vo.SummaryQueryDefinitionVo;
import cloud.xcan.angus.api.manager.SimpleSummaryManager;
import cloud.xcan.angus.api.manager.converter.AnalysisConverter;
import cloud.xcan.angus.core.jpa.repository.SimpleSummaryRepository;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.AnalysisCustomizationFacade;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class AnalysisCustomizationFacadeImpl implements AnalysisCustomizationFacade {

  @Resource
  private SimpleSummaryManager simpleSummaryManager;

  @Override
  public SummaryQueryDefinitionVo definitions() {
    return AnalysisConverter.toQueryDefinition(SimpleSummaryRepository.REGISTER);
  }

  @Override
  public Object summary(CustomizationSummaryDto dto) {
    return simpleSummaryManager.getSummary(AnalysisConverter.toQueryBuilder(dto));
  }

  @Override
  public Map<String, Object> summary(List<CustomizationSummaryDto> dto) {
    return simpleSummaryManager.getSummary(dto.stream().map(AnalysisConverter::toQueryBuilder)
        .collect(Collectors.toList()));
  }

}
