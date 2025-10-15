/* eslint-disable @typescript-eslint/ban-types */
export type ComponentsKey = 'schemas' | 'responses' | 'parameters' | 'examples' | 'requestBodies' | 'headers' | 'securitySchemes'
export type DataType = 'boolean' | 'string' | 'number' | 'integer' | 'array' | 'object';

export type ArrayItems = {
    type: DataType;
    example?: string;
    examples?: string[];
    enum?: string[];
    properties?: {
        [key: string]: {
            type: DataType;
            example?: string;
            examples?: string[];
            enum?: string[];
            properties?: ArrayItems['properties'];
            items?: {
                type: DataType;
                example?: string;
                examples?: string[];
                enum?: string[];
                properties: ArrayItems['properties'];
            };
        };
    };
    items?: ArrayItems;
}

export type ObjectProperties = {
    [key: string]: {
        type: DataType;
        example?: string;
        examples?: string[];
        enum?: string[];
        properties?: ObjectProperties;
        items?: ArrayItems;
    };
}

export type RequestBodyContent = {
    schema: {
        type: DataType;
        properties: ObjectProperties;
        required: string[];
        example: string;
        examples: string[];
        items: ArrayItems;
    }
}

export type Schema = {
    type: string;
    format: string;
    description: string;
    required: string[];
    items: Schema;
    properties: {
        [key: string]: {
            type: string;
            format: string;
            $ref?: string;
            required: boolean;
        }
    };
    discriminator: {
        propertyName: string;
        mapping: { [key: string]: string; }
    };
    xml: {
        name: string;
        namespace: string;
        prefix: string;
        attribute: boolean;
        wrapped: boolean;
    };
    externalDocs: {
        description: string;
        url: string;
    };
    example: any;
}

export type PathItemInfo = {
    _id: string;
    _uri: string;
    _method: string;
    _navs: {
        _id: string;
        _name: string;
        _key: string;
    }[];
    tags: string[];
    summary: string;
    description: string;
    parameters: {
        name: string;
        in: string;
        required: boolean;
        schema: {
            type: string;
            $ref: string;
            examples: any[];
            example: any;
            enum?: string[]
            default?: any;
            items?: {
                type: string;
                properties?: {
                    type: string;
                }
            };
            properties?: {
                type: string;
            };
        };
        examples: {
            [key: string]: {
                value: any;
                $ref: string;
            }
        };
        example: any;
        'x-xc-example': string;
        'x-xc-value': string;
        'x-xc-enabled': boolean;
    }[];
    requestBody: {
        content: RequestBodyContent;
    };
    responses: {
        [key: string]: {
            description: string;
            $ref?: string;
            headers?: {
                [key: string]: {
                    schema: {
                        type: DataType;
                        format: string;
                    };
                    description: string;
                }
            },
            content?: {
                [key: string]: {
                    example: any;
                    examples: any;
                    schema: {
                        type: DataType;
                        example: any;
                        examples: any;
                        oneOf: string[];
                        $refs: string;
                        properties: {
                            [key: string]: {
                                type: DataType;
                                example: any;
                                examples: any;
                                oneOf: string[];
                                $refs: string;
                            }
                        }
                    }
                }
            }
        }
    };
    security: {
        [key: string]: string[];
    }[];
    deprecated: boolean;
}

export type OpenApiInfo = {
    openapi: string;
    info: {
        title: string;
        description: string;
        termsOfService: string;
        summary: string;
        contact: {
            name: string;
            email: string;
            url: string;
        };
        license: {
            name: string;
            url: string;
        };
        version: string;
    };
    externalDocs: {
        description: string;
        url: string;
    };
    servers: {
        _id: string;
        url: string;
        variables: {
            [key: string]: {
                default: string;
                enum: string[];
                description: string;
            }
        };
        description: string;
    }[]
    security: { _id: string; _name: string; _value: string[] }[];
    tags: {
        _id: string;// 前端扩展字段
        name: string;
        description: string;
        externalDocs: {
            description: string;
            url: string;
        }
    }[];
    components: {
        schemas?: {
            [key: string]: {
                type: string;
                format: string;
                description: string;
                required: string[];
                properties: {
                    [key: string]: {
                        type: string;
                        format: string;
                        $ref?: string;
                    }
                };
                discriminator: {
                    propertyName: string;
                    mapping: { [key: string]: string; }
                };
                xml: {
                    name: string;
                    namespace: string;
                    prefix: string;
                    attribute: boolean;
                    wrapped: boolean;
                };
                externalDocs: {
                    description: string;
                    url: string;
                };
                example: any;
            }
        };
        responses?: {

        };
        parameters?: {

        };
        examples?: {

        };
        requestBodies?: {

        };
        headers?: {

        };
        securitySchemes?: {
            type: 'http' | 'apiKey' | 'oauth2' | 'openIdConnect';
            scheme: 'basic' | 'bearer';
            typeName: string;
            in: string;
            name: string;
            description: string;
            flows: {
                implicit: {
                    authorizationUrl: string;
                    tokenUrl: string;
                    refreshUrl: string;
                    scopes: { [key: string]: string; };
                };
                authorizationCode: {
                    authorizationUrl: string;
                    tokenUrl: string;
                    refreshUrl: string;
                    scopes: { [key: string]: string; };
                };
                password: {
                    authorizationUrl: string;
                    tokenUrl: string;
                    refreshUrl: string;
                    scopes: { [key: string]: string; };
                };
                clientCredentials: {
                    authorizationUrl: string;
                    tokenUrl: string;
                    refreshUrl: string;
                    scopes: { [key: string]: string; };
                }
            }
        };
        links?: {

        };
        callbacks?: {

        };
        pathItems?: {

        };
    };
    paths: {
        [key: string]: {
            [key: string]: {
                tags: string[];
                summary: string;
                description: string;
                parameters: {
                    name: string;
                    in: string;
                    required: boolean;
                    'x-xc-value': string;
                    'x-xc-enabled': boolean;
                }[];
                requestBody: {
                    content: {
                        [key: string]: {
                            schema: {
                                type: object;
                                properties: {
                                    [key: string]: {
                                        type: string;
                                        example: string;
                                        'x-xc-value': string;
                                    };
                                };
                                required: string[];
                            }
                        }
                    }
                };
                responses: {
                    [key: string]: {
                        description: string;
                        content: {
                            [key: string]: {
                                schema: {
                                    type: string;
                                    example: string;
                                    'x-xc-value': string;
                                }
                            }
                        }
                    }
                };
                deprecated: boolean;
                security: {
                    [key: string]: any;
                }[];
                'x-xc-status': {
                    value: 'UNKNOWN' | 'IN_DESIGN' | 'IN_DEV' | 'DEV_COMPLETED' | 'RELEASED';
                    message: string;
                };
            };
        };
    };
    'x-xc-projectId': string;
}
