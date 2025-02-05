package cloud.xcan.sdf.core.angustester.interfaces.data.facade;

import java.util.Map;

public interface DataParameterizationFacade {

  Map<String, String> valuePreview(Long targetId, String targetType);

}
