package cloud.xcan.sdf.core.angustester.application.query.apis;

import cloud.xcan.sdf.core.angustester.domain.apis.trash.ApisTrash;

public interface ApisTrashQuery {

  Long count(Long projectId);

  ApisTrash findMyTrashForBiz(Long id, String biz);

}




