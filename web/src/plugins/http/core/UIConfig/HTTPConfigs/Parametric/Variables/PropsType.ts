export type FormState = {
    source: 'HTTP_SAMPLING',
    defaultValue: string;
    expression: string;
    matchItem: string;
    method: 'EXACT_VALUE' | 'JSON_PATH' | 'REGEX' | 'X_PATH';
    parameterName: string;
    location: 'PATH_PARAMETER' | 'QUERY_PARAMETER' | 'REQUEST_HEADER' | 'FORM_PARAMETER' | 'REQUEST_RAW_BODY' | 'RESPONSE_HEADER' | 'RESPONSE_BODY';
    name: string;
}
