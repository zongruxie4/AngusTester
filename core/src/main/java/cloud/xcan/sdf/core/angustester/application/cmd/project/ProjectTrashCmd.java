package cloud.xcan.sdf.core.angustester.application.cmd.project;

import cloud.xcan.sdf.core.angustester.domain.project.trash.ProjectTrash;
import java.util.List;

public interface ProjectTrashCmd {

  void add0(List<ProjectTrash> trashes);

  void clear(Long id);

  void clearAll();

  void back(Long id);

  void backAll();

}
