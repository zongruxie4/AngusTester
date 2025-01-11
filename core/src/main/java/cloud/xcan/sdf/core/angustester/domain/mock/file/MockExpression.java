package cloud.xcan.sdf.core.angustester.domain.mock.file;

import static cloud.xcan.sdf.api.commonlink.TesterConstant.MAX_MOCK_FUNC_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X4;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

/**
 * @author xiaolong.liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class MockExpression {

  @Length(max = DEFAULT_NAME_LENGTH_X4)
  private String fieldName;

  @Length(max = MAX_MOCK_FUNC_LENGTH)
  private String mockFunc;

}
