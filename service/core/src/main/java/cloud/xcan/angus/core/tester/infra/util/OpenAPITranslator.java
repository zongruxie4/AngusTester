package cloud.xcan.angus.core.tester.infra.util;

import cloud.xcan.angus.extension.angustester.deepseek.api.TranslationService;
import cloud.xcan.angus.spec.locale.SupportedLanguage;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariable;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

/**
 * OpenAPI Specification Translator Translates all translatable fields in an OpenAPI specification
 * document
 * <p>
 * Features: - Translation caching to avoid redundant API calls - Parallel processing for improved
 * performance - Fine-grained field-level translation control
 */
@Slf4j
public class OpenAPITranslator {

  private final TranslationService translationService;
  private final SupportedLanguage sourceLanguage;
  private final SupportedLanguage targetLanguage;

  // Translation cache: original text -> translated text
  private final ConcurrentMap<String, String> translationCache = new ConcurrentHashMap<>();

  // Custom thread pool for parallel processing
  private final ForkJoinPool translationPool = new ForkJoinPool(
      Runtime.getRuntime().availableProcessors() * 2);

  public OpenAPITranslator(TranslationService translationService,
      SupportedLanguage sourceLanguage, SupportedLanguage targetLanguage) {
    this.translationService = translationService;
    this.sourceLanguage = sourceLanguage;
    this.targetLanguage = targetLanguage;
  }

  /**
   * Translate entire OpenAPI document
   *
   * @param openApi OpenAPI specification to translate
   */
  public void translateOpenAPI(OpenAPI openApi) {
    if (openApi == null) {
      return;
    }

    // Translate top-level sections in sequence
    translateInfo(openApi.getInfo());
    translateExternalDocs(openApi.getExternalDocs());
    translateServers(openApi.getServers());
    translateTags(openApi.getTags());

    // Parallel processing for paths and components
    translationPool.submit(() -> {
      translatePaths(openApi.getPaths());
      translateComponents(openApi.getComponents());
    }).join();

    // Shutdown pool after completion (optional)
    translationPool.shutdown();
  }

  // Translate Info section
  private void translateInfo(Info info) {
    if (info == null) {
      return;
    }

    translateField("info.title", info::getTitle, info::setTitle);
    translateField("info.description", info::getDescription, info::setDescription);
    translateField("info.summary", info::getSummary, info::setSummary);
  }

  // Translate External Documentation
  private void translateExternalDocs(ExternalDocumentation externalDocs) {
    if (externalDocs == null) {
      return;
    }

    translateField("externalDocs.description", externalDocs::getDescription,
        externalDocs::setDescription);
  }

  // Translate Servers list
  private void translateServers(List<Server> servers) {
    if (servers == null) {
      return;
    }

    // Process servers in parallel
    servers.parallelStream().forEach(server -> {
      translateField("server.description", server::getDescription, server::setDescription);

      // Process server variables
      if (server.getVariables() != null) {
        server.getVariables().entrySet().parallelStream().forEach(entry -> {
          ServerVariable variable = entry.getValue();
          translateField("server.variable.description",
              variable::getDescription, variable::setDescription);
        });
      }
    });
  }

  // Translate Tags list
  private void translateTags(List<Tag> tags) {
    if (tags == null) {
      return;
    }

    // Process tags in parallel
    tags.parallelStream().forEach(tag -> {
      translateField("tag.name", tag::getName, tag::setName);
      translateField("tag.description", tag::getDescription, tag::setDescription);
      translateExternalDocs(tag.getExternalDocs());
    });
  }

  // Translate Paths section
  private void translatePaths(Paths paths) {
    if (paths == null) {
      return;
    }

    // Process paths in parallel
    paths.entrySet().parallelStream().forEach(entry -> {
      PathItem pathItem = entry.getValue();
      translateField("path.summary", pathItem::getSummary, pathItem::setSummary);
      translateField("path.description", pathItem::getDescription, pathItem::setDescription);

      // Process path-level servers
      if (pathItem.getServers() != null) {
        pathItem.getServers().parallelStream().forEach(server -> {
          translateField("path.server.description", server::getDescription, server::setDescription);

          // Process server variables
          if (server.getVariables() != null) {
            server.getVariables().entrySet().parallelStream().forEach(varEntry -> {
              translateField("path.server.variable.description",
                  varEntry.getValue()::getDescription,
                  varEntry.getValue()::setDescription);
            });
          }
        });
      }

      // Process path parameters
      if (pathItem.getParameters() != null) {
        pathItem.getParameters().parallelStream().forEach(this::translateParameter);
      }

      // Process operations
      translateOperation(pathItem.getGet(), "get");
      translateOperation(pathItem.getPut(), "put");
      translateOperation(pathItem.getPost(), "post");
      translateOperation(pathItem.getDelete(), "delete");
      translateOperation(pathItem.getOptions(), "options");
      translateOperation(pathItem.getHead(), "head");
      translateOperation(pathItem.getPatch(), "patch");
      translateOperation(pathItem.getTrace(), "trace");
    });
  }

