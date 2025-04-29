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
    moderator: { fullName: string, id: string } | string;
    participants: { fullName: string, id: string }[];
    startTime: Dayjs;
    type: 'DAILY_STANDUP' | 'PLANNING' | 'RETROSPECTIVE' | 'REVIEW' | 'OTHER';
}
