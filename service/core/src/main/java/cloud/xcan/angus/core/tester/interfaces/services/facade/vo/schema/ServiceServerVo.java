package cloud.xcan.angus.core.tester.interfaces.services.facade.vo.schema;

import cloud.xcan.angus.remote.NameJoinField;
import io.swagger.v3.oas.models.servers.Server;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ServiceServerVo {

  private Long serviceId;

  @NameJoinField(id = "serviceId", repository = "servicesRepo")
  private String serviceName;

  private Server server;

}
