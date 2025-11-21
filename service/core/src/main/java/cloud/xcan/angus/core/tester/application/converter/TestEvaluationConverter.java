package cloud.xcan.angus.core.tester.application.converter;

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
      return builder.score(0.0).numerator(0L).denominator(0L).rate(0.0).build();
    }

    // Count passed cases (excluding canceled)
    long passedCases = cases.stream()
        .filter(c -> c.getTestResult() == CaseTestResult.PASSED)
        .count();

    double rate = (double) passedCases / totalCases * 100.0;
    double score = rate / 10.0;

    return builder.score(Math.max(0.0, Math.min(10.0, score)))
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
      return builder.score(0.0).build();
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
      return builder.score(0.0).build();
    }

    double avgScore = totalScore / scoredCases;
    // Normalize to 0-10 scale
    double normalizedScore = avgScore / 10.0 * 10.0;

    return builder.score(Math.max(0.0, Math.min(10.0, normalizedScore))).build();
  }

  /**
   * Calculate overall score as weighted average of all metrics
   */
  public static Double calculateOverallScore(
      LinkedHashMap<EvaluationPurpose, MetricResult> metrics) {
    if (metrics.isEmpty()) {
      return 0.0;
    }

    double totalScore = 0.0;
    int count = 0;

    for (TestEvaluationResult.MetricResult metric : metrics.values()) {
      if (metric.getScore() != null) {
        totalScore += metric.getScore();
        count++;
      }
    }

    return count > 0 ? totalScore / count : 0.0;
  }
}
