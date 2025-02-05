package cloud.xcan.sdf.core.angustester.application.cmd.data;

import cloud.xcan.sdf.api.commonlink.apis.StrategyWhenDuplicated;
import cloud.xcan.sdf.core.angustester.domain.data.dataset.Dataset;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface DatasetCmd {

  IdKey<Long, Object> add(Dataset dataset);

  void update(Dataset dataset);

  IdKey<Long, Object> replace(Dataset dataset);

  List<IdKey<Long, Object>> clone(HashSet<Long> ids);

  void delete(Collection<Long> ids);

  List<IdKey<Long, Object>> imports(Long projectId, StrategyWhenDuplicated strategyWhenDuplicated,
      String content, MultipartFile file);
}




