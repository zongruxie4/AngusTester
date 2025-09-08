export type ServerInfo = {
    'x-xc-id': string;
    extensions?: Record<string, any>;
    'description': string;
    'emptyContent': true;
    'notEmptyContent': true;
    'url': string;
    'variables': {
        [key:string]: {
            'default': string;
            'description': string;
            'enum': string[]
        };
    }
}
