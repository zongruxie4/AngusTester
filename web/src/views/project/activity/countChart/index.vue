
<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref } from 'vue';
import { enumLoader } from '@xcan-angus/tools';
import { useI18n } from 'vue-i18n';
import { DateType, PieSetting } from './PropsType';

// type Filters = { key: string, value: string | boolean | string[], op: string; };

interface Props {
  geteway:string;
  resource:string;
  barTitle:string;
  dateType:DateType;
  visible?:boolean;
  userId?:string;
  searchParams?: {
    key: string,
    op: string,
    value: string
  }[]
}
const props = withDefaults(defineProps<Props>(), {
  resource: '',
  barTitle: '',
  dateType: 'MONTH',
  visible: true,
  userId: '',
  searchParams: () => []
});

const LoadChart = defineAsyncComponent(() => import('./loadChart.vue'));

const { t } = useI18n();
// ---------------蓝---------绿-----------黄------------红---------------浅蓝-------------紫----------橘黄---------灰色-------------粉色-------------浅蓝1
// const COLOR = ['45, 142, 255', '82, 196, 26', '255, 165, 43', '245, 34, 45', '103, 215, 255', '201, 119, 255', '255, 102, 0', '217, 217, 217', '251, 129, 255', '171, 211, 255', '255, 185, 37', '191, 199, 255'];

/**
 * 饼图统计图配置说明：
 * key: 对应数据库表的列,请求接口的groupByColumns参数,返回的统计数据和顺序有关,可参照后台接口返回（开启loadCountGroup()可查看具体返回结果）
 * value: 每一列统计的类型名称,前端配置，对应统计图的title
 * type: 分两种 一种是统计结果有多种类型,对应具体的enum接口;另一种只有0 和 1（0和1前端配置）
 * color:rgba格式，配置rgb即可,颜色数组的长度对应type的长度，缺少会报错
 * 顺序决定统计的结果的顺序
 */

// 用户统计配置
const activityGroup = ref<PieSetting[]>([
  { key: 'target_type', value: t('资源类型'), type: [], color: [] }
]);

const hasPieChart = ref(false);
// 获取饼图统计相关枚举
const loadEnums = async () => {
  switch (props.resource) {
    case 'Activity':
      await loadScriptType();
      hasPieChart.value = true;
      break;
  }
};

const loadScriptType = async () => {
  const [error, data] = await enumLoader.load('CombinedTargetType');
  if (error) {
    return;
  }
  activityGroup.value[0].type = data;
  activityGroup.value[0].color = data?.map(item => getScriptTypeColor(item.value));
};

const getScriptTypeColor = (value:string) => {
  switch (value) {
    case 'PROJECT':
      return 'rgba(166, 206, 255, 1)';
    case 'SERVICE':
      return 'rgba(162, 222, 236, 1)';
    case 'API':
      return 'rgba(45, 142, 255, 1)';
    case 'MODULE':
      return 'rgba(127, 145, 255, 1)';
    case 'SCENARIO':
      return 'rgba(251, 129, 255, 1)';
    case 'TASK':
      return 'rgba(255, 102, 0, 1)';
    case 'TASK_SPRINT':
      return 'rgba(255, 165, 43, 1)';
    case 'API_CASE':
      return 'rgba(118, 196, 125, 1)';
    case 'TAG':
      return 'rgba(217, 217, 217, 1)';
    case 'VARIABLE':
      return 'rgba(245, 34, 45, 1)';
    case 'DATASET':
      return 'rgba(127, 145, 255, 1)';
    case 'FUNC_PLAN':
      return 'rgba(255, 185, 37, 1)';
    case 'FUNC_CASE':
      return 'rgba(201, 119, 255, 1)';
    case 'SCRIPT':
      return 'rgba(191, 199, 255, 1)';
    case 'MOCK_SERVICE':
      return 'rgba(103, 215, 255, 1)';
    case 'MOCK_APIS':
      return 'rgba(127, 91, 72, 1)';
    case 'REPORT':
      return 'rgba(57, 129, 184, 1)';
    case 'EXECUTION':
      return 'rgba(228, 112, 57, 1)';
    case 'FUNC_REVIEW':
      return 'rgba(165, 1135, 106, 1)';
    case 'MEETING':
      return 'rgba(169, 104, 55, 0.7)';
    case 'BASELINE':
      return 'rgba(134, 97, 151, 1)';
    case 'FUNC_CASE_BASELINE':
      return 'rgba(134, 97, 151, 1)';
    case 'TASK_ANALYSIS':
      return 'rgba(178, 88, 131, 1)';
    case 'FUNC_CASE_ANALYSIS':
      return 'rgba(180, 128, 48, 1)';
    case 'SCENARIO_MONITOR':
      return 'rgba(227, 220, 155, 1)';
    case 'SOFTWARE_VERSION':
      return 'rgba(117, 118, 148, 1)';
  }
};

const pieParmas = computed(() => {
  switch (props.resource) {
    case 'Activity': // 用户
      return activityGroup.value;
    default:
      return [];
  }
});

onMounted(() => {
  loadEnums();
});
</script>
<template>
  <LoadChart
    :searchParams="props.searchParams"
    :visible="props.visible"
    :geteway="props.geteway"
    :resource="props.resource"
    :barTitle="props.barTitle"
    :pieParmas="pieParmas"
    :hasPieChart="hasPieChart"
    :userId="props.userId"
    :dateType="props.dateType">
    <template #default>
      <slot></slot>
    </template>
  </LoadChart>
</template>
