package cloud.xcan.angus.core.tester.interfaces.services.facade.vo.auth;


import cloud.xcan.angus.api.commonlink.services.ServicesPermission;
import cloud.xcan.angus.api.enums.AuthObjectType;
import java.io.Serializable;
import java.util.List;

public interface ServicesAuthVo extends Serializable {

  Long getId();

  void setId(Long id);

  AuthObjectType getAuthObjectType();

  void setAuthObjectType(AuthObjectType authObjectType);

  Long getAuthObjectId();

  void setAuthObjectId(Long authObjectId);

  String getName();

  void setName(String name);

  List<ServicesPermission> getPermissions();

  void setPermissions(List<ServicesPermission> permissions);

  Long getServiceId();

  void setServiceId(Long serviceId);

  Boolean getCreator();

  void setCreator(Boolean creator);
}



