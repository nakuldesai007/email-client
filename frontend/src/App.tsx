import React, { useMemo, useState } from 'react';
import { QueryClient, QueryClientProvider, useQuery, useQueryClient } from '@tanstack/react-query';
import { Sidebar } from './components/Sidebar';
import { EmailList } from './components/EmailList';
import { EmailDetail, type ReplyForwardPayload } from './components/EmailDetail';
import { ComposeEmail } from './components/ComposeEmail';
import { SentEmails } from './components/SentEmails';
import { TrashEmails } from './components/TrashEmails';
import { EmptyState } from './components/EmptyState';
import { fetchInbox } from './services/emailClient';

const queryClient = new QueryClient();

type View = 'inbox' | 'sent' | 'drafts' | 'trash';

function AppContent() {
  const [selectedEmailId, setSelectedEmailId] = useState<string | null>(null);
  const [currentView, setCurrentView] = useState<View>('inbox');
  const [openedEmails, setOpenedEmails] = useState<Set<string>>(new Set());
  const [isRefreshing, setIsRefreshing] = useState(false);
  const queryClient = useQueryClient();
  const [composeConfig, setComposeConfig] = useState<{
    mode: 'new' | 'reply' | 'forward';
    initialValues?: ReplyForwardPayload;
  } | null>(null);
  
  // Track when an email is opened
  const handleEmailClick = (emailId: string) => {
    setSelectedEmailId(emailId);
    setOpenedEmails(prev => new Set(prev).add(emailId));
  };
  
  // Close reading pane when view changes
  const handleViewChange = (view: View) => {
    setCurrentView(view);
    setSelectedEmailId(null);
  };

  const openCompose = () => {
    setComposeConfig({ mode: 'new', initialValues: undefined });
  };

  const handleReply = (payload: ReplyForwardPayload) => {
    setComposeConfig({ mode: 'reply', initialValues: payload });
  };

  const handleForward = (payload: ReplyForwardPayload) => {
    setComposeConfig({ mode: 'forward', initialValues: payload });
  };
  
  // Get user email from environment or use placeholder
  const userEmail = 'nakuldesai007@gmail.com';

  // Fetch inbox to get unread count
  const { data: inboxData, refetch: refetchInbox, isFetching: isInboxFetching } = useQuery({
    queryKey: ['inbox'],
    queryFn: fetchInbox,
    refetchInterval: 30000,
    refetchOnWindowFocus: 'always',
    refetchOnReconnect: 'always',
    refetchOnMount: 'always',
  });
  const handleRefreshClick = async () => {
    setIsRefreshing(true);
    setSelectedEmailId(null);
    setOpenedEmails(new Set());
    try {
      await refetchInbox({ throwOnError: false });
      await queryClient.refetchQueries({ queryKey: ['trash'], type: 'active' });
      await queryClient.refetchQueries({ queryKey: ['sent'], type: 'active' });
    } finally {
      setIsRefreshing(false);
    }
  };

  const unreadCount = useMemo(() => {
    const emails = inboxData?.emails ?? [];
    return emails.reduce((count, email) => {
      if (!email.unread) {
        return count;
      }
      return openedEmails.has(email.id) ? count : count + 1;
    }, 0);
  }, [inboxData, openedEmails]);

  const renderMainContent = () => {

    // View title and description
    const viewConfig = {
      inbox: { title: 'Inbox', description: 'Your received messages' },
      sent: { title: 'Sent', description: 'Messages you\'ve sent' },
      drafts: { title: 'Drafts', description: 'Unfinished messages' },
      trash: { title: 'Trash', description: 'Deleted messages' },
    };

    const config = viewConfig[currentView];

    return (
      <div className="flex-1 flex flex-col bg-gradient-to-br from-indigo-50 via-purple-50 to-pink-50 relative overflow-hidden">
        {/* Decorative background elements */}
        <div className="absolute inset-0 bg-grid-pattern opacity-[0.02] pointer-events-none"></div>
        <div className="absolute top-0 right-0 w-[500px] h-[500px] bg-gradient-to-br from-violet-300/30 to-fuchsia-300/20 rounded-full blur-3xl pointer-events-none"></div>
        <div className="absolute bottom-0 left-0 w-[500px] h-[500px] bg-gradient-to-tr from-indigo-300/30 to-blue-300/20 rounded-full blur-3xl pointer-events-none"></div>
        <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-[400px] h-[400px] bg-gradient-to-br from-purple-200/20 to-pink-200/20 rounded-full blur-3xl pointer-events-none"></div>

        {/* Header with gradient and glass effect */}
        <header className="relative bg-gradient-to-r from-indigo-500/10 via-purple-500/10 to-pink-500/10 backdrop-blur-xl border-b border-indigo-200/50 px-8 py-5 shadow-lg">
          <div className="flex items-center justify-between gap-6 flex-wrap">
            <div className="min-w-[200px]">
              <div className="flex items-center gap-3">
                <h1 className="text-2xl font-bold bg-gradient-to-r from-indigo-600 via-purple-600 to-pink-600 bg-clip-text text-transparent">
                  {config.title}
                </h1>
                {currentView === 'inbox' && unreadCount > 0 && (
                  <span className="px-3 py-1 bg-gradient-to-r from-amber-500 to-orange-500 text-white text-xs font-bold rounded-full shadow-lg shadow-amber-300/50 animate-pulse flex items-center gap-1.5">
                    <svg className="w-3.5 h-3.5" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z" />
                      <path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z" />
                    </svg>
                    {unreadCount} Unread
                  </span>
                )}
              </div>
              <p className="text-sm text-indigo-700/80 mt-1 font-medium">{config.description}</p>
            </div>
            <div className="flex items-center gap-3">
              <button
                type="button"
                onClick={handleRefreshClick}
                disabled={isRefreshing || isInboxFetching}
                className="group relative inline-flex items-center gap-2 px-4 py-2 bg-indigo-600 text-white text-sm font-semibold rounded-full shadow-lg shadow-indigo-300/40 hover:bg-indigo-700 transition-all disabled:opacity-60 disabled:cursor-not-allowed disabled:hover:bg-indigo-600"
                title="Refresh mailbox"
              >
                <span className="absolute inset-0 rounded-full bg-white/10 opacity-0 group-hover:opacity-100 transition-opacity blur-sm"></span>
                {isRefreshing || isInboxFetching ? (
                  <svg className="w-5 h-5 animate-spin relative" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                    <circle className="opacity-30" cx="12" cy="12" r="9" strokeWidth="3" />
                    <path className="opacity-80" strokeWidth="3" strokeLinecap="round" d="M12 3a9 9 0 019 9" />
                  </svg>
                ) : (
                  <svg className="w-5 h-5 relative" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2.2}
                      d="M4.5 6.75l.75-.75M19.5 6.75l-.75-.75M4.5 17.25l.75.75M19.5 17.25l-.75.75M5 12a7 7 0 1114 0"
                    />
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2.2}
                      d="M12 5v4l2.5-1.25M12 19v-4l-2.5 1.25"
                    />
                  </svg>
                )}
                <span className="relative">Refresh</span>
              </button>
            </div>
          </div>
        </header>

        {/* Content Area with Split View */}
        <main className="flex-1 overflow-hidden relative flex flex-col min-h-0">
          <div className="flex-1 flex max-w-[1920px] mx-auto w-full p-6 gap-4 min-h-0">
            {/* Email List - Left Section */}
            <div className={`${selectedEmailId ? 'w-[45%]' : 'flex-1'} bg-white/40 backdrop-blur-md rounded-2xl shadow-xl border border-white/60 overflow-hidden transition-all duration-300`}>
              {currentView === 'inbox' && (
                <EmailList onEmailClick={handleEmailClick} selectedEmailId={selectedEmailId} openedEmails={openedEmails} />
              )}
              {currentView === 'sent' && (
                <SentEmails onEmailClick={handleEmailClick} selectedEmailId={selectedEmailId} openedEmails={openedEmails} />
              )}
              {currentView === 'drafts' && (
                <EmptyState
                  title="No Drafts"
                  description="You don't have any draft messages. Start composing a new email to create a draft."
                />
              )}
              {currentView === 'trash' && (
                <TrashEmails onEmailClick={handleEmailClick} selectedEmailId={selectedEmailId} openedEmails={openedEmails} />
              )}
            </div>

            {/* Reading Pane - Right Section */}
            {selectedEmailId && (
              <div className="flex-1 bg-white/40 backdrop-blur-md rounded-2xl shadow-xl border border-white/60 overflow-hidden transition-all duration-300 min-h-0">
                <div className="h-full flex flex-col min-h-0">
                  {/* Reading pane header */}
                  <div className="flex items-center justify-between px-6 py-3 border-b border-slate-200/60 bg-white/50 flex-shrink-0">
                    <h3 className="text-sm font-semibold text-slate-700">Reading Pane</h3>
                    <button
                      onClick={() => setSelectedEmailId(null)}
                      className="p-1.5 hover:bg-slate-200/50 rounded-lg transition-colors"
                      title="Close"
                    >
                      <svg className="w-4 h-4 text-slate-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                      </svg>
                    </button>
                  </div>
                  {/* Reading pane content */}
                  <div className="flex-1 min-h-0">
                    <EmailDetail 
                      emailId={selectedEmailId} 
                      onClose={() => setSelectedEmailId(null)}
                      compact={true}
                      currentView={currentView}
                      onReply={handleReply}
                      onForward={handleForward}
                    />
                  </div>
                </div>
              </div>
            )}
          </div>
        </main>
      </div>
    );
  };

  return (
    <div className="flex h-screen bg-gradient-to-br from-slate-900 to-indigo-900 overflow-hidden">
      {/* Sidebar */}
      <Sidebar
        currentView={currentView}
        onViewChange={handleViewChange}
        onCompose={openCompose}
        userEmail={userEmail}
      />

      {/* Main Content */}
      {renderMainContent()}

      {/* Compose Modal */}
      {composeConfig && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <ComposeEmail 
            onClose={() => setComposeConfig(null)} 
            mode={composeConfig.mode}
            initialValues={composeConfig.initialValues}
          />
        </div>
      )}
    </div>
  );
}

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AppContent />
    </QueryClientProvider>
  );
}

export default App;

