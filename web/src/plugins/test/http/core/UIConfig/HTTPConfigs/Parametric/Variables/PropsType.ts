import { HttpExtractionLocation, ExtractionMethod } from '@xcan-angus/infra';

export type FormState = {
    source: 'HTTP_SAMPLING',
    defaultValue: string;
    expression: string;
    matchItem: string;
    method: ExtractionMethod;
    parameterName: string;
    location: HttpExtractionLocation;
    name: string;
}
