import { Extraction } from './utils/extract/PropsType';
import { AssertType, AssertCondition } from './utils/assert/PropsType';

export type FormItem = {
    assertionCondition: AssertCondition|undefined;
    condition: string|undefined;
    expression: string|undefined;
    matchItem: string|undefined;
    description: string|undefined;
    enabled: boolean;
    expected: string|undefined;
    extraction: Extraction;
    parameterName: string|undefined;
    name: string|undefined;
    type: {message:string;value:AssertType}|AssertType|undefined;
    result?: {
        failure: boolean;
        message: string;
    };
}
