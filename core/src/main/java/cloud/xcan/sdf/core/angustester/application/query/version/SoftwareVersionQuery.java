package cloud.xcan.sdf.core.angustester.application.query.version;

import cloud.xcan.sdf.core.angustester.domain.version.SoftwareVersion;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface SoftwareVersionQuery {

  SoftwareVersion detail(Long id);

  Page<SoftwareVersion> find(GenericSpecification<SoftwareVersion> spec, PageRequest pageable);

  SoftwareVersion checkAndFind(Long id);

  List<SoftwareVersion> checkAndFind(Collection<Long> ids);

  void checkExits(Long projectId, String name);

  void checkNotExits(Long projectId, String name);

  void setVersionProgress(List<SoftwareVersion> versionsDb);
}
