package cloud.xcan.angus.core.tester.application.query.data;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.data.variables.Variable;
import cloud.xcan.angus.model.element.extraction.DefaultExtraction;
import io.swagger.v3.oas.models.servers.Server;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VariableQuery {

  Variable detail(Long id);

  String valuePreview(Long id, String name, String value, DefaultExtraction extraction);

  Map<String, String> valuePreview(List<Long> ids);

  Page<Variable> find(GenericSpecification<Variable> spec, Pageable pageable);

  List<Server> findServerByServiceId(Long serviceId);

  List<Variable> findByProjectAndIds(Long projectId, LinkedHashSet<Long> ids);

  Variable checkAndFind(Long id);

  List<Variable> checkAndFind(Collection<Long> ids);

  List<Variable> checkAndFindByName(Long projectId, List<String> names);

  void checkTenantQuota(int inc);

  void checkRequiredParam(Variable variable);

  void checkAddNameExists(Variable variable);

  void checkUpdateNameExists(Variable variable);

  void setSafeCloneName(Variable clonedVariable);


}
