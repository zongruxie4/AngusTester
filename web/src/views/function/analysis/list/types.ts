export const TemplateIconConfig = {
  PROGRESS: 'icon-jindufenxi',
  BURNDOWN: 'icon-ranjinfenxi',
  WORKLOAD: 'icon-gongzuoliangfenxi',
  OVERDUE_ASSESSMENT: 'icon-yuqipinggufenxi',
  BUGS: 'icon-guzhangfenxi',
  SUBMITTED_BUGS: 'icon-guzhangfenxi',
  HANDLING_EFFICIENCY: 'icon-chulixiaoshuaifenxi',
  CORE_KPI: 'icon-hexinkpifenxi',
  FAILURES: 'icon-quexianfenxi',
  BACKLOG_TASKS: 'icon-jiyarenwufenxi',
  RECENT_DELIVERY: 'icon-jinqijiaofuliangfenxi',
  LEAD_TIME: 'icon-jiaofuzhouqifenxi',
  UNPLANNED_TASKS: 'icon-jihuawairenwufenxi',
  TASK_GROWTH_TREND: 'icon-renwuzengchangqushifenxi',
  RESOURCE_CREATION: 'icon-ziyuanchuangjianfenxi',
  REVIEW_EFFICIENCY: 'icon-chulixiaoshuaifenxi',
  TESTING_EFFICIENCY: 'icon-chulixiaoshuaifenxi',
  BACKLOG_CASES: 'icon-jiyarenwufenxi',
  UNPLANNED_CASES: 'icon-jihuawairenwufenxi',
  CASE_GROWTH_TREND: 'icon-renwuzengchangqushifenxi',
  '': 'icon-liebiaoshitu'
};

export type MenuItem = {
  key: 'none' | 'createdBy' | 'lastModifiedBy' | 'last1Day' | 'last3Days' | 'last7Days';
  name: string;
}
