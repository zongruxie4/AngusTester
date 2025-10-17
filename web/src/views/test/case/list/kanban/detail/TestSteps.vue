<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { Button } from 'ant-design-vue';
import { Icon, NoData } from '@xcan-angus/vue-ui';
import { cloneDeep } from 'lodash-es';
import { testCase } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { CaseDetail } from '@/views/test/types';
import { CaseInfoEditProps } from '@/views/test/case/list/types';

const CaseStep = defineAsyncComponent(() => import('@/views/test/case/list/CaseSteps.vue'));

const { t } = useI18n();

const props = withDefaults(defineProps<CaseInfoEditProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined,
  canEdit: false
});


const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: CaseDetail): void;
  (event: 'update:dataSource', value: CaseDetail): void;
}>();

const editFlag = ref(false);
const steps = ref<{expectedResult:string;step:string;}[]>([]);
const stepsContent = ref<{expectedResult:string;step:string;}[]>([]);

/*
  Enter steps edit mode and clone current steps as working copy.
*/
const openEditSteps = () => {
  editFlag.value = true;
  stepsContent.value = cloneDeep(steps.value);
};

const cancel = () => {
  editFlag.value = false;
};

/*
  Persist updated steps and refresh case detail.
*/
const saveSteps = async () => {
  loadingChange(true);
  const [error] = await testCase.updateCase([{
    id: props.dataSource?.id,
    steps: stepsContent.value
  }]);
  loadingChange(false);
  if (error) {
    return;
  }

  editFlag.value = false;
  change();
};

const change = async () => {
  const id = props.dataSource?.id;
  if (!id) {
    return;
  }

  loadingChange(true);
  const [error, res] = await testCase.getCaseDetail(id);
  loadingChange(false);
  if (error) {
    return;
  }

  const data = res?.data || {};
  emit('change', data);
  emit('update:dataSource', data);
};

const loadingChange = (value:boolean) => {
  emit('loadingChange', value);
};

const defaultValue = computed(() => {
  return editFlag.value ? stepsContent.value : steps.value;
});

onMounted(() => {
  watch(() => props.dataSource, (newValue) => {
    if (!newValue) {
      steps.value = [];
      return;
    }

    steps.value = newValue.steps;
    stepsContent.value = cloneDeep(steps.value);
  }, { immediate: true });
});
</script>

<template>
  <div class="section-container">
    <div class="section-header">
      <h3 class="section-title">{{ t('common.testSteps') }}</h3>
      <Button
        v-if="props.canEdit"
        v-show="!editFlag"
        type="link"
        class="edit-btn"
        @click="openEditSteps">
        <Icon icon="icon-shuxie" />
      </Button>
    </div>

    <NoData v-if="!editFlag&&!steps.length" size="small" />

    <CaseStep
      v-if="steps.length || editFlag"
      v-model:value="stepsContent"
      :stepView="props.dataSource?.stepView?.value"
      :defaultValue="defaultValue"
      :readonly="!editFlag" />

    <div v-show="editFlag" class="mt-2.5 space-x-2.5 w-full flex items-center justify-end">
      <Button size="small" @click="cancel">{{ t('actions.cancel') }}</Button>
      <Button
        size="small"
        type="primary"
        @click="saveSteps">
        {{ t('actions.confirm') }}
      </Button>
    </div>
  </div>
</template>

<style scoped>
/* Section container styles */
.section-container {
  margin-top: 0;
  margin-left: -20px;
  margin-right: -20px;
}

/* Section header styles */
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px 8px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin: 0;
  line-height: 1.2;
}

/* Edit button styles */
.edit-btn {
  flex-shrink: 0;
  padding: 0;
  height: 16px;
  width: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: none;
  color: #1890ff !important;
  cursor: pointer;
  transition: color 0.2s;
}

.edit-btn:focus {
  color: #1890ff !important;
  background: none !important;
  border: none !important;
  box-shadow: none !important;
}

.edit-btn:hover {
  color: #1890ff;
}

.edit-btn .anticon {
  font-size: 12px;
}
</style>
