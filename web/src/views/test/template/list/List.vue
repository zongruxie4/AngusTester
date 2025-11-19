<script setup lang="ts">
import { Button, Card, Dropdown, Menu, Tag } from 'ant-design-vue';
import { Icon, IconMore, NoData } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { TestTemplateDetail } from '../types';
import { TestTemplateType } from '@/enums/enums';

// Props definition
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
}>();

const { t } = useI18n();

/**
 * Get template type display text
 */
const getTemplateTypeText = (type: string) => {
  return type === TestTemplateType.TEST_PLAN 
    ? t('xcm.enum.TestTemplateType.TEST_PLAN') 
    : t('xcm.enum.TestTemplateType.TEST_CASE');
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
  const items = [];
  if (!template.isSystem) {
    items.push({
      key: 'edit',
      label: t('common.edit')
    });
    items.push({
      key: 'delete',
      label: t('common.delete'),
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
</script>

<template>
  <div class="flex flex-col space-y-4">
    <div class="flex justify-end mb-2">
      <Button type="primary" @click="handleAdd">
        <Icon name="icon-plus" />
        {{ t('testTemplate.actions.addTemplate') }}
      </Button>
    </div>

    <div v-if="!loading && dataList.length === 0" class="flex-1 flex items-center justify-center py-20">
      <NoData />
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <Card
        v-for="template in dataList"
        :key="template.id"
        class="template-card"
        :class="{ 'system-template': template.isSystem }">
        <template #title>
          <div class="flex items-center justify-between">
            <div class="flex items-center space-x-2 flex-1 min-w-0">
              <span class="font-semibold truncate">{{ template.name }}</span>
              <Tag v-if="template.isSystem" color="orange">
                {{ t('testTemplate.messages.systemTemplate') }}
              </Tag>
            </div>
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
              <IconMore class="cursor-pointer" />
            </Dropdown>
          </div>
        </template>

        <div class="flex flex-col space-y-2">
          <div class="flex items-center space-x-2">
            <Tag :color="getTemplateTypeColor(template.templateType)">
              {{ getTemplateTypeText(template.templateType) }}
            </Tag>
          </div>

          <div class="text-sm text-theme-sub-content">
            <div v-if="template.createdByName">
              {{ t('common.createdBy') }}: {{ template.createdByName }}
            </div>
            <div v-if="template.createdDate">
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
</style>

