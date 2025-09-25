<script lang="ts" setup>
import { ref, nextTick } from 'vue';
import { useI18n } from 'vue-i18n';
import { Grid, Icon, SelectUser } from '@xcan-angus/vue-ui';
import { funcCase } from '@/api/tester';
import { Button } from 'ant-design-vue';
const { t } = useI18n();

interface Props {
  caseInfo?: {[key: string]: any},
  userInfo?: {[key: string]: any},
  readonly?: boolean
}
const props = withDefaults(defineProps<Props>(), {
  caseInfo: undefined,
  readonly: true
});

const emits = defineEmits<{(e: 'change')}>()

const peopleInfoColumns = [
  [
    {
      label: t('common.tester'),
      dataIndex: 'testerName'
    },
    {
      label: t('common.developer'),
      dataIndex: 'developerName'
    },
    {
      label: t('common.reviewer'),
      dataIndex: 'reviewerName'
    },
    {
      label: t('common.creator'),
      dataIndex: 'createdByName'
    },
    {
      label: t('common.modifier'),
      dataIndex: 'lastModifiedByName'
    }]
];

const isEditTester = ref(false);
const testerIcContent = ref();
const testerSelectRef = ref()
const saveTesterLoading = ref(false);
const toEditTester = () => {
  isEditTester.value = true;
  testerIcContent.value = props.caseInfo?.testerId;

  nextTick(() => {
    testerSelectRef.value.focus();
  });

};

const saveTester = async () => {
  if (testerIcContent.value === props.caseInfo?.testerId) {
    isEditTester.value = false;
    return;
  }
  if (saveTesterLoading.value) {
    return;
  }
  saveTesterLoading.value = true;
  const [error] = await funcCase.updateCase([{
    id: props?.caseInfo?.id,
    testerId: testerIcContent.value
  }]);
  saveTesterLoading.value = false;
  isEditTester.value = false;
  if (error) {
    return;
  }
  emits('change');
}

const handleSetTester = () => {
  testerIcContent.value = props?.userInfo?.id;
  saveTester();
}

const isEditDeveloper = ref(false);
const developerIcContent = ref();
const developerSelectRef = ref()
const saveDeveloperLoading = ref(false);
const toEditDeveloper = () => {
  isEditDeveloper.value = true;
  developerIcContent.value = props.caseInfo?.developerId;

  nextTick(() => {
    developerSelectRef.value.focus();
  });

};

const saveDeveloper = async () => {
  if (developerIcContent.value === props.caseInfo?.developerId) {
    isEditDeveloper.value = false;
    return;
  }
  if (saveDeveloperLoading.value) {
    return;
  }
  saveDeveloperLoading.value = true;
  const [error] = await funcCase.updateCase([{
    id: props?.caseInfo?.id,
    developerId: developerIcContent.value
  }]);
  saveDeveloperLoading.value = false;
  isEditDeveloper.value = false;
  if (error) {
    return;
  }
  emits('change');
}

const handleDeveloper = () => {
  developerIcContent.value = props?.userInfo?.id;
  saveDeveloper();
}




</script>
<template>
  <div class="bg-white rounded-lg p-6">
    <Grid
      :columns="peopleInfoColumns"
      :dataSource="props.caseInfo"
      :spacing="24"
      :marginBottom="6"
      labelSpacing="12px"
      font-size="14px"
      class="member-info-grid">
      <template #testerName="{text}">
        <template v-if="isEditTester">
          <SelectUser
            ref="testerSelectRef"
            v-model:value="testerIcContent"
            class="flex-1 w-full"
            size="small"
            @blur="saveTester" />
        </template>

        <template v-else>
          <div class="flex items-center">
            <span>{{ text }}</span>
            <Button
              v-show="!props.readonly"
              :loading="saveTesterLoading"
              size="small"
              type="link">
              <Icon
                icon="icon-xiugai"
                class="text-3.5 text-theme-special text-theme-text-hover cursor-pointer ml-2"
                @click="toEditTester" />
            </Button>
            <Button
              v-show="!props.readonly && props.caseInfo?.testerId !== props.userInfo?.id"
              :loading="saveTesterLoading"
              type="link"
              size="small"
              class="p-0 h-3.5 leading-3.5 ml-1"
              @click="handleSetTester">
              {{ t('functionCase.detail.caseDetail.assignToMe') }}
            </Button>
          </div>

        </template>


      </template>


      <template #developerName="{text}">
        <template v-if="isEditDeveloper">
          <SelectUser
            ref="developerSelectRef"
            v-model:value="developerIcContent"
            class="flex-1 w-full"
            size="small"
            @blur="saveDeveloper" />
        </template>

        <template v-else>
          <div class="flex items-center">
            <span>{{ text }}</span>
            <Button
              v-show="!props.readonly"
              :loading="saveDeveloperLoading"
              size="small"
              type="link">
              <Icon
                icon="icon-xiugai"
                class="text-3.5 text-theme-special text-theme-text-hover cursor-pointer ml-2"
                @click="toEditDeveloper" />
            </Button>
            <Button
              v-show="!props.readonly && props.caseInfo?.developerId !== props.userInfo?.id"
              :loading="saveDeveloperLoading"
              type="link"
              size="small"
              class="p-0 h-3.5 leading-3.5 ml-1"
              @click="handleDeveloper">
              {{ t('functionCase.detail.caseDetail.assignToMe') }}
            </Button>
          </div>
        </template>


      </template>
    </Grid>
  </div>
</template>

<style scoped>
:deep(.member-info-grid) {
  .ant-descriptions-item-label {
    @apply text-gray-600 font-medium;
  }

  .ant-descriptions-item-content {
    @apply text-gray-900;
  }
}
</style>
