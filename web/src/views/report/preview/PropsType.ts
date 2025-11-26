import { i18n } from '@xcan-angus/infra';

const I18nInstance = i18n.getI18n();
const t = I18nInstance?.global?.t || ((value: string):string => value);
export type ExecResult = {
    caseResults: {
        apisId: string;
        assertionSummary: {
            disabledNum: string;
            failureNum: string;
            ignoreNum: string;
            successNum: string;
            totalNum: string
        };
        caseId: string;
        caseName: string;
        caseType: string;
        createdDate: string;
        enabled: boolean;
        execBy: string;
        execByName: string;
        execId: string;
        execName: string;
        execStatus: {
            value: 'COMPLETED' | 'CREATED' | 'FAILED' | 'PENDING' | 'RUNNING' | 'STOPPED' | 'TIMEOUT';
            message: string;
        };
        failureMessage: string;
        id: string;
        lastExecDate: string;
        passed: boolean;
        plugin: string;
        sampleContent: {
            iteration: string;
            key: string;
            name: string;
            nodeId: string;
            sampleResult: {
                assertions: {
                    actualCondition: string;
                    actualExpected: string;
                    assertionCondition: string;
                    condition: string;
                    description: string;
                    enabled: boolean;
                    enabled: boolean;
                    expected: string;
                    expression: string;
                    extractValue: string;
                    extraction: {
                        defaultValue: string;
                        expression: string;
                        failureMessage: string;
                        finalValue: string;
                        matchItem: string;
                        method: string;
                        name: string;
                        value: string
                    };
                    hasStatusAssertion: boolean;
                    hasVariableValueInExpected: boolean;
                    ignore: boolean;
                    matchItem: string;
                    name: string;
                    parameterName: string;
                    result: {
                        failure: boolean;
                        ignored: boolean;
                        message: string
                    };
                    success: boolean;
                    type: string
                }[];
                failMessage: string;
                name: string;
                success: boolean;
                target: string
            };
            timestamp: string;
            timestamp0: string
        }[];
        scriptId: string;
        testFailureNum: string;
        testNum: string
    }[];
    caseSummary: {
        disabledNum: string;
        failNum: string;
        successNum: string;
        totalNum: string
    };
    createdDate: string;
    execBy: string;
    execByName: string;
    execId: string;
    execName: string;
    execStatus: {
        value: 'COMPLETED' | 'CREATED' | 'FAILED' | 'PENDING' | 'RUNNING' | 'STOPPED' | 'TIMEOUT';
        message: string;
    };
    failureMessage: string;
    id: string;
    indicatorFunc: {
        security: boolean;
        securityCheckSetting: string;
        smoke: boolean;
        smokeCheckSetting: string;
        userDefinedSecurityAssertion: {
            actualCondition: string;
            actualExpected: string;
            assertionCondition: string;
            condition: string;
            description: string;
            enabled: boolean;
            enabled: boolean;
            expected: string;
            expression: string;
            extractValue: string;
            extraction: {
                defaultValue: string;
                expression: string;
                failureMessage: string;
                finalValue: string;
                location: string;
                matchItem: string;
                method: string;
                name: string;
                parameterName: string;
                request: {
                    authentication: {
                        apiKeys: [
                            {
                                in: string;
                                name: string;
                                value: string
                            }
                        ];
                        description: string;
                        enabled: boolean;
                        name: string;
                        oauth2: {
                            authFlow: string;
                            clientCredentials: {
                                clientId: string;
                                clientIn: string;
                                clientSecret: string;
                                password: string;
                                refreshUrl: string;
                                scopes: [
                                    string
                                ];
                                tokenUrl: string;
                                username: string
                            };
                            newToken: boolean;
                            password: {
                                clientId: string;
                                clientIn: string;
                                clientSecret: string;
                                password: string;
                                refreshUrl: string;
                                scopes: [
                                    string
                                ];
                                tokenUrl: string;
                                username: string
                            };
                            token: string
                        };
                        type: string;
                        value: string
                    };
                    body: {
                        contentEncoding: string;
                        fileName: string;
                        format: string;
                        forms: [
                            {
                                contentEncoding: string;
                                contentType: string;
                                description: string;
                                enabled: boolean;
                                fileName: string;
                                format: string;
                                name: string;
                                type: string;
                                value: string
                            }
                        ];
                        rawContent: string
                    };
                    endpoint: string;
                    method: string;
                    parameters: [
                        {
                            description: string;
                            enabled: boolean;
                            format: string;
                            in: string;
                            name: string;
                            type: string;
                            value: string
                        }
                    ];
                    server: {
                        description: string;
                        url: string;
                        variables: {
                            additionalProp1: {
                                allowableValues: [
                                    string
                                ];
                                defaultValue: string;
                                description: string
                            };
                            additionalProp2: {
                                allowableValues: [
                                    string
                                ];
                                defaultValue: string;
                                description: string
                            };
                            additionalProp3: {
                                allowableValues: [
                                    string
                                ];
                                defaultValue: string;
                                description: string
                            }
                        }
                    };
                    url: string;
                    validSecurity: boolean
                };
                value: string
            };
            hasStatusAssertion: boolean;
            hasVariableValueInExpected: boolean;
            ignore: boolean;
            matchItem: string;
            name: string;
            parameterName: string;
            result: {
                failure: boolean;
                ignored: boolean;
                message: string
            };
            success: boolean;
            type: string
        };
        userDefinedSmokeAssertion: {
            actualCondition: string;
            actualExpected: string;
            assertionCondition: string;
            condition: string;
            description: string;
            enabled: boolean;
            enabled: boolean;
            expected: string;
            expression: string;
            extractValue: string;
            extraction: {
                defaultValue: string;
                expression: string;
                failureMessage: string;
                finalValue: string;
                location: string;
                matchItem: string;
                method: string;
                name: string;
                parameterName: string;
                request: {
                    authentication: {
                        apiKeys: [
                            {
                                in: string;
                                name: string;
                                value: string
                            }
                        ];
                        description: string;
                        enabled: boolean;
                        name: string;
                        oauth2: {
                            authFlow: string;
                            clientCredentials: {
                                clientId: string;
                                clientIn: string;
                                clientSecret: string;
                                password: string;
                                refreshUrl: string;
                                scopes: [
                                    string
                                ];
                                tokenUrl: string;
                                username: string
                            };
                            newToken: boolean;
                            password: {
                                clientId: string;
                                clientIn: string;
                                clientSecret: string;
                                password: string;
                                refreshUrl: string;
                                scopes: [
                                    string
                                ];
                                tokenUrl: string;
                                username: string
                            };
                            token: string
                        };
                        type: string;
                        value: string
                    };
                    body: {
                        contentEncoding: string;
                        fileName: string;
                        format: string;
                        forms: [
                            {
                                contentEncoding: string;
                                contentType: string;
                                description: string;
                                enabled: boolean;
                                fileName: string;
                                format: string;
                                name: string;
                                type: string;
                                value: string
                            }
                        ];
                        rawContent: string
                    };
                    endpoint: string;
                    method: string;
                    parameters: [
                        {
                            description: string;
                            enabled: boolean;
                            format: string;
                            in: string;
                            name: string;
                            type: string;
                            value: string
                        }
                    ];
                    server: {
                        description: string;
                        url: string;
                        variables: {
                            additionalProp1: {
                                allowableValues: [
                                    string
                                ];
                                defaultValue: string;
                                description: string
                            };
                            additionalProp2: {
                                allowableValues: [
                                    string
                                ];
                                defaultValue: string;
                                description: string
                            };
                            additionalProp3: {
                                allowableValues: [
                                    string
                                ];
                                defaultValue: string;
                                description: string
                            }
                        }
                    };
                    url: string;
                    validSecurity: boolean
                };
                value: string
            };
            hasStatusAssertion: boolean;
            hasVariableValueInExpected: boolean;
            ignore: boolean;
            matchItem: string;
            name: string;
            parameterName: string;
            result: {
                failure: boolean;
                ignored: boolean;
                message: string
            };
            success: boolean;
            type: string
        }
    };
    indicatorPerf: {
        art: string;
        duration: {
            unit: string;
            value: string
        };
        errorRate: string;
        percentile: string;
        rampUpInterval: {
            unit: string;
            value: string
        };
        rampUpThreads: string;
        threads: string;
        tps: string
    };
    indicatorStability: {
        art: string;
        cpu: string;
        disk: string;
        duration: {
            unit: string;
            value: string
        };
        errorRate: string;
        memory: string;
        network: string;
        percentile: string;
        threads: string;
        tps: string
    };
    lastExecDate: string;
    nodeUsageSummary: {
        [key: string]: {
            maxCpu: string;
            maxFilesystem: string;
            maxMemory: string;
            maxNetwork: string;
            meanCpu: string;
            meanFilesystem: string;
            meanMemory: string;
            meanNetwork: string
        };
    };
    passed: boolean;
    plugin: string;
    sampleContent: {
        iteration: string;
        key: string;
        name: string;
        nodeId: string;
        sampleResult: {
            assertions: {
                actualCondition: string;
                actualExpected: string;
                assertionCondition: string;
                condition: string;
                description: string;
                enabled: boolean;
                enabled: boolean;
                expected: string;
                expression: string;
                extractValue: string;
                extraction: {
                    defaultValue: string;
                    expression: string;
                    failureMessage: string;
                    finalValue: string;
                    matchItem: string;
                    method: string;
                    name: string;
                    value: string
                };
                hasStatusAssertion: boolean;
                hasVariableValueInExpected: boolean;
                ignore: boolean;
                matchItem: string;
                name: string;
                parameterName: string;
                result: {
                    failure: boolean;
                    ignored: boolean;
                    message: string
                };
                success: boolean;
                type: string
            }[];
            failMessage: string;
            name: string;
            success: boolean;
            target: string
        };
        timestamp: string;
        timestamp0: string
    }[];
    sampleSummary: {
        brps: string;
        bwps: string;
        duration: string;
        endTime: string;
        errorRate: string;
        errors: string;
        extCounter1: string;
        extCounter2: string;
        extCounter3: string;
        extGauge1: string;
        extGauge2: string;
        extGauge3: string;
        finish: boolean;
        iterations: string;
        n: string;
        name: string;
        operations: string;
        ops: string;
        readBytes: string;
        startTime: string;
        threadMaxPoolSize: string;
        threadPoolActiveSize: string;
        threadPoolSize: string;
        threadRunning: boolean;
        threadTerminated: boolean;
        tps: string;
        tranMax: string;
        tranMean: string;
        tranMin: string;
        tranP50: string;
        tranP75: string;
        tranP90: string;
        tranP95: string;
        tranP99: string;
        tranP999: string;
        transactions: string;
        uploadResultBytes: string;
        uploadResultProgress: string;
        uploadResultTotalBytes: string;
        writeBytes: string
    };
    scriptId: string;
    scriptName: string;
    scriptSource: string;
    scriptSourceId: string;
    scriptSourceName: string;
    scriptType: {
        value: 'MOCK_APIS' | 'MOCK_DATA' | 'TEST_CUSTOMIZATION' | 'TEST_FUNCTIONALITY' | 'TEST_PERFORMANCE' | 'TEST_STABILITY';
        message: string;
    };
    targetSummary: {
        disabledNum: string;
        failNum: string;
        successNum: string;
        totalNum: string
    };
    testFailureNum: string;
    testNum: string;
    usageFailedNodeId: string
}

