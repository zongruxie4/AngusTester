export interface Props {
    dataSource: {
        deprecated: boolean;
        operationId: string;
        parameters?: Array<any>;
        tags?: string[];
        responses: Record<string, Record<string, any>>;
        method: string;
        endpoint: string;
        summary: string;
        description: string;
        security: {[key: string]: string[]}[];
        'x-xc-status': string;
    };
    id: string;
    openapiDoc: {[key: string]: any};
}
