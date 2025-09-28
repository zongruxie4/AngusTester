// Infrastructure imports
import { AssertionCondition, AssertionType } from '@xcan-angus/infra';

// Local type imports
import { Extraction } from './utils/extract/PropsType';

// Result type definition for assertion execution results
export interface AssertionExecutionResult {
    failure: boolean;
    message: string;
}

// Assertion type definition that can be either enum value or object with message and value
export type AssertionTypeDefinition = 
    | { message: string; value: AssertionType }
    | AssertionType
    | undefined;

// Main form item type definition for API assertions
export interface ApiAssertionFormItem {
    assertionCondition: AssertionCondition | undefined;
    condition: string | undefined;
    expression: string | undefined;
    matchItem: string | undefined;
    description: string | undefined;
    enabled: boolean;
    expected: string | undefined;
    extraction: Extraction;
    parameterName: string | undefined;
    name: string | undefined;
    type: AssertionTypeDefinition;
    result?: AssertionExecutionResult;
}

// Legacy export alias for backward compatibility
export type FormItem = ApiAssertionFormItem;
