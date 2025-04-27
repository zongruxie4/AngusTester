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
        variables: Record<string, { default: string; description: string; enum: string[]; extensions: Record<string, string> }>;
    }[];
    specVersion: string;
    tags: {
        description: string;
        extensions: Record<string, string>;
        externalDocs: { description: string; extensions: Record<string, string>; url: string };
        name: string;
    }[];
}
