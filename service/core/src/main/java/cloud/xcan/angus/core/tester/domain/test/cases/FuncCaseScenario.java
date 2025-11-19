package cloud.xcan.angus.core.tester.domain.test.cases;

import cloud.xcan.angus.spec.experimental.EntitySupport;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "func_case_scenario")
@Setter
@Getter
@Accessors(chain = true)
public class FuncCaseScenario extends EntitySupport<FuncCaseScenario, Long> {

  @Id
  private Long id;

  @Column(name = "case_id")
  private Long caseId;

  @Column(name = "scenario_id")
  private Long scenarioId;

  @Override
  public Long identity() {
    return id;
  }
}
