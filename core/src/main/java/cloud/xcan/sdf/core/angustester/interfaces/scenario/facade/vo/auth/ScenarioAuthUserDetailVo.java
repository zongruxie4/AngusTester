package cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.auth;

import cloud.xcan.sdf.api.NameJoinField;

public class ScenarioAuthUserDetailVo extends ScenarioAuthDetailVo {

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
