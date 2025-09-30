<script setup lang="ts">
import { defineAsyncComponent, onMounted, toRef, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { AsyncComponent, Hints, Icon, IconRefresh, Input, NoData, Spin } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { useData, useActions, useManagement } from './composables';
import type { TagProps } from './types';

// Initialize internationalization
const { t } = useI18n();

// Props definition with proper TypeScript support
const props = withDefaults(defineProps<TagProps>(), {
  projectId: '',
  userInfo: () => ({ id: '' }),
  appInfo: () => ({ id: '' }),
  notify: '',
  disabled: false
});

// Async component definitions for better code splitting
const CreateModal = defineAsyncComponent(() => import('./Add.vue'));

// Convert props to reactive references for composables
const projectIdRef = toRef(props, 'projectId');

// Initialize data management composable
const {
  dataList,
  loading,
  loaded,
  searchedFlag,
  total,
  showLoadMore,
  fetchTagList,
  loadMoreTags,
  refreshData,
  searchTags,
  resetData,
  addTagToList,
  removeTagFromList,
  updateTagInList
} = useData(projectIdRef);

// Initialize action management composable
const {
  modalVisible,
  createTag,
  updateTag,
  deleteTag,
  openCreateModal,
  closeCreateModal,
  handleCreateSuccess
} = useActions(
  refreshData,
  addTagToList,
  removeTagFromList,
  updateTagInList
);

// Initialize tag management composable with update callback
const handleTagUpdate = async (index: number, tagId: string, newName: string): Promise<boolean> => {
  return await updateTag([{ id: tagId, name: newName }], index);
};

const {
  searchValue,
  editId,
  handleSearchChange,
  clearSearch,
  startEdit,
  cancelEdit,
  handleEditEnter,
  handleEditBlur,
  resetState
} = useManagement(dataList, searchTags, handleTagUpdate);

/**
 * Handles the refresh button click
 * Resets pagination and fetches fresh data
 */
const handleRefresh = async (): Promise<void> => {
  await refreshData();
};

/**
 * Handles successful tag creation from modal
 * Updates local data and closes modal
 */
const handleTagCreateSuccess = (newTagData: { id: string; name: string }): void => {
  handleCreateSuccess(newTagData);
};

/**
 * Handles tag editing initiation
 * Starts edit mode for the specified tag
 */
const handleTagEdit = (tag: any): void => {
  startEdit(tag, props.disabled);
};

/**
 * Handles tag deletion
 * Shows confirmation and deletes the tag
 */
const handleTagDelete = (tag: any, index: number): void => {
  deleteTag(tag, index);
};

/**
 * Handles Enter key press during editing
 * Saves the edited tag name
 */
const handleTagEditEnter = async (tagId: string, index: number, event: { target: { value: string } }): Promise<void> => {
  await handleEditEnter(tagId, index, event);
};

/**
 * Handles blur event during editing
 * Auto-saves after a delay
 */
const handleTagEditBlur = (tagId: string, index: number, event: { target: { value: string } }): void => {
  handleEditBlur(tagId, index, event);
};

/**
 * Handles load more button click
 * Loads additional tags for pagination
 */
const handleLoadMore = async (): Promise<void> => {
  await loadMoreTags();
};

/**
 * Resets all component state
 * Used when switching projects or initializing
 */
const resetComponentState = (): void => {
  resetData();
  resetState();
  closeCreateModal();
};

// Lifecycle hooks and watchers

/**
 * Watches for project ID changes and initializes data
 * Resets state and fetches new data when project changes
 */
onMounted(() => {
  watch(projectIdRef, (newProjectId) => {
    resetComponentState();

    if (!newProjectId) {
      return;
    }

    // Fetch initial data for the new project
    fetchTagList();
  }, {
    immediate: true
  });
});
</script>
<template>
  <div class="w-full h-full leading-5 text-xs overflow-auto">
    <!-- Tag description section -->
    <div class="space-y-4">
      <div class="space-y-2">
        <div class="flex items-center space-x-2">
          <div class="w-1 h-4 bg-gradient-to-b from-green-500 to-green-600 rounded-full"></div>
          <span class="text-3.5 font-semibold">{{ t('tag.introduce.aboutTag') }}</span>
        </div>
        <div class="text-3.5 text-gray-700 ml-3 font-serif">{{ t('tag.introduce.description') }}</div>
      </div>

      <div class="space-y-2">
        <div class="flex items-center space-x-2">
          <div class="w-1 h-4 bg-gradient-to-b from-purple-500 to-purple-600 rounded-full"></div>
          <span class="text-3.5 font-semibold">{{ t('tag.messages.addedTags') }}</span>
        </div>

        <Spin
          :spinning="loading"
          class="flex flex-col min-h-96">
          <template v-if="loaded">
            <!-- Empty state when no tags exist -->
            <div v-if="!searchedFlag && dataList.length === 0" class="flex-1 flex flex-col items-center justify-center py-16">
              <img src="../../../assets/images/nodata.png" class="w-32 h-32 mb-4">
              <div v-if="!props.disabled" class="flex items-center text-gray-500 text-xs">
                <span>{{ t('tag.messages.noTags') }}</span>
                <Button
                  type="link"
                  size="small"
                  class="text-xs py-0 px-1 text-blue-600"
                  @click="openCreateModal">
                  {{ t('tag.acctions.addTag') }}
                </Button>
              </div>
              <div v-else class="text-gray-500 text-xs">
                {{ t('tag.messages.noTagsDescription') }}
              </div>
            </div>

            <!-- Tags list with search and actions -->
            <template v-else>
              <div class="flex items-center justify-between mt-2 mb-2">
                <div class="flex items-center">
                  <Input
                    v-model:value="searchValue"
                    :placeholder="t('tag.messages.tagNamePlaceholder')"
                    class="!w-60 mr-3"
                    trimAll
                    :allowClear="true"
                    :maxlength="50"
                    @change="handleSearchChange">
                    <template #suffix>
                      <Icon class="text-xs cursor-pointer text-gray-400" icon="icon-sousuo" />
                    </template>
                  </Input>
                  <Hints
                    v-if="!props.disabled"
                    :text="t('tag.messages.addTagHint')"
                    class="text-xs text-gray-500" />
                </div>

                <div class="flex items-center space-x-3">
                  <Button
                    v-if="!props.disabled"
                    type="primary"
                    size="small"
                    class="flex items-center space-x-1 text-xs"
                    @click="openCreateModal">
                    <Icon icon="icon-jia" class="text-xs" />
                    <span>{{ t('tag.actions.addTag') }}</span>
                  </Button>

                  <IconRefresh @click="handleRefresh">
                    <template #default>
                      <div class="flex items-center cursor-pointer text-gray-600 space-x-1 hover:text-gray-800 text-xs">
                        <Icon icon="icon-shuaxin" class="text-xs" />
                        <span>{{ t('actions.refresh') }}</span>
                      </div>
                    </template>
                  </IconRefresh>
                </div>
              </div>

              <!-- No data state for search results -->
              <NoData v-if="dataList.length === 0" class="flex-1 py-12" />

              <!-- Tag display area -->
              <div v-else class="flex items-center flex-wrap gap-2 mt-2">
                <div
                  v-for="(item, index) in dataList"
                  :key="item.id"
                  class="">
                  <!-- Inline edit mode -->
                  <div v-if="editId === item.id" class="flex items-center">
                    <Input
                      :placeholder="t('tag.tagNamePlaceholder')"
                      class="w-40 mr-2 text-xs"
                      trim
                      :value="item.name"
                      :allowClear="false"
                      :maxlength="50"
                      @blur="handleTagEditBlur(item.id, index, $event)"
                      @pressEnter="handleTagEditEnter(item.id, index, $event)" />
                    <Button
                      type="link"
                      size="small"
                      class="px-0 py-0 text-xs"
                      @click="cancelEdit">
                      {{ t('actions.cancel') }}
                    </Button>
                  </div>

                  <!-- Normal display mode -->
                  <div
                    v-else
                    class="flex items-center bg-gray-50 hover:bg-gray-100 rounded-lg px-3 py-1.5 cursor-pointer transition-colors group"
                    @dblclick="handleTagEdit(item)">
                    <Icon icon="icon-biaoqian1" class="mr-2 text-xs text-gray-500" />
                    <span :title="item.showTitle" class="truncate mr-2 text-xs text-gray-700">{{ item.showName }}</span>
                    <Icon
                      v-if="!props.disabled && item.hasEditPermission"
                      class="text-gray-400 hover:text-red-500 transition-colors opacity-0 group-hover:opacity-100 text-xs"
                      icon="icon-shanchuguanbi"
                      @click="handleTagDelete(item, index)" />
                  </div>
                </div>

                <!-- Load more button -->
                <Button
                  v-if="showLoadMore"
                  size="small"
                  type="link"
                  class="px-0 py-0 h-6 leading-6 text-xs text-blue-600"
                  @click="handleLoadMore">
                  {{ t('common.loadMore') }}
                </Button>
              </div>
            </template>
          </template>
        </Spin>
      </div>
    </div>

    <!-- Modal for create tag operation -->
    <AsyncComponent :visible="modalVisible">
      <CreateModal
        v-model:visible="modalVisible"
        :projectId="props.projectId"
        @ok="handleTagCreateSuccess" />
    </AsyncComponent>
  </div>
</template>
