import { TestTemplateType, CaseStepView } from '@/enums/enums';
import { CaseTestStep } from '@/views/test/types';

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

export type TestTemplateEditFormState = {
  id?: string;
  name: string;
  templateType: TestTemplateType | string;
  templateContent: TestTemplateContent;
};

