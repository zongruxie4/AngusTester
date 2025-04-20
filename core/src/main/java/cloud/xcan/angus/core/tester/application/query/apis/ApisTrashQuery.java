package cloud.xcan.angus.core.tester.application.query.apis;

import cloud.xcan.angus.core.tester.domain.apis.trash.ApisTrash;

public interface ApisTrashQuery {

  Long count(Long projectId);

  ApisTrash findMyTrashForBiz(Long id, String biz);

}




