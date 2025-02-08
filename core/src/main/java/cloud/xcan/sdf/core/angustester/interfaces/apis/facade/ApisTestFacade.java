package cloud.xcan.sdf.core.angustester.interfaces.apis.facade;

import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.test.ApisTestScriptGenerateDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.test.ApisTestTaskGenerateDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.test.TestResultDetailVo;
import cloud.xcan.sdf.model.script.TestType;
import io.swagger.v3.oas.models.servers.Server;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * @author xiaolong.liu
 */
public interface ApisTestFacade {

  void testEnabled(Long apisId, Set<TestType> testTypes, boolean enabled);

  List<TestType> testEnabledFind(Long apisId);

  void scriptGenerate(Long apisId, Set<ApisTestScriptGenerateDto> dtos);

  void scriptDelete(Long apisId, Set<TestType> testTypes);

  void testTaskGenerate(Long apisId, @Nullable Long taskSprintId, Set<ApisTestTaskGenerateDto> dto);

  void testTaskRetest(Long apisId);

  void testTaskReopen(Long apisId);

  void testTaskDelete(Long apisId, Set<TestType> testTypes);

  void testExecAdd(Long apisId, Set<TestType> testTypes, @Nullable List<Server> servers);

  void testExecAdd(HashSet<Long> apisIds, HashSet<TestType> testTypes,
      @Nullable List<Server> servers);

  void testCaseExecAdd(Long apisId, LinkedHashSet<Long> caseIds);

  TestResultDetailVo testResultDetail(Long apisId);

}
