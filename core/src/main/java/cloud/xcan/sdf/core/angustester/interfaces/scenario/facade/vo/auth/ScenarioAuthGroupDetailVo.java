package cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.auth;

import cloud.xcan.sdf.api.NameJoinField;

public class ScenarioAuthGroupDetailVo extends ScenarioAuthDetailVo {

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
