export type ReportContent = {
    content: {
        template: string;
        task: {
            id: string;
            name: string;
            code: string;
            projectId: string;
            projectName: string;
            targetId: string;
            targetName: string;
            sprintId: string;
            sprintName: string;
            moduleId: string;
            moduleName: string;
            backlog: boolean;
            priority: {
                value: 'HIGH' | 'HIGHEST' | 'LOW' | 'LOWEST' | 'MEDIUM';
                message: string
            };
            status: {
                value: 'CANCELED' | 'COMPLETED' | 'CONFIRMING' | 'IN_PROGRESS' | 'PENDING';
                message: string;
            };
            taskType: {
                value: 'BUG' | 'REQUIREMENT' | 'DESIGN' | 'STORY' | 'TASK';
                message: string;
            };
            testType: {
                value: 'CUSTOMIZATION' | 'FUNCTIONAL' | 'PERFORMANCE' | 'STABILITY';
                message: string;
            };
            refTaskInfos: {
                id: string;
                code: string;
                taskType: {
                    value: 'BUG' | 'REQUIREMENT' | 'DESIGN' | 'STORY' | 'TASK';
                    message: string;
                };
                name: string;
            }[];
            refCaseInfos: {
                id: string;
                code: string;
                name: string;
            }[];
            execBy: string;
            execByName: string;
            execDate: string;
            execFailureMessage: string;
            execId: string;
            execName: string;
            scriptId: string;
            scriptName: string;
            execResult: {
                value: 'FAIL' | 'SUCCESS';
                message: string;
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
            confirmerId: string;
            confirmerName: string;
            evalWorkload: string;
            actualWorkload: string;
            evalWorkloadMethod: {
                value: 'STORY_POINT' | 'WORKING_HOURS';
                message: string;
            };
            failNum: string;
            totalNum: string;
            description: string;
            confirmTask: boolean;
            overdue: boolean;
            commentNum: string;
            createdBy: string;
            createdByName: string;
            createdDate: string;
            lastModifiedBy: string;
            lastModifiedByName: string;
            lastModifiedDate: string;
        };
        remarks: {
            id: string;
            content: string;
            taskId: string;
            createdBy: string;
            createdByName: string;
            createdDate: string;
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
                message: string;
            };
            targetName: string;
            optDate: string;
            description: string;
            detail: string;
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
                sequence0: string;
            }[];
            sequence0: string;
        }[];
        testResult: {
            id: string;
            execId: string;
            execName: string;
            execStatus: {
                value: string;
                message: string;
            };
            caseResults: {
                id: string;
                execId: string;
                execName: string;
                execStatus: {
                    value: string;
                    message: string;
                };
                plugin: string;
                scriptId: string;
                apisId: string;
                caseId: string;
                caseName: string;
                caseType: {
                    value: string;
                    message: string;
                };
                enabled: boolean;
                passed: boolean;
                testNum: string;
                testFailureNum: string;
                assertionSummary: {
                    totalNum: string;
                    successNum: string;
                    failureNum: string;
                    ignoreNum: string;
                    disabledNum: string;
                };
                sampleContent: {
                    nodeId: string;
                    timestamp: string;
                    timestamp0: string;
                    name: string;
                    iteration: string;
                    key: string;
                    sampleResult: {
                        target: string;
                        name: string;
                        success: boolean;
                        request0: {
                            method: string;
                            url: string;
                            queryString: string;
                            headers: {
                                Authorization: string;
                            };
                            size: string;
                        };
                        response: {
                            status: string;
                            size: string;
                            bodySize: string;
                            headers: { [key: string]: string };
                            rawContent: string;
                            timeline: {
                                fetchStart: string;
                                domainLookupStart: string;
                                domainLookupEnd: string;
                                connectStart: string;
                                connectEnd: string;
                                secureConnectionStart: string;
                                secureConnectionEnd: string;
                                requestStart: string;
                                responseStart: string;
                                responseEnd: string;
                                total: string;
                                requestDuration: string;
                                responseDuration: string;
                                sampleDuration: string;
                            };
                            headerArray: string[];
                        };
                        assertions: [
                            {
                                name: string;
                                enabled: boolean;
                                type: {
                                    value: string;
                                    message: string;
                                };
                                assertionCondition: {
                                    value: string;
                                    message: string;
                                };
                                expression: string;
                                ignore: boolean;
                                enabled: boolean;
                                success: boolean;
                            }
                        ]
                    }
                }[];
                execBy: string;
                lastExecDate: string;
                createdDate: string;
            }[];
            plugin: string;
            scriptType: {
                value: string;
                message: string;
            };
            scriptId: string;
            scriptSource: {
                value: string;
                message: string;
            };
            scriptSourceId: string;
            indicatorFunc: {
                smoke: boolean;
                smokeCheckSetting: {
                    value: string;
                    message: string;
                };
                security: boolean;
                securityCheckSetting: {
                    value: string;
                    message: string;
                }
            };
            passed: boolean;
            failureMessage: string;
            testNum: string;
            testFailureNum: string;
            nodeUsageSummary: {
                maxCpu: string;
                meanCpu: string;
                maxMemory: string;
                meanFilesystem: string;
                maxFilesystem: string;
                meanMemory: string;
                maxNetwork: string;
                meanNetwork: string;
            };
            indicatorStability: {
                threads: string;
                duration: string;
                tps: string;
                art: string;
                percentile: {
                    value: string;
                    message: string;
                },
                errorRate: string;
                cpu: string;
                memory: string;
                disk: string;
                network: string;
            };
            sampleSummary: {
                finish: boolean;
                name: string;
                duration: string;
                startTime: string;
                endTime: string;
                errors: string;
                iterations: string;
                n: string;
                operations: string;
                transactions: string;
                readBytes: string;
                writeBytes: string;
                ops: string;
                tps: string;
                brps: string;
                bwps: string;
                tranMean: string;
                tranMin: string;
                tranMax: string;
                tranP50: string;
                tranP75: string;
                tranP90: string;
                tranP95: string;
                tranP99: string;
                tranP999: string;
                errorRate: string;
                threadPoolSize: string;
                threadPoolActiveSize: string;
                threadMaxPoolSize: string;
                threadRunning: boolean;
                threadTerminated: boolean;
                uploadResultBytes: string;
                uploadResultTotalBytes: string;
                extCounter1: string;
                extCounter2: string;
                extCounter3: string;
                extGauge1: string;
                extGauge2: string;
                extGauge3: string;
            };
            targetSummary: {
                totalNum: string;
                disabledNum: string;
                successNum: string;
                failNum: string;
            };
            caseSummary: {
                totalNum: string;
                disabledNum: string;
                successNum: string;
                failNum: string;
            };
            sampleContent: {
                nodeId: string;
                timestamp: string;
                timestamp0: string;
                name: string;
                iteration: string;
                key: string;
                sampleResult: {
                    target: string;
                    name: string;
                    success: boolean;
                    request0: {
                        method: string;
                        url: string;
                        queryString: string;
                        headers: {
                            Authorization: string;
                        };
                        size: string;
                    };
                    response: {
                        status: string;
                        size: string;
                        bodySize: string;
                        headers: { [key: string]: string; };
                        rawContent: string;
                        timeline: {
                            fetchStart: string;
                            domainLookupStart: string;
                            domainLookupEnd: string;
                            connectStart: string;
                            connectEnd: string;
                            secureConnectionStart: string;
                            secureConnectionEnd: string;
                            requestStart: string;
                            responseStart: string;
                            responseEnd: string;
                            total: string;
                            requestDuration: string;
                            responseDuration: string;
                            sampleDuration: string;
                        };
                        headerArray: string[]
                    };
                    assertions: {
                        name: string;
                        enabled: boolean;
                        type: {
                            value: string;
                            message: string;
                        };
                        assertionCondition: {
                            value: string;
                            message: string;
                        };
                        expected: string;
                        actualExpected: string;
                        ignore: boolean;
                        enabled: boolean;
                        success: boolean
                    }[];
                }
            }[];
            execBy: string;
            execByName: string;
            lastExecDate: string;
            createdDate: string;
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
    },

}
