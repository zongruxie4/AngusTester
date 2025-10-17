<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Arrow, Hints, Icon, IconRequired, Input, notification, Spin } from '@xcan-angus/vue-ui';
import { Button, Textarea, Tooltip, TypographyParagraph } from 'ant-design-vue';
import { services } from '@/api/tester';
import { regexpUtils, utils, duration } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';
import { OpenAPIV3_1 } from '@/types/openapi-types';
import { TagInfo } from '@/views/apis/services/services/types';

interface Props {
  visible: boolean;
  disabled: boolean;
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  disabled: true,
  id: ''
});

const { t } = useI18n();

const emit = defineEmits<{(e: 'update:visible', value:boolean): void}>();

// Data State
const tagList = ref<TagInfo[]>([]);
const originalTagList = ref<TagInfo[]>([]);
const isLoading = ref(false);
const isAddButtonDisabled = ref(false);
const currentEditingTag = ref<TagInfo>();
const lastExpandState = ref(false);
const tagListRef = ref<HTMLElement | null>();

// Template for new tag data
const newTagTemplate: TagInfo = {
  id: utils.uuid('api'),
  name: '',
  description: '',
  externalDocs: {
    url: '',
    ellipsis: false,
    showEllipsis: false,
    urlErr: {
      emptyUrl: false,
      errUrl: false
    }
  },
  isEdit: true,
  isExpand: true,
  isAdd: true,
  delLoading: false,
  saveLoading: false,
  nameErr: false,
  ellipsis: false,
  showEllipsis: false
};

/**
 * Load tag list from API
 */
const loadTagList = async () => {
  isLoading.value = true;
  const [error, { data }] = await services.getServicesSchemaTag(props.id);
  isLoading.value = false;
  if (error) {
    return;
  }

  // If no data, show empty template
  if (!data?.length) {
    tagList.value = [JSON.parse(JSON.stringify(newTagTemplate))];
    currentEditingTag.value = tagList.value[0];
    return;
  }

  // Map API data to component format
  tagList.value = data.map(item => ({
    ...item,
    id: utils.uuid('api'),
    isEdit: false,
    isAdd: false,
    isExpand: false,
    delLoading: false,
    saveLoading: false,
    nameErr: false,
    ellipsis: false,
    showEllipsis: false,
    externalDocs: {
      ...item.externalDocs,
      ellipsis: false,
      showEllipsis: false,
      urlErr: {
        emptyUrl: false,
        errUrl: false
      }
    }
  }));

  // Store original data for comparison
  originalTagList.value = JSON.parse(JSON.stringify(tagList.value));
  isAddButtonDisabled.value = false;
};

/**
 * Add new tag to the list
 */
const addNewTag = () => {
  const editingTags = tagList.value.filter(item => item.isEdit);
  if (editingTags?.length) {
    const validationResult = validateTagData(editingTags[0]);
    if (validationResult) {
      return;
    }
    if (checkUnsavedChanges()) {
      return;
    }
  }

  // If first item is already being added, reset it
  if (tagList.value[0]?.isAdd) {
    tagList.value[0] = { ...JSON.parse(JSON.stringify(newTagTemplate)), id: tagList.value[0].id };
    return;
  }

  // Add new tag at the beginning
  tagList.value.unshift(JSON.parse(JSON.stringify(newTagTemplate)));
  currentEditingTag.value = tagList.value[0];
  isAddButtonDisabled.value = true;
  setOtherTagsToNonEdit(tagList.value, currentEditingTag.value.id);
};

/**
 * Delete tag from list
 */
const deleteTag = async (tag: TagInfo) => {
  if (tag.delLoading) {
    return;
  }

  // If it's a new tag, just remove from list
  if (tag.isAdd) {
    if (tagList.value.length === 1) {
      return;
    }
    tagList.value = tagList.value.filter(item => item.id !== tag.id);
    return;
  }

  // Delete from API
  isLoading.value = true;
  const [error] = await services.delServicesSchemaTag(props.id, [tag.name]);
  isLoading.value = false;
  if (error) {
    return;
  }

  notification.success(t('actions.tips.deleteSuccess'));
  tagList.value = tagList.value.filter(item => item.id !== tag.id);
  originalTagList.value = originalTagList.value.filter(item => item.id !== tag.id);

  // If no tags left, add empty template
  if (tagList.value.length === 0) {
    tagList.value.unshift(JSON.parse(JSON.stringify(newTagTemplate)));
  }
};

