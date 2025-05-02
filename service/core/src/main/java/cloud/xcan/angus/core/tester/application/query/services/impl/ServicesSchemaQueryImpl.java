package cloud.xcan.angus.core.tester.application.query.services.impl;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.angus.core.tester.application.converter.ApisConverter.toSchemaApis;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SERVICE_NO_IMPORT_APIS_PLUGIN_CODE;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SERVICE_NO_IMPORT_APIS_PLUGIN_T;
import static cloud.xcan.angus.extension.angustester.api.utils.OpenApiParser.checkAndParseOpenApi;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.UrlEnvVariableChecker.containsEnvVariable;
import static io.swagger.v3.oas.models.extension.ExtensionKey.ID_KEY;
import static io.swagger.v3.oas.models.extension.OpenAPIUtils.getExtensionLong;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.spring.SpringContextHolder;
import cloud.xcan.angus.core.tester.application.converter.ApisConverter;
import cloud.xcan.angus.core.tester.application.converter.ServicesCompConverter;
import cloud.xcan.angus.core.tester.application.query.apis.ApisQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesAuthQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesCompQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesSchemaQuery;
import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.services.comp.ServicesComp;
import cloud.xcan.angus.core.tester.domain.services.schema.SchemaFormat;
import cloud.xcan.angus.core.tester.domain.services.schema.ServiceServer;
import cloud.xcan.angus.core.tester.domain.services.schema.ServicesSchema;
import cloud.xcan.angus.core.tester.domain.services.schema.ServicesSchemaRepo;
import cloud.xcan.angus.extension.angustester.api.ApiImportSource;
import cloud.xcan.angus.extension.angustester.api.ApisParseProvider;
import cloud.xcan.angus.remote.message.SysException;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.spec.utils.GzipUtils;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import io.swagger.v3.core.util.Json31;
import io.swagger.v3.core.util.Yaml31;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.extension.ExtensionKey;
import io.swagger.v3.oas.models.extension.OpenAPIUtils;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.parser.core.models.ParseOptions;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;

@Biz
public class ServicesSchemaQueryImpl implements ServicesSchemaQuery {

  private static final Logger log = LoggerFactory.getLogger(ServicesSchemaQueryImpl.class);
  @Resource
  private ServicesSchemaRepo servicesSchemaRepo;

  @Resource
  private ServicesCompQuery servicesCompQuery;

  @Resource
  private ApisQuery apisQuery;

  @Resource
  private ServicesAuthQuery servicesAuthQuery;

  @Resource
  private ServicesSchemaQuery servicesSchemaQuery;

  @Cacheable(key = "'servicesId_' + #serviceId", value = "servicesSchema")
  @Override
  public ServicesSchema detail(Long serviceId) {
    return new BizTemplate<ServicesSchema>() {
      @Override
      protected void checkParams() {
        servicesAuthQuery.checkViewAuth(getUserId(), serviceId);
      }

      @Override
      protected ServicesSchema process() {
        return servicesSchemaRepo.findByServiceId(serviceId)
            .orElseThrow(() -> ResourceNotFound.of(serviceId, "ServicesSchema"));
      }
    }.execute();
  }

  @Override
  public String openapiDetail(Long serviceId, Set<Long> apisIds, SchemaFormat format,
      boolean gzipCompression, boolean onlyApisComponents) {
    return new BizTemplate<String>() {

      @Override
      protected void checkParams() {
        servicesAuthQuery.checkViewAuth(getUserId(), serviceId);
      }

      @Override
      protected String process() {
        OpenAPI openApi = openapiDetail0(serviceId, apisIds, onlyApisComponents);

        // Format output
        String result = SchemaFormat.json.equals(format) ? Json31.pretty(openApi)
            : Yaml31.pretty(openApi);

        if (gzipCompression) {
          try {
            result = GzipUtils.compress(result);
          } catch (IOException e) {
            throw SysException.of("Gzip OpenAPI exception, cause: " + e.getMessage());
          }
        }
        return result;
      }
    }.execute();
  }

