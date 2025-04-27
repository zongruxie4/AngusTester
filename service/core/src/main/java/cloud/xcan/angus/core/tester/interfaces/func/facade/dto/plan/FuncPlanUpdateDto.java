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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Valid
@Setter
@Getter
@Accessors(chain = true)
public class FuncPlanUpdateDto {

  @NotNull
  @Schema(description = "Plan id", requiredMode = RequiredMode.REQUIRED)
  private Long id;

  //@Schema(description = "Project id", example = "1")
  //private Long projectId;

  @Schema(description = "Plan name, Brief Overview of the Plan, supporting up to 200 characters.", example = "Example plan")
  @Length(max = MAX_NAME_LENGTH_X2)
  private String name;

  //@Schema(description = "Whether to enable authorization control, default disabled")
  //public Boolean auth;

  @Schema(description = "Plan start date, Determine the start times of the testing activities to ensure completion within the project cycle.", example = "2023-06-10 00:00:00")
  private LocalDateTime startDate;

  @Schema(description = "Plan deadline date, Determine the end times of the testing activities to ensure completion within the project cycle.", example = "2029-06-20 00:00:00")
  private LocalDateTime deadlineDate;

  @Schema(description =
      "Owner id. By assigning a responsible person to the plan, clarify the accountability and authority for testing, "
          + "facilitating task completion and progress control. Responsibilities include problem resolution, progress promotion, team collaboration, risk identification, and management.", requiredMode = RequiredMode.REQUIRED)
  private Long ownerId;

  @Schema(description =
      "Specify the testers involved in this test plan; only authorized testers are allowed to participate. "
          + "Define the roles of testers, outlining their responsibilities within the testing scope to avoid ambiguity and task omission.")
  private LinkedHashMap<Long, @Length(max = MAX_REMARK_LENGTH_X4) String> testerResponsibilities;

  @EditorContentLength
  @Schema(description =
      "Testing scope for testing plan. Define the specific content and extent covered by the testing activities, "
          + "including which functional modules, platforms, versions, etc. ")
  private String testingScope;

  @EditorContentLength
  @Schema(description =
      "Testing objectives for testing plan. Specify the specific purposes and expected outcomes of the testing activities, "
          + "helping the testing team focus on core testing objectives.")
  private String testingObjectives;

  @EditorContentLength
  @Schema(description = "Acceptance criteria for testing plan. Clearly define the specific conditions and standards for software product delivery. ")
  private String acceptanceCriteria;

  @EditorContentLength
  @Schema(description =
      "Other plan information. This is the other description of the testing plan. Additional details such as testing strategies, "
          + "risk assessment, and management. ")
  private String otherInformation;

  @Schema(description =
      "Plan attachments. Additional documents and information, such as requirement specifications, reference materials and standards, "
          + "system architecture diagrams, testing specifications, technical documents, etc.")
  @Size(max = MAX_ATTACHMENT_NUM_X2)
  private List<Attachment> attachments;

  ///////////// Other setting /////////////

  //@Schema(description = "Use case prefix", example = "EXAMPLE_")
  //@Length(max = MAX_KEY_LENGTH)
  //private String casePrefix;

  @Schema(description = "Enable use case review flag", example = "true")
  private Boolean review;

  @Schema(description = "Workload evaluation method", example = "STORY_POINT")
  private EvalWorkloadMethod evalWorkloadMethod;

  public static void main(String[] args) {
    LinkedHashMap<UserInfo, @Length(max = MAX_REMARK_LENGTH_X4) String> testerResponsibilities = new LinkedHashMap<>();
    testerResponsibilities.put(new UserInfo().setId(1000001L), "1.用户登录与权限管理\n"
        + "2.数据管理与操作\n"
        + "3.报表生成与导出");

    System.out.println(JsonUtils.toJson(testerResponsibilities));
  }
}
