import { TestTemplateType, CaseStepView } from '@/enums/enums';
import { CaseTestStep } from '@/views/test/types';
import { EvalWorkloadMethod } from '@xcan-angus/infra';

export type TestTemplateContent = {
  templateType: TestTemplateType;
  testingScope?: string;
  testingObjectives?: string;
  acceptanceCriteria?: string;
  otherInformation?: string;
  precondition?: string;
  stepView?: CaseStepView;
  steps?: CaseTestStep[];
  description?: string;
  [key: string]: any;
};

export type TestTemplateDetail = {
  id: string;
  name: string;
  templateType: TestTemplateType | string;
  templateContent: TestTemplateContent;
  isSystem: boolean;
  createdBy: string;
  createdByName: string;
  createdDate: string;
  lastModifiedBy: string;
  lastModifiedByName: string;
  lastModifiedDate: string;
};
export type IssueTemplateEditFormState = {
  name: string;
  taskType: string;
  bugLevel?: string;
  priority: string;
  missingBug?: boolean;
  evalWorkloadMethod: EvalWorkloadMethod;
  evalWorkload?: number;
  actualWorkload?: number;
  description?: string;
  templateType: 'ISSUE';
};

export type PlanTemplateEditFormState = {
  testingScope?: string;
  testingObjectives?: string;
  acceptanceCriteria?: string;
  otherInformation?: string;
  templateType: TestTemplateType;
};

export type CaseTemplateEditFormState = {
  testLayer: string;
  testPurpose: string;
  precondition?: string;
  steps?: CaseTestStep[];
  description?: string;
  templateType: TestTemplateType;
  stepView?: CaseStepView;
}

export type TestTemplateEditFormState = {
  id?: string;
  name: string;
  templateType: TestTemplateType | string;
  templateContent?: TestTemplateContent;
  templateContent_issue?: IssueTemplateEditFormState;
  templateContent_plan?: PlanTemplateEditFormState;
  templateContent_case?: CaseTemplateEditFormState;
};

