package cloud.xcan.sdf.core.angustester.interfaces.func.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.FuncCaseDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.FuncCaseListVo;
import java.util.HashSet;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface FuncBaselineCaseFacade {

  void add(Long baselineId, HashSet<Long> caseIds);

  void delete(Long baselineId, HashSet<Long> caseIds);

  FuncCaseDetailVo detail(Long baselineId, Long caseId);

  PageResult<FuncCaseListVo> list(Long baselineId, FuncCaseFindDto dto);

  PageResult<FuncCaseListVo> search(Long baselineId, boolean exportFlag, FuncCaseSearchDto dto);

  ResponseEntity<Resource> export(Long baselineId, FuncCaseSearchDto dto, HttpServletResponse response);

}
