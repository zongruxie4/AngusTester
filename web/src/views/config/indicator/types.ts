/**
 * Performance indicator configuration
 * @interface PerformanceIndicator
 */
export interface PerformanceIndicator {
  /**
   * Average response time threshold
   */
  art: string;

  /**
   * Concurrent users count
   */
  threads: string;

  /**
   * Error rate threshold (percentage)
   */
  errorRate: string;

  /**
   * Transactions per second threshold
   */
  tps: string;

  /**
   * Percentile configuration
   */
  percentile: {
    /**
     * Percentile value
     */
    value: string;

    /**
     * Percentile display message
     */
    message: string;
  } | string;

  /**
   * Percentile display name
   */
  percentileName?: string;

  /**
   * Test duration
   */
  duration: string;

  /**
   * Ramp up threads count
   */
  rampUpThreads: number | undefined;

  /**
   * Ramp up interval duration
   */
  rampUpInterval: string;
}

/**
 * Stability indicator configuration
 * @interface StabilityIndicator
 */
export interface StabilityIndicator {
  /**
   * CPU usage threshold (percentage)
   */
  cpu: string;

  /**
   * Concurrent users count
   */
  threads: string;

  /**
   * Disk usage threshold (percentage)
   */
  disk: string;

  /**
   * Test duration
   */
  duration: string;

  /**
   * Error rate threshold (percentage)
   */
  errorRate: string;

  /**
   * Network usage threshold (MB)
   */
  network: string;

  /**
   * Memory usage threshold (percentage)
   */
  memory: string;

  /**
   * Percentile configuration
   */
  percentile: {
    /**
     * Percentile value
     */
    value: string;

    /**
     * Percentile display message
     */
    message: string;
  } | string;

  /**
   * Percentile display name
   */
  percentileName?: string;

  /**
   * Average response time threshold
   */
  art: string;

  /**
   * Transactions per second threshold
   */
  tps: string;
}

/**
 * Efficiency indicator level configuration
 * @interface EfficiencyIndicatorLevel
 */
export interface EfficiencyIndicatorLevel {
  /**
   * Efficiency level name
   */
  level: string;

  /**
   * Workload value
   */
  workload: string;

  /**
   * Completed rate (percentage)
   */
  completedRate: string;

  /**
   * Overdue rate (percentage)
   */
  overdueRate: string;

  /**
   * One-time passed rate (percentage)
   */
  oneTimePassedRate: string;

  /**
   * Saving workload rate (percentage)
   */
  savingWorkloadRate: string;
}
