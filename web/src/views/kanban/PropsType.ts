export type ProjectInfo = {
    name: string;
    description: string;
    avatar: string;
    ownerName: string;
    lastModifiedDate: string;
    id: string;
    ownerId: string;
    members: {
        USER?: { name: string; avatar?: string }[];
        DEPT?: { name: string; avatar?: string }[];
        GROUP?: { name: string; avatar?: string }[];
    };
}
