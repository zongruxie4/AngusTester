<script lang="ts" setup>
import { computed, inject, nextTick, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon, Input, notification, Select, Spin } from '@xcan-angus/vue-ui';
import { Button, DatePicker, Form, FormItem, Popover } from 'ant-design-vue';
import { EnumMessage, enumUtils, EvalWorkloadMethod, TESTER, utils } from '@xcan-angus/infra';
import dayjs from 'dayjs';
import { project, issue } from '@/api/tester';
import { EditFormState, MeetingInfo } from '../types';
import { TaskMeetingType } from '@/enums/enums';
import { BasicProps } from '@/types/types';

/**
 * Import Components
 */
import SelectEnum from '@/components/enum/SelectEnum.vue';
import RichEditor from '@/components/richEditor/index.vue';

// COMPONENT PROPS
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  data: undefined
});

// COMPOSABLES & INJECTIONS
const { t } = useI18n();
const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));
const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));
const replaceTabPane = inject<(id: string, data: { [key: string]: any }) => void>('replaceTabPane', () => ({}));

// REACTIVE STATE
const formRef = ref();
const contentRichEditorRef = ref();

const evalWorkloadMethodOptions = ref<EnumMessage<EvalWorkloadMethod>[]>([]);
const meetingDataSource = ref<MeetingInfo>();
const meetingFormState = ref<EditFormState>({
  content: '',
  date: '',
  location: '',
  moderator: undefined,
  participants: [],
  projectId: '',
  sprintId: undefined,
  subject: '',
  timeEnd: '',
  timeStart: '',
  endTime: '',
  startTime: '',
  type: TaskMeetingType.DAILY_STANDUP
});

const isLoading = ref(false);
const projectMembers = ref<{ fullName: string, id: string; }[]>([]);

/**
 * Determines if the form is in edit mode based on data prop
 * @returns True if editing existing meeting, false if creating new meeting
 */
const isEditMode = computed(() => {
  return !!props.data?.id;
});

/**
 * Field name mapping for select components
 */
const selectFieldNames = {
  label: 'fullName',
  value: 'id'
};

/**
 * Builds API parameters from form state
 * @returns Formatted parameters for API calls
 */
const buildApiParameters = () => {
  const {
    content,
    date,
    timeEnd,
    timeStart,
    type,
    location,
    moderator,
    subject,
    participants,
    sprintId
  } = meetingFormState.value;
  return {
    content,
    date,
    time: `${timeStart}~${timeEnd}`,
    location,
    moderator: projectMembers.value.find(member => member.id === moderator) || { fullName: '', id: '' },
    participants: participants.map(participantId => projectMembers.value.find(member => member.id === participantId)).filter(Boolean),
    sprintId,
    subject,
    type,
    id: props.data?.id || undefined,
    projectId: props.projectId
  };
};

/**
 * Refreshes the meeting list tab
 */
const refreshMeetingList = () => {
  nextTick(() => {
    updateTabPane({ _id: 'meetingList', notify: utils.uuid() });
  });
};

/**
 * Validates that meeting content is required and not empty
 * @returns Promise that resolves if valid, rejects with error if invalid
 */
const validateContentRequired = async () => {
  if (!meetingFormState.value.content) {
    return Promise.reject(new Error(t('meeting.messages.contentRequired')));
  }

  const richEditorData = contentRichEditorRef.value?.getData();
  if (!richEditorData) {
    return Promise.reject(new Error(t('meeting.messages.contentRequired')));
  }

  const parsedData = JSON.parse(richEditorData);
  if (parsedData.length < 2) {
    if (parsedData.length === 1) {
      const contentValue = parsedData[0].insert.replaceAll('\n', '');
      if (!contentValue) {
        return Promise.reject(new Error(t('meeting.messages.contentRequired')));
      }
    }
  }

  return Promise.resolve();
};

/**
 * Validates that meeting time is required
 * @returns Promise that resolves if valid, rejects with error if invalid
 */
const validateTimeRequired = async () => {
  if (!meetingFormState.value.timeEnd || !meetingFormState.value.timeStart) {
    return Promise.reject(new Error(t('meeting.messages.timeRequired')));
  }
  return Promise.resolve();
};

/**
 * Handles meeting update operation
 */
const handleMeetingUpdate = async () => {
  const apiParams = buildApiParameters();
  isLoading.value = true;
  const [error] = await issue.updateMeeting(apiParams);
  isLoading.value = false;

  if (error) {
    return;
  }

  notification.success(t('actions.tips.editSuccess'));

  const meetingId = apiParams.id;
  const meetingSubject = apiParams.subject;
  updateTabPane({ name: meetingSubject, _id: meetingId });
  if (meetingDataSource.value) {
    meetingDataSource.value.subject = meetingSubject;
  }
};

/**
 * Handles meeting creation operation
 */
