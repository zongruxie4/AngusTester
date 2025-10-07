<script setup lang="ts">
import { Button, Tag, Tooltip } from 'ant-design-vue';
import { inject, ref, watch, Ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { TESTER } from '@xcan-angus/infra';
import { Icon, Select } from '@xcan-angus/vue-ui';
import { funcPlan } from '@/api/tester';

const { t } = useI18n();

// Component props interface
interface Props {
  planId?: string;
  planName?: string;
}
const props = withDefaults(defineProps<Props>(), {
  planId: '',
  planName: ''
});

// Component emits
const emit = defineEmits<{
  (e: 'change', value: string | undefined): void
}>();

// Basic state management
const projectId = inject<Ref<string>>('projectId', ref(''));
const planInfo = ref<{ id: string, name: string }>();
const checkedId = ref<string>();

// UI state management
const showSelect = ref(false);
const selectValue = ref<string>();

// Event handlers

/**
 * Handle add plan button click
 */
const handleAddPlan = () => {
  showSelect.value = true;
  selectValue.value = undefined;
};

/**
 * Handle plan selection change
 * @param _value - Selected value
 * @param option - Selected option
 */
const selectChange = (_value: any, option: any) => {
  showSelect.value = false;
  const { id, name } = option;
  const existId = planInfo.value?.id;
  if (existId === id) {
    return;
  }

  planInfo.value = { id, name };
  checkedId.value = id;
  emit('change', checkedId.value);
};

/**
 * Toggle plan selection
 */
const selectTag = () => {
  if (checkedId.value) {
    checkedId.value = undefined;
  } else {
    checkedId.value = planInfo.value?.id;
  }

  emit('change', checkedId.value);
};

/**
 * Close plan tag
 */
const closeTag = () => {
  planInfo.value = undefined;
  checkedId.value = undefined;
  emit('change', checkedId.value);
};

/**
 * Handle select blur
 */
const handleBlur = () => {
  showSelect.value = false;
};

/**
 * Load plan details
 */
const loadPlanList = async () => {
  const [error, res] = await funcPlan.getPlanDetail(props.planId);
  if (error) {
    return;
  }

  planInfo.value = res?.data;
};

// Watchers
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

// Expose functions to parent components
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
        @click="handleAddPlan">
        <Icon icon="icon-jia" class="text-3 mr-1 -mt-0.25" />
        {{ t('common.plan') }}
      </Button>

      <Select
        v-else
        v-model:value="selectValue"
        size="small"
        class="w-43"
        :placeholder="t('testCase.selectPlanModal.selectPlan')"
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
