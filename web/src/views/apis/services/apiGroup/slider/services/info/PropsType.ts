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
