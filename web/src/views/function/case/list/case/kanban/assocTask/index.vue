<script setup lang="ts">
import { computed, ref } from 'vue';
import { Button } from 'ant-design-vue';
import { Icon, IconTask, NoData, Select } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/tools';
import { funcCase } from '@/api/tester';

import { CaseInfo } from '../PropsType';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  dataSource: CaseInfo;
  canEdit: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined,
  canEdit: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: CaseInfo): void;
  (event: 'update:dataSource', value: CaseInfo): void;
}>();

const editFlag = ref(false);
const taskIds = ref<string[]>([]);

const toEdit = () => {
  editFlag.value = true;
};

const cancel = () => {
  editFlag.value = false;
};

const ok = async () => {
  const params = [{
    id: caseId.value,
    refTaskIds: taskIds.value
  }];
  editFlag.value = false;
  loadingChange(true);
  const [error] = await funcCase.updateCase(params);
  loadingChange(false);
  if (error) {
    return;
  }

  change();
};

const selectChange = (ids:string[]) => {
  taskIds.value = ids;
};

const loadingChange = (value:boolean) => {
  emit('loadingChange', value);
};

const change = async () => {
  const id = props.dataSource?.id;
  if (!id) {
    return;
  }

  loadingChange(true);
  const [error, res] = await funcCase.getCaseInfo(id);
  loadingChange(false);
  if (error) {
    return;
  }

  const data = res?.data || {};
  emit('change', data);
  emit('update:dataSource', data);
};

const caseId = computed(() => {
  return props.dataSource?.id;
});

const refTaskList = computed(() => {
  return props.dataSource?.refTaskInfos?.map(item => {
    return {
      ...item,
      linkUrl: `/task#task?taskId=${item.id}`
    };
  }) || [];
});

const refTaskIds = computed(() => {
  return refTaskList.value.map(item => item.id);
});
</script>

<template>
  <div class="h-full text-3 leading-5 pl-5 overflow-y-auto">
    <div class="flex items-center text-theme-title mb-2.5">
      <span class="font-semibold">关联任务</span>
      <Button
        v-if="props.canEdit"
        v-show="!editFlag"
        type="link"
        class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none"
        @click="toEdit">
        <Icon icon="icon-shuxie" class="text-3.5" />
      </Button>
    </div>

    <template v-if="!editFlag">
      <div v-if="refTaskList.length" class="w-full space-y-1.5 truncate">
        <RouterLink
          v-for="item in refTaskList"
          :key="item.id"
          :to="item.linkUrl"
          target="_blank"
          class="flex items-center overflow-hidden">
          <IconTask :value="item.taskType?.value" />
          <span class="truncate ml-1">{{ item.name }}</span>
        </RouterLink>
      </div>

      <NoData
        v-else
        size="small"
        style="height: calc(100% - 30px);" />
    </template>

    <template v-else>
      <Select
        :value="refTaskIds"
        showSearch
        internal
        allowClear
        :fieldNames="{ label: 'name', value: 'id' }"
        :maxTagCount="10"
        :maxTagTextLength="15"
        :maxTags="20"
        :action="`${TESTER}/task/search?projectId=${props.projectId}`"
        class="w-full"
        placeholder="最多可关联20个任务"
        mode="multiple"
        @change="selectChange">
        <template #option="record">
          <div class="flex items-center leading-4.5 overflow-hidden">
            <IconTask :value="record.taskType?.value" class="text-4 flex-shrink-0" />
            <div class="link truncate ml-1" :title="record.name">
              {{ record.name }}
            </div>
            <div
              v-if="record.overdueFlag"
              class="flex-shrink-0 border border-status-error rounded px-0.5 ml-2"
              style="transform: scale(0.9);color: rgba(245, 34, 45, 100%);line-height: 16px;">
              <span class="inline-block transform-gpu">已逾期</span>
            </div>
          </div>
        </template>
      </Select>

      <div class="flex items-center space-x-2.5 mt-2.5 justify-end">
        <Button
          type="default"
          size="small"
          @click="cancel">
          取消
        </Button>
        <Button
          type="primary"
          size="small"
          @click="ok">
          确定
        </Button>
      </div>
    </template>
  </div>
</template>
