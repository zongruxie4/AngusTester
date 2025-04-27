package cloud.xcan.angus.core.tester.application.cmd.scenario;

import cloud.xcan.angus.core.tester.domain.scenario.trash.ScenarioTrash;
import java.util.List;

public interface ScenarioTrashCmd {

  void add0(List<ScenarioTrash> trashScenarios);

  void clear(Long id);

  void clearAll(Long projectId);

  void back(Long id);

  void backAll(Long projectId);
}




