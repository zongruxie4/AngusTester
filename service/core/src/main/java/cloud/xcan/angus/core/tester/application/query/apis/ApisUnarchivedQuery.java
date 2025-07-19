package cloud.xcan.angus.core.tester.application.query.apis;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.apis.unarchived.ApisUnarchived;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ApisUnarchivedQuery {

  ApisUnarchived detail(Long id);

  Long count(Long projectId);

  Page<ApisUnarchived> list(GenericSpecification<ApisUnarchived> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match);

  ApisUnarchived checkAndFind(Long id);

  void checkUpdateApiPermission(List<ApisUnarchived> apis);

}
