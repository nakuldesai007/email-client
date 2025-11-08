import React from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { fetchEmail, moveToTrash, permanentlyDelete } from '../services/emailClient';
import type { MoveToTrashResponse, ListInboxResponse, ListTrashResponse, EmailPreviewMessage, GetEmailResponse } from '../generated/email_service';

export interface ReplyForwardPayload {
  to?: string;
  subject: string;
  body: string;
  cc?: string[];
  bcc?: string[];
}

interface EmailDetailProps {
  emailId: string;
  onClose: () => void;
  compact?: boolean;
  currentView: 'inbox' | 'sent' | 'drafts' | 'trash';
  onReply?: (payload: ReplyForwardPayload) => void;
  onForward?: (payload: ReplyForwardPayload) => void;
}

const stripHtml = (html: string) => {
  const withLineBreaks = html
    .replace(/<br\s*\/?>/gi, '\n')
    .replace(/<\/p>/gi, '\n\n')
    .replace(/<\/div>/gi, '\n')
    .replace(/<\/li>/gi, '\n')
    .replace(/<\/h[1-6]>/gi, '\n');
  return withLineBreaks.replace(/<[^>]*>/g, '').replace(/\r?\n\s*\n+/g, '\n\n').trim();
};

const extractEmailAddress = (value: string) => {
  const match = value.match(/<([^>]+)>/);
  if (match && match[1]) {
    return match[1].trim();
  }
  return value.trim();
};

const quoteText = (text: string) =>
  text
    .split(/\r?\n/)
    .map((line) => `> ${line}`)
    .join('\n');

const formatQuoteDate = (timestamp?: { seconds: string | bigint; nanos: number }) => {
  if (!timestamp) return '';
  const seconds =
    typeof timestamp.seconds === 'bigint'
      ? Number(timestamp.seconds)
      : parseInt(timestamp.seconds);
  const date = new Date(seconds * 1000);
  return date.toLocaleString('en-US', {
    weekday: 'short',
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  });
};

