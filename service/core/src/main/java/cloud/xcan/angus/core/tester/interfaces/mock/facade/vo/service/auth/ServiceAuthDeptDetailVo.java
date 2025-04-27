package cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.service.auth;

import cloud.xcan.angus.remote.NameJoinField;

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
