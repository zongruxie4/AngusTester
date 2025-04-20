package cloud.xcan.angus.core.tester.domain.mock.file;

import static cloud.xcan.angus.api.commonlink.TesterConstant.MAX_MOCK_FUNC_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X4;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

/**
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class MockExpression {

  @Length(max = MAX_NAME_LENGTH_X4)
  private String fieldName;

  @Length(max = MAX_MOCK_FUNC_LENGTH)
  private String mockFunc;

}
