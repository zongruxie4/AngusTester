export type FormState = {
    name: string | undefined;
    type: 'MOCK_APIS' | 'MOCK_DATA' | 'TEST_CUSTOMIZATION' | 'TEST_FUNCTIONALITY' | 'TEST_PERFORMANCE' | 'TEST_STABILITY' | undefined,
    typeName: string | undefined;
    description: string | undefined;
}
