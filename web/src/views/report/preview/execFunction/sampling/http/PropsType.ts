export type ExecInfo = {
    id: string;
    name: string;
    plugin: string;
    scriptType: {
        value: string;
        message: string;
    };
    scriptId: string;
    scriptName: string;
    status: {
        value: string;
        message: string;
    };
    syncTestResult: boolean;
    thread: string;
    priority: string;
    ignoreAssertions: boolean;
    iterations:string;
    updateTestResult: boolean;
    reportInterval: string;
    execNodes: {
        id: string;
        name: string;
        ip: string;
        domain: '';
        agentPort: string;
    }[];
    availableNodes: {
        id: string;
        name: string;
        ip: string;
        domain: '';
        agentPort: string;
    }[];
    configuration: {
        thread: {
            threads: string;
        };
        iterations:string;
        priority: string;
        nodeSelectors: {
            availableNodeIds: string[]
        }
    };
    task: {
        arguments: {
            httpSetting: {
                connectTimeout: string;
                readTimeout: string;
                retryNum: string;
                maxRedirects: string;
            };
            updateTestResult: boolean;
            ignoreAssertions: boolean
        };
        pipelines: {
            target: 'http' | 'TRANS_START' | 'TRANS_END' | 'RENDEZVOUS' | 'WAITING_TIME' | 'THROUGHPUT';
            name: string;
            transactionName: string;
            description: string;
            enabled: boolean;
            children:ExecInfo['task']['pipelines'];
            request: {
                method: string;
                url: string;
                parameters: {
                    name: string;
                    in: string;
                    description: string;
                    enabled: boolean;
                    type: string;
                    value: string;
                }[];
                body: {
                    type: string;
                    rawContent: string;
                };
                validSecurity: boolean
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
                enabled: boolean;
                success: boolean;
                ignore: boolean
            }[];
            kind: string;
        }[];
    };
    pipelineTargetMappings: {[key:string]:any[]};
    actualStartDate: string;
    endDate: string;
    singleTargetPipeline: boolean;
    execBy: string;
    execByName: string;
    createdBy: string;
    creator: string;
    createdDate: string;
    modifiedBy: string;
    modifier: string;
    modifiedDate: string;
    sampleSummaryInfo: {
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
    hasOperationPermission: boolean;
    schedulingNum: string;
    lastSchedulingDate: string;
    lastSchedulingResult: {
        success: boolean;
        message: string;
        execId: string;
        console: string[];
        exitCode: string;
        deviceId: string;
    }[];
    durationProgress: boolean;
    currentDuration: string;
    currentDurationProgress: string;
    iterationsProgress: boolean;
    currentIterations: string;
    currentIterationsProgress: string;
    result: string;
}

export type AssertCondition = 'CONTAIN' |
    'EQUAL' |
    'GREATER_THAN' |
    'GREATER_THAN_EQUAL' |
    'IS_EMPTY' |
    'IS_NULL' |
    'LESS_THAN' |
    'LESS_THAN_EQUAL' |
    'NOT_CONTAIN' |
    'NOT_EMPTY' |
    'NOT_EQUAL' |
    'NOT_NULL' |
    'REG_MATCH' |
    'XPATH_MATCH' |
    'JSON_PATH_MATCH';

export type ExecContent = {
    finish: boolean;
    nodeId: string;
    timestamp0: string;
    name: string;
    extField: string;
    iteration: string;
    key: string;
    content: {
        name: string;
        success: boolean;
        failMessage: string;
        request0: {
            method: string;
            url: string;
            headers: {[key:string]:string};
            forms: string;
            rawContent: string;
            size: string;
            headerArray: string[];
            queryString:string;
        };
        response: {
            status: string;
            size: string;
            bodySize: string;
            headers: {[key:string]:string};
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
            };
            headerArray: string[];
        };
        assertions: {
            name: string;
            enabled: boolean;
            type: {
                value: 'STATUS' | 'HEADER' | 'BODY' | 'BODY_SIZE' | 'SIZE' | 'DURATION';
                message: string;
            };
            assertionCondition: {
                value: AssertCondition;
                message: string;
            };
            expected: string;
            enabled: boolean;
            success: boolean;
            ignore: boolean
            actualExpected: string;
            condition?: string;
            actualCondition?: string;
            parameterName: string;
            result?: {
                failure: boolean;
                message: string;
            };
            extraction?:{
                name: string;
                method: {
                    value: string;
                    message: string;
                },
                expression: string;
                matchItem: string;
                defaultValue:string;
                location: string;
                parameterName: string;
                value: string;
                finalValue: string;
            }
        }[];
    };
    timestamp: string;
}
