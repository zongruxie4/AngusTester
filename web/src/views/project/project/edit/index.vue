<script lang="ts" setup>
import { defineAsyncComponent, inject, onMounted, ref } from 'vue';
import {
  Button,
  Form,
  FormItem,
  Popover,
  RadioButton,
  RadioGroup,
  TabPane,
  Tabs
} from 'ant-design-vue';
import { DatePicker, Icon, Image, Input, notification, Select, SelectUser, Cropper } from '@xcan-angus/vue-ui';
import { GM } from '@xcan-angus/tools';
import { project } from 'src/api/tester';

interface Props {
  projectId: string;
  data?: {
    tab: string
  }
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  data: undefined
});
const emit = defineEmits<{(e: 'ok'): void}>();

const changeProjectInfo = inject('changeProjectInfo', (projectId: string, force: boolean) => ({ projectId, force }));
const getNewCurrentProject = inject('getNewCurrentProject', () => (undefined));
const projectInfo = inject('projectInfo', ref({ id: '' }));
const delTabPane = inject('delTabPane');

const activeKey = ref('basic');
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

const members = ref({
  USER: [],
  DEPT: [],
  GROUP: []
});
const memberType = ref('user');
const descRef = ref();
const uploadAvatarVisible = ref(false);
const uploadParams = {
  bizKey: 'angusTesterProjectAvatar'
};

const uploadOption = {
  outputSize: 1,
  outputType: 'png',
  info: true,
  canScale: true,
  autoCrop: true,
  autoCropWidth: '280',
  autoCropHeight: '280',
  fixed: true,
  fixedNumber: [1, 1],
  full: false,
  fixedBox: false,
  canMove: true,
  canMoveBox: true,
  original: false,
  centerBox: false,
  high: true,
  infoTrue: false,
  maxImgSize: 2000,
  enlarge: 1,
  mode: 'contain',
  preW: 0,
  limitMinSize: [100, 100]
};

const defaultOptionsUser = ref<{ [key: string]: any }>({});
const defaultOptionsDept = ref<{ [key: string]: any }>({});
const defaultOptionsGroup = ref<{ [key: string]: any }>({});

const Tags = defineAsyncComponent(() => import('@/views/project/project/edit/tag/index.vue'));
const Module = defineAsyncComponent(() => import('@/views/project/project/edit/module/index.vue'));
const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));
const Version = defineAsyncComponent(() => import('@/views/task/version/list/index.vue'));

const toolbarOptions = ['title', 'color', 'weight', 'block', 'link', 'list', 'direction', 'table', 'zoom'];
const projectData = ref<{[key: string]: string | boolean | string[] | object}>({ type: 'AGILE', importExample: false });
const formRef = ref();
const loading = ref(false);
const loadProjectData = async () => {
  const [error, { data }] = await project.getProjectDetail(props.projectId);
  if (error) {
    return;
  }
  // projectData.value = data;
  const { startDate, deadlineDate, type, id, name, ownerId, description, avatar } = data;
  projectData.value = { id, name, ownerId, description, avatar };
  projectData.value.members = data.members;
  projectData.value.type = type?.value || 'AGILE';
  projectData.value.dateRange = [startDate, deadlineDate];
  if (projectData.value.members?.USER) {
    members.value.USER = projectData.value.members?.USER.map(i => {
      defaultOptionsUser.value[i.id] = { ...i, fullName: i.name };
      return i.id;
    });
  }

  if (projectData.value.members?.DEPT) {
    members.value.DEPT = projectData.value.members?.DEPT.map(i => {
      defaultOptionsDept.value[i.id] = { ...i, fullName: i.name };
      return i.id;
    });
  }

  if (projectData.value.members?.GROUP) {
    members.value.GROUP = projectData.value.members?.GROUP.map(i => {
      defaultOptionsGroup.value[i.id] = { ...i, fullName: i.name };
      return i.id;
    });
  }
};

const openCropper = () => {
  uploadAvatarVisible.value = true;
};

const uploadImgSuccess = (resp) => {
  projectData.value.avatar = resp?.data?.[0]?.url;
};

const delImg = () => {
  projectData.value.avatar = '';
};

const selectProjectType = (value) => {
  projectData.value.type = value;
};

const validateDesc = () => {
  if (descRef.value && descRef.value.getLength() > 2000) {
    return Promise.reject('不能超过2000字符');
  }
  return Promise.resolve();
};

