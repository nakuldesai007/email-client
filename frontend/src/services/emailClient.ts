import { GrpcWebFetchTransport } from '@protobuf-ts/grpcweb-transport';
import { EmailServiceClient } from '../generated/email_service.client';
import type {
  ListInboxRequest,
  ListInboxResponse,
  ListSentRequest,
  ListSentResponse,
  ListTrashRequest,
  ListTrashResponse,
  SendEmailRequestMessage,
  SendEmailResponse,
  GetEmailRequest,
  GetEmailResponse,
  DeleteEmailRequest,
  DeleteEmailResponse,
  MoveToTrashRequest,
  MoveToTrashResponse,
  PermanentlyDeleteRequest,
  PermanentlyDeleteResponse,
  RestoreEmailRequest,
  RestoreEmailResponse,
} from '../generated/email_service';

// Create transport that points to Envoy proxy (defaults to port 8080)
const transport = new GrpcWebFetchTransport({
  baseUrl: 'http://localhost:8080',
  timeout: 30000, // 30 second timeout for slower operations like fetching email content
});

// Create the gRPC-Web client
export const emailClient = new EmailServiceClient(transport);

// Typed wrapper functions for easier usage
export async function fetchInbox(): Promise<ListInboxResponse> {
  const request: ListInboxRequest = {};
  const { response } = await emailClient.listInbox(request);
  return response;
}

export async function fetchSentEmails(): Promise<ListSentResponse> {
  const request: ListSentRequest = {};
  const { response } = await emailClient.listSent(request);
  return response;
}

export async function fetchTrash(): Promise<ListTrashResponse> {
  const request: ListTrashRequest = {};
  const { response } = await emailClient.listTrash(request);
  return response;
}

export async function sendEmail(params: {
  to: string;
  subject: string;
  body: string;
  cc?: string[];
  bcc?: string[];
  attachments?: string[];
}): Promise<SendEmailResponse> {
  const request: SendEmailRequestMessage = {
    to: params.to,
    subject: params.subject,
    body: params.body,
    cc: params.cc ?? [],
    bcc: params.bcc ?? [],
    attachments: params.attachments ?? [],
  };
  
  const { response } = await emailClient.sendEmail(request);
  return response;
}

export async function fetchEmail(id: string): Promise<GetEmailResponse> {
  const request: GetEmailRequest = { id };
  const { response } = await emailClient.getEmail(request);
  return response;
}

export async function deleteEmail(id: string): Promise<DeleteEmailResponse> {
  const request: DeleteEmailRequest = { id };
  const { response } = await emailClient.deleteEmail(request);
  return response;
}

export async function moveToTrash(id: string): Promise<MoveToTrashResponse> {
  const request: MoveToTrashRequest = { id };
  const { response } = await emailClient.moveToTrash(request);
  if (!response.success) {
    throw new Error('Failed to move email to trash on the server.');
  }
  return response;
}

export async function restoreEmail(id: string): Promise<RestoreEmailResponse> {
  const request: RestoreEmailRequest = { id };
  const { response } = await emailClient.restoreEmail(request);
  if (!response.success) {
    throw new Error('Failed to restore email from trash on the server.');
  }
  return response;
}

export async function permanentlyDelete(id: string): Promise<PermanentlyDeleteResponse> {
  const request: PermanentlyDeleteRequest = { id };
  const { response } = await emailClient.permanentlyDelete(request);
  return response;
}

