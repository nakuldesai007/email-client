import React from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { fetchTrash, moveToTrash, permanentlyDelete } from '../services/emailClient';
import type { EmailPreviewMessage, ListInboxResponse, ListSentResponse, ListTrashResponse, MoveToTrashResponse } from '../generated/email_service';

type SerializedEmailPreview = {
  id: string;
  from: string;
  subject: string;
  unread: boolean;
  receivedAt?: {
    seconds: string;
    nanos: number;
  };
};

type DragPayload = {
  id: string;
  origin: 'inbox' | 'trash' | 'sent';
  email?: SerializedEmailPreview;
};

function serializePreview(preview: EmailPreviewMessage): SerializedEmailPreview {
  return {
    id: preview.id,
    from: preview.from,
    subject: preview.subject,
    unread: preview.unread,
    receivedAt: preview.receivedAt
      ? {
          seconds: preview.receivedAt.seconds.toString(),
          nanos: preview.receivedAt.nanos,
        }
      : undefined,
  };
}

function deserializePreview(preview: SerializedEmailPreview): EmailPreviewMessage {
  return {
    id: preview.id,
    from: preview.from,
    subject: preview.subject,
    unread: preview.unread,
    receivedAt: preview.receivedAt
      ? {
          seconds: BigInt(preview.receivedAt.seconds),
          nanos: preview.receivedAt.nanos,
        }
      : undefined,
  };
}

interface TrashEmailsProps {
  onEmailClick: (emailId: string) => void;
  selectedEmailId?: string | null;
  openedEmails?: Set<string>;
}

