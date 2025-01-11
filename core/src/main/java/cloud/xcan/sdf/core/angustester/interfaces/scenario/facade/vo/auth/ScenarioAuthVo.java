package cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.auth;


import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.spec.experimental.Value;
import java.io.Serializable;
import java.util.List;

public interface ScenarioAuthVo extends Serializable {

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

  Boolean getCreatorFlag();

  void setCreatorFlag(Boolean creatorFlag);

  Long getScenarioId();

  void setScenarioId(Long scenarioId);
}



