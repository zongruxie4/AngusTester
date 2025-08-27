<script lang="ts" setup>
import { defineAsyncComponent, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Image, Icon } from '@xcan-angus/vue-ui';
import { TabPane, Tabs, Tag } from 'ant-design-vue';
import { project } from 'src/api/tester';
import DefaultProjectPng from '@/views/project/project/images/default.png';
import dayjs from 'dayjs';

const { t } = useI18n();

export type Project = {
  name: string;
  ownerId: string;
  id: string;
  avatar?: string;
  description?: string;
  members?: { [key: string]: any }
}

interface Props {
  closable: boolean;
  projectId: string;
  data?: {
    tab: string
  }

}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  dataSource: undefined,
  closable: true,
  data: () => ({
    tab: ''
  })
});

const Tags = defineAsyncComponent(() => import('@/views/project/project/edit/tag/index.vue'));
const Module = defineAsyncComponent(() => import('@/views/project/project/edit/module/index.vue'));
const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));
const Version = defineAsyncComponent(() => import('@/views/task/version/list/index.vue'));

const projectTypeTipConfig = {
  AGILE: [t('project.projectDetail.projectTypeTip.agile.features'), t('project.projectDetail.projectTypeTip.agile.scenarios')],
  GENERAL: [t('project.projectDetail.projectTypeTip.general.features'), t('project.projectDetail.projectTypeTip.general.scenarios')],
  TESTING: [t('project.projectDetail.projectTypeTip.testing.features'), t('project.projectDetail.projectTypeTip.testing.scenarios')]
};

const projectTypeName = {
  AGILE: t('project.projectDetail.projectTypeName.agile'),
  GENERAL: t('project.projectDetail.projectTypeName.general'),
  TESTING: t('project.projectDetail.projectTypeName.testing')
};
const activeKey = ref('basic');

const formData = ref({
  name: '',
  ownerId: undefined,
  description: '',
  avatar: '',
  type: 'AGILE',
  id: undefined,
  startDate: undefined,
  deadlineDate: undefined,
  ownerName: undefined,
  members: {
    USER: [] as any[],
    DEPT: [] as any[],
    GROUP: [] as any[]
  }
});

const loadformData = async () => {
  const [error, { data }] = await project.getProjectDetail(props.projectId);
  if (error) {
    return;
  }
  formData.value = {
    ...data,
    type: data.type?.value || 'AGILE',
    members: data.members || { USER: [], DEPT: [], GROUP: [] }
  };
};

watch(() => props.projectId, newValue => {
  if (!newValue) {
    return;
  }
  loadformData();
  if (props.data?.tab) {
    activeKey.value = props.data.tab;
  }
}, {
  immediate: true
});