const ok = async () => {
  formRef.value.validate().then(async () => {
    loading.value = true;
    const { USER, DEPT, GROUP } = members.value;
    const { dateRange, ...otherProject } = projectData.value;
    const [error] = props.projectId
      ? await project.updateProject({ ...otherProject, startDate: dateRange[0], deadlineDate: dateRange[1], memberTypeIds: { USER: USER.length ? USER : undefined, DEPT: DEPT.length ? DEPT : undefined, GROUP: GROUP.length ? GROUP : undefined } })
      : await project.addProject({ ...otherProject, startDate: dateRange[0], deadlineDate: dateRange[1], memberTypeIds: { USER: USER.length ? USER : undefined, DEPT: DEPT.length ? DEPT : undefined, GROUP: GROUP.length ? GROUP : undefined } });
    loading.value = false;
    if (error) {
      return;
    }
    if (props.projectId === projectInfo.value.id) {
      changeProjectInfo(props.projectId, true);
    }
    emit('ok');
    if (props.projectId) {
      notification.success('修改项目成功');
    } else {
      notification.success('创建项目成功');
      delTabPane('addProject');
    }
    getNewCurrentProject();
  });
};

const cancel = () => {
  delTabPane(`${props.projectId}_project`);
  delTabPane('addProject');
};

onMounted(() => {
  if (props.projectId) {
    loadProjectData();
    if (props.data?.tab) {
      activeKey.value = props.data.tab;
    }
  }
});

