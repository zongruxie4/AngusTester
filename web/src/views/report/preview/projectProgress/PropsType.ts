export type ReportContent = {
    content: {
        apis: {
            progress: {
                completed: string;
                completedRate: string;
                total: string;
            };
            testApis: {
                allApis: {
                    id: string;
                    method: string;
                    operationId: string;
                    status: {
                        value: 'DEV_COMPLETED' | 'IN_DESIGN' | 'IN_DEV' | 'RELEASED' | 'UNKNOWN';
                        message: string;
                    };
                    summary: string;
                    url: string;
                }[];
                enabledFuncTestNum: string;
                enabledPerfTestNum: string;
                enabledStabilityTestNum: string;
                enabledTestApiIds: {
                    FUNCTIONAL: string[];
                    PERFORMANCE: string[];
                    STABILITY: string[];
                };
                enabledTestNum: string;
                totalApisNum: string;
            };
            apisByStatus: {
                IN_DEV: string;
                UNKNOWN: string;
                RELEASED: string;
                IN_DESIGN: string;
                DEV_COMPLETED: string;
            };
            apisByMethod: {
                GET: string;
                PUT: string;
                HEAD: string;
                POST: string;
                PATCH: string;
                TRACE: string;
                DELETE: string;
                OPTIONS: string;
            };
            testResultCount: {
                enabledTestNum: string;
                testPassedNum: string;
                testUnPassedNum: string;
                testedNum: string;
                unTestedNum: string;
            };
            testResultInfos: {
                enabledTest: string;
                failed: string;
                funcTestFailureMessage: string;
                funcTestPassed: string;
                id: string;
                method: string;
                operationId: string;
                passed: string;
                perfTestFailureMessage: string;
                perfTestPassed: string;
                stabilityTestFailureMessage: string;
                stabilityTestPassed: string;
                status: {
                    value: 'DEV_COMPLETED' | 'IN_DESIGN' | 'IN_DEV' | 'RELEASED' | 'UNKNOWN';
                    message: string;
                };
                summary: string;
                tested: boolean;
                url: string;
                testFuncFlag: boolean;
                testPerfFlag: boolean;
                testStabilityFlag: boolean;
            }[];
        };
        cases: {
            burnDownCharts: {
                NUM: {
                    completed: string;
                    endDate: string;
                    expected: {
                        timeSeries: string;
                        value: { [key: string]: any };
                    }[];
                    remaining: {
                        timeSeries: string;
                        value: { [key: string]: any };
                    }[];
                    startDate: string;
                    total: string;
                };
                WORKLOAD: {
                    completed: string;
                    endDate: string;
                    expected: {
                        timeSeries: string;
                        value: { [key: string]: any };
                    }[];
                    remaining: {
                        timeSeries: string;
                        value: { [key: string]: any };
                    }[];
                    startDate: string;
                    total: string;
                };
            };
            cases: {
                actualWorkload: string;
                code: string;
                createdBy: string;
                createdByName: string;
                createdDate: string;
                deadlineDate: string;
                description: string;
                developerId: string;
                developerName: string;
                evalWorkload: string;
                evalWorkloadMethod: {
                    value: 'STORY_POINT' | 'WORKING_HOURS';
                    message: string;
                };
                id: string;
                lastModifiedBy: string;
                lastModifiedByName: string;
                lastModifiedDate: string;
                moduleId: string;
                moduleName: string;
                name: string;
                overdueFlag: boolean;
                planId: string;
                planName: string;
                priority: {
                    value: 'HIGH' | 'HIGHEST' | 'LOW' | 'LOWEST' | 'MEDIUM';
                    message: string;
                };
                projectId: string;
                reviewDate: string;
                reviewFailNum: string;
                review: boolean;
                reviewNum: string;
                reviewRemark: string;
                reviewStatus: {
                    value: 'FAILED' | 'PASSED' | 'PENDING';
                    message: string;
                };
                reviewerId: string;
                reviewerName: string;
                testFailNum: string;
                testNum: string;
                testRemark: string;
                testResult: {
                    value: 'BLOCKED' | 'CANCELED' | 'NOT_PASSED' | 'PASSED' | 'PENDING';
                    message: string;
                };
                testResultHandleDate: string;
                testerId: string;
                testerName: string;
            }[];
            testerOverview: {
                testerId: string;
                priorityOverview: { [key: string]: string };
                statusOverview: {
                    actualWorkload: string;
                    blockedTestNum: string;
                    canceledTestNum: string;
                    completedWorkload: string;
                    evalWorkload: string;
                    failedReviewNum: string;
                    notPassedTestNum: string;
                    oneTimeNotPassedNum: string;
                    oneTimeNotPassedRate: string;
                    oneTimePassedNum: string;
                    oneTimePassedRate: string;
                    oneTimePassedReviewNum: string;
                    oneTimePassedReviewRate: string;
                    overdueNum: string;
                    overdueRate: string;
                    passedReviewNum: string;
                    passedTestNum: string;
                    pendingReviewNum: string;
                    pendingTestNum: string;
                    progress: string;
                    savingWorkload: string;
                    savingWorkloadRate: string;
                    totalCaseNum: string;
                    totalNotReviewCaseNum: string;
                    totalReviewCaseNum: string;
                    totalReviewFailTimes: string;
                    totalReviewTimes: string;
                    totalReviewedCaseNum: string;
                    validCaseNum: string;
                };
            }[];
            testerRanking: {
                actualWorkloadRank: {
                    assigneeId: string;
                    score: string;
                }[];
                completedWorkloadRank: {
                    assigneeId: string;
                    score: string;
                }[];
                oneTimePassedReviewNumRank: {
                    assigneeId: string;
                    score: string;
                }[];
                oneTimePassedReviewRateRank: {
                    assigneeId: string;
                    score: string;
                }[];
                oneTimePassedTestNumRank: {
                    assigneeId: string;
                    score: string;
                }[];
                oneTimePassedTestRateRank: {
                    assigneeId: string;
                    score: string;
                }[];
                oneTimeUnPassedTestNumRank: {
                    assigneeId: string;
                    score: string;
                }[];
                oneTimeUnPassedTestRateRank: {
                    assigneeId: string;
                    score: string;
                }[];
                overdueNumRank: {
                    assigneeId: string;
                    score: string;
                }[];
                overdueRateRank: {
                    assigneeId: string;
                    score: string;
                }[];
                passedTestNumRank: {
                    assigneeId: string;
                    score: string;
                }[];
                passedTestRateRank: {
                    assigneeId: string;
                    score: string;
                }[];
                savingWorkloadRank: {
                    assigneeId: string;
                    score: string;
                }[];
                validCaseNumRank: {
                    assigneeId: string;
                    score: string;
                }[];
            };
            testers: {
                avatar: string;
                email: string;
                fullName: string;
                id: string;
                mobile: string;
                username: string;
            }[];
            totalOverview: {
                actualWorkload: string;
                blockedTestNum: string;
                canceledTestNum: string;
                completedWorkload: string;
                evalWorkload: string;
                failedReviewNum: string;
                notPassedTestNum: string;
                oneTimeNotPassedNum: string;
                oneTimeNotPassedRate: string;
                oneTimePassedNum: string;
                oneTimePassedRate: string;
                oneTimePassedReviewNum: string;
                oneTimePassedReviewRate: string;
                overdueNum: string;
                overdueRate: string;
                passedReviewNum: string;
                passedTestNum: string;
                pendingReviewNum: string;
                pendingTestNum: string;
                progress: string;
                savingWorkloadRate: string;
                totalCaseNum: string;
                totalNotReviewCaseNum: string;
                totalReviewCaseNum: string;
                totalReviewFailTimes: string;
                totalReviewTimes: string;
                totalReviewedCaseNum: string;
                validCaseNum: string;
            };
            totalPriorityOverview: {
                LOW: string;
                HIGH: string;
                LOWEST: string;
                MEDIUM: string;
                HIGHEST: string;
            };
            totalTestResultOverview: {
                PASSED: string;
                BLOCKED: string;
                PENDING: string;
                CANCELED: string;
                NOT_PASSED: string;
            };
        };
        progress: {
            completed: string;
            completedRate: string;
            total: string;
        };
        scenarios: {
            progress: {
                completed: string;
                completedRate: string;
                total: string;
            };
            testResultCount: {
                enabledTestNum: string;
                testPassedNum: string;
                testUnPassedNum: string;
                testedNum: string;
                unTestedNum: string;
            };
            testResultInfos: {
                enabledTest: boolean;
                failed: boolean;
                funcTestFailureMessage: string;
                funcTestPassed: boolean;
                id: string;
                method: string;
                operationId: string;
                passed: boolean;
                perfTestFailureMessage: string;
                perfTestPassed: boolean;
                stabilityTestFailureMessage: string;
                stabilityTestPassed: boolean;
                status: {
                    value: 'DEV_COMPLETED' | 'IN_DESIGN' | 'IN_DEV' | 'RELEASED';
                    message: string;
                };
                summary: string;
                tested: boolean;
                url: string;
                testFuncFlag: boolean;
                testPerfFlag: boolean;
                testStabilityFlag: boolean;
            }[];
            testScenarios: {
                allScenarios: {
                    id: string;
                    name: string;
                    plugin: string;
                    scriptId: string;
                    scriptType: {
                        value: 'MOCK_APIS' | 'MOCK_DATA' | 'TEST_CUSTOMIZATION' | 'TEST_FUNCTIONALITY' | 'TEST_PERFORMANCE' | 'TEST_STABILITY';
                        message: string;
                    };
                }[];
                enabledFuncTestNum: string;
                enabledPerfTestNum: string;
                enabledStabilityTestNum: string;
                enabledTestNum: string;
                enabledTestScenarioIds: { [key: string]: string };
                totalScenarioNum: string;
            };
            sceByPluginName: {
                [key:string]:string;
            };
            sceByScriptType: {
                MOCK_APIS: string;
                MOCK_DATA: string;
                TEST_STABILITY: string;
                TEST_PERFORMANCE: string;
                TEST_CUSTOMIZATION: string;
                TEST_FUNCTIONALITY: string;
            };
        };
        tasks: {
            assigneeOverview: {
                assigneeId: string;
                priorityOverview: { [key: string]: string; };
                typeOverview: { [key: string]: string; };
                statusOverview: {
                    actualWorkload: string;
                    bugNum: string;
                    canceledNum: string;
                    completedNum: string;
                    completedWorkload: string;
                    confirmingNum: string;
                    evalWorkload: string;
                    inProgressNum: string;
                    oneTimeNotPassedNum: string;
                    oneTimeNotPassedRate: string;
                    oneTimePassedNum: string;
                    oneTimePassedRate: string;
                    overdueNum: string;
                    overdueRate: string;
                    pendingNum: string;
                    progress: string;
                    savingWorkload: string;
                    savingWorkloadRate: string;
                    totalTaskNum: string;
                    validBugNum: string;
                    validBugRate: string;
                    validTaskNum: string;
                };
                totalStatusOverview: {
                    CANCELED: string;
                    COMPLETED: string;
                    CONFIRMING: string;
                    IN_PROGRESS: string;
                    PENDING: string;
                };
            }[];
            assigneeRanking: {
                actualWorkloadRank: {
                    assigneeId: string;
                    score: string;
                }[];
                completedNumRank: {
                    assigneeId: string;
                    score: string;
                }[];
                completedRateRank: {
                    assigneeId: string;
                    score: string;
                }[];
                completedWorkloadRank: {
                    assigneeId: string;
                    score: string;
                }[];
                oneTimePassedNumRank: {
                    assigneeId: string;
                    score: string;
                }[];
                oneTimePassedRateRank: {
                    assigneeId: string;
                    score: string;
                }[];
                oneTimeUnPassedNumRank: {
                    assigneeId: string;
                    score: string;
                }[];
                oneTimeUnPassedRateRank: {
                    assigneeId: string;
                    score: string;
                }[];
                overdueNumRank: {
                    assigneeId: string;
                    score: string;
                }[];
                overdueRateRank: {
                    assigneeId: string;
                    score: string;
                }[];
                savingWorkloadRank: {
                    assigneeId: string;
                    score: string;
                }[];
                validBugNumRank: {
                    assigneeId: string;
                    score: string;
                }[];
                validBugRateRank: {
                    assigneeId: string;
                    score: string;
                }[];
                validTaskNumRank: {
                    assigneeId: string;
                    score: string;
                }[];
            };
            assignees: {
                [key: string]: {
                    avatar: string;
                    email: string;
                    fullName: string;
                    id: string;
                    mobile: string;
                    username: string;
                };
            };
            burnDownCharts: {
                NUM: {
                    completed: string;
                    endDate: string;
                    expected: {
                        timeSeries: string;
                        value: { [key: string]: any };
                    }[];
                    remaining: {
                        timeSeries: string;
                        value: { [key: string]: any };
                    }[];
                    startDate: string;
                    total: string;
                };
                WORKLOAD: {
                    completed: string;
                    endDate: string;
                    expected: {
                        timeSeries: string;
                        value: { [key: string]: any };
                    }[];
                    remaining: {
                        timeSeries: string;
                        value: { [key: string]: any };
                    }[];
                    startDate: string;
                    total: string;
                };
            };
            tasks: {
                actualWorkload: string;
                assigneeId: string;
                assigneeName: string;
                backlogFlag: boolean;
                canceledDate: string;
                code: string;
                completedDate: string;
                confirmTaskFlag: boolean;
                confirmedDate: string;
                confirmorId: string;
                confirmorName: string;
                createdBy: string;
                createdByName: string;
                createdDate: string;
                deadlineDate: string;
                description: string;
                evalWorkload: string;
                evalWorkloadMethod: {
                    value: 'STORY_POINT' | 'WORKING_HOURS';
                    message: string;
                };
                execBy: string;
                execByName: string;
                execDate: string;
                execFailureMessage: string;
                execId: string;
                execName: string;
                execResult: {
                    value: 'FAIL' | 'SUCCESS';
                    message: string;
                };
                execTestFailureNum: string;
                execTestNum: string;
                failNum: string;
                id: string;
                lastModifiedBy: string;
                lastModifiedByName: string;
                lastModifiedDate: string;
                moduleId: string;
                moduleName: string;
                name: string;
                overdueFlag: boolean;
                priority: {
                    value: 'HIGH' | 'HIGHEST' | 'LOW' | 'LOWEST' | 'MEDIUM';
                    message: string;
                };
                processedDate: string;
                projectId: string;
                sprintId: string;
                sprintName: string;
                startDate: string;
                status: {
                    value: 'CANCELED' | 'COMPLETED' | 'CONFIRMING' | 'IN_PROGRESS' | 'PENDING';
                    message: string;
                };
                tags: { id: string; name: string; }[];
                targetId: string;
                targetName: string;
                targetParentId: string;
                targetParentName: string;
                taskType: {
                    value: 'API_TEST' | 'BUG' | 'REQUIREMENT' | 'SCENARIO_TEST' | 'STORY' | 'TASK';
                    message: string;
                };
                testType: {
                    value: 'CUSTOMIZATION' | 'FUNCTIONAL' | 'PERFORMANCE' | 'STABILITY';
                    message: string;
                };
                totalNum: string;
            }[];
            totalOverview: {
                actualWorkload: string;
                bugNum: string;
                canceledNum: string;
                completedNum: string;
                completedWorkload: string;
                confirmingNum: string;
                evalWorkload: string;
                inProgressNum: string;
                oneTimeNotPassedNum: string;
                oneTimeNotPassedRate: string;
                oneTimePassedNum: string;
                oneTimePassedRate: string;
                overdueNum: string;
                overdueRate: string;
                pendingNum: string;
                progress: string;
                savingWorkload: string;
                savingWorkloadRate: string;
                totalTaskNum: string;
                validBugNum: string;
                validBugRate: string;
                validTaskNum: string;
            };
            totalPriorityOverview: {
                LOW: string;
                HIGH: string;
                LOWEST: string;
                MEDIUM: string;
                HIGHEST: string;
            };
            totalTypeOverview: {
                BUG: string;
                TASK: string;
                STORY: string;
                API_TEST: string;
                REQUIREMENT: string;
                SCENARIO_TEST: string;
            };
            totalStatusOverview: {
                CANCELED: string;
                COMPLETED: string;
                CONFIRMING: string;
                IN_PROGRESS: string;
                PENDING: string;
            };
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
