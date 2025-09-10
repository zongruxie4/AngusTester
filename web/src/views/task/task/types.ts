import { EnumMessage, Priority, EvalWorkloadMethod } from '@xcan-angus/infra';
import { TaskStatus, TaskType, TestType, ExecStatus, ExecResult } from '@/enums/enums';

export type TaskInfo = {
  id: string;
  name: string;
  code: string;
  projectId: string;
  sprintId: string;
  sprintName: string;
  targetId: string;
  targetParentId: string;
  taskType: EnumMessage<TaskType>;
  testType: EnumMessage<TestType>;
  deadlineDate: string;
  assigneeId: string;
  assigneeName: string;
  priority: EnumMessage<Priority>;
  evalWorkloadMethod: EnumMessage<EvalWorkloadMethod>;
  status: EnumMessage<TaskStatus>;
  execResult: EnumMessage<ExecResult>;
  execFailureMessage: string;
  execTestNum: string;
  execTestFailureNum: string;
  execId: string;
  execName: string;
  execBy: string;
  execByName: string;
  execDate: string;
  failNum: string;
  totalNum: string;
  overdue: false;
  createdBy: string;
  createdByName: string;
  createdDate: string;
}

export type StatisticsInfo = {
    actualWorkload: string;
    apiTestNum: string;
    bugNum: string;
    canceledNum: string;
    completedNum: string;
    completedWorkload: string;
    confirmingNum: string;
    evalWorkload: string;
    functionalNum: string;
    inProgressNum: string;
    oneTimePassedNum: string;
    oneTimePassedRate: string;
    overdueNum: string;
    pendingNum: string;
    perfNum: string;
    processFailTimes: string;
    processTimes: string;
    requirementNum: string;
    scenarioTestNum: string;
    stabilityNum: string;
    storyNum: string;
    taskNum: string;
    testFailNum: string;
    testSuccessNum: string;
    totalStatusNum: string;
    totalTaskNum: string;
    totalTaskTypeNum: string;
    totalTestTypeNum: string;
    validTaskNum: string;
    progress: string;
}

export type ActionMenuItem = {
    name: string;
    key: 'delete' | 'edit' | 'start' | 'processed' | 'uncompleted' | 'completed' | 'reopen' | 'restart' | 'cancel' | 'move' | 'cancelFavourite' | 'favourite' | 'cancelFollow' | 'follow' | 'copyLink';
    icon: string;
    disabled: boolean;
    hide: boolean;
    tip?: string;
}

export type TreeData = {
    name: string;
    sequece: string;
    level: number;
    children?: TreeData[];
    id: string;
    pid?: string;
    index: number;
    ids: string[];
    isLast: boolean;
  }

// TODO 递归修改为循环
export const travelTreeData = (treeData, callback = (item) => item) => {
  function travel (treeData, level = 0, ids: string[] = []) {
    treeData.forEach((item, idx) => {
      item.level = level;
      item.index = idx;
      item.ids = [...ids, item.id];
      item.isLast = idx === (treeData.length - 1);
      // eslint-disable-next-line no-unused-expressions
      travel(item.children || [], level + 1, item.ids),
      item.childLevels = (item.children?.length ? Math.max(...item.children.map(i => i.childLevels)) : 0) + 1;
      item = callback(item);
    });
  }
  travel(treeData);
  return treeData;
};

export type searchPanelOption = {
  id: string;
  name: string;
  showTitle: string;
  showName: string;
}

export type SearchPanelMenuItem = {
  key: 'none' | 'createdBy' | 'assigneeId' | 'progress' | 'confirmorId' | 'lastDay' | 'lastThreeDays' | 'lastWeek' | string;
  name: string;
  groupKey?: 'assigneeId' | 'time';
}
