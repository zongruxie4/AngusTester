package cloud.xcan.angus.core.tester.application.cmd.services;

import cloud.xcan.angus.api.commonlink.apis.ApiSource;
import cloud.xcan.angus.api.commonlink.apis.StrategyWhenDuplicated;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.core.tester.domain.services.schema.ServicesSchema;
import cloud.xcan.angus.extension.angustester.api.ApiImportSource;
import cloud.xcan.angus.spec.locale.SupportedLanguage;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ServicesSchemaCmd {

  void replace(Long serviceId, SecurityRequirement sr, Server server, Tag tag);

  void apisServerReplace(Long serviceId, Long serverId);

  void replaceAll(Long serviceId, Info info, ExternalDocumentation extDoc,
      List<SecurityRequirement> srs, List<Server> servers, List<Tag> tags,
      Map<String, Object> extensions
  );

  void delete(Long serviceId, Set<String> srNames, Set<Long> serverIds,
      Set<String> tagNames);

  void openapiReplace(Long serviceId, Boolean forced, boolean gzipCompression,
      String content, StrategyWhenDuplicated strategyWhenDuplicated, boolean deleteWhenNotExisted,
      ApiSource apiSource, ApiImportSource importSource, boolean mergeSchema, String syncName);

  void openapiReplace(Long serviceId, Boolean forced,
      OpenAPI openApi, StrategyWhenDuplicated strategyWhenDuplicated, boolean deleteWhenNotExisted,
      ApiSource apiSource, ApiImportSource importSource, boolean mergeSchema, String syncName);

  void translate(Long id, SupportedLanguage sourceLanguage, SupportedLanguage targetLanguage);

  void init(Services services);

  void init(Services services, OpenAPI openAPI);

  void clone(Long clonedServiceId, Long serviceId);

  void deleteByServiceIdIn(Collection<Long> serviceIds);

  void updateSchema(Long serviceId, ServicesSchema serviceSchemaDb, OpenAPI openApi,
      boolean mergeSchema, boolean cover);

}
