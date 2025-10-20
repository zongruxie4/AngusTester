<script setup lang="ts">
import { defineAsyncComponent, inject, nextTick, onMounted, ref, computed, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Progress } from 'ant-design-vue';
import {
  Arrow, AsyncComponent, Colon, DropdownSort, Icon, IconTask,
  Image, Input, Popover, Spin, Tooltip
} from '@xcan-angus/vue-ui';
import { Priority } from '@xcan-angus/infra';
import Draggable from 'vuedraggable';
import { TaskType, TaskSprintPermission } from '@/enums/enums';
import { BasicProps } from '@/types/types';
import { IssueMenuKey } from '@/views/issue/menu';
import { TaskDetail } from '../types';

import TaskPriority from '@/components/task/TaskPriority/index.vue';
import SelectEnum from '@/components/enum/SelectEnum.vue';
import { SprintInfo } from '@/views/issue/sprint/types';

// Import composables
import { useBacklogState } from './composables/useBacklogState';
import { useTaskActions } from './composables/useActions';
import { useUIOptions } from './composables/useUIOptions';
import { useTaskData } from './composables/useTaskData';
import { useSprintData } from './composables/useSprintData';

const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined
});

const { t } = useI18n();
const aiEnabled = inject('aiEnabled', ref(false));
const deleteTabPane = inject<(value: string[]) => void>('deleteTabPane');

const Introduce = defineAsyncComponent(() => import('@/views/issue/backlog/Introduce.vue'));
const SearchPanel = defineAsyncComponent(() => import('@/views/issue/backlog/SearchPanel.vue'));
const EditTaskModal = defineAsyncComponent(() => import('@/views/issue/backlog/Edit.vue'));
const SplitIssue = defineAsyncComponent(() => import('@/views/issue/backlog/SplitIssue.vue'));
const AiGenerateIssue = defineAsyncComponent(() => import('@/views/issue/backlog/AiGenerateIssue.vue'));

const ApiInfo = defineAsyncComponent(() => import('@/views/issue/backlog/info/Apis.vue'));
const BasicInfo = defineAsyncComponent(() => import('@/views/issue/backlog/info/Basic.vue'));
const ScenarioInfo = defineAsyncComponent(() => import('@/views/issue/backlog/info/Scenario.vue'));
const PersonnelInfo = defineAsyncComponent(() => import('@/views/issue/backlog/info/Personnel.vue'));
const DateInfo = defineAsyncComponent(() => import('@/views/issue/backlog/info/Date.vue'));
const AssocIssues = defineAsyncComponent(() => import('@/views/issue/backlog/info/AssocIssues.vue'));
const AssocCases = defineAsyncComponent(() => import('@/views/issue/backlog/info/AssocCases.vue'));
const Attachments = defineAsyncComponent(() => import('@/views/issue/backlog/info/Attachment.vue'));
const Remarks = defineAsyncComponent(() => import('@/views/issue/backlog/info/Remark.vue'));
const Comments = defineAsyncComponent(() => import('@/views/issue/backlog/info/Comment.vue'));
const Activities = defineAsyncComponent(() => import('@/views/issue/backlog/info/Activity.vue'));

// Initialize composables
const {
  backlogData,
  sprintData,
  loading,
  modal,
  selected,
  newTask,
  search,
  taskNameEditing,
  sprintExpansion,
  currentInfo,
  ui,
  sprintContainerRef,
  newTaskNameInputRef,
  updateCurrentTaskInfo,
  setLoadingState,
  selectTask
} = useBacklogState();

// Initialize task data composable
const {
  getTaskParams,
  loadBacklogList,
  handleBacklogTaskSort,
  refreshBacklogData,
  handleSearchChange,
  clearAllFilters,
  toggleAssignedToMeFilter,
  toggleCreatedByMeFilter,
  toggleDateFilter,
  userId,
  selectNone
} = useTaskData(props, backlogData, loading, search);

// Initialize sprint data composable
const {
  loadSprintList,
  handleSprintMemberHover,
  handleSprintTaskSort,
  refreshAllTaskData,
  totalTaskNum
} = useSprintData(props, sprintData, loading, getTaskParams);

// Initialize UI options composable
const {
  sortOption,
  backlogSortOption,
  drawerActiveKeyChange,
  handlePopoverVisibilityChange,
  toggleSprintExpansion,
  handleSprintExpansionChange,
  closeTaskDrawer,
  taskType,
  taskId,
  hasPermission,
  drawerActionItems
} = useUIOptions(ui, sprintExpansion, currentInfo, sprintData);

