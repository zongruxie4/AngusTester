package cloud.xcan.angus.core.tester.interfaces.script.facade.vo.auth;

import cloud.xcan.angus.remote.NameJoinField;

public class ScriptAuthDeptDetailVo extends ScriptAuthDetailVo {

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
