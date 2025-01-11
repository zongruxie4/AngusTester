package cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.auth;

import cloud.xcan.sdf.api.NameJoinField;

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
