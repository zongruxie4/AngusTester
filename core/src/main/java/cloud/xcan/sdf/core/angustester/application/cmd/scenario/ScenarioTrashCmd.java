package cloud.xcan.sdf.core.angustester.application.cmd.scenario;

import cloud.xcan.sdf.core.angustester.domain.scenario.trash.ScenarioTrash;
import java.util.List;

public interface ScenarioTrashCmd {

  void add0(List<ScenarioTrash> trashScenarios);

  void clear(Long id);

  void clearAll(Long projectId);

  void back(Long id);

  void backAll(Long projectId);
}




