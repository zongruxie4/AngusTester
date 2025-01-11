package cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.service.auth;

import cloud.xcan.sdf.api.NameJoinField;

public class ServiceAuthDeptDetailVo extends ServiceAuthDetailVo {

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
