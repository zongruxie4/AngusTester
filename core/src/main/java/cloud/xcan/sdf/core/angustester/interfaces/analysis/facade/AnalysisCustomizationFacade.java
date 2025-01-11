package cloud.xcan.sdf.core.angustester.interfaces.analysis.facade;


import cloud.xcan.sdf.api.gm.analysis.dto.CustomizationSummaryDto;
import cloud.xcan.sdf.api.gm.analysis.vo.SummaryQueryDefinitionVo;
import java.util.List;
import java.util.Map;

public interface AnalysisCustomizationFacade {

  SummaryQueryDefinitionVo definitions();

  Object summary(CustomizationSummaryDto dto);

  Map<String, Object> summary(List<CustomizationSummaryDto> dtos);

}
