import React, { useEffect, useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { sendEmail } from '../services/emailClient';

interface ComposeEmailProps {
  onClose?: () => void;
  mode?: 'new' | 'reply' | 'forward';
  initialValues?: {
    to?: string;
    subject?: string;
    body?: string;
    cc?: string[];
    bcc?: string[];
  };
}

export function ComposeEmail({ onClose, mode = 'new', initialValues }: ComposeEmailProps) {
  const queryClient = useQueryClient();
  const [to, setTo] = useState(initialValues?.to ?? '');
  const [subject, setSubject] = useState(initialValues?.subject ?? '');
  const [body, setBody] = useState(initialValues?.body ?? '');
  const [cc, setCc] = useState(initialValues?.cc?.join(', ') ?? '');
  const [bcc, setBcc] = useState(initialValues?.bcc?.join(', ') ?? '');

  const mutation = useMutation({
    mutationFn: sendEmail,
    onSuccess: () => {
      // Refresh inbox after sending
      queryClient.invalidateQueries({ queryKey: ['inbox'] });
      // Reset form
      setTo('');
      setSubject('');
      setBody('');
      setCc('');
      setBcc('');
      onClose?.();
    },
  });

  useEffect(() => {
    setTo(initialValues?.to ?? '');
    setSubject(initialValues?.subject ?? '');
    setBody(initialValues?.body ?? '');
    setCc(initialValues?.cc?.join(', ') ?? '');
    setBcc(initialValues?.bcc?.join(', ') ?? '');
    mutation.reset();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [initialValues]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    mutation.mutate({
      to,
      subject,
      body,
      cc: cc ? cc.split(',').map(s => s.trim()) : [],
      bcc: bcc ? bcc.split(',').map(s => s.trim()) : [],
    });
  };

  const [showCcBcc, setShowCcBcc] = useState(false);
  useEffect(() => {
    const hasCc = Boolean(initialValues?.cc && initialValues.cc.length > 0);
    const hasBcc = Boolean(initialValues?.bcc && initialValues.bcc.length > 0);
    setShowCcBcc(hasCc || hasBcc);
  }, [initialValues]);

  const headerTitle =
    mode === 'reply' ? 'Reply' : mode === 'forward' ? 'Forward Message' : 'New Message';
  const submitLabel =
    mode === 'reply' ? 'Send Reply' : mode === 'forward' ? 'Send Forward' : 'Send Email';

  return (
    <div className="bg-white rounded-xl shadow-2xl border border-gray-200 max-w-4xl w-full max-h-[90vh] flex flex-col">
      {/* Header */}
      <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200 bg-slate-50">
        <h2 className="text-xl font-bold text-gray-900">{headerTitle}</h2>
        {onClose && (
          <button
            onClick={onClose}
            className="p-2 text-gray-400 hover:text-gray-600 hover:bg-slate-200 rounded-lg transition-colors"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        )}
      </div>

      {/* Form */}
      <form onSubmit={handleSubmit} className="flex-1 flex flex-col overflow-hidden">
        <div className="p-6 space-y-4 overflow-y-auto">
          {/* To field */}
          <div className="flex items-center border-b border-gray-200 pb-3">
            <label htmlFor="to" className="text-sm font-medium text-gray-600 w-16">
              To
            </label>
            <input
              id="to"
              type="email"
              value={to}
              onChange={(e) => setTo(e.target.value)}
              required
              className="flex-1 px-2 py-1 focus:outline-none text-gray-900"
              placeholder="recipient@example.com"
            />
            {!showCcBcc && (
              <button
                type="button"
                onClick={() => setShowCcBcc(true)}
                className="text-xs text-indigo-600 hover:text-indigo-700 font-medium ml-2"
              >
                Cc/Bcc
              </button>
            )}
          </div>

          {/* Cc field */}
          {showCcBcc && (
            <div className="flex items-center border-b border-gray-200 pb-3">
              <label htmlFor="cc" className="text-sm font-medium text-gray-600 w-16">
                Cc
              </label>
              <input
                id="cc"
                type="text"
                value={cc}
                onChange={(e) => setCc(e.target.value)}
                className="flex-1 px-2 py-1 focus:outline-none text-gray-900"
                placeholder="Optional"
              />
            </div>
          )}

          {/* Bcc field */}
          {showCcBcc && (
            <div className="flex items-center border-b border-gray-200 pb-3">
              <label htmlFor="bcc" className="text-sm font-medium text-gray-600 w-16">
                Bcc
              </label>
              <input
                id="bcc"
                type="text"
                value={bcc}
                onChange={(e) => setBcc(e.target.value)}
                className="flex-1 px-2 py-1 focus:outline-none text-gray-900"
                placeholder="Optional"
              />
            </div>
          )}

          {/* Subject field */}
          <div className="flex items-center border-b border-gray-200 pb-3">
            <label htmlFor="subject" className="text-sm font-medium text-gray-600 w-16">
              Subject
            </label>
            <input
              id="subject"
              type="text"
              value={subject}
              onChange={(e) => setSubject(e.target.value)}
              required
              className="flex-1 px-2 py-1 focus:outline-none text-gray-900"
              placeholder="Email subject"
            />
          </div>

          {/* Body */}
          <div className="flex-1 min-h-[300px]">
            <textarea
              id="body"
              value={body}
              onChange={(e) => setBody(e.target.value)}
              required
              className="w-full h-full min-h-[300px] px-2 py-1 focus:outline-none text-gray-900 resize-none"
              placeholder="Type your message here..."
            />
          </div>

          {/* Error message */}
          {mutation.isError && (
            <div className="bg-red-50 border border-red-200 rounded-lg p-3">
              <p className="text-red-800 text-sm font-semibold">Failed to send email</p>
              <p className="text-red-600 text-xs mt-1">{String(mutation.error)}</p>
            </div>
          )}

          {/* Success message */}
          {mutation.isSuccess && (
            <div className="bg-emerald-50 border border-emerald-200 rounded-lg p-3">
              <p className="text-emerald-800 text-sm font-semibold">Email sent successfully!</p>
            </div>
          )}
        </div>

        {/* Footer with actions */}
        <div className="flex items-center justify-between px-6 py-4 border-t border-gray-200 bg-slate-50">
          <div className="flex items-center gap-2">
            <button
              type="button"
              className="p-2 text-gray-600 hover:bg-slate-200 rounded-lg transition-colors"
              title="Attach file"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13" />
              </svg>
            </button>
          </div>
          <div className="flex items-center gap-3">
            {onClose && (
              <button
                type="button"
                onClick={onClose}
                className="px-5 py-2.5 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
              >
                Cancel
              </button>
            )}
            <button
              type="submit"
              disabled={mutation.isPending}
              className="px-6 py-2.5 text-sm font-medium text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors shadow-lg shadow-indigo-600/30"
            >
              {mutation.isPending ? (
                <span className="flex items-center gap-2">
                  <svg className="animate-spin h-4 w-4" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  Sending...
                </span>
              ) : (
                submitLabel
              )}
            </button>
          </div>
        </div>
      </form>
    </div>
  );
}

