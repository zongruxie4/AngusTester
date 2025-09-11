import type { Dayjs } from 'dayjs';
import { EnumMessage } from '@xcan-angus/infra';
import { TaskMeetingType } from '@/enums/enums';

export type MeetingInfo = {
    id: string;
    content: string;
    date: Dayjs;
    endTime: string;
    location: string;
    moderator: { fullName: string, id: string; avatar?: string };
    participants: { fullName: string, id: string; avatar?: string }[];
    startTime: string;
    subject: string;
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
  endTime: Dayjs | string;
  location: string;
  timeEnd: Dayjs | string;
  timeStart: Dayjs | string;
  subject: string;
  moderator: string | undefined;
  participants: string[];
  startTime: Dayjs | string;
  type: TaskMeetingType;
  projectId?: string;
  sprintId?: string;
}
