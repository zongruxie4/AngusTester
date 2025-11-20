/**
 * <p>Evaluation module type definitions</p>
 */

/**
 * <p>Evaluation scope type</p>
 */
export type ScopeType = 'project' | 'application' | 'plan';

/**
 * <p>Evaluation scope information</p>
 */
export interface EvaluationScope {
  type: ScopeType;
  name: string;
  id?: string;
}

/**
 * <p>Evaluation metrics key</p>
 */
export type MetricKey =
  | 'requirement_completion_rate'
  | 'function_test_coverage'
  | 'function_test_pass_rate'
  | 'performance_test_pass_rate'
  | 'stability_test_pass_rate'
  | 'compatibility_score'
  | 'usability_score'
  | 'maintainability_score'
  | 'extensibility_score';

/**
 * <p>Evaluation metrics configuration</p>
 */
export interface EvaluationMetrics {
  [key: string]: {
    score: number;
    value: string;
  };
}

/**
 * <p>Evaluation result</p>
 */
export interface EvaluationResult {
  overallScore: number;
  metrics: EvaluationMetrics;
  generatedAt?: string;
}

/**
 * <p>Evaluation status</p>
 */
export type EvaluationStatus = 'completed' | 'processing' | 'failed';

/**
 * <p>Evaluation item</p>
 */
export interface EvaluationItem {
  id: string;
  name: string;
  scope: EvaluationScope;
  metrics: MetricKey[];
  createdAt: string;
  createdBy: string;
  status: EvaluationStatus;
  result?: EvaluationResult;
}