// Initialize task actions composable
const {
  createNewTask,
  loadTaskInfoById,
  assignTaskToSprint,
  moveTaskBetweenSprints,
  moveTaskToBacklog,
  handleDragAddToSprint,
  handleDragAddToBacklog,
  confirmDeleteTask,
  handleTaskNameDoubleClick,
  handleTaskNameEditEnter,
  cancelTaskNameEdit,
  handleTaskNameClick,
  handleNewTaskNameEnter,
  showAddTaskForm,
  cancelAddTask,
  openCreateTaskModal,
  openTaskEditModal,
  showSplitTaskModal,
  showAiGenerateTaskModal,
  handleTaskEditSuccess,
  handleSplitTaskSuccess,
  handleSplitTaskCancel,
  handleAiGenerateTaskSuccess,
  handleAiGenerateTaskCancel
} = useTaskActions(
  props,
  backlogData,
  sprintData,
  currentInfo,
  loading,
  modal,
  selected,
  newTask,
  taskNameEditing,
  ui,
  deleteTabPane
);

// Enhanced showAddTaskForm with focus handling
const showAddTaskFormEnhanced = () => {
  showAddTaskForm();
  nextTick(() => {
    setTimeout(() => {
      newTaskNameInputRef.value?.focus();
    }, 100);
  });
};

// Enhanced selectTask with proper async handling
const selectTaskEnhanced = async (id: string, data?: SprintInfo) => {
  await selectTask(id, data, loadTaskInfoById);
};

// Computed properties for stable data
const stableSprintList = computed(() => sprintData.sprintList || []);
const stableBacklogList = computed(() => backlogData.backlogList || []);

// Check if data is ready for rendering
const isDataReady = computed(() => {
  if (loading.isLoading) return false;
  if (sprintData.sprintList.length === 0) return false;
  if (Object.keys(sprintData.sprintTasksMap).length === 0) return false;

  // Check that all sprints have valid task arrays
  for (const sprint of sprintData.sprintList) {
    if (!sprintData.sprintTasksMap[sprint.id] || !Array.isArray(sprintData.sprintTasksMap[sprint.id])) {
      return false;
    }
  }
  return true;
});

// Check if sprint data is stable for a specific sprint
const isSprintDataStable = (sprintId: string) => {
  return sprintData.sprintTasksMap[sprintId] &&
         Array.isArray(sprintData.sprintTasksMap[sprintId]) &&
         sprintData.sprintTasksMap[sprintId].length >= 0;
};

onMounted(() => {
  watch(() => props.projectId, async (newValue) => {
    if (!newValue) {
      return;
    }

    await loadSprintList();
    await loadBacklogList(1);
  }, { immediate: true });
});
</script>

