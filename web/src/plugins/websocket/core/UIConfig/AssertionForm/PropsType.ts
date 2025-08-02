import { AssertionCondition, BasicAssertionType } from '@xcan-angus/infra';

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
