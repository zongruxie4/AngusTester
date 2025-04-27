export type SelectOption = {
    id: string;
    name: string;
    showTitle: string;
    showName: string;
}

export type MenuItem = {
    key: 'none' | 'createdBy' | 'assigneeId' | 'progress' | 'confirmorId' | 'lastDay' | 'lastThreeDays' | 'lastWeek' | string;
    name: string;
    groupKey?: 'assigneeId' | 'time';
}
