import React, { useState, useEffect } from 'react';
import { Notification } from '../types';
import { notificationService } from '../services/notificationService';
import { useToast } from '../App';
import LoadingSpinner from '../components/common/LoadingSpinner';

const TYPE_CONFIG: Record<string, { icon: string; color: string }> = {
  ORDER: { icon: '📦', color: '#3498db' },
  PAYMENT: { icon: '💳', color: '#2ecc71' },
  DELIVERY: { icon: '🛵', color: '#e67e22' },
  PROMO: { icon: '🎁', color: '#9b59b6' },
};

const getTypeConfig = (type: string) => TYPE_CONFIG[type] ?? { icon: '🔔', color: '#95a5a6' };

const Notifications: React.FC = () => {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(true);
  const { showToast } = useToast();

  useEffect(() => {
    notificationService.getMyNotifications()
      .then(setNotifications)
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const unreadCount = notifications.filter((n) => !n.read).length;

  const handleMarkRead = async (id: number) => {
    try {
      await notificationService.markAsRead(id);
      setNotifications((prev) => prev.map((n) => n.id === id ? { ...n, read: true } : n));
    } catch {
      // silently fail
    }
  };

  const handleMarkAllRead = async () => {
    try {
      await notificationService.markAllAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
      showToast('All notifications marked as read', 'success');
    } catch {
      showToast('Failed to mark all as read', 'error');
    }
  };

  const formatTime = (dateStr: string) => {
    const date = new Date(dateStr);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);
    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 7) return `${diffDays}d ago`;
    return date.toLocaleDateString('en-IN', { day: 'numeric', month: 'short' });
  };

  return (
    <div style={{ maxWidth: '640px', margin: '0 auto', padding: '40px 24px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '32px', flexWrap: 'wrap', gap: '12px' }}>
        <div>
          <h1 style={{ fontSize: '28px', fontWeight: 800, color: '#1A1A2E' }}>Notifications</h1>
          {unreadCount > 0 && (
            <p style={{ fontSize: '14px', color: '#FF6B35', fontWeight: 600 }}>{unreadCount} unread</p>
          )}
        </div>
        {unreadCount > 0 && (
          <button onClick={handleMarkAllRead} className="btn-ghost" style={{ fontSize: '14px' }}>
            ✓ Mark all read
          </button>
        )}
      </div>

      {loading ? <LoadingSpinner /> : notifications.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">🔔</div>
          <h3>No notifications yet</h3>
          <p>We'll notify you about your orders and deliveries here.</p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          {notifications.map((notif) => {
            const config = getTypeConfig(notif.type);
            return (
              <div
                key={notif.id}
                onClick={() => !notif.read && handleMarkRead(notif.id)}
                style={{
                  background: notif.read ? 'white' : '#FFF3EE',
                  borderRadius: '14px',
                  padding: '16px 18px',
                  display: 'flex',
                  gap: '14px',
                  alignItems: 'flex-start',
                  cursor: notif.read ? 'default' : 'pointer',
                  border: notif.read ? '1px solid #f0f0f0' : '1px solid rgba(255,107,53,0.2)',
                  transition: 'background 0.2s',
                  boxShadow: notif.read ? 'none' : '0 2px 8px rgba(255,107,53,0.08)',
                }}
              >
                {/* Icon */}
                <div style={{
                  width: '40px', height: '40px', borderRadius: '50%',
                  background: `${config.color}18`,
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  fontSize: '18px', flexShrink: 0,
                }}>
                  {config.icon}
                </div>
                {/* Content */}
                <div style={{ flex: 1 }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '8px' }}>
                    <p style={{ fontWeight: notif.read ? 500 : 700, fontSize: '14px', color: '#1A1A2E' }}>{notif.title}</p>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexShrink: 0 }}>
                      <span style={{ fontSize: '12px', color: '#999' }}>{formatTime(notif.createdAt)}</span>
                      {!notif.read && (
                        <span style={{ width: '8px', height: '8px', borderRadius: '50%', background: '#FF6B35', flexShrink: 0 }} />
                      )}
                    </div>
                  </div>
                  <p style={{ fontSize: '13px', color: '#666', marginTop: '4px', lineHeight: 1.5 }}>{notif.message}</p>
                  {notif.orderId && (
                    <p style={{ fontSize: '12px', color: '#999', marginTop: '4px' }}>Order #{notif.orderId}</p>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default Notifications;
