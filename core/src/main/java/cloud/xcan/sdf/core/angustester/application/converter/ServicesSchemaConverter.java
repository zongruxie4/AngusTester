package cloud.xcan.sdf.core.angustester.application.converter;

import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserFullname;
import static cloud.xcan.sdf.core.spring.SpringContextHolder.getCachedUidGenerator;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNull;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.stringSafe;
import static io.swagger.v3.oas.models.extension.ExtensionKey.ID_KEY;

import cloud.xcan.sdf.core.angustester.domain.services.Services;
import cloud.xcan.sdf.core.angustester.domain.services.schema.ServicesSchema;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class ServicesSchemaConverter {

  public static ServicesSchema toInitProjectSchema(Services services, OpenAPI openapi) {
    if (isNull(openapi)) {
      openapi = new OpenAPI();
      Info info = toInitInfo(services);
      openapi.setInfo(info);
    }
    ServicesSchema schema = new ServicesSchema();
    schema.setProjectId(services.getProjectId());
    schema.setServiceId(services.getId());
    schema.setOpenapi(openapi.getOpenapi());
    schema.setInfo(openapi.getInfo());
    schema.setSpecVersion(openapi.getSpecVersion());
    return schema;
  }

  @NotNull
  public static Info toInitInfo(Services services) {
    Info info = new Info();
    info.setTitle(stringSafe(services.getName(), "undefined"));
    info.setVersion("1.0");
    Contact contact = new Contact();
    contact.setName(getUserFullname());
    info.setContact(contact);
    return info;
  }

  public static ServicesSchema toClonedSchema(ServicesSchema schemaDb, Long serviceId) {
    return new ServicesSchema()
        .setServiceId(serviceId)
        .setProjectId(schemaDb.getProjectId())
        .setOpenapi(schemaDb.getOpenapi())
        .setInfo(schemaDb.getInfo())
        .setExternalDocs(schemaDb.getExternalDocs())
        .setServers(schemaDb.getServers())
        .setSecurity(schemaDb.getSecurity())
        .setTags(schemaDb.getTags())
        .setExtensions(schemaDb.getExtensions())
        .setSpecVersion(schemaDb.getSpecVersion());
  }

  public static void updateSchema(ServicesSchema schemaDb, OpenAPI openApi, boolean mergeSchema,
      boolean cover) {
    schemaDb.setOpenapi(openApi.getOpenapi())
        .setInfo(openApi.getInfo())
        .setExternalDocs(openApi.getExternalDocs())
        .setSpecVersion(openApi.getSpecVersion());
    if (mergeSchema) {
      // Append
      if (isNotEmpty(openApi.getServers())) {
        Map<String, Server> serversDbMap = schemaDb.getServers().stream()
            .collect(Collectors.toMap(Server::getUrl, server -> server, (u1, u2) -> u2));
        List<Server> openapiServers = openApi.getServers();
        if (isNotEmpty(schemaDb.getServers())) {
          // Find updated if needs cover
          if (cover) {
            List<Server> updatedServers = openapiServers.stream()
                .filter(x -> serversDbMap.containsKey(x.getUrl()))
                .collect(Collectors.toList());
            if (isNotEmpty(updatedServers)) {
              for (Server updatedServer : updatedServers) {
                Server serverDb = serversDbMap.get(updatedServer.getUrl());
                serverDb.setDescription(updatedServer.getDescription());
                serverDb.setVariables(updatedServer.getVariables());
              }
            }
          }

          List<Server> addServers = openapiServers.stream()
              .filter(x -> !serversDbMap.containsKey(x.getUrl()))
              .collect(Collectors.toList());
          if (isNotEmpty(addServers)) {
            for (Server addServer : addServers) {
              addServer.addExtension(ID_KEY, getCachedUidGenerator().getUID());
              schemaDb.getServers().add(addServer);
            }
          }
        } else {
          for (Server server : openApi.getServers()) {
            server.addExtension(ID_KEY, getCachedUidGenerator().getUID());
          }
          schemaDb.setServers(openApi.getServers());
        }
      }
      if (isNotEmpty(openApi.getSecurity())) {
        if (isNotEmpty(schemaDb.getSecurity())) {
          List<SecurityRequirement> removeDuplicate = new ArrayList<>(openApi.getSecurity());
          Set<String> existedKeys = new HashSet<>();
          for (SecurityRequirement securityRequirement : openApi.getSecurity()) {
            existedKeys.addAll(securityRequirement.keySet());
          }
          for (SecurityRequirement securityRequirement : schemaDb.getSecurity()) {
            for (String key : securityRequirement.keySet()) {
              if (!existedKeys.contains(key)) {
                removeDuplicate.add(securityRequirement);
              }
            }
          }
          schemaDb.setSecurity(removeDuplicate);
        } else {
          schemaDb.setSecurity(openApi.getSecurity());
        }
      }
      if (isNotEmpty(openApi.getTags())) {
        if (isNotEmpty(schemaDb.getTags())) {
          List<Tag> tags = new ArrayList<>(openApi.getTags());
          tags.addAll(schemaDb.getTags());
          List<Tag> removeDuplicate = tags.stream()
              .collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(
                  Comparator.comparing(Tag::getName))), ArrayList::new));
          schemaDb.setTags(removeDuplicate);
        } else {
          schemaDb.setTags(openApi.getTags());
        }
      }
      if (isNotEmpty(openApi.getExtensions())) {
        schemaDb.getExtensions().putAll(openApi.getExtensions());
      }
    } else {
      // Replace
      schemaDb.setServers(openApi.getServers())
          .setSecurity(openApi.getSecurity())
          .setTags(openApi.getTags())
          .setExtensions(openApi.getExtensions());
    }
  }

}
