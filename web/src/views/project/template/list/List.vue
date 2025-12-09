<script setup lang="ts">
import { ref, computed, defineAsyncComponent } from 'vue';
import { Button, Card, Dropdown, Menu, Tag } from 'ant-design-vue';
import { Icon, NoData, Input } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { TestTemplateDetail } from '../types';
import { TestTemplateType } from '@/enums/enums';
import { debounce } from 'throttle-debounce';
import { duration } from '@xcan-angus/infra';
interface Props {
  dataList: TestTemplateDetail[];
  loading: boolean;
}

const props = defineProps<Props>();

// Emits definition
const emit = defineEmits<{
  edit: [templateData: TestTemplateDetail];
  delete: [templateData: TestTemplateDetail];
  add: [];
  view: [templateData: TestTemplateDetail];
  import: [];
  export: [templateData: TestTemplateDetail];
}>();

const { t } = useI18n();

const searchKeyword = ref<string>('');

const richTextContent = defineAsyncComponent(() => import('@/components/editor/richEditor/textContent/index.vue'));

const handleSearchKeywordChange = debounce(duration.search, (event: Event) => {
  const target = event.target as HTMLInputElement;
  const value = target.value;
  searchKeyword.value = value;
});


const showData = computed(() => {
  return props.dataList.filter(item => item.name.includes(searchKeyword.value));
});

/**
 * Get template type display text
 */
const getTemplateTypeText = (type: string) => {
  return type === TestTemplateType.TEST_PLAN 
    ? t('xcm.enum.TestTemplateType.TEST_PLAN') 
    : type === TestTemplateType.TEST_CASE
    ? t('xcm.enum.TestTemplateType.TEST_CASE')
    : type === 'ISSUE' ? t('issue.title') : '';
};

/**
 * Get template type color
 */
const getTemplateTypeColor = (type: string) => {
  return type === TestTemplateType.TEST_PLAN ? 'blue' : 'green';
};

/**
 * Handle edit action
 */
const handleEdit = (template: TestTemplateDetail) => {
  if (template.isSystem) {
    return;
  }
  emit('edit', template);
};

/**
 * Handle delete action
 */
const handleDelete = (template: TestTemplateDetail) => {
  emit('delete', template);
};

/**
 * Handle add action
 */
const handleAdd = () => {
  emit('add');
};

/**
 * Get dropdown menu items for template actions
 */
const getDropdownItems = (template: TestTemplateDetail) => {
  const items: {key: string, label: string, danger?: boolean}[] = [];
  if (!template.isSystem) {
    items.push({
      key: 'edit',
      label: t('actions.edit')
    });
    items.push({
      key: 'delete',
      label: t('actions.delete'),
      danger: true
    });
  }
  return items;
};

/**
 * Handle dropdown menu click
 */
const handleDropdownClick = (template: TestTemplateDetail, key: string) => {
  if (key === 'edit') {
    handleEdit(template);
  } else if (key === 'delete') {
    handleDelete(template);
  }
};

const handleView = (template: TestTemplateDetail) => {
  emit('view', template);
};

const handleImportTemplate = () => {
  emit('import');
};

const handleOpenExportModal = (template: TestTemplateDetail) => {
  emit('export', template);
};

</script>

<template>
  <div class="flex flex-col">
    <div class="flex justify-between items-center mb-2 space-x-2">
      <Input
      :value="searchKeyword"
      :placeholder="t('common.placeholders.searchKeyword')"
      class="w-70"
      @change="handleSearchKeywordChange" />
      <div class="flex-1"></div>
      <Button type="primary" @click="handleImportTemplate">
        <Icon icon="icon-shangchuan" class="mr-1" />
        导入模版
      </Button>
      <Button type="primary" @click="handleAdd">
        <Icon icon="icon-jia" class="mr-1" />
        {{ t('testTemplate.actions.addTemplate') }}
      </Button>
    </div>

    <div v-if="!loading && showData.length === 0" class="flex-1 flex items-center justify-center py-20">
      <NoData />
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2  gap-4 text-3 template-list-wrapper">
      <Card
        v-for="template in showData"
        :key="template.id"
        class="template-card"
        :class="{ 'system-template': template.isSystem }">
        <template #title>
          <div class="flex items-center justify-between">
            <div class="flex items-center space-x-2 flex-1 min-w-0">
              <span class="font-semibold text-3.5 truncate text-theme-special" @click="handleView(template)">{{ template.name }}</span>
            </div>
            <Button type="link" size="small" @click="handleOpenExportModal(template)">
              <Icon icon="icon-daochu1" class="text-4" />
            </Button>
            <Dropdown
              v-if="!template.isSystem"
              :trigger="['click']">
              <template #overlay>
                <Menu @click="({ key }) => handleDropdownClick(template, key as string)">
                  <Menu.Item
                    v-for="item in getDropdownItems(template)"
                    :key="item.key"
                    :danger="item.danger">
                    {{ item.label }}
                  </Menu.Item>
                </Menu>
              </template>
               <Icon icon="icon-gengduo" />
            </Dropdown>
          </div>
        </template>

        <div class="flex flex-col space-y-2 text-3 h-full justify-between">
          <div v-if="template.templateType === TestTemplateType.TEST_CASE || template.templateType === 'ISSUE'" class="truncate">
            <richTextContent
              :value="template.templateContent?.description"
              :emptyText="t('common.noDescription')" />
          </div>
          <div class="truncate" v-if="template.templateType === TestTemplateType.TEST_PLAN">
            <richTextContent
              :value="template.templateContent?.otherInformation"
              :emptyText="t('common.noDescription')" />
          </div>
          <div class="flex items-center justify-between flex-wrap">
            <div class="flex items-center space-x-1 mt-2">
              <Tag :color="getTemplateTypeColor(template.templateType)">
                {{ getTemplateTypeText(template.templateType) }}
              </Tag>
              <Tag :color="template.isSystem ? 'orange' : 'default'">
                {{ template.isSystem ? t('testTemplate.messages.systemTemplate') : t('testTemplate.messages.customTemplate') }}
              </Tag>
            </div>
            <div class="ml-2 mt-2">
              {{ t('common.createdDate') }}: {{ template.createdDate }}
            </div>
          </div>
        </div>
      </Card>
    </div>
  </div>
</template>

<style scoped>
.template-card {
  transition: all 0.3s;
}

.template-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.system-template {
  border-color: #ffa940;
}
:deep(.ant-card-body) {
  height: calc(100% - 55px);
}


@media (max-width: 1200px) {
  .template-list-wrapper {
    @apply lg:grid-cols-2
  }
}

@media (min-width: 1200px) {
  .template-list-wrapper {
    @apply lg:grid-cols-4
  }
}
</style>