  @Override
  public OpenAPI openapiDetail0(Long serviceId, Set<Long> apisIds, boolean onlyApisComponents) {
    return new BizTemplate<OpenAPI>() {

      @Override
      protected void checkParams() {
        servicesAuthQuery.checkViewAuth(getUserId(), serviceId);
      }

      @Override
      protected OpenAPI process() {
        OpenAPI openApi = new OpenAPI();

        // Merge apis and project component servers
        ServicesSchema schemaDb = servicesSchemaQuery.checkAndFind(serviceId);
        List<Apis> apis = apisQuery.findByServiceAndIds(serviceId, apisIds);

        Apis singleApi = nonNull(apis) && apis.size() == 1 ? apis.get(0) : null;
        List<Server> servers = nonNull(singleApi)
            ? List.of(singleApi.getCurrentServer()) : mergeServers(apis, schemaDb.getServers());
        List<SecurityRequirement> securityRequirements = nonNull(singleApi)
            ? singleApi.getSecurity() : schemaDb.getSecurity();
        Info info = schemaDb.getInfo();
        if (nonNull(singleApi)) {
          info.setTitle(singleApi.getSummary());
          info.setDescription(singleApi.getDescription());
        }

        // Assemble OpenAPI common schema
        openApi.openapi(schemaDb.getOpenapi())
            .info(schemaDb.getInfo())
            .externalDocs(schemaDb.getExternalDocs())
            .servers(servers)
            .security(securityRequirements)
            // .tags(schemaDb.getTags())
            .extensions(schemaDb.getExtensions())
            .specVersion(schemaDb.getSpecVersion())
            .addExtension(ExtensionKey.PROJECT_ID_KEY, serviceId);

        // Assemble OpenAPI Paths schema
        if (isNotEmpty(apis)) {
          Paths paths = ApisConverter.toPaths(apis.stream()
              .collect(Collectors.groupingBy(Apis::getEndpoint)));
          openApi.paths(paths);
        }

        // Assemble OpenAPI tags
        if (isNotEmpty(apisIds) && isNotEmpty(schemaDb.getTags())) {
          // Only find the reference tags used by the API
          Set<String> apisTags = apis.stream().map(Apis::getTags)
              .filter(ObjectUtils::isNotEmpty).flatMap(Collection::stream)
              .collect(Collectors.toSet());
          openApi.setTags(schemaDb.getTags().stream()
              .filter(x -> apisTags.contains(x.getName())).collect(Collectors.toList()));
        } else {
          openApi.setTags(schemaDb.getTags());
        }

        // Assemble OpenAPI components schema
        List<ServicesComp> compsDb = nonNull(singleApi) || onlyApisComponents
            ? findProjectApisComps(serviceId, apis)
            : servicesCompQuery.findByServiceId(serviceId);
        if (isNotEmpty(compsDb)) {
          Components components = ServicesCompConverter.toOpenApiComp(
              compsDb.stream().collect(Collectors.groupingBy(ServicesComp::getType)));
          openApi.components(components);
        }

        return openApi;
      }
    }.execute();
  }

  @Override
  public List<ServiceServer> serverListByProject(Long projectId) {
    return new BizTemplate<List<ServiceServer>>() {

      @Override
      protected List<ServiceServer> process() {
        List<Long> serviceIds = servicesSchemaRepo.findServiceIdByProjectId(projectId);
        if (isEmpty(serviceIds)) {
          return emptyList();
        }
        List<ServiceServer> servers = new ArrayList<>();
        for (Long serviceId : serviceIds) {
          List<Server> servers0 = findServersByServiceId(serviceId);
          for (Server server : servers0) {
            servers.add(new ServiceServer().setServiceId(serviceId).setServer(server));
          }
        }
        return servers;
      }
    }.execute();
  }

