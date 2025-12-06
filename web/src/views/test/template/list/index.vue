<script setup lang="ts">
import { defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { modal, notification, Spin } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';
import { template } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { BasicProps } from '@/types/types';
import { TestTemplateDetail } from '../types';

// Async components
const Introduce = defineAsyncComponent(() => import('@/views/test/template/list/Introduce.vue'));
const List = defineAsyncComponent(() => import('@/views/test/template/list/List.vue'));

// Composables
const { t } = useI18n();

// Props and injects
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined
});

const addTabPane = inject<(data: any) => void>('addTabPane', () => ({}));

// Reactive data
const isDataLoaded = ref(false);
const isLoading = ref(false);

// Data state
const dataList = ref<TestTemplateDetail[]>([]);

/**
 * Loads template list data from API
 */
const loadData = async () => {
  isLoading.value = true;
  const [error, res] = await template.getTemplateList();
  isDataLoaded.value = true;
  isLoading.value = false;

  if (error) {
    notification.error(error.message || t('testTemplate.messages.loadFailed'));
    dataList.value = [];
    return;
  }

  const data = res?.data;
  if (data) {
    dataList.value = (data || []) as TestTemplateDetail[];
  }
};

/**
 * Handles template edit action
 * @param templateData - Template detail data
 */
const handleEdit = (templateData: TestTemplateDetail) => {
  addTabPane({
    _id: templateData.id,
    value: 'templateEdit',
    noCache: true,
    data: { _id: templateData.id, id: templateData.id }
  });
};

/**
 * Handles template delete action
 * @param templateData - Template detail data
 */
const handleDelete = async (templateData: TestTemplateDetail) => {
  if (templateData.isSystem) {
    notification.warning(t('testTemplate.messages.systemTemplateCannotDelete'));
    return;
  }

  modal.confirm({
    title: t('actions.delete'),
    content: t('testTemplate.messages.deleteConfirm', { name: templateData.name }),
    onOk: async () => {
      const [error] = await template.deleteTemplate(templateData.id);
      if (error) {
        return;
      }

      notification.success(t('actions.tips.deleteSuccess'));
      loadData();
    }
  });
};

/**
 * Handles template add action
 */
const handleAdd = () => {
  const newId = utils.uuid();
  addTabPane({
    _id: newId,
    name: t('testTemplate.actions.addTemplate'),
    value: 'templateEdit',
    noCache: true,
    data: { _id: newId }
  });
};

const handleView = (templateData: TestTemplateDetail) => {
  addTabPane({
    _id: templateData.id + 'detail',
    value: 'templateDetail',
    noCache: true,
    data: { _id: templateData.id + 'detail', id: templateData.id }
  });
};

// lifecycle hooks
onMounted(() => {
  watch(() => props.notify, (newValue) => {
    if (!newValue) {
      return;
    }
    loadData();
  }, { immediate: false });

  loadData();
});
</script>

<template>
  <div class="flex flex-col h-full overflow-auto px-5 py-5 leading-5 text-3">
    <div class="flex">
      <Introduce class="mb-7 flex-1" />
    </div>

    <div class="text-3.5 font-semibold mb-1">{{ t('testTemplate.addedTemplates') }}</div>
    <Spin :spinning="isLoading" class="flex-1 flex flex-col">
      <template v-if="isDataLoaded">
        <div v-if="dataList.length === 0" class="flex-1 flex flex-col items-center justify-center">
          <img src="../../../../assets/images/nodata.png">
          <div class="flex items-center text-theme-sub-content text-3.5 leading-5 space-x-1">
            <span>{{ t('testTemplate.noTemplates') }}</span>
            <a class="router-link flex-1 truncate cursor-pointer text-primary" @click="handleAdd">
              {{ t('testTemplate.actions.addTemplate') }}
            </a>
          </div>
        </div>

        <div v-else class="flex-1 flex flex-col">
          <List
            :dataList="dataList"
            :loading="isLoading"
            @edit="handleEdit"
            @delete="handleDelete"
            @add="handleAdd"
            @view="handleView" />
        </div>
      </template>
    </Spin>
  </div>
</template>

