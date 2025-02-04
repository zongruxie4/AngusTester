package cloud.xcan.sdf.core.angustester.application.converter;

import static cloud.xcan.sdf.core.angustester.application.query.services.impl.ServicesCompQueryImpl.toComponent;
import static cloud.xcan.sdf.core.angustester.domain.apis.converter.ApiResponseConverter.OPENAPI_MAPPER;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.distinctByKey;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static io.swagger.v3.oas.models.Components.COMPONENTS_EXTENSIONS_REF;

import cloud.xcan.sdf.core.angustester.domain.services.comp.ServicesComp;
import cloud.xcan.sdf.core.angustester.domain.services.comp.ServicesCompType;
import cloud.xcan.sdf.spec.experimental.Assert;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.links.Link;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.SneakyThrows;

public class ServicesCompConverter {

  public static ServicesComp toProjectComp(Long serviceId, ServicesCompType type, String key,
      String model) {
    Assert.assertNotEmpty(model, "Component model cannot be empty");
    switch (type) {
      case schemas: {
        Schema schema = toComponent(Schema.class, model);
        return toProjectSchemaComp(serviceId, key, schema);
      }
      case responses: {
        ApiResponse response = toComponent(ApiResponse.class, model);
        return toProjectResponseComp(serviceId, key, response);
      }
      case parameters: {
        Parameter parameter = toComponent(Parameter.class, model);
        return toProjectParameterComp(serviceId, key, parameter);
      }
      case examples: {
        Example example = toComponent(Example.class, model);
        return toProjectExampleComp(serviceId, key, example);
      }
      case requestBodies: {
        RequestBody requestBody = toComponent(RequestBody.class, model);
        return toProjectRequestBodyComp(serviceId, key, requestBody);
      }
      case headers: {
        Header header = toComponent(Header.class, model);
        return toProjectHeaderComp(serviceId, key, header);
      }
      case securitySchemes: {
        SecurityScheme securityScheme = toComponent(SecurityScheme.class, model);
        return toProjectSecuritySchemaComp(serviceId, key, securityScheme);
      }
      case links: {
        Link link = toComponent(Link.class, model);
        return toProjectLinkComp(serviceId, key, link);
      }
      case extensions: {
        Object extension = toComponent(Object.class, model);
        return toProjectExtensionComp(serviceId, key, extension);
      }
      case callbacks: {
      }
      case pathItems: {
      }
      default: {
        throw new IllegalArgumentException("Unsupported type: " + type);
      }
    }
  }

  @SneakyThrows
  public static Map<String, ServicesComp> toProjectComp(Long serviceId, Components components) {
    Map<String, ServicesComp> flat = new HashMap<>();
    if (isNotEmpty(components.getSchemas())) {
      for (String key : components.getSchemas().keySet()) {
        Schema schema = components.getSchemas().get(key);
        flat.put(Schema.format$ref(key), toProjectSchemaComp(serviceId, key, schema));
      }
    }
    if (isNotEmpty(components.getResponses())) {
      for (String key : components.getResponses().keySet()) {
        ApiResponse response = components.getResponses().get(key);
        flat.put(ApiResponse.format$ref(key), toProjectResponseComp(serviceId, key, response));
      }
    }
    if (isNotEmpty(components.getParameters())) {
      for (String key : components.getParameters().keySet()) {
        Parameter parameter = components.getParameters().get(key);
        flat.put(Parameter.format$ref(key), toProjectParameterComp(serviceId, key, parameter));
      }
    }
    if (isNotEmpty(components.getExamples())) {
      for (String key : components.getExamples().keySet()) {
        Example example = components.getExamples().get(key);
        flat.put(Example.format$ref(key), toProjectExampleComp(serviceId, key, example));
      }
    }
    if (isNotEmpty(components.getRequestBodies())) {
      for (String key : components.getRequestBodies().keySet()) {
        RequestBody requestBody = components.getRequestBodies().get(key);
        flat.put(RequestBody.format$ref(key),
            toProjectRequestBodyComp(serviceId, key, requestBody));
      }
    }
    if (isNotEmpty(components.getHeaders())) {
      for (String key : components.getHeaders().keySet()) {
        Header header = components.getHeaders().get(key);
        flat.put(Header.format$ref(key), toProjectHeaderComp(serviceId, key, header));
      }
    }
    if (isNotEmpty(components.getSecuritySchemes())) {
      for (String key : components.getSecuritySchemes().keySet()) {
        SecurityScheme securityScheme = components.getSecuritySchemes().get(key);
        flat.put(SecurityScheme.format$ref(key),
            toProjectSecuritySchemaComp(serviceId, key, securityScheme));
      }
    }
    if (isNotEmpty(components.getLinks())) {
      for (String key : components.getLinks().keySet()) {
        Link link = components.getLinks().get(key);
        flat.put(Link.format$ref(key), toProjectLinkComp(serviceId, key, link));
      }
      //} else if (isNotEmpty(components.getCallbacks())) {
    }
    if (isNotEmpty(components.getExtensions())) {
      for (String key : components.getExtensions().keySet()) {
        Object extension = components.getExtensions().get(key);
        flat.put(COMPONENTS_EXTENSIONS_REF + key,
            toProjectExtensionComp(serviceId, key, extension));
      }
      //} else if (isNotEmpty(components.getPathItems())) {
      //
    }
    return flat;
  }

