package cloud.xcan.angus.core.tester.interfaces.issue.facade.vo.sprint.auth;

import cloud.xcan.angus.remote.NameJoinField;

public class TaskSprintAuthGroupDetailVo extends TaskSprintAuthDetailVo {

  @NameJoinField(id = "authObjectId", repository = "commonGroupRepo")
  private String name;

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }
}