export type ExecContent = {
    finish: true;
    nodeId: string;
    timestamp0: string;
    name: string;
    extField: string;
    iteration: string;
    key: string;
    content: {
        name: string;
        success: true;
        request0: {
            method: string;
            url: string;
            headers: {
                Authorization: string;
                'Content-Type': string
            };
            body: string;
            size: string
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
                sampleDuration: string
            };
            headerArray: string[]
        };
        assertions: {
            name: string;
            enabled: true;
            type: {
                value: string;
                message: string
            };
            assertionCondition: {
                value: string;
                message: string
            };
            expected: string;
            actualExpected: string;
            ignore: true;
            enabled: true;
            success: false
        }[];
        target: string
    };
    timestamp: string
}

export type ExecInfo = {
    actualStartDate: string;
    appNodes: {
        agentPort: string;
        domain: string;
        id: string;
        ip: string;
        name: string;
        publicIp: string;
        regionId: string;
        spec: {
            cpu: string;
            memory: string;
            network: string;
            networkChargeType: string;
            nodeId: string;
            nodeNum: string;
            showLabel: string;
            sysDisk: string;
            sysDiskCategory: string
        }
    }[];
    assocApiCaseIds: string[];
    availableNodes: {
        agentPort: string;
        domain: string;
        id: string;
        ip: string;
        name: string;
        publicIp: string;
        regionId: string;
        spec: {
            cpu: string;
            memory: string;
            network: string;
            networkChargeType: string;
            nodeId: string;
            nodeNum: string;
            showLabel: string;
            sysDisk: string;
            sysDiskCategory: string
        }
    }[

    ];
    canUpdateTestResult: boolean;
    configuration: {
        duration: {
            unit: string;
            value: string
        };
        iterations: string;
        lang: string;
        nodeSelectors: {
            appNodeIds: string[];
            availableNodeIds: string[];
            num: string;
            strategy: {
                cpuIdleRate: string;
                cpuSpec: string;
                diskIdleRate: string;
                diskSpec: string;
                enabled: boolean;
                idleRateEnabled: boolean;
                lastExecuted: boolean;
                maxTaskNum: string;
                memoryIdleRate: string;
                memorySpec: string;
                specEnabled: boolean
            }
        };
        onError: {
            action: string;
            sampleError: boolean;
            sampleErrorNum: string
        };
        priority: string;
        reportInterval: {
            unit: string;
            value: string
        };
        runnerSetupTimeout: {
            unit: string;
            value: string
        };
        shutdownTimeout: {
            unit: string;
            value: string
        };
        startAtDate: string;
        startMode: string;
        startupTimeout: {
            unit: string;
            value: string
        };
        thread: {
            rampDownInterval: {
                unit: string;
                value: string
            };
            rampDownThreads: string;
            rampUpInterval: {
                unit: string;
                value: string
            };
            rampUpThreads: string;
            resetAfterRamp: boolean;
            threads: string
        };
        updateVariableByIteration: boolean;
        variables: {
            actualValue: string;
            description: string;
            extractable: boolean;
            extraction: {
                defaultValue: string;
                expression: string;
                failureMessage: string;
                finalValue: string;
                matchItem: string;
                method: string;
                name: string;
                value: string
            };
            name: string;
            passwordValue: boolean;
            value: string
        }[]
    };
    createdBy: string;
    createdByName: string;
    createdDate: string;
    currentDuration: string;
    currentDurationProgress: string;
    currentIterations: string;
    currentIterationsProgress: string;
    duration: {
        unit: string;
        value: string
    };
    durationProgress: boolean;
    endDate: string;
    execBy: string;
    execByName: string;
    execNodes:
    {
        agentPort: string;
        domain: string;
        id: string;
        ip: string;
        name: string;
        publicIp: string;
        regionId: string;
        spec: {
            cpu: string;
            memory: string;
            network: string;
            networkChargeType: string;
            nodeId: string;
            nodeNum: string;
            showLabel: string;
            sysDisk: string;
            sysDiskCategory: string
        }
    }[];
    hasOperationPermission: boolean;
    id: string;
    ignoreAssertions: boolean;
    iterations: string;
    iterationsProgress: boolean;
    lastModifiedBy: string;
    lastModifiedByName: string;
    lastModifiedDate: string;
    lastSchedulingDate: string;
    lastSchedulingResult: {
        console: string[];
        deviceId: string;
        execId: string;
        exitCode: string;
        message: string;
        results: [];
        success: boolean
    }[];
    meterMessage: string;
    meterStatus: string;
    name: string;
    no: string;
    pipelineTargetMappings: { [key: string]: string[] };
    plugin: string;
    priority: string;
    projectId: string;
    reportInterval: {
        unit: string;
        value: string
    };
    sampleContents: {
        content: { [key: string]: any };
        extField: string;
        finish: boolean;
        iteration: string;
        key: string;
        name: string;
        nodeId: string;
        timestamp: string;
        timestamp0: string
    }[];
    sampleSummaryInfo: {
        brps: string;
        bwps: string;
        duration: string;
        endTime: string;
        errorRate: string;
        errors: string;
        extCounter1: string;
        extCounter2: string;
        extCounter3: string;
        extGauge1: string;
        extGauge2: string;
        extGauge3: string;
        finish: boolean;
        iterations: string;
        n: string;
        name: string;
        operations: string;
        ops: string;
        readBytes: string;
        startTime: string;
        threadMaxPoolSize: string;
        threadPoolActiveSize: string;
        threadPoolSize: string;
        threadRunning: boolean;
        threadTerminated: boolean;
        tps: string;
        tranMax: string;
        tranMean: string;
        tranMin: string;
        tranP50: string;
        tranP75: string;
        tranP90: string;
        tranP95: string;
        tranP99: string;
        tranP999: string;
        transactions: string;
        uploadResultBytes: string;
        uploadResultProgress: string;
        uploadResultTotalBytes: string;
        writeBytes: string
    };
    schedulingNum: string;
    scriptId: string;
    scriptName: string;
    scriptSource: string;
    scriptSourceId: string;
    scriptSourceName: string;
    scriptType: {
        value: 'MOCK_APIS' | 'MOCK_DATA' | 'TEST_CUSTOMIZATION' | 'TEST_FUNCTIONALITY' | 'TEST_PERFORMANCE' | 'TEST_STABILITY';
        message: string;
    };
    singleTargetPipeline: boolean;
    startAtDate: string;
    startMode: string;
    status: {
        value: string;
        message: string;
    };
    syncTestResult: boolean;
    syncTestResultFailure: string;
    task: {
        arguments: { [key: string]: string };
        mockApis:
        {
            assertive: boolean;
            description: string;
            endpoint: string;
            method: string;
            mockServiceId: string;
            mockServiceUrl: string;
            name: string;
            responses: {
                content: {
                    content: string;
                    contentEncoding: string;
                    delay: {
                        fixedTime: {
                            unit: string;
                            value: string
                        };
                        maxRandomTime: {
                            unit: string;
                            value: string
                        };
                        minRandomTime: {
                            unit: string;
                            value: string
                        };
                        mode: string
                    };
                    headers: {
                        name: string;
                        value: string
                    }[];
                    status: string
                };
                match: {
                    body: {
                        condition: string;
                        expected: string;
                        expression: string
                    };
                    parameters: {
                        condition: string;
                        expected: string;
                        expression: string;
                        in: string;
                        name: string
                    }[];
                    path: {
                        condition: string;
                        expected: string;
                        expression: string
                    };
                    priority: string
                };
                name: string;
                pushback: {
                    autoPush: boolean;
                    body: {
                        contentEncoding: string;
                        fileName: string;
                        format: string;
                        forms: {
                            contentEncoding: string;
                            contentType: string;
                            description: string;
                            enabled: boolean;
                            fileName: string;
                            format: string;
                            name: string;
                            type: string;
                            value: string
                        }[];
                        rawContent: string
                    };
                    delay: {
                        fixedTime: {
                            unit: string;
                            value: string
                        };
                        maxRandomTime: {
                            unit: string;
                            value: string
                        };
                        minRandomTime: {
                            unit: string;
                            value: string
                        };
                        mode: string
                    };
                    method: string;
                    parameters: {
                        description: string;
                        enabled: boolean;
                        format: string;
                        in: string;
                        name: string;
                        type: string;
                        value: string
                    }[];
                    url: string
                }
            }[]
        }[];
        mockData: {
            assertive: boolean;
            description: string;
            fields: {
                name: string;
                type: string;
                value: string
            }[

            ];
            name: string;
            settings: {
                batchRows: string;
                location: string;
                rows: string;
                storeRequest: {
                    authentication: {
                        apiKeys: {
                            in: string;
                            name: string;
                            value: string
                        }[];
                        description: string;
                        enabled: boolean;
                        name: string;
                        oauth2: {
                            authFlow: string;
                            clientCredentials: {
                                clientId: string;
                                clientIn: string;
                                clientSecret: string;
                                password: string;
                                refreshUrl: string;
                                scopes: string[];
                                tokenUrl: string;
                                username: string
                            };
                            newToken: boolean;
                            password: {
                                clientId: string;
                                clientIn: string;
                                clientSecret: string;
                                password: string;
                                refreshUrl: string;
                                scopes: string[];
                                tokenUrl: string;
                                username: string
                            };
                            token: string
                        };
                        type: string;
                        value: string
                    };
                    body: {
                        contentEncoding: string;
                        fileName: string;
                        format: string;
                        forms: {
                            contentEncoding: string;
                            contentType: string;
                            description: string;
                            enabled: boolean;
                            fileName: string;
                            format: string;
                            name: string;
                            type: string;
                            value: string
                        }[

                        ];
                        rawContent: string
                    };
                    contentType: string;
                    parameters: {
                        description: string;
                        enabled: boolean;
                        format: string;
                        in: string;
                        name: string;
                        type: string;
                        value: string
                    }[

                    ];
                    setting: {
                        connectTimeout: {
                            unit: string;
                            value: string
                        };
                        maxRedirects: string;
                        readTimeout: {
                            unit: string;
                            value: string
                        };
                        retryNum: string
                    };
                    url: string
                }
            }
        };
        pipelines: [
            {
                actionOnEOF: string;
                assertive: boolean;
                datasets: [
                    {
                        description: string;
                        extractable: boolean;
                        extraction: {
                            defaultValue: string;
                            expression: string;
                            failureMessage: string;
                            finalValue: string;
                            matchItem: string;
                            method: string;
                            name: string;
                            value: string
                        };
                        name: string;
                        parameters: {
                            name: string;
                            value: string
                        }[

                        ]
                    }
                ];
                description: string;
                enabled: boolean;
                mockFunctionTokens: {
                    [key: string]: {
                        identifier: string;
                        params: {
                            additionalProp1: string;
                            additionalProp2: string;
                            additionalProp3: string
                        }
                    }[

                    ];
                };
                mockTokens: {
                    additionalProp1: string;
                    additionalProp2: string;
                    additionalProp3: string
                };
                name: string;
                sharingMode: string
            }
        ];
        setup: {
            clearTestDir: boolean;
            clearTestFiles: string[

            ];
            httpRequests:
            {
                description: string;
                enabled: boolean;
                name: string;
                request: {
                    authentication: {
                        apiKeys: {
                            in: string;
                            name: string;
                            value: string
                        }[];
                        description: string;
                        enabled: boolean;
                        name: string;
                        oauth2: {
                            authFlow: string;
                            clientCredentials: {
                                clientId: string;
                                clientIn: string;
                                clientSecret: string;
                                password: string;
                                refreshUrl: string;
                                scopes: string[];
                                tokenUrl: string;
                                username: string
                            };
                            newToken: boolean;
                            password: {
                                clientId: string;
                                clientIn: string;
                                clientSecret: string;
                                password: string;
                                refreshUrl: string;
                                scopes: string[];
                                tokenUrl: string;
                                username: string
                            };
                            token: string
                        };
                        type: string;
                        value: string
                    };
                    body: {
                        contentEncoding: string;
                        fileName: string;
                        format: string;
                        forms: {
                            contentEncoding: string;
                            contentType: string;
                            description: string;
                            enabled: boolean;
                            fileName: string;
                            format: string;
                            name: string;
                            type: string;
                            value: string
                        }[];
                        rawContent: string
                    };
                    endpoint: string;
                    method: string;
                    parameters: {
                        description: string;
                        enabled: boolean;
                        format: string;
                        in: string;
                        name: string;
                        type: string;
                        value: string
                    }[

                    ];
                    server: {
                        description: string;
                        url: string;
                        variables: {
                            [key: string]: {
                                allowableValues: string[

                                ];
                                defaultValue: string;
                                description: string
                            }
                        }
                    };
                    url: string;
                    validSecurity: boolean
                }
            }[];
            runFunctions: string[];
            runNativeCommands: string[]
        };
        tearDown: {
            clearTestDir: boolean;
            clearTestFiles: string[];
            httpRequests: {
                description: string;
                enabled: boolean;
                name: string;
                request: {
                    authentication: {
                        apiKeys: {
                            in: string;
                            name: string;
                            value: string
                        }[];
                        description: string;
                        enabled: boolean;
                        name: string;
                        oauth2: {
                            authFlow: string;
                            clientCredentials: {
                                clientId: string;
                                clientIn: string;
                                clientSecret: string;
                                password: string;
                                refreshUrl: string;
                                scopes: string[];
                                tokenUrl: string;
                                username: string
                            };
                            newToken: boolean;
                            password: {
                                clientId: string;
                                clientIn: string;
                                clientSecret: string;
                                password: string;
                                refreshUrl: string;
                                scopes: string[

                                ];
                                tokenUrl: string;
                                username: string
                            };
                            token: string
                        };
                        type: string;
                        value: string
                    };
                    body: {
                        contentEncoding: string;
                        fileName: string;
                        format: string;
                        forms: {
                            contentEncoding: string;
                            contentType: string;
                            description: string;
                            enabled: boolean;
                            fileName: string;
                            format: string;
                            name: string;
                            type: string;
                            value: string
                        }[];
                        rawContent: string
                    };
                    endpoint: string;
                    method: string;
                    parameters: {
                        description: string;
                        enabled: boolean;
                        format: string;
                        in: string;
                        name: string;
                        type: string;
                        value: string
                    }[];
                    server: {
                        description: string;
                        url: string;
                        variables: {
                            [key: string]: {
                                allowableValues: string[];
                                defaultValue: string;
                                description: string
                            }
                        }
                    };
                    url: string;
                    validSecurity: boolean
                }
            }[];
            runFunctions: string[];
            runNativeCommands: string[]
        }
    };
    thread: string;
    trial: boolean;
    updateTestResult: boolean
};

