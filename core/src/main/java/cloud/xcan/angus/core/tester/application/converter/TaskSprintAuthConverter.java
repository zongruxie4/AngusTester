package cloud.xcan.angus.core.tester.application.converter;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprintPermission;
import cloud.xcan.angus.core.tester.domain.task.sprint.auth.TaskSprintAuth;
import cloud.xcan.angus.idgen.UidGenerator;
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
            .setCreator(true))
        .collect(Collectors.toList());
  }

}