export function TrashEmails({ onEmailClick, selectedEmailId, openedEmails }: TrashEmailsProps) {
  const queryClient = useQueryClient();
  const [isDragActive, setIsDragActive] = React.useState(false);

  const dragMoveMutation = useMutation({
    mutationFn: ({ id }: { id: string; origin: string; preview?: SerializedEmailPreview }) => moveToTrash(id),
    onSuccess: (data: MoveToTrashResponse, variables) => {
      const newId = data?.newId && data.newId.length > 0 ? data.newId : variables.id;
      const preview = variables.preview
        ? {
            ...deserializePreview(variables.preview),
            id: newId,
            unread: variables.preview.unread ?? false,
          }
        : undefined;

      if (variables.origin === 'inbox') {
        queryClient.setQueryData(['inbox'], (previous: ListInboxResponse | undefined) => {
          if (!previous) {
            return previous;
          }
          return {
            ...previous,
            emails: previous.emails.filter(entry => entry.id !== variables.id && entry.id !== newId),
          };
        });
      } else if (variables.origin === 'sent') {
        queryClient.setQueryData(['sent'], (previous: ListSentResponse | undefined) => {
          if (!previous) {
            return previous;
          }
          return {
            ...previous,
            emails: previous.emails.filter(entry => entry.id !== variables.id && entry.id !== newId),
          };
        });
      }

      if (preview) {
        queryClient.setQueryData(['trash'], (previous: ListTrashResponse | undefined) => {
          if (!previous) {
            return { emails: [preview] };
          }
          const filtered = previous.emails.filter(entry => entry.id !== variables.id && entry.id !== newId);
          return {
            ...previous,
            emails: [preview, ...filtered],
          };
        });
      }

      queryClient.invalidateQueries({ queryKey: ['trash'] });
      queryClient.invalidateQueries({ queryKey: ['inbox'] });
      queryClient.invalidateQueries({ queryKey: ['sent'] });
    },
    onError: (error) => {
      console.error('Failed to move email to trash via drag and drop', error);
      window.alert('Unable to move this email. Please try again.');
    },
  });

  const { data, isLoading, error } = useQuery({
    queryKey: ['trash'],
    queryFn: fetchTrash,
  });

  if (isLoading) {
    return (
      <div className="flex items-center justify-center p-8">
        <div className="animate-spin h-8 w-8 border-4 border-blue-500 border-t-transparent rounded-full"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-lg p-4 m-4">
        <p className="text-red-800 font-semibold">Failed to load trash</p>
        <p className="text-red-600 text-sm mt-1">{String(error)}</p>
      </div>
    );
  }

  const emails = data?.emails ?? [];

  if (emails.length === 0) {
    return (
      <div className="text-center p-8 text-gray-500">
        <svg className="w-16 h-16 mx-auto mb-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
        </svg>
        <p className="text-lg font-semibold">Trash is empty</p>
        <p className="text-sm mt-2">Deleted messages will appear here</p>
      </div>
    );
  }

  const handleDragOver = (event: React.DragEvent<HTMLDivElement>) => {
    if (!event.dataTransfer.types.includes('application/email-item')) {
      return;
    }
    event.preventDefault();
    event.dataTransfer.dropEffect = 'move';
    setIsDragActive(true);
  };

  const handleDragLeave = (event: React.DragEvent<HTMLDivElement>) => {
    if (event.currentTarget.contains(event.relatedTarget as Node)) {
      return;
    }
    setIsDragActive(false);
  };

  const handleDrop = (event: React.DragEvent<HTMLDivElement>) => {
    event.preventDefault();
    setIsDragActive(false);
    const payloadRaw = event.dataTransfer.getData('application/email-item');
    if (!payloadRaw) {
      return;
    }
    try {
      const payload = JSON.parse(payloadRaw) as DragPayload;
      if (!payload?.id || payload.origin === 'trash') {
        return;
      }
      dragMoveMutation.mutate({ id: payload.id, origin: payload.origin, preview: payload.email });
    } catch (parseError) {
      console.error('Failed to parse dragged email payload', parseError);
    }
  };

  return (
    <div
      className={`h-full overflow-auto transition-all duration-200 ${isDragActive ? 'ring-2 ring-rose-300 ring-offset-2 ring-offset-white/20' : ''}`}
      onDragOver={handleDragOver}
      onDragLeave={handleDragLeave}
      onDrop={handleDrop}
    >
      <div className="p-3 space-y-1.5">
        {emails.map((email: EmailPreviewMessage) => (
          <TrashEmailRow 
            key={email.id} 
            email={email} 
            onClick={() => onEmailClick(email.id)}
            isSelected={selectedEmailId === email.id}
            isOpened={openedEmails?.has(email.id) ?? false}
          />
        ))}
      </div>
    </div>
  );
}

