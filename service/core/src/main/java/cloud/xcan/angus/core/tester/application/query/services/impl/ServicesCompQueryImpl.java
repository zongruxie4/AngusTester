package cloud.xcan.angus.core.tester.application.query.services.impl;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotNull;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.angus.core.tester.application.converter.ApisConverter.findAllRef0;
import static cloud.xcan.angus.core.tester.domain.apis.converter.ApiResponseConverter.OPENAPI_MAPPER;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.biz.Biz;
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

@Biz
public class ServicesCompQueryImpl implements ServicesCompQuery {

  @Resource
  private ServicesCompRepo servicesCompRepo;

  @Resource
  private ServicesAuthQuery servicesAuthQuery;

  @Resource
  private ServicesCompQuery servicesCompQuery;

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
            ? cached.stream().filter(x -> types.contains(x.getType())).collect(Collectors.toList())
            : cached.stream().filter(x -> types.contains(x.getType()) && keys.contains(x.getKey()))
                .collect(Collectors.toList());
      }
    }.execute();
  }

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
            .filter(x -> refs.contains(x.getRef())).collect(Collectors.toList());
      }
    }.execute();
  }

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

  @Cacheable(key = "'servicesId_' + #serviceId", value = "servicesComps")
  @Override
  public List<ServicesComp> findByServiceId(Long serviceId) {
    return servicesCompRepo.findByServiceId(serviceId);
  }

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

  @Override
  public void checkRefFormat(String ref) {
    assertTrue(ReferenceUtils.isLocalRefToComponents(ref),
        "Only local reference components are supported");
    assertTrue(ref.split("/").length == 4,
        "The parameter ref format is invalid, it must be: #/components/{type}/{key}");
  }

  @Override
  public void checkRefFormat(Collection<String> refs) {
    for (String ref : refs) {
      checkRefFormat(ref);
    }
  }

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
