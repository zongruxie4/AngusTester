<script lang="ts" setup>
import { computed, defineAsyncComponent, onMounted, ref } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Select } from '@xcan-angus/vue-ui';
import _ from 'lodash-es';
import { func } from '@/api/tester';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  baselineId: string;
  planId: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  baselineId: undefined,
  planId: undefined
});

const CompareModal = defineAsyncComponent(() => import('./compareModal.vue'));

const caselineList = ref([]);
const allCaseIds = ref<string[]>([]);
const compareVisible = ref(false);
const compareLineId = ref();
const baseCase = ref({});
const compareCase = ref({});

const baseLine = computed(() => {
  const target = caselineList.value.find(line => line.id === props.baselineId);
  return target;
});

const compareLine = computed(() => {
  const target = caselineList.value.find(line => line.id === compareLineId.value);
  return target;
});

const loadBaseLineList = async () => {
  const [error, { data }] = await func.searchBaseline({
    pageSize: 2000,
    pageNo: 1,
    projectId: props.projectId
  });
  if (error) {
    return;
  }
  caselineList.value = data?.list || [];
  // allCaseIds.value.push(caselineList.value.map(cases => cases.id));
  compareLineId.value = caselineList.value[0].id;
};

const loadBaseCase = async () => {
  const [error, { data }] = await func.searchCaseInBaseline(props.baselineId, {
    pageSize: 2000,
    pageNo: 1,
    projectId: props.projectId
  });
  if (error) {
    return;
  }
  (data?.list || []).forEach(cases => {
    allCaseIds.value.push(cases.id);
    baseCase.value[cases.id] = cases;
  });
};

const loadCompareCase = async () => {
  const [error, { data }] = await func.searchCaseInBaseline(compareLineId.value, {
    pageSize: 2000,
    pageNo: 1,
    projectId: props.projectId
  });
  if (error) {
    return;
  }
  allCaseIds.value = Object.keys(baseCase.value);
  (data?.list || []).forEach(cases => {
    if (!allCaseIds.value.includes(cases.id)) {
      allCaseIds.value.push(cases.id);
    }
    compareCase.value[cases.id] = cases;
  });
};

const handleChangeCompareLineId = () => {
  compareCase.value = {};
  loadCompareCase();
};

onMounted(async () => {
  await loadBaseLineList();
  Promise.all([loadBaseCase(), loadCompareCase()])
    .then(() => {
      allCaseIds.value = _.uniq(allCaseIds.value);
    });
});

const leftRawConfig = [
  {
    title: '',
    dataIndex: 'idx',
    class: 'w-8 border-r text-center'
  },
  {
    title: '名称',
    dataIndex: 'name',
    class: 'flex-1 border-r'
  },
  {
    title: '版本号',
    dataIndex: 'version',
    class: 'w-20'
  }
];

const rightRawConfig = [
  {
    title: '名称',
    dataIndex: 'name',
    class: 'flex-1 border-r'
  },
  {
    title: '版本号',
    dataIndex: 'version',
    class: 'w-20 border-r'
  },
  {
    title: '操作',
    dataIndex: 'action',
    class: 'w-20 text-center'
  }
];

const selectCaseId = ref();
const openCompareModal = (caseId) => {
  compareVisible.value = true;
  selectCaseId.value = caseId;
};

</script>
<template>
  <div class="overflow-y-auto -mt-4">
    <div class="flex border-b border-l border-r">
      <div class="flex-1 leading-8  border-r">
        <div class="text-center">
          {{ baseLine?.name }}
        </div>
      </div>
      <div class="flex-1 leading-8">
        <div class="text-center px-1">
          <Select
            v-model:value="compareLineId"
            class="w-full"
            :options="caselineList"
            :fieldNames="{
              value: 'id',
              label: 'name'
            }"
            @change="handleChangeCompareLineId" />
        </div>
      </div>
    </div>

    <div class="flex border-b  border-l border-r">
      <div class="flex-1 leading-8 border-r">
        <div class="text-center flex">
          <div
            v-for="leftRaw in leftRawConfig"
            :key="leftRaw.dataIndex"
            class="bg-gray-bg"
            :class="leftRaw.class">
            {{ leftRaw.title }}
          </div>
        </div>
      </div>
      <div class="flex-1 leading-8">
        <div class="text-center flex">
          <div
            v-for="rightRaw in rightRawConfig"
            :key="rightRaw.dataIndex"
            class="bg-gray-bg"
            :class="rightRaw.class">
            {{ rightRaw.title }}
          </div>
        </div>
      </div>
    </div>

    <div
      v-for="(caseId, idx) in allCaseIds"
      :key="caseId"
      class="flex leading-8 border-b  border-l border-r">
      <div class="flex-1 leading-8 border-r flex">
        <div
          v-for="(leftRaw) in leftRawConfig"
          :key="leftRaw.dataIndex"
          :class="leftRaw.class"
          class="px-1">
          <tmplate v-if="leftRaw.dataIndex === 'version'">
            <span v-if="baseCase[caseId]?.[leftRaw.dataIndex]">v{{ baseCase[caseId][leftRaw.dataIndex] }}</span>
          </tmplate>
          <template v-else-if="leftRaw.dataIndex === 'idx'">
            {{ idx + 1 }}
          </template>
          <template v-else>
            {{ baseCase[caseId]?.[leftRaw.dataIndex] }}
          </template>
        </div>
      </div>
      <div class="flex-1 leading-8  flex" :class="{'bg-status-add': !baseCase[caseId] && compareCase[caseId], 'bg-status-del': baseCase[caseId] && !compareCase[caseId]}">
        <div
          v-for="rightRaw in rightRawConfig"
          :key="rightRaw.dataIndex"
          :class="rightRaw.class"
          class="px-1">
          <tmplate v-if="rightRaw.dataIndex === 'version'">
            <span v-if="compareCase[caseId]?.[rightRaw.dataIndex]">v{{ compareCase[caseId][rightRaw.dataIndex] }}</span>
          </tmplate>
          <tmplate v-else-if="rightRaw.dataIndex === 'action'">
            <Button
              v-if="compareCase[caseId] && baseCase[caseId]"
              type="link"
              size="small"
              @click="openCompareModal(caseId)">
              比照
            </Button>
          </tmplate>
          <template v-else>
            {{ compareCase[caseId]?.[rightRaw.dataIndex] }}
          </template>
        </div>
      </div>
    </div>

    <AsyncComponent :visible="compareVisible">
      <CompareModal
        v-model:visible="compareVisible"
        :baselineId="props.baselineId"
        :comparelineId="compareLineId"
        :caseId="selectCaseId"
        :projectId="props.projectId" />
    </AsyncComponent>
  </div>
</template>
