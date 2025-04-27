package cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.apis.log;


import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author XiaoLong Liu
 */
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

  private Boolean pushback;

  private LocalDateTime requestDate;

  private int responseStatus;

  private LocalDateTime createdDate;

}
