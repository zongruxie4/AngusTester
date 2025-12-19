package cloud.xcan.angus.core.tester.application.query.services.impl;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotNull;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.angus.core.tester.application.converter.ApisConverter.findAllRef0;
import static cloud.xcan.angus.core.tester.domain.apis.converter.ApiResponseConverter.OPENAPI_MAPPER;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.converter.ServicesCompConverter;
import cloud.xcan.angus.core.tester.application.query.services.ServicesAuthQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesCompQuery;
import cloud.xcan.angus.core.tester.domain.services.comp.ServicesComp;
import cloud.xcan.angus.core.tester.domain.services.comp.ServicesCompRepo;
import cloud.xcan.angus.core.tester.domain.services.comp.ServicesCompType;
import cloud.xcan.angus.core.tester.infra.util.RefResolver;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.parser.reference.ReferenceUtils;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.SneakyThrows;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Implementation of ServicesCompQuery for services component management and query operations.
 * </p>
 * <p>
 * Provides methods for services component CRUD operations, reference resolution, and OpenAPI
 * component handling.
 * </p>
 */
@Service
public class ServicesCompQueryImpl implements ServicesCompQuery {

  @Resource
  private ServicesCompRepo servicesCompRepo;
  @Resource
  private ServicesAuthQuery servicesAuthQuery;
  @Resource
  private ServicesCompQuery servicesCompQuery;

