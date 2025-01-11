package cloud.xcan.sdf.core.angustester.domain.task.meeting;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

@EndpointRegister
public enum TaskMeetingType implements EnumMessage<String> {
  DAILY_STANDUP, PLANNING, REVIEW, RETROSPECTIVE, BACKLOG_REFINEMENT, OTHER;

  @Override
  public String getValue() {
    return this.name();
  }
}
