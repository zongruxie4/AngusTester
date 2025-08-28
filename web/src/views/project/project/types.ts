import {EnumMessage} from '@xcan-angus/infra';
import {ProjectType} from '@/enums/enums';

// Project type definition
export type Project = {
  id?: string;
  name?: string;
  description?: string;
  avatar?: string;
  ownerId?: string;
  ownerName?: string;
  type?: EnumMessage<ProjectType>;
  members?: {
    USER?: {name: string; avatar?: string}[];
    DEPT?: {name: string; avatar?: string}[];
    GROUP?: {name: string; avatar?: string}[];
  };
  membersNum?: number;
  showMembers?: {
    USER: {name: string; avatar?: string}[];
    DEPT: {name: string; avatar?: string}[];
    GROUP: {name: string; avatar?: string}[];
  };
  startDate?: string;
  deadlineDate?: string;
  createdBy?: string;
  lastModifiedDate?: string;
  // Temp fields
  dateRange?: [string, string];
  importExample?: boolean;
};

// Type definitions
export interface TreeData {
  name: string;
  sequence: string;
  level: number;
  children?: TreeData[];
  id: string;
  pid?: string;
  index?: number;
  ids?: string[];
  isLast?: boolean;
  childLevels?: number;
  disabled?: boolean;
}