/**
 * Save tag changes
 */
const saveTag = (tag: TagInfo) => {
  const validationResult = validateTagData(tag);
  if (validationResult) {
    return;
  }

  // Check for duplicate names in new tags
  if (tag.isAdd) {
    const duplicateCount = tagList.value.filter(item => item.name === tag.name)?.length;
    if (duplicateCount >= 2) {
      notification.warning(t('service.tag.messages.nameExists'));
      tag.nameErr = true;
      return;
    }
  } else {
    // Check if data has changed for existing tags
    if (!hasDataChanged(tag)) {
      tag.isEdit = false;
      tag.isExpand = lastExpandState.value;
      return;
    }
  }
  saveTagToAPI(tag);
};

/**
 * Save tag data to API
 */
const saveTagToAPI = async (tag: TagInfo) => {
  // Check for duplicate names in new tags
  if (tag.isAdd) {
    const duplicateCount = tagList.value.filter(item => item.name === tag.name)?.length;
    if (duplicateCount >= 2) {
      notification.warning(t('service.tag.messages.nameExists'));
      tag.nameErr = true;
      return;
    }
  } else {
    // Check if data has changed for existing tags
    if (!hasDataChanged(tag)) {
      tag.isEdit = false;
      tag.isExpand = lastExpandState.value;
      return;
    }
  }

  // Build API parameters
  let params: OpenAPIV3_1.TagObject = {
    name: tag.name
  };
  if (tag.description) {
    params.description = tag.description;
  }
  if (tag.externalDocs.url) {
    params = {
      ...params,
      externalDocs: {
        url: tag.externalDocs.url
      }
    };
    if (tag.externalDocs.description) {
      params = {
        ...params,
        externalDocs: {
          url: tag.externalDocs.url,
          description: tag.externalDocs.description
        }
      };
    }
  }

  isLoading.value = true;
  const [error] = await services.addServicesSchemaTag(props.id, params);
  isLoading.value = false;
  if (error) {
    return false;
  }

  originalTagList.value = JSON.parse(JSON.stringify(tagList.value));
  tag.isEdit = false;
  tag.isExpand = lastExpandState.value;
  isAddButtonDisabled.value = false;
  currentEditingTag.value = undefined;
  notification.success(t('actions.tips.saveSuccess'));
  if (tag.isAdd) {
    tag.isAdd = false;
  }
};

/**
 * Validate tag data for required fields
 */
const validateTagData = (tag: TagInfo): boolean => {
  let hasEmpty = false;
  if (!tag.name) {
    tag.nameErr = true;
    hasEmpty = true;
  }

  if (tag.externalDocs.description) {
    if (!tag.externalDocs.url) {
      tag.externalDocs.urlErr.emptyUrl = true;
      hasEmpty = true;
    }
  }
  return hasEmpty;
};

/**
 * Check if there are unsaved changes in current editing tag
 */
const checkUnsavedChanges = () => {
  if (currentEditingTag.value) {
    const hasUpdate = hasDataChanged(tagList.value.filter(item => item.id === currentEditingTag.value?.id)[0]);
    if (hasUpdate) {
      notification.warning(t('service.tag.messages.dataNotSaved'));
      return true;
    }
  }
  return false;
};

/**
 * Check if tag data has changed from original
 */
const hasDataChanged = (newData: TagInfo) => {
  const originalDataList = originalTagList.value.filter(item => item?.id === newData?.id);
  if (!originalDataList.length) {
    return false;
  }
  const originalData = {
    name: originalDataList[0].name,
    description: originalDataList[0]?.description || '',
    externalDocs: {
      description: originalDataList[0].externalDocs?.description || '',
      url: originalDataList[0]?.externalDocs?.url || ''
    },
    id: originalDataList[0].id
  };
  const currentData = {
    name: newData.name,
    description: newData?.description || '',
    externalDocs: {
      description: newData.externalDocs?.description || '',
      url: newData?.externalDocs?.url || ''
    },
    id: newData.id
  };
  return !utils.deepCompare(originalData, currentData);
};

/**
 * Toggle tag expand/collapse state
 */
