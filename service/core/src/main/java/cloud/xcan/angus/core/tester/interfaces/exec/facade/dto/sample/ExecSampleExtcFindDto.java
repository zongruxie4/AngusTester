package cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.sample;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_PARAM_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_PARAM_TYPE_LENGTH;

import cloud.xcan.angus.remote.OrderSort;
import cloud.xcan.angus.remote.PageQuery;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
@Accessors(chain = true)
public class ExecSampleExtcFindDto extends PageQuery {

  /**
   * Note: Support `in` operation.
   */
  @Length(max = MAX_PARAM_NAME_LENGTH)
  private String name;

  /**
   * Note: Support `in` operation.
   */
  private Long nodeId;

  @Length(max = MAX_PARAM_TYPE_LENGTH)
  private String extField;

  private Long timestamp;

  @Override
  public String getDefaultOrderBy() {
    return "timestamp";
  }

  @Override
  public OrderSort getDefaultOrderSort() {
    return OrderSort.ASC;
  }
}
