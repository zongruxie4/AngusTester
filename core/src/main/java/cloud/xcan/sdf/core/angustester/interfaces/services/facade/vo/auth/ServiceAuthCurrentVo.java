package cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.auth;

import cloud.xcan.sdf.api.commonlink.services.ServicesPermission;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ServiceAuthCurrentVo {

  private boolean serviceAuthFlag;

  private Set<ServicesPermission> permissions;
}