function TrashEmailRow({ email, onClick, isSelected, isOpened }: { email: EmailPreviewMessage; onClick: () => void; isSelected: boolean; isOpened: boolean }) {
  const queryClient = useQueryClient();
  
  const deleteMutation = useMutation({
    mutationFn: (id: string) => permanentlyDelete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['trash'] });
    },
  });

  const handleDelete = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (window.confirm('Are you sure you want to permanently delete this email? This action cannot be undone.')) {
      deleteMutation.mutate(email.id);
    }
  };

  const formatDate = (timestamp?: { seconds: string | bigint; nanos: number }) => {
    if (!timestamp) return '';
    const seconds = typeof timestamp.seconds === 'bigint' 
      ? Number(timestamp.seconds) 
      : parseInt(timestamp.seconds);
    const date = new Date(seconds * 1000);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffHours = diffMs / (1000 * 60 * 60);

    if (diffHours < 24) {
      return date.toLocaleTimeString('en-US', {
        hour: 'numeric',
        minute: '2-digit',
      });
    } else if (diffHours < 24 * 7) {
      return date.toLocaleDateString('en-US', { weekday: 'short' });
    } else {
      return date.toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
      });
    }
  };

  const getSenderName = (from: string) => {
    const match = from.match(/^"?([^"<]+)"?\s*</);
    return match ? match[1].trim() : from.split('<')[0].trim() || from;
  };

  const handleDragStart = (event: React.DragEvent<HTMLDivElement>) => {
    event.dataTransfer.effectAllowed = 'move';
    event.dataTransfer.setData('text/plain', email.id);
    try {
      event.dataTransfer.setData('application/email-item', JSON.stringify({ id: email.id, origin: 'trash', email: serializePreview(email) }));
    } catch (error) {
      console.error('Unable to serialize email for drag operation', error);
    }
  };

  return (
    <div
      onClick={onClick}
      draggable
      onDragStart={handleDragStart}
      className={`group relative overflow-hidden rounded-xl cursor-pointer transition-all duration-300 ${
        isSelected
          ? 'bg-gradient-to-r from-indigo-100/90 to-purple-100/90 backdrop-blur-sm border-2 border-indigo-400/60 shadow-lg shadow-indigo-200/50 ring-2 ring-indigo-300/30'
          : isOpened
          ? 'bg-gradient-to-r from-red-50/80 to-pink-50/80 backdrop-blur-sm border border-red-200/50 shadow-sm hover:shadow-md hover:border-red-300/70 hover:scale-[1.01]'
          : 'bg-gradient-to-r from-red-50/60 to-pink-50/60 backdrop-blur-sm border border-red-200/40 shadow-sm hover:shadow-md hover:border-red-300/60 hover:scale-[1.01]'
      }`}
    >
      {/* Indicator bar */}
      {isSelected ? (
        <div className="absolute left-0 top-0 bottom-0 w-1 bg-gradient-to-b from-indigo-500 via-purple-600 to-indigo-700"></div>
      ) : (
        <div className="absolute left-0 top-0 bottom-0 w-1 bg-gradient-to-b from-red-400 via-pink-500 to-red-600"></div>
      )}

      <div className="flex items-center gap-3 px-4 py-2.5">
        {/* Avatar with glass effect */}
        <div className={`relative w-9 h-9 rounded-full flex items-center justify-center font-bold text-xs flex-shrink-0 backdrop-blur-sm ${
          isSelected
            ? 'bg-gradient-to-br from-indigo-500 to-purple-600 text-white shadow-md shadow-indigo-300/50'
            : 'bg-gradient-to-br from-red-400 to-pink-500 text-white shadow-md shadow-red-300/50'
        }`}>
          <span className="relative z-10">{getSenderName(email.from)[0]?.toUpperCase() || '?'}</span>
        </div>

        {/* Email Info */}
        <div className="flex-1 min-w-0">
          <div className="flex items-center justify-between mb-0.5">
            <div className="flex items-center gap-2 min-w-0 flex-1">
              <p className={`text-xs truncate ${
                isSelected ? 'font-bold text-indigo-900' : 'font-semibold text-red-800'
              }`}>
                {getSenderName(email.from)}
              </p>
            </div>
            <p className={`text-[10px] ml-3 flex-shrink-0 font-medium ${
              isSelected ? 'text-indigo-700' : 'text-red-600'
            }`}>
              {formatDate(email.receivedAt)}
            </p>
          </div>
          <p className={`text-xs truncate ${
            isSelected ? 'font-medium text-indigo-800' : 'text-red-700'
          }`}>
            {email.subject || '(no subject)'}
          </p>
        </div>

        {/* Hover actions */}
        <div className="opacity-0 group-hover:opacity-100 transition-all duration-200 flex items-center gap-2">
          <button 
            onClick={handleDelete}
            disabled={deleteMutation.isPending}
            className="p-2 rounded-lg transition-all backdrop-blur-sm hover:bg-red-200/70 text-red-700 disabled:opacity-50 font-bold"
            title="Permanently Delete"
          >
            {deleteMutation.isPending ? (
              <svg className="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
            ) : (
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M6 18L18 6M6 6l12 12" />
              </svg>
            )}
          </button>
        </div>
      </div>

      {/* Glass shine effect */}
      <div className="absolute inset-0 bg-gradient-to-br from-white/10 via-transparent to-transparent pointer-events-none"></div>
    </div>
  );
}

