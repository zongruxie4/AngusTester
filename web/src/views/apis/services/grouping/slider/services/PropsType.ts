export type Status = 'DEV_COMPLETED' | 'IN_DESIGN' | 'IN_DEV' | 'RELEASED';
export type TargetType = 'API' | 'PROJECT' | 'SERVICE';
export type Source = 'CREATED' | 'EDITOR' | 'IMPORT' | 'SYNC';
export type ImportSource = 'OPENAPI' | 'POSTMAN';

export interface IInfomation {
    apiNum: number;
    auth: boolean;
    code: string;
    createdBy: string;
    createdByName: string;
    createdDate: string;
    id: string;
    importSource: { message: string; value: ImportSource; };
    lastModifiedBy: string;
    lastModifiedByName: string;
    lastModifiedDate: string;
    name: string;
    pid: string;
    source: { message: string; value: Source; };
    status: { message: string; value: Status; };
    targetType: { message: string; value: TargetType; };
}

export interface ISchema {
    extensions: Record<string, string>;
    externalDocs: { description: string; extensions?: Record<string, string>; url: string; };
    info: {
        contact?: { email: string; extensions?: Record<string, string>; name: string; url: string; };
        description?: string;
        extensions: Record<string, string>;
        license?: { extensions?: Record<string, string>; identifier?: string; name: string; url: string; };
        summary?: string;
        termsOfService?: string;
        title: string;
        version?: string;
    };
    lastModifiedBy: string;
    lastModifiedDate: string;
    openapi: string;
    serviceId: string;
    security: Record<string, string[]>[];
    servers: {
        description: string;
        extensions: Record<string, string>;
        url: string;
        variables: Record<string, {
            default: string;
            description: string;
            enum: string[];
            extensions: Record<string, string>
        }>;
    }[];
    specVersion: string;
    tags: {
        description: string;
        extensions: Record<string, string>;
        externalDocs: { description: string; extensions: Record<string, string>; url: string };
        name: string;
    }[];
}
