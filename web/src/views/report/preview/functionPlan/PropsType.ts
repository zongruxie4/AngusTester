export type ReportContent = {
    content: {
        template: string;
        plan: {
            id: string;
            projectId: string;
            projectName: string;
            name: string;
            status: {
                value: string;
                message: string;
            };
            attachments: {
                id: string;
                name: string;
                url: string;
            }[];
            description: string;
            auth: boolean;
            review: boolean;
            startDate: string;
            deadlineDate: string;
            ownerId: string;
            ownerName: string;
            ownerAvatar: string;
            acceptanceCriteria: string;
            otherInformation: string;
            autoUpdateResultByExec: boolean;
            evalWorkloadMethod: {
                value: string;
                message: string;
            };
            tenantId: string;
            tenantName: string;
            createdBy: string;
            createdByName: string;
            createdDate: string;
            lastModifiedBy: string;
            lastModifiedByName: string;
            lastModifiedDate: string;
            caseNum: string;
            casePrefix: string;
            progress: {
                total: string;
                completed: string;
                completedRate: string;
            };
            members: {
                id: string;
                username: string;
                fullName: string;
                mobile: string;
                email: string;
                avatar: string;
            }[];
            showMembers: {
                id: string;
                username: string;
                fullName: string;
                mobile: string;
                email: string;
                avatar: string;
            }[];
            testingScope: string;
            testingObjectives: string;
            testerResponsibilities: { [key: string]: string };
        };
        progress: {
            total: string;
            completed: string;
            completedRate: string
        };
        members: {
            id: string;
            username: string;
            fullName: string;
            mobile: string
        }[];
        cases: {
            testers: {
                [key: string]: {
                    id: string;
                    fullName: string;
                    avatar: string
                };
            };
            totalOverview: {
                totalCaseNum: string;
                validCaseNum: string;
                bugNum: string;
                validBugNum: string;
                pendingTestNum: string;
                passedTestNum: string;
                notPassedTestNum: string;
                blockedTestNum: string;
                inProgressNum: string;
                confirmingNum: string;
                completedNum: string;
                canceledTestNum: string;
                overdueNum: string;
                overdueRate: string;
                oneTimePassedNum: string;
                oneTimePassedRate: string;
                oneTimeNotPassedNum: string;
                oneTimeNotPassedRate: string;
                oneTimePassedReviewRate: string;
                evalWorkload: string;
                actualWorkload: string;
                completedWorkload: string;
                savingWorkload: string;
                savingWorkloadRate: string;
                progress: string;
                validBugRate: string
            };
            totalTypeOverview: {
                BUG: string;
                TASK: string;
                STORY: string;
                API_TEST: string;
                REQUIREMENT: string;
                SCENARIO_TEST: string
            };
            totalPriorityOverview: {
                LOW: string;
                HIGH: string;
                LOWEST: string;
                MEDIUM: string;
                HIGHEST: string
            };
            testerOverview: {
                testerId: string;
                statusOverview: {
                    totalCaseNum: string;
                    validCaseNum: string;
                    bugNum: string;
                    validBugNum: string;
                    pendingTestNum: string;
                    passedTestNum: string;
                    notPassedTestNum: string;
                    blockedTestNum: string;
                    inProgressNum: string;
                    confirmingNum: string;
                    completedNum: string;
                    canceledTestNum: string;
                    overdueNum: string;
                    overdueRate: string;
                    oneTimePassedNum: string;
                    oneTimePassedRate: string;
                    oneTimeNotPassedNum: string;
                    oneTimeNotPassedRate: string;
                    oneTimePassedReviewRate: string;
                    evalWorkload: string;
                    actualWorkload: string;
                    completedWorkload: string;
                    savingWorkload: string;
                    savingWorkloadRate: string;
                    progress: string;
                    validBugRate: string
                };
                typeOverview: {
                    BUG: string;
                    TASK: string;
                    STORY: string;
                    API_TEST: string;
                    REQUIREMENT: string;
                    SCENARIO_TEST: string
                };
                priorityOverview: {
                    LOW: string;
                    HIGH: string;
                    LOWEST: string;
                    MEDIUM: string;
                    HIGHEST: string
                }
            }[];
            testerRanking: {
                [key: string]: {
                    testerId: string;
                    score: string
                }[];
            };
            burnDownCharts: {
                NUM: {
                    total: string;
                    completed: string;
                    startDate: string;
                    endDate: string;
                    remaining: {
                        timeSeries: string;
                        value: string
                    }[];
                    expected: {
                        timeSeries: string;
                        value: string
                    }[];
                };
                WORKLOAD: {
                    total: string;
                    completed: string;
                    startDate: string;
                    endDate: string;
                    remaining: {
                        timeSeries: string;
                        value: string
                    }[];
                    expected: {
                        timeSeries: string;
                        value: string
                    }[];
                };
            };
            cases: {
                caseType: {
                    value: string;
                    message: string
                };
                status: {
                    value: string;
                    message: string
                };
                priority: {
                    value: string;
                    message: string
                };
                testType: {
                    value: string;
                    message: string
                };
                review: boolean;
                reviewDate: string;
                reviewFailNum: string;
                reviewNum: string;
                reviewRemark: string;
                reviewStatus: {
                    value: 'FAILED' | 'PASSED' | 'PENDING',
                    message: string;
                };
                reviewerName: string;
                testFailNum: string;
                testNum: string;
                testRemark: string;
                testResult: {
                    value: 'BLOCKED' | 'CANCELED' | 'NOT_PASSED' | 'PASSED' | 'PENDING';
                    message: string;
                };
                testResultHandleDate: string;
                overdueFlag: boolean;
                moduleName: string;
                failNum: string;
                totalNum: string;
                completedDate: string;
                evalWorkload: string;
                actualWorkload: string;
                testerId: string;
                createdDate: string;
                id: string;
                name: string;
                code: string;
                projectId: string;
                planId: string;
                planName: string;
                backlog: boolean;
                startDate: string;
                deadlineDate: string;
                processedDate: string;
                testerName: string;
                confirmorId: string;
                confirmorName: string;
                evalWorkloadMethod: {
                    value: string;
                    message: string
                };
                confirmCaseFlag: boolean;
                createdBy: string;
                createdByName: string;
                lastModifiedBy: string;
                lastModifiedByName: string;
                lastModifiedDate: string
            }[];
        }
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
        createdAt: { value: 'AT_SOME_DATE' | 'NOW' | 'PERIODICALLY'; message: string; };
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
        status: { value: 'FAILURE' | 'PENDING' | 'SUCCESS'; message: string; };
        targetId: string;
        targetName: string;
        targetType: { value: 'API' | 'API_CASE' | 'DATASET' | 'EXECUTION' | 'FUNC_CASE' | 'FUNC_PLAN' | 'MOCK_APIS' | 'MOCK_SERVICE' | 'MODULE' | 'PROJECT' | 'REPORT' | 'SCENARIO' | 'SCRIPT' | 'SERVICE' | 'TAG' | 'TASK' | 'TASK_SPRINT' | 'VARIABLE'; message: string; };
        template: { value: 'APIS_TESTING_RESULT' | 'EXEC_CUSTOMIZATION_RESULT' | 'EXEC_FUNCTIONAL_RESULT' | 'EXEC_PERFORMANCE_RESULT' | 'EXEC_STABILITY_RESULT' | 'FUNC_TESTING_CASE' | 'FUNC_TESTING_PLAN' | 'PROJECT_ACTIVITY' | 'PROJECT_DATA_ASSETS' | 'PROJECT_EFFICIENCY' | 'PROJECT_PROGRESS' | 'SCENARIO_TESTING_RESULT' | 'SERVICES_TESTING_RESULT' | 'TASK' | 'TASK_SPRINT'; message: string; };
        tenantId: string;
        tenantName: string;
        version: string;
    }
}
