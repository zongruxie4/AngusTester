package cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.sprint.auth;

import cloud.xcan.sdf.api.NameJoinField;

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
