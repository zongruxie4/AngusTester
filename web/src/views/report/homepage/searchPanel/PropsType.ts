export type SelectOption = {
    id: string;
    name: string;
    showTitle: string;
    showName: string;
}

export type MenuItem = {
    key: 'none' | 'createdBy' | 'lastModifiedBy' | 'lastDay' | 'lastThreeDays' | 'lastWeek';
    name: string;
}