</script>
<template>
  <div class="p-5 h-full overflow-auto">
    <div v-if="!props.projectId" class="flex space-x-10">
      <Form
        ref="formRef"
        labelAlign="right"
        :colon="false"
        class="w-320"
        :model="projectData"
        :labelCol="{ style: {width: '70px'} }">
        <div class="flex space-x-7">
          <div class="flex flex-col space-y-4 w-40 h-100 cursor-pointer ">
            <div
              class="flex-1 flex flex-col items-center justify-around space-y-3  px-2 rounded py-4 border ant-card-hoverable relative"

              @click="selectProjectType('AGILE')">
              <Icon
                v-show="projectData.type === 'AGILE'"
                icon="icon-xuanzhongduigou"
                class="text-theme-special text-5 right-0 bottom-0 absolute " />
              <Icon icon="icon-minjiexiangmuguanli" class="text-10" />
              <div>敏捷项目管理</div>
            </div>
            <div
              class="flex-1 flex flex-col items-center justify-around space-y-3  px-2 rounded py-4 border ant-card-hoverable relative"

              @click="selectProjectType('GENERAL')">
              <Icon
                v-show="projectData.type === 'GENERAL'"
                icon="icon-xuanzhongduigou"
                class="text-theme-special text-5 right-0 bottom-0 absolute " />
              <Icon icon="icon-jiandanxiangmuguanli" class="text-10" />
              <div>一般项目管理</div>
            </div>

            <div
              class="flex-1 flex flex-col items-center justify-around space-y-3  px-2 rounded py-4 border ant-card-hoverable relative"

              @click="selectProjectType('TESTING')">
              <Icon
                v-show="projectData.type === 'TESTING'"
                icon="icon-xuanzhongduigou"
                class="text-theme-special text-5 right-0 bottom-0 absolute " />
              <Icon icon="icon-ceshixiangmuguanli" class="text-10" />
              <div>测试项目管理</div>
            </div>
          </div>
          <div class="w-186">
            <FormItem label=" ">
              <div v-if="projectData.avatar" class="flex space-x-2 justify-start">
                <div class="relative">
                  <Image
                    :src="projectData.avatar"
                    class="w-25 h-25"
                    alt="avatar" />
                  <div class="absolute left-0 top-0 right-0 bottom-0 text-center opacity-0 hover:opacity-100" style="background-color: rgba(15, 23, 35, 50%);">
                    <Icon
                      icon="icon-qingchu"
                      class="mt-10 cursor-pointer text-5 text-white"
                      @click="delImg" />
                  </div>
                </div>
              </div>
              <div v-else class="text-left">
                <div
                  class="ant-upload-text w-25 h-25 rounded cursor-pointer bg-blue-2  flex flex-col items-center spacey-2 justify-around"
                  @click="openCropper">
                  <img src="../images/default.png" class="w-15" />
                  <div class="text-3">点击替换图标</div>
                </div>
              </div>
            </FormItem>
            <FormItem
              label="项目名称"
              name="name"
              class="input_select"
              required>
              <Input
                v-model:value="projectData.name"
                placeholder="项目名称，最多100个字符"
                :maxlength="100">
              </Input>
            </FormItem>

            <FormItem
              label="时间计划"
              name="dateRange"
              class="input_select"
              required>
              <DatePicker
                v-model:value="projectData.dateRange"
                class="w-full"
                :allowClear="false"
                type="date-range">
              </DatePicker>
              <Popover placement="right">
                <template #content>
                  <div class="max-w-100">
                    时间限制有助于确保项目在约定的时间内完成，这对于满足客户期望和合同义务至关重要。
                  </div>
                </template>
                <Icon icon="icon-tishi1" class="text-tips absolute -right-5 top-2 text-3.5" />
              </Popover>
            </FormItem>
            <div class="flex items-center space-x-3">
              <FormItem
                label="负责人"
                name="ownerId"
                required
                class="relative input_select flex-1">
                <SelectUser
                  v-model:value="projectData.ownerId"
                  size="small"
                  placeholder="请选择项目负责人"
                  :allowClear="false" />
                <Popover placement="right">
                  <template #content>
                    <div class="max-w-100">
                      通过为项目设置负责人，可以明确责任和权利，更好地促进项目的完成和进度控制，如：解决问题、推进进度、协作团队成员等。
                    </div>
                  </template>
                  <Icon icon="icon-tishi1" class="text-tips absolute -right-5 top-2 text-3.5" />
                </Popover>
              </FormItem>
              <FormItem label="导入示例" class="import-example-label  flex-1 !mb-5 !ml-10">
                <RadioGroup
                  v-model:value="projectData.importExample"
                  :options="[{ value: true, label: '是'}, { value: false, label: '否' }]">
                </RadioGroup>
                <Popover placement="right">
                  <template #content>
                    <div class="max-w-100">
                      导入主要业务示例数据，用于快速熟悉AngusTester业务与功能演示。
                    </div>
                  </template>
                  <Icon icon="icon-tishi1" class="text-tips text-3.5" />
                </Popover>
              </FormItem>
            </div>

            <FormItem
              label="项目成员"
              class="input_select"
              required>
              <RadioGroup
                v-model:value="memberType"
                buttonStyle="solid"
                size="small">
                <RadioButton value="user">
                  用户
                </RadioButton>
                <RadioButton value="dept">
                  部门
                </RadioButton>
                <RadioButton value="group">
                  组
                </RadioButton>
              </RadioGroup>
              <Popover placement="right">
                <template #content>
                  <div class="max-w-110">
                    明确项目的参与人员，项目成员会自动分配项目下数据权限，方便团队协作。
                  </div>
                </template>
                <Icon icon="icon-tishi1" class="text-tips text-3.5 ml-2" />
              </Popover>
              <div class="mt-3.5">
                <Select
                  v-show="memberType === 'user'"
                  v-model:value="members.USER"
                  :showSearch="true"
                  placeholder="选择用户"
                  :action="`${GM}/user?fullTextSearch=true`"
                  :defaultOptions="defaultOptionsUser"
                  mode="multiple"
                  :fieldNames="{ label: 'fullName', value: 'id' }">
                </Select>

                <Select
                  v-show="memberType === 'dept'"
                  v-model:value="members.DEPT"
                  placeholder="选择部门"
                  :showSearch="true"
                  :action="`${GM}/dept?fullTextSearch=true`"
                  :defaultOptions="defaultOptionsDept"
                  mode="multiple"
                  :fieldNames="{ label: 'name', value: 'id' }">
                </Select>

                <Select
                  v-show="memberType === 'group'"
                  v-model:value="members.GROUP"
                  placeholder="选择组"
                  :showSearch="true"
                  :action="`${GM}/group?fullTextSearch=true`"
                  :defaultOptions="defaultOptionsGroup"
                  mode="multiple"
                  :fieldNames="{ label: 'name', value: 'id' }">
                </Select>
              </div>
            </FormItem>
            <FormItem
              label="项目描述"
              name="description"
              :rules="[{validator: validateDesc}]">
              <RichEditor
                ref="descRef"
                v-model:value="projectData.description"
                :toolbarOptions="toolbarOptions"
                :options="{placeholder: '描述，最多2000个字符'}" />
            </FormItem>
          </div>
          <div class="w-100">
            <div class="text-5 font-semibold mb-2">{{ projectTypeName[projectData.type] }}</div>
            <div class="py-3 border rounded min-h-40 flex flex-col justify-center items-center">
              <img
                v-show="projectData.type==='AGILE'"
                src="../../add/agile.png"
                class=" inline-block"
                style="width: 74%" />
              <img
                v-show="projectData.type==='GENERAL'"
                src="../../add/general.png"
                class=" inline-block"
                style="width: 92%" />
              <img
                v-show="projectData.type==='TESTING'"
                src="../../add/testing.png"
                class=" inline-block"
                style="width: 70%" />
            </div>
            <div class="space-y-2 mt-7">
              <div v-for="item in projectTypeTipConfig[projectData.type]" class="flex space-x-1">
                <Icon icon="icon-duihao-copy" class="text-3.5 mt-1" />
                <p class="text-3.5">{{ item }}</p>
              </div>
            </div>
          </div>
        </div>
        <FormItem label="" class="mt-5">
          <div class="flex justify-center items-center">
            <Button
              type="primary"
              size="small"
              htmlType="submit"
              :loading="loading"
              @click="ok">
              <Icon icon="icon-dangqianxuanzhong" class="mr-1" />
              保存
            </Button>
            <Button
              size="small"
              class="ml-2"
              @click="cancel">
              取消
            </Button>
          </div>
        </FormItem>
      </Form>
    </div>

    <Tabs
      v-if="props.projectId"
      v-model:activeKey="activeKey"
      size="small">
      <TabPane key="basic" tab="基本信息">
        <Form
          ref="formRef"
          labelAlign="right"
          :colon="false"
          class="w-300 pb-10"
          :model="projectData"
          :labelCol="{ style: {width: '70px'} }">
          <div class="flex space-x-10">
            <div class="w-170">
              <FormItem label=" ">
                <div v-if="projectData.avatar" class="flex space-x-2 justify-start">
                  <div class="relative">
                    <Image
                      :src="projectData.avatar"
                      class="w-25 h-25"
                      alt="avatar" />
                    <div class="absolute left-0 top-0 right-0 bottom-0 text-center opacity-0 hover:opacity-100" style="background-color: rgba(15, 23, 35, 50%);">
                      <Icon
                        icon="icon-qingchu"
                        class="mt-10 cursor-pointer text-5 text-white"
                        @click="delImg" />
                    </div>
                  </div>
                </div>
                <div v-else class="text-left">
                  <div
                    class="ant-upload-text w-25 h-25 rounded cursor-pointer bg-blue-2  flex flex-col items-center spacey-2 justify-around"
                    @click="openCropper">
                    <img src="../images/default.png" class="w-15" />
                    <div class="text-3">点击替换图标</div>
                  </div>
                </div>
              </FormItem>
              <FormItem
                label="项目名称"
                name="name"
                class="input_select"
                required>
                <Input
                  v-model:value="projectData.name"
                  placeholder="项目名称，最多100个字符"
                  :maxlength="100">
                </Input>
              </FormItem>

              <FormItem
                label="时间计划"
                name="dateRange"
                class="input_select"
                required>
                <DatePicker
                  v-model:value="projectData.dateRange"
                  class="w-full"
                  :allowClear="false"
                  type="date-range">
                </DatePicker>
                <Popover placement="right">
                  <template #content>
                    <div class="max-w-100">
                      时间限制有助于确保项目在约定的时间内完成，这对于满足客户期望和合同义务至关重要。
                    </div>
                  </template>
                  <Icon icon="icon-tishi1" class="text-tips absolute -right-5 top-2 text-3.5" />
                </Popover>
              </FormItem>

              <FormItem
                label="负责人"
                name="ownerId"
                required
                class="relative input_select">
                <SelectUser
                  v-model:value="projectData.ownerId"
                  size="small"
                  placeholder="请选择项目负责人"
                  :allowClear="false" />
                <Popover placement="right">
                  <template #content>
                    <div class="max-w-100">
                      通过为项目设置负责人，可以明确责任和权利，更好地促进项目的完成和进度控制，如：解决问题、推进进度、协作团队成员等。
                    </div>
                  </template>
                  <Icon icon="icon-tishi1" class="text-tips absolute -right-5 top-2 text-3.5" />
                </Popover>
              </FormItem>

              <FormItem
                label="项目成员"
                class="input_select"
                required>
                <RadioGroup
                  v-model:value="memberType"
                  buttonStyle="solid"
                  size="small">
                  <RadioButton value="user">
                    用户
                  </RadioButton>
                  <RadioButton value="dept">
                    部门
                  </RadioButton>
                  <RadioButton value="group">
                    组
                  </RadioButton>
                </RadioGroup>
                <Popover placement="right">
                  <template #content>
                    <div class="max-w-110">
                      明确项目的参与人员，项目成员会自动分配项目下数据权限，方便团队协作。
                    </div>
                  </template>
                  <Icon icon="icon-tishi1" class="text-tips text-3.5 ml-2" />
                </Popover>
                <div class="mt-3.5">
                  <Select
                    v-show="memberType === 'user'"
                    v-model:value="members.USER"
                    :showSearch="true"
                    placeholder="选择用户"
                    :action="`${GM}/user?fullTextSearch=true`"
                    :defaultOptions="defaultOptionsUser"
                    mode="multiple"
                    :fieldNames="{ label: 'fullName', value: 'id' }">
                  </Select>

                  <Select
                    v-show="memberType === 'dept'"
                    v-model:value="members.DEPT"
                    placeholder="选择部门"
                    :showSearch="true"
                    :action="`${GM}/dept?fullTextSearch=true`"
                    :defaultOptions="defaultOptionsDept"
                    mode="multiple"
                    :fieldNames="{ label: 'name', value: 'id' }">
                  </Select>

                  <Select
                    v-show="memberType === 'group'"
                    v-model:value="members.GROUP"
                    placeholder="选择组"
                    :showSearch="true"
                    :action="`${GM}/group?fullTextSearch=true`"
                    :defaultOptions="defaultOptionsGroup"
                    mode="multiple"
                    :fieldNames="{ label: 'name', value: 'id' }">
                  </Select>
                </div>
              </FormItem>

              <FormItem
                label="项目描述"
                name="description"
                :rules="[{validator: validateDesc}]">
                <RichEditor
                  ref="descRef"
                  v-model:value="projectData.description"
                  :toolbarOptions="toolbarOptions"
                  :options="{placeholder: '描述，最多2000个字符'}" />
              </FormItem>
            </div>
            <div class="w-100 pt-12">
              <div class="text-5 font-semibold mb-2">{{ projectTypeName[projectData.type] }}</div>
              <div class="py-3 border rounded min-h-40 flex flex-col justify-center items-center">
                <img
                  v-show="projectData.type==='AGILE'"
                  src="../../add/agile.png"
                  class="inline-block"
                  style="width: 74%" />
                <img
                  v-show="projectData.type==='GENERAL'"
                  src="../../add/general.png"
                  class="inline-block"
                  style="width: 92%" />
                <img
                  v-show="projectData.type==='TESTING'"
                  src="../../add/testing.png"
                  class="inline-block"
                  style="width: 70%" />
              </div>
              <div class="space-y-2 mt-7">
                <div v-for="item in projectTypeTipConfig[projectData.type]" class="flex space-x-1">
                  <Icon icon="icon-duihao-copy" class="text-3.5 mt-1" />
                  <p class="text-3.5">{{ item }}</p>
                </div>
              </div>
            </div>
          </div>
          <FormItem label=" " class="mt-5">
            <div class="flex items-center">
              <Button
                type="primary"
                size="small"
                htmlType="submit"
                :loading="loading"
                @click="ok">
                <Icon icon="icon-dangqianxuanzhong" class="mr-1" />
                保存
              </Button>
              <Button
                size="small"
                class="ml-2"
                @click="cancel">
                取消
              </Button>
            </div>
          </FormItem>
        </Form>
      </TabPane>
      <TabPane key="module" tab="软件模块">
        <Module :projectId="props.projectId" :projectName="projectData.name" />
      </TabPane>
      <TabPane key="version" tab="软件版本">
        <Version
          :projectId="props.projectId"
          class="!p-0"
          :showDetail="false" />
      </TabPane>
      <TabPane key="biaoqian" tab="数据标签">
        <Tags :projectId="props.projectId" />
      </TabPane>
    </Tabs>
    <Cropper
      v-model:visible="uploadAvatarVisible"
      title="上传图标"
      :params="uploadParams"
      :options="uploadOption"
      @success="uploadImgSuccess" />
  </div>
</template>
<style scoped>

</style>
