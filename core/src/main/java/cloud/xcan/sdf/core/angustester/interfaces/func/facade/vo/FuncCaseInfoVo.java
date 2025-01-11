package cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo;


import cloud.xcan.sdf.api.enums.Priority;
import cloud.xcan.sdf.api.enums.ReviewStatus;
import cloud.xcan.sdf.core.angustester.domain.func.cases.CaseTestResult;
import io.swagger.annotations.ApiModel;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Note: The current class field order is consistent with the export order and configuration header
 * message. Please do not modify the order arbitrarily.
 */
@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class FuncCaseInfoVo {

  private Long id;

  private String name;

  private String code;

  private Priority priority;

  private BigDecimal evalWorkload;

  public LocalDateTime deadlineDate;

  private Boolean overdueFlag;

  private String description;

  private Long testerId;

  private String testerName;

  private String testerAvatar;

  private Boolean unplannedFlag;

  private Boolean reviewFlag;

  private ReviewStatus reviewStatus;

  private String reviewRemark;

  private CaseTestResult testResult;

  private String testRemark;

}
