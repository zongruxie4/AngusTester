import type { Dayjs } from 'dayjs';
import { EnumMessage } from '@xcan-angus/infra';
import { TaskMeetingType } from '@/enums/enums';

export type MeetingInfo = {
    id: string;
    content: string;
    date: Dayjs;
    endTime: string;
    location: string;
    moderator: { fullName: string, id: string };
    participants: { fullName: string, id: string }[];
    startTime: string;
    subject: string;
    type: EnumMessage<TaskMeetingType>;
    sprintName?: string;
    time?: string;
    participantNames?: string;
    moderatorName?: string;
}

export type EditFormState = {
  id?: string;
  content: string;
  date: Dayjs | string;
  endTime: Dayjs;
  location: string;
  timeEnd: string;
  timeStart: string;
  subject: string;
  moderator: { fullName: string, id: string } | string;
  participants: { fullName: string, id: string }[];
  startTime: Dayjs;
  type: TaskMeetingType;
}
