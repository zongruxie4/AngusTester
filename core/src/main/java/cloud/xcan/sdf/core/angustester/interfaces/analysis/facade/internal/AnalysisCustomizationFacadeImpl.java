package cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.internal;

import cloud.xcan.sdf.api.gm.analysis.dto.CustomizationSummaryDto;
import cloud.xcan.sdf.api.gm.analysis.vo.SummaryQueryDefinitionVo;
import cloud.xcan.sdf.api.manager.SimpleSummaryManager;
import cloud.xcan.sdf.api.manager.converter.AnalysisConverter;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.AnalysisCustomizationFacade;
import cloud.xcan.sdf.core.jpa.repository.SimpleSummaryRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
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
  public Map<String, Object> summary(List<CustomizationSummaryDto> dtos) {
    return simpleSummaryManager.getSummary(dtos.stream().map(AnalysisConverter::toQueryBuilder)
        .collect(Collectors.toList()));
  }

}
