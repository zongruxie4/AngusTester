<script lang="ts" setup>
// Vue composition API imports
import { defineAsyncComponent, onMounted, computed, ref, onUnmounted } from 'vue';
import { useI18n } from 'vue-i18n';

// Ant Design components
import { Button, Pagination, TabPane, Tabs, Tag, Tooltip, Popover } from 'ant-design-vue';

// Custom UI components
import {
  ActivityTimeline, DropdownSort, Hints, Icon, IconRefresh,
  Image, Input, Spin, Dropdown, NoData
} from '@xcan-angus/vue-ui';

// API and utilities
import { PageQuery, User, appContext } from '@xcan-angus/infra';
import DefaultProjectImage from '@/views/project/project/images/default.png';

// Composables
import { useData, useActions } from '../composables';
import type { Project } from '../types';

// Initialize i18n
const { t } = useI18n();

// Type definitions
type OrderByKey = 'createdDate' | 'createdByName';

type Props = {
  projectId: string;
  userInfo: User;
  appInfo: { id: number | undefined; };
}

// Props and emits
const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined
});

const emits = defineEmits<{(e: 'delOk', value: string[]);}>();

// Async component definitions
const Introduce = defineAsyncComponent(() => import('@/views/project/project/list/introduce.vue'));
const RichText = defineAsyncComponent(() => import('@/components/richEditor/textContent/index.vue'));

// Initialize composables
const {
  loading,
  keyword,
  data,
  orderBy,
  orderSort,
  pagination,
  fetchProjectList,
  handleSearch,
  updateSort,
  handlePageChange,
  refreshData
} = useData();

const {
  addProjectTab,
  editProjectTab,
  openDetailTab,
  deleteProject,
  canEditProject,
  canDeleteProject
} = useActions({
  onDataChange: refreshData
});

// Sort menu configuration
const sortMenuItems = computed(() => [
  {
    name: t('project.sortMenu.createdDate'),
    key: 'createdDate' as OrderByKey,
    orderSort: PageQuery.OrderSort.Desc
  },
  {
    name: t('project.sortMenu.createdByName'),
    key: 'createdByName' as OrderByKey,
    orderSort: PageQuery.OrderSort.Asc
  }
]);

// More actions menu configuration
const moreButton = computed(() => [
  {
    key: 'module',
    name: t('project.moreButton.module')
  },
  {
    key: 'version',
    name: t('project.moreButton.version')
  },
  {
    key: 'biaoqian',
    name: t('project.moreButton.tag')
  }
]);

const activityType = [];

// Event handlers using composables
const handleKeywordChange = (e: any): void => {
  const value = e.target ? e.target.value : e;
  handleSearch(value);
};

const handleSortChange = (value: {
  orderBy: OrderByKey;
  orderSort: PageQuery.OrderSort;
}): void => {
  updateSort({
    orderBy: value.orderBy,
    orderSort: value.orderSort
  });
};

const handleDeleteProject = async (projectData: Project): Promise<void> => {
  const success = await deleteProject(projectData, pagination, props.projectId);
  if (success) {
    emits('delOk', [projectData.id!, `${projectData.id}_detail`, `${projectData.id}_project`]);
  }
};

const handleEditProject = (projectData: Project, key?: string): void => {
  editProjectTab(projectData, key || 'basic');
};

const handleOpenDetailTab = (projectData: Project): void => {
  openDetailTab(projectData);
};

// Check user permissions
const canUserEditProject = (projectData: Project): boolean => {
  return canEditProject(projectData, String(props.userInfo?.id || ''), (appContext.isAppAdmin() || appContext.isSysAdmin()));
};

const canUserDeleteProject = (projectData: Project): boolean => {
  return canDeleteProject(projectData, String(props.userInfo?.id || ''), (appContext.isAppAdmin() || appContext.isSysAdmin()));
};

/**
 * <p>Dynamic height calculation for activity timeline</p>
 * <p>Automatically calculates remaining screen height</p>
 */
const timelineHeight = ref('calc(100vh - 400px)');

/**
 * <p>Calculate dynamic height based on screen size and other elements</p>
 * <p>Updates height when window resizes</p>
 */
const calculateHeight = () => {
  const viewportHeight = window.innerHeight;
  const headerHeight = 64; // Estimated header height
  const padding = 40; // Page padding
  const otherElementsHeight = 260; // Estimated height of other elements (statistics cards, charts, etc.)

  const availableHeight = viewportHeight - headerHeight - padding - otherElementsHeight;
  const finalHeight = Math.max(availableHeight, 350); // Minimum height 350px

  timelineHeight.value = `${finalHeight}px`;
};

