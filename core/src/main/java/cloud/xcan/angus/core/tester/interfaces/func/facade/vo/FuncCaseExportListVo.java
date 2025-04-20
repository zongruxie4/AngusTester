package cloud.xcan.angus.core.tester.interfaces.func.facade.vo;


import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DEFAULT_DATE_TIME_FORMAT;

import cloud.xcan.angus.api.enums.EvalWorkloadMethod;
import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.api.enums.ReviewStatus;
import cloud.xcan.angus.core.export.BooeanWriteConverter;
import cloud.xcan.angus.core.export.EnumMessageWriteConverter;
import cloud.xcan.angus.core.export.ListStringWriteConverter;
import cloud.xcan.angus.core.tester.domain.func.cases.CaseTestResult;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
public class FuncCaseExportListVo {

  @ColumnWidth(50)
  private String name;

  private String code;

  private String planName;

  private String moduleName;

  @ExcelProperty(converter = EnumMessageWriteConverter.class)
  private Priority priority;

  @ExcelProperty(converter = BooeanWriteConverter.class)
  private Boolean overdue;

  @DateTimeFormat(DEFAULT_DATE_TIME_FORMAT)
  public LocalDateTime deadlineDate;

  @ExcelProperty(converter = EnumMessageWriteConverter.class)
  private EvalWorkloadMethod evalWorkloadMethod;

  private BigDecimal evalWorkload;

  private BigDecimal actualWorkload;

  @ColumnWidth(50)
  private String precondition;

  @ColumnWidth(50)
  private String steps;

  @ColumnWidth(50)
  private String description;

  private String reviewerName;

  private LocalDateTime reviewDate;

  @ExcelProperty(converter = EnumMessageWriteConverter.class)
  private ReviewStatus reviewStatus;

  private String reviewRemark;

  private Integer reviewNum;

  private Integer reviewFailNum;

  private String testerName;

  private String developerName;

  private Boolean unplanned;

  private Integer testNum;

  private Integer testFailNum;

  @ExcelProperty(converter = EnumMessageWriteConverter.class)
  private CaseTestResult testResult;

  private String testRemark;

  @DateTimeFormat(DEFAULT_DATE_TIME_FORMAT)
  private LocalDateTime testResultHandleDate;

  @ExcelProperty(converter = ListStringWriteConverter.class)
  private List<String> refTags;

  @ExcelProperty(converter = ListStringWriteConverter.class)
  private List<String> refCases;

  @ExcelProperty(converter = ListStringWriteConverter.class)
  private List<String> refTasks;

  private String createdByName;
  @DateTimeFormat(DEFAULT_DATE_TIME_FORMAT)
  private LocalDateTime createdDate;

  private String lastModifiedByName;
  @DateTimeFormat(DEFAULT_DATE_TIME_FORMAT)
  private LocalDateTime lastModifiedDate;

}
