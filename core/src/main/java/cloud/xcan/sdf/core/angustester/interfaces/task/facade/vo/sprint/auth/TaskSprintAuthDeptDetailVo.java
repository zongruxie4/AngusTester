package cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.sprint.auth;

import cloud.xcan.sdf.api.NameJoinField;

public class TaskSprintAuthDeptDetailVo extends TaskSprintAuthDetailVo {

  @NameJoinField(id = "authObjectId", repository = "commonDeptRepo")
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
