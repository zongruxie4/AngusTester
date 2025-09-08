/**
 * <p>Resource information interface for scenario statistics.</p>
 * <p>Contains comprehensive scenario count data organized by various dimensions.</p>
 * <p>Used by chart components to display scenario distribution and statistics.</p>
 */
export type ResourceInfo = {
    /**
     * <p>Total number of all scenarios in the project.</p>
     * <p>String format to handle large numbers and API compatibility.</p>
     */
    allSce: string;

    /**
     * <p>Number of scenarios created in the last week.</p>
     * <p>Used for tracking recent activity and growth trends.</p>
     */
    sceByLastWeek: string;

    /**
     * <p>Number of scenarios created in the last month.</p>
     * <p>Used for tracking medium-term activity and growth trends.</p>
     */
    sceByLastMonth: string;

    /**
     * <p>Scenario counts categorized by script/test type.</p>
     * <p>Provides breakdown of scenarios by their functional purpose.</p>
     */
    sceByScriptType: {
        /** Number of mock API scenarios */
        MOCK_APIS: string;
        /** Number of mock data scenarios */
        MOCK_DATA: string;
        /** Number of custom test scenarios */
        TEST_CUSTOMIZATION: string;
        /** Number of functional test scenarios */
        TEST_FUNCTIONALITY: string;
        /** Number of performance test scenarios */
        TEST_PERFORMANCE: string;
        /** Number of stability test scenarios */
        TEST_STABILITY: string;
    };

    /**
     * <p>Scenario counts categorized by plugin name.</p>
     * <p>Dynamic object where keys are plugin names and values are scenario counts.</p>
     * <p>Used for displaying plugin-specific scenario distribution in bar charts.</p>
     */
    sceByPluginName: { [key: string]: string; };
}
