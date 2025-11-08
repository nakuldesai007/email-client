/// <reference types="vitest" />

import React from 'react';
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { render, screen, waitFor, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { EmailList } from '../EmailList';
import type { ListInboxResponse, ListTrashResponse, MoveToTrashResponse } from '../../generated/email_service';
import { fetchInbox, moveToTrash } from '../../services/emailClient';

vi.mock('../../services/emailClient', () => ({
  fetchInbox: vi.fn(),
  moveToTrash: vi.fn(),
}));

const mockFetchInbox = vi.mocked(fetchInbox);
const mockMoveToTrash = vi.mocked(moveToTrash);

function renderWithClient(queryClient: QueryClient, ui: React.ReactNode) {
  return render(<QueryClientProvider client={queryClient}>{ui}</QueryClientProvider>);
}

describe('EmailList trash behaviour', () => {
  const originalConfirm = window.confirm;
  const originalAlert = window.alert;
  let consoleErrorSpy: ReturnType<typeof vi.spyOn>;

  beforeEach(() => {
    window.confirm = vi.fn(() => true);
    window.alert = vi.fn();
    consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
  });

  afterEach(() => {
    cleanup();
    vi.clearAllMocks();
    window.confirm = originalConfirm;
    window.alert = originalAlert;
    consoleErrorSpy.mockRestore();
  });

  it('moves the email to trash immediately after confirmation', async () => {
    const initialInbox: ListInboxResponse = {
      emails: [
        {
          id: 'email-1',
          from: 'Sender <sender@example.com>',
          subject: 'Welcome!',
          unread: true,
        },
      ],
    };
    mockFetchInbox.mockResolvedValueOnce(initialInbox);
    mockFetchInbox.mockResolvedValue({ emails: [] });
    mockMoveToTrash.mockResolvedValueOnce({ success: true, newId: 'email-1-trash' } as MoveToTrashResponse);

    const queryClient = new QueryClient({
      defaultOptions: {
        queries: {
          retry: false,
        },
      },
    });

    renderWithClient(queryClient, <EmailList onEmailClick={vi.fn()} />);

    await screen.findByText('Welcome!');
    const deleteButton = await screen.findByTitle('Delete');

    await userEvent.click(deleteButton);

    await waitFor(() => {
      expect(mockMoveToTrash).toHaveBeenCalledWith('email-1');
    });

    await waitFor(() => {
      const inboxData = queryClient.getQueryData<ListInboxResponse>(['inbox']);
      expect(inboxData?.emails ?? []).toHaveLength(0);
    });

    const trashData = queryClient.getQueryData<ListTrashResponse>(['trash']);
    expect(trashData?.emails[0]).toMatchObject({
      id: 'email-1-trash',
      subject: 'Welcome!',
    });
  });

  it('shows an alert and keeps the email when the server call fails', async () => {
    const initialInbox: ListInboxResponse = {
      emails: [
        {
          id: 'email-2',
          from: 'Sender <sender@example.com>',
          subject: 'Second Mail',
          unread: true,
        },
      ],
    };
    mockFetchInbox.mockResolvedValueOnce(initialInbox);
    mockFetchInbox.mockResolvedValue(initialInbox);
    mockMoveToTrash.mockRejectedValueOnce(new Error('network error'));

    const queryClient = new QueryClient({
      defaultOptions: {
        queries: {
          retry: false,
        },
      },
    });

    renderWithClient(queryClient, <EmailList onEmailClick={vi.fn()} />);

    await screen.findByText('Second Mail');
    const deleteButton = await screen.findByTitle('Delete');

    await userEvent.click(deleteButton);

    await waitFor(() => {
      expect(mockMoveToTrash).toHaveBeenCalledWith('email-2');
    });

    await waitFor(() => {
      expect(window.alert).toHaveBeenCalled();
    });
    expect(consoleErrorSpy).toHaveBeenCalled();

    const inboxData = queryClient.getQueryData<ListInboxResponse>(['inbox']);
    expect(inboxData?.emails[0]?.id).toBe('email-2');

    const trashData = queryClient.getQueryData<ListTrashResponse>(['trash']);
    expect(trashData?.emails ?? []).toHaveLength(0);
  });
});


