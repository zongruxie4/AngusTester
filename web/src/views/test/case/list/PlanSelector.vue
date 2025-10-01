<script setup lang="ts">
import { Button, Tag, Tooltip } from 'ant-design-vue';
import { inject, ref, watch, Ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { TESTER } from '@xcan-angus/infra';
import { Icon, Select } from '@xcan-angus/vue-ui';
import { funcPlan } from '@/api/tester';

const { t } = useI18n();

interface Props {
  planId?: string;
  planName?: string;
}
const props = withDefaults(defineProps<Props>(), {
  planId: '',
  planName: ''
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'change', value:string|undefined): void
}>();

// Inject project information
const projectId = inject<Ref<string>>('projectId', ref(''));
const planInfo = ref<{ id: string, name: string }>();
const checkedId = ref<string>();

const showSelect = ref(false);
const todoHandler = () => {
  showSelect.value = true;
  selectValue.value = undefined;
};

const selectValue = ref<string>();
const selectChange = (_value: any, option: any) => {
  showSelect.value = false;
  const { id, name } = option;
  const exsitId = planInfo.value?.id;
  if (exsitId === id) {
    return;
  }

  planInfo.value = { id, name };
  checkedId.value = id;
  emit('change', checkedId.value);
};

const selectTag = () => {
  if (checkedId.value) {
    checkedId.value = undefined;
  } else {
    checkedId.value = planInfo.value?.id;
  }

  emit('change', checkedId.value);
};

const closeTag = () => {
  planInfo.value = undefined;
  checkedId.value = undefined;
  emit('change', checkedId.value);
};

const handleBlur = () => {
  showSelect.value = false;
};

const loadPlanList = async () => {
  const [error, res] = await funcPlan.getPlanDetail(props.planId);
  if (error) {
    return;
  }

  planInfo.value = res?.data;
};

watch(() => props.planId, (newValue) => {
  if (newValue) {
    checkedId.value = newValue;
    if (props.planName) {
      planInfo.value = { id: newValue, name: props.planName };
    } else {
      loadPlanList();
    }

    return;
  }

  checkedId.value = undefined;
}, {
  immediate: true
});

defineExpose({
  checkedId
});
</script>
<template>
  <div class="flex items-center text-3.5">
    <template v-if="!planInfo">
      <Button
        v-if="!showSelect"
        class="h-6 flex items-center"
        size="small"
        @click="todoHandler">
        <Icon icon="icon-jia" class="text-3 mr-1 -mt-0.25" />
        {{ t('common.plan') }}
      </Button>

      <Select
        v-else
        v-model:value="selectValue"
        size="small"
        class="w-43"
        :placeholder="t('functionCase.selectPlanModal.selectPlan')"
        showSearch
        :fieldNames="{ label: 'name', value: 'id' }"
        :action="`${TESTER}/func/plan?projectId=${projectId}&fullTextSearch=true`"
        @change="selectChange"
        @blur="handleBlur" />
    </template>

    <template v-else>
      <Tooltip :title="planInfo?.name" placement="topLeft">
        <Tag
          :class="checkedId===planInfo?.id?'tag-checked':''"
          closable
          class="h-6 rounded-xl px-2.5"
          @click="selectTag"
          @close="closeTag">
          <div class="truncate" style="max-width: 80px;">{{ planInfo?.name }}</div>
        </Tag>
      </Tooltip>
    </template>
  </div>
</template>
<style scoped>
.ant-tag.tag-checked :deep(.ant-tag-close-icon)>svg {
  color: #fff;
}

:deep(.tag-checked) {
  background-color: rgba(255, 129, 0, 60%);
  color: #fff;
}
</style>
