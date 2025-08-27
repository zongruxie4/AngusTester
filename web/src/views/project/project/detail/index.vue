<script lang="ts" setup>
// Vue composition API imports
import {defineAsyncComponent, ref, watch} from 'vue';
import {useI18n} from 'vue-i18n';

// External libraries
import dayjs from 'dayjs';

// Custom UI components
import {Icon, Image} from '@xcan-angus/vue-ui';

// Ant Design components
import {TabPane, Tabs, Tag} from 'ant-design-vue';

// API imports
import {project} from '@/api/tester';
import {Project} from '@/views/project/project/types';

// Static assets
import DefaultProjectImage from '@/views/project/project/images/default.png';
import {ProjectType} from "@/enums/enums";
// Initialize i18n
const { t } = useI18n();

interface Props {
  closable: boolean;
  projectId: string;
  data?: {
    tab: string
  }
}

// Props
const props = withDefaults(defineProps<Props>(), {
  visible: false,
  dataSource: undefined,
  closable: true,
  data: () => ({
    tab: ''
  })
});

// Async component definitions
const Tags = defineAsyncComponent(() => import('@/views/project/project/edit/tag/index.vue'));
const Module = defineAsyncComponent(() => import('@/views/project/project/edit/module/index.vue'));
const Version = defineAsyncComponent(() => import('@/views/task/version/list/index.vue'));
const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));

// Configuration objects
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

// Reactive data
const activeKey = ref('basic');

const detailData = ref({} as Project);

// Data loading function
const getDetailData = async () => {
  const [error, { data }] = await project.getProjectDetail(props.projectId);
  if (error) {
    return;
  }
  detailData.value = {
    ...data,
    type: data.type || { value: ProjectType.AGILE, message: 'Agile' },
    members: data.members || { USER: [], DEPT: [], GROUP: [] }
  };
};