export function EmailDetail({ emailId, onClose, compact = false, currentView, onReply, onForward }: EmailDetailProps) {
  const queryClient = useQueryClient();
  
  const { data, isLoading, error } = useQuery<GetEmailResponse>({
    queryKey: ['email', emailId],
    queryFn: () => fetchEmail(emailId),
    enabled: !!emailId,
  });
  
  React.useEffect(() => {
    const detail = data?.email;
    if (!detail) {
      return;
    }
    const inboxSnapshot = queryClient.getQueryData<ListInboxResponse>(['inbox']);
    if (!inboxSnapshot) {
      return;
    }
    queryClient.setQueryData(['inbox'], (previous: ListInboxResponse | undefined) => {
      const source = previous ?? inboxSnapshot;
      const exists = source.emails.some(entry => entry.id === detail.id);
      if (!exists) {
        return source;
      }
      return {
        ...source,
        emails: source.emails.map(entry =>
          entry.id === detail.id ? { ...entry, unread: false } : entry
        ),
      };
    });
  }, [data, queryClient]);

  const deleteMutation = useMutation({
    mutationFn: async (id: string) => {
      if (currentView === 'trash') {
        const response = await permanentlyDelete(id);
        return { success: response.success, newId: id };
      }
      const response: MoveToTrashResponse = await moveToTrash(id);
      return response;
    },
    onSuccess: async (result) => {
      const detail = data?.email;
      const originalId = emailId;
      const newId = result?.newId && result.newId.length > 0 ? result.newId : originalId;

      if (currentView === 'trash') {
        queryClient.setQueryData(['trash'], (previous: ListTrashResponse | undefined) => {
          if (!previous) {
            return previous;
          }
          return {
            ...previous,
            emails: previous.emails.filter(entry => entry.id !== newId && entry.id !== originalId),
          };
        });
        await queryClient.invalidateQueries({ queryKey: ['trash'] });
      } else {
        const inboxSnapshot = queryClient.getQueryData<ListInboxResponse>(['inbox']);
        if (inboxSnapshot) {
          queryClient.setQueryData(['inbox'], (previous: ListInboxResponse | undefined) => {
            const source = previous ?? inboxSnapshot;
            return {
              ...source,
              emails: source.emails.filter(entry => entry.id !== originalId && entry.id !== newId),
            };
          });
        }

        if (detail) {
          const preview: EmailPreviewMessage = {
            id: newId,
            from: detail.from,
            subject: detail.subject,
            receivedAt: detail.receivedAt,
            unread: false,
          };
          queryClient.setQueryData(['trash'], (previous: ListTrashResponse | undefined) => {
            if (!previous) {
              return { emails: [preview] };
            }
            const filtered = previous.emails.filter(entry => entry.id !== originalId && entry.id !== newId);
            return {
              ...previous,
              emails: [preview, ...filtered],
            };
          });
        } else {
          await queryClient.invalidateQueries({ queryKey: ['trash'] });
        }

        await queryClient.invalidateQueries({ queryKey: ['inbox'] });
      }

      queryClient.invalidateQueries({ queryKey: ['sent'] });
      queryClient.invalidateQueries({ queryKey: ['email', emailId] });
      onClose();
    },
    onError: (error) => {
      console.error('Failed to move email to trash', error);
      window.alert('Unable to move this email to trash. Please try again.');
    },
  });

  const formatDate = (timestamp?: { seconds: string | bigint; nanos: number }) => {
    if (!timestamp) return '';
    const seconds = typeof timestamp.seconds === 'bigint' 
      ? Number(timestamp.seconds) 
      : parseInt(timestamp.seconds);
    const date = new Date(seconds * 1000);
    return date.toLocaleString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: 'numeric',
      minute: '2-digit',
    });
  };

  if (isLoading) {
    return (
      <div className="fixed inset-0 bg-white z-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin h-12 w-12 border-4 border-blue-500 border-t-transparent rounded-full mx-auto mb-4"></div>
          <p className="text-gray-600">Loading email content...</p>
          <p className="text-sm text-gray-400 mt-2">Fetching from server</p>
        </div>
      </div>
    );
  }

  if (error || !data?.email) {
    return (
      <div className="fixed inset-0 bg-white z-50 flex items-center justify-center p-4">
        <div className="max-w-md w-full bg-red-50 border border-red-200 rounded-lg p-6">
          <p className="text-red-800 font-semibold mb-2">Failed to load email</p>
          <p className="text-red-600 text-sm mb-4">{String(error)}</p>
          <button
            onClick={onClose}
            className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 transition-colors"
          >
            Go Back
          </button>
        </div>
      </div>
    );
  }

  const email = data.email;
  const isHtml = email.body.trim().startsWith('<');

  const getSenderName = (from: string) => {
    const match = from.match(/^"?([^"<]+)"?\s*</);
    return match ? match[1].trim() : from.split('<')[0].trim() || from;
  };

  const senderInitial = getSenderName(email.from)[0]?.toUpperCase() || '?';

  const handleDelete = async () => {
    const targetId = data?.email?.id ?? emailId;
    const confirmationMessage =
      currentView === 'trash'
        ? 'Are you sure you want to permanently delete this email? This action cannot be undone.'
        : 'Move this email to trash?';
    if (window.confirm(confirmationMessage)) {
      deleteMutation.mutate(targetId);
    }
  };

  const buildReplyPayload = (): ReplyForwardPayload => {
    const replySubject = email.subject?.toLowerCase().startsWith('re:')
      ? email.subject || 'Re: (no subject)'
      : `Re: ${email.subject || '(no subject)'}`;
    const plainBody = isHtml ? stripHtml(email.body) : email.body;
    const quotedBody = quoteText(plainBody);
    const formattedDate = formatQuoteDate(email.receivedAt);
    const replyBody = `\n\nOn ${formattedDate}, ${email.from} wrote:\n${quotedBody}`;

    return {
      to: extractEmailAddress(email.from),
      subject: replySubject,
      body: replyBody,
    };
  };

  const buildForwardPayload = (): ReplyForwardPayload => {
    const forwardSubject = email.subject?.toLowerCase().startsWith('fwd:')
      ? email.subject || 'Fwd: (no subject)'
      : `Fwd: ${email.subject || '(no subject)'}`;
    const plainBody = isHtml ? stripHtml(email.body) : email.body;
    const formattedDate = formatQuoteDate(email.receivedAt);
    const headers = [
      '--- Forwarded message ---',
      `From: ${email.from}`,
      `Date: ${formattedDate}`,
      `Subject: ${email.subject || '(no subject)'}`,
    ];
    if (email.to && email.to.length > 0) {
      headers.push(`To: ${email.to.join(', ')}`);
    }
    if (email.cc && email.cc.length > 0) {
      headers.push(`Cc: ${email.cc.join(', ')}`);
    }
    const forwardBody = `\n\n${headers.join('\n')}\n\n${plainBody}`;

    return {
      subject: forwardSubject,
      body: forwardBody,
    };
  };

  const handleReplyClick = () => {
    if (onReply) {
      onReply(buildReplyPayload());
    }
  };

  const handleForwardClick = () => {
    if (onForward) {
      onForward(buildForwardPayload());
    }
  };

  // Compact mode for reading pane
  if (compact) {
    return (
      <div className="h-full flex flex-col bg-transparent min-h-0">
        {/* Subject & Actions */}
        <div className="px-6 py-4 border-b border-slate-200/60 flex-shrink-0">
          <div className="flex items-start justify-between">
            <h2 className="text-xl font-bold text-gray-900 flex-1">{email.subject || '(no subject)'}</h2>
            <div className="flex items-center gap-2 ml-4">
              <button
                className="p-2 hover:bg-slate-200/50 rounded-lg transition-colors"
                title="Reply"
                onClick={handleReplyClick}
              >
                <svg className="w-4 h-4 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 10h10a8 8 0 018 8v2M3 10l6 6m-6-6l6-6" />
                </svg>
              </button>
              <button
                className="p-2 hover:bg-slate-200/50 rounded-lg transition-colors"
                title="Forward"
                onClick={handleForwardClick}
              >
                <svg className="w-4 h-4 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 8l4 4m0 0l-4 4m4-4H7m8-4V4m0 4v8" />
                </svg>
              </button>
              <button 
                onClick={handleDelete}
                disabled={deleteMutation.isPending}
                className="p-2 hover:bg-red-100 text-red-600 rounded-lg transition-colors disabled:opacity-50"
                title="Delete"
              >
                {deleteMutation.isPending ? (
                  <svg className="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                ) : (
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                  </svg>
                )}
              </button>
            </div>
          </div>
        </div>

        {/* Sender/Recipient Info */}
        <div className="px-6 py-3 border-b border-slate-200/60 bg-slate-50/50 flex-shrink-0">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center font-semibold text-white text-sm">
              {senderInitial}
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-semibold text-gray-900">{getSenderName(email.from)}</p>
              <div className="flex items-center gap-4 text-xs text-gray-500 mt-0.5">
                <span>{email.from}</span>
                <span>•</span>
                <span>{formatDate(email.receivedAt)}</span>
              </div>
              {email.to && email.to.length > 0 && (
                <div className="flex items-center gap-1.5 text-xs text-gray-600 mt-1.5">
                  <span className="font-medium text-gray-500">To:</span>
                  <span className="truncate">{email.to.join(', ')}</span>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Body */}
        <div className="flex-1 overflow-y-auto min-h-0 px-6 py-4">
          {isHtml ? (
            <div 
              className="prose prose-sm prose-slate max-w-none"
              dangerouslySetInnerHTML={{ __html: email.body }}
            />
          ) : (
            <div className="whitespace-pre-wrap text-sm text-gray-800 leading-relaxed">
              {email.body}
            </div>
          )}
        </div>
      </div>
    );
  }

  // Full-screen mode
  return (
    <div className="fixed inset-0 bg-gradient-to-br from-slate-50 via-blue-50/30 to-purple-50/20 z-50 flex flex-col overflow-hidden">
      {/* Enhanced Header */}
      <div className="bg-white/80 backdrop-blur-xl border-b border-gray-200/60 sticky top-0 z-10 shadow-lg">
        <div className="max-w-6xl mx-auto px-6 py-4">
          <div className="flex items-center justify-between">
            <button
              onClick={onClose}
              className="flex items-center gap-2 px-5 py-2.5 text-gray-700 hover:bg-gradient-to-r hover:from-slate-100 hover:to-slate-50 rounded-xl transition-all duration-200 font-semibold shadow-sm hover:shadow-md border border-transparent hover:border-slate-200"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M15 19l-7-7 7-7" />
              </svg>
              Back to List
            </button>
            <div className="flex items-center gap-3">
              <button
                className="p-2.5 hover:bg-indigo-50 text-indigo-600 rounded-xl transition-all duration-200 shadow-sm hover:shadow-md border border-transparent hover:border-indigo-200"
                title="Reply"
                onClick={handleReplyClick}
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 10h10a8 8 0 018 8v2M3 10l6 6m-6-6l6-6" />
                </svg>
              </button>
              <button
                className="p-2.5 hover:bg-blue-50 text-blue-600 rounded-xl transition-all duration-200 shadow-sm hover:shadow-md border border-transparent hover:border-blue-200"
                title="Forward"
                onClick={handleForwardClick}
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 8l4 4m0 0l-4 4m4-4H7m8-4V4m0 4v8" />
                </svg>
              </button>
              <button 
                onClick={handleDelete}
                disabled={deleteMutation.isPending}
                className="p-2.5 hover:bg-red-50 text-red-600 rounded-xl transition-all duration-200 disabled:opacity-50 shadow-sm hover:shadow-md border border-transparent hover:border-red-200"
                title="Delete"
              >
                {deleteMutation.isPending ? (
                  <svg className="w-5 h-5 animate-spin" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                ) : (
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                  </svg>
                )}
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Email Content - Scrollable */}
      <div className="flex-1 overflow-y-auto">
        <div className="max-w-6xl mx-auto px-6 py-8">
          <div className="bg-white rounded-xl shadow-xl border border-gray-200/60 overflow-hidden">
          {/* Subject & Status */}
          <div className="px-8 py-6 border-b border-gray-200">
            <div className="flex items-start justify-between">
              <div className="flex-1">
                <h1 className="text-3xl font-bold text-gray-900 mb-2">{email.subject || '(no subject)'}</h1>
                {email.unread && (
                  <span className="inline-flex items-center gap-1.5 px-3 py-1 bg-indigo-100 text-indigo-700 text-xs font-semibold rounded-full">
                    <span className="w-2 h-2 bg-indigo-500 rounded-full animate-pulse"></span>
                    Unread
                  </span>
                )}
              </div>
            </div>
          </div>

          {/* Sender Info */}
          <div className="px-8 py-6 border-b border-gray-200 bg-slate-50">
            <div className="flex items-start gap-4">
              <div className="w-12 h-12 rounded-full bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center font-semibold text-white text-lg flex-shrink-0">
                {senderInitial}
              </div>
              <div className="flex-1 min-w-0">
                <div className="flex items-center justify-between mb-2">
                  <div>
                    <p className="text-base font-semibold text-gray-900">{getSenderName(email.from)}</p>
                    <p className="text-sm text-gray-500">{email.from}</p>
                  </div>
                  <p className="text-sm text-gray-500">{formatDate(email.receivedAt)}</p>
                </div>
                
                <div className="space-y-1.5 mt-3">
                  {email.to && email.to.length > 0 && (
                    <div className="flex items-start gap-2">
                      <span className="text-xs font-medium text-gray-500 uppercase tracking-wide min-w-[60px]">To:</span>
                      <span className="text-sm text-gray-700">{email.to.join(', ')}</span>
                    </div>
                  )}
                  {email.cc && email.cc.length > 0 && (
                    <div className="flex items-start gap-2">
                      <span className="text-xs font-medium text-gray-500 uppercase tracking-wide min-w-[60px]">Cc:</span>
                      <span className="text-sm text-gray-700">{email.cc.join(', ')}</span>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>

            {/* Body */}
            <div className="px-8 py-8">
              {isHtml ? (
                <div 
                  className="prose prose-slate max-w-none prose-headings:text-gray-900 prose-a:text-indigo-600 hover:prose-a:text-indigo-700"
                  dangerouslySetInnerHTML={{ __html: email.body }}
                />
              ) : (
                <div className="whitespace-pre-wrap text-base text-gray-800 leading-relaxed">
                  {email.body}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

