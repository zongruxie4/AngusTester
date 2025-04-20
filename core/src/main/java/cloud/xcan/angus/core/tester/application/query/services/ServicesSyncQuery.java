package cloud.xcan.angus.core.tester.application.query.services;

import cloud.xcan.angus.api.pojo.auth.SimpleHttpAuth;
import cloud.xcan.angus.core.tester.domain.services.sync.ServicesSync;
import java.util.Collection;
import java.util.List;

public interface ServicesSyncQuery {

  List<ServicesSync> find(Long serviceId);

  ServicesSync find(Long serviceId, String name);

  List<ServicesSync> find(Long serviceId, Collection<String> names);

  void checkSyncAddNumQuota(Long serviceId, int incr);

  void checkSyncNumQuota(Long serviceId, int num);

  void checkRepeatedNameInParams(List<String> names);

  String checkAndGetOpenApiContent(String syncUrl, List<SimpleHttpAuth> auths);
}




