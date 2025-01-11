package cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.auth;

import cloud.xcan.sdf.api.NameJoinField;

public class ServicesAuthDeptDetailVo extends ServicesAuthDetailVo {

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
