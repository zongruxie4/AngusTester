package cloud.xcan.sdf.core.angustester.application.query.services;

import cloud.xcan.sdf.core.angustester.domain.apis.Apis;
import cloud.xcan.sdf.core.angustester.domain.services.schema.SchemaFormat;
import cloud.xcan.sdf.core.angustester.domain.services.schema.ServiceServer;
import cloud.xcan.sdf.core.angustester.domain.services.schema.ServicesSchema;
import cloud.xcan.sdf.extension.angustester.api.ApiImportSource;
import cloud.xcan.sdf.extension.angustester.api.ApisParseProvider;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public interface ServicesSchemaQuery {

  ServicesSchema detail(Long serviceId);

  String openapiDetail(Long serviceId, Set<Long> apisIds, SchemaFormat format,
      boolean gzipCompression, boolean onlyApisComponents);

  List<Server> findServersByServiceIds(Collection<Long> serviceId);

  OpenAPI openapiDetail0(Long serviceId, Set<Long> apisIds, boolean onlyApisComponents);

  List<ServiceServer> serverListByProject(Long projectId);

  Server serverDetail(Long serviceId, Long serverId);

  List<Server> findServersByServiceId(Long serviceId);

  ServicesSchema findByServiceId(Long serviceId);

  ServicesSchema checkAndFind(Long serviceId);

  //@Cacheable(key = "'servicesId_' + #serviceId", value = "servicesSchema")
  ServicesSchema checkAndFind0(Long serviceId);

  void checkSchemaExisted(Long serviceId);

  void checkValidServer(List<Server> servers);

  ApisParseProvider checkAndGetApisParseProvider(ApiImportSource importSource);

  @NotNull
  List<Apis> parseOpenApis(String content);

}
