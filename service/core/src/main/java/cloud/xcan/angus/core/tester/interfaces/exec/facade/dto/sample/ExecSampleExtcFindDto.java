package cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.sample;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_PARAM_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_PARAM_TYPE_LENGTH;

import cloud.xcan.angus.remote.OrderSort;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
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
  @Schema(description = "Sample name for fuzzy search and filtering with 'in' operation support")
  private String name;

  /**
   * Note: Support `in` operation.
   */
  @Schema(description = "Node identifier for sample filtering with 'in' operation support")
  private Long nodeId;

  @Length(max = MAX_PARAM_TYPE_LENGTH)
  @Schema(description = "Extended field for additional sample data filtering")
  private String extField;

  @Schema(description = "Sample timestamp for temporal filtering")
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
