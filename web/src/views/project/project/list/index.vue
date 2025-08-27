<script lang="ts" setup>
import { defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Pagination, TabPane, Tabs, Tag, Tooltip, Popover } from 'ant-design-vue';
import {
  ActivityTimeline,
  AsyncComponent,
  DropdownSort,
  Hints,
  Icon,
  IconRefresh,
  Image,
  Input,
  modal,
  NoData,
  notification,
  Spin,
  Dropdown
} from '@xcan-angus/vue-ui';
import { project } from 'src/api/tester';
import { debounce } from 'throttle-debounce';
import { duration } from '@xcan-angus/infra';
import { getCurrentPage } from '@/utils/utils';
import DefaultProjectImage from '@/views/project/project/images/default.png';

const { t } = useI18n();

type OrderByKey = 'createdDate' | 'createdByName';
type OrderSortKey = 'ASC' | 'DESC';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined
});

const emits = defineEmits<{(e: 'delOk', value: string[]);}>();

type Project = {
  name: string;
  description?: string;
  avatar?: string;
  ownerName: string;
  lastModifiedDate: string;
  id: string;
  ownerId: string;
  members: {
    USER?: {name: string; avatar?: string}[];
    DEPT?: {name: string; avatar?: string}[];
    GROUP?: {name: string; avatar?: string}[];
  };
}

const Introduce = defineAsyncComponent(() => import('@/views/project/project/list/introduce/index.vue'));
const AddModal = defineAsyncComponent(() => import('@/views/project/add/index.vue'));
const RichText = defineAsyncComponent(() => import('@/components/richEditor/textContent/index.vue'));

const isAdmin = inject('isAdmin', ref(false));
const addTabPane = inject('addTabPane');
const changeProjectInfo = inject('changeProjectInfo', (projectId: string, force: boolean) => ({ projectId, force }));
const getNewCurrentProject = inject('getNewCurrentProject', () => (undefined));

const loading = ref();
const keyword = ref();
const data = ref<Project[]>([]);

const orderBy = ref<OrderByKey>();
const orderSort = ref<OrderSortKey>();
const pagination = ref({
  pageSize: 5,
  current: 1,
  total: 0,
  pageSizeOptions: [5],
  hideOnSinglePage: true
});

const modalVisible = ref(false);
const editProjectData = ref();
const addProject = () => {
  // modalVisible.value = true;
  // editProjectData.value = undefined;
  addTabPane({
    type: 'project',
    _id: 'addProject',
    name: t('project.addProject')
  });
};

const toSort = (value: {
  orderBy: OrderByKey;
  orderSort: OrderSortKey;
}): void => {
  orderBy.value = value.orderBy;
  orderSort.value = value.orderSort;
  pagination.value.current = 1;
  loadData();
};

const closeModal = () => {
  modalVisible.value = false;
};

const onCreateProject = () => {
  changeProjectInfo(editProjectData.value.id, true);
  closeModal();
  loadData();
};

const getParams = () => {
  const { pageSize, current } = pagination.value;
  const params:{
    pageNo:number;
    pageSize:number;
    filters?:{
      key:'name';
      op:'MATCH_END',
      value:string;
    }[];
    orderBy?: OrderByKey;
    orderSort?:OrderSortKey;
  } = {
    pageNo: current,
    pageSize
  };

  if (keyword.value) {
    params.filters = [{ value: keyword.value, op: 'MATCH_END', key: 'name' }];
  }

  if (orderSort.value) {
    params.orderBy = orderBy.value;
    params.orderSort = orderSort.value;
  }

  return params;
};

// 拉取项目列表
const loadData = async () => {
  const params = getParams();
  loading.value = true;
  const [error, resp] = await project.getProjectList(params);
  loading.value = false;
  if (error) {
    return;
  }
  data.value = resp.data?.list || [];
  pagination.value.total = +resp.data?.total || 0;
  data.value.forEach(item => {
    item.membersNum = (item.members?.USER || []).length + (item.members?.GROUP || []).length + (item.members?.DEPT || []).length;
    item.showMembers = {
      USER: item.members?.USER || [],
      GROUP: [],
      DEPT: []
    };
    if (item.showMembers.USER.length > 10) {
      item.showMembers.USER = item.showMembers.USER.slice(0, 10);
      return;
    }
    item.showMembers.GROUP = item.members?.GROUP || [];

    if (item.showMembers.USER.length + item.showMembers.GROUP.length > 10) {
      item.showMembers.GROUP = item.showMembers.GROUP.slice(0, 10 - item.showMembers.USER.length);
      return;
    }

    item.showMembers.DEPT = item.members?.DEPT || [];

    if (item.showMembers.USER.length + item.showMembers.GROUP.length + item.showMembers.DEPT.length > 10) {
      item.showMembers.DEPT = item.showMembers.DEPT.slice(0, 10 - (item.showMembers.USER.length + item.showMembers.GROUP.length));
    }
  });
};

const changePage = (current) => {
  pagination.value.current = current;
  loadData();
};

const editProject = (record: Project, key?: string) => {
  // modalVisible.value = true;
  // editProjectData.value = record;
  addTabPane({
    type: 'project',
    _id: `${record.id}_project`,
    name: record.name,
    id: record.id,
    data: {
      tab: key || ''
    }
  });
};

