package cloud.xcan.angus.core.tester.interfaces.report.facade.vo.auth;

import cloud.xcan.angus.remote.NameJoinField;

public class ReportAuthGroupDetailVo extends ReportAuthDetailVo {

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
