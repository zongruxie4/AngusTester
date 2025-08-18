<script lang="ts" setup>
import { defineAsyncComponent, inject, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
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
import { GM } from '@xcan-angus/infra';
import { project } from 'src/api/tester';

const { t } = useI18n();

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
  AGILE: [t('project.projectEdit.projectTypeTip.agile.features'), t('project.projectEdit.projectTypeTip.agile.scenarios')],
  GENERAL: [t('project.projectEdit.projectTypeTip.general.features'), t('project.projectEdit.projectTypeTip.general.scenarios')],
  TESTING: [t('project.projectEdit.projectTypeTip.testing.features'), t('project.projectEdit.projectTypeTip.testing.scenarios')]
};

const projectTypeName = {
  AGILE: t('project.projectEdit.projectTypeName.agile'),
  GENERAL: t('project.projectEdit.projectTypeName.general'),
  TESTING: t('project.projectEdit.projectTypeName.testing')
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
    return Promise.reject(t('project.projectEdit.validation.maxCharacters'));
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
              notification.success(t('project.projectEdit.messages.updateSuccess'));
    } else {
              notification.success(t('project.projectEdit.messages.createSuccess'));
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
              <div>{{ t('project.projectEdit.projectTypeName.agile') }}</div>
            </div>
            <div
              class="flex-1 flex flex-col items-center justify-around space-y-3  px-2 rounded py-4 border ant-card-hoverable relative"

              @click="selectProjectType('GENERAL')">
              <Icon
                v-show="projectData.type === 'GENERAL'"
                icon="icon-xuanzhongduigou"
                class="text-theme-special text-5 right-0 bottom-0 absolute " />
              <Icon icon="icon-jiandanxiangmuguanli" class="text-10" />
              <div>{{ t('project.projectEdit.projectTypeName.general') }}</div>
            </div>

            <div
              class="flex-1 flex flex-col items-center justify-around space-y-3  px-2 rounded py-4 border ant-card-hoverable relative"

              @click="selectProjectType('TESTING')">
              <Icon
                v-show="projectData.type === 'TESTING'"
                icon="icon-xuanzhongduigou"
                class="text-theme-special text-5 right-0 bottom-0 absolute " />
              <Icon icon="icon-ceshixiangmuguanli" class="text-10" />
              <div>{{ t('project.projectEdit.projectTypeName.testing') }}</div>
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
                  <div class="text-3">{{ t('project.projectEdit.actions.clickToReplaceIcon') }}</div>
                </div>
              </div>
            </FormItem>
            <FormItem
              :label="t('project.projectEdit.form.projectName')"
              name="name"
              class="input_select"
              required>
              <Input
                v-model:value="projectData.name"
                :placeholder="t('project.projectEdit.form.projectNamePlaceholder')"
                :maxlength="100">
              </Input>
            </FormItem>

            <FormItem
              :label="t('project.projectEdit.form.timePlan')"
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
                    {{ t('project.projectEdit.form.timePlanDescription') }}
                  </div>
                </template>
                <Icon icon="icon-tishi1" class="text-tips absolute -right-5 top-2 text-3.5" />
              </Popover>
            </FormItem>
            <div class="flex items-center space-x-3">
              <FormItem
                :label="t('project.projectEdit.form.owner')"
                name="ownerId"
                required
                class="relative input_select flex-1">
                <SelectUser
                  v-model:value="projectData.ownerId"
                  size="small"
                  :placeholder="t('project.projectEdit.form.ownerPlaceholder')"
                  :allowClear="false" />
                <Popover placement="right">
                  <template #content>
                    <div class="max-w-100">
                     {{ t('project.projectEdit.form.ownerDescription') }}
                    </div>
                  </template>
                  <Icon icon="icon-tishi1" class="text-tips absolute -right-5 top-2 text-3.5" />
                </Popover>
              </FormItem>
              <FormItem :label="t('project.projectEdit.form.importExample')" class="import-example-label  flex-1 !mb-5 !ml-10">
                <RadioGroup
                  v-model:value="projectData.importExample"
                  :options="[{ value: true, label: '是'}, { value: false, label: '否' }]">
                </RadioGroup>
                <Popover placement="right">
                  <template #content>
                    <div class="max-w-100">
                      {{t('project.projectEdit.form.importExampleDescription')}}
                    </div>
                  </template>
                  <Icon icon="icon-tishi1" class="text-tips text-3.5" />
                </Popover>
              </FormItem>
            </div>

            <FormItem
              :label="t('project.projectEdit.form.projectMembers')"
              class="input_select"
              required>
              <RadioGroup
                v-model:value="memberType"
                buttonStyle="solid"
                size="small">
                <RadioButton value="user">
                  {{t('project.projectEdit.form.user')}}
                </RadioButton>
                <RadioButton value="dept">
                  {{ t('project.projectEdit.form.department') }}
                </RadioButton>
                <RadioButton value="group">
                  {{ t('project.projectEdit.form.group') }}
                </RadioButton>
              </RadioGroup>
              <Popover placement="right">
                <template #content>
                  <div class="max-w-110">
                    {{ t('project.projectEdit.form.membersDescription') }}
                  </div>
                </template>
                <Icon icon="icon-tishi1" class="text-tips text-3.5 ml-2" />
              </Popover>
              <div class="mt-3.5">
                <Select
                  v-show="memberType === 'user'"
                  v-model:value="members.USER"
                  :showSearch="true"
                  :placeholder="t('project.projectEdit.form.selectUser')"
                  :action="`${GM}/user?fullTextSearch=true`"
                  :defaultOptions="defaultOptionsUser"
                  mode="multiple"
                  :fieldNames="{ label: 'fullName', value: 'id' }">
                </Select>

                <Select
                  v-show="memberType === 'dept'"
                  v-model:value="members.DEPT"
                  :placeholder="t('project.projectEdit.form.selectDepartment')"
                  :showSearch="true"
                  :action="`${GM}/dept?fullTextSearch=true`"
                  :defaultOptions="defaultOptionsDept"
                  mode="multiple"
                  :fieldNames="{ label: 'name', value: 'id' }">
                </Select>

                <Select
                  v-show="memberType === 'group'"
                  v-model:value="members.GROUP"
                  :placeholder="t('project.projectEdit.form.selectGroup')"
                  :showSearch="true"
                  :action="`${GM}/group?fullTextSearch=true`"
                  :defaultOptions="defaultOptionsGroup"
                  mode="multiple"
                  :fieldNames="{ label: 'name', value: 'id' }">
                </Select>
              </div>
            </FormItem>
            <FormItem
              :label="t('project.projectEdit.form.description')"
              name="description"
              :rules="[{validator: validateDesc}]">
              <RichEditor
                ref="descRef"
                v-model:value="projectData.description"
                :toolbarOptions="toolbarOptions"
                :options="{placeholder: t('project.projectEdit.form.descriptionPlaceholder')}" />
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
              {{ t('actions.save') }}
            </Button>
            <Button
              size="small"
              class="ml-2"
              @click="cancel">
              {{t('actions.cancel')}}
            </Button>
          </div>
        </FormItem>
      </Form>
    </div>

    <Tabs
      v-if="props.projectId"
      v-model:activeKey="activeKey"
      size="small">
      <TabPane key="basic" :tab="t('project.projectEdit.tabs.basicInfo')">
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
                    <div class="text-3">{{ t('project.projectEdit.actions.clickToReplaceIcon') }}</div>
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
                      {{ t('project.projectEdit.form.timePlanDescription') }}
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
                  :placeholder="t('project.projectEdit.form.ownerPlaceholder')"
                  :allowClear="false" />
                <Popover placement="right">
                  <template #content>
                    <div class="max-w-100">
                      {{ t('project.projectEdit.form.ownerDescription') }}
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
                    {{t('project.projectEdit.form.user')}}
                  </RadioButton>
                  <RadioButton value="dept">
                    {{ t('project.projectEdit.form.department') }}
                  </RadioButton>
                  <RadioButton value="group">
                    {{t('project.projectEdit.form.group')}}
                  </RadioButton>
                </RadioGroup>
                <Popover placement="right">
                  <template #content>
                    <div class="max-w-110">
                      {{ t('project.projectEdit.form.membersDescription') }}
                    </div>
                  </template>
                  <Icon icon="icon-tishi1" class="text-tips text-3.5 ml-2" />
                </Popover>
                <div class="mt-3.5">
                  <Select
                    v-show="memberType === 'user'"
                    v-model:value="members.USER"
                    :showSearch="true"
                    :placeholder="t('project.projectEdit.form.selectUser')"
                    :action="`${GM}/user?fullTextSearch=true`"
                    :defaultOptions="defaultOptionsUser"
                    mode="multiple"
                    :fieldNames="{ label: 'fullName', value: 'id' }">
                  </Select>

                  <Select
                    v-show="memberType === 'dept'"
                    v-model:value="members.DEPT"
                    :placeholder="t('project.projectEdit.form.selectDepartment')"
                    :showSearch="true"
                    :action="`${GM}/dept?fullTextSearch=true`"
                    :defaultOptions="defaultOptionsDept"
                    mode="multiple"
                    :fieldNames="{ label: 'name', value: 'id' }">
                  </Select>

                  <Select
                    v-show="memberType === 'group'"
                    v-model:value="members.GROUP"
                    :placeholder="t('project.projectEdit.form.selectGroup')"
                    :showSearch="true"
                    :action="`${GM}/group?fullTextSearch=true`"
                    :defaultOptions="defaultOptionsGroup"
                    mode="multiple"
                    :fieldNames="{ label: 'name', value: 'id' }">
                  </Select>
                </div>
              </FormItem>

              <FormItem
                :label="t('project.projectEdit.form.projectDescription')"
                name="description"
                :rules="[{validator: validateDesc}]">
                <RichEditor
                  ref="descRef"
                  v-model:value="projectData.description"
                  :toolbarOptions="toolbarOptions"
                  :options="{placeholder: t('project.projectEdit.form.projectDescriptionPlaceholder')}" />
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
                {{t('actions.save')}}
              </Button>
              <Button
                size="small"
                class="ml-2"
                @click="cancel">
                {{t('actions.cancel')}}
              </Button>
            </div>
          </FormItem>
        </Form>
      </TabPane>
      <TabPane key="module" :tab="t('project.projectEdit.tabs.module')">
        <Module :projectId="props.projectId" :projectName="projectData.name" />
      </TabPane>
      <TabPane key="version" :tab="t('project.projectEdit.tabs.version')">
        <Version
          :projectId="props.projectId"
          class="!p-0"
          :showDetail="false" />
      </TabPane>
      <TabPane key="biaoqian" :tab="t('project.projectEdit.tabs.tag')">
        <Tags :projectId="props.projectId" />
      </TabPane>
    </Tabs>
    <Cropper
      v-model:visible="uploadAvatarVisible"
      :title="t('project.projectEdit.actions.uploadIcon')"
      :params="uploadParams"
      :options="uploadOption"
      @success="uploadImgSuccess" />
  </div>
</template>
<style scoped>

</style>
