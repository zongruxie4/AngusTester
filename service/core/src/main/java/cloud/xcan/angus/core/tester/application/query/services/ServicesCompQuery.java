package cloud.xcan.angus.core.tester.application.query.services;

import cloud.xcan.angus.core.tester.domain.services.comp.ServicesComp;
import cloud.xcan.angus.core.tester.domain.services.comp.ServicesCompType;
import io.swagger.v3.oas.models.Components;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ServicesCompQuery {

  ServicesComp detailByRef(Long serviceId, String ref);

  List<ServicesComp> listByType(Long serviceId, Set<ServicesCompType> types,
      Set<String> keys);

  List<ServicesComp> listByRef(Long serviceId, Set<String> refs);

  List<ServicesComp> listAll(Long serviceId);

  List<ServicesComp> findByServiceId(Long id);

  Components findOpenAPIComponents(Long serviceId);

  void checkRefFormat(String ref);

  void checkRefFormat(Collection<String> refs);

}
