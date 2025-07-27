package cloud.xcan.angus.core.tester.interfaces.func.facade.dto.plan;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_ATTACHMENT_NUM_X2;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_REMARK_LENGTH_X4;

import cloud.xcan.angus.api.commonlink.user.UserInfo;
import cloud.xcan.angus.api.enums.EvalWorkloadMethod;
import cloud.xcan.angus.api.pojo.Attachment;
import cloud.xcan.angus.spec.utils.JsonUtils;
import cloud.xcan.angus.validator.EditorContentLength;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class FuncPlanUpdateDto {

  @NotNull
  @Schema(description = "Functional test plan identifier for update operation", requiredMode = RequiredMode.REQUIRED)
  private Long id;

  //@Schema(description = "Project id", example = "1")
  //private Long projectId;

  @Schema(description = "Functional test plan name for identification and organization", example = "Example plan")
  @Length(max = MAX_NAME_LENGTH_X2)
  private String name;

  //@Schema(description = "Whether to enable authorization control, default disabled")
  //public Boolean auth;

  @Schema(description = "Test plan start date for timeline management", example = "2023-06-10 00:00:00")
  private LocalDateTime startDate;

  @Schema(description = "Test plan completion deadline for timeline management", example = "2029-06-20 00:00:00")
  private LocalDateTime deadlineDate;

  @Schema(description = "Test plan owner identifier for responsibility and authority assignment", requiredMode = RequiredMode.REQUIRED)
  private Long ownerId;

  @Schema(description = "Tester responsibility mapping for role definition and task assignment")
  private LinkedHashMap<Long, @Length(max = MAX_REMARK_LENGTH_X4) String> testerResponsibilities;

  @EditorContentLength
  @Schema(description = "Comprehensive testing scope definition for activity coverage")
  private String testingScope;

  @EditorContentLength
  @Schema(description = "Testing objectives specification for outcome focus and validation")
  private String testingObjectives;

  @EditorContentLength
  @Schema(description = "Acceptance criteria definition for delivery standards and conditions")
  private String acceptanceCriteria;

  @EditorContentLength
  @Schema(description = "Additional test plan information for strategy and risk management")
  private String otherInformation;

  @Schema(description = "Test plan supporting documents and reference materials")
  @Size(max = MAX_ATTACHMENT_NUM_X2)
  private List<Attachment> attachments;

  ///////////// Other setting /////////////

  //@Schema(description = "Use case prefix", example = "EXAMPLE_")
  //@Length(max = MAX_KEY_LENGTH)
  //private String casePrefix;

  @Schema(description = "Test case review workflow enablement flag", example = "true")
  private Boolean review;

  @Schema(description = "Workload evaluation methodology for effort estimation", example = "STORY_POINT")
  private EvalWorkloadMethod evalWorkloadMethod;

  public static void main(String[] args) {
    LinkedHashMap<UserInfo, @Length(max = MAX_REMARK_LENGTH_X4) String> testerResponsibilities = new LinkedHashMap<>();
    testerResponsibilities.put(new UserInfo().setId(1000001L), "1.用户登录与权限管理\n"
        + "2.数据管理与操作\n"
        + "3.报表生成与导出");

    System.out.println(JsonUtils.toJson(testerResponsibilities));
  }
}