<template>
  <div class="flex flex-col h-full py-5 leading-5 text-3">
    <Introduce class="mb-7 mx-5" />
    <Spin :spinning="loading.isLoading" class="flex-1 px-5 overflow-hidden">
      <SearchPanel
        v-model:search="search"
        :userId="userId"
        :selectNone="selectNone"
        @search-change="(value) => {handleSearchChange(value), refreshAllTaskData()}"
        @clear-all-filters="(event) => {clearAllFilters(event), refreshAllTaskData()}"
        @toggle-created-by-me-filter="(event)=> {toggleCreatedByMeFilter(event),refreshAllTaskData()}"
        @toggle-assigned-to-me-filter="(event) => {toggleAssignedToMeFilter(event), refreshAllTaskData()}"
        @toggle-date-filter="(event) => {toggleDateFilter(event), refreshAllTaskData()}" />

      <div class="h-0 border-t border-solid border-theme-text-box mt-2.5"></div>

      <div class="flex items-start overflow-hidden" style="height: calc(100% - 39px);">
        <div ref="sprintContainerRef" class="flex-1 h-full pr-2.5 pt-2.5 overflow-auto scroll-smooth">
          <div class="border border-solid border-theme-text-box rounded">
            <div class="header-container border-solid border-b border-theme-text-box">
              <div class="flex items-center px-2.5 py-4 flex-nowrap space-x-5">
                <div
                  class="flex-1 flex items-center truncate cursor-pointer"
                  @click="handleSprintExpansionChange(!sprintExpansion.expandedSprintIds.has('sprintBacklog'), 'sprintBacklog')">
                  <Arrow
                    :open="sprintExpansion.expandedSprintIds.has('sprintBacklog')"
                    type="dashed"
                    class="mr-1.5"
                    style="font-size: 12px;"
                    @change="handleSprintExpansionChange($event, 'sprintBacklog')" />

                  <div class="font-semibold text-theme-title w-70 mr-5">
                    {{ t('backlog.sprintBacklogTitle') }}
                  </div>
                  <div class="w-25 font-semibold">
                    {{ sprintData.sprintList.length || 0 }} {{ t('backlog.sprintCount') }}
                  </div>
                  <div class="w-25 flex-shrink-0 flex items-center space-x-1 font-semibold">
                    <span>{{ totalTaskNum || 0 }}</span>
                    <span>{{ t('backlog.issueCount') }}</span>
                  </div>
                </div>
              </div>
            </div>

            <template v-for="item in stableSprintList" :key="`sprint-${item.id}`">
              <div
                v-if="isDataReady && isSprintDataStable(item.id)"
                v-show="sprintExpansion.expandedSprintIds.has('sprintBacklog')"
                :class="{ 'draggable-container-open': sprintExpansion.expandedSprintIds.has(item.id) }"
                class="draggable-container">
                <Draggable
                  :id="item.id"
                  :list="sprintData.sprintTasksMap[item.id]"
                  :animation="300"
                  ghostClass="ghost"
                  group="tasks"
                  itemKey="id"
                  tag="div"
                  @add="handleDragAddToSprint($event, item.id)">
                  <template #header>
                    <div class="header-container border-solid border-b border-theme-text-box">
                      <div class="flex items-center px-2.5 py-4 flex-nowrap space-x-5">
                        <div class="flex-1 flex items-center truncate pl-5">
                          <Arrow
                            :open="sprintExpansion.expandedSprintIds.has(item.id)"
                            type="dashed"
                            class="mr-1.5"
                            style="font-size: 12px;"
                            @change="handleSprintExpansionChange($event, item.id)" />

                          <div :title="item.name" class="w-65 truncate text-theme-title  mr-5">
                            <span class="cursor-pointer" @click.stop="toggleSprintExpansion(item.id)">{{ item.name }}</span>
                          </div>

                          <div class="w-25 flex-shrink-0 flex items-center space-x-1 text-theme-sub-content">
                            <span>{{ sprintData.sprintTaskCountMap[item.id] || 0 }}</span>
                            <span>{{ t('backlog.issueCount') }}</span>
                          </div>

                          <div class="flex-shrink-0 flex items-center mr-5">
                            <div
                              :class="item.status?.value"
                              class="text-3 leading-3 px-1 py-1 rounded  text-white flex items-center flex-none whitespace-nowrap mr-3.5">
                              <div class="transform-gpu scale-90">{{ item.status?.message }}</div>
                            </div>

                            <Progress :percent="+item.progress?.completedRate" style="width:150px;" />
                          </div>

                          <div class="flex-shrink-0 text-theme-sub-content">
                            <span>{{ item.startDate }}</span>
                            <span class="ml-2 mr-2"> - </span>
                            <span>{{ item.deadlineDate }}</span>
                          </div>
                        </div>

                        <div class="flex items-center flex-shrink-0 flex-nowrap space-x-2.5">
                          <DropdownSort :menuItems="sortOption" @click="handleSprintTaskSort($event, item.id)">
                            <Button
                              size="small"
                              type="text"
                              class="inline-flex space-x-1 items-center"
                              style="height:20px;padding:0;line-height:20px;">
                              <Icon icon="icon-biaotoupaixu" class="text-3.5" />
                              <span>{{ t('actions.sort') }}</span>
                            </Button>
                          </DropdownSort>

                          <Button
                            :disabled="sprintData.sprintPermissionsMap[item.id]?.includes(TaskSprintPermission.MODIFY_SPRINT)"
                            size="small"
                            type="text"
                            style="height:20px;padding:0;line-height:20px;">
                            <RouterLink class="flex items-center space-x-1" :to="`/issue#${IssueMenuKey.SPRINT}?id=${item.id}`">
                              <Icon icon="icon-shuxie" class="text-3.5" />
                              <span>{{ t('actions.edit') }}</span>
                            </RouterLink>
                          </Button>

                          <RouterLink
                            class="flex items-center space-x-1"
                            :to="`/issue#${IssueMenuKey.ISSUE}?sprintId=${item.id}&sprintName=${item.name}`">
                            <Icon icon="icon-renwu2" class="text-3.5" />
                            <span>{{ t('backlog.actions.enterSprint') }}</span>
                          </RouterLink>
                        </div>
                      </div>

                      <div
                        v-if="!!sprintData.sprintMembersMap[item.id]?.length"
                        class="flex items-center pr-7.5 pl-12.5 pb-3 space-x-1.5 transform-gpu -translate-y-1.5">
                        <Tooltip v-for="member in sprintData.sprintMembersMap[item.id]" :key="member.id">
                          <template #title>
                            <div class="leading-5 text-theme-content">
                              <div class="mb-1 text-theme-title">{{ member.fullName }}</div>

                              <div class="flex items-center mb-0.5">
                                <div class="flex items-center w-12.25">
                                  <span>{{ t('backlog.issueCount') }}</span>
                                  <Colon class="w-1" />
                                </div>
                                <span>{{ sprintData.sprintMemberProgressMap[item.id]?.[member.id]?.validTaskNum || 0 }}</span>
                              </div>

                              <div class="flex items-center">
                                <div class="flex items-center w-12.25">
                                  <span>{{ t('common.workload') }}</span>
                                  <Colon class="w-1" />
                                </div>
                                <span>{{ sprintData.sprintMemberProgressMap[item.id]?.[member.id]?.evalWorkload || 0 }}</span>
                              </div>
                            </div>
                          </template>

                          <div @mouseenter="handleSprintMemberHover(item.id)">
                            <Image
                              :key="member.id"
                              :alt="member.fullName"
                              :src="member.avatar"
                              type="avatar"
                              class="cursor-pointer"
                              :style="{ width: '20px', height: '20px', borderRadius: '20px' }" />
                          </div>
                        </Tooltip>
                      </div>
                    </div>

                    <template v-if="sprintData.sprintTaskCountMap[item.id] === 0">
                      <div
                        v-show="sprintExpansion.expandedSprintIds.has(item.id)"
                        class="empty-draggable mt-4.75 mx-5 h-9.5 flex items-center justify-center rounded text-theme-sub-content">
                        {{ t('backlog.noIssueInSprint') }}
                      </div>
                    </template>
                  </template>

                  <template #item="{ element, index }: { element: TaskDetail; index: number; }">
                    <div
                      v-show="sprintExpansion.expandedSprintIds.has(item.id)"
                      :id="element.id"
                      :class="{ 'active-item': taskId === element.id, 'draggable-item-hover': ui.popoverId === element.id }"
                      class="draggable-item flex items-center space-x-5 truncate !ml-10"
                      @click="selectTaskEnhanced(element.id, item)">
                      <IconTask :value="element.taskType?.value" />
                      <TaskPriority :value="element.priority" />
                      <span>{{ element.code }}</span>
                      <span class="flex-1 truncate" :title="element.name">{{ element.name }}</span>
                      <div class="flex items-center space-x-2.5 action-container">
                        <Button
                          :disabled="!hasPermission(element,'edit')"
                          type="text"
                          size="small"
                          class="px-0 h-5 leading-5 space-x-1 flex items-center"
                          @click.stop="openTaskEditModal(element.id, item.id)">
                          <Icon icon="icon-shuxie" class="text-3.5" />
                          <span>{{ t('actions.edit') }}</span>
                        </Button>

                        <Button
                          :disabled="!hasPermission(element,'split')"
                          type="text"
                          size="small"
                          class="px-0 h-5 leading-5 space-x-1 flex items-center"
                          @click.stop="showSplitTaskModal(element)">
                          <Icon icon="icon-guanlianziyuan" class="text-3.5" />
                          <span>{{ t('actions.split') }}</span>
                        </Button>

                        <Popover
                          overlayClassName="px-content-1.5"
                          placement="left"
                          :mouseEnterDelay="0.3"
                          @visibleChange="handlePopoverVisibilityChange($event, element.id)">
                          <template #content>
                            <div class="max-w-100 space-y-1 leading-5 text-3 truncate">
                              <div
                                :title="t('backlog.actions.moveToBacklog')"
                                class="popover-item truncate cursor-pointer px-2"
                                @click="moveTaskToBacklog(item.id, element, index)">
                                {{ t('backlog.actions.moveToBacklog') }}
                              </div>

                              <template v-for="_sprint in sprintData.sprintList" :key="_sprint.id">
                                <div
                                  v-if="_sprint.id !== item.id"
                                  :title="_sprint.name"
                                  class="popover-item truncate cursor-pointer px-2"
                                  @click="moveTaskBetweenSprints(item.id, _sprint.id, element, index)">
                                  {{ _sprint.name }}
                                </div>
                              </template>
                            </div>
                          </template>

                          <Button
                            :disabled="!hasPermission(element,'move')"
                            type="text"
                            size="small"
                            class="px-0 h-5 leading-5 space-x-1 flex items-center"
                            @click.stop="">
                            <Icon icon="icon-diedai" class="text-3.5" />
                            <span>{{ t('backlog.actions.moveTo') }}</span>
                          </Button>
                        </Popover>

                        <Button
                          :disabled="!hasPermission(element,'delete')"
                          type="text"
                          size="small"
                          class="px-0 h-5 leading-5 space-x-1 flex items-center"
                          @click.stop="confirmDeleteTask(element, index, item.id)">
                          <Icon icon="icon-qingchu" class="text-3.5" />
                          <span>{{ t('actions.delete') }}</span>
                        </Button>
                      </div>
                    </div>
                  </template>
                </Draggable>
              </div>
            </template>

            <div
              v-if="isDataReady && stableBacklogList && Array.isArray(stableBacklogList)"
              class="draggable-container draggable-container-open">
              <Draggable
                id="backlog"
                :key="`backlog-${stableBacklogList.length}`"
                :list="stableBacklogList"
                :animation="300"
                ghostClass="ghost"
                group="tasks"
                itemKey="id"
                tag="div"
                @add="handleDragAddToBacklog">
                <template #header>
                  <div class="flex items-center justify-between px-2.5 header-container border-solid border-b border-theme-text-box">
                    <div
                      class="flex items-center py-4 truncate cursor-pointer flex-1"
                      @click="handleSprintExpansionChange(!sprintExpansion.expandedSprintIds.has('productBacklog'), 'productBacklog')">
                      <Arrow
                        :open="sprintExpansion.expandedSprintIds.has('productBacklog')"
                        type="dashed"
                        class="mr-1.5"
                        style="font-size: 12px;"
                        @change="handleSprintExpansionChange($event, 'productBacklog')" />
                      <div class="truncate text-theme-title font-semibold w-70 mr-5">
                        {{ t('backlog.productBacklogTitle') }}
                      </div>

                      <div class="flex-shrink-0 flex items-center space-x-1 mr-3.5 font-semibold">
                        <span>{{ backlogData.backlogTotalCount || 0 }}</span>
                        <span>{{ t('backlog.issueCount') }}</span>
                      </div>
                    </div>

                    <div class="inline-flex space-x-2 items-center mr-5">
                      <DropdownSort
                        :menuItems="backlogSortOption"
                        @click="handleBacklogTaskSort">
                        <Button
                          size="small"
                          type="text"
                          class="inline-flex space-x-1 items-center"
                          style="height:20px;padding:0;line-height:20px;">
                          <Icon icon="icon-biaotoupaixu" class="text-3.5" />
                          <span>{{ t('actions.sort') }}</span>
                        </Button>
                      </DropdownSort>

                      <Button
                        type="text"
                        size="small"
                        class="inline-flex space-x-1 items-center"
                        style="height:20px;padding:0;line-height:20px;"
                        @click.stop="refreshBacklogData">
                        <Icon class="text-3.5" icon="icon-shuaxin" />
                        <span>{{ t('actions.refresh') }}</span>
                      </Button>
                    </div>
                  </div>

                  <div
                    v-if="backlogData.isBacklogDataLoaded && !backlogData.backlogTotalCount"
                    class="empty-draggable mt-4.75 mx-5 h-9.5 flex items-center justify-center rounded text-theme-sub-content">
                    {{ t('backlog.noBacklog') }}
                  </div>
                </template>

                <template #footer>
                  <div class="px-5 mt-3.5">
                    <div
                      v-if="aiEnabled"
                      v-show="!modal.isAddingNewTask"
                      class="flex items-center space-x-2.5">
                      <Button
                        type="primary"
                        size="small"
                        class="flex items-center space-x-1"
                        @click="showAddTaskFormEnhanced">
                        <Icon icon="icon-jia" class="text-3.5" />
                        <span>{{ t('backlog.actions.add') }}</span>
                      </Button>

                      <Button
                        type="primary"
                        size="small"
                        ghost
                        class="flex items-center space-x-1"
                        @click="showAiGenerateTaskModal">
                        <Icon icon="icon-jia" class="text-3.5" />
                        <span>{{ t('backlog.actions.aiAdd') }}</span>
                      </Button>
                    </div>

                    <Button
                      v-else
                      v-show="!modal.isAddingNewTask"
                      type="default"
                      size="small"
                      style="border-color: #40a9ff;background-color: rgba(244, 2250, 254, 100%);"
                      class="flex items-center space-x-1 border-dashed w-full h-11"
                      @click="showAddTaskFormEnhanced">
                      <Icon icon="icon-jia" class="text-3.5" />
                      <span>{{ t('backlog.actions.add') }}</span>
                    </Button>

                    <div v-show="modal.isAddingNewTask" class="flex items-center">
                      <SelectEnum
                        v-model:value="newTask.newTaskType"
                        enumKey="TaskType"
                        :placeholder="t('common.taskType')"
                        :excludes="({value}) => [TaskType.API_TEST, TaskType.SCENARIO_TEST].includes(value as any)"
                        class="w-28 mr-2">
                        <template #option="record">
                          <div class="flex items-center">
                            <IconTask :value="record.value" class="text-4 flex-shrink-0" />
                            <span class="ml-1">{{ record.label }}</span>
                          </div>
                        </template>
                      </SelectEnum>

                      <SelectEnum
                        v-model:value="newTask.newTaskPriority"
                        internal
                        enumKey="Priority"
                        :placeholder="t('common.placeholders.selectPriority')"
                        class="w-28 mr-2">
                        <template #option="record">
                          <TaskPriority :value="{ value: record.value, label: record.label }" />
                        </template>
                      </SelectEnum>

                      <Input
                        ref="newTaskNameInputRef"
                        v-model:value="newTask.newTaskName"
                        :maxlength="200"
                        :placeholder="t('common.placeholders.inputName2')"
                        trim
                        class="w-200 mr-5"
                        @pressEnter="handleNewTaskNameEnter" />

                      <div class="flex items-center space-x-2.5">
                        <Button
                          :disabled="!newTask.newTaskName"
                          type="primary"
                          size="small"
                          @click="createNewTask">
                          {{ t('actions.save') }}
                        </Button>

                        <Button
                          type="default"
                          size="small"
                          @click="cancelAddTask">
                          {{ t('actions.cancel') }}
                        </Button>

                        <Button
                          type="default"
                          size="small"
                          @click="openCreateTaskModal">
                          {{ t('backlog.actions.openAddModal') }}
                        </Button>
                      </div>
                    </div>
                  </div>
                </template>

                <template #item="{ element, index }: { element: TaskDetail; index: number; }">
                  <div
                    v-show="sprintExpansion.expandedSprintIds.has('productBacklog')"
                    :id="element.id"
                    :class="{ 'active-item': taskId === element.id, 'draggable-item-hover': ui.popoverId === element.id }"
                    class="draggable-item flex items-center justify-between space-x-5 truncate"
                    @click="selectTaskEnhanced(element.id)">
                    <div class="flex items-center space-x-5 truncate">
                      <IconTask :value="element.taskType?.value" />
                      <TaskPriority :value="element.priority" />
                      <div>{{ element.code }}</div>
                      <div
                        v-if="taskNameEditing.editingTaskNameIds.has(element.id)"
                        class="flex items-center"
                        @click.stop="">
                        <Input
                          ref="newTaskNameInputRef"
                          v-model:value="taskNameEditing.editingTaskNameMap[element.id]"
                          :maxlength="200"
                          :placeholder="t('common.placeholders.inputName2')"
                          trim
                          class="w-100 mr-5"
                          @pressEnter="handleTaskNameEditEnter(element,index)" />

                        <Button
                          :disabled="!taskNameEditing.editingTaskNameMap[element.id]"
                          type="primary"
                          size="small"
                          class="mr-2.5"
                          @click="handleTaskNameEditEnter(element,index)">
                          {{ t('actions.save') }}
                        </Button>

                        <Button
                          type="default"
                          size="small"
                          class="bg-white"
                          @click="cancelTaskNameEdit(element.id)">
                          {{ t('actions.cancel') }}
                        </Button>
                      </div>

                      <div
                        v-else
                        class="flex-1 truncate cursor-default"
                        :title="element.name"
                        @click.stop="handleTaskNameClick"
                        @dblclick.stop="handleTaskNameDoubleClick(element)">
                        {{ element.name }}
                      </div>
                    </div>

                    <div class="flex items-center space-x-2.5 action-container">
                      <Button
                        type="text"
                        size="small"
                        class="px-0 h-5 leading-5 space-x-1 flex items-center"
                        @click.stop="openTaskEditModal(element.id)">
                        <Icon icon="icon-shuxie" class="text-3.5" />
                        <span>{{ t('actions.edit') }}</span>
                      </Button>

                      <Button
                        type="text"
                        size="small"
                        class="px-0 h-5 leading-5 space-x-1 flex items-center"
                        @click.stop="showSplitTaskModal(element)">
                        <Icon icon="icon-guanlianziyuan" class="text-3.5" />
                        <span>{{ t('actions.split') }}</span>
                      </Button>

                      <Popover
                        overlayClassName="px-content-1.5"
                        placement="left"
                        :mouseEnterDelay="0.3"
                        @visibleChange="handlePopoverVisibilityChange($event, element.id)">
                        <template #content>
                          <div class="max-w-100 space-y-1 leading-5 text-3 truncate">
                            <div
                              v-for="_sprint in sprintData.sprintList"
                              :key="_sprint.id"
                              :title="_sprint.name"
                              class="popover-item truncate cursor-pointer px-2"
                              @click="assignTaskToSprint(_sprint.id, element, index)">
                              {{ _sprint.name }}
                            </div>
                          </div>
                        </template>

                        <Button
                          type="text"
                          size="small"
                          class="px-0 h-5 leading-5 space-x-1 flex items-center"
                          @click.stop="">
                          <Icon icon="icon-diedai" class="text-3.5" />
                          <span>{{ t('actions.assign') }}</span>
                        </Button>
                      </Popover>

                      <Button
                        type="text"
                        size="small"
                        class="px-0 h-5 leading-5 space-x-1 flex items-center"
                        @click.stop="confirmDeleteTask(element, index)">
                        <Icon icon="icon-qingchu" class="text-3.5" />
                        <span>{{ t('actions.delete') }}</span>
                      </Button>
                    </div>
                  </div>
                </template>
              </Draggable>
            </div>
          </div>
        </div>

        <div class="drawer-container flex items-start" :class="{ 'drawer-open': !!taskId }">
          <div class="flex-shrink-0 h-full w-9 py-1.5 space-y-1 overflow-y-auto drawer-action-container">
            <div
              v-for="item in drawerActionItems"
              :key="item.key"
              :class="{ 'drawer-active-item': ui.drawerActiveKey === item.key }"
              class="action-item cursor-pointer w-full h-8 flex items-center justify-center"
              :title="item.title"
              @click="drawerActiveKeyChange(item.key)">
              <Icon :icon="item.icon" class="text-4" />
            </div>
          </div>

          <div class="w-full h-full flex-1 overflow-hidden">
            <div class="flex items-center justify-between mt-4 pl-5 space-x-2.5">
              <div class="flex-1 flex items-center truncate">
                <RouterLink
                  v-if="currentInfo.currentSprintInfo"
                  :to="`/issue#${IssueMenuKey.SPRINT}?id=${currentInfo.currentSprintInfo?.id}`"
                  :title="currentInfo.currentSprintInfo?.name"
                  class="truncate"
                  style="max-width: 50%;">
                  {{ currentInfo.currentSprintInfo?.name }}
                </RouterLink>

                <div v-else class="flex-shrink-0">Backlog</div>
                <div class="mx-1.5">/</div>
                <RouterLink
                  :to="`/issue#${IssueMenuKey.ISSUE}?id=${currentInfo.currentTaskInfo?.id}`"
                  class="truncate flex-1"
                  :title="currentInfo.currentTaskInfo?.name">
                  {{ currentInfo.currentTaskInfo?.name }}
                </RouterLink>
              </div>

              <Button
                type="default"
                size="small"
                class="p-0 h-3.5 leading-3.5 border-none"
                @click="closeTaskDrawer">
                <Icon icon="icon-shanchuguanbi" class="text-3.5 cursor-pointer" />
              </Button>
            </div>

            <div style="height: calc(100% - 36px);" class="pt-3.5 pb-3.5 overflow-hidden">
              <AsyncComponent :visible="!!taskId">
                <ApiInfo
                  v-if="taskType === TaskType.API_TEST"
                  v-show="ui.drawerActiveKey === 'basic'"
                  :projectId="props.projectId"
                  :appInfo="props.appInfo"
                  :userInfo="props.userInfo"
                  :dataSource="currentInfo.currentTaskInfo"
                  @refresh="refreshAllTaskData"
                  @change="updateCurrentTaskInfo"
                  @loadingChange="setLoadingState" />

                <ScenarioInfo
                  v-else-if="taskType === TaskType.SCENARIO_TEST"
                  v-show="ui.drawerActiveKey === 'basic'"
                  :projectId="props.projectId"
                  :appInfo="props.appInfo"
                  :userInfo="props.userInfo"
                  :dataSource="currentInfo.currentTaskInfo"
                  @refresh="refreshAllTaskData"
                  @change="updateCurrentTaskInfo"
                  @loadingChange="setLoadingState" />

                <BasicInfo
                  v-else
                  v-show="ui.drawerActiveKey === 'basic'"
                  :projectId="props.projectId"
                  :appInfo="props.appInfo"
                  :userInfo="props.userInfo"
                  :dataSource="currentInfo.currentTaskInfo"
                  @refresh="refreshAllTaskData"
                  @change="updateCurrentTaskInfo"
                  @loadingChange="setLoadingState" />

                <PersonnelInfo
                  v-show="ui.drawerActiveKey === 'person'"
                  :projectId="props.projectId"
                  :appInfo="props.appInfo"
                  :userInfo="props.userInfo"
                  :dataSource="currentInfo.currentTaskInfo"
                  @change="updateCurrentTaskInfo"
                  @loadingChange="setLoadingState" />

                <DateInfo
                  v-show="ui.drawerActiveKey === 'date'"
                  :projectId="props.projectId"
                  :appInfo="props.appInfo"
                  :userInfo="props.userInfo"
                  :dataSource="currentInfo.currentTaskInfo"
                  @change="updateCurrentTaskInfo"
                  @loadingChange="setLoadingState" />

                <AssocIssues
                  v-show="ui.drawerActiveKey === 'tasks'"
                  :projectId="props.projectId"
                  :appInfo="props.appInfo"
                  :userInfo="props.userInfo"
                  :dataSource="currentInfo.currentTaskInfo"
                  @change="updateCurrentTaskInfo"
                  @loadingChange="setLoadingState" />

                <AssocCases
                  v-show="ui.drawerActiveKey === 'cases'"
                  :projectId="props.projectId"
                  :appInfo="props.appInfo"
                  :userInfo="props.userInfo"
                  :dataSource="currentInfo.currentTaskInfo"
                  @change="updateCurrentTaskInfo"
                  @loadingChange="setLoadingState" />

                <Attachments
                  v-show="ui.drawerActiveKey === 'attachments'"
                  :projectId="props.projectId"
                  :appInfo="props.appInfo"
                  :userInfo="props.userInfo"
                  :dataSource="currentInfo.currentTaskInfo"
                  @change="updateCurrentTaskInfo"
                  @loadingChange="setLoadingState" />

                <Comments
                  v-show="ui.drawerActiveKey === 'comment'"
                  :projectId="props.projectId"
                  :appInfo="props.appInfo"
                  :userInfo="props.userInfo"
                  :dataSource="currentInfo.currentTaskInfo"
                  @change="updateCurrentTaskInfo"
                  @loadingChange="setLoadingState" />

                <Activities
                  v-show="ui.drawerActiveKey === 'activity'"
                  :projectId="props.projectId"
                  :appInfo="props.appInfo"
                  :userInfo="props.userInfo"
                  :dataSource="currentInfo.currentTaskInfo"
                  @change="updateCurrentTaskInfo"
                  @loadingChange="setLoadingState" />

                <Remarks
                  v-show="ui.drawerActiveKey === 'remarks'"
                  :id="currentInfo.currentTaskInfo?.id || '-1'"
                  :projectId="props.projectId || ''"
                  :appInfo="props.appInfo || { id: '' }"
                  :userInfo="props.userInfo || { id: '', fullName: '' }" />
              </AsyncComponent>
            </div>
          </div>
        </div>
      </div>

      <AsyncComponent :visible="modal.isEditTaskModalVisible">
        <EditTaskModal
          v-model:visible="modal.isEditTaskModalVisible"
          :taskId="selected.selectedTaskId"
          :sprintId="selected.selectedSprintId"
          :projectId="props.projectId || ''"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :deadlineDate="''"
          :priority="Priority.MEDIUM"
          @ok="handleTaskEditSuccess" />
      </AsyncComponent>
    </Spin>

    <AsyncComponent :visible="modal.isSplitTaskModalVisible">
      <SplitIssue
        v-model:visible="modal.isSplitTaskModalVisible"
        :projectId="props.projectId || ''"
        :dataSource="selected.selectedTaskInfo"
        :userInfo="props.userInfo || { id: '', fullName: '' }"
        :appInfo="props.appInfo || { id: '' }"
        @ok="handleSplitTaskSuccess"
        @cancel="handleSplitTaskCancel" />
    </AsyncComponent>

    <AsyncComponent :visible="modal.isAiGenerateTaskModalVisible">
      <AiGenerateIssue
        v-model:visible="modal.isAiGenerateTaskModalVisible"
        :projectId="props.projectId || ''"
        @ok="handleAiGenerateTaskSuccess"
        @cancel="handleAiGenerateTaskCancel" />
    </AsyncComponent>
  </div>
