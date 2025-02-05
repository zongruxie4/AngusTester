package cloud.xcan.sdf.core.angustester.application.query.data;

import cloud.xcan.angus.model.element.dataset.DatasetParameter;
import cloud.xcan.angus.model.element.extraction.DefaultExtraction;
import cloud.xcan.sdf.core.angustester.domain.data.dataset.Dataset;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DatasetQuery {

  Dataset detail(Long id);

  LinkedHashMap<String, List<String>> valuePreview(Long id, String name,
      List<DatasetParameter> parameters, DefaultExtraction extraction, Long rowNum);

  Map<String, String> valuePreview(List<Long> ids);

  Page<Dataset> find(GenericSpecification<Dataset> spec, Pageable pageable);

  List<Dataset> findByProjectAndIds(Long projectId, LinkedHashSet<Long> ids);

  Dataset checkAndFind(Long id);

  List<Dataset> checkAndFind(Collection<Long> ids);

  List<Dataset> checkAndFindByName(Long projectId, List<String> names);

  void checkTenantQuota(int inc);

  void checkRequiredParam(Dataset dataset);

  void checkAddNameExists(Dataset dataset);

  void checkUpdateNameExists(Dataset dataset);

  void setSafeCloneName(Dataset dataset);

}
