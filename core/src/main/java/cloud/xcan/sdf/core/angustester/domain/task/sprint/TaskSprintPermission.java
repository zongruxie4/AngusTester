package cloud.xcan.sdf.core.angustester.domain.task.sprint;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;
import java.util.List;

/**
 * @author xiaolong.liu
 */
@EndpointRegister
public enum TaskSprintPermission implements EnumMessage<String> {
  MODIFY_SPRINT, DELETE_SPRINT, ADD_TASK, MODIFY_TASK, DELETE_TASK, EXPORT_TASK,
  RESTART_TASK, REOPEN_TASK, GRANT;

  public static List<TaskSprintPermission> ALL = List.of(
      MODIFY_SPRINT, DELETE_SPRINT, ADD_TASK, MODIFY_TASK, DELETE_TASK, EXPORT_TASK,
      RESTART_TASK, REOPEN_TASK, GRANT
  );

  @Override
  public String getValue() {
    return this.name();
  }

  public boolean notIgnorePublicAccess() {
    return this.equals(RESTART_TASK) || this.equals(GRANT)
        /* this.equals(REOPEN_TASK) <- Fix: Allow assignee reopen */;
  }

  public boolean isGrant() {
    return this.equals(GRANT);
  }
}