const handleMeetingCreation = async () => {
  const apiParams = buildApiParameters();
  isLoading.value = true;
  const [error, response] = await issue.addMeeting(apiParams);
  isLoading.value = false;

  if (error) {
    return;
  }

  notification.success(t('actions.tip.addSuccess'));

  const currentTabId = props.data?._id;
  const newMeetingId = response?.data?.id;
  const meetingSubject = apiParams.subject;
  if (currentTabId && newMeetingId) {
    replaceTabPane(currentTabId, {
      _id: newMeetingId,
      uiKey: newMeetingId,
      name: meetingSubject,
      data: { _id: newMeetingId, id: newMeetingId }
    });
  }
};

/**
 * Loads enum options for form dropdowns
 */
const loadEnumOptions = () => {
  evalWorkloadMethodOptions.value = enumUtils.enumToMessages(EvalWorkloadMethod);
};

/**
 * Loads project members for moderator and participants selection
 */
const loadProjectMembers = async () => {
  const [error, { data }] = await project.getProjectMember(props.projectId);
  if (error) {
    return;
  }
  projectMembers.value = (data || []).map(member => ({
    ...member
  }));
};

/**
 * Loads meeting detail data for editing
 * @param meetingId - ID of the meeting to load
 */
const loadMeetingData = async (meetingId: string) => {
  if (isLoading.value) {
    return;
  }

  isLoading.value = true;
  const [error, response] = await issue.getMeetingDetail(meetingId);
  isLoading.value = false;

  if (error) {
    return;
  }

  const meetingData = response?.data as MeetingInfo;
  if (!meetingData) {
    return;
  }

  meetingDataSource.value = meetingData;
  populateFormWithData(meetingData);

  const meetingSubject = meetingData.subject;
  if (meetingSubject && typeof updateTabPane === 'function') {
    updateTabPane({ name: meetingSubject, _id: meetingId });
  }
};

/**
 * Populates form with meeting data
 * @param meetingData - Meeting data to populate form with
 */
const populateFormWithData = (meetingData: MeetingInfo) => {
  if (!meetingData) {
    meetingFormState.value = {
      content: '',
      date: '',
      location: '',
      moderator: undefined,
      participants: [],
      projectId: '',
      sprintId: undefined,
      subject: '',
      timeEnd: '',
      timeStart: '',
      endTime: '',
      startTime: '',
      type: TaskMeetingType.DAILY_STANDUP
    };
    return;
  }

  const { type, date, time, moderator, participants } = meetingData;
  const [startTime = '', endTime = ''] = (time || '').split('~');

  // Add moderator to members list if not already present
  if (!projectMembers.value.find(member => member.id === moderator.id)) {
    projectMembers.value.push(moderator);
  }

  meetingFormState.value = {
    ...meetingData,
    type: type?.value || type,
    date: dayjs(date),
    timeStart: dayjs(startTime),
    timeEnd: dayjs(endTime),
    startTime: dayjs(startTime),
    endTime: dayjs(endTime),
    moderator: moderator?.id,
    participants: (participants || []).map(participant => {
      if (participant.fullName && participant.id && !projectMembers.value.find(member => member.id === participant.id)) {
        projectMembers.value.push({
          ...participant
        });
      }
      return participant?.id;
    }).filter(Boolean) as string[]
  };
};

/**
 * Handles form submission (save operation)
 */
const handleFormSubmit = async () => {
  formRef.value.validate().then(async () => {
    if (!isEditMode.value) {
      await handleMeetingCreation();
    } else {
      await handleMeetingUpdate();
    }
    refreshMeetingList();
  });
};

/**
 * Handles form cancellation
 */
const handleFormCancel = () => {
  if (props.data?._id) {
    deleteTabPane([props.data._id]);
  }
};

/**
 * Component mounted lifecycle hook
 * Initializes enums, loads members, and sets up data watcher
 */
