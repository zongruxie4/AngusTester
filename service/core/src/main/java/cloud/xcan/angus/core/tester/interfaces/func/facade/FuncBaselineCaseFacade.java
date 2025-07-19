package cloud.xcan.angus.core.tester.interfaces.func.facade;

import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.FuncCaseFindDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.FuncCaseDetailVo;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.FuncCaseListVo;
import cloud.xcan.angus.remote.PageResult;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashSet;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface FuncBaselineCaseFacade {

  void add(Long baselineId, HashSet<Long> caseIds);

  void delete(Long baselineId, HashSet<Long> caseIds);

  FuncCaseDetailVo detail(Long baselineId, Long caseId);

  PageResult<FuncCaseListVo> list(boolean export, Long baselineId, FuncCaseFindDto dto);

  ResponseEntity<Resource> export(Long baselineId, FuncCaseFindDto dto, HttpServletResponse response);

}
