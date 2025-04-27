package cloud.xcan.angus.core.tester.domain.kanban;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum OverdueRiskLevel implements EnumMessage<String> {

  /**
   * Workload overdue more than {@link OverdueRiskLevel#HIGH_OVERDUE_DAYS} days.
   */
  HIGH,
  /**
   * Workload overdue within {@link OverdueRiskLevel#HIGH_OVERDUE_DAYS} days.
   */
  LOW,
  /**
   * No overdue workload
   */
  NONE;

  public static final int HIGH_OVERDUE_DAYS = 3;

  @Override
  public String getValue() {
    return this.name();
  }

  public static OverdueRiskLevel calcLevel(double overdueDays){
    if (overdueDays <= 0){
      return NONE;
    }
    if (overdueDays < HIGH_OVERDUE_DAYS){
      return LOW;
    }
    return HIGH;
  }
}
