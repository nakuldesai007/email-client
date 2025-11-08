import React, { useState } from 'react';

interface SidebarProps {
  currentView: 'inbox' | 'sent' | 'drafts' | 'trash';
  onViewChange: (view: 'inbox' | 'sent' | 'drafts' | 'trash') => void;
  onCompose: () => void;
  userEmail: string;
}

export function Sidebar({ currentView, onViewChange, onCompose, userEmail }: SidebarProps) {
  const [isCollapsed, setIsCollapsed] = useState(false);
  const menuItems = [
    { id: 'inbox' as const, label: 'Inbox', icon: InboxIcon, count: null },
    { id: 'sent' as const, label: 'Sent', icon: SendIcon, count: null },
    { id: 'drafts' as const, label: 'Drafts', icon: DocumentIcon, count: 0 },
    { id: 'trash' as const, label: 'Trash', icon: TrashIcon, count: null },
  ];

  // Extract initials from email
  const initials = userEmail
    .split('@')[0]
    .split('.')
    .map(part => part[0]?.toUpperCase())
    .join('')
    .slice(0, 2) || 'U';

  const sidebarWidthClass = isCollapsed ? 'w-20' : 'w-64';

  return (
    <aside
      className={`${sidebarWidthClass} bg-slate-900 text-white flex flex-col h-screen flex-shrink-0 transition-[width] duration-300 ease-in-out overflow-hidden`}
      data-collapsed={isCollapsed}
    >
      {/* Logo/Brand */}
      <div className={`p-6 border-b border-slate-700 ${isCollapsed ? 'py-4 flex flex-col items-center gap-3' : ''}`}>
        <div className={`flex items-center gap-3 w-full ${isCollapsed ? 'justify-center' : ''}`}>
          <div className={`flex items-center gap-3 flex-1 min-w-0 ${isCollapsed ? 'justify-center' : ''}`}>
            <svg className={`${isCollapsed ? 'w-12 h-12' : 'w-10 h-10'} flex-shrink-0 transition-all duration-200`} viewBox="0 0 96 96">
            <defs>
              <linearGradient id="g1" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" stopColor="#FFD44D" />
                  <stop offset="100%" stopColor="#C7961A" />
              </linearGradient>
            </defs>
              <rect x="8" y="20" width="80" height="56" rx="8" fill="url(#g1)" stroke="#A0730F" strokeWidth="4" />
              <polyline points="8 26 48 52 88 26" fill="none" stroke="#A0730F" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" />
              <circle cx="48" cy="48" r="16" fill="white" stroke="#A0730F" strokeWidth="3" />
              <text x="48" y="54" textAnchor="middle" fontSize="14" fontWeight="700" fill="#A0730F" fontFamily="Arial">
                ND
              </text>
            </svg>
            {!isCollapsed && <h1 className="text-xl font-bold truncate">MailFlow</h1>}
          </div>
          {!isCollapsed && (
            <button
              type="button"
              onClick={() => setIsCollapsed(prev => !prev)}
              className="p-2 bg-slate-800 hover:bg-slate-700 rounded-lg transition-colors focus:outline-none focus:ring-2 focus:ring-indigo-400"
              aria-label="Collapse sidebar"
              title="Collapse sidebar"
            >
              <svg
                className="w-4 h-4 text-white transition-transform"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth={2}
              >
                <path strokeLinecap="round" strokeLinejoin="round" d="M15 6l-6 6 6 6" />
              </svg>
            </button>
          )}
        </div>
      </div>

      {isCollapsed && (
        <div className="px-4 pt-3 pb-2 border-b border-slate-800">
          <button
            type="button"
            onClick={() => setIsCollapsed(false)}
            className="w-full flex items-center justify-center focus:outline-none focus:ring-2 focus:ring-indigo-400 rounded-xl"
            aria-label="Expand sidebar"
            title="Expand sidebar"
          >
            <span className="inline-flex flex-col items-center justify-center gap-1.5 bg-slate-800 hover:bg-slate-700 transition-colors rounded-xl px-3 py-2 shadow-sm">
              <span className="w-6 h-1.5 rounded-full bg-white/90"></span>
              <span className="w-6 h-1.5 rounded-full bg-white/70"></span>
              <span className="w-6 h-1.5 rounded-full bg-white/50"></span>
            </span>
          </button>
        </div>
      )}

      {/* Compose Button */}
      <div className={`p-4 ${isCollapsed ? 'px-2' : ''}`}>
        <button
          onClick={onCompose}
          className={`w-full flex items-center justify-center ${isCollapsed ? 'p-3 rounded-full aspect-square mx-auto max-w-[3.5rem]' : 'gap-2 px-4 py-3 rounded-lg'} bg-indigo-600 hover:bg-indigo-700 transition-colors font-medium text-sm shadow-lg shadow-indigo-900/50`}
          aria-label="Compose email"
          title="Compose email"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
          </svg>
          {!isCollapsed && 'Compose'}
        </button>
      </div>

      {/* Navigation */}
      <nav className="flex-1 px-3 py-2">
        {menuItems.map((item) => {
          const Icon = item.icon;
          const isActive = currentView === item.id;
          
          return (
            <button
              key={item.id}
              onClick={() => onViewChange(item.id)}
              className={`w-full flex items-center ${isCollapsed ? 'justify-center px-2' : 'justify-between px-3'} py-2.5 rounded-lg transition-all mb-1 ${
                isActive
                  ? 'bg-indigo-600 text-white shadow-md'
                  : 'text-slate-300 hover:bg-slate-800 hover:text-white'
              }`}
              aria-label={item.label}
              title={item.label}
            >
              <div className={`flex items-center ${isCollapsed ? 'justify-center' : 'gap-3'}`}>
                <Icon className="w-5 h-5" />
                {!isCollapsed && <span className="font-medium text-sm">{item.label}</span>}
              </div>
              {!isCollapsed && item.count !== null && item.count > 0 && (
                <span className="px-2 py-0.5 bg-slate-700 text-slate-200 text-xs rounded-full">
                  {item.count}
                </span>
              )}
            </button>
          );
        })}
      </nav>

      {/* User Profile */}
      <div className="p-4 border-t border-slate-700">
        <div className={`flex items-center ${isCollapsed ? 'justify-center' : 'gap-3'}`}>
          <div className="w-10 h-10 bg-gradient-to-br from-emerald-500 to-teal-600 rounded-full flex items-center justify-center font-semibold text-sm flex-shrink-0">
            {initials}
          </div>
          {!isCollapsed && (
          <div className="flex-1 min-w-0">
            <p className="text-sm font-medium text-white truncate">{userEmail.split('@')[0]}</p>
            <p className="text-xs text-slate-400 truncate">{userEmail}</p>
          </div>
          )}
        </div>
      </div>
    </aside>
  );
}

