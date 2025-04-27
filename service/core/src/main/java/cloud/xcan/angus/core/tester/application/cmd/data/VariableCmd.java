package cloud.xcan.angus.core.tester.application.cmd.data;

import cloud.xcan.angus.api.commonlink.apis.StrategyWhenDuplicated;
import cloud.xcan.angus.core.tester.domain.data.variables.Variable;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface VariableCmd {

  IdKey<Long, Object> add(Variable variable);

  void update(Variable variable);

  IdKey<Long, Object> replace(Variable variable);

  List<IdKey<Long, Object>> clone(HashSet<Long> ids);

  List<IdKey<Long, Object>> imports(Long projectId, StrategyWhenDuplicated strategyWhenDuplicated,
      String content, MultipartFile file);

  List<IdKey<Long, Object>> importExample(Long projectId);

  void delete(Collection<Long> ids);

}