/**
 * <p>Handle window resize for responsive height</p>
 */
const handleResize = () => {
  calculateHeight();
};

// Lifecycle hooks
onMounted(() => {
  fetchProjectList();
  calculateHeight();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
});

// Expose methods for parent component
defineExpose({
  refresh: refreshData
});
</script>
<template>
  <div class="p-5 overflow-y-auto text-3 flex flex-col h-full">
    <Introduce class="mb-5" />
    <div class="flex space-x-6 min-h-0 flex-1">
      <div class="flex-1 space-y-2 mr-8 min-w-0">
        <div class="text-3.5 font-semibold mb-1">{{ t('project.addedProjects') }}</div>
        <Spin
          :spinning="loading"
          :delay="0"
          class="pb-0.5">
          <div class="w-full flex items-center justify-between mb-3.5">
            <div class="flex items-center mr-3.5">
              <Input
                v-model:value="keyword"
                class="w-75"
                trim
                :maxlength="100"
                :allowClear="true"
                :placeholder="t('project.searchPlaceholder')"
                @change="handleKeywordChange" />
              <Hints :text="t('project.adminHint')" class="ml-2" />
            </div>

            <div class="flex items-center space-x-3">
              <Button
                type="primary"
                size="small"
                @click="addProjectTab">
                <Icon icon="icon-jia" />
                <span class="ml-1">{{ t('project.addProject') }}</span>
              </Button>

              <DropdownSort
                v-model:orderBy="orderBy"
                v-model:orderSort="orderSort"
                :menuItems="sortMenuItems"
                @click="handleSortChange">
                <div class="flex items-center cursor-pointer text-theme-content space-x-1 text-theme-text-hover">
                  <Icon icon="icon-shunxu" />
                  <span>{{ t('project.sort') }}</span>
                </div>
              </DropdownSort>

              <IconRefresh
                :loading="loading"
                :disabled="loading"
                @click="refreshData">
                <template #default>
                  <div class="flex items-center cursor-pointer text-theme-content space-x-1 text-theme-text-hover">
                    <Icon icon="icon-shuaxin" />
                    <span class="ml-1">{{ t('actions.refresh') }}</span>
                  </div>
                </template>
              </IconRefresh>
            </div>
          </div>

          <div class="project-list-container">
            <div
              v-for="item in data"
              :key="item.id"
              class="project-item">
              <div class="project-avatar-section">
                <Image
                  :src="item.avatar"
                  :defaultImg="DefaultProjectImage"
                  class="project-avatar"
                  :style="'width: 52px; height: 52px; object-fit: cover; border-radius: 50%;'" />
              </div>

              <div class="project-main-content">
                <div class="project-header-row">
                  <div
                    class="project-name"
                    :title="item.name"
                    @click="handleOpenDetailTab(item)">
                    {{ item.name }}
                  </div>
                  <Tag class="project-type-tag">{{ item.type?.message || 'Agile Project' }}</Tag>
                  <div class="project-id-text" :title="item.id">ID: <span>{{ item.id }}</span></div>
                </div>

                <div class="project-description-row">
                  <p
                    v-if="item.description"
                    class="project-description-text">
                    <RichText :value="item.description" />
                  </p>
                  <p v-else class="project-no-description-text">{{ t('project.noDescription') }}</p>
                </div>
              </div>

              <div class="project-details-section">
                <div class="project-members-info">
                  <div class="owner-info">
                    <span class="info-label">{{ t('project.owner') }}: </span>
                    <span class="info-value">{{ item.ownerName }}</span>
                  </div>

                  <div class="members-info">
                    <span class="info-label">{{ t('project.members') }}: </span>
                    <div class="members-avatars-container">
                      <Tooltip
                        v-for="(avatars, idx) in item.showMembers?.USER || []"
                        :key="idx"
                        :title="avatars.name">
                        <template #title>
                          <span>{{ avatars.name }}</span>
                        </template>
                        <div class="member-avatar">
                          <Image
                            class="w-full"
                            type="avatar"
                            :src="avatars.avatar" />
                        </div>
                      </Tooltip>

                      <Tooltip
                        v-for="(avatars, idx) in item.showMembers?.GROUP || []"
                        :key="idx"
                        :title="t('project.group')">
                        <template #title>
                          <span>{{ t('project.group') }}</span>
                        </template>
                        <Tag class="group-tag">{{ avatars.name }}</Tag>
                      </Tooltip>

                      <Tooltip
                        v-for="(avatars, idx) in item.showMembers?.DEPT || []"
                        :key="idx"
                        :title="t('project.department')">
                        <template #title>
                          <span>{{ t('project.department') }}</span>
                        </template>
                        <div>
                          <Tag class="dept-tag">{{ avatars.name }}</Tag>
                        </div>
                      </Tooltip>

                      <Popover v-if="(item.membersNum || 0) > 10">
                        <template #title>{{ t('project.allMembers') }}</template>
                        <template #content>
                          <div class="space-y-4 max-w-100">
                            <div v-if="item.members?.USER?.length" class="flex">
                              <span class="w-15 text-right">{{ t('project.user') }}：</span>
                              <div class="flex flex-1 flex-wrap">
                                <div
                                  v-for="(avatars, idx) in item.members.USER || []"
                                  :key="idx"
                                  class="mb-2 inline-flex space-x-2 pr-4">
                                  <Image
                                    class="w-5 h-5 rounded-full "
                                    type="avatar"
                                    :src="avatars.avatar" />
                                  <span>{{ avatars.name }}</span>
                                </div>
                              </div>
                            </div>

                            <div v-if="item.members?.GROUP?.length" class="flex">
                              <span class="w-15 text-right">{{ t('project.group') }}：</span>
                              <div class="flex flex-1 flex-wrap">
                                <div
                                  v-for="(avatars, idx) in item.members?.GROUP || []"
                                  :key="idx"
                                  class="mb-2 pr-3">
                                  <Tag class="h-5 leading-5">{{ avatars.name }}</Tag>
                                </div>
                              </div>
                            </div>

                            <div v-if="item.members?.DEPT?.length" class="flex">
                              <span class="w-15 text-right">{{ t('project.department') }}：</span>
                              <div class="flex flex-1 flex-wrap">
                                <div
                                  v-for="(avatars, idx) in item.members?.DEPT || []"
                                  :key="idx"
                                  class="mb-2 pr-3">
                                  <Tag class="h-5 leading-5">{{ avatars.name }}</Tag>
                                </div>
                              </div>
                            </div>
                          </div>
                        </template>
                        <div class="more-members-indicator">...</div>
                      </Popover>
                    </div>
                  </div>
                </div>

                <div class="project-time-info">
                  <span class="info-label">{{ t('project.modifyTime') }}: </span>
                  <span class="info-value">{{ item.lastModifiedDate }}</span>
                </div>
              </div>

              <div class="project-actions-section">
                <div class="edit-action-row">
                  <Button
                    :title="t('actions.edit')"
                    size="small"
                    type="text"
                    :disabled="!canUserEditProject(item)"
                    class="action-button edit-action"
                    @click="handleEditProject(item)">
                    <Icon icon="icon-bianji" class="text-3.5 cursor-pointer text-theme-text-hover" />
                    {{ t('actions.edit') }}
                  </Button>
                </div>

                <div class="delete-action-row">
                  <Button
                    :title="t('actions.delete')"
                    size="small"
                    type="text"
                    :disabled="!canUserDeleteProject(item)"
                    class="action-button delete-action"
                    @click="handleDeleteProject(item)">
                    <Icon icon="icon-qingchu" class="text-3.5 cursor-pointer text-theme-text-hover" />
                    {{ t('actions.delete') }}
                  </Button>
                </div>

                <div class="dropdown-row">
                  <Dropdown :menuItems="moreButton" @click="handleEditProject(item, $event.key)">
                    <Button
                      size="small"
                      type="text"
                      class="action-button more-action">
                      <Icon icon="icon-gengduo" class="more-options-icon" />
                      {{ t('actions.more') }}
                    </Button>
                  </Dropdown>
                </div>
              </div>
            </div>
            <NoData
              v-if="!data.length"
              size="small"
              class="my-10" />
          </div>

          <Pagination
            v-bind="pagination"
            class="my-3 mt-6"
            @change="handlePageChange" />
        </Spin>
      </div>

      <Tabs size="small" class="w-right" :style="{ height: timelineHeight }">
        <TabPane key="my" :tab="t('project.myActivity')">
          <ActivityTimeline
            :types="activityType"
            :userId="String(props.userInfo?.id || '')"
            :showUserName="false" />
        </TabPane>

        <TabPane key="all" :tab="t('project.allActivity')">
          <ActivityTimeline :types="activityType" />
        </TabPane>
      </Tabs>
    </div>
  </div>
