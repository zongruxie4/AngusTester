
export type FormState = {
    id?: string;
    scenarioId: string;
    name: string;
    description?: string;
    noticeSetting?: {
        enabled: boolean;
        orgType: 'DEPT'|'GROUP'|'USER',
        orgs: string[]
    };
    serverSetting?: {url: string; description?: string; variables?: {[key: string]: {[key: string]: string}}}[];
    timeSetting?: {
      createdAt: 'AT_SOME_DATE'| 'NOW'|'PERIODICALLY';
      createdAtSomeDate?: string;
      dayOfMonth?: string;
      dayOfWeek?: string;
      periodicCreationUnit: 'DAILY'| 'MONTHLY'| 'WEEKLY';
      timeOfDay?: string;
      minuteOfHour?: string;
      hourOfDay?: string;
    };
};
