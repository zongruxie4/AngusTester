package cloud.xcan.angus.core.tester.application.query.func;


import cloud.xcan.angus.core.tester.domain.func.trash.FuncTrash;

public interface FuncTrashQuery {

  Long count(Long projectId);

  FuncTrash findMyTrashForBiz(Long id, String biz);

}
