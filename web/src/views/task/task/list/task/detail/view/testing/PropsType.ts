export type TestInfo = {
    caseResults: {
        apisId: string;
        assertionSummary: {
            disabledNum: string;
            failureNum: string;
            ignoreNum: string;
            successNum: string;
            totalNum: string;
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
        execStatus: string;
        failureMessage: string;
        id: string;
        lastExecDate: string;
        passed: boolean;
        plugin: string;
        sampleContent:
        {
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
                    extraction: { [key: string]: any };
                    ignore: boolean;
                    matchItem: string;
                    name: string;
                    parameterName: string;
                    result: {
                        failure: boolean;
                        ignored: boolean;
                        message: string;
                    };
                    success: boolean;
                    type: string;
                }[];
                failMessage: string;
                name: string;
                success: boolean;
                target: string;
            };
            timestamp: string;
            timestamp0: string;
        }[];
        scriptId: string;
        testFailureNum: string;
        testNum: string;
    }[];
    caseSummary: {
        disabledNum: string;
        failNum: string;
        successNum: string;
        totalNum: string;
    };
    createdDate: string;
    execBy: string;
    execByName: string;
    execDate: string;
    execId: string;
    execName: string;
    execStatus: {
        message: string;
        value: 'COMPLETED'|'CREATED'|'FAILED'|'PENDING'|'RUNNING'|'STOPPED'|'TIMEOUT';
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
                apisId: string;
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
                        apiKeys: {
                            in: string;
                            name: string;
                            value: string;
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
                                username: string;
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
                                username: string;
                            };
                            token: string;
                        };
                        type: string;
                        value: string;
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
                            value: string;
                        }[];
                        rawContent: string;
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
                        value: string;
                    }[];
                    server: {
                        description: string;
                        url: string;
                        variables: {
                            additionalProp1: {
                                allowableValues: string[];
                                defaultValue: string;
                                description: string;
                            };
                            additionalProp2: {
                                allowableValues: string[];
                                defaultValue: string;
                                description: string;
                            };
                            additionalProp3: {
                                allowableValues: string[];
                                defaultValue: string;
                                description: string;
                            }
                        }
                    };
                    url: string;
                    validSecurity: boolean
                };
                value: string;
            };
            ignore: boolean;
            matchItem: string;
            name: string;
            parameterName: string;
            result: {
                failure: boolean;
                ignored: boolean;
                message: string;
            };
            success: boolean;
            type: string;
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
                apisId: string;
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
                        apiKeys: {
                            in: string;
                            name: string;
                            value: string;
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
                                username: string;
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
                                username: string;
                            };
                            token: string;
                        };
                        type: string;
                        value: string;
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
                            value: string;
                        }[];
                        rawContent: string;
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
                        value: string;
                    }[];
                    server: {
                        description: string;
                        url: string;
                        variables: {
                            additionalProp1: {
                                allowableValues: string[];
                                defaultValue: string;
                                description: string;
                            };
                            additionalProp2: {
                                allowableValues: string[];
                                defaultValue: string;
                                description: string;
                            };
                            additionalProp3: {
                                allowableValues: string[];
                                defaultValue: string;
                                description: string;
                            }
                        }
                    };
                    url: string;
                    validSecurity: boolean
                };
                value: string;
            };
            ignore: boolean;
            matchItem: string;
            name: string;
            parameterName: string;
            result: {
                failure: boolean;
                ignored: boolean;
                message: string;
            };
            success: boolean;
            type: string;
        }
    };
    indicatorPerf: {
        art: string;
        duration: {
            unit: string;
            value: string;
        };
        errorRate: string;
        percentile: string;
        rampUpInterval: {
            unit: string;
            value: string;
        };
        rampUpThreads: string;
        threads: string;
        tps: string;
    };
    indicatorStability: {
        art: string;
        cpu: string;
        disk: string;
        duration: {
            unit: string;
            value: string;
        };
        errorRate: string;
        memory: string;
        network: string;
        percentile: string;
        threads: string;
        tps: string;
    };
    lastExecDate: string;
    nodeUsageSummary: {
        additionalProp1: {
            maxCpu: string;
            maxFilesystem: string;
            maxMemory: string;
            maxNetwork: string;
            meanCpu: string;
            meanFilesystem: string;
            meanMemory: string;
            meanNetwork: string;
        };
        additionalProp2: {
            maxCpu: string;
            maxFilesystem: string;
            maxMemory: string;
            maxNetwork: string;
            meanCpu: string;
            meanFilesystem: string;
            meanMemory: string;
            meanNetwork: string;
        };
        additionalProp3: {
            maxCpu: string;
            maxFilesystem: string;
            maxMemory: string;
            maxNetwork: string;
            meanCpu: string;
            meanFilesystem: string;
            meanMemory: string;
            meanNetwork: string;
        }
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
                extraction: { [key: string]: any };
                ignore: boolean;
                matchItem: string;
                name: string;
                parameterName: string;
                result: {
                    failure: boolean;
                    ignored: boolean;
                    message: string;
                };
                success: boolean;
                type: string;
            }[];
            failMessage: string;
            name: string;
            success: boolean;
            target: string;
        };
        timestamp: string;
        timestamp0: string;
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
        writeBytes: string;
    };
    scriptId: string;
    scriptName: string;
    scriptSource: string;
    scriptSourceId: string;
    scriptSourceName: string;
    scriptType: string;
    targetSummary: {
        disabledNum: string;
        failNum: string;
        successNum: string;
        totalNum: string;
    };
    testFailureNum: string;
    testNum: string;
    usageFailedNodeId: string;
};
