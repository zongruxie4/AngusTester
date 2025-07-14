package cloud.xcan.angus.core.tester.application.cmd.services.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.SERVICE;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.angus.core.spring.SpringContextHolder.getCachedUidGenerator;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.application.converter.ServicesSchemaConverter.toClonedSchema;
import static cloud.xcan.angus.core.tester.application.converter.ServicesSchemaConverter.toInitProjectSchema;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SERVICE_DOC_CHANGE_REMINDER;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;
import static io.swagger.v3.oas.models.extension.ExtensionKey.ID_KEY;
import static io.swagger.v3.oas.models.extension.OpenAPIUtils.getExtensionLong;
import static java.util.Collections.emptyMap;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.apis.ApiSource;
import cloud.xcan.angus.api.commonlink.apis.StrategyWhenDuplicated;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.spring.env.EnvHelper;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.apis.ApisCmd;
import cloud.xcan.angus.core.tester.application.cmd.services.ServicesCompCmd;
import cloud.xcan.angus.core.tester.application.cmd.services.ServicesSchemaCmd;
import cloud.xcan.angus.core.tester.application.converter.ApisConverter;
import cloud.xcan.angus.core.tester.application.converter.ServicesSchemaConverter;
import cloud.xcan.angus.core.tester.application.query.apis.ApisQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesAuthQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesSchemaQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.apis.ApisRepo;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.core.tester.domain.services.schema.ServicesSchema;
import cloud.xcan.angus.core.tester.domain.services.schema.ServicesSchemaRepo;
import cloud.xcan.angus.core.tester.infra.util.OpenAPITranslator;
import cloud.xcan.angus.extension.angustester.api.ApiImportSource;
import cloud.xcan.angus.extension.angustester.deepseek.api.TranslationService;
import cloud.xcan.angus.extension.angustester.deepseek.api.TranslationServiceProvider;
import cloud.xcan.angus.l2cache.spring.RedisCaffeineCacheManager;
import cloud.xcan.angus.remote.message.SysException;
import cloud.xcan.angus.spec.annotations.DoInFuture;
import cloud.xcan.angus.spec.locale.SupportedLanguage;
import cloud.xcan.angus.spec.utils.GzipUtils;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.extension.OpenAPIUtils;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