const delProject = (data: Project) => {
  modal.confirm({
    content: t('project.confirmDelete', { name: data.name }),
    async onOk () {
      const [error] = await project.deleteProject(data.id);
      if (error) {
        return;
      }

      notification.success(t('project.deleteSuccess'));
      pagination.value.current = getCurrentPage(pagination.value.current, pagination.value.pageSize, pagination.value.total);
      loadData();
      emits('delOk', [data.id, `${data.id}_detail`, `${data.id}_project`]);
      if (data.id === props.projectId) {
        changeProjectInfo('', false);
      } else {
        getNewCurrentProject();
      }
    }
  });
};

const handleDetail = async (data: Project) => {
  addTabPane({
    _id: `${data.id}_detail`,
    name: data.name,
    type: 'projectDetail',
    id: data.id
  });
};

const moreButton = [
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
];

const onKeywordChange = debounce(duration.search, () => {
  pagination.value.current = 1;
  loadData();
});

onMounted(() => {
  loadData();
  watch(() => props.projectId, (newValue, oldValue) => {
    if (newValue && !oldValue) {
      loadData();
    }
  });
});

const sortMenuItems: {
  name: string;
  key: OrderByKey;
  orderSort: OrderSortKey;
}[] = [
  {
    name: t('project.sortMenu.createdDate'),
    key: 'createdDate',
    orderSort: 'DESC'
  },
  {
    name: t('project.sortMenu.createdByName'),
    key: 'createdByName',
    orderSort: 'ASC'
  }
];

const activityType = [];

defineExpose({
  refresh: loadData
});

</script>
<template>
  <div class="p-5 overflow-y-auto text-3 flex flex-col h-full">
    <Introduce class="mb-7" />
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
                @change="onKeywordChange" />
              <Hints :text="t('project.adminHint')" class="ml-2" />
            </div>

            <div class="flex items-center space-x-3">
              <Button
                type="primary"
                size="small"
                @click="addProject">
                <Icon icon="icon-jia" />
                <span class="ml-1">{{ t('project.addProject') }}</span>
              </Button>

              <DropdownSort
                v-model:orderBy="orderBy"
                v-model:orderSort="orderSort"
                :menuItems="sortMenuItems"
                @click="toSort">
                <div class="flex items-center cursor-pointer text-theme-content space-x-1 text-theme-text-hover">
                  <Icon icon="icon-shunxu" />
                  <span>{{ t('project.sort') }}</span>
                </div>
              </DropdownSort>

              <IconRefresh
                :loading="loading"
                :disabled="loading"
                @click="loadData">
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
                  style="width: 52px; height: 52px; object-fit: cover; border-radius: 50%;" />
              </div>

              <div class="project-main-content">
                <div class="project-header-row">
                  <div
                    class="project-name"
                    :title="item.name"
                    @click="handleDetail(item)">
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
                        v-for="(avatars, idx) in item.showMembers.USER || []"
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
                        v-for="(avatars, idx) in item.showMembers.GROUP || []"
                        :key="idx"
                        :title="t('project.group')">
                        <template #title>
                          <span>{{ t('project.group') }}</span>
                        </template>
                        <Tag class="group-tag">{{ avatars.name }}</Tag>
                      </Tooltip>

                      <Tooltip
                        v-for="(avatars, idx) in item.showMembers.DEPT || []"
                        :key="idx"
                        :title="t('project.department')">
                        <template #title>
                          <span>{{ t('project.department') }}</span>
                        </template>
                        <div>
                          <Tag class="dept-tag">{{ avatars.name }}</Tag>
                        </div>
                      </Tooltip>

                      <Popover v-if="item.membersNum > 10">
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
                    :disabled="!isAdmin && props.userInfo?.id !== item.ownerId && props.userInfo?.id !== item.createdBy"
                    class="action-button edit-action"
                    @click="editProject(item)">
                    <Icon icon="icon-bianji" class="text-3.5 cursor-pointer text-theme-text-hover" />
                    {{ t('actions.edit') }}
                  </Button>
                </div>

                <div class="delete-action-row">
                  <Button
                    :title="t('actions.delete')"
                    size="small"
                    type="text"
                    :disabled="!isAdmin && props.userInfo?.id !== item.ownerId && props.userInfo?.id !== item.createdBy"
                    class="action-button delete-action"
                    @click="delProject(item)">
                    <Icon icon="icon-qingchu" class="text-3.5 cursor-pointer text-theme-text-hover" />
                    {{ t('actions.delete') }}
                  </Button>
                </div>

                <div class="dropdown-row">
                  <Dropdown :menuItems="moreButton" @click="editProject(item, $event.key)">
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
            @change="changePage" />
        </Spin>
      </div>

      <Tabs size="small" class="w-right h-115">
        <TabPane key="my" :tab="t('project.myActivity')">
          <ActivityTimeline
            :types="activityType"
            :userId="props.userInfo?.id"
            :showUserName="false" />
        </TabPane>

        <TabPane key="all" :tab="t('project.allActivity')">
          <ActivityTimeline :types="activityType" />
        </TabPane>
      </Tabs>
    </div>
  </div>

  <AsyncComponent :visible="modalVisible">
    <AddModal
      v-model:visible="modalVisible"
      :dataSource="editProjectData"
      @cancel="closeModal"
      @ok="onCreateProject" />
  </AsyncComponent>
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

/* 项目列表容器 */
.project-list-container {
  border-top: 1px solid var(--theme-text-box);
  flex: 1;
  margin-bottom: 20px;
}

/* 项目列表项 */
.project-item {
  display: flex;
  align-items: flex-start;
  padding: 1rem 1.25rem 0.5rem 1.25rem;
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

/* 项目头像区域 */
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

/* 项目主要内容区域 */
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

/* 项目详情区域 */
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

/* 项目操作区域 */
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
