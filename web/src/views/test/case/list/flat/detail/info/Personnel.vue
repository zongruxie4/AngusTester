<script setup lang="ts">
import { nextTick, ref } from 'vue';
import { Grid, Icon, SelectUser } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import { testCase } from '@/api/tester';
import { CaseDetail } from '@/views/test/types';
import { appContext } from '@xcan-angus/infra';

interface Props {
  id?: number;
  dataSource?: CaseDetail;
  projectId?: string;
  taskId?: number;
  actionAuth?: {[key: string]: any};
  columns?: any[][];
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined,
  dataSource: undefined,
  projectId: undefined,
  taskId: undefined,
  actionAuth: () => ({}),
  columns: () => []
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'change', value: Partial<CaseDetail>): void;
  (event: 'loadingChange', value: boolean): void;
}>();

const { t } = useI18n();

const testerSelectRef = ref();
const isEditTester = ref(false);
const testerIcContent = ref();
const saveTesterLoading = ref(false);

/**
 * <p>Enter editing mode for tester selector.</p>
 * <p>Sets the current tester value and focuses the selector.</p>
 */
const handleEditTester = () => {
  isEditTester.value = true;
  testerIcContent.value = props.dataSource?.testerId;
  nextTick(() => {
    testerSelectRef.value.focus();
  });
};

/**
 * <p>Persist tester selection to backend.</p>
 * <p>Updates the tester assignment if changed.</p>
 */
const saveTester = async () => {
  if (!props.dataSource) {
    isEditTester.value = false;
    return;
  }

  if (testerIcContent.value === props.dataSource?.testerId) {
    isEditTester.value = false;
    return;
  }
  if (saveTesterLoading.value) {
    return;
  }
  saveTesterLoading.value = true;
  const [error] = await testCase.updateCase([{
    id: props.dataSource.id,
    testerId: testerIcContent.value
  }]);
  saveTesterLoading.value = false;
  isEditTester.value = false;
  if (error) {
    return;
  }
  emit('change', { testerId: testerIcContent.value });
};

/**
 * <p>Set current user as tester.</p>
 * <p>Assigns the current user to be the tester for this case.</p>
 */
const handleSetTester = async () => {
  if (!props.dataSource) return;

  if (appContext.getUser()?.id === props.dataSource?.testerId) {
    return;
  }
  if (saveTesterLoading.value) {
    return;
  }
  saveTesterLoading.value = true;
  const [error] = await testCase.updateCase([{
    id: props.dataSource.id,
    testerId: appContext.getUser()?.id
  }]);
  saveTesterLoading.value = false;
  if (error) {
    return;
  }
  emit('change', { testerId: appContext.getUser()?.id });
};
</script>

<template>
  <Grid
    :columns="columns"
    :dataSource="dataSource"
    :spacing="20"
    :marginBottom="4"
    labelSpacing="10px"
    font-size="12px"
    class="pt-2 pl-5.5">
    <template #testerName="{text}">
      <template v-if="isEditTester">
        <SelectUser
          ref="testerSelectRef"
          v-model:value="testerIcContent"
          class="flex-1"
          size="small"
          @blur="saveTester" />
      </template>
      <template v-else>
        <span>{{ text }}</span>
        <Icon
          v-if="props.actionAuth['edit']"
          icon="icon-xiugai"
          class="text-3.5 text-theme-special text-theme-text-hover cursor-pointer ml-2"
          @click="handleEditTester" />
        <Button
          v-if="props.actionAuth['edit'] && dataSource.testerId !== appContext.getUser()?.id"
          :loading="saveTesterLoading"
          type="link"
          size="small"
          class="p-0 h-3.5 leading-3.5 ml-1"
          @click="handleSetTester">
          {{ t('actions.assignToMe') }}
        </Button>
      </template>
    </template>
  </Grid>
</template>
