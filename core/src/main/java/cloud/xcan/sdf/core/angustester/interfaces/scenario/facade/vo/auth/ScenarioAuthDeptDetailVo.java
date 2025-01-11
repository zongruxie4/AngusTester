package cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.auth;

import cloud.xcan.sdf.api.NameJoinField;

public class ScenarioAuthDeptDetailVo extends ScenarioAuthDetailVo {

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
