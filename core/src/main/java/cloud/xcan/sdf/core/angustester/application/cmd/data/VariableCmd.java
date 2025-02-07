package cloud.xcan.sdf.core.angustester.application.cmd.data;

import cloud.xcan.sdf.api.commonlink.apis.StrategyWhenDuplicated;
import cloud.xcan.sdf.core.angustester.domain.data.variables.Variable;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface VariableCmd {

  IdKey<Long, Object> add(Variable variable);

  void update(Variable variable);

  IdKey<Long, Object> replace(Variable variable);

  List<IdKey<Long, Object>> clone(HashSet<Long> ids);

  void delete(Collection<Long> ids);

  List<IdKey<Long, Object>> exampleImport(Long projectId);

  List<IdKey<Long, Object>> imports(Long projectId, StrategyWhenDuplicated strategyWhenDuplicated,
      String content, MultipartFile file);

}
