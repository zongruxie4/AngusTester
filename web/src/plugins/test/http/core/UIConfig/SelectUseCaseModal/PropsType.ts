import { HTTPInfo } from '../HTTPConfigs/PropsType';
import { HttpMethod, EnumMessage } from '@xcan-angus/infra';

export type UseCaseInfo = {
    id: string;
    caseType: {
        value: string;
        message: string;
    };
    caseId: string;
    apisId: string;
    name: string;
    apisSummary: string;
    method: EnumMessage<HttpMethod>;
    uri: string;
    currentServer: {
        url: string;
        emptyContent: boolean;
        notEmptyContent: boolean;
        'x-xc-id': string;
        'x-xc-serverSource': string;
    };
    parameters: {
        name: string;
        in: string;
        description: string;
        schema: {
            type: string;
            exampleSetFlag: boolean;
            types: string[];
        };
        'x-xc-enabled': boolean;
    }[];
    requestBody: {
        $ref:string;
        description: string;
        content: {
            [key:string]: {
                schema: {[key:string]:any};
                exampleSetFlag: boolean;
                'x-xc-value': string;
            }
        };
        required: boolean;
    };
    authentication: {
        type: string;
        name: string;
        in: string;
    };
    assertions: HTTPInfo['assertions'];
    variables:{
        id:string;
        name:string;
        description:string;
        enabled:boolean;
        enabled?:boolean;
        scope: {
            value: 'GLOBAL' | 'CURRENT';
            message: string;
        };
        source: {
            value: 'SCENARIO';
            message: string;
        };
        value:string;
        extraction:{
            method: {
                value:'EXACT_VALUE' | 'JSON_PATH' | 'REGEX' | 'X_PATH';
                message:string;
            };
            expression: string;
            defaultValue: string;
            parameterName: string;
            location: 'PATH_PARAMETER'|'QUERY_PARAMETER'|'REQUEST_HEADER'|'FORM_PARAMETER'|'REQUEST_RAW_BODY'|'RESPONSE_HEADER'|'RESPONSE_BODY';
            request: {
                method:{
                    value:'POST'|'GET'|'PUT'|'PATCH'|'DELETE'|'HEAD'|'OPTIONS'|'TRACE';
                    message:string;
                };
                url:string;
                server:{
                    url:string;
                    description:string;
                    variables:{
                        allowableValues:string[];
                        defaultValue:string;
                        description:string;
                    };
                };
                endpoint:string;
                authentication:{
                    type:'none'|'http'|'apiKey'|'oauth2';
                    name:string;
                    description:string;
                    enabled:boolean;
                    value:boolean;
                    apiKeys:{
                        name:string;
                        in:string;
                        value:string;
                    }[];
                    oauth2:{
                        clientCredentials:{
                            tokenUrl:string;
                            refreshUrl:string;
                            scopes:string[];
                            clientId:string;
                            clientSecret:string;
                            clientIn:'QUERY_PARAMETER'|'BASIC_AUTH_HEADER'|'REQUEST_BODY';
                            username:string;
                            password:string;
                        };
                        password:{
                            tokenUrl:string;
                            refreshUrl:string;
                            scopes:string[];
                            clientId:string;
                            clientSecret:string;
                            clientIn:'QUERY_PARAMETER'|'BASIC_AUTH_HEADER'|'REQUEST_BODY';
                            username:string;
                            password:string;
                        };
                        authFlow:'clientCredentials'| 'password';
                        newToken: boolean;
                        token: string;
                    };
                };
                parameters:{
                    name:string;
                    in:{
                        value:'QUERY'|'PATH'|'COOKIE'|'HEADER';
                        message:string;
                    };
                    description:string;
                    enabled:boolean;
                    type:'string'|'number'|'integer'|'boolean'|'array'|'object';
                    format:string;
                    value:string;
                }[];
                body:{
                    format:string;
                    contentEncoding:'gzip_base64';
                    forms:{
                        name:string;
                        description:string;
                        enabled:boolean;
                        type:'string'|'number'|'integer'|'boolean'|'array'|'object';
                        format:string;
                        contentType:string;
                        contentEncoding:'gzip_base64';
                        fileName:string;
                        value:string;
                    }[];
                    rawContent:string;
                }
            };
        };
    }[];
}
