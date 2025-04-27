<script setup lang="ts">
import { computed } from 'vue';

import { ReportContent } from '../PropsType';

type Props = {
  projectInfo: { [key: string]: any };
  userInfo: { [key: string]: any };
  appInfo: { [key: string]: any };
  dataSource: ReportContent;
}

const props = withDefaults(defineProps<Props>(), {
  projectInfo: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const tasksMap = computed(() => {
  const tasks = props.dataSource?.content?.tasks?.tasks || [];
  return tasks.reduce((prev, cur) => {
    const id = cur.assigneeId;
    if (prev[id]) {
      prev[id].push(cur);
    } else {
      prev[id] = [cur];
    }

    return prev;
  }, {} as {[key:string]:ReportContent['content']['tasks']['tasks']});
});

const assignees = computed(() => {
  const _assignees = props.dataSource?.content?.tasks?.assignees;
  if (!_assignees) {
    return [];
  }
  return Object.values(_assignees);
});
</script>

<template>
  <div>
    <h1 class="text-theme-title font-medium mb-5">
      <span id="a10" class="text-4 text-theme-title font-medium">三、<em class="inline-block w-0.25"></em>经办人汇总结果</span>
    </h1>

    <div
      v-for="(item,index) in assignees"
      :key="item.id"
      class="mb-7 last:mb-0">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-2.5 text-theme-title">
        <span :id="`a${index+11}`">3.{{ index+1 }}<em class="inline-block w-3.5"></em>{{ item.fullname }}</span>
      </h2>

      <div class="space-y-5">
        <div v-for="_task in tasksMap[item.id]" :key="_task.id">
          <div class="flex items-center space-x-1 mb-2  break-all whitespace-pre-wrap"><em class="block w-1 h-1 mr-1 bg-gray-500 rounded"></em><span>{{ _task.name }}</span></div>
          <div class="border border-solid border-border-input">
            <div class="flex border-b border-solid border-border-input">
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                编码
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ _task?.code }}
              </div>
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                优先级
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                {{ _task?.priority?.message }}
              </div>
            </div>

            <div class="flex border-b border-solid border-border-input">
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                所属模块
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ _task?.moduleName }}
              </div>
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                任务类型
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                {{ _task?.taskType?.message }}
              </div>
            </div>

            <div class="flex border-b border-solid border-border-input">
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                测试类型
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ _task?.testType?.message }}
              </div>
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                任务状态
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                {{ _task?.status?.message }}
              </div>
            </div>

            <div class="flex border-b border-solid border-border-input">
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                完成时间
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ _task?.completedDate }}
              </div>
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                是否逾期
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                {{ _task?.overdueFlag?'是':'否' }}
              </div>
            </div>

            <div class="flex border-b border-solid border-border-input">
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                截止时间
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ _task?.deadlineDate }}
              </div>
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                经办人
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                {{ _task?.assigneeName }}
              </div>
            </div>

            <div class="flex border-b border-solid border-border-input">
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                开始时间
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ _task?.startDate }}
              </div>
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                是否确认任务
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                {{ _task?.confirmTaskFlag?'是':'否' }}
              </div>
            </div>

            <div class="flex border-b border-solid border-border-input">
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                确认人
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ _task?.confirmorName }}
              </div>
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                工作量评估方式
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                {{ _task?.evalWorkloadMethod?.message }}
              </div>
            </div>

            <div class="flex border-b border-solid border-border-input">
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                评估工作量
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ _task?.evalWorkload }}
              </div>
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                实际工作量
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                {{ _task?.actualWorkload }}
              </div>
            </div>

            <div class="flex border-b border-solid border-border-input">
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                总处理次数
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ _task?.totalNum }}
              </div>
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                处理失败次数
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                {{ _task?.failNum }}
              </div>
            </div>

            <div class="flex border-b border-solid border-border-input">
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                添加人
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ _task?.createdByName }}
              </div>
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                添加时间
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                {{ _task?.createdDate }}
              </div>
            </div>

            <div class="flex">
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                最后修改人
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
                {{ _task?.lastModifiedByName }}
              </div>
              <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                最后修改时间
              </div>
              <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
                {{ _task?.lastModifiedDate }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