export type ReportInfo = {
    auth: boolean;
    basicInfoSetting: {
        otherInformation: string;
        reportContacts: string;
        reportCopyright: string;
        watermark: string
    };
    category: string;
    contentSetting: {
        catalogContent: { [key: string]: string };
        filter: {
            createdDateEnd: string;
            createdDateStart: string;
            creatorObjectId: string;
            creatorObjectType: string;
            planOrSprintId: string;
            targetId: string;
            targetType: { value: 'API' | 'API_CASE' | 'DATASET' | 'EXECUTION' | 'FUNC_CASE' | 'FUNC_PLAN' | 'MOCK_APIS' | 'MOCK_SERVICE' | 'MODULE' | 'PROJECT' | 'REPORT' | 'SCENARIO' | 'SCRIPT' | 'SERVICE' | 'TAG' | 'TASK' | 'TASK_SPRINT' | 'VARIABLE'; message: string; };
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
            second: string
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
    template: { value: 'APIS_TESTING_RESULT' | 'EXEC_CUSTOMIZATION_RESULT' | 'EXEC_FUNCTIONAL_RESULT' | 'EXEC_PERFORMANCE_RESULT' | 'EXEC_STABILITY_RESULT' | 'FUNC_TESTING_CASE' | 'FUNC_TESTING_PLAN' | 'PROJECT_ACTIVITY' | 'PROJECT_DATA_ASSETS' | 'PROJECT_EFFICIENCY' | 'PROJECT_PROGRESS' | 'SCENARIO_TESTING_RESULT' | 'SERVICES_TESTING_RESULT' | 'TASK' | 'TASK_SPRINT' | 'TEST_EVALUATION'; message: string; };
    tenantId: string;
    tenantName: string;
    version: string
}

export type ReportContent = {
    content: { [key: string]: any };
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

export type SummaryListItem = {
    values: {
        id?: string;// 前端自动添加
        duration: string;
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
        name: string;
        maxOps: string;
        minOps: string;
        meanOps: string;
        minTps: string;
        maxTps: string;
        meanTps: string;
        minBrps: string;
        maxBrps: string;
        meanBrps: string;
        minBwps: string;
        maxBwps: string;
        meanBwps: string
    }[];
    timestamp: string;
}

export const chartSeriesColorConfig = {
  0: '84,112,198',
  1: '145,204,117',
  2: '250,200,88',
  3: '238,102,102',
  4: '115,192,222',
  5: '59,162,114',
  6: '252,132,82',
  7: '154,96,180',
  8: '234,124,204',
  9: '166, 206, 255',
  10: '162, 222, 236',
  11: '45, 142, 255',
  12: '127, 145, 255',
  13: '251, 129, 255',
  14: '255, 102, 0',
  15: '255, 165, 43',
  16: '118, 196, 125',
  17: '217, 217, 217',
  18: '245, 34, 45',
  19: '255, 185, 37',
  20: '201, 119, 255',
  21: '191, 199, 255',
  22: '103, 215, 255',
  23: '127, 91, 72',
  24: '57, 129, 184',
  25: '228, 112, 57',
  26: '165, 1135, 106',
  27: '169, 104, 55',
  28: '134, 97, 151',
  29: '178, 88, 131',
  30: '180, 128, 48',
  31: '227, 220, 155',
  32: '117, 118, 148'
};

export const bigSequence = [
  t('reportPreview.serial.1'),
  t('reportPreview.serial.2'),
  t('reportPreview.serial.3'),
  t('reportPreview.serial.4'),
  t('reportPreview.serial.5'),
  t('reportPreview.serial.6'),
  t('reportPreview.serial.7'),
  t('reportPreview.serial.8'),
  t('reportPreview.serial.9'),
  t('reportPreview.serial.10'),
  t('reportPreview.serial.11'),
  t('reportPreview.serial.12'),
  t('reportPreview.serial.13'),
  t('reportPreview.serial.14'),
  t('reportPreview.serial.15'),
  t('reportPreview.serial.16'),
  t('reportPreview.serial.17'),
  t('reportPreview.serial.18'),
  t('reportPreview.serial.19'),
  t('reportPreview.serial.20')
];
export const smallSequnce = [
  ['1.1', '1.2', '1.3', '1.4', '1.5', '1.6', '1.7', '1.8', '1.9', '1.10', '1.11', '1.12'],
  ['2.1', '2.2', '2.3', '2.4', '2.5', '2.6', '2.7', '2.8', '2.9', '2.10', '2.11', '2.12'],
  ['3.1', '3.2', '3.3', '3.4', '3.5', '3.6', '3.7', '3.8', '3.9', '3.10', '3.11', '3.12'],
  ['4.1', '4.2', '4.3', '4.4', '4.5', '4.6', '4.7', '4.8', '4.9', '4.10', '4.11', '4.12'],
  ['5.1', '5.2', '5.3', '5.4', '5.5', '5.6', '5.7', '5.8', '5.9', '5.10', '5.11', '5.12'],
  ['6.1', '6.2', '6.3', '6.4', '6.5', '6.6', '6.7', '6.8', '6.9', '6.10', '6.11', '6.12'],
  ['7.1', '7.2', '7.3', '7.4', '7.5', '7.6', '7.7', '7.8', '7.9', '7.10', '7.11', '7.12'],
  ['8.1', '8.2', '8.3', '8.4', '8.5', '8.6', '8.7', '8.8', '8.9', '8.10', '8.11', '8.12'],
  ['9.1', '9.2', '9.3', '9.4', '9.5', '9.6', '9.7', '9.8', '9.9', '9.10', '9.11', '9.12'],
  ['10.1', '10.2', '10.3', '10.4', '10.5', '10.6', '10.7', '10.8', '10.9', '10.10', '10.11', '10.12'],
  ['11.1', '11.2', '11.3', '11.4', '11.5', '11.6', '11.7', '11.8', '11.9', '11.10', '11.11', '11.12'],
  ['12.1', '12.2', '12.3', '12.4', '12.5', '12.6', '12.7', '12.8', '12.9', '12.10', '12.11', '12.12'],
  ['13.1', '13.2', '13.3', '13.4', '13.5', '13.6', '13.7', '13.8', '13.9', '13.10', '13.11', '13.12'],
  ['14.1', '14.2', '14.3', '14.4', '14.5', '14.6', '14.7', '14.8', '14.9', '14.10', '14.11', '14.12'],
  ['15.1', '15.2', '15.3', '15.4', '15.5', '15.6', '15.7', '15.8', '15.9', '15.10', '15.11', '15.12'],
  ['16.1', '16.2', '16.3', '16.4', '16.5', '16.6', '16.7', '16.8', '16.9', '16.10', '16.11', '16.12'],
  ['17.1', '17.2', '17.3', '17.4', '17.5', '17.6', '17.7', '17.8', '17.9', '17.10', '17.11', '17.12']
];
