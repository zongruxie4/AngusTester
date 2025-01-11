package cloud.xcan.sdf.core.angustester.domain.func.summary;


import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DEFAULT_DATE_TIME_FORMAT;

import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.api.commonlink.user.UserInfo;
import cloud.xcan.sdf.api.enums.EvalWorkloadMethod;
import cloud.xcan.sdf.api.pojo.Attachment;
import cloud.xcan.sdf.api.pojo.Progress;
import cloud.xcan.sdf.core.angustester.domain.func.plan.FuncPlanStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import javax.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class FuncPlanSummary {

  private Long id;

  private Long projectId;

  @NameJoinField(id = "projectId", repository = "projectRepo")
  private String projectName;

  private String name;

  private FuncPlanStatus status;

  @JsonFormat(pattern = DEFAULT_DATE_TIME_FORMAT)
  private LocalDateTime startDate;

  @JsonFormat(pattern = DEFAULT_DATE_TIME_FORMAT)
  private LocalDateTime deadlineDate;

  private Long ownerId;

  private String ownerName;

  private String ownerAvatar;

  private LinkedHashMap<Long, String> testerResponsibilities;

  private List<UserInfo> members;

  private String testingScope;

  private String testingObjectives;

  private String acceptanceCriteria;

  private String otherInformation;

  private List<Attachment> attachments;

  private String casePrefix;

  private Boolean reviewFlag;

  private EvalWorkloadMethod evalWorkloadMethod;

  private Boolean authFlag;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  @JsonFormat(pattern = DEFAULT_DATE_TIME_FORMAT)
  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  @JsonFormat(pattern = DEFAULT_DATE_TIME_FORMAT)
  private LocalDateTime lastModifiedDate;

  private long caseNum;

  private long validCaseNum;

  private Progress progress;

}
