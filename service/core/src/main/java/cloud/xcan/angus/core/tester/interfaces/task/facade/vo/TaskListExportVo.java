package cloud.xcan.angus.core.tester.interfaces.task.facade.vo;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DEFAULT_DATE_TIME_FORMAT;

import cloud.xcan.angus.api.enums.EvalWorkloadMethod;
import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.api.enums.Result;
import cloud.xcan.angus.core.export.BooeanWriteConverter;
import cloud.xcan.angus.core.export.EnumMessageWriteConverter;
import cloud.xcan.angus.core.export.ListStringWriteConverter;
import cloud.xcan.angus.core.tester.domain.task.BugLevel;
import cloud.xcan.angus.core.tester.domain.task.TaskStatus;
import cloud.xcan.angus.core.tester.domain.task.TaskType;
import cloud.xcan.angus.model.script.TestType;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.converters.longconverter.LongStringConverter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Note: The current class field order is consistent with the export order and configuration header
 * message. Please do not modify the order arbitrarily.
 *
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class TaskListExportVo {

  @ColumnWidth(50)
  private String name;

  private String code;

  @ColumnWidth(38)
  //@ExcelIgnore
  //@ExcelProperty(converter = LongStringConverter.class)
  //private Long projectId;
  //@NameJoinField(id = "projectId", repository = "projectRepo")
  private String projectName;

  //@ExcelIgnore
  //private Long sprintId;
  @ColumnWidth(38)
  //@NameJoinField(id = "sprintId", repository = "taskSprintRepo")
  private String sprintName;

  @ColumnWidth(28)
  private String moduleName;

  @ExcelProperty(converter = EnumMessageWriteConverter.class)
  private TaskType taskType;

  @ExcelProperty(converter = EnumMessageWriteConverter.class)
  private BugLevel bugLevel;

  @ExcelProperty(converter = EnumMessageWriteConverter.class)
  private TestType testType;

  //@ExcelIgnore
  //private Long assigneeId;
  //@NameJoinField(id = "assigneeId", repository = "commonUserBaseRepo")
  private String assigneeName;

  //@ExcelIgnore
  //private Long confirmorId;
  //@NameJoinField(id = "confirmorId", repository = "commonUserBaseRepo")
  private String confirmorName;

  //@ExcelIgnore
  //private Long testerId;
  //@NameJoinField(id = "testerId", repository = "commonUserBaseRepo")
  private String testerName;

  @ExcelProperty(converter = BooeanWriteConverter.class)
  private Boolean missingBug;

  @ExcelProperty(converter = BooeanWriteConverter.class)
  private Boolean unplanned;

  @ExcelProperty(converter = EnumMessageWriteConverter.class)
  private TaskStatus status;

  private String softwareVersion;

  @ExcelProperty(converter = EnumMessageWriteConverter.class)
  private Priority priority;

  @ExcelProperty(converter = BooeanWriteConverter.class)
  private Boolean overdue;

  @DateTimeFormat(DEFAULT_DATE_TIME_FORMAT)
  private LocalDateTime deadlineDate;

  @ColumnWidth(80)
  private String description;

  @ExcelProperty(converter = EnumMessageWriteConverter.class)
  private EvalWorkloadMethod evalWorkloadMethod;

  private BigDecimal evalWorkload;

  private BigDecimal actualWorkload;

  @DateTimeFormat(DEFAULT_DATE_TIME_FORMAT)
  private LocalDateTime startDate;

  @DateTimeFormat(DEFAULT_DATE_TIME_FORMAT)
  public LocalDateTime processedDate;

  @DateTimeFormat(DEFAULT_DATE_TIME_FORMAT)
  private LocalDateTime canceledDate;

  @DateTimeFormat(DEFAULT_DATE_TIME_FORMAT)
  public LocalDateTime confirmedDate;

  @DateTimeFormat(DEFAULT_DATE_TIME_FORMAT)
  private LocalDateTime completedDate;

  @ExcelProperty(converter = ListStringWriteConverter.class)
  private List<String> refTags;

  @ExcelProperty(converter = ListStringWriteConverter.class)
  private List<String> refTasks;

  @ExcelProperty(converter = ListStringWriteConverter.class)
  private List<String> refCases;

  @ExcelProperty(converter = LongStringConverter.class)
  private Long targetId;

  private String targetName;

  //  @ExcelProperty(converter = LongStringConverter.class)
  //  private Long targetParentId;
  //  private String targetParentName;

  @Schema(description="The total number of tasks processed")
  private Integer totalNum;
  @Schema(description="The number of task processing failures")
  private Integer failNum;

  @ExcelProperty(converter = EnumMessageWriteConverter.class)
  private Result execResult;
  private String execFailureMessage;
  private Integer execTestNum;
  private Integer execTestFailureNum;

  //@ExcelIgnore
  //private Long execId;
  private String execName;
  //@ExcelIgnore
  //private Long execBy;
  //@NameJoinField(id = "execBy", repository = "commonUserBaseRepo")
  private String execByName;
  @DateTimeFormat(DEFAULT_DATE_TIME_FORMAT)
  private LocalDateTime execDate;

  //@ExcelIgnore
  //private Long createdBy;
  //@NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;
  @DateTimeFormat(DEFAULT_DATE_TIME_FORMAT)
  private LocalDateTime createdDate;

  //@ExcelIgnore
  //private Long lastModifiedBy;
  //@NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;
  @DateTimeFormat(DEFAULT_DATE_TIME_FORMAT)
  private LocalDateTime lastModifiedDate;

}