// Icon components - Distinctive modern icons with fill
function InboxIcon({ className }: { className?: string }) {
  return (
    <svg className={className} fill="currentColor" viewBox="0 0 24 24">
      <path d="M3 3h18a1 1 0 011 1v16a1 1 0 01-1 1H3a1 1 0 01-1-1V4a1 1 0 011-1zm17 4.238l-7.928 7.1L4 7.216V19h16V7.238zM4.511 5l7.55 6.662L19.502 5H4.511z"/>
    </svg>
  );
}

function SendIcon({ className }: { className?: string }) {
  return (
    <svg className={className} fill="currentColor" viewBox="0 0 24 24">
      <path d="M1.946 9.315c-.522-.174-.527-.455.01-.634l19.087-6.362c.529-.176.832.12.684.638l-5.454 19.086c-.15.529-.455.547-.679.045L12 14l6-8-8 6-8.054-2.685z"/>
    </svg>
  );
}

function DocumentIcon({ className }: { className?: string }) {
  return (
    <svg className={className} fill="currentColor" viewBox="0 0 24 24">
      <path d="M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-7 9h-2V5h2v6zm0 4h-2v-2h2v2z"/>
    </svg>
  );
}

function TrashIcon({ className }: { className?: string }) {
  return (
    <svg className={className} fill="currentColor" viewBox="0 0 24 24">
      <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
    </svg>
  );
}

