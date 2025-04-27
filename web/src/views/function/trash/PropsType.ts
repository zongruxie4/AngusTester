export type TrashItem = {
    id: string;
    createdBy: string;
    deletedBy: string;
    createdByAvatar: string;
    deletedByAvatar: string;
    createByName: string;
    deletedByName: string;
    targetName: string;
    targetId: string;
    createdDate: string;
    deletedDate: string;

    // 前端自己添加的字段，用于判断是否拥有还原、删除操作
    disabled?: boolean;
}
