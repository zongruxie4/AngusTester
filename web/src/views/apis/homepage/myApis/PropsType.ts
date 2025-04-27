export type ApiItem = {
    id: string;
    source: {
        value: string;
        message: string;
    };
    projectId: string;
    projectName: string;
    protocol: {
        value: string;
        message: string;
    };
    method: string;
    endpoint: string;
    summary: string;
    deprecated: boolean;
    status: {
        value: string;
        message: string;
    };
    createdBy: string;
    createdByName: string;
    ownerId: string;
    ownerName: string;
    avatar: string;
    createdDate: string;
    lastModifiedDate: string;
    authFlag: boolean;
}
