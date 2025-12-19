export type ReportContent = {
    content: {
        template: string;

    };
    report: {
        auth: boolean;
        basicInfoSetting: {
            otherInformation: string;
            reportContacts: string;
            reportCopyright: string;
            watermark: string;
        };
        category: string;
        contentSetting: {
            catalogContent: { [key: string]: { [key: string]: any }; };
            filter: {
                createdDateEnd: string;
                createdDateStart: string;
                creatorObjectId: string;
                creatorObjectType: string;
                planOrSprintId: string;
                targetId: string;
                targetType: string;
            }
        };
        createTimeSetting: {
            createdAt: string;
            createdAtSomeDate: string;
            dayOfMonth: string;
            dayOfWeek: string;
            nextGenerationDate: string;
            onetimeGeneration: boolean;
            periodicCreationUnit: string;
            timeOfDay: {
                hour: string;
                minute: string;
                nano: string;
                second: string;
            }
        };
        createdAt: { value: string; message: string; };
        createdBy: string;
        creator: string;
        createdDate: string;
        description: string;
        failureMessage: string;
        id: string;
        modifiedBy: string;
        modifier: string;
        modifiedDate: string;
        name: string;
        nextGenerationDate: string;
        projectId: string;
        projectName: string;
        status: { value: string; message: string; };
        targetId: string;
        targetName: string;
        targetType: { value: string; message: string; };
        template: { value: string; message: string; };
        tenantId: string;
        tenantName: string;
        version: string;
    },

}
