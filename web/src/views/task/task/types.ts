export type ResourceInfo = {
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

type TreeData = {
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
