package cloud.xcan.sdf.core.angustester.domain.services.schema;

import io.swagger.v3.oas.models.servers.Server;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ServiceServer {

  private Long serviceId;

  private Server server;

}
