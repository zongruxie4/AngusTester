package cloud.xcan.angus.model.apis;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ApisInfo {

  private Long id;

  private String summary;

  private String method;

  private String url;

  //private String description;

  private String operationId;

  //private Long ownerId;

  //@NameJoinField(id = "ownerId", repository = "commonUserBaseRepo")
  //private String ownerName;

  private ApiStatus status;

  /**
   * Whether to enable functional testing, default enabled.
   * <p>
   * After enabled, the test results will be included in the efficiency analysis
   */
  private Boolean testFunc;
  /**
   * Whether to enable performance testing, default enabled.
   * <p>
   * After enabled, the test results will be included in the efficiency analysis.
   */
  private Boolean testPerf;
  /**
   * Whether to enable stability testing, default enabled.
   * <p>
   * After enabled, the test results will be included in the efficiency analysis
   */
  private Boolean testStability;

}