  @Override
  public Server serverDetail(Long serviceId, Long serverId) {
    return new BizTemplate<Server>() {

      @Override
      protected Server process() {
        ServicesSchema schema = servicesSchemaQuery.findByServiceId(serviceId);
        Server server = null;
        if (Objects.nonNull(schema) && isNotEmpty(schema.getServers())) {
          server = schema.getServers().stream()
              .filter(x -> Objects.equals(serverId, getExtensionLong(x.getExtensions(), ID_KEY)))
              .findFirst().orElse(null);
        }
        if (nonNull(server)) {
          return server;
        }
        throw ResourceNotFound.of(serverId, "Server");
      }
    }.execute();
  }

  private List<Server> mergeServers(List<Apis> apis, List<Server> servers) {
    Map<String, Server> serversMap = new HashMap<>();
    if (isNotEmpty(servers)) {
      for (Server server : servers) {
        if (isNotEmpty(server.getUrl())) {
          serversMap.put(server.getUrl(), server);
        }
      }
    }
    if (isNotEmpty(apis)) {
      for (Apis api : apis) {
        if (nonNull(api.getCurrentServer())
            && isNotEmpty(api.getCurrentServer().getUrl())) {
          serversMap.put(api.getCurrentServer().getUrl(), api.getCurrentServer());
        }
      }
    }
    return new ArrayList<>(serversMap.values());
  }

  @Override
  public List<Server> findServersByServiceIds(Collection<Long> serviceIds) {
    List<Server> servers = new ArrayList<>();
    for (Long serviceId : serviceIds) {
      ServicesSchema schema = servicesSchemaQuery.findByServiceId(serviceId);
      if (Objects.nonNull(schema)) {
        servers.addAll(schema.getServers());
      }
    }
    return servers;
  }

  @Override
  public List<Server> findServersByServiceId(Long serviceId) {
    List<Server> servers = new ArrayList<>();
    ServicesSchema schema = servicesSchemaQuery.findByServiceId(serviceId);
    if (Objects.nonNull(schema) && isNotEmpty(schema.getServers())) {
      servers.addAll(schema.getServers());
    }
    return servers;
  }

  @Cacheable(key = "'servicesId_' + #serviceId", value = "servicesSchema")
  @Override
  public ServicesSchema findByServiceId(Long serviceId) {
    return servicesSchemaRepo.findByServiceId(serviceId).orElse(null);
  }

  @Cacheable(key = "'servicesId_' + #serviceId", value = "servicesSchema")
  @Override
  public ServicesSchema checkAndFind(Long serviceId) {
    return servicesSchemaRepo.findByServiceId(serviceId)
        .orElseThrow(() -> ResourceNotFound.of(serviceId, "ServicesSchema"));
  }

  //@Cacheable(key = "'servicesId_' + #serviceId", value = "servicesSchema")
  @Override
  public ServicesSchema checkAndFind0(Long serviceId) {
    return servicesSchemaRepo.findByServiceId(serviceId)
        .orElseThrow(() -> ResourceNotFound.of(serviceId, "ServicesSchema"));
  }

  @Override
  public void checkSchemaExisted(Long serviceId) {
    assertResourceNotFound(servicesSchemaRepo.existsByServiceId(serviceId),
        serviceId, "OpenAPI Schema");
  }

  @Override
  public void checkValidServer(List<Server> servers) {
    if (isNotEmpty(servers)) {
      for (Server server : servers) {
        assertTrue(isNotEmpty(server.getUrl()), "Server url should not be empty");
        assertTrue(!containsEnvVariable(server.getUrl())
            || isNotEmpty(server.getVariables()), "Server variables is missing");
      }
    }
  }

