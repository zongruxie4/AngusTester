import type { Dayjs } from 'dayjs';

export type FormState = {
    id?: string;
    content: string;
    date: Dayjs|string;
    endTime: Dayjs;
    location: string;
    timeEnd: string;
    timeStart: string;
    subject: string;
    moderator: { fullname: string, id: string } | string;
    participants: { fullname: string, id: string }[];
    startTime: Dayjs;
    type: 'DAILY_STANDUP' | 'PLANNING' | 'RETROSPECTIVE' | 'REVIEW' | 'OTHER';
}
