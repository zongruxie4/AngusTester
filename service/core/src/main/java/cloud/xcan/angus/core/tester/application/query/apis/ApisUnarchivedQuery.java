package cloud.xcan.angus.core.tester.application.query.apis;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.apis.unarchived.ApisUnarchived;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApisUnarchivedQuery {

  ApisUnarchived detail(Long id);

  Long count(Long projectId);

  Page<ApisUnarchived> find(GenericSpecification<ApisUnarchived> spec,
      Pageable pageable, Class<ApisUnarchived> clz);

  ApisUnarchived checkAndFind(Long id);

  void checkUpdateApiPermission(List<ApisUnarchived> apis);

}
