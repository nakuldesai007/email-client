/// <reference types="vitest" />

import React from 'react';
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { render, screen, waitFor, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { EmailList } from '../EmailList';
import { TrashEmails } from '../TrashEmails';
import type { ListInboxResponse, ListTrashResponse, MoveToTrashResponse } from '../../generated/email_service';
import { fetchInbox, fetchTrash, moveToTrash } from '../../services/emailClient';

vi.mock('../../services/emailClient', () => ({
  fetchInbox: vi.fn(),
  fetchTrash: vi.fn(),
  moveToTrash: vi.fn(),
}));

const mockFetchInbox = vi.mocked(fetchInbox);
const mockFetchTrash = vi.mocked(fetchTrash);
const mockMoveToTrash = vi.mocked(moveToTrash);

function renderSuite(ui: React.ReactNode, client: QueryClient) {
  return render(<QueryClientProvider client={client}>{ui}</QueryClientProvider>);
}

describe('Inbox to Trash integration', () => {
  const originalConfirm = window.confirm;

  beforeEach(() => {
    window.confirm = vi.fn(() => true);
  });

  afterEach(() => {
    cleanup();
    vi.clearAllMocks();
    window.confirm = originalConfirm;
  });

  it('removes the email from inbox and renders it in trash after deletion', async () => {
    const inboxResponse: ListInboxResponse = {
      emails: [
        {
          id: 'email-10',
          from: 'Announcements <announce@example.com>',
          subject: 'System Maintenance',
          unread: true,
        },
      ],
    };

    const trashEmpty: ListTrashResponse = { emails: [] };
    const trashWithEmail: ListTrashResponse = {
      emails: [
        {
          id: 'email-10-trash',
          from: 'Announcements <announce@example.com>',
          subject: 'System Maintenance',
          unread: false,
        },
      ],
    };

    mockFetchInbox.mockResolvedValueOnce(inboxResponse);
    mockFetchInbox.mockResolvedValueOnce({ emails: [] });

    mockFetchTrash.mockResolvedValueOnce(trashEmpty);
    mockFetchTrash.mockResolvedValueOnce(trashWithEmail);

    mockMoveToTrash.mockResolvedValueOnce({
      success: true,
      newId: 'email-10-trash',
    } as MoveToTrashResponse);

    const queryClient = new QueryClient({
      defaultOptions: {
        queries: {
          retry: false,
        },
      },
    });

    renderSuite(
      <div className="flex">
        <div>
          <EmailList onEmailClick={vi.fn()} />
        </div>
        <div>
          <TrashEmails onEmailClick={vi.fn()} />
        </div>
      </div>,
      queryClient
    );

    await screen.findByText('System Maintenance');

    const deleteButton = await screen.findByTitle('Delete');
    await userEvent.click(deleteButton);

    await waitFor(() => {
      expect(mockMoveToTrash).toHaveBeenCalledWith('email-10');
    });

    await waitFor(() => {
      const inboxData = queryClient.getQueryData<ListInboxResponse>(['inbox']);
      expect(inboxData?.emails ?? []).toHaveLength(0);
    });

    await waitFor(() => {
      expect(mockFetchTrash).toHaveBeenCalledTimes(2);
    });

    await screen.findByText('System Maintenance', { selector: 'p.text-xs' });
    const trashList = await screen.findAllByText('System Maintenance');
    expect(trashList.length).toBeGreaterThanOrEqual(1);
  });
});


