export type SelectOption = {
    id: string;
    name: string;
    showTitle: string;
    showName: string;
}

export type MenuItem = {
    key: 'none' | 'createdBy' | 'lastModifiedBy' | 'execBy' | 'lastDay' | 'lastThreeDays' | 'lastWeek';
    name: string;
}
