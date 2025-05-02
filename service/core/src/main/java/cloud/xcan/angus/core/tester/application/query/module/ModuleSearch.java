package cloud.xcan.angus.core.tester.application.query.module;

import cloud.xcan.angus.core.tester.domain.module.Module;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.List;
import java.util.Set;

public interface ModuleSearch {

  List<Module> search(Set<SearchCriteria> criteria, Class<Module> cls, String... matches);
}
