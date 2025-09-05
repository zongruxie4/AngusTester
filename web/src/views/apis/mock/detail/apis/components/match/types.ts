import { ParameterIn, FullMatchCondition, StringMatchCondition } from '@xcan-angus/infra';

export type ResponseMatchInfo = {
    priority: string | undefined;
    path: {
        condition: { value: StringMatchCondition; message: string; };
        expected: string | undefined;
        expression: string | undefined;
    } | undefined;

    body: {
        condition: { value: FullMatchCondition; message: string; };
        expected: string | undefined;
        expression: string | undefined;
    } | undefined;

    parameters: {
        condition: { value: FullMatchCondition; message: string; };
        expected: string | undefined;
        expression: string | undefined;
        in: ParameterIn.header | ParameterIn.query;
        name: string;
    }[] | undefined;
}

export type ResponseMatchConfig = {
    priority: string | undefined;
    path: {
        condition: StringMatchCondition;
        expected: string | undefined;
        expression: string | undefined;
    } | undefined;

    body: {
        condition: FullMatchCondition;
        expected: string | undefined;
        expression: string | undefined;
    } | undefined;

    parameters: {
        condition: FullMatchCondition;
        expected: string | undefined;
        expression: string | undefined;
        in: ParameterIn.header | ParameterIn.query;
        name: string;
    }[] | undefined;
}
