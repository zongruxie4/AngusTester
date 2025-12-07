package cloud.xcan.angus.core.tester.application.converter;

import static cloud.xcan.angus.spec.utils.ObjectUtils.calcRate;
import static cloud.xcan.angus.spec.utils.ObjectUtils.formatDouble;

import cloud.xcan.angus.core.tester.domain.project.evaluation.EvaluationPurpose;
import cloud.xcan.angus.core.tester.domain.project.evaluation.TestEvaluationResult;
import cloud.xcan.angus.core.tester.domain.project.evaluation.TestEvaluationResult.MetricResult;
import cloud.xcan.angus.core.tester.domain.test.TestPurpose;
import cloud.xcan.angus.core.tester.domain.test.cases.CaseTestResult;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfo;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class TestEvaluationConverter {

  /**
   * Calculate functional test passed rate
   */
  public static TestEvaluationResult.MetricResult calculateFunctionalPassedRate(
      List<FuncCaseInfo> cases, TestEvaluationResult.MetricResult.MetricResultBuilder builder) {
    List<FuncCaseInfo> functionalCases = cases.stream()
        .filter(c -> c.getTestPurpose() == TestPurpose.FUNCTIONAL)
        .collect(Collectors.toList());

    return calculatePassedRate(functionalCases, builder);
  }

  /**
   * Calculate performance test passed rate
   */
  public static TestEvaluationResult.MetricResult calculatePerformancePassedRate(
      List<FuncCaseInfo> cases, TestEvaluationResult.MetricResult.MetricResultBuilder builder) {
    List<FuncCaseInfo> performanceCases = cases.stream()
        .filter(c -> c.getTestPurpose() == TestPurpose.PERFORMANCE)
        .collect(Collectors.toList());

    return calculatePassedRate(performanceCases, builder);
  }

  /**
   * Calculate stability test passed rate
   */
  public static TestEvaluationResult.MetricResult calculateStabilityPassedRate(
      List<FuncCaseInfo> cases, TestEvaluationResult.MetricResult.MetricResultBuilder builder) {
    List<FuncCaseInfo> stabilityCases = cases.stream()
        .filter(c -> c.getTestPurpose() == TestPurpose.STABILITY)
        .collect(Collectors.toList());

    return calculatePassedRate(stabilityCases, builder);
  }

  /**
   * Calculate passed rate for a list of cases
   */
  public static TestEvaluationResult.MetricResult calculatePassedRate(
      List<FuncCaseInfo> cases, TestEvaluationResult.MetricResult.MetricResultBuilder builder) {
    long totalCases = cases.size();
    if (totalCases == 0) {
      return builder.totalCases(0).totalScore(0.0).score(0.0)
          .numerator(0L).denominator(0L).rate(0.0)
          .build();
    }

    // Count passed cases (excluding canceled)
    long passedCases = cases.stream()
        .filter(c -> c.getTestResult() == CaseTestResult.PASSED)
        .count();

    double rate = calcRate(passedCases, totalCases);
    double score = formatDouble(rate / 10, "0.00");

    return builder
        .totalCases(cases.size())
        .totalScore(passedCases * 10D)
        .score(Math.max(0.0, score))
        .numerator(passedCases)
        .denominator(totalCases)
        .rate(rate)
        .build();
  }

  /**
   * Calculate security score
   */
  public static TestEvaluationResult.MetricResult calculateSecurityScore(
      List<FuncCaseInfo> cases, TestEvaluationResult.MetricResult.MetricResultBuilder builder) {
    List<FuncCaseInfo> securityCases = cases.stream()
        .filter(c -> c.getTestPurpose() == TestPurpose.SECURITY)
        .collect(Collectors.toList());

    return calculateScoreByTestResult(securityCases, builder);
  }

  /**
   * Calculate compatibility score
   */
  public static TestEvaluationResult.MetricResult calculateCompatibilityScore(
      List<FuncCaseInfo> cases, TestEvaluationResult.MetricResult.MetricResultBuilder builder) {
    List<FuncCaseInfo> compatibilityCases = cases.stream()
        .filter(c -> c.getTestPurpose() == TestPurpose.COMPATIBILITY)
        .collect(Collectors.toList());

    return calculateScoreByTestResult(compatibilityCases, builder);
  }

  /**
   * Calculate usability score
   */
  public static TestEvaluationResult.MetricResult calculateAvailabilityScore(
      List<FuncCaseInfo> cases, TestEvaluationResult.MetricResult.MetricResultBuilder builder) {
    List<FuncCaseInfo> usabilityCases = cases.stream()
        .filter(c -> c.getTestPurpose() == TestPurpose.AVAILABILITY)
        .collect(Collectors.toList());

    return calculateScoreByTestResult(usabilityCases, builder);
  }

  /**
   * Calculate usability score
   */
  public static TestEvaluationResult.MetricResult calculateComplianceScore(
      List<FuncCaseInfo> cases, TestEvaluationResult.MetricResult.MetricResultBuilder builder) {
    List<FuncCaseInfo> usabilityCases = cases.stream()
        .filter(c -> c.getTestPurpose() == TestPurpose.COMPLIANCE)
        .collect(Collectors.toList());

    return calculateScoreByTestResult(usabilityCases, builder);
  }

  /**
   * Calculate usability score
   */
  public static TestEvaluationResult.MetricResult calculateUsabilityScore(
      List<FuncCaseInfo> cases, TestEvaluationResult.MetricResult.MetricResultBuilder builder) {
    List<FuncCaseInfo> usabilityCases = cases.stream()
        .filter(c -> c.getTestPurpose() == TestPurpose.USABILITY)
        .collect(Collectors.toList());

    return calculateScoreByTestResult(usabilityCases, builder);
  }

  /**
   * Calculate maintainability score
   */
  public static TestEvaluationResult.MetricResult calculateMaintainabilityScore(
      List<FuncCaseInfo> cases, TestEvaluationResult.MetricResult.MetricResultBuilder builder) {
    List<FuncCaseInfo> maintainabilityCases = cases.stream()
        .filter(c -> c.getTestPurpose() == TestPurpose.MAINTAINABILITY)
        .collect(Collectors.toList());

    return calculateScoreByTestResult(maintainabilityCases, builder);
  }

  /**
   * Calculate scalability score
   */
  public static TestEvaluationResult.MetricResult calculateScalabilityScore(
      List<FuncCaseInfo> cases, TestEvaluationResult.MetricResult.MetricResultBuilder builder) {
    List<FuncCaseInfo> scalabilityCases = cases.stream()
        .filter(c -> c.getTestPurpose() == TestPurpose.SCALABILITY)
        .collect(Collectors.toList());

    return calculateScoreByTestResult(scalabilityCases, builder);
  }

  /**
   * Calculate score based on test results (for SCORE metrics)
   */
  public static TestEvaluationResult.MetricResult calculateScoreByTestResult(
      List<FuncCaseInfo> cases, TestEvaluationResult.MetricResult.MetricResultBuilder builder) {
    long totalCases = cases.size();
    if (totalCases == 0) {
      return builder.totalCases(0).totalScore(0.0).score(0.0).build();
    }

    // Calculate average score based on test results
    double totalScore = 0.0;
    long scoredCases = 0;

    for (FuncCaseInfo c : cases) {
      if (c.getTestResult() == CaseTestResult.PASSED) {
        // Use testScore if available, otherwise default to 10
        Integer testScore = c.getTestScore();
        totalScore += (testScore != null ? testScore : 10);
        scoredCases++;
      } else if (c.getTestResult() == CaseTestResult.NOT_PASSED) {
        // Failed cases get 0 score
        totalScore += 0;
        scoredCases++;
      }
    }

    if (scoredCases == 0) {
      return builder.totalCases(totalCases).totalScore(0.0).score(0.0).build();
    }

    double avgScore = totalScore / scoredCases;
    // Normalize to 0-10 scale
    double normalizedScore = avgScore / 10.0 * 10.0;

    return builder.totalCases(totalCases).totalScore(totalScore)
        .score(Math.max(0.0, Math.min(10.0, normalizedScore))).build();
  }

  /**
   * Calculate overall score as weighted average of all metrics Uses weight from each MetricResult
   * if available and greater than 0, otherwise falls back to simple average
   */
  public static Double calculateOverallScore(
      LinkedHashMap<EvaluationPurpose, MetricResult> metrics) {
    if (metrics.isEmpty()) {
      return 0.0;
    }

    double weightedSum = 0.0;
    int totalWeight = 0;
    int unweightedCount = 0;
    double unweightedSum = 0.0;

    for (TestEvaluationResult.MetricResult metric : metrics.values()) {
      if (metric.getScore() != null) {
        Integer weight = metric.getWeight();
        if (weight != null && weight > 0) {
          // Use weighted calculation
          weightedSum += metric.getScore() * weight;
          totalWeight += weight;
        } else {
          // Track unweighted metrics for fallback
          unweightedSum += metric.getScore();
          unweightedCount++;
        }
      }
    }

    // If we have weighted metrics, use weighted average
    if (totalWeight > 0) {
      // If there are also unweighted metrics, include them with default weight of 1
      if (unweightedCount > 0) {
        // Combine weighted and unweighted metrics
        // Unweighted metrics get default weight of 1
        double combinedSum = weightedSum + unweightedSum;
        int combinedWeight = totalWeight + unweightedCount;
        return formatDouble(combinedSum / combinedWeight, "0.00");
      }
      // All metrics have weights, use pure weighted average
      return formatDouble(weightedSum / totalWeight, "0.00");
    }

    // Fallback to simple average if no weights are available
    return unweightedCount > 0 ? formatDouble(unweightedSum / unweightedCount, "0.00") : 0.0;
  }
}
