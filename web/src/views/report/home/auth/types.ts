/**
 * Type definition for space item in authorization management
 */
export type SpaceItem = {
    id: string;
    name: string;
    auth: boolean;
    remark: string;
    bizKey: string;
    size: string;
    subDirectoryNum: string;
    subFileNum: string;
    createdBy: string;
    createdByName: string;
    createdDate: string;
};
