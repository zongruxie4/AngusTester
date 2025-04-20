package cloud.xcan.angus.core.tester.domain.task.meeting;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum TaskMeetingType implements EnumMessage<String> {
  DAILY_STANDUP, PLANNING, REVIEW, RETROSPECTIVE, BACKLOG_REFINEMENT, OTHER;

  @Override
  public String getValue() {
    return this.name();
  }
}
