package cloud.xcan.angus.api.commonlink.exec.result;

import cloud.xcan.angus.api.commonlink.exec.ExecStatus;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.spec.annotations.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "exec_test_case_result")
@Setter
@Getter
@Accessors(chain = true)
public class ExecTestCaseResultInfo {

  @Id
  private Long id;

  @Column(name = "exec_id")
  private Long execId;

  @Enumerated(EnumType.STRING)
  @Column(name = "exec_status")
  private ExecStatus execStatus;

  @Enumerated(EnumType.STRING)
  @Column(name = "script_type")
  private ScriptType scriptType;

  @Nullable
  @Column(name = "script_id")
  private Long scriptId;

  @Column(name = "scenario_id")
  private Long scenarioId;

  @Column(name = "apis_id")
  private Long apisId;

  @Column(name = "case_id")
  private Long caseId;

  //@Column(name = "service_id") -> Changes will occur after movement
  //private Long serviceId;

  private Boolean smoke;

  private Boolean security;

  private Boolean passed;

  @Column(name = "failure_message")
  private String failureMessage;

  @Column(name = "test_num")
  private int testNum;

  @Column(name = "test_failure_num")
  private int testFailureNum;

  @Column(name = "exec_by")
  private Long execBy;

  @Column(name = "last_exec_date")
  private LocalDateTime lastExecDate;

  @Column(name = "created_date")
  private LocalDateTime createdDate;

}
