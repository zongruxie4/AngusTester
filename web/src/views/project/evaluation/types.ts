import { EnumMessage } from '@xcan-angus/infra';
import { EvaluationScope, EvaluationPurpose } from '@/enums/enums';

/**
 * Evaluation detail interface
 */
export interface EvaluationDetail {
  id: number;
  name: string;
  projectId: number;
  projectName?: string;
  scope?: EnumMessage<EvaluationScope> | EvaluationScope;
  purposes?: Array<EnumMessage<EvaluationPurpose> | EvaluationPurpose>;
  resourceId?: number;
  startDate?: string;
  deadlineDate?: string;
  result?: any;
  createdBy?: number;
  createdByName?: string;
  createdDate?: string;
  lastModifiedBy?: number;
  lastModifiedName?: string;
  lastModifiedDate?: string;
}

/**
 * Evaluation form state interface
 */
export interface EvaluationFormState {
  id?: number;
  projectId: string;
  name: string;
  scope?: EvaluationScope;
  purposes?: EvaluationPurpose[];
  resourceId?: number | string;
  startDate?: string;
  deadlineDate?: string;
  date?: [string, string];
}

