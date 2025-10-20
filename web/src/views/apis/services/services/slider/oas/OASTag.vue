<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Arrow, Hints, Icon, Spin } from '@xcan-angus/vue-ui';
import { Tooltip, TypographyParagraph } from 'ant-design-vue';
import { services } from '@/api/tester';
import { utils } from '@xcan-angus/infra';
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
 * Toggle tag expand/collapse state
 */
const toggleTagExpand = (tag: TagInfo) => {

  tag.isExpand = !tag.isExpand;
  setOtherTagsToNonEdit(tagList.value, tag.id);
  isAddButtonDisabled.value = false;
};

/**
 * Set all other tags to non-edit state except the specified one
 */
const setOtherTagsToNonEdit = (arr: TagInfo[], id: string): void => {
  for (let i = 0; i < arr.length; i++) {
    if (arr[i].id !== id) {
      arr[i].isExpand = false;
    }
  }
};

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
    <div
      ref="tagListRef"
      style="min-height: 400px;scrollbar-gutter: stable;"
      class="flex-1 overflow-y-auto flex flex-col space-y-2 pr-1.5 -mr-3.5 mb-5 text-3 text-text-content">
      <div
        v-for="tag in tagList"
        :key="tag.id"
        class="border border-border-divider p-2 rounded mt-2">
        <div>
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

            <div v-if="tag.isExpand && tag.externalDocs.url" class="border-t border-dashed border-border-divider mt-2"></div>

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
