import type { Dayjs } from 'dayjs';
import { EnumMessage } from '@xcan-angus/infra';
import { TaskMeetingType } from '@/enums/enums';

export type MeetingInfo = {
  id: string;
  date: Dayjs;
  location: string;
  moderator: { fullName: string, id: string; avatar?: string };
  participants: { fullName: string, id: string; avatar?: string }[];
  subject: string;
  content: string;
  startTime: string;
  endTime: string;
  type: EnumMessage<TaskMeetingType>;
  sprintName?: string;
  time?: string;
  participantNames?: string;
  moderatorName?: string;
  lastModifiedByName?: string;
  lastModifiedDate?: string;
  description?: string;
  otherInformation?: string;
}

export type EditFormState = {
  id?: string;
  content: string;
  date: Dayjs | string;
  location: string;
  timeStart: Dayjs | string;
  timeEnd: Dayjs | string;
  endTime: Dayjs | string;
  subject: string;
  moderator: string | undefined;
  participants: string[];
  startTime: Dayjs | string;
  type: TaskMeetingType;
  projectId?: string;
  sprintId?: string;
}
