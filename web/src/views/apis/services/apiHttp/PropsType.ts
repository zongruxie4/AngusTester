export type AssertType = 'STATUS' | 'HEADER' | 'BODY' | 'BODY_SIZE' | 'SIZE' | 'DURATION';

export type ExtractMethod = 'EXACT_VALUE' | 'JSON_PATH' | 'REGEX' | 'X_PATH';

export type Extraction = {
    method: ExtractMethod|undefined;
    expression: string|undefined;
    location: string|undefined;
    matchItem: string|undefined;
    apisId?: string;
    defaultValue?: string;
    failureMessage?: string;
    finalValue?: string;
    request?: Request;
    name?: string;
    parameterName?: string;
    value?: string;
  }

export type AssertCondition = 'CONTAIN' |
    'EQUAL' |
    'GREATER_THAN' |
    'GREATER_THAN_EQUAL' |
    'IS_EMPTY' |
    'IS_NULL' |
    'LESS_THAN' |
    'LESS_THAN_EQUAL' |
    'NOT_CONTAIN' |
    'NOT_EMPTY' |
    'NOT_EQUAL' |
    'NOT_NULL' |
    'REG_MATCH' |
    'XPATH_MATCH' |
    'JSON_PATH_MATCH';

export type ConditionResult = {
        failure: boolean;// 执行结果
        name: string;// 提取的变量名
        conditionMessage: string;// 条件表达式格式错误原因
        failureMessage: string;// 提取失败原因
        value: string;// 变量的值
        ignored: boolean;// 是否忽略该条断言
        message: string;
    }

export type AssertConfig = {
        assertionCondition: AssertCondition;
        condition: string;
        description: string;
        enabled: boolean;
        expected: string;
        extraction: Extraction;
        parameterName: string;
        name: string;
        type: AssertType;
        result?: {
            failure: boolean;
            message: string;
        };
    }

export type AssertResult = {
        name: string;
        expected?:string;
        extractValue?:string;
        type: {message:string;value:AssertType;}|AssertType;
        parameterName: string;
        condition: string;
        extraction: boolean;// 是否是提取期望值
        assertionCondition: AssertCondition;
        result: {
            ignored?:boolean;
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
