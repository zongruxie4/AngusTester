package cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.auth;

import cloud.xcan.angus.remote.NameJoinField;

public class ApiAuthUserDetailVo extends ApisAuthDetailVo {

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
