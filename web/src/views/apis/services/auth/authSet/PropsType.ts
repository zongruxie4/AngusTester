export type TreeElement = {
    auth: boolean;
    pid: string;
    id: string;
    name: string;
    level: number;
    hasApisFlag: boolean;
    targetType: { message: string; value: 'PROJECT' | 'SERVICE'; };
    isApi?: boolean;
    children?: TreeElement[];
}

export type ArrayItem = {
    auth: boolean;
    pid: string;
    id: string;
    name: string;
    level: number;
    hasApisFlag: boolean;
    targetType: 'PROJECT' | 'SERVICE';
    children: string[];
    endpoint?: string;
    method?: string;
    isApi?: boolean;
}