  public static Components toOpenApiComp(Map<ServicesCompType, List<ServicesComp>> compsMap) {
    Components components = new Components();
    for (ServicesCompType type : compsMap.keySet()) {
      List<ServicesComp> comps = compsMap.get(type).stream()
          .filter(distinctByKey(ServicesComp::getRef)).collect(Collectors.toList());
      switch (type) {
        case schemas: {
          components.schemas(comps.stream().collect(
              Collectors.toMap(ServicesComp::getKey, x -> x.toComponent(Schema.class))));
          break;
        }
        case responses: {
          components.responses(comps.stream().collect(
              Collectors.toMap(ServicesComp::getKey, x -> x.toComponent(ApiResponse.class))));
          break;
        }
        case parameters: {
          components.parameters(comps.stream().collect(
              Collectors.toMap(ServicesComp::getKey, x -> x.toComponent(Parameter.class))));
          break;
        }
        case examples: {
          components.examples(comps.stream().collect(
              Collectors.toMap(ServicesComp::getKey, x -> x.toComponent(Example.class))));
          break;
        }
        case requestBodies: {
          components.requestBodies(comps.stream().collect(
              Collectors.toMap(ServicesComp::getKey, x -> x.toComponent(RequestBody.class))));
          break;
        }
        case headers: {
          components.headers(comps.stream().collect(
              Collectors.toMap(ServicesComp::getKey, x -> x.toComponent(Header.class))));
          break;
        }
        case securitySchemes: {
          components.securitySchemes(comps.stream().collect(
              Collectors.toMap(ServicesComp::getKey, x -> x.toComponent(SecurityScheme.class))));
          break;
        }
        case links: {
          components.links(comps.stream().collect(
              Collectors.toMap(ServicesComp::getKey, x -> x.toComponent(Link.class))));
          break;
        }
        case extensions: {
          components.extensions(comps.stream().collect(
              Collectors.toMap(ServicesComp::getKey, x -> x.toComponent(Map.class))));
          break;
        }
        default: {
          // NOOP
        }
      }
    }
    return components;
  }

  @SneakyThrows
  private static ServicesComp toProjectSchemaComp(Long serviceId, String key, Schema schema) {
    return new ServicesComp().setServiceId(serviceId).setType(ServicesCompType.schemas)
        .setKey(key).setRef(Schema.format$ref(key))
        .setSchema(schema)
        .setModel(OPENAPI_MAPPER.writeValueAsString(schema))
        .setDescription(schema.getDescription())
        .setSchemaHash(schema.hashCode());
  }

  @SneakyThrows
  private static ServicesComp toProjectResponseComp(Long serviceId, String key,
      ApiResponse response) {
    return new ServicesComp().setServiceId(serviceId).setType(ServicesCompType.responses)
        .setKey(key).setRef(ApiResponse.format$ref(key))
        .setResponse(response)
        .setModel(OPENAPI_MAPPER.writeValueAsString(response))
        .setDescription(response.getDescription())
        .setSchemaHash(response.hashCode());
  }

