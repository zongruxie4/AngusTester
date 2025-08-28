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

export type SourceItem = {
    targetId: string;
    targetName: string;
    targetType: {
        value: 'API' | 'SCENARIO';
        message: string;
    };
    createdBy: string;
    createdByName: string;
    createdDate: string;
}

export type IPane = { // TODO 重复定义了很多个
    _id: string;
    name: string;
    value: string;
    closable?: boolean;
    forceRender?: boolean;
    icon?: string;
    active?: boolean;

    data?: { [key: string]: any; };
};
