import { Extraction } from './utils/PropsType2';
import { AssertionCondition, AssertionType } from '@xcan-angus/infra';

export type FormItem = {
    assertionCondition: AssertionCondition|undefined;
    condition?: string|undefined;
    expression: string|undefined;
    matchItem: string|undefined;
    description: string|undefined;
    expected: string|undefined;
    extraction: Extraction;
    parameterName: string|undefined;
    name: string|undefined;
    type: {message:string;value:AssertionType}| AssertionType|undefined;
    result?: {
        failure: boolean;
        message: string;
    };
}
