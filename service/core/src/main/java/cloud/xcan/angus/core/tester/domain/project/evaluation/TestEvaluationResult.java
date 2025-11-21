package cloud.xcan.angus.core.tester.domain.project.evaluation;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>Test evaluation result data structure</p>
 * <p>
 * Represents the calculated results of a test evaluation, including overall score
 * and individual metric results for each evaluation purpose.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestEvaluationResult implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Overall evaluation score (weighted average of all metrics)
   */
  private Double overallScore;

  /**
   * Individual metric results for each evaluation purpose
   * Key: EvaluationPurpose enum value
   * Value: MetricResult containing score and formatted value
   */
  private LinkedHashMap<EvaluationPurpose, MetricResult> metrics;


  /**
   * <p>Individual metric result for a specific evaluation purpose</p>
   * <p>
   * Contains comprehensive calculation fields for different evaluation purposes:
   * - For RATE metrics: numerator, denominator, rate, and formatted value
   * - For SCORE metrics: score value and formatted display value
   * - For BUG_RATE: detailed bug statistics including valid/invalid bugs, missing bugs, etc.
   * </p>
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class MetricResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Numeric score value (0-10) for calculation purposes
     * Used for overall score calculation and comparison
     */
    private Double score;

    // ========== Rate Calculation Fields (for RATE metrics) ==========

    /**
     * Numerator value for rate calculation
     * Used for: CASE_REQUIREMENT_COVERAGE_RATE, FUNCTIONAL_PASSED_RATE,
     * PERFORMANCE_PASSED_RATE, STABILITY_PASSED_RATE
     * Example: passed test cases count, covered requirements count
     */
    private Long numerator;

    /**
     * Denominator value for rate calculation
     * Used for: CASE_REQUIREMENT_COVERAGE_RATE, FUNCTIONAL_PASSED_RATE,
     * PERFORMANCE_PASSED_RATE, STABILITY_PASSED_RATE
     * Example: total test cases count, total requirements count
     */
    private Long denominator;

    /**
     * Calculated rate value (0-100)
     * Formula: (numerator / denominator) * 100
     */
    private Double rate;

  }
}
