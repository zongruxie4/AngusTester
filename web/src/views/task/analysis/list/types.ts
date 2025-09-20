export const TemplateIconConfig = {
  '': 'icon-liebiaoshitu',
  PROGRESS: 'icon-jindufenxi',
  BURNDOWN: 'icon-ranjinfenxi',
  WORKLOAD: 'icon-gongzuoliangfenxi',
  OVERDUE_ASSESSMENT: 'icon-yuqipinggufenxi',
  BUGS: 'icon-guzhangfenxi',
  HANDLING_EFFICIENCY: 'icon-chulixiaoshuaifenxi',
  CORE_KPI: 'icon-hexinkpifenxi',
  FAILURES: 'icon-quexianfenxi',
  BACKLOG_TASKS: 'icon-jiyarenwufenxi',
  RECENT_DELIVERY: 'icon-jinqijiaofuliangfenxi',
  LEAD_TIME: 'icon-jiaofuzhouqifenxi',
  UNPLANNED_TASKS: 'icon-jihuawairenwufenxi',
  TASK_GROWTH_TREND: 'icon-renwuzengchangqushifenxi',
  RESOURCE_CREATION: 'icon-ziyuanchuangjianfenxi',
  CUSTOM_DEFINITION: 'icon-zidingyifenxi'
};

export type MenuItem = {
    key: 'none' | 'createdBy' | 'lastModifiedBy' | 'lastDay' | 'lastThreeDays' | 'lastWeek';
    name: string;
}
