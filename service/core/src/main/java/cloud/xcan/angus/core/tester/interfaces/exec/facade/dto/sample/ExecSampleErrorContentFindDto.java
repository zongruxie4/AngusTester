package cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.sample;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_PARAM_NAME_LENGTH;

import cloud.xcan.angus.remote.OrderSort;
import cloud.xcan.angus.remote.PageQuery;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
@Accessors(chain = true)
public class ExecSampleErrorContentFindDto extends PageQuery {

  /**
   * Note: Support `in` operation.
   */
  @Length(max = MAX_PARAM_NAME_LENGTH)
  private String name;

  /**
   * Note: Support `in` operation.
   */
  private Long nodeId;

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
