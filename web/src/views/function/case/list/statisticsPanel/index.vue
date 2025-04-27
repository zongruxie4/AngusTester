<script setup lang="ts">
import { Icon } from '@xcan-angus/vue-ui';
import { CountObj } from '../PropsType';

interface Props {
  dataSource: CountObj
}
const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ({}) as CountObj
});

const topCarObj = {
  progress: {
    name: '进度',
    icon: 'icon-zonglan'
  },
  totalCaseNum: {
    name: '总用例',
    icon: 'icon-zongyongli'
  },
  pendingTestNum: {
    name: '待测试',
    icon: 'icon-daiceshi'
  },
  passedTestNum: {
    name: '测试通过',
    icon: 'icon-ceshitongguo'
  },
  notPassedTestNum: {
    name: '测试未通过',
    icon: 'icon-ceshiweitongguo'
  },
  oneTimePassedTestNum: {
    name: '一次性测试通过数',
    icon: 'icon-a-yicixingceshitongguoshu'
  },
  oneTimePassedTestRate: {
    name: '一次性测试通过率',
    icon: 'icon-a-yicixingceshitongguoshuai'
  },
  overdueNum: {
    name: '已逾期',
    icon: 'icon-yiyuqi1'
  },
  blockedTestNum: {
    name: '阻塞中',
    icon: 'icon-zusaizhong'
  },
  canceledTestNum: {
    name: '已取消',
    icon: 'icon-yiquxiao'
  },

  evalWorkload: {
    name: '评估工作量',
    icon: 'icon-pinggugongzuoliang'
  },
  completedWorkload: {
    name: '已完成工作量',
    icon: 'icon-wanchenggongzuoliang'
  },
  oneTimePassReviewNum: {
    name: '一次性通过评审数',
    icon: 'icon-a-yicixingpingshentongguoshu'
  },
  oneTimePassReviewRate: {
    name: '一次性评审通过率',
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
