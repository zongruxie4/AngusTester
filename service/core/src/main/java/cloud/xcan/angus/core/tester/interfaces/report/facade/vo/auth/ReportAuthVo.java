package cloud.xcan.angus.core.tester.interfaces.report.facade.vo.auth;


import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.spec.experimental.Value;
import java.io.Serializable;
import java.util.List;

public interface ReportAuthVo extends Serializable {

  Long getId();

  void setId(Long id);

  AuthObjectType getAuthObjectType();

  void setAuthObjectType(AuthObjectType authObjectType);

  Long getAuthObjectId();

  void setAuthObjectId(Long authObjectId);

  String getName();

  void setName(String name);

  List<? extends Value<?>> getPermissions();

  void setPermissions(List<? extends Value<?>> permissions);

  Boolean getCreator();

  void setCreator(Boolean creator);

  Long getReportId();

  void setReportId(Long reportId);

}



