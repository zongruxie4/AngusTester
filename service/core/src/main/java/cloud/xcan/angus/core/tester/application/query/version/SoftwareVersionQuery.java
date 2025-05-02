package cloud.xcan.angus.core.tester.application.query.version;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.version.SoftwareVersion;
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
