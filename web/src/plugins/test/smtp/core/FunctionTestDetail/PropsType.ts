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
            assertive: boolean;
            description: string;
            enabled: boolean;
            extractableVariable: boolean;
            kind: string;
            mail: {
                content: {
                    subject: string;
                    suppressSubject: boolean;
                    enableDebugLogging: boolean;
                    message: string;
                    messageSizeStatistics: boolean;
                    plainBody: boolean;
                },
                mailFrom: string;
                receiverTo: string;
            }
            name: string;
            server: {
                connectionTimeout: string;
                password: string;
                port: number;
                readTimeout: string;
                security: {
                    enforceStartTLS: boolean;
                    trustAllCerts: boolean;
                    use: {
                        value: string;
                        message: string;
                    },
                    useLocalTrustStore: boolean;
                };
                server: string;
                username: string;
                useAuth: boolean;
            }
            target: 'SMTP';
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