onMounted(async () => {
  loadEnumOptions();
  await loadProjectMembers();

  watch(() => props.data, async (newValue, oldValue) => {
    const currentMeetingId = newValue?.id;
    if (!currentMeetingId) {
      return;
    }

    const previousMeetingId = oldValue?.id;
    if (currentMeetingId === previousMeetingId) {
      return;
    }

    await loadMeetingData(currentMeetingId);
  }, { immediate: true });
});
</script>
<template>
  <Spin :spinning="isLoading" class="h-full text-3 leading-5 px-5 py-5 overflow-auto">
    <div class="flex items-center space-x-2.5 mb-5">
      <Button
        type="primary"
        size="small"
        class="flex items-center space-x-1"
        @click="handleFormSubmit">
        <Icon icon="icon-dangqianxuanzhong" class="text-3.5" />
        <span>{{ t('actions.save') }}</span>
      </Button>
      <Button
        size="small"
        class="flex items-center space-x-1"
        @click="handleFormCancel">
        <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-zhongzhi2" />
        <span>{{ t('actions.cancel') }}</span>
      </Button>
    </div>

    <Form
      ref="formRef"
      :model="meetingFormState"
      size="small"
      :labelCol="{ style: { width: '112px' } }"
      class="max-w-242.5"
      layout="horizontal">
      <FormItem
        required
        name="subject"
        :label="t('common.subject')">
        <Input
          v-model:value="meetingFormState.subject"
          :maxlength="200"
          :placeholder="t('meeting.placeholder.inputSubject')" />
      </FormItem>

      <div class="flex space-x-2">
        <FormItem
          required
          :label="t('common.type')"
          name="type"
          class="flex-1 min-w-0">
          <div class="flex items-center space-x-1">
            <SelectEnum
              v-model:value="meetingFormState.type"
              :lazy="false"
              class="flex-1 min-w-0"
              enumKey="TaskMeetingType" />
            <Popover placement="right" :content="t('meeting.messages.meetingTypeTip')">
              <Icon icon="icon-tishi1" class="text-tips text-3.5 cursor-pointer" />
            </Popover>
          </div>
        </FormItem>

        <FormItem
          :label="t('common.sprint')"
          class="flex-1 min-w-0"
          name="sprintId">
          <div class="flex items-center space-x-1">
            <Select
              v-model:value="meetingFormState.sprintId"
              :placeholder="t('common.placeholders.selectSprint')"
              :fieldNames="{
                value: 'id',
                label: 'name'
              }"
              :action="`${TESTER}/task/sprint?projectId=${props.projectId}&fullTextSearch=true`">
            </Select>
          </div>
        </FormItem>
      </div>

      <div class="flex space-x-2">
        <FormItem
          required
          :label="t('meeting.columns.date')"
          class="flex-1 min-w-0"
          name="date">
          <div class="flex items-center space-x-1">
            <DatePicker
              v-model:value="meetingFormState.date"
              format="YYYY-MM-DD"
              showToday
              class="flex-1 min-w-0" />
            <Icon icon="" class="text-tips text-3.5 cursor-pointer" />
          </div>
        </FormItem>

        <FormItem
          :label="t('meeting.columns.time')"
          class="flex-1 min-w-0"
          name="time"
          :rules="{validator: validateTimeRequired, message: t('meeting.messages.timeRequired'), required: true}">
          <div class="w-full flex items-center space-x-1">
            <DatePicker
              v-model:value="meetingFormState.timeStart"
              mode="time"
              picker="time"
              :placeholder="t('common.startTime')"
              class="flex-1" />
            <span>-</span>
            <DatePicker
              v-model:value="meetingFormState.timeEnd"
              mode="time"
              picker="time"
              :placeholder="t('common.endTime')"
              class="flex-1" />
          </div>
        </FormItem>
      </div>

      <div class="flex space-x-2">
        <FormItem :label="t('meeting.columns.location')" class="flex-1 min-w-0">
          <div class="flex items-center space-x-1">
            <Input
              v-model:value="meetingFormState.location"
              :placeholder="t('meeting.placeholder.inputLocation')"
              :maxlength="100"
              class="flex-1 min-w-0" />
            <Icon icon="" class="text-tips text-3.5 cursor-pointer" />
          </div>
        </FormItem>

        <FormItem
          required
          :label="t('meeting.columns.moderator')"
          class="flex-1 min-w-0"
          name="moderator">
          <div class="flex items-center space-x-1">
            <Select
              v-model:value="meetingFormState.moderator"
              :placeholder="t('meeting.placeholder.selectModerator')"
              :options="projectMembers"
              :fieldNames="selectFieldNames"
              class="flex-1 min-w-0" />
          </div>
        </FormItem>
      </div>

      <FormItem
        required
        :label="t('meeting.columns.participants')"
        class="min-w-0"
        name="participants">
        <div class="flex items-center space-x-1">
          <Select
            v-model:value="meetingFormState.participants"
            :placeholder="t('meeting.placeholder.selectParticipants')"
            :maxTags="200"
            :options="projectMembers"
            :fieldNames="selectFieldNames"
            mode="multiple"
            class="flex-1 min-w-0" />
        </div>
      </FormItem>

      <FormItem
        :label="t('meeting.columns.content')"
        class="flex-1 !mb-5"
        name="content"
        :rules="{required: true, validator: validateContentRequired}">
        <RichEditor
          ref="contentRichEditorRef"
          v-model:value="meetingFormState.content"
          class="review-description" />
      </FormItem>
    </Form>
  </Spin>
</template>

<style scoped>
::deep(.ant-form-item-label>label::after) {
  margin-right: 10px;
}

/* Force bold labels */
:deep(.ant-form-item-label) > label {
  font-weight: 600 !important;
}

.ant-tabs-small > :deep(.ant-tabs-nav) .ant-tabs-tab {
  padding-top: 0;
}
</style>