  @Override
  public ApisParseProvider checkAndGetApisParseProvider(ApiImportSource importSource) {
    ApisParseProvider enabledApisParseProvider = null;
    Map<String, ApisParseProvider> providerMap = null;
    try {
      //Fix:: Dependency injection On linux
      providerMap = SpringContextHolder.getCtx().getBeansOfType(ApisParseProvider.class);
    } catch (Exception e) {
      // NOOP
    }
    if (nonNull(providerMap) && isNotEmpty(providerMap.values())) {
      for (ApisParseProvider provider : providerMap.values()) {
        if (nonNull(provider.getSource()) && provider.getSource().equals(importSource)) {
          enabledApisParseProvider = provider;
          break;
        }
      }
    }
    if (isNull(enabledApisParseProvider)) {
      // No apis parse plugin available
      throw SysException.of(SERVICE_NO_IMPORT_APIS_PLUGIN_CODE,
          SERVICE_NO_IMPORT_APIS_PLUGIN_T, new Object[]{importSource});
    }
    return enabledApisParseProvider;
  }

  @Override
  public @NotNull List<Apis> parseOpenApis(String content) {
    // Import OpenAPI format data, Note: Include Postman format data.
    ParseOptions parseOptions = new ParseOptions();
    parseOptions.setResolve(true);
    parseOptions.setInferSchemaType(false);
    parseOptions.setResolveFully(true);
    OpenAPI openAPI = checkAndParseOpenApi(content, null, parseOptions);
    Map<String, Operation> operationsMap = OpenAPIUtils.flatPaths(openAPI.getPaths());
    return isEmpty(operationsMap) ? new ArrayList<>()
        : operationsMap.keySet().stream().map(x -> toSchemaApis(operationsMap.get(x)))
            .collect(Collectors.toList());
  }

  private List<ServicesComp> findProjectApisComps(Long serviceId, List<Apis> apis) {
    List<ServicesComp> allComps = servicesCompQuery.findByServiceId(serviceId);
    List<ServicesComp> finalComps = new ArrayList<>();
    if (isNotEmpty(apis)) {
      // Only find the security schemes or reference components used by the API
      Set<String> apisRefComps = new HashSet<>();
      for (Apis api : apis) {
        Map<String, String> refs = apisQuery.findApisAllRef(api);
        if (!refs.isEmpty()) {
          apisRefComps.addAll(refs.keySet());
        }
      }
      if (!apisRefComps.isEmpty()) {
        // Use map to remove duplicates
        Map<String, ServicesComp> finalRefComps = new HashMap<>();
        for (ServicesComp comp : allComps) {
          if (apisRefComps.contains(comp.getRef()) && !finalRefComps.containsKey(comp.getRef())) {
            finalRefComps.put(comp.getRef(), comp);
          }
        }
        finalComps.addAll(finalRefComps.values());
      }

      Map<String, ServicesComp> securityComps = allComps.stream()
          .filter(x -> x.getType().isSecuritySchemes())
          .collect(Collectors.toMap(ServicesComp::getKey, x -> x));
      // Use map to remove duplicates
      Map<String, ServicesComp> finalSecurityComps = new HashMap<>();
      boolean hasSecurityRequirement = false;
      for (Apis api : apis) {
        if (isNotEmpty(api.getSecurity())) {
          hasSecurityRequirement = true;
          for (SecurityRequirement sr : api.getSecurity()) {
            for (Entry<String, List<String>> entry : sr.entrySet()) {
              if (securityComps.containsKey(entry.getKey())) {
                finalSecurityComps.put(entry.getKey(), securityComps.get(entry.getKey()));
              }
            }
          }
        }
      }

      if (hasSecurityRequirement && finalSecurityComps.isEmpty()) {
        log.error("The security plan and security requirement names are inconsistent or missing.");
        finalComps.addAll(securityComps.values());
      } else {
        finalComps.addAll(finalSecurityComps.values());
      }

      if (finalComps.isEmpty()) {
        return null;
      }
    }
    return finalComps;
  }
}
