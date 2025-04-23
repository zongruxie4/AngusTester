package cloud.xcan.angus.core.tester.interfaces.func.facade.vo;


import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.api.enums.ReviewStatus;
import cloud.xcan.angus.core.tester.domain.func.cases.CaseTestResult;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Note: The current class field order is consistent with the export order and configuration header
 * message. Please do not modify the order arbitrarily.
 */
@Valid
@Setter
@Getter
@Accessors(chain = true)
public class FuncCaseInfoVo {

  private Long id;

  private String name;

  private String code;

  private Integer version;

  private String softwareVersion;

  private Priority priority;

  private BigDecimal evalWorkload;

  public LocalDateTime deadlineDate;

  private Boolean overdue;

  private String description;

  private Long testerId;

  private String testerName;

  private String testerAvatar;

  private Boolean unplanned;

  private Boolean review;

  private ReviewStatus reviewStatus;

  private String reviewRemark;

  private CaseTestResult testResult;

  private String testRemark;

}
