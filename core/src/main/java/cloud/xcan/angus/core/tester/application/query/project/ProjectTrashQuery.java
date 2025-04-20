package cloud.xcan.angus.core.tester.application.query.project;


import cloud.xcan.angus.core.tester.domain.project.trash.ProjectTrash;

public interface ProjectTrashQuery {

  Long count();

  ProjectTrash findMyTrashForBiz(Long id, String biz);
}
