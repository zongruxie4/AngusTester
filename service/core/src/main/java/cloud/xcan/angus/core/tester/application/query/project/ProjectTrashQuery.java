package cloud.xcan.angus.core.tester.application.query.project;


import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.project.trash.ProjectTrash;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ProjectTrashQuery {

  Long count();

  Page<ProjectTrash> list(GenericSpecification<ProjectTrash> specification, PageRequest pageable,
      boolean fullTextSearch, String[] match);

  ProjectTrash findMyTrashForBiz(Long id, String biz);

}
