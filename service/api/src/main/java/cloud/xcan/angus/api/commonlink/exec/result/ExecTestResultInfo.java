package cloud.xcan.angus.api.commonlink.exec.result;

import cloud.xcan.angus.api.commonlink.exec.ExecStatus;
import cloud.xcan.angus.model.script.ScriptSource;
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
@Table(name = "exec_test_result")
@Setter
@Getter
@Accessors(chain = true)
public class ExecTestResultInfo {

  @Id
  @Column(name = "id")
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

  @Enumerated(EnumType.STRING)
  @Column(name = "script_source")
  private ScriptSource scriptSource;

  @Column(name = "script_source_id")
  private Long scriptSourceId;

  //@Column(name = "service_id") -> Changes will occur after movement
  //private Long serviceId;

  private boolean passed;

  @Column(name = "failure_message")
  private String failureMessage;

  @Column(name = "test_num")
  private int testNum;

  @Column(name = "test_failure_num")
  private int testFailureNum;

  @Column(name = "usage_failed_node_id")
  private Long usageFailedNodeId;

  @Column(name = "exec_by")
  private Long execBy;

  @Column(name = "last_exec_date")
  private LocalDateTime lastExecDate;

  @Column(name = "created_date")
  private LocalDateTime createdDate;

}
