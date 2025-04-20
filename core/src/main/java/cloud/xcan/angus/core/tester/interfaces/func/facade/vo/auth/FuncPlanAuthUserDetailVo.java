package cloud.xcan.angus.core.tester.interfaces.func.facade.vo.auth;

import cloud.xcan.angus.remote.NameJoinField;

public class FuncPlanAuthUserDetailVo extends FuncPlanAuthDetailVo {

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
