package cloud.xcan.angus.core.tester.domain.mock.data;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class MockTextRequest {

  private String outKey;

  private String text;

  private int iterations;
}
