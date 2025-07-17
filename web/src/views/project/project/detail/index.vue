<script lang="ts" setup>
import { defineAsyncComponent, ref, watch } from 'vue';
import { Image, notification, Icon } from '@xcan-angus/vue-ui';
import { Form, FormItem, TabPane, Tabs, Tag } from 'ant-design-vue';
import { project } from 'src/api/tester';
import DefaultProjectPng from '@/views/project/project/images/default.png';
import dayjs from 'dayjs';

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

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'cancel', value: boolean): void;
  (e: 'ok'): void;
}>();

const projectTypeTipConfig = {
  AGILE: ['核心特点：Scrum框架支持 • 增量交付 • 用户需求驱动。', '适用场景：敏捷开发团队快速迭代产品。'],
  GENERAL: ['核心特点：工作分解结构 • 进度管理 • 资源依赖可视化。', '适用场景：传统研发团队跨部门协作项目。'],
  TESTING: ['核心特点：测试计划定制 • 多类型测试 • 缺陷跟踪闭环。', '适用场景：质量保障团队专项测试项目。']
};

const projectTypeName = {
  AGILE: '敏捷项目管理',
  GENERAL: '一般项目管理',
  TESTING: '测试项目管理'
};
const activeKey = ref('basic');
const formRef = ref();
// const memberType = ref('user');

const loading = ref(false);
const formData = ref({
  name: '',
  ownerId: undefined,
  description: '',
  avatar: '',
  type: 'AGILE'
});

const members = ref({
  USER: [],
  DEPT: [],
  GROUP: []
});

const loadformData = async () => {
  const [error, { data }] = await project.getProjectDetail(props.projectId);
  if (error) {
    return;
  }
  formData.value = data;
  formData.value.type = data.type?.value || 'AGILE';
  if (formData.value.members?.USER) {
    members.value.USER = formData.value.members?.USER.map(i => {
      defaultOptionsUser.value[i.id] = { ...i, fullName: i.name };
      return i.id;
    });
  }

  if (formData.value.members?.DEPT) {
    members.value.DEPT = formData.value.members?.DEPT.map(i => {
      defaultOptionsDept.value[i.id] = { ...i, fullName: i.name };
      return i.id;
    });
  }

  if (formData.value.members?.GROUP) {
    members.value.GROUP = formData.value.members?.GROUP.map(i => {
      defaultOptionsGroup.value[i.id] = { ...i, fullName: i.name };
      return i.id;
    });
  }
};

const defaultOptionsUser = ref<{ [key: string]: any }>({});
const defaultOptionsDept = ref<{ [key: string]: any }>({});
const defaultOptionsGroup = ref<{ [key: string]: any }>({});

