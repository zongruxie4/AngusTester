import type { Dayjs } from 'dayjs';

export type MeetingInfo = {
    id: string;
    content: string;
    date: Dayjs;
    endTime: Dayjs;
    location: string;
    moderator: { fullName: string, id: string };
    participants: { fullName: string, id: string }[];
    startTime: Dayjs;
    type: 'DAILY_STANDUP' | 'PLANNING' | 'RETROSPECTIVE' | 'REVIEW' | 'OTHER';
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
  type: 'DAILY_STANDUP' | 'PLANNING' | 'RETROSPECTIVE' | 'REVIEW' | 'OTHER';
}
