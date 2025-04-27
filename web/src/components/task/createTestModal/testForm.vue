<script setup lang="ts">
import { ref, watch } from 'vue';
import { DatePicker, Select, SelectEnum, TaskPriority } from '@xcan-angus/vue-ui';

import { FormData, Priority } from './PropsType';

interface Props {
  value: FormData;
  type: 'API' | 'PROJECT' | 'SERVICE' | 'SCENARIO';
  users: {fullname: string; id: string}[];
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  users: () => []
});

const formData = ref<FormData>({
  deadlineDate: '',
  priority: 'MEDIUM',
  assigneeId: undefined
});

const userIsEmpty = ref(false);
const userChange = (id: string) => {
  userIsEmpty.value = false;
  update({ assigneeId: id });
};

const dateIsEmpty = ref(false);
const dateChange = (deadlineDate) => {
  dateIsEmpty.value = false;
  update({ deadlineDate });
};

const enumIsEmpty = ref(false);
const enumChange = (val: Priority) => {
  enumIsEmpty.value = false;
  update({ priority: val });
};

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:value', value: FormData): void
}>();
const update = (_value: { [key: string]: string | string[] }) => {
  emit('update:value', { ...formData.value, ..._value });
};

const seletionOptions = ref<{ fullname: string, userId: string }[]>();
watch(() => props.value, (newValue) => {
  formData.value = newValue;
  // seletionOptions.value = newValue.selectOptions?.map(item => ({ fullname: item.name, userId: item.id }));
}, { deep: true, immediate: true });

const validate = () => {
  let errorNum = 0;
  if (!formData.value.assigneeId) {
    userIsEmpty.value = true;
    errorNum++;
  }

  if (!formData.value.deadlineDate) {
    dateIsEmpty.value = true;
    errorNum++;
  }
  if (!formData.value.priority) {
    enumIsEmpty.value = true;
    errorNum++;
  }

  return !errorNum;
};

const reset = () => {
  userIsEmpty.value = false;
  dateIsEmpty.value = false;
  enumIsEmpty.value = false;
  formData.value = props.value || {
    deadlineDate: '',
    priority: 'MEDIUM',
    assigneeId: undefined
  };
};

// const appInfo = inject('appInfo') as Ref<Record<string, any>>;
// const appId = appInfo?.value?.id;
// const userListAction = computed(() => {
//   if (appId) {
//     return `${GM}/app/${appId}/auth/user`;
//   }
//   return undefined;
// });

defineExpose({ validate, reset });
</script>

<template>
  <div class="space-y-5">
    <div class="text-3 text-theme-content">
      <div class="mb-1">分配经办人</div>
      <Select
        showSearch
        :error="userIsEmpty"
        :value="formData.assigneeId"
        class="w-full"
        placeholder="请选择经办人"
        :allowClear="true"
        :options="props.users"
        :fieldNames="{ label: 'fullname', value: 'id' }"
        size="small"
        @change="userChange" />
    </div>

    <div class="text-3 text-theme-content">
      <div class="mb-1">测试截止时间</div>
      <DatePicker
        :error="dateIsEmpty"
        class="w-full"
        :value="formData.deadlineDate"
        :allowClear="true"
        size="small"
        showTime
        @change="dateChange" />
    </div>

    <div class="text-3 text-theme-content">
      <div class="mb-1">优先级</div>
      <SelectEnum
        class="w-full"
        enumKey="Priority"
        :error="enumIsEmpty"
        :value="formData.priority"
        :allowClear="false"
        size="small"
        @change="enumChange">
        <template #option="record">
          <TaskPriority :value="record" />
        </template>
      </SelectEnum>
    </div>
  </div>
</template>