</template>
<style scoped>
.w-right {
  width: 400px;
}

@media screen and (min-width: 1280px) and (max-width: 1480px) {
  .w-right {
    width: 330px;
  }
}

@media screen and (min-width: 1481px) and (max-width: 1800px) {
  .w-right {
    width: 350px;
  }
}

/* 添加活动线组件的样式 */
.ant-tabs {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.ant-tabs>:deep(.ant-tabs-content-holder) {
  flex: 1;
  overflow: hidden;
}

.ant-tabs>:deep(.ant-tabs-tabpane) {
  height: 100%;
  overflow: auto;
}

.project-list-container {
  border-top: 1px solid var(--theme-text-box);
  flex: 1;
  margin-bottom: 15px;
}

.project-item {
  display: flex;
  align-items: flex-start;
  padding: 1rem 1.25rem 0.2rem 1.25rem;
  border-bottom: 1px solid #f1f5f9;
  border-top: 1px solid #f1f5f9;
  transition: all 0.3s ease;
  background: white;
}

.project-item:first-child {
  border-top: 1px solid #f1f5f9;
}

.project-item:hover {
  background: #f8fafc;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.project-avatar-section {
  flex-shrink: 0;
  margin-right: 1rem;
}

.project-avatar {
  width: 62px !important;
  height: 62px !important;
  border-radius: 50% !important;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  border: 1px solid #f1f5f9;
  object-fit: cover;
  flex-shrink: 0;
  overflow: hidden;
}

.project-main-content {
  flex: 1;
  min-width: 0;
  margin-right: 1.5rem;
}

.project-header-row {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 0.5rem;
}

.project-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
  color: #1890ff;
  font-weight: 500;
  transition: color 0.2s ease;
}

