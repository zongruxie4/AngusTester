export type TreeElement = {
    auth: boolean;
    pid: string;
    id: string;
    name: string;
    level: number;
    hasApis: boolean;
    targetType: { message: string; value: 'SERVICE'; };
    isApi?: boolean;
    children?: TreeElement[];
}

export type ArrayItem = {
    auth: boolean;
    pid: string;
    id: string;
    name: string;
    level: number;
    hasApis: boolean;
    targetType: 'SERVICE';
    children: string[];
    endpoint?: string;
    method?: string;
    isApi?: boolean;
}
