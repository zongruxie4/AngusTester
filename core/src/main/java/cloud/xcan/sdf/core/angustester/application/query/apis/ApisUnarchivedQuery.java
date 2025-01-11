package cloud.xcan.sdf.core.angustester.application.query.apis;

import cloud.xcan.sdf.core.angustester.domain.apis.unarchived.ApisUnarchived;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
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
