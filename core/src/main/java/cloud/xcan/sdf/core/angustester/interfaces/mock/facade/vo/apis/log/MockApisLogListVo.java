package cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.apis.log;

import io.swagger.annotations.ApiModel;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author xiaolong.liu
 */
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class MockApisLogListVo {

  private Long id;

  private String requestId;

  private String remote;

  private Long mockServiceId;

  private Long mockApisId;

  private String summary;

  private String method;

  private String endpoint;

  private Boolean pushbackFlag;

  private LocalDateTime requestDate;

  private int responseStatus;

  private LocalDateTime createdDate;

}
