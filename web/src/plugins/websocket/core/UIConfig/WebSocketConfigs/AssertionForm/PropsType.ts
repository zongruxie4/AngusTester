import { BasicAssertionType, AssertionCondition } from '@xcan-angus/infra';

export type AssertionConfig = {
  name: string;
  type: BasicAssertionType;
  description: string;
  expected: string;
  assertionCondition: AssertionCondition;
  enabled: boolean;
  matchItem: string;
  expression: string;
}

export type AssertionInfo = {
  name: string;
  type: {value:BasicAssertionType;message:string;};
  description: string;
  expected: string;
  assertionCondition: {value:AssertionCondition;message:string;};
  enabled: boolean;
  matchItem: string;
  expression: string;
}
