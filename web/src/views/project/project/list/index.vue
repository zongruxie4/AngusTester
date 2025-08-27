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

const Summary = defineAsyncComponent(() => import('@/views/project/project/list/introduce/index.vue'));
const AddModal = defineAsyncComponent(() => import('@/views/project/add/index.vue'));
const Process = defineAsyncComponent(() => import('@/views/project/project/list/process/index.vue'));
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
    <Summary class="mb-7" />
    <Process class="mb-7 mt-1" />
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
                :maxlength="200"
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

          <div class="border-t flex-1 border-theme-text-box">
            <div
              v-for="item in data"
              :key="item.id"
              class="border-b border-theme-text-box px-3.5 py-3 rounded flex text-3 hover:bg-gray-1">
              <Image
                :src="item.avatar"
                :defaultImg="DefaultProjectImage"
                class="w-12 h-12 rounded-full" />
              <div class="flex-1 space-y-2.5 min-w-0 px-2">
                <div class="flex space-x-2 items-center">
                  <div
                    class="flex-1/2 truncate cursor-pointer "
                    style="color:#1890ff;"
                    :title="item.name"
                    @click="handleDetail(item)">
                    {{ item.name }}
                  </div>
                  <Tag>{{ item.type?.message || '敏捷项目管理' }}</Tag>
                  <div class="flex-1/2 truncate" :title="item.id">ID: <span>{{ item.id }}</span></div>
                </div>
                <p
                  v-if="item.description"
                  class="truncate">
                  <RichText :value="item.description" />
                </p>
                <p v-else class="text-gray-3">{{ t('project.noDescription') }}</p>
              </div>
              <div class="flex-1 space-y-2.5">
                <div class="flex">
                  <span>{{ t('project.owner') }}:<span>{{ item.ownerName }}</span></span>
                  <div class="ml-6">{{ t('project.members') }}：</div>
                  <div class="inline-flex space-x-1 flex-1 flex-wrap">
                    <Tooltip
                      v-for="(avatars, idx) in item.showMembers.USER || []"
                      :key="idx"
                      :title="avatars.name">
                      <template #title>
                        <span>{{ avatars.name }}</span>
                      </template>
                      <div class="w-5 h-5 rounded-full overflow-hidden">
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
                      <Tag class="h-5 leading-5">{{ avatars.name }}</Tag>
                    </Tooltip>
                    <Tooltip
                      v-for="(avatars, idx) in item.showMembers.DEPT || []"
                      :key="idx"
                      :title="t('project.department')">
                      <template #title>
                        <span>{{ t('project.department') }}</span>
                      </template>
                      <div>
                        <Tag class="h-5 leading-5">{{ avatars.name }}</Tag>
                      </div>
                    </Tooltip>

                    <Popover v-if="item.membersNum > 10">
                      <template #title>{{ t('project.allMembers') }}</template>
                      <template #content>
                        <div class="space-y-2 max-w-100">
                          <div v-if="item.members?.USER?.length" class="flex">
                            <span class="w-15 text-right">{{ t('project.user') }}： </span>
                            <div class="flex flex-1 flex-wrap">
                              <div
                                v-for="(avatars, idx) in item.members.USER || []"
                                :key="idx"
                                class="mb-1 inline-flex space-x-1 pr-2">
                                <Image
                                  class="w-5 h-5 rounded-full "
                                  type="avatar"
                                  :src="avatars.avatar" />
                                <span>{{ avatars.name }}</span>
                              </div>
                            </div>
                          </div>

                          <div v-if="item.members?.GROUP?.length" class="flex">
                            <span class="w-15 text-right">{{ t('project.group') }}： </span>
                            <div class="flex flex-1 flex-wrap">
                              <div
                                v-for="(avatars, idx) in item.members?.GROUP || []"
                                :key="idx"
                                class="mb-1 pr-1">
                                <Tag class="h-5 leading-5">{{ avatars.name }}</Tag>
                              </div>
                            </div>
                          </div>

                          <div v-if="item.members?.DEPT?.length" class="flex">
                            <span class="w-15 text-right">{{ t('project.department') }}： </span>
                            <div class="flex flex-1 flex-wrap">
                              <div
                                v-for="(avatars, idx) in item.members?.DEPT || []"
                                :key="idx"
                                class="mb-1 pr-1">
                                <Tag class="h-5 leading-5">{{ avatars.name }}</Tag>
                              </div>
                            </div>
                          </div>
                        </div>
                      </template>
                      <div>...</div>
                    </Popover>
                  </div>
                </div>
                <div class="flex justify-between items-center">
                  <span>{{ t('project.modifyTime') }}：{{ item.lastModifiedDate }}</span>
                </div>
              </div>
              <div class="flex items-center flex-wrap">
                <Button
                  :title="t('actions.edit')"
                  size="small"
                  type="text"
                  :disabled="!isAdmin && props.userInfo?.id !== item.ownerId && props.userInfo?.id !== item.createdBy"
                  class="space-x-1 flex items-center py-0 px-1"
                  @click="editProject(item)">
                  <Icon icon="icon-bianji" class="text-3.5 cursor-pointer text-theme-text-hover" />
                  {{ t('actions.edit') }}
                </Button>

                <Button
                  :title="t('actions.delete')"
                  size="small"
                  type="text"
                  :disabled="!isAdmin && props.userInfo?.id !== item.ownerId && props.userInfo?.id !== item.createdBy"
                  class="space-x-1 flex items-center py-0 px-1"
                  @click="delProject(item)">
                  <Icon icon="icon-qingchu" class="text-3.5 cursor-pointer text-theme-text-hover" />
                  {{ t('actions.delete') }}
                </Button>

                <Dropdown :menuItems="moreButton" @click="editProject(item, $event.key)">
                  <Icon icon="icon-gengduo" class="ml-1" />
                </Dropdown>
              </div>
            </div>
            <NoData
              v-if="!data.length"
              size="small"
              class="my-10" />
          </div>

          <Pagination
            v-bind="pagination"
            class="my-3"
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
</style>