  /**
   * <p>
   * Get detailed information of a services component by reference.
   * </p>
   * <p>
   * Retrieves component details and resolves nested references to build a complete component model.
   * Supports recursive reference resolution for complex component structures.
   * </p>
   *
   * @param serviceId Service ID
   * @param ref       Component reference
   * @return Services component with resolved references
   */
  @Override
  public ServicesComp detailByRef(Long serviceId, String ref) {
    return new BizTemplate<ServicesComp>() {
      @Override
      protected void checkParams() {
        // NOOP:: In listByRef#check()
        // checkRefFormat(ref);
        // projectAuthQuery.checkViewAuth(getUserId(), serviceId);
      }

      @Override
      protected ServicesComp process() {
        List<ServicesComp> comps = servicesCompQuery.findByServiceId(serviceId);
        if (isEmpty(comps)) {
          return null;
        }
        // Find ref models
        ServicesComp comp = comps.stream().filter(x -> x.getRef().equals(ref)).findFirst()
            .orElse(null);
        if (nonNull(comp) && isNotEmpty(comp.getModel())) {
          Set<String> refs;
          try {
            refs = RefResolver.findPropertyValues(comp.getModel(), "$ref");
            Map<String, String> allRefModels = new HashMap<>();
            if (isNotEmpty(refs)) {
              Map<String, String> compModelMap = comps.stream()
                  .collect(Collectors.toMap(ServicesComp::getRef, ServicesComp::getModel));
              findAllRef0(allRefModels, refs, compModelMap);
              comp.setResolvedRefModels(allRefModels);
            }
          } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
          }
        }
        return comp;
      }
    }.execute();
  }

  /**
   * <p>
   * List services components by type with optional key filtering.
   * </p>
   * <p>
   * Retrieves components filtered by type and optionally by specific keys. Requires view permission
   * for the service.
   * </p>
   *
   * @param serviceId Service ID
   * @param types     Set of component types to filter by
   * @param keys      Optional set of component keys to filter by
   * @return List of filtered services components
   */
  @Override
  public List<ServicesComp> listByType(Long serviceId, Set<ServicesCompType> types,
      @Nullable Set<String> keys) {
    return new BizTemplate<List<ServicesComp>>() {
      @Override
      protected void checkParams() {
        servicesAuthQuery.checkViewAuth(getUserId(), serviceId);
      }

      @Override
      protected List<ServicesComp> process() {
        List<ServicesComp> cached = servicesCompQuery.findByServiceId(serviceId);
        return isEmpty(keys)
            ? cached.stream().filter(x -> types.contains(x.getType())).toList()
            : cached.stream().filter(x -> types.contains(x.getType()) && keys.contains(x.getKey()))
                .toList();
      }
    }.execute();
  }

  /**
   * <p>
   * List services components by reference.
   * </p>
   * <p>
   * Retrieves components filtered by specific references. Validates reference format before
   * processing. Requires view permission for the service.
   * </p>
   *
   * @param serviceId Service ID
   * @param refs      Set of component references to filter by
   * @return List of filtered services components
   */
  @Override
  public List<ServicesComp> listByRef(Long serviceId, Set<String> refs) {
    return new BizTemplate<List<ServicesComp>>() {
      @Override
      protected void checkParams() {
        checkRefFormat(refs);
        servicesAuthQuery.checkViewAuth(getUserId(), serviceId);
      }

      @Override
      protected List<ServicesComp> process() {
        return servicesCompQuery.findByServiceId(serviceId).stream()
            .filter(x -> refs.contains(x.getRef())).toList();
      }
    }.execute();
  }

  /**
   * <p>
   * List all services components for a service.
   * </p>
   * <p>
   * Retrieves all components associated with a service. Requires view permission for the service.
   * </p>
   *
   * @param serviceId Service ID
   * @return List of all services components
   */
  @Override
  public List<ServicesComp> listAll(Long serviceId) {
    return new BizTemplate<List<ServicesComp>>() {
      @Override
      protected void checkParams() {
        servicesAuthQuery.checkViewAuth(getUserId(), serviceId);
      }

      @Override
      protected List<ServicesComp> process() {
        return servicesCompQuery.findByServiceId(serviceId);
      }
    }.execute();
  }

  /**
   * <p>
   * Find all services components by service ID with caching.
   * </p>
   * <p>
   * Retrieves all components for a service with Spring cache support for performance optimization.
   * </p>
   *
   * @param serviceId Service ID
   * @return List of services components
   */
  @Cacheable(key = "'servicesId_' + #serviceId", value = "servicesComps")
  @Override
  public List<ServicesComp> findByServiceId(Long serviceId) {
    return servicesCompRepo.findByServiceId(serviceId);
  }

  /**
   * <p>
   * Find OpenAPI components for a service.
   * </p>
   * <p>
   * Converts services components to OpenAPI Components format. This method is deprecated due to
   * potential ClassCastException issues with Gson serialization.
   * </p>
   *
   * @param serviceId Service ID
   * @return OpenAPI Components object
   * @deprecated Due to ClassCastException issues with Gson serialization
   */
  // java.lang.ClassCastException: class com.google.gson.internal.LinkedTreeMap cannot be cast to class io.swagger.v3.oas.models.Components
  // @Cacheable(key = "'servicesId_' + #serviceId", value = "openAPIComps")
  @Override
  @Deprecated
  public Components findOpenAPIComponents(Long serviceId) {
    List<ServicesComp> comps = servicesCompQuery.findByServiceId(serviceId);
    if (isEmpty(comps)) {
      return null;
    }
    return ServicesCompConverter.toOpenApiComp(comps.stream()
        .collect(Collectors.groupingBy(ServicesComp::getType)));
  }

  /**
   * <p>
   * Check if a reference format is valid.
   * </p>
   * <p>
   * Validates that the reference is a local reference to components and follows the correct
   * format.
   * </p>
   *
   * @param ref Reference string to validate
   */
  @Override
  public void checkRefFormat(String ref) {
    assertTrue(ReferenceUtils.isLocalRefToComponents(ref),
        "Only local reference components are supported");
    assertTrue(ref.split("/").length == 4,
        "The parameter ref format is invalid, it must be: #/components/{type}/{key}");
  }

  /**
   * <p>
   * Check if multiple reference formats are valid.
   * </p>
   * <p>
   * Validates each reference in the collection using the single reference validation method.
   * </p>
   *
   * @param refs Collection of reference strings to validate
   */
  @Override
  public void checkRefFormat(Collection<String> refs) {
    for (String ref : refs) {
      checkRefFormat(ref);
    }
  }

  /**
   * <p>
   * Convert a JSON model string to a component object.
   * </p>
   * <p>
   * Deserializes a JSON model string into the specified component class using OpenAPI mapper.
   * Validates that the resulting object is not null.
   * </p>
   *
   * @param clz   Target component class
   * @param model JSON model string
   * @param <T>   Component type
   * @return Deserialized component object
   */
  @SneakyThrows
  public static <T> T toComponent(Class<T> clz, String model) {
    if (isEmpty(model)) {
      return null;
    }
    T t = OPENAPI_MAPPER.readValue(model, clz);
    assertNotNull(t, "The component model is not in a valid format");
    return t;
  }
}
