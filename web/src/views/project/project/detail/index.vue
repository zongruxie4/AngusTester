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
  AGILE: ['支持Scrum敏捷开发与敏捷测试流程，旨在通过迭代和增量的方式开发和测试软件，‌这种方法可以有 效加速产品交付，同时确保产品开发方向始终和用 户的需求和期望保持一致。', '适用于软件项目和具有敏捷经验的团队。'],
  GENERAL: ['支持通用化研发和测试流程，将项目递归分解为较小、更易管理的组件的层次结构，然后基于看板、平铺或甘特图管理任务进度、依赖关系和时间线。', '适用于有软件项目和所有开发测试团队。'],
  TESTING: ['支持通用测试流程，包括指定测试计划，开展功能测试、接口测试、场景测试等活动。', '适用于所有软件测试团队。']
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
            <FormItem label="负责人:" name="ownerId">
              <div class="text-3">
                {{ formData.ownerName }}
              </div>
            </FormItem>

            <FormItem label="时间:">
              <div v-if="formData.startDate" class="text-3">
                {{ dayjs(formData.startDate).format('YYYY-MM-DD') }} - {{ dayjs(formData.deadlineDate).format('YYYY-MM-DD') || '' }}
              </div>
            </FormItem>

            <FormItem label="成员:">
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

            <FormItem label="描述:">
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