  // Translate API operation
  private void translateOperation(Operation operation, String httpMethod) {
    if (operation == null) {
      return;
    }

    // Translate operation-level fields
    translateField(httpMethod + ".tags", () -> String.join(",", operation.getTags()),
        value -> operation.setTags(List.of(value.split(","))));

    translateField(httpMethod + ".summary", operation::getSummary, operation::setSummary);
    translateExternalDocs(operation.getExternalDocs());

    // Process parameters
    if (operation.getParameters() != null) {
      operation.getParameters().parallelStream().forEach(this::translateParameter);
    }

    // Process request body
    if (operation.getRequestBody() != null) {
      translateField(httpMethod + ".requestBody.description",
          operation.getRequestBody()::getDescription,
          operation.getRequestBody()::setDescription);

      if (operation.getRequestBody().getContent() != null) {
        operation.getRequestBody().getContent().entrySet().parallelStream().forEach(entry -> {
          translateSchemaFields(entry.getValue().getSchema(), httpMethod + ".requestBody.schema");
        });
      }
    }

    // Process responses
    if (operation.getResponses() != null) {
      operation.getResponses().entrySet().parallelStream().forEach(responseEntry -> {
        ApiResponse response = responseEntry.getValue();
        translateField(httpMethod + ".response.description", response::getDescription,
            response::setDescription);

        if (response.getContent() != null) {
          response.getContent().entrySet().parallelStream().forEach(contentEntry -> {
            translateSchemaFields(contentEntry.getValue().getSchema(),
                httpMethod + ".response.schema");
          });
        }
      });
    }

    // Process operation-level servers
    if (operation.getServers() != null) {
      operation.getServers().parallelStream().forEach(server -> {
        translateField(httpMethod + ".server.description", server::getDescription,
            server::setDescription);

        if (server.getVariables() != null) {
          server.getVariables().entrySet().parallelStream().forEach(varEntry -> {
            translateField(httpMethod + ".server.variable.description",
                varEntry.getValue()::getDescription,
                varEntry.getValue()::setDescription);
          });
        }
      });
    }
  }

  // Translate parameter
  private void translateParameter(Parameter parameter) {
    if (parameter == null) {
      return;
    }

    translateField("parameter.description", parameter::getDescription, parameter::setDescription);
    translateSchemaFields(parameter.getSchema(), "parameter.schema");
  }

  // Translate Components section
  private void translateComponents(Components components) {
    if (components == null) {
      return;
    }

    // Process schemas in parallel
    if (components.getSchemas() != null) {
      components.getSchemas().entrySet().parallelStream().forEach(entry -> {
        translateSchemaFields(entry.getValue(), "component.schema");
      });
    }

    // Process responses in parallel
    if (components.getResponses() != null) {
      components.getResponses().entrySet().parallelStream().forEach(entry -> {
        ApiResponse response = entry.getValue();
        translateField("component.response.description", response::getDescription,
            response::setDescription);

        if (response.getContent() != null) {
          response.getContent().entrySet().parallelStream().forEach(contentEntry -> {
            translateSchemaFields(contentEntry.getValue().getSchema(), "component.response.schema");
          });
        }
      });
    }

    // Process parameters in parallel
    if (components.getParameters() != null) {
      components.getParameters().entrySet().parallelStream().forEach(entry -> {
        translateParameter(entry.getValue());
      });
    }

    // Process request bodies in parallel
    if (components.getRequestBodies() != null) {
      components.getRequestBodies().entrySet().parallelStream().forEach(entry -> {
        translateField("component.requestBody.description",
            entry.getValue()::getDescription,
            entry.getValue()::setDescription);

        if (entry.getValue().getContent() != null) {
          entry.getValue().getContent().entrySet().parallelStream().forEach(contentEntry -> {
            translateSchemaFields(contentEntry.getValue().getSchema(),
                "component.requestBody.schema");
          });
        }
      });
    }

    // Process headers in parallel
    if (components.getHeaders() != null) {
      components.getHeaders().entrySet().parallelStream().forEach(entry -> {
        translateField("component.header.description",
            entry.getValue()::getDescription,
            entry.getValue()::setDescription);

        if (entry.getValue().getContent() != null) {
          entry.getValue().getContent().entrySet().parallelStream().forEach(contentEntry -> {
            translateSchemaFields(contentEntry.getValue().getSchema(), "component.header.schema");
          });
        }
      });
    }

    // Process security schemes in parallel
    if (components.getSecuritySchemes() != null) {
      components.getSecuritySchemes().entrySet().parallelStream().forEach(entry -> {
        translateField("component.securityScheme.description",
            entry.getValue()::getDescription,
            entry.getValue()::setDescription);
      });
    }
  }

  // Translate schema fields
  private void translateSchemaFields(Schema<?> schema, String contextPath) {
    if (schema == null) {
      return;
    }

    // Translate schema-level fields
    translateField(contextPath + ".title", schema::getTitle, schema::setTitle);
    translateField(contextPath + ".description", schema::getDescription, schema::setDescription);

    // Process properties in parallel
    if (schema.getProperties() != null) {
      schema.getProperties().entrySet().parallelStream().forEach(propEntry -> {
        String propContext = contextPath + ".property." + propEntry.getKey();
        translateSchemaFields(propEntry.getValue(), propContext);
      });
    }
  }

  /**
   * Translate field with caching and error handling
   *
   * @param fieldId Field identifier for logging
   * @param getter  Function to get original value
   * @param setter  Function to set translated value
   */
  private void translateField(String fieldId,
      Supplier<String> getter,
      java.util.function.Consumer<String> setter) {
    String original = getter.get();
    if (original == null || original.trim().isEmpty()) {
      return;
    }

    try {
      // Check translation cache first
      String translated = translationCache.computeIfAbsent(original, key -> {
        try {
          // Perform actual translation if not in cache
          return translationService.translate(key, sourceLanguage, targetLanguage);
        } catch (Exception e) {
          System.err.printf("Translation failed for [%s]: %s. Error: %s%n",
              fieldId, key, e.getMessage());
          return key; // Return original on failure
        }
      });

      // Set translated value
      setter.accept(translated);

      // Log translation result (consider using proper logger in production)
      log.debug(String.format("Translated [%s]: %s -> %s%n", fieldId, original, translated));
    } catch (Exception e) {
      log.error(String.format("Field processing failed for [%s]: %s. Error: %s%n",
          fieldId, original, e.getMessage()));
      setter.accept(original); // Fallback to original text
    }
  }
}