const toggleTagExpand = (tag: TagInfo) => {
  const editingTags = tagList.value.filter(item => item.isEdit);
  if (editingTags?.length) {
    if (editingTags[0].isAdd) {
      tagList.value = tagList.value.filter(item => item.id !== editingTags[0].id);
    } else {
      const validationResult = validateTagData(editingTags[0]);
      if (validationResult) {
        return;
      }
      if (checkUnsavedChanges()) {
        return;
      }
    }
  }

  tag.isExpand = !tag.isExpand;
  if (!tag.isExpand) {
    tag.isEdit = false;
  }
  setOtherTagsToNonEdit(tagList.value, tag.id);
  isAddButtonDisabled.value = false;
};

/**
 * Start editing a tag
 */
const startEditingTag = (tag: TagInfo) => {
  const editingTags = tagList.value.filter(item => item.isEdit);
  if (editingTags?.length) {
    if (editingTags[0].isAdd) {
      tagList.value = tagList.value.filter(item => item.id !== editingTags[0].id);
      isAddButtonDisabled.value = false;
    } else {
      const validationResult = validateTagData(editingTags[0]);
      if (validationResult) {
        return;
      }
      if (checkUnsavedChanges()) {
        return;
      }
    }
  }

  lastExpandState.value = tag.isExpand;
  tag.isEdit = true;
  tag.isExpand = true;
  tag.showEllipsis = false;
  tag.ellipsis = false;
  tag.externalDocs.showEllipsis = false;
  tag.externalDocs.ellipsis = false;
  currentEditingTag.value = tag;
  setOtherTagsToNonEdit(tagList.value, tag.id);
  isAddButtonDisabled.value = false;
};

/**
 * Cancel editing a tag
 */
const cancelEditingTag = (tag: TagInfo) => {
  // If canceling a new tag
  if (tag.isAdd) {
    if (tagList.value.length === 1) {
      emit('update:visible', false);
      return;
    }
    tagList.value = tagList.value.filter(item => item.id !== tag.id);
    isAddButtonDisabled.value = false;
    currentEditingTag.value = undefined;
    return;
  }

  // If canceling an existing tag, restore original data if changed
  const hasUpdate = hasDataChanged(tag);
  if (hasUpdate) {
    const originalTag = originalTagList.value.find(item => item.id === tag.id);
    for (let i = 0; i < tagList.value.length; i++) {
      if (tag.id === tagList.value[i].id) {
        tagList.value[i] = originalTag ? JSON.parse(JSON.stringify(originalTag)) : tag;
        tagList.value[i].isEdit = false;
        tagList.value[i].isExpand = lastExpandState.value;
        break;
      }
    }
  }

  tag.isEdit = false;
  tag.isExpand = lastExpandState.value;
  currentEditingTag.value = undefined;
  isAddButtonDisabled.value = false;
};

/**
 * Set all other tags to non-edit state except the specified one
 */
const setOtherTagsToNonEdit = (arr: TagInfo[], id: string): void => {
  for (let i = 0; i < arr.length; i++) {
    if (arr[i].id !== id) {
      arr[i].isEdit = false;
      arr[i].isExpand = false;
    }
  }
};

// Event Handlers
const handleTagNameChange = debounce(duration.search, (value: string, tag: TagInfo) => {
  tag.nameErr = !value;
});

const handleExternalDocsUrlChange = debounce(duration.search, (value: string, tag: TagInfo) => {
  if (!value) {
    tag.externalDocs.urlErr.emptyUrl = true;
    return;
  }

  tag.externalDocs.urlErr.emptyUrl = false;
  if (regexpUtils.isUrl(value)) {
    tag.externalDocs.urlErr.errUrl = false;
    return;
  }
  tag.externalDocs.urlErr.errUrl = true;
});

const handleDescriptionExpand = (event, tag: TagInfo) => {
  event.stopPropagation();
  tag.ellipsis = !tag.ellipsis;
};

const getDescriptionEllipsisConfig = (tag: TagInfo) => {
  return {
    rows: 3,
    onEllipsis: (ellipsis) => {
      tag.showEllipsis = ellipsis;
    }
  };
};

const handleExternalDocsDescriptionExpand = (event, tag: TagInfo) => {
  event.stopPropagation();
  tag.externalDocs.ellipsis = !tag.externalDocs.ellipsis;
};

