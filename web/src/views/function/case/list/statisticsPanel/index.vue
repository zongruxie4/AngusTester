<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { Icon } from '@xcan-angus/vue-ui';
import { CountObj } from './chart/PropsType';

const { t } = useI18n();

interface Props {
  dataSource: CountObj
}
const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ({}) as CountObj
});

const topCarObj = {
  progress: {
    name: t('functionCase.statisticsPanel.progress'),
    icon: 'icon-zonglan'
  },
  totalCaseNum: {
    name: t('functionCase.statisticsPanel.totalCaseNum'),
    icon: 'icon-zongyongli'
  },
  pendingTestNum: {
    name: t('functionCase.statisticsPanel.pendingTestNum'),
    icon: 'icon-daiceshi'
  },
  passedTestNum: {
    name: t('functionCase.statisticsPanel.passedTestNum'),
    icon: 'icon-ceshitongguo'
  },
  notPassedTestNum: {
    name: t('functionCase.statisticsPanel.notPassedTestNum'),
    icon: 'icon-ceshiweitongguo'
  },
  oneTimePassedTestNum: {
    name: t('functionCase.statisticsPanel.oneTimePassedTestNum'),
    icon: 'icon-a-yicixingceshitongguoshu'
  },
  oneTimePassedTestRate: {
    name: t('functionCase.statisticsPanel.oneTimePassedTestRate'),
    icon: 'icon-a-yicixingceshitongguoshuai'
  },
  overdueNum: {
    name: t('functionCase.statisticsPanel.overdueNum'),
    icon: 'icon-yiyuqi1'
  },
  blockedTestNum: {
    name: t('functionCase.statisticsPanel.blockedTestNum'),
    icon: 'icon-zusaizhong'
  },
  canceledTestNum: {
    name: t('functionCase.statisticsPanel.canceledTestNum'),
    icon: 'icon-yiquxiao'
  },

  evalWorkload: {
    name: t('functionCase.statisticsPanel.evalWorkload'),
    icon: 'icon-pinggugongzuoliang'
  },
  completedWorkload: {
    name: t('functionCase.statisticsPanel.completedWorkload'),
    icon: 'icon-wanchenggongzuoliang'
  },
  oneTimePassReviewNum: {
    name: t('functionCase.statisticsPanel.oneTimePassReviewNum'),
    icon: 'icon-a-yicixingpingshentongguoshu'
  },
  oneTimePassReviewRate: {
    name: t('functionCase.statisticsPanel.oneTimePassReviewRate'),
    icon: 'icon-a-yicixingpingshentongguoshuai'
  }
};

const getCount = (key) => {
  if (!props.dataSource) {
    return 0;
  }
  if (props.dataSource[key] === undefined) {
    return ['oneTimePassedTestRate', 'oneTimePassReviewRate', 'progress'].includes(key) ? '0.00%' : 0;
  }
  return ['oneTimePassedTestRate', 'oneTimePassReviewRate', 'progress'].includes(key)
    ? props.dataSource[key] + '%'
    : key === 'totalUsedPlan'
      ? `${props.dataSource[key]}/${props.dataSource.totalPlan}`
      : props.dataSource[key];
};
</script>
<template>
  <div class="flex items-center flex-wrap text-3 leading-3 text-theme-sub-content" style="max-width: 1280px;">
    <div
      v-for="(value,key) in topCarObj"
      :key="key"
      class="flex p-3 rounded-lg mt-2 mr-2 "
      style="min-width: 172px;background-color: rgb(252, 250, 255);">
      <Icon class="text-10  mr-3 flex-none" :icon="value.icon" />
      <div class="pt-1.5 flex flex-col justify-between">
        <div class="text-theme-content text-4 text">
          {{ getCount(key) }}
        </div>
        <div>{{ value.name }}</div>
      </div>
    </div>
  </div>
</template>
<style scoped>
.card-bg {
  background-color: rgb(252, 250, 255);
}

.flex-w {
  flex: 1 1 20%;
}

.right-item {
  margin-left: 8px;
}

@media screen and (min-width: 1441px) {
  .right-item {
    margin-left: 20px;
  }
}
</style>
