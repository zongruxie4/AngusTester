package cloud.xcan.angus.core.tester.application.query.apis;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.apis.trash.ApisTrash;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ApisTrashQuery {

  Long count(Long projectId);

  Page<ApisTrash> list(GenericSpecification<ApisTrash> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match);

  ApisTrash findMyTrashForBiz(Long id, String biz);

}




