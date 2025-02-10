package cloud.xcan.sdf.core.angustester.domain.mock.data;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author xiaolong.liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class MockFuncRequest {

  private String outKey;

  private String function;

  private int iterations;

}
