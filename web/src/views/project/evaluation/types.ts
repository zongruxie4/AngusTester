import { EnumMessage } from '@xcan-angus/infra';
import { EvaluationScope, EvaluationPurpose } from '@/enums/enums';

/**
 * Evaluation detail interface
 */
export interface EvaluationDetail {
  id: string;
  name: string;
  projectId: string;
  projectName?: string;
  scope?: EnumMessage<EvaluationScope> | EvaluationScope;
  purposes?: Array<EnumMessage<EvaluationPurpose> | EvaluationPurpose>;
  resourceId?: string;
  startDate?: string;
  deadlineDate?: string;
  result?: any;
  createdBy?: string;
  creator?: string;
  createdDate?: string;
  modifiedBy?: string;
  lastModifiedName?: string;
  modifiedDate?: string;
}

/**
 * Evaluation form state interface
 */
export interface EvaluationFormState {
  id?: string;
  projectId: string;
  name: string;
  scope?: EvaluationScope;
  purposes?: EvaluationPurpose[];
  resourceId?: string | string;
  startDate?: string;
  deadlineDate?: string;
  date?: [string, string];
}

