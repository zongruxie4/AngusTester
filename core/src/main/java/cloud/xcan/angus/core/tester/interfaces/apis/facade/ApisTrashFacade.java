package cloud.xcan.angus.core.tester.interfaces.apis.facade;

import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.trash.ApisTrashSearchDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.trash.ApisTrashDetailVo;
import cloud.xcan.angus.remote.PageResult;

/**
 * @author XiaoLong Liu
 */public interface ApisTrashFacade {

  void clear(Long id);

  void clearAll(Long projectId);

  void back(Long id);

  void backAll(Long projectId);

  Long count(Long projectId);

  PageResult<ApisTrashDetailVo> search(ApisTrashSearchDto dto);

}
