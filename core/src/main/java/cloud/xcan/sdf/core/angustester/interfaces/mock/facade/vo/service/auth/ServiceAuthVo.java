package cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.service.auth;

import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.core.angustester.domain.mock.service.auth.MockServicePermission;
import java.io.Serializable;
import java.util.List;

/**
 * @author xiaolong.liu
 */
public interface ServiceAuthVo extends Serializable {

  Long getId();

  void setId(Long id);

  AuthObjectType getAuthObjectType();

  void setAuthObjectType(AuthObjectType authObjectType);

  Long getAuthObjectId();

  void setAuthObjectId(Long authObjectId);

  String getName();

  void setName(String name);

  List<MockServicePermission> getPermissions();

  void setPermissions(List<MockServicePermission> permissions);

  Boolean getCreatorFlag();

  void setCreatorFlag(Boolean creatorFlag);
}
