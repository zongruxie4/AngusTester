export type FormState = {
  projectId: string;
  name: string;
  description: string;
  parameters: {
    name: string;
    value: string;
  }[];
  id?: string;
}
