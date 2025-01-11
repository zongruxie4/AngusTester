package cloud.xcan.sdf.core.angustester.application.cmd.project;

import cloud.xcan.sdf.core.angustester.domain.project.Project;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.List;

public interface ProjectCmd {

  IdKey<Long, Object> add(Project project);

  void update(Project project);

  IdKey<Long, Object> replace(Project project);

  void delete(Long id);

  void delete0(List<Long> ids);

}




