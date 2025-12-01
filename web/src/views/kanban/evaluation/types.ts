/**
 * Evaluation data interface
 */
export interface EvaluationData {
  /** Compliance score */
  COMPLIANCE_SCORE: {
    score: number;
  };
  /** Availability score */
  AVAILABILITY_SCORE: {
    score: number;
  };
  /** Performance passed rate */
  PERFORMANCE_SCORE: {
    rate: number;
    numerator: number;
    denominator: number;
    score: number;
  };
  /** Functional passed rate */
  FUNCTIONAL_SCORE: {
    rate: number;
    numerator: number;
    denominator: number;
    score: number;
  };

  STABILITY_SCORE: {
    rate: number;
    numerator: number;
    denominator: number;
    score: number;
  };
  COMPATIBILITY_SCORE: {
    score: number;
  };
  USABILITY_SCORE: {
    score: number;
  };
  MAINTAINABILITY_SCORE: {
    score: number;
  };
  SCALABILITY_SCORE: {
    score: number;
  };
  SECURITY_SCORE: {
    score: number;
  };


  /** Overall score */
  overallScore: number;
  /** Score level */
  scoreLevel: string;
  /** Requirement completion data */
  // requirementCompletion: {
  //   rate: number;
  //   completed: number;
  //   total: number;
  // };
  /** Function test coverage data */
  // functionTestCoverage: {
  //   rate: number;
  //   covered: number;
  //   total: number;
  // };
  /** Function test pass rate */
  // functionTestPassRate: {
  //   rate: number;
  //   passed: number;
  //   total: number;
  // };
  /** Performance test pass rate */
  // performanceTestPassRate: {
  //   rate: number;
  //   passed: number;
  //   total: number;
  // };
  /** Stability test pass rate */
  // stabilityTestPassRate: {
  //   rate: number;
  //   passed: number;
  //   total: number;
  // };
  /** Compatibility score */
  compatibilityScore: number;
  /** Usability score */
  usabilityScore: number;
  /** Maintainability score */
  maintainabilityScore: number;
  /** Extensibility score */
  extensibilityScore: number;
  /** Statistics data */
  statistics: {
    totalEvaluations: number;
    completedEvaluations: number;
    averageScore: number;
    highestScore: number;
    lowestScore: number;
  };
}

/**
 * Chart configuration interface
 */
export interface ChartConfig {
  /** Chart title configuration */
  title?: {
    text?: string;
    subtext?: string;
    left?: string;
    top?: string;
    padding?: number;
    textAlign?: string;
    textStyle?: {
      fontSize?: number;
      fontWeight?: string;
      color?: string;
    };
    subtextStyle?: {
      fontSize?: number;
      color?: string;
    };
  };
  /** Chart tooltip configuration */
  tooltip?: {
    trigger?: string;
    formatter?: string | ((params: any) => string);
    show?: boolean;
  };
  /** Chart legend configuration */
  legend?: {
    top?: string;
    left?: string;
    right?: string;
    orient?: string;
    itemHeight?: number;
    itemWidth?: number;
    itemGap?: number;
    textStyle?: {
      fontSize?: number;
    };
  };
  /** Chart series configuration */
  series?: Array<any>;
  /** Chart grid configuration */
  grid?: {
    left?: string | number;
    right?: string | number;
    bottom?: string | number;
    top?: string | number;
  };
  /** Chart x-axis configuration */
  xAxis?: any;
  /** Chart y-axis configuration */
  yAxis?: any;
  /** Chart graphic configuration */
  graphic?: any;
  /** Chart gauge configuration */
  angleAxis?: any;
  /** Chart radius axis configuration */
  radiusAxis?: any;
  /** Chart polar configuration */
  polar?: any;
}
