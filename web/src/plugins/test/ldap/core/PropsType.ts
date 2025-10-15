
export type PipelineConfig = {
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
    testType: 'ADD' | 'MODIFY' | 'DELETE' | 'SEARCH';
    userDefined: boolean;
    entryDn: string;// length - 4096
    arguments: {
        [key: string]: string;
    };
    searchBase: string;// length - 4096
    searchFilter: string;// length - 4096
    deleteEntry: string;// length - 4096
    id: string;// 前端自动生成，用于给每条记录添加id
}

export type LdapPipelineInfo = PipelineConfig;

