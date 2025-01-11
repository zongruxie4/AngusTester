package cloud.xcan.sdf.core.angustester.application.converter;

import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.core.angustester.domain.task.sprint.TaskSprintPermission;
import cloud.xcan.sdf.core.angustester.domain.task.sprint.auth.TaskSprintAuth;
import cloud.xcan.sdf.idgen.UidGenerator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class TaskSprintAuthConverter {

  @NotNull
  public static List<TaskSprintAuth> toTaskSprintAuths(Set<Long> creatorIds, Long sprintId
      , UidGenerator uidGenerator) {
    return creatorIds.stream()
        .map(creatorId -> new TaskSprintAuth().setId(uidGenerator.getUID())
            .setSprintId(sprintId)
            .setAuthObjectType(AuthObjectType.USER)
            .setAuthObjectId(creatorId)
            .setAuths(TaskSprintPermission.ALL)
            .setCreatorFlag(true))
        .collect(Collectors.toList());
  }

}