const ok = async () => {
  formRef.value.validate().then(async () => {
    loading.value = true;
    const { USER, DEPT, GROUP } = members.value;
    const [error] = !formData.value.id
      ? await project.addProject({ ...formData.value, memberTypeIds: { USER: USER.length ? USER : undefined, DEPT: DEPT.length ? DEPT : undefined, GROUP: GROUP.length ? GROUP : undefined } })
      : await project.replaceProject({ ...formData.value, memberTypeIds: { USER: USER.length ? USER : undefined, DEPT: DEPT.length ? DEPT : undefined, GROUP: GROUP.length ? GROUP : undefined } });
    loading.value = false;
    if (error) {
      return;
    }
    if (formData.value.id) {
      notification.success('修改项目成功');
    } else {
      notification.success('添加项目成功');
    }

    emit('update:visible', false);
    emit('ok');
  });
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
      <TabPane key="basic" tab="基本信息">
        <div class="flex space-x-5">
          <Form
            ref="formRef"
            :colon="false"
            :model="formData"
            :labelCol="{ span: 3 }"
            class="w-120 text-3">
            <FormItem label=" ">
              <div class="flex space-x-2">
                <Image
                  v-if="formData.avatar"
                  :src="formData.avatar"
                  class="w-25 h-25"
                  alt="avatar" />
                <img
                  v-else
                  class="w-20 h-20 m-2"
                  :src="DefaultProjectPng" />
              </div>
            </FormItem>
            <FormItem label="名称:" name="name">
              <div class="text-3">
                {{ formData.name }}
              </div>
            </FormItem>


            <FormItem label="时间计划:">
              <div v-if="formData.startDate" class="text-3">
                {{ dayjs(formData.startDate).format('YYYY-MM-DD') }} - {{ dayjs(formData.deadlineDate).format('YYYY-MM-DD') || '' }}
              </div>
            </FormItem>

            <FormItem label="负责人:" name="ownerId">
              <div class="text-3">
                {{ formData.ownerName }}
              </div>
            </FormItem>

            <FormItem label="成员:" class="member-item">
              <Tabs size="small" class="-mt-1.5">
                <TabPane key="user" tab="用户">
                  <div class="flex flex-1 flex-wrap">
                    <div
                      v-for="(avatars, idx) in formData.members?.USER || []"
                      :key="idx">
                      <div class=" pr-2 inline-flex space-x-1">
                        <Image
                          class="w-5 h-5 rounded-full"
                          type="avatar"
                          :src="avatars.avatar" />
                        <span>{{ avatars.name }}</span>
                      </div>
                    </div>
                  </div>
                </TabPane>
                <TabPane key="group" tab="组">
                  <div class="flex flex-1 flex-wrap">
                    <div
                      v-for="(avatars, idx) in formData.members?.GROUP || []"
                      :key="idx"
                      class="pr-1">
                      <Tag class="h-5 leading-5">{{ avatars.name }}</Tag>
                    </div>
                  </div>
                </TabPane>
                <TabPane key="dept" tab="部门">
                  <div class="flex flex-1 flex-wrap">
                    <div
                      v-for="(avatars, idx) in formData.members?.DEPT || []"
                      :key="idx"
                      class="pr-1">
                      <Tag class="h-5 leading-5">{{ avatars.name }}</Tag>
                    </div>
                  </div>
                </TabPane>
              </Tabs>
            </FormItem>

            <FormItem label="描述:" class="desc-item">
              <RichEditor
                :value="formData.description"
                emptyText="无描述~"
                mode="view" />
            </FormItem>
          </Form>
          <div class="w-100">
            <div class="text-5 font-semibold mb-2">{{ projectTypeName[formData.type] }}</div>
            <div class="py-3 border rounded min-h-40 flex flex-col justify-center items-center">
              <img
                v-show="formData.type==='AGILE'"
                src="../../add/agile.png"
                class=" inline-block"
                style="width: 74%" />
              <img
                v-show="formData.type==='GENERAL'"
                src="../../add/general.png"
                class=" inline-block"
                style="width: 92%" />
              <img
                v-show="formData.type==='TESTING'"
                src="../../add/testing.png"
                class=" inline-block"
                style="width: 70%" />
            </div>
            <div class="space-y-2 mt-7">
              <div v-for="item in projectTypeTipConfig[formData.type]" class="flex space-x-1">
                <Icon icon="icon-duihao-copy" class="text-3.5 mt-1" />
                <p class="text-3.5">{{ item }}</p>
              </div>
            </div>
          </div>
        </div>
      </TabPane>
      <TabPane key="module" tab="软件模块">
        <Module
          :projectId="props.projectId"
          :disabled="true"
          :projectName="formData.name" />
      </TabPane>
      <TabPane key="version" tab="软件版本">
        <Version
          :projectId="props.projectId"
          class="!p-0"
          :showDetail="false" />
      </TabPane>
      <TabPane key="biaoqian" tab="数据标签">
        <Tags :projectId="props.projectId" :disabled="true" />
      </TabPane>
    </Tabs>
  </div>
</template>
<style scoped>
:deep(.ant-form) .ant-form-item-label > label {
  @apply h-8;
}
:deep(.ant-form) .member-item .ant-form-item-label > label {
  @apply h-6;
}
</style>
