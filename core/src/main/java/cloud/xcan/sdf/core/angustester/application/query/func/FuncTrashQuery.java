package cloud.xcan.sdf.core.angustester.application.query.func;


import cloud.xcan.sdf.core.angustester.domain.func.trash.FuncTrash;

public interface FuncTrashQuery {

  Long count(Long projectId);

  FuncTrash findMyTrashForBiz(Long id, String biz);

}