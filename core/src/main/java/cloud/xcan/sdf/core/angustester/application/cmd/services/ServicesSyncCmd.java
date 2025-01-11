package cloud.xcan.sdf.core.angustester.application.cmd.services;

import cloud.xcan.sdf.api.pojo.auth.SimpleHttpAuth;
import cloud.xcan.sdf.core.angustester.domain.services.sync.ServicesSync;
import io.swagger.v3.oas.models.OpenAPI;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ServicesSyncCmd {

  void replace(Long serviceId, ServicesSync sync);

  void replaceAll(Long serviceId, List<ServicesSync> syncs);

  void sync(Long serviceId, String name);

  OpenAPI test(String syncUrl, List<SimpleHttpAuth> auths);

  void delete(Long serviceId, Set<String> names);

  void deleteAllByServices(Collection<Long> servicesIds);

}




