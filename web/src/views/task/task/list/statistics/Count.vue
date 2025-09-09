<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { Icon } from '@xcan-angus/vue-ui';

import { ResourceInfo } from '../../types';

type Props = {
    dataSource: ResourceInfo;
}

const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined
});
</script>

<template>
  <div class="flex flex-wrap leading-5 text-3">
    <div class="section-item">
      <Icon class="flex-shrink-0 text-9" icon="icon-zonglan" />
      <div class="ml-3">
        <div class="text-3.75">
          <span>{{ props.dataSource?.progress?.replace(/(\d+\.\d{2})\d+/,'$1') }}</span>
          <span class="text-3.5">%</span>
        </div>
        <div class="flex-shrink-0 whitespace-nowrap text-theme-sub-content">{{ t('task.list.statistics.progress') }}</div>
      </div>
    </div>

    <div class="section-item">
      <Icon class="flex-shrink-0 text-9" icon="icon-meimiaoshiwushu" />
      <div class="ml-3">
        <div class="text-3.75"> {{ props.dataSource?.totalTaskNum }} </div>
        <div class="flex-shrink-0 whitespace-nowrap text-theme-sub-content">{{ t('task.list.statistics.totalTasks') }}</div>
      </div>
    </div>

    <div class="section-item">
      <Icon class="flex-shrink-0 text-9" icon="icon-daiceshi" />
      <div class="ml-3">
        <div class="text-3.75"> {{ props.dataSource?.pendingNum }} </div>
        <div class="flex-shrink-0 whitespace-nowrap text-theme-sub-content">{{ t('task.list.statistics.pendingTasks') }}</div>
      </div>
    </div>

    <div class="section-item">
      <Icon class="flex-shrink-0 text-9" icon="icon-renwuceshizhong" />
      <div class="ml-3">
        <div class="text-3.75"> {{ props.dataSource?.inProgressNum }} </div>
        <div class="flex-shrink-0 whitespace-nowrap text-theme-sub-content">{{ t('task.list.statistics.inProgressTasks') }}</div>
      </div>
    </div>

    <div class="section-item">
      <Icon class="flex-shrink-0 text-9" icon="icon-daiqueren" />
      <div class="ml-3">
        <div class="text-3.75"> {{ props.dataSource?.confirmingNum }} </div>
        <div class="flex-shrink-0 whitespace-nowrap text-theme-sub-content">{{ t('task.list.statistics.confirmingTasks') }}</div>
      </div>
    </div>

    <div class="section-item">
      <Icon class="flex-shrink-0 text-9" icon="icon-ceshiwancheng" />
      <div class="ml-3">
        <div class="text-3.75"> {{ props.dataSource?.completedNum }} </div>
        <div class="flex-shrink-0 whitespace-nowrap text-theme-sub-content">{{ t('task.list.statistics.completedTasks') }}</div>
      </div>
    </div>

    <div class="section-item">
      <Icon class="flex-shrink-0 text-9" icon="icon-yiquxiao" />
      <div class="ml-3">
        <div class="text-3.75"> {{ props.dataSource?.canceledNum }} </div>
        <div class="flex-shrink-0 whitespace-nowrap text-theme-sub-content">{{ t('task.list.statistics.canceledTasks') }}</div>
      </div>
    </div>

    <div class="section-item">
      <Icon class="flex-shrink-0 text-9" icon="icon-yiyuqi1" />
      <div class="ml-3">
        <div class="text-3.75"> {{ props.dataSource?.overdueNum }} </div>
        <div class="flex-shrink-0 whitespace-nowrap text-theme-sub-content">{{ t('task.list.statistics.overdueTasks') }}</div>
      </div>
    </div>

    <div class="section-item">
      <Icon class="flex-shrink-0 text-9" icon="icon-yiceshiyonglishu" />
      <div class="ml-3">
        <div class="text-3.75"> {{ props.dataSource?.evalWorkload }} </div>
        <div class="flex-shrink-0 whitespace-nowrap text-theme-sub-content">{{ t('task.list.statistics.evalWorkload') }}</div>
      </div>
    </div>

    <div class="section-item">
      <Icon class="flex-shrink-0 text-9" icon="icon-chenggongshu" />
      <div class="ml-3">
        <div class="text-3.75"> {{ props.dataSource?.completedWorkload }} </div>
        <div class="flex-shrink-0 whitespace-nowrap text-theme-sub-content">{{ t('task.list.statistics.completedWorkload') }}</div>
      </div>
    </div>

    <div class="section-item">
      <Icon class="flex-shrink-0 text-9" icon="icon-yicixingtongguoshu" />
      <div class="ml-3">
        <div class="text-3.75"> {{ props.dataSource?.oneTimePassedNum }} </div>
        <div class="flex-shrink-0 whitespace-nowrap text-theme-sub-content">{{ t('task.list.statistics.oneTimePassedNum') }}</div>
      </div>
    </div>

    <div class="section-item">
      <Icon class="flex-shrink-0 text-9" icon="icon-yicitongguoshuai" />
      <div class="ml-3">
        <div class="text-3.75">
          <span>{{ props.dataSource?.oneTimePassedRate?.replace(/(\d+\.\d{2})\d+/,'$1') }}</span>
          <span class="text-3.5">%</span>
        </div>
        <div class="flex-shrink-0 whitespace-nowrap text-theme-sub-content">{{ t('task.list.statistics.oneTimePassedRate') }}</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.section-item {
    display: flex;
    flex-wrap: nowrap;
    align-items: center;
    margin-right: 8px;
    margin-bottom: 8px;
    padding: 10px;
    border-radius: 8px;
    background-color: rgb(252, 250, 255);
}

@media screen and (max-width: 1550px) {
    .section-item {
        width: calc((100% - 32px)/4);
    }
}

@media screen and (min-width: 1551px) {
    .section-item {
        width: calc((100% - 48px)/6);
    }
}
</style>