const getExternalDocsDescriptionEllipsisConfig = (tag: TagInfo) => {
  return {
    rows: 3,
    onEllipsis: (ellipsis) => {
      tag.externalDocs.showEllipsis = ellipsis;
    }
  };
};

// Lifecycle
onMounted(() => {
  loadTagList();
});
</script>
<template>
  <Spin class="h-full flex flex-col" :spinning="isLoading">
    <Hints :text="t('service.tag.hints.main')" />
    <Hints :text="t('service.tag.hints.sub')" class="mt-2 hints-text" />
    <div class="mt-2">
      <Button
        size="small"
        type="primary"
        class="flex items-center"
        :disabled="props.disabled || tagList.length > 1999 || isAddButtonDisabled"
        @click="addNewTag">
        <Icon icon="icon-jia" class="mr-1" />
        {{ t('actions.add') }}
      </Button>
    </div>
    <div
      ref="tagListRef"
      style="min-height: 400px;scrollbar-gutter: stable;"
      class="flex-1 overflow-y-auto flex flex-col space-y-2 pr-1.5 -mr-3.5 mb-5 text-3 text-text-content">
      <div
        v-for="tag in tagList"
        :key="tag.id"
        class="border border-border-divider p-2 rounded mt-2">
        <div v-if="!tag.isAdd && !tag.isEdit">
          <div class="flex items-start justify-between -mt-1 leading-5">
            <div class="mr-2 mt-1.5 truncate cursor-pointer flex items-center" @click="toggleTagExpand(tag)">
              <Icon icon="icon-biaoqian" class="mr-1 flex-none" />
              <div class="truncate flex-1">
                <Tooltip
                  :overlayStyle="{zIndex: 9999}"
                  :title="tag.name"
                  placement="topLeft">
                  {{ tag.name }}
                </Tooltip>
              </div>
            </div>
            <div class="flex flex-none items-center mt-1.75 leading-5">
              <Tooltip :title="t('actions.edit')" placement="top">
                <template v-if="props.disabled">
                  <Icon
                    icon="icon-shuxie"
                    class="mr-1 cursor-not-allowed text-text-disabled" />
                </template>
                <template v-else>
                  <Icon
                    icon="icon-shuxie"
                    class="mr-1"
                    :class="tag.isAdd?'text-text-placeholder cursor-not-allowed':'hover:text-text-link-hover cursor-pointer'"
                    @click="startEditingTag(tag)" />
                </template>
              </Tooltip>
              <Tooltip :title="t('actions.delete')" placement="top">
                <template v-if="props.disabled">
                  <Icon
                    icon="icon-qingchu"
                    class="mr-1 cursor-not-allowed text-text-disabled text-3.5" />
                </template>
                <template v-else>
                  <Icon
                    icon="icon-qingchu"
                    class="mr-1 text-3.5"
                    :class="tag.isAdd?'text-text-placeholder cursor-not-allowed':'hover:text-text-link-hover cursor-pointer'"
                    @click="deleteTag(tag)" />
                </template>
              </Tooltip>
              <Arrow
                :open="tag.isExpand"
                @click="toggleTagExpand(tag)" />
            </div>
          </div>
        </div>
        <div
          :class="tag.isExpand ? 'open-info' : 'stop-info'"
          class="transition-height duration-500 overflow-hidden leading-3 text-3">
          <template v-if="tag.isExpand">
            <div v-if="tag.isEdit" class="flex-1 text-3">
              <div><IconRequired />{{ t('common.name') }}</div>
              <Input
                v-model:value="tag.name"
                :placeholder="t('common.placeholders.searchKeyword')"
                size="small"
                class="mt-2 mb-5"
                :maxlength="200"
                :error="tag.nameErr"
                :disabled="!tag.isAdd"
                @change="(event)=>handleTagNameChange(event.target.value,tag)" />
            </div>
            <template v-if="tag.isEdit">
              <div class="pl-1.75">{{ t('common.description') }}</div>
              <Textarea
                v-model:value="tag.description"
                size="small"
                class="mt-2 mb-5"
                :placeholder="t('service.tag.form.descriptionPlaceholder')"
                :rows="3"
                :maxlength="400"
                :disabled="!tag.isEdit" />
            </template>
            <template v-else>
              <template v-if="tag.description">
                <TypographyParagraph
                  class="break-all whitespace-break-spaces leading-5 mt-1"
                  :ellipsis="tag.ellipsis ? false : getDescriptionEllipsisConfig(tag)"
                  :content="tag.description"
                  :copyable="tag.showEllipsis?{ tooltip: false }:false">
                  <template v-if="tag.showEllipsis" #copyableIcon>
                    <a @click="(e)=>handleDescriptionExpand(e,tag)">{{ tag.ellipsis ? t('actions.collapse') : t('actions.expand') }}</a>
                  </template>
                </TypographyParagraph>
              </template>
            </template>
            <div v-if="tag.isExpand && !tag.isEdit && tag.externalDocs.url" class="border-t border-dashed border-border-divider mt-2"></div>
            <div v-if="tag.isEdit" class="flex-1">
              <div class="pl-1.75">{{ t('service.tag.form.externalDocsUrl') }}</div>
              <Input
                v-model:value="tag.externalDocs.url"
                :placeholder="t('service.tag.form.externalDocsUrlPlaceholder')"
                size="small"
                class="mt-2"
                :maxlength="400"
                :error="tag.externalDocs.urlErr.emptyUrl || tag.externalDocs.urlErr.errUrl"
                @change="(event)=>handleExternalDocsUrlChange(event.target.value,tag)" />
              <div v-if="tag.isEdit" class="text-rule text-3 h-5">
                <template v-if="tag.externalDocs.urlErr.emptyUrl">{{ t('service.tag.validation.enterUrl') }}</template>
                <template v-if="!tag.externalDocs.urlErr.emptyUrl && tag.externalDocs.urlErr.errUrl">
                  {{ t('service.tag.validation.enterCorrectUrl') }}
                </template>
              </div>
            </div>
            <template v-else>
              <div v-if="tag.externalDocs.url" class="mt-2 mb-1 leading-5">
                <Icon icon="icon-lianjie2" class="text-3 mr-1 -mt-0.5" />
                {{ t('service.tag.form.externalDocsLink') }}
              </div>
              <a
                :href="tag.externalDocs.url"
                target="_blank"
                class="break-all whitespace-pre-wrap cursor-pointer text-text-link leading-5">
                {{ tag.externalDocs.url }}
              </a>
            </template>
            <template v-if="tag.isEdit">
              <div class="pl-1.75">{{ t('service.tag.form.externalDocsDescription') }}</div>
              <Textarea
                v-model:value="tag.externalDocs.description"
                size="small"
                class="mt-2"
                :placeholder="t('service.tag.form.externalDocsDescriptionPlaceholder')"
                :rows="3"
                :maxlength="400" />
            </template>
            <template v-else>
              <template v-if="tag.externalDocs?.description">
                <TypographyParagraph
                  class="break-all whitespace-break-spaces leading-5 mt-1"
                  :ellipsis="getExternalDocsDescriptionEllipsisConfig(tag)"
                  :content="tag.externalDocs.description"
                  :copyable="tag.externalDocs.showEllipsis ? { tooltip: false }:false">
                  <template v-if="tag.externalDocs.showEllipsis" #copyableIcon>
                    <a @click="(e)=>handleExternalDocsDescriptionExpand(e,tag)">
                      {{ tag.externalDocs.ellipsis ? t('actions.collapse') : t('actions.expand') }}
                    </a>
                  </template>
                </TypographyParagraph>
              </template>
            </template>
            <div class="flex justify-end" :class="tag.isEdit?'mt-3':''">
              <template v-if="tag.isEdit">
                <Button
                  type="link"
                  size="small"
                  class="mr-2 px-0"
                  :disabled="tagList.length === 1 && tag.isAdd"
                  @click="cancelEditingTag(tag)">
                  {{ t('actions.cancel') }}
                </Button>
                <Button
                  size="small"
                  type="link"
                  class="px-0"
                  :disabled="props.disabled || tag.saveLoading || !tagList.length"
                  @click="saveTag(tag)">
                  {{ t('actions.save') }}
                </Button>
              </template>
            </div>
          </template>
        </div>
      </div>
    </div>
  </Spin>
</template>
<style scoped>
.open-info {
  height: auto;
}

.stop-info {
  height: 0;
}

:deep(.hints-text svg) {
  visibility: hidden;
}

:deep(.v-note-wrapper .v-note-read-model) {
  width: 264px;
  padding: 8px;
}
</style>
