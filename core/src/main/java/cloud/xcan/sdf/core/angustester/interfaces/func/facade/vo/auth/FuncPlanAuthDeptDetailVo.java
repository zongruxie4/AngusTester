package cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.auth;

import cloud.xcan.sdf.api.NameJoinField;

public class FuncPlanAuthDeptDetailVo extends FuncPlanAuthDetailVo {

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
