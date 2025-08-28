export type IPane = {
    [key: string]: any;
} & {
    tab: string;
    key: string;
    closable: boolean;
    forceRender?: boolean;
    icon?: string;
    active?: boolean;
}

export const API_AUTH_CODE = [
  'MODIFY', 'DEBUG', 'SHARE', 'VIEW', 'DELETE', 'TEST', 'GRANT', 'RELEASE', 'EXPORT'
];
