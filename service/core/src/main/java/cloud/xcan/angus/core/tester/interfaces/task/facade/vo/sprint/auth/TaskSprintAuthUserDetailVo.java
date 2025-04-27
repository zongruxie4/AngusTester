package cloud.xcan.angus.core.tester.interfaces.task.facade.vo.sprint.auth;

import cloud.xcan.angus.remote.NameJoinField;

public class TaskSprintAuthUserDetailVo extends TaskSprintAuthDetailVo {

  @NameJoinField(id = "authObjectId", repository = "commonUserBaseRepo")
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
