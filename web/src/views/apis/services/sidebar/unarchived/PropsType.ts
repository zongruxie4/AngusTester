export type UnarchivedItem = {
    id: string;
    protocol: {
        value: string;
        message: string;
    },
    method: string;
    endpoint: string;
    summary: string;
    createdDate: string;
    lastModifiedDate: string;
}
