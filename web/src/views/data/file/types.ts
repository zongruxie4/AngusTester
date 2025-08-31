import { i18n } from '@xcan-angus/infra';

const t = i18n.getI18n()?.global?.t || ((value: string) => value);

export const columns = [
  { title: t('fileSpace.columns.name'), dataIndex: 'name', width: '30%', ellipsis: true },
  {
    title: t('fileSpace.columns.fileCount'),
    dataIndex: 'fileNum',
    width: 60,
    ellipsis: true
  },
  {
    title: t('fileSpace.columns.folderCount'),
    dataIndex: 'subDirectoryNum',
    width: 80,
    ellipsis: true
  },
  {
    title: t('fileSpace.columns.actualSize'), dataIndex: 'size', ellipsis: true, width: 80
  },
  { title: t('fileSpace.columns.lastModifiedDate'), dataIndex: 'lastModifiedDate', ellipsis: true, width: 160 },
  {
    title: t('fileSpace.columns.action'), dataIndex: 'action', width: 380
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
      subFileNum: number | string,
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
  auth: string[];
  quotaSize?: {
    value: string;
    unit: {
      value: string;
      message: string;
    }
  };
  size: string;
  subDirectoryNum: string;
  subFileNum: string;
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
