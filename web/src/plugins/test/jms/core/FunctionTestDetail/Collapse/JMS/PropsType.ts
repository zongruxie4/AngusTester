import { HttpMethod, AssertionCondition, BasicAssertionType, ParameterIn } from '@xcan-angus/infra';

export type JMSInfo = {
    target: 'JMS';
    name: string;
    linkName?: string;
    description: string;
    enabled: boolean;
    beforeName: string;
    transactionName: string;
    request: {
        method: HttpMethod;
        url: string;
        server: {
            url: string;
            variables: {
                [key: string]: {
                    defaultValue: string;
                    allowableValues: string[];
                    description?: string;
                }
            };
            description?: string;
        };
        endpoint: string;
        parameters: {
            name: string;
            in: ParameterIn;
            value: string;
            enabled: boolean;
            type: 'string';
        }[];
        body: { [key: string]: { [key: string]: any } };
        authentication: {
            type: string;
            enabled: boolean;
            'x-xc-value': string;
            scheme: string;
        };
    };
    assertions: {
        name: string;
        enabled: boolean;
        type: {
            value: BasicAssertionType;
            message: string;
        },
        expected: string;
        assertionCondition: {
            value: AssertionCondition;
            message: string;
        };
        expression: string;
        description: string;
        parameterName: string;
        condition: string;
        extraction: {
            method: {
                value: string;
                message: string;
            },
            expression: string;
            matchItem: string;
            defaultValue: string;
            location: string;
            parameterName: string;
        };
    }[];
    apisId: string;
    caseId: string;
    id: string;// 前端自动生成，用于给每条记录添加id
}
