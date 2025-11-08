import React from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { fetchSentEmails, moveToTrash } from '../services/emailClient';
import type { EmailPreviewMessage, ListSentResponse, ListTrashResponse } from '../generated/email_service';

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

interface SentEmailsProps {
  onEmailClick: (emailId: string) => void;
  selectedEmailId?: string | null;
  openedEmails?: Set<string>;
}

export function SentEmails({ onEmailClick, selectedEmailId, openedEmails }: SentEmailsProps) {
  const { data, isLoading, error } = useQuery({
    queryKey: ['sent'],
    queryFn: fetchSentEmails,
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
        <p className="text-red-800 font-semibold">Failed to load sent emails</p>
        <p className="text-red-600 text-sm mt-1">{String(error)}</p>
      </div>
    );
  }

  const emails = data?.emails ?? [];

  if (emails.length === 0) {
    return (
      <div className="text-center p-8 text-gray-500">
        <p className="text-lg">No sent messages</p>
      </div>
    );
  }

  return (
    <div className="h-full overflow-auto">
      <div className="p-3 space-y-1.5">
        {emails.map((email: EmailPreviewMessage) => (
          <SentEmailRow 
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

function SentEmailRow({ email, onClick, isSelected, isOpened }: { email: EmailPreviewMessage; onClick: () => void; isSelected: boolean; isOpened: boolean }) {
  const queryClient = useQueryClient();
  
  const deleteMutation = useMutation({
    mutationFn: (id: string) => moveToTrash(id),
    onSuccess: (_, variables) => {
      queryClient.setQueryData(['sent'], (previous: ListSentResponse | undefined) => {
        if (!previous) return previous;
        return {
          ...previous,
          emails: previous.emails.filter((entry) => entry.id !== variables),
        };
      });
      queryClient.setQueryData(['trash'], (previous: ListTrashResponse | undefined) => {
        if (!previous) {
          return { emails: [email] };
        }
        const alreadyExists = previous.emails.some((entry) => entry.id === email.id);
        if (alreadyExists) {
          return previous;
        }
        return {
          ...previous,
          emails: [email, ...previous.emails],
        };
      });
      queryClient.invalidateQueries({ queryKey: ['trash'] });
      queryClient.invalidateQueries({ queryKey: ['sent'] });
    },
  });

  const handleDelete = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (window.confirm('Move this email to trash?')) {
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

  const getRecipientName = (emailAddress: string) => {
    if (!emailAddress) return 'Unknown';
    const match = emailAddress.match(/^"?([^"<]+)"?\s*</);
    return match ? match[1].trim() : emailAddress.split('<')[0].trim() || emailAddress;
  };

  const handleDragStart = (event: React.DragEvent<HTMLDivElement>) => {
    event.dataTransfer.effectAllowed = 'move';
    event.dataTransfer.setData('text/plain', email.id);
    try {
      event.dataTransfer.setData('application/email-item', JSON.stringify({ id: email.id, origin: 'sent', email: serializePreview(email) }));
    } catch (error) {
      console.error('Unable to serialize email for drag operation', error);
    }
  };

  // For sent emails, the 'from' field in preview actually contains recipient info
  const recipientDisplay = getRecipientName(email.from);

  return (
    <div
      onClick={onClick}
      draggable
      onDragStart={handleDragStart}
      className={`group relative overflow-hidden rounded-lg cursor-pointer transition-all duration-200 ${
        isSelected
          ? 'bg-gradient-to-r from-indigo-100/90 to-purple-100/90 backdrop-blur-sm border-2 border-indigo-400/60 shadow-lg shadow-indigo-200/50 ring-2 ring-indigo-300/30'
          : isOpened
          ? 'bg-gradient-to-r from-blue-50/80 to-sky-50/80 backdrop-blur-sm border border-blue-200/50 shadow-sm hover:shadow-md hover:border-blue-300/70'
          : 'bg-white/60 backdrop-blur-sm border border-slate-200/60 shadow-sm hover:shadow-md hover:border-slate-300/80'
      }`}
    >
      <div className="flex items-center gap-3 px-4 py-2.5">
        {/* Avatar with glass effect - more compact */}
        <div className="relative w-9 h-9 rounded-full flex items-center justify-center font-bold text-xs flex-shrink-0 backdrop-blur-sm bg-gradient-to-br from-emerald-400 via-teal-500 to-cyan-500 text-white shadow-md shadow-teal-300/50">
          <svg className="w-4 h-4 relative z-10" fill="currentColor" viewBox="0 0 24 24">
            <path d="M1.946 9.315c-.522-.174-.527-.455.01-.634l19.087-6.362c.529-.176.832.12.684.638l-5.454 19.086c-.15.529-.455.547-.679.045L12 14l6-8-8 6-8.054-2.685z"/>
          </svg>
        </div>

        {/* Email Info - more compact */}
        <div className="flex-1 min-w-0">
          <div className="flex items-center justify-between mb-0.5">
            <div className="flex items-center gap-2 min-w-0 flex-1">
              <p className={`text-xs truncate ${
                isSelected ? 'font-bold text-indigo-900' : isOpened ? 'font-semibold text-blue-800' : 'font-semibold text-slate-700'
              }`}>
                {recipientDisplay}
              </p>
            </div>
            <p className={`text-[10px] ml-3 flex-shrink-0 font-medium ${
              isSelected ? 'text-indigo-700' : isOpened ? 'text-blue-600' : 'text-slate-500'
            }`}>
              {formatDate(email.receivedAt)}
            </p>
          </div>
          <p className={`text-xs truncate ${
            isSelected ? 'font-medium text-indigo-800' : isOpened ? 'text-blue-700' : 'text-slate-600'
          }`}>
            {email.subject || '(no subject)'}
          </p>
        </div>

        {/* Hover actions with glass effect */}
        <div className="opacity-0 group-hover:opacity-100 transition-all duration-200 flex items-center gap-2">
          <button 
            onClick={handleDelete}
            disabled={deleteMutation.isPending}
            className="p-2 rounded-lg transition-all backdrop-blur-sm hover:bg-red-100/70 text-red-600 disabled:opacity-50"
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

      {/* Glass shine effect */}
      <div className="absolute inset-0 bg-gradient-to-br from-white/10 via-transparent to-transparent pointer-events-none"></div>
    </div>
  );
}

