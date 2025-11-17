package cloud.xcan.angus.core.tester.interfaces.apis.facade;

import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.test.ApisTestScriptGenerateDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.test.TestResultDetailVo;
import cloud.xcan.angus.model.script.TestType;
import io.swagger.v3.oas.models.servers.Server;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * @author XiaoLong Liu
 */
public interface ApisTestFacade {

  void testEnabled(Long apisId, Set<TestType> testTypes, boolean enabled);

  List<TestType> testEnabledFind(Long apisId);

  void scriptGenerate(Long apisId, Set<ApisTestScriptGenerateDto> dto);

  void scriptDelete(Long apisId, Set<TestType> testTypes);

  void testExecAdd(Long apisId, Set<TestType> testTypes, @Nullable List<Server> servers);

  void testExecAdd(HashSet<Long> apisIds, HashSet<TestType> testTypes,
      @Nullable List<Server> servers);

  void testCaseExecAdd(Long apisId, LinkedHashSet<Long> caseIds);

  TestResultDetailVo testResultDetail(Long apisId);

}
