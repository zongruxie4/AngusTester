package cloud.xcan.sdf.core.angustester.application.query.project;


import cloud.xcan.sdf.core.angustester.domain.project.trash.ProjectTrash;

public interface ProjectTrashQuery {

  Long count();

  ProjectTrash findMyTrashForBiz(Long id, String biz);
}