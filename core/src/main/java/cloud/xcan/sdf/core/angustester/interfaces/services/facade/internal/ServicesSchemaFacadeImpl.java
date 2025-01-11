package cloud.xcan.sdf.core.angustester.interfaces.services.facade.internal;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNull;

import cloud.xcan.sdf.api.commonlink.apis.ApiSource;
import cloud.xcan.sdf.api.commonlink.apis.StrategyWhenDuplicated;
import cloud.xcan.sdf.core.angustester.application.cmd.services.ServicesSchemaCmd;
import cloud.xcan.sdf.core.angustester.application.query.data.VariableQuery;
import cloud.xcan.sdf.core.angustester.application.query.services.ServicesSchemaQuery;
import cloud.xcan.sdf.core.angustester.domain.services.schema.ServiceServer;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.ServicesSchemaFacade;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.schema.ApisSchemaOpenApiDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.internal.assembler.ServicesSchemaAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.schema.ServiceSchemaDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.schema.ServiceServerVo;
import cloud.xcan.sdf.core.biz.NameJoin;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class ServicesSchemaFacadeImpl implements ServicesSchemaFacade {

  @Resource
  private ServicesSchemaCmd servicesSchemaCmd;

  @Resource
  private ServicesSchemaQuery servicesSchemaQuery;

  @Resource
  private VariableQuery variableQuery;

  @Override
  public void infoReplace(Long serviceId, Info info) {
    servicesSchemaCmd.replaceAll(serviceId, info, null, null, null, null, null);
  }

  @Override
  public Info infoDetail(Long serviceId) {
    return servicesSchemaQuery.detail(serviceId).getInfo();
  }

  @Override
  public void externalDocReplace(Long serviceId, ExternalDocumentation externalDoc) {
    servicesSchemaCmd.replaceAll(serviceId, null, externalDoc, null, null, null, null);
  }

  @Override
  public ExternalDocumentation externalDocDetail(Long serviceId) {
    return servicesSchemaQuery.detail(serviceId).getExternalDocs();
  }

  @Override
  public void securityRequirementReplace(Long serviceId, SecurityRequirement sr) {
    servicesSchemaCmd.replace(serviceId, sr, null, null);
  }

  @Override
  public void securityRequirementReplaceAll(Long serviceId,
      List<SecurityRequirement> srs) {
    servicesSchemaCmd.replaceAll(serviceId, null, null, srs, null, null, null);
  }

  @Override
  public void securityRequirementDelete(Long serviceId, Set<String> names) {
    servicesSchemaCmd.delete(serviceId, names, null, null);
  }

  @Override
  public List<SecurityRequirement> securityRequirementList(Long serviceId) {
    return servicesSchemaQuery.detail(serviceId).getSecurity();
  }

  @Override
  public void serverReplace(Long serviceId, Server server) {
    servicesSchemaCmd.replace(serviceId, null, server, null);
  }

  @Override
  public void apisServerReplace(Long serviceId, Long serverId) {
    servicesSchemaCmd.apisServerReplace(serviceId, serverId);
  }

  @Override
  public void serverReplaceAll(Long serviceId, List<Server> servers) {
    servicesSchemaCmd.replaceAll(serviceId, null, null, null, servers, null, null);
  }

  @Override
  public void serverDelete(Long serviceId, Set<Long> serverIds) {
    servicesSchemaCmd.delete(serviceId, null, serverIds, null);
  }

  @Override
  public List<Server> serverList(Long serviceId, Boolean joinServerInVariable) {
    List<Server> servers = servicesSchemaQuery.detail(serviceId).getServers();
    if (isNull(joinServerInVariable) || !joinServerInVariable) {
      return servers;
    }
    List<Server> allServers = new ArrayList<>(servers);
    List<Server> joinedServers = variableQuery.findServerByServiceId(serviceId);
    allServers.addAll(joinedServers);
    return allServers;
  }

  @Override
  public Server serverDetail(Long serviceId, Long serverId) {
    return servicesSchemaQuery.serverDetail(serviceId, serverId);
  }

  @NameJoin
  @Override
  public List<ServiceServerVo> serverListByProject(Long projectId) {
    List<ServiceServer> servers = servicesSchemaQuery.serverListByProject(projectId);
    return isNotEmpty(servers) ? servers.stream().map(ServicesSchemaAssembler::toServiceServerVo)
        .collect(Collectors.toList()) : null;
  }

  @Override
  public void tagReplace(Long serviceId, Tag tag) {
    servicesSchemaCmd.replace(serviceId, null, null, tag);
  }

  @Override
  public void tagReplaceAll(Long serviceId, List<Tag> tags) {
    servicesSchemaCmd.replaceAll(serviceId, null, null, null, null, tags, null);
  }

  @Override
  public void tagDelete(Long serviceId, Set<String> names) {
    servicesSchemaCmd.delete(serviceId, null, null, names);
  }

  @Override
  public List<Tag> tagList(Long serviceId) {
    return servicesSchemaQuery.detail(serviceId).getTags();
  }

  @Override
  public void extensionsReplace(Long serviceId, Map<String, Object> extensions) {
    servicesSchemaCmd.replaceAll(serviceId, null, null, null, null, null, extensions);
  }

  @Override
  public Map<String, Object> extensionsList(Long serviceId) {
    return servicesSchemaQuery.detail(serviceId).getExtensions();
  }

  @Override
  public ServiceSchemaDetailVo detail(Long serviceId) {
    return ServicesSchemaAssembler.toDetailVo(servicesSchemaQuery.detail(serviceId));
  }

  @Override
  public void openapiReplace(Long serviceId, Boolean forced,
      Boolean gzipCompression, String content) {
    servicesSchemaCmd.openapiReplace(serviceId, forced,
        gzipCompression, content, StrategyWhenDuplicated.COVER, true,
        ApiSource.EDITOR, null, false, null);
  }

  @Override
  public String openapiDetail(Long serviceId, ApisSchemaOpenApiDto dto) {
    return servicesSchemaQuery.openapiDetail(serviceId, dto.getApiIds(),
        dto.getFormat(), dto.isGzipCompression(), dto.isOnlyApisComponents());
  }
}
