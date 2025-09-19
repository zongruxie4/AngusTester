export type FormState = {
    source: 'JDBC',
    defaultValue: string;
    expression: string;
    matchItem: string;
    method: 'EXACT_VALUE' | 'JSON_PATH' | 'REGEX' | 'X_PATH';
    name: string;
}
