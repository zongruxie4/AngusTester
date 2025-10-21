import { Extraction } from '../extract/PropsType';
import { AssertionCondition, AssertionType } from '@xcan-angus/infra';

export type ConditionResult = {
    failure: boolean;// 执行结果
    name: string;// 提取的变量名
    conditionMessage: string;// 条件表达式格式错误原因
    failureMessage: string;// 提取失败原因
    value: string;// 变量的值
    ignored: boolean;// 是否忽略该条断言
    message: string;
}

export type AssertResult = {
    name: string;
    type: AssertionType;
    parameterName: string;
    condition: string;
    extraction: boolean;// 是否是提取期望值
    assertionCondition: AssertionCondition;
    result: {
        expectedData: {
            data: string|null;
            message: string;
            errorMessage: string;
        };
        failure: boolean;
        realValueData: {
            data: string|null;
            message: string;
            errorMessage: string;
        };
        message: string;
    };
    _condition: {
        failure: boolean;// 执行结果
        name: string;// 提取的变量名
        conditionMessage: string;// 断言表达式错误的原因
        failureMessage: string;// 提取失败的原因
        value: string;// 提取变量的值
        ignored: boolean;// 是否忽略该条断言
        message: string;
    };
};

export type Parameter = {
    form: { [key: string]: any };
    rawBody: { [key: string]: any };
    responseBody: {
        data: any;
        size: number;
    };
    query: { [key: string]: any };
    path: { [key: string]: any };
    header: { [key: string]: any };
    duration: number;
    responseHeader: Record<string, string>;
    status: number;
}

export type AssertConfig = {
    assertionCondition: AssertionCondition;
    condition: string;
    description: string;
    enabled: boolean;
    expected: string;
    extraction: Extraction;
    parameterName: string;
    name: string;
    type: AssertionType;
    result?: {
        failure: boolean;
        message: string;
    };
}
