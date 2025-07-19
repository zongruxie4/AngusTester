package cloud.xcan.angus.core.tester.application.query.func;


import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.func.trash.FuncTrash;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface FuncTrashQuery {

  Long count(Long projectId);

  Page<FuncTrash> list(GenericSpecification<FuncTrash> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match);

  FuncTrash findMyTrashForBiz(Long id, String biz);

}
