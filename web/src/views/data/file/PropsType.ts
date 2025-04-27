export const columns = [
  { title: '名称', dataIndex: 'name', width: '30%', ellipsis: true },
  {
    title: '文件数',
    dataIndex: 'fileNum',
    width: 60,
    ellipsis: true
  },
  {
    title: '文件夹数',
    dataIndex: 'subdirectoryNum',
    width: 80,
    ellipsis: true
  },
  {
    title: '实际大小', dataIndex: 'size', ellipsis: true, width: 80
  },
  { title: '修改时间', dataIndex: 'lastModifiedDate', ellipsis: true, width: 160 },
  {
    title: '操作', dataIndex: 'action', width: 380
  }
];

export const SPACE_PERMISSIONS = ['VIEW', 'MODIFY', 'DELETE', 'SHARE', 'GRANT', 'OBJECT_READ', 'OBJECT_WRITE', 'OBJECT_DELETE'];

export interface FileType {
    message:string,
    value:string
}
export interface SourceType {
    id:string,
    name:string,
    summary: {
      usedSize:number | string,
      subfileNum: number | string,
    },
    type:FileType,
    lastModifiedDate:string,
    renameFlag?:boolean,
    cacheName?:string
    parentDirectoryId?: string,
    spaceId: string
}

export interface SpaceInfoType {
  id: string;
  name: string;
  auth?: string[];
  authFlag: boolean;
  quotaSize?: {
    value: string;
    unit: {
      value: string;
      message: string;
    }
  };
  size: string;
  subdirectoryNum: string;
  subfileNum: string;
  createdByName: string;
  createdDate: string
}

export interface PaginationType {
    current: number;
    pageSize: number;
    total: number;
    hideOnSinglePage:boolean;
}

export interface SearchType {
    name?:string
}

export interface SortType{
    orderBy?: string,
    orderSort?: string
}

export interface CrumbType {
    id:string,
    name:string
}
