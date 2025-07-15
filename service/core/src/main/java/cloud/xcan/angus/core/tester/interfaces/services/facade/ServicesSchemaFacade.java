package cloud.xcan.angus.core.tester.interfaces.services.facade;

import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.schema.ApisSchemaOpenApiDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.vo.schema.ServiceSchemaDetailVo;
import cloud.xcan.angus.core.tester.interfaces.services.facade.vo.schema.ServiceServerVo;
import cloud.xcan.angus.spec.locale.SupportedLanguage;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ServicesSchemaFacade {

  void infoReplace(Long serviceId, Info dto);

  Info infoDetail(Long serviceId);

  void externalDocReplace(Long serviceId, ExternalDocumentation dto);

  ExternalDocumentation externalDocDetail(Long serviceId);

  void securityRequirementReplace(Long serviceId, SecurityRequirement dto);

  void securityRequirementReplaceAll(Long serviceId, List<SecurityRequirement> dto);

  void securityRequirementDelete(Long serviceId, Set<String> names);

  List<SecurityRequirement> securityRequirementList(Long serviceId);

  void serverReplace(Long serviceId, Server dto);

  void apisServerReplace(Long serviceId, Long serverId);

  void serverReplaceAll(Long serviceId, List<Server> dto);

  void serverDelete(Long serviceId, Set<Long> serverIds);

  List<Server> serverList(Long serviceId, Boolean joinServerInVariable);

  Server serverDetail(Long serviceId, Long serverId);

  List<ServiceServerVo> serverListByProject(Long projectId);

  void tagReplace(Long serviceId, Tag dto);

  void tagReplaceAll(Long serviceId, List<Tag> dto);

  void tagDelete(Long serviceId, Set<String> names);

  List<Tag> tagList(Long serviceId);

  void extensionsReplace(Long serviceId, Map<String, Object> dto);

  Map<String, Object> extensionsList(Long serviceId);

  ServiceSchemaDetailVo detail(Long serviceId);

  void openapiReplace(Long serviceId, Boolean forced, Boolean gzipCompression,
      String content);

  String openapiDetail(Long serviceId, ApisSchemaOpenApiDto dto);

  void translate(Long id, SupportedLanguage sourceLanguage, SupportedLanguage targetLanguage);

}
