export type HttpMethod = MockServicePermission.DELETE | 'GET' | 'HEAD' | 'OPTIONS' | 'PATCH' | 'POST' | 'PUT' | 'TRACE';
export type DelayMode = 'FIXED' | 'NONE' | 'RANDOM';
export type ParametersInType = 'cookie' | 'header' | 'query';
export type RadioType = 'none' | 'application/x-www-form-urlencode' | 'multipart/form-data' | 'raw'
export type ContentType = 'application/x-www-form-urlencode' | 'multipart/form-data' | 'application/json' | 'text/html' | 'application/xml' | 'application/javascript' | 'text/plain' | '*/*'
export type Languge = 'json' | 'html' | 'typescript' | 'text'
export type ContentEncoding = ContentEncoding

export type DelayData = {
    fixedTime?: string;
    maxRandomTime?: string;
    minRandomTime?: string;
    mode: DelayMode;
}

export type PushbackBody = {
    forms: {
        name: string;
        value: string;
    }[];
    rawContent: string;
    contentType?: string;
};

export type ResponseHeader = { name: string; value: string; disabled: boolean; }

export type ResponseContentInfo = {
    content: string | undefined;
    contentEncoding: ContentEncoding | undefined;
    delay: {
        fixedTime?: string;
        maxRandomTime?: string;
        minRandomTime?: string;
        mode: { value: DelayMode; message: string; };
    } | undefined;
    headers: ResponseHeader[] | undefined;
    status: string;
}

export type ResponseContentConfig = {
    content: string | undefined;
    contentEncoding: ContentEncoding | undefined;
    delay: DelayData | undefined;
    headers: ResponseHeader[] | undefined;
    status: string;
}

export type ParametersType = {
    in: ParametersInType;
    name: string;
    value: string;
    disabled: boolean;
}

export type ResponsePushbackInfo = {
    autoPush: boolean;
    body: PushbackBody;
    delay: {
        fixedTime?: string;
        maxRandomTime?: string;
        minRandomTime?: string;
        mode: { value: DelayMode; message: string; };
    };
    method: { value: HttpMethod; message: string; };
    parameters: ParametersType[];
    url: string;
}

export type ResponsePushbackConfig = {
    autoPush: boolean;
    body: PushbackBody;
    delay: DelayData | undefined;
    method: HttpMethod;
    parameters: ParametersType[];
    url: string;
}
