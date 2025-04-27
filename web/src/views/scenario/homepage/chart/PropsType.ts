export type ResourceInfo = {
    allSce: string;
    sceByLastWeek: string;
    sceByLastMonth: string;
    sceByScriptType: {
        MOCK_APIS: string;
        MOCK_DATA: string;
        TEST_CUSTOMIZATION: string;
        TEST_FUNCTIONALITY: string;
        TEST_PERFORMANCE: string;
        TEST_STABILITY: string;
    };
    sceByPluginName: { [key: string]: string; };
}
