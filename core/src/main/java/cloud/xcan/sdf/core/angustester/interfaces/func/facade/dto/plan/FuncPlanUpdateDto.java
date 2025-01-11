package cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.plan;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X2;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_REMARK_LENGTH_X10;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_REMARK_LENGTH_X4;
import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_ATTACHMENT_NUM_X2;

import cloud.xcan.sdf.api.commonlink.user.UserInfo;
import cloud.xcan.sdf.api.enums.EvalWorkloadMethod;
import cloud.xcan.sdf.api.pojo.Attachment;
import cloud.xcan.sdf.spec.utils.JsonUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class FuncPlanUpdateDto {

  @NotNull
  @ApiModelProperty(value = "Plan id", example = "1", required = true)
  private Long id;

  //@ApiModelProperty(value = "Project id", example = "1")
  //private Long projectId;

  @ApiModelProperty(value = "Plan name, Brief Overview of the Plan, supporting up to 200 characters.", example = "Example plan")
  @Length(max = DEFAULT_NAME_LENGTH_X2)
  private String name;

  //@ApiModelProperty(value = "Whether to enable authorization control, default disabled")
  //public Boolean authFlag;

  @ApiModelProperty(value = "Plan start date, Determine the start times of the testing activities to ensure completion within the project cycle.", example = "2023-06-10 00:00:00")
  private LocalDateTime startDate;

  @ApiModelProperty(value = "Plan deadline date, Determine the end times of the testing activities to ensure completion within the project cycle.", example = "2029-06-20 00:00:00")
  private LocalDateTime deadlineDate;

  @ApiModelProperty(value =
      "Owner id. By assigning a responsible person to the plan, clarify the accountability and authority for testing, "
          + "facilitating task completion and progress control. Responsibilities include problem resolution, progress promotion, team collaboration, risk identification, and management.", example = "1", required = true)
  private Long ownerId;

  @ApiModelProperty(value =
      "Specify the testers involved in this test plan; only authorized testers are allowed to participate. "
          + "Define the roles of testers, outlining their responsibilities within the testing scope to avoid ambiguity and task omission.")
  private LinkedHashMap<Long, @Length(max = DEFAULT_REMARK_LENGTH_X4) String> testerResponsibilities;

  @Length(max = DEFAULT_REMARK_LENGTH_X10)
  @ApiModelProperty(value =
      "Testing scope for testing plan. Define the specific content and extent covered by the testing activities, "
          + "including which functional modules, platforms, versions, etc. ")
  private String testingScope;

  @ApiModelProperty(value =
      "Testing objectives for testing plan. Specify the specific purposes and expected outcomes of the testing activities, "
          + "helping the testing team focus on core testing objectives.")
  private String testingObjectives;

  @Length(max = DEFAULT_REMARK_LENGTH_X10)
  @ApiModelProperty(value = "Acceptance criteria for testing plan. Clearly define the specific conditions and standards for software product delivery. ")
  private String acceptanceCriteria;

  @Length(max = DEFAULT_REMARK_LENGTH_X10)
  @ApiModelProperty(value =
      "Other plan information. This is the other description of the testing plan. Additional details such as testing strategies, "
          + "risk assessment, and management. ")
  private String otherInformation;

  @ApiModelProperty(value =
      "Plan attachments. Additional documents and information, such as requirement specifications, reference materials and standards, "
          + "system architecture diagrams, testing specifications, technical documents, etc.")
  @Size(max = MAX_ATTACHMENT_NUM_X2)
  private List<Attachment> attachments;

  ///////////// Other setting /////////////

  //@ApiModelProperty(value = "Use case prefix", example = "EXAMPLE_")
  //@Length(max = DEFAULT_KEY_LENGTH)
  //private String casePrefix;

  @ApiModelProperty(value = "Enable use case review flag", example = "true")
  private Boolean reviewFlag;

  @ApiModelProperty(value = "Workload evaluation method", example = "STORY_POINT")
  private EvalWorkloadMethod evalWorkloadMethod;

  public static void main(String[] args) {
    LinkedHashMap<UserInfo, @Length(max = DEFAULT_REMARK_LENGTH_X4) String> testerResponsibilities = new LinkedHashMap<>();
    testerResponsibilities.put(new UserInfo().setId(1000001L), "1.用户登录与权限管理\n"
        + "2.数据管理与操作\n"
        + "3.报表生成与导出");

    System.out.println(JsonUtils.toJson(testerResponsibilities));
  }
}
