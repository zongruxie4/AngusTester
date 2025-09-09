export type AssertionCondition = 'CONTAIN' | 'EQUAL' | 'GREATER_THAN' | 'GREATER_THAN_EQUAL' | 'IS_EMPTY' | 'IS_NULL' | 'JSON_PATH_MATCH' | 'LESS_THAN' | 'LESS_THAN_EQUAL' | 'NOT_CONTAIN' | 'NOT_EMPTY' | 'NOT_EQUAL' | 'NOT_NULL' | 'REG_MATCH' | 'XPATH_MATCH'

export type LdapInfo = {
    id: string;// 前端自动生成，用于给每条记录添加id
    target: 'LDAP';
    name: string;
    description: string;
    enabled: boolean;
    beforeName: string;
    server: {
        server: string;
        port: string;
        username: string;// length - 400
        password: string;// length - 4096
        rootDn: string;// length - 4096
    };
    testType: {
        value: 'ADD' | 'MODIFY' | 'DELETE' | 'SEARCH';
        message: string;
    };
    userDefined: boolean;
    entryDn: string;// length - 4096
    arguments: {
        [key: string]: string;
    };
    searchBase: string;// length - 4096
    searchFilter: string;// length - 4096
    deleteEntry: string;// length - 4096
    linkName?: string;
}
