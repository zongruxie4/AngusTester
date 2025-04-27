export type SelectOption = {
    id: string;
    name: string;
    showTitle: string;
    showName: string;
}

export type MenuItem = {
    key: 'none' | 'createdBy' | 'assigneeId' | 'lastModifiedBy' | 'confirmorId' | 'lastDay' | 'lastThreeDays' | 'lastWeek';
    name: string;
    groupKey?: 'assigneeId' | 'time';
}
