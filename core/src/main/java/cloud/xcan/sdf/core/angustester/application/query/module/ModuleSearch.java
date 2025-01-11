package cloud.xcan.sdf.core.angustester.application.query.module;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.module.Module;
import java.util.List;
import java.util.Set;

public interface ModuleSearch {

  List<Module> search(Set<SearchCriteria> criterias, Class<Module> cls, String... matches);
}
