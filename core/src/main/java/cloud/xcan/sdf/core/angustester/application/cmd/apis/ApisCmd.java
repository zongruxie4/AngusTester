package cloud.xcan.sdf.core.angustester.application.cmd.apis;

import cloud.xcan.sdf.core.angustester.domain.apis.Apis;
import cloud.xcan.sdf.core.angustester.domain.services.ServiceApisScope;
import cloud.xcan.sdf.core.angustester.domain.services.Services;
import cloud.xcan.sdf.core.angustester.domain.services.schema.SchemaFormat;
import cloud.xcan.sdf.model.apis.ApiStatus;
import cloud.xcan.sdf.spec.experimental.IdKey;
import cloud.xcan.sdf.spec.http.HttpMethod;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface ApisCmd {

  List<IdKey<Long, Object>> add(List<Apis> apis, Services servicesDb, boolean saveActivity);

  List<IdKey<Long, Object>> archive(List<Apis> apis);

  void update(List<Apis> apis, boolean saveActivity);

  void rename(Long id, String name);

  List<IdKey<Long, Object>> replace(List<Apis> apis);

  IdKey<Long, Object> clone(Long id);

  void move(List<Long> apisIds, Long targetProjectId);

  void statusUpdate(Long id, ApiStatus status);

  void serverReplace(Long id, Server server);

  void serverReplaceAll(Long id, List<Server> servers);

  void serverDelete(Long id, Set<String> urls);

  void addParameters(Long serviceId, ServiceApisScope modifyScope, String matchEndpointRegex,
      HttpMethod matchMethod, Set<Long> selectedApisIds, Set<String> filterTags,
      List<Parameter> parameters);

  void updateParameters(Long serviceId, ServiceApisScope modifyScope,
      String matchEndpointRegex, HttpMethod matchMethod, Set<Long> selectedApisIds,
      Set<String> filterTags, List<Parameter> parameters);

  void deleteParameters(Long serviceId, ServiceApisScope modifyScope,
      String matchEndpointRegex, HttpMethod matchMethod, Set<Long> selectedApisIds,
      Set<String> filterTags, List<String> names);

  void enableParameters(Long serviceId, ServiceApisScope modifyScope,
      String matchEndpointRegex, HttpMethod matchMethod, Set<Long> selectedApisIds,
      Set<String> filterTags, List<String> names, Boolean enabled);

  void updateAuth(Long serviceId, ServiceApisScope modifyScope, String matchEndpointRegex,
      HttpMethod matchMethod, Set<Long> selectedApisIds, Set<String> filterTags,
      SecurityScheme authentication);

  void updateServer(Long serviceId, ServiceApisScope modifyScope, String matchEndpointRegex,
      HttpMethod matchMethod, Set<Long> selectedApisIds, Set<String> filterTags, Server server);

  void addVariableReference(Long serviceId, ServiceApisScope modifyScope,
      String matchEndpointRegex, HttpMethod matchMethod, Set<Long> selectedApisIds,
      Set<String> filterTags, List<String> variableNames);

  void deleteVariableReference(Long serviceId, ServiceApisScope modifyScope,
      String matchEndpointRegex, HttpMethod matchMethod, Set<Long> selectedApisIds,
      Set<String> filterTags, List<String> variableNames);

  void addDatasetReference(Long serviceId, ServiceApisScope modifyScope,
      String matchEndpointRegex, HttpMethod matchMethod, Set<Long> selectedApisIds,
      Set<String> filterTags, List<String> datasetNames);

  void deleteDatasetReference(Long serviceId, ServiceApisScope modifyScope,
      String matchEndpointRegex, HttpMethod matchMethod, Set<Long> selectedApisIds,
      Set<String> filterTags, List<String> datasetNames);

  IdKey<Long, Object> assocMockApisAdd(Long id, Long mockServiceId, String name);

  ResponseEntity<Resource> export(Long id, SchemaFormat format, HttpServletResponse response);

  void delete(Collection<Long> ids, boolean checkPermission);

  void update0(List<Apis> apisDb);

  void delete0(List<Long> ids);

  void updateSyncApis(Map<String, Apis> updatedApisDbMap, Map<String, Apis> openApisMap);


}