</script>
<template>
  <div class="p-5 h-full overflow-auto">
    <Tabs v-model:activeKey="activeKey" size="small">
      <TabPane key="basic" :tab="t('project.projectDetail.tabs.basicInfo')">
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-28">
          <!-- 左侧项目基本信息 -->
          <div class="lg:col-span-2 space-y-6">
            <!-- 项目头像和名称 -->
            <div class="flex items-center space-x-4">
              <div class="flex-shrink-0">
                <Image
                  v-if="formData.avatar"
                  :src="formData.avatar"
                  class="w-8 h-8 rounded-lg object-cover border border-gray-200"
                  alt="avatar" />
                <img
                  v-else
                  class="w-8 h-8 rounded-lg object-cover border border-gray-200"
                  :src="DefaultProjectPng" />
              </div>
              <div class="flex items-center min-w-0 pt-2">
                <h1 class="text-lg font-bold text-blue-600 truncate">{{ formData.name }}</h1>
              </div>
            </div>

            <!-- 项目信息网格 -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <!-- 时间计划 -->
              <div v-if="formData.startDate" class="space-y-2">
                <div class="flex items-center space-x-2">
                  <div class="w-1 h-4 bg-gradient-to-b from-green-500 to-green-600 rounded-full"></div>
                  <span class="text-xs font-semibold text-gray-600">{{ t('project.projectDetail.form.timePlan') }}</span>
                </div>
                <div class="flex items-center space-x-1 text-green-600 ">
                  <Icon icon="icon-time" class="text-xs" />
                  <span class="text-xs font-medium">
                    {{ dayjs(formData.startDate).format('YYYY-MM-DD') }} - {{ dayjs(formData.deadlineDate).format('YYYY-MM-DD') || '' }}
                  </span>
                </div>
              </div>

              <!-- 项目负责人 -->
              <div class="space-y-2">
                <div class="flex items-center space-x-2">
                  <div class="w-1 h-4 bg-gradient-to-b from-purple-500 to-purple-600 rounded-full"></div>
                  <span class="text-xs font-semibold text-gray-600">{{ t('project.projectDetail.form.owner') }}</span>
                </div>
                <div class="flex items-center space-x-1 text-purple-600 ">
                  <Icon icon="icon-user" class="text-xs" />
                  <span class="text-xs font-medium">{{ formData.ownerName }}</span>
                </div>
              </div>
            </div>

            <!-- 项目成员 -->
            <div class="space-y-4">
              <div class="flex items-center space-x-2">
                <div class="w-1 h-4 bg-gradient-to-b from-indigo-500 to-indigo-600 rounded-full"></div>
                <span class="text-xs font-semibold text-gray-600">{{ t('project.projectDetail.form.projectMembers') }}</span>
              </div>
              <Tabs size="small" class="member-tabs">
                <TabPane key="user" :tab="t('project.projectDetail.form.user')">
                  <div class="flex flex-wrap gap-2">
                    <div
                      v-for="(member, idx) in formData.members?.USER || []"
                      :key="idx"
                      class="flex items-center space-x-2 px-3 py-1.5 bg-gray-50 hover:bg-gray-100 rounded-lg transition-colors">
                      <Image
                        class="w-6 h-6 rounded-full object-cover border border-gray-200 flex-shrink-0"
                        type="avatar"
                        :src="member.avatar" />
                      <span class="text-xs text-gray-700">{{ member.name }}</span>
                    </div>
                  </div>
                </TabPane>
                <TabPane key="group" :tab="t('project.projectDetail.form.group')">
                  <div class="flex flex-wrap gap-2">
                    <Tag
                      v-for="(group, idx) in formData.members?.GROUP || []"
                      :key="idx"
                      class="px-3 py-1 bg-gray-100 hover:bg-gray-200 text-gray-700 border-gray-300 transition-colors text-xs">
                      {{ group.name }}
                    </Tag>
                  </div>
                </TabPane>
                <TabPane key="dept" :tab="t('project.projectDetail.form.department')">
                  <div class="flex flex-wrap gap-2">
                    <Tag
                      v-for="(dept, idx) in formData.members?.DEPT || []"
                      :key="idx"
                      class="px-3 py-1 bg-gray-100 hover:bg-gray-200 text-gray-700 border-gray-300 transition-colors text-xs">
                      {{ dept.name }}
                    </Tag>
                  </div>
                </TabPane>
              </Tabs>
            </div>

            <!-- 项目描述 -->
            <div class="space-y-3">
              <div class="flex items-center space-x-2">
                <div class="w-1 h-4 bg-gradient-to-b from-gray-500 to-gray-600 rounded-full"></div>
                <span class="text-xs font-semibold text-gray-600">{{ t('project.projectDetail.form.projectDescription') }}</span>
              </div>
              <div class="text-gray-700 text-xs ">
                <RichEditor
                  :value="formData.description"
                  :emptyText="t('project.projectDetail.form.noDescription')"
                  mode="view" />
              </div>
            </div>
          </div>

          <!-- 右侧项目类型信息 -->
          <div class="space-y-6">
            <div class="space-y-4">
              <h3 class="text-sm font-bold text-gray-800">{{ projectTypeName[formData.type] }}</h3>
              <div class="flex justify-center items-center py-8">
                <img
                  v-show="formData.type==='AGILE'"
                  src="../images/agile.png"
                  class="max-w-full h-auto"
                  style="width: 74%" />
                <img
                  v-show="formData.type==='GENERAL'"
                  src="../images/general.png"
                  class="max-w-full h-auto"
                  style="width: 92%" />
                <img
                  v-show="formData.type==='TESTING'"
                  src="../images/testing.png"
                  class="max-w-full h-auto"
                  style="width: 70%" />
              </div>
              <div class="space-y-3">
                <div
                  v-for="item in projectTypeTipConfig[formData.type]"
                  :key="item"
                  class="flex items-start space-x-2">
                  <Icon icon="icon-duihao-copy" class="text-green-500 text-xs mt-0.5 flex-shrink-0" />
                  <p class="text-xs text-gray-700 leading-relaxed">{{ item }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </TabPane>
      <TabPane key="module" :tab="t('project.projectDetail.tabs.module')">
        <Module
          :projectId="props.projectId"
          :disabled="true"
          :projectName="formData.name" />
      </TabPane>
      <TabPane key="version" :tab="t('project.projectDetail.tabs.version')">
        <Version
          :projectId="props.projectId"
          class="!p-0"
          :showDetail="false" />
      </TabPane>
      <TabPane key="biaoqian" :tab="t('project.projectDetail.tabs.tag')">
        <Tags :projectId="props.projectId" :disabled="true" />
      </TabPane>
    </Tabs>
  </div>
</template>

<style scoped>

:deep(.ant-form) .desc-item .ant-form-item-label > label {
  @apply h-8;
}
</style>