</template>

<style>
.ant-popover.px-content-1\.5 .ant-popover-inner-content {
  max-height: 500px;
  padding-right: 6px;
  padding-left: 6px;
  overflow: auto;
}
</style>

<style scoped>
.PENDING {
  background-color: rgba(45, 142, 255, 100%);
}

.IN_PROGRESS {
  background-color: rgba(103, 215, 255, 100%);
}

.COMPLETED {
  background-color: rgba(82, 196, 26, 100%);
}

.BLOCKED {
  background-color: rgba(255, 165, 43, 100%);
}

:deep(.ant-progress-outer) {
  width: 100px;
}

.active-key {
  background-color: #4ea0fd;
  color: #fff;
}

.router-link {
  color: #1890ff;
  cursor: pointer;
}

.drawer-action-container {
  background-color: rgba(247, 248, 251, 100%);
  color: var(--content-text-sub-content);
}

.draggable-container {
  position: relative;
}

.draggable-container.draggable-container-open {
  padding-bottom: 20px;
}

.draggable-container::after {
  content: "";
  display: block;
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 1px;
  background-color: var(--border-text-box);
}

.draggable-container:last-child::after {
  display: none;
}

.empty-draggable {
  border: 2px dashed var(--border-text-box);
}

.draggable-item {
  position: relative;
  margin-right: 20px;
  margin-left: 20px;
  padding: 8px;
  border-radius: 4px;
  cursor: move;
}

.draggable-item::after {
  content: "";
  display: block;
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 1px;
  background-color: var(--border-text-box);
}

.draggable-item:last-child::after {
  display: none;
}

.draggable-item:hover,
.draggable-item.active-item {
  background-color: rgba(239, 240, 243, 100%);
}

.draggable-item:last-child {
  border-bottom: none;
}

.action-container {
  visibility: hidden;
}

.draggable-item-hover .action-container,
.draggable-item:hover .action-container {
  visibility: visible;
}

.popover-item {
  padding-top: 4px;
  padding-bottom: 4px;
  border-radius: 4px;
}

.popover-item:hover {
  background-color: var(--border-text-box);
}

.ghost {
  opacity: 0.5;
}

.drawer-container {
  width: 0;
  overflow: hidden;
  transition: all 150ms linear 0ms;
  border-left: 1px solid transparent;
  opacity: 0;
}

.drawer-container.drawer-open {
  width: 400px;
  height: 100%;
  border-left: 1px solid var(--border-text-box);
  opacity: 1;
}

.action-item:hover,
.action-item.drawer-active-item {
  background-color: rgba(239, 240, 243, 100%);
}

:deep(.ant-input-affix-wrapper-sm) {
  background-color: #fff;
}
</style>