.project-name:hover {
  color: #096dd9;
}

.project-type-tag {
  background: linear-gradient(135deg, #e0f2fe, #f0f9ff);
  color: #0369a1;
  border: 1px solid #bae6fd;
  border-radius: 6px;
  padding: 0.25rem 0.5rem;
  font-size: 0.75rem;
  font-weight: 500;
}

.project-id-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #64748b;
  font-size: 0.875rem;
}

.project-description-row {
  margin-bottom: 0.5rem;
}

.project-description-text {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #475569;
  margin: 0;
  line-height: 1.4;
  max-height: 2.8em;
}

.project-no-description-text {
  color: #94a3b8;
  font-style: italic;
  margin: 0;
}

.project-details-section {
  flex: 1;
  min-width: 0;
  margin-right: 1.5rem;
}

.project-members-info {
  display: flex;
  flex-direction: column;
  gap: 0.5875rem;
  margin-bottom: 0.5875rem;
}

.owner-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.members-info {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
}

.info-label {
  color: #64748b;
  font-weight: 500;
  white-space: nowrap;
}

.info-value {
  color: #334155;
  font-weight: 500;
}

.members-avatars-container {
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem;
  align-items: center;
}

.member-avatar {
  width: 1.25rem;
  height: 1.25rem;
  border-radius: 50%;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.group-tag {
  height: 1.25rem;
  line-height: 1.25rem;
  background: linear-gradient(135deg, #dcfce7, #f0fdf4);
  color: #15803d;
  border: 1px solid #bbf7d0;
  border-radius: 4px;
  font-size: 0.75rem;
  padding: 0 0.375rem;
}

.dept-tag {
  height: 1.25rem;
  line-height: 1.25rem;
  background: linear-gradient(135deg, #f3e8ff, #faf5ff);
  color: #7c3aed;
  border: 1px solid #ddd6fe;
  border-radius: 4px;
  font-size: 0.75rem;
  padding: 0 0.375rem;
}

.more-members-indicator {
  color: #94a3b8;
  cursor: pointer;
  font-weight: 500;
  transition: color 0.2s ease;
}

.more-members-indicator:hover {
  color: #64748b;
}

.project-time-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.project-actions-section {
  display: flex;
  flex-direction: column;
  gap: 0.0625rem;
  flex-shrink: 0;
}

.edit-action-row,
.delete-action-row,
.dropdown-row {
  display: flex;
  align-items: center;
}

.action-button {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  transition: all 0.2s ease;
  border: none;
  background: transparent;
}

.edit-action:hover {
  background: #eff6ff;
  color: #2563eb;
}

.delete-action:hover {
  background: #fef2f2;
  color: #dc2626;
}

.more-action:hover {
  background: #f1f5f9;
  color: #334155;
}

.more-options-icon {
  color: #64748b;
  transition: color 0.2s ease;
}
</style>
