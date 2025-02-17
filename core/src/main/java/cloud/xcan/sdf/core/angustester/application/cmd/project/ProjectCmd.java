package cloud.xcan.sdf.core.angustester.application.cmd.project;

import cloud.xcan.sdf.core.angustester.domain.ExampleDataType;
import cloud.xcan.sdf.core.angustester.domain.project.Project;
import cloud.xcan.sdf.core.angustester.domain.project.ProjectType;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

public interface ProjectCmd {

  IdKey<Long, Object> add(Project project);

  IdKey<Long, Object> add0(Project project);

  void update(Project project);

  IdKey<Long, Object> replace(Project project);

  IdKey<Long, Object> importExample(String name, ProjectType type, Set<ExampleDataType> dataTypes);

  IdKey<Long, Object> importProjectExample(@Nullable String name, ProjectType type,
      Project project);

  void delete(Long id);

  void delete0(List<Long> ids);

}
