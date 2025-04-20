package cloud.xcan.angus.core.tester.application.cmd.project;

import cloud.xcan.angus.core.tester.domain.project.trash.ProjectTrash;
import java.util.List;

public interface ProjectTrashCmd {

  void add0(List<ProjectTrash> trashes);

  void clear(Long id);

  void clearAll();

  void back(Long id);

  void backAll();

}