  @SneakyThrows
  private static ServicesComp toProjectParameterComp(Long serviceId, String key,
      Parameter parameter) {
    return new ServicesComp().setServiceId(serviceId).setType(ServicesCompType.parameters)
        .setKey(key).setRef(Parameter.format$ref(key))
        .setParameter(parameter)
        .setModel(OPENAPI_MAPPER.writeValueAsString(parameter))
        .setDescription(parameter.getDescription())
        .setSchemaHash(parameter.hashCode());
  }

  @SneakyThrows
  private static ServicesComp toProjectExampleComp(Long serviceId, String key, Example example) {
    return new ServicesComp().setServiceId(serviceId).setType(ServicesCompType.examples)
        .setKey(key).setRef(Example.format$ref(key))
        .setExample(example)
        .setModel(OPENAPI_MAPPER.writeValueAsString(example))
        .setDescription(example.getDescription())
        .setSchemaHash(example.hashCode());
  }

  @SneakyThrows
  private static ServicesComp toProjectRequestBodyComp(Long serviceId, String key,
      RequestBody requestBody) {
    return new ServicesComp().setServiceId(serviceId).setType(ServicesCompType.requestBodies)
        .setKey(key).setRef(RequestBody.format$ref(key))
        .setRequestBody(requestBody)
        .setModel(OPENAPI_MAPPER.writeValueAsString(requestBody))
        .setDescription(requestBody.getDescription())
        .setSchemaHash(requestBody.hashCode());
  }

  @SneakyThrows
  private static ServicesComp toProjectHeaderComp(Long serviceId, String key, Header header) {
    return new ServicesComp().setServiceId(serviceId).setType(ServicesCompType.headers)
        .setKey(key).setRef(Header.format$ref(key))
        .setHeader(header)
        .setModel(OPENAPI_MAPPER.writeValueAsString(header))
        .setDescription(header.getDescription())
        .setSchemaHash(header.hashCode());
  }

  @SneakyThrows
  private static ServicesComp toProjectSecuritySchemaComp(Long serviceId, String key,
      SecurityScheme securityScheme) {
    return new ServicesComp().setServiceId(serviceId).setType(ServicesCompType.securitySchemes)
        .setKey(key).setRef(SecurityScheme.format$ref(key))
        .setSecurityScheme(securityScheme)
        .setModel(OPENAPI_MAPPER.writeValueAsString(securityScheme))
        .setDescription(securityScheme.getDescription())
        .setSchemaHash(securityScheme.hashCode());
  }

  @SneakyThrows
  private static ServicesComp toProjectLinkComp(Long serviceId, String key, Link link) {
    return new ServicesComp().setServiceId(serviceId).setType(ServicesCompType.links)
        .setKey(key).setRef(Link.format$ref(key))
        .setLink(link)
        .setModel(OPENAPI_MAPPER.writeValueAsString(link))
        .setDescription(link.getDescription())
        .setSchemaHash(link.hashCode());
  }

  @SneakyThrows
  private static ServicesComp toProjectExtensionComp(Long serviceId, String key, Object extension) {
    return new ServicesComp().setServiceId(serviceId).setType(ServicesCompType.extensions)
        .setKey(key).setRef(COMPONENTS_EXTENSIONS_REF + key)
        .setExtension(extension)
        .setModel(OPENAPI_MAPPER.writeValueAsString(extension))
        //.setDescription(link.getDescription())
        .setSchemaHash(extension.hashCode());
  }

  public static void openApiToUpdateComp(ServicesComp compDb, ServicesComp openApiComp) {
    compDb.setKey(openApiComp.getKey())
        .setRef(openApiComp.getRef())
        .setModel(openApiComp.getModel())
        .setDescription(openApiComp.getDescription())
        .setSchemaHash(openApiComp.getSchemaHash());
  }

  public static void updateComp(ServicesComp compDb, ServicesComp comp) {
    compDb.setServiceId(comp.getServiceId())
        .setType(comp.getType())
        .setKey(comp.getKey()).setRef(comp.getRef())
        .setModel(comp.getModel())
        .setDescription(comp.getDescription())
        .setSchemaHash(comp.getSchemaHash());
  }

  public static ServicesComp toClonedProjectComp(ServicesComp comp, Long serviceId) {
    return new ServicesComp().setServiceId(serviceId)
        .setType(comp.getType())
        .setKey(comp.getKey())
        .setRef(comp.getRef())
        .setModel(comp.getModel())
        .setDescription(comp.getDescription());
  }

}
