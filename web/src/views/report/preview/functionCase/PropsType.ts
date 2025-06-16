export type ReportContent = {
    content: {
        template: string;
        cases: {
            id: string;
            name: string;
            code: string;
            projectId: string;
            projectName: string;
            targetId: string;
            targetName: string;
            planId: string;
            planName: string;
            moduleId: string;
            moduleName: string;
            testerId: string;
            testerName: string;
            precondition: string;
            backlog: false;
            priority: {
                value: 'HIGH' | 'HIGHEST' | 'LOW' | 'LOWEST' | 'MEDIUM';
                message: string
            };
            status: {
                value: 'CANCELED' | 'COMPLETED' | 'CONFIRMING' | 'IN_PROGRESS' | 'PENDING';
                message: string
            };
            caseType: {
                value: 'API_TEST' | 'BUG' | 'REQUIREMENT' | 'SCENARIO_TEST' | 'STORY' | 'TASK';
                message: string
            };
            testType: {
                value: 'CUSTOMIZATION' | 'FUNCTIONAL' | 'PERFORMANCE' | 'STABILITY';
                message: string
            };
            testResult: {
                value: string;
                message: string
            };
            reviewStatus: {
                value: string;
                message: string;
            };
            reviewDate: string;
            reviewFailNum: string;
            review: boolean;
            reviewNum: string;
            reviewRemark: string;
            reviewerId: string;
            reviewerName: string;
            execBy: string;
            execByName: string;
            execDate: string;
            execFailureMessage: string;
            execId: string;
            execName: string;
            scriptId: string;
            scriptName: string;
            testNum: string;
            testFailNum: string;
            testRemark: string;
            steps: {
                step: string;
                expectedResult: string
            }[];
            refMap: {
                TASK: {
                    code: string;
                    id: string;
                    name: string;
                    taskType: {
                        value: 'API_TEST' | 'BUG' | 'REQUIREMENT' | 'SCENARIO_TEST' | 'STORY' | 'TASK';
                        message: string;
                    };
                }[]
                CASE: {
                    code: string;
                    id: string;
                    name: string;
                    taskType: {
                        value: 'API_TEST' | 'BUG' | 'REQUIREMENT' | 'SCENARIO_TEST' | 'STORY' | 'TASK';
                        message: string;
                    };
                }[]
            };
            tags: { id: string; name: string; }[];
            startDate: string;
            deadlineDate: string;
            confirmedDate: string;
            completedDate: string;
            processedDate: string;
            canceledDate: string;
            assigneeId: string;
            assigneeName: string;
            confirmorId: string;
            confirmorName: string;
            evalWorkload: string;
            actualWorkload: string;
            evalWorkloadMethod: {
                value: 'STORY_POINT' | 'WORKING_HOURS';
                message: string
            };
            failNum: string;
            totalNum: string;
            description: string;
            confirmTask: true;
            overdue: false;
            commentNum: string;
            createdBy: string;
            createdByName: string;
            createdDate: string;
            lastModifiedBy: string;
            lastModifiedByName: string;
            lastModifiedDate: string
        };
        remarks: {
            id: string;
            content: string;
            caseId: string;
            createdBy: string;
            createdByName: string;
            createdDate: string
        }[];
        activities: {
            id: string;
            projectId: string;
            userId: string;
            fullName: string;
            avatar: string;
            targetId: string;
            parentTargetId: string;
            targetType: {
                value: string;
                message: string
            };
            targetName: string;
            optDate: string;
            description: string;
            detail: string
        }[];
        comments: {
            id: string;
            pid: string;
            content: string;
            userId: string;
            userName: string;
            parentName: string | undefined;
            avatar: string;
            createdDate: string;
            totalCommentNum: string;
            children: {
                id: string;
                pid: string;
                content: string;
                userId: string;
                userName: string;
                parentName: string | undefined;
                avatar: string;
                createdDate: string;
                totalCommentNum: string;
                sequence0: string
            }[];
            sequence0: string
        }[]
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
        createdByName: string;
        createdDate: string;
        description: string;
        failureMessage: string;
        id: string;
        lastModifiedBy: string;
        lastModifiedByName: string;
        lastModifiedDate: string;
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
    }
}