// Watchers
watch(() => props.projectId, newValue => {
  if (!newValue) {
    return;
  }
  getDetailData();
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
          <!-- Left side: Project basic information -->
          <div class="lg:col-span-2 space-y-6">
            <!-- Project avatar and name -->
            <div class="flex items-center space-x-4">
              <div class="flex-shrink-0">
                <Image
                  v-if="detailData.avatar"
                  :src="detailData.avatar"
                  class="w-10 h-10 rounded-lg object-cover border border-gray-200"
                  alt="avatar" />
                <img
                  v-else
                  class="w-10 h-10 rounded-lg object-cover border border-gray-200"
                  :src="DefaultProjectImage" />
              </div>
              <div class="flex items-center min-w-0 pt-2">
                <h1 class="text-lg font-bold text-blue-600 truncate">{{ detailData.name }}</h1>
              </div>
            </div>

            <!-- Project information grid -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <!-- Time plan -->
              <div v-if="detailData.startDate" class="space-y-2">
                <div class="flex items-center space-x-2">
                  <div class="w-1 h-4 bg-gradient-to-b from-blue-500 to-blue-600 rounded-full"></div>
                  <span class="text-xs font-semibold text-gray-600">{{ t('project.projectDetail.form.timePlan') }}</span>
                </div>
                <div class="flex items-center space-x-1 text-green-600 ">
                  <Icon icon="icon-time" class="text-xs" />
                  <span class="text-xs font-medium">
                    {{
                      dayjs(detailData.startDate).format('YYYY-MM-DD')
                    }} - {{ dayjs(detailData.deadlineDate).format('YYYY-MM-DD') || '' }}
                  </span>
                </div>
              </div>

              <!-- Project owner -->
              <div class="space-y-2">
                <div class="flex items-center space-x-2">
                  <div class="w-1 h-4 bg-gradient-to-b from-blue-500 to-blue-600 rounded-full"></div>
                  <span class="text-xs font-semibold text-gray-600">{{ t('project.projectDetail.form.owner') }}</span>
                </div>
                <div class="flex items-center space-x-1 text-purple-600 ">
                  <Icon icon="icon-user" class="text-xs" />
                  <span class="text-xs font-medium">{{ detailData.ownerName }}</span>
                </div>
              </div>
            </div>

            <!-- Project members -->
            <div class="space-y-4">
              <div class="flex items-center space-x-2">
                <div class="w-1 h-4 bg-gradient-to-b from-blue-500 to-blue-600 rounded-full"></div>
                <span class="text-xs font-semibold text-gray-600">{{ t('project.projectDetail.form.projectMembers') }}</span>
              </div>
              <Tabs size="small" class="member-tabs">
                <TabPane key="user" :tab="t('project.projectDetail.form.user')">
                  <div class="flex flex-wrap gap-2">
                    <div
                      v-for="(member, idx) in detailData.members?.USER || []"
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
                      v-for="(group, idx) in detailData.members?.GROUP || []"
                      :key="idx"
                      class="px-3 py-1 bg-gray-100 hover:bg-gray-200 text-gray-700 border-gray-300 transition-colors text-xs">
                      {{ group.name }}
                    </Tag>
                  </div>
                </TabPane>
                <TabPane key="dept" :tab="t('project.projectDetail.form.department')">
                  <div class="flex flex-wrap gap-2">
                    <Tag
                      v-for="(dept, idx) in detailData.members?.DEPT || []"
                      :key="idx"
                      class="px-3 py-1 bg-gray-100 hover:bg-gray-200 text-gray-700 border-gray-300 transition-colors text-xs">
                      {{ dept.name }}
                    </Tag>
                  </div>
                </TabPane>
              </Tabs>
            </div>

            <!-- Project description -->
            <div class="space-y-3">
              <div class="flex items-center space-x-2">
                <div class="w-1 h-4 bg-gradient-to-b from-blue-500 to-blue-600 rounded-full"></div>
                <span class="text-xs font-semibold text-gray-600">{{ t('project.projectDetail.form.projectDescription') }}</span>
              </div>
              <div class="text-gray-700 text-xs ">
                <RichEditor
                  :value="detailData.description"
                  :emptyText="t('project.projectDetail.form.noDescription')"
                  mode="view" />
              </div>
            </div>
          </div>

          <!-- Right side: Project type information -->
          <div class="space-y-6">
            <div class="space-y-4">
              <h3 class="section-title">
                {{
                  detailData.type?.value ? projectTypeName[detailData.type.value as keyof typeof projectTypeName] : ''
                }}
              </h3>
              <div class="flex justify-center items-center py-8 preview-image">
                <img
                  v-show="detailData.type?.value === ProjectType.AGILE"
                  src="../images/agile.png"
                  class="max-w-full h-auto"
                  style="width: 64%" />
                <img
                  v-show="detailData.type?.value === ProjectType.GENERAL"
                  src="../images/general.png"
                  class="max-w-full h-auto"
                  style="width: 72%" />
                <img
                  v-show="detailData.type?.value === ProjectType.TESTING"
                  src="../images/testing.png"
                  class="max-w-full h-auto"
                  style="width: 60%" />
              </div>
              <div class="preview-features">
                <div
                  v-for="(item, index) in projectTypeTipConfig[detailData.type?.value as keyof typeof projectTypeTipConfig]"
                  :key="index"
                  class="feature-item">
                  <Icon icon="icon-duihao-copy" class="feature-icon" />
                  <p class="feature-text">{{ item }}</p>
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
          :projectName="detailData.name" />
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

.preview-image {
  padding: 24px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 20px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e5e7eb;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-top: 10px;
  margin-bottom: 10px;
}

.feature-icon {
  font-size: 14px;
  color: #059669;
  margin-top: 2px;
  flex-shrink: 0;
}

.feature-text {
  font-size: 14px;
  line-height: 1.5;
  color: #374151;
  margin: 0;
}
</style>
