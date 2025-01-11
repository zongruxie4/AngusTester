package cloud.xcan.sdf.core.angustester.interfaces.apis.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.trash.ApisTrashSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.trash.ApisTrashDetailVo;

/**
 * @author xiaolong.liu
 */
public interface ApisTrashFacade {

  void clear(Long id);

  void clearAll(Long projectId);

  void back(Long id);

  void backAll(Long projectId);

  Long count(Long projectId);

  PageResult<ApisTrashDetailVo> search(ApisTrashSearchDto dto);

}