@Biz
public class ServicesSchemaCmdImpl extends CommCmd<ServicesSchema, Long> implements
    ServicesSchemaCmd {

  @Resource
  private ServicesSchemaRepo servicesSchemaRepo;

  @Resource
  private ServicesSchemaQuery servicesSchemaQuery;

  @Resource
  private ServicesSchemaCmd servicesSchemaCmd;

  @Resource
  private ServicesCompCmd servicesCompCmd;

  @Resource
  private ServicesAuthQuery servicesAuthQuery;

  @Resource
  private ServicesQuery servicesQuery;

  @Resource
  private ApisCmd apisCmd;

  @Resource
  private ApisRepo apisRepo;

  @Resource
  private ApisQuery apisQuery;

  @Resource
  private ActivityCmd activityCmd;

  @DoInFuture("Add quota restrictions")
  @CacheEvict(key = "'servicesId_' + #serviceId", value = "servicesSchema")
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replace(Long serviceId, SecurityRequirement sr, Server server, Tag tag) {
    new BizTemplate<Void>() {
      ServicesSchema schemaDb;

      @Override
      protected void checkParams() {
        servicesAuthQuery.checkModifyAuth(getUserId(), serviceId);
        schemaDb = servicesSchemaQuery.checkAndFind0(serviceId);
        if (nonNull(server)) {
          servicesSchemaQuery.checkValidServer(List.of(server));
        }
      }

      @Override
      protected Void process() {
        Services projectDb = servicesQuery.checkAndFind(serviceId);

        // Add or update SecurityRequirement
        if (nonNull(sr)) {
          addOrUpdateSecurityRequirement(schemaDb, sr);
          activityCmd.add(toActivity(SERVICE, projectDb, ActivityType.SCHEMA_SR_UPDATED));
        }

        // Add or update server
        if (nonNull(server)) {
          addOrUpdateServer(schemaDb, server);
          activityCmd.add(toActivity(SERVICE, projectDb, ActivityType.SCHEMA_SERVER_UPDATED));
        }

        // Add or update server
        if (nonNull(tag)) {
          addAndUpdateTag(schemaDb, tag);
          activityCmd.add(toActivity(SERVICE, projectDb, ActivityType.SCHEMA_TAG_UPDATED));
        }

        servicesSchemaRepo.save(schemaDb);
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void apisServerReplace(Long serviceId, Long serverId) {
    new BizTemplate<Void>() {
      Services serviceDb;
      ServicesSchema schemaDb;

      @Override
      protected void checkParams() {
        serviceDb = servicesQuery.checkAndFind(serviceId);
        servicesAuthQuery.checkModifyAuth(getUserId(), serviceId);
        schemaDb = servicesSchemaQuery.checkAndFind0(serviceId);
      }

      @Override
      protected Void process() {
        Server server = isEmpty(schemaDb.getServers())
            ? null : schemaDb.getServers().stream()
            .filter(x -> nonNull(x) && x.getExtensions().containsKey(ID_KEY)
                && Objects.equals(serverId, Long.valueOf(x.getExtensions().get(ID_KEY).toString())))
            .findFirst().orElse(null);
        if (nonNull(server)) {
          apisRepo.updateCurrentServerById(Long.valueOf(
              server.getExtensions().get(ID_KEY).toString()), server);

          activityCmd.add(toActivity(SERVICE, serviceDb, ActivityType.APIS_SERVER_SYNC));
        }
        return null;
      }
    }.execute();
  }

  @DoInFuture("Add quota restrictions")
  @CacheEvict(key = "'servicesId_' + #serviceId", value = "servicesSchema")
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceAll(Long serviceId, Info info, ExternalDocumentation extDoc,
      List<SecurityRequirement> srs, List<Server> servers, List<Tag> tags,
      Map<String, Object> extensions) {
    new BizTemplate<Void>() {
      ServicesSchema schemaDb;

      @Override
      protected void checkParams() {
        servicesAuthQuery.checkModifyAuth(getUserId(), serviceId);
        schemaDb = servicesSchemaQuery.checkAndFind0(serviceId);
        if (nonNull(servers)) {
          servicesSchemaQuery.checkValidServer(servers);
        }
      }

      @Override
      protected Void process() {
        Services projectDb = servicesQuery.checkAndFind(serviceId);

        if (nonNull(info)) {
          schemaDb.setInfo(info);
          activityCmd.add(toActivity(SERVICE, projectDb, ActivityType.SCHEMA_INFO_UPDATED));
        }
        if (nonNull(extDoc)) {
          schemaDb.setExternalDocs(extDoc);
          activityCmd.add(toActivity(SERVICE, projectDb, ActivityType.SCHEMA_EXT_DOC_UPDATED));
        }
        if (nonNull(srs)) {
          schemaDb.setSecurity(srs);
          activityCmd.add(toActivity(SERVICE, projectDb, ActivityType.SCHEMA_SR_UPDATED));
        }
        if (nonNull(servers)) {
          schemaDb.setServers(servers);
          activityCmd.add(toActivity(SERVICE, projectDb, ActivityType.SCHEMA_SERVER_UPDATED));
        }
        if (nonNull(tags)) {
          schemaDb.setTags(tags);
          activityCmd.add(toActivity(SERVICE, projectDb, ActivityType.SCHEMA_TAG_UPDATED));
        }
        if (nonNull(extensions)) {
          schemaDb.setExtensions(extensions);
          activityCmd.add(toActivity(SERVICE, projectDb, ActivityType.SCHEMA_EXT_UPDATED));
        }

        servicesSchemaRepo.save(schemaDb);
        return null;
      }
    }.execute();
  }

  @CacheEvict(key = "'servicesId_' + #serviceId", value = "servicesSchema")
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long serviceId, Set<String> srNames, Set<Long> serverIds,
      Set<String> tagNames) {
    new BizTemplate<Void>() {
      ServicesSchema schemaDb;

      @Override
      protected void checkParams() {
        servicesAuthQuery.checkModifyAuth(getUserId(), serviceId);
        schemaDb = servicesSchemaQuery.checkAndFind0(serviceId);
      }

      @Override
      protected Void process() {
        Services serviceDb = servicesQuery.checkAndFind(serviceId);

        // Delete SecurityRequirement
        if (isNotEmpty(srNames)) {
          if (isEmpty(schemaDb.getSecurity())) {
            return null;
          }
          List<SecurityRequirement> srs = new ArrayList<>();
          for (SecurityRequirement sr : schemaDb.getSecurity()) {
            for (String name : sr.keySet()) {
              if (srNames.contains(name)) {
                sr.remove(name);
              }
            }
            if (!sr.isEmpty()) {
              srs.add(sr);
            }
          }
          schemaDb.setSecurity(srs);
          activityCmd.add(toActivity(SERVICE, serviceDb, ActivityType.SCHEMA_SR_UPDATED));
        }

        // Delete server
        if (isNotEmpty(serverIds)) {
          if (isEmpty(schemaDb.getServers())) {
            return null;
          }
          schemaDb.setServers(schemaDb.getServers().stream()
              .filter(x -> !serverIds.contains(getExtensionLong(x.getExtensions(), ID_KEY)))
              .collect(Collectors.toList()));
          activityCmd.add(toActivity(SERVICE, serviceDb, ActivityType.SCHEMA_SERVER_UPDATED));
        }

        // Delete server
        if (isNotEmpty(tagNames)) {
          if (isEmpty(schemaDb.getTags())) {
            return null;
          }
          schemaDb.setTags(schemaDb.getTags().stream()
              .filter(x -> !tagNames.contains(x.getName())).collect(Collectors.toList()));
          activityCmd.add(toActivity(SERVICE, serviceDb, ActivityType.SCHEMA_TAG_UPDATED));
        }
        servicesSchemaRepo.save(schemaDb);
        return null;
      }
    }.execute();
  }

  /**
   * Note: When an api is deleted, the front-end page should prompt the user to prevent accidental
   * deletion; Additionally, if the user has not modified or saved, do not submit the request to the
   * backend interface.
   */
  @Override
  public void openapiReplace(Long serviceId, Boolean forced, boolean gzipCompression,
      String content, StrategyWhenDuplicated strategyWhenDuplicated, boolean deleteWhenNotExisted,
      ApiSource apiSource, ApiImportSource importSource, boolean mergeSchema, String syncName) {
    new BizTemplate<Void>() {
      String decompressedContent;
      OpenAPI openApi;

      @Override
      protected void checkParams() {
        // Decompress OpenAPI documentation
        try {
          decompressedContent = gzipCompression ? GzipUtils.decompress(content) : content;
        } catch (IOException e) {
          assertTrue(false, "Incorrect gzip format, cause:" + e.getMessage());
        }
      }

      @Override
      protected Void process() {
        // Check the content is valid OpenAPI documents
        openApi = servicesSchemaQuery.checkAndGetApisParser(
                importSource.isWideOpenapi() ? ApiImportSource.OPENAPI : importSource)
            .parse(decompressedContent);

        // Update schema
        servicesSchemaCmd.openapiReplace(serviceId, true, openApi, StrategyWhenDuplicated.IGNORE,
            false,
            null, null, false, null);
        return null;
      }
    }.execute();
  }

  @CacheEvict(key = "'servicesId_' + #serviceId", value = "servicesSchema")
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void openapiReplace(Long serviceId, Boolean forced,
      OpenAPI openApi, StrategyWhenDuplicated strategyWhenDuplicated, boolean deleteWhenNotExisted,
      ApiSource apiSource, ApiImportSource importSource, boolean mergeSchema, String syncName) {
    new BizTemplate<Void>() {
      ServicesSchema serviceSchemaDb;
      Services serviceDb;

      @Override
      protected void checkParams() {
        // Check and find project schema
        // projectSchemaDb = projectSchemaQuery.checkAndFind(serviceId); -> Fix:: After the first successful import, the query project document is empty.
        serviceSchemaDb = servicesSchemaQuery.checkAndFind(serviceId);
        serviceDb = servicesQuery.checkAndFind(serviceId);
        // Check the modify project permission
        servicesAuthQuery.checkModifyAuth(getUserId(), serviceId);
      }

      @Override
      protected Void process() {
        // Update project schema
        // Warn:: Multiple files importing the same project will be overwritten by the last imported file
        servicesSchemaCmd.updateSchema(serviceId, serviceSchemaDb, openApi, mergeSchema,
            nonNull(strategyWhenDuplicated) && strategyWhenDuplicated.isCover());

        // Update Apis (Operation Object Schema)
        Map<String, Apis> apisDbMap = apisQuery.findByServiceId(serviceId).stream()
            .collect(Collectors.toMap(x -> x.getMethod().getValue().toLowerCase()
                + ":" + x.getEndpoint(), x -> x));
        // Warn: Since 3.1 is not required
        if (isNotEmpty(openApi.getPaths())) {
          Map<String, Operation> operationsMap = OpenAPIUtils.flatPaths(openApi.getPaths());
          Map<String, Apis> openApisMap = isEmpty(operationsMap) ? emptyMap()
              : operationsMap.keySet().stream().collect(Collectors
                  .toMap(x -> x, x -> ApisConverter.toSchemaApis(operationsMap.get(x))));
          // Find update apis
          if (StrategyWhenDuplicated.COVER.equals(strategyWhenDuplicated)) {
            Map<String, Apis> updatedApisDbMap = apisDbMap.keySet().stream()
                .filter(x -> openApisMap.containsKey(x) &&
                    /*apisDbMap.get(x).sameSchemaInfoAs(openApisMap.get(x))*/
                    apisDbMap.get(x).getSchemaHash() != openApisMap.get(x).getSchemaHash())
                .collect(Collectors.toMap(x -> x, apisDbMap::get));
            if (isNotEmpty(updatedApisDbMap)) {
              assertTrue(forced, SERVICE_DOC_CHANGE_REMINDER);
              apisCmd.updateSyncApis(updatedApisDbMap, openApisMap);
            }
          }

          if (deleteWhenNotExisted) {
            Collection<Apis> deletedApisInDb = apisDbMap.keySet().stream()
                .filter(x -> !openApisMap.containsKey(x)
                    // Note:: If is synchronization, only delete the synchronization of its own data.
                    && (isEmpty(syncName) || syncName.equals(apisDbMap.get(x).getSyncName())))
                .collect(Collectors.toMap(x -> x, apisDbMap::get)).values();
            if (isNotEmpty(deletedApisInDb)) {
              assertTrue(forced, SERVICE_DOC_CHANGE_REMINDER);
              // Note:: The following method not delete the components ref, deleted by apisCmd.delete0() when clear Trash
              apisCmd.delete(deletedApisInDb.stream().map(Apis::getId).collect(Collectors.toSet()),
                  false);
            }
          }

          // Find new apis
          Collection<Apis> newApis = openApisMap.keySet().stream()
              .filter(x -> !apisDbMap.containsKey(x))
              .collect(Collectors.toMap(x -> x, openApisMap::get)).values();
          if (isNotEmpty(newApis)) {
            apisCmd.add(newApis.stream().map(
                    x -> ApisConverter.assembleSchemaToAddApis(x, serviceDb, openApi.getComponents(),
                        apiSource, importSource, syncName))
                .collect(Collectors.toList()), serviceDb, false);
          }
        } else {
          if (deleteWhenNotExisted) {
            // Note:: The following method not delete the components ref, deleted by apisCmd.delete0() when clear Trash
            apisCmd.delete(apisDbMap.values().stream().map(Apis::getId).collect(Collectors.toSet()),
                false);
          }
        }

        // Update ServicesComp (Components Object Schema)
        if (isNotEmpty(openApi.getComponents())) {
          servicesCompCmd.replaceByOpenApi(serviceDb.getId(), openApi.getComponents(),
              strategyWhenDuplicated, deleteWhenNotExisted);
        }

        // Add editor activity
        if (isNull(apiSource) || ApiSource.EDITOR.equals(apiSource)) {
          activityCmd.add(toActivity(SERVICE, serviceDb, ActivityType.SCHEMA_OPENAPI_UPDATED));
        } else if (ApiSource.SYNC.equals(apiSource)) {
          activityCmd.add(toActivity(SERVICE, serviceDb, ActivityType.SCHEMA_OPENAPI_SYNC));
        } else if (ApiSource.IMPORT.equals(apiSource)) {
          activityCmd.add(toActivity(SERVICE, serviceDb, ActivityType.IMPORT));
        }
        return null;
      }
    }.execute();
  }

  @Override
  public void translate(Long serviceId, SupportedLanguage sourceLanguage,
      SupportedLanguage targetLanguage) {
    new BizTemplate<Void>() {
      @Override
      protected Void process() {
        System.setProperty("deepseek.api.key", EnvHelper.getString("DEEPSEEK_TRANSLATION_API_KEY"));

        TranslationService translationService = servicesSchemaQuery.checkAndGetTranslationService(
            TranslationServiceProvider.DeepSeek);
        try {
          translationService.loadConfig();
        } catch (Exception e) {
          throw new SysException(e.getMessage());
        }

        OpenAPI openApi = servicesSchemaQuery.openapiDetail0(serviceId, null, false);
        OpenAPITranslator translator = new OpenAPITranslator(
            translationService, sourceLanguage, targetLanguage
        );
        translator.translateOpenAPI(openApi);

        servicesSchemaCmd.openapiReplace(serviceId, true, openApi, StrategyWhenDuplicated.IGNORE,
            false, null, null, false, null);
        return null;
      }
    }.execute();
  }

  @Override
  public void init(Services services) {
    insert0(toInitProjectSchema(services, null));
  }

  @Override
  public void init(Services services, OpenAPI openAPI) {
    insert0(toInitProjectSchema(services, openAPI));
  }

  @Override
  public void clone(Long clonedServiceId, Long serviceId) {
    ServicesSchema schemaDb = servicesSchemaQuery.findByServiceId(clonedServiceId);
    if (nonNull(schemaDb)) {
      insert0(toClonedSchema(schemaDb, serviceId));
    }
  }

  @Override
  public void deleteByServiceIdIn(Collection<Long> serviceIds) {
    servicesSchemaRepo.deleteByServiceIdIn(serviceIds);
    ((RedisCaffeineCacheManager) cacheManager).evict("servicesSchema",
        serviceIds.stream().map(id -> "serviceId_" + id)
            .collect(Collectors.toList()));
  }

  @Override
  public void updateSchema(Long serviceId, ServicesSchema serviceSchemaDb, OpenAPI openApi,
      boolean mergeSchema, boolean cover) {
    ServicesSchemaConverter.updateSchema(serviceSchemaDb, openApi, mergeSchema, cover);
    servicesSchemaRepo.save(serviceSchemaDb);
    activityCmd.add(toActivity(SERVICE, serviceSchemaDb, ActivityType.SCHEMA_UPDATED));
  }

  private void addOrUpdateSecurityRequirement(ServicesSchema schemaDb, SecurityRequirement sr) {
    List<SecurityRequirement> securityDb = schemaDb.getSecurity();
    if (isNotEmpty(securityDb)) {
      boolean update = false;
      // Only use one-dimensional element
      for (String name : securityDb.get(0).keySet()) {
        if (sr.containsKey(name)) {
          securityDb.get(0).put(name, sr.get(name));
          update = true;
          break;
        }
      }
      if (!update) {
        securityDb.get(0).putAll(sr);
      }
    } else {
      schemaDb.setSecurity(List.of(sr));
    }
  }

  private void addOrUpdateServer(ServicesSchema schemaDb, Server server) {
    boolean update = false;
    Long serverId = getExtensionLong(server.getExtensions(), ID_KEY);
    List<Server> serversDb = schemaDb.getServers();
    if (isNotEmpty(serversDb)) {
      for (Server serverDb : serversDb) {
        Long serverDbId = getExtensionLong(serverDb.getExtensions(), ID_KEY);
        if (nonNull(serverId) && nonNull(serverDbId) && serverId.equals(serverDbId)) {
          serverDb.description(server.getDescription())
              .url(server.getUrl())
              .variables(server.getVariables())
              .extensions(server.getExtensions());
          update = true;
          break;
        }
      }
      if (!update) {
        if (isNull(serverId)) {
          server.addExtension(ID_KEY, getCachedUidGenerator().getUID());
        }
        schemaDb.getServers().add(server);
      }
    } else {
      if (isNull(serverId)) {
        server.addExtension(ID_KEY, getCachedUidGenerator().getUID());
      }
      schemaDb.setServers(List.of(server));
    }
  }

  private void addAndUpdateTag(ServicesSchema schemaDb, Tag tag) {
    boolean update = false;
    List<Tag> tagsDb = schemaDb.getTags();
    if (isNotEmpty(tagsDb)) {
      for (Tag tagDb : tagsDb) {
        if (tagDb.getName().equals(tag.getName())) {
          tagDb.description(tag.getDescription())
              .externalDocs(tag.getExternalDocs())
              .extensions(tag.getExtensions());
          update = true;
          break;
        }
      }
      if (!update) {
        schemaDb.getTags().add(tag);
      }
    } else {
      schemaDb.setTags(List.of(tag));
    }
  }

  @Override
  protected BaseRepository<ServicesSchema, Long> getRepository() {
    return this.servicesSchemaRepo;
  }

}
