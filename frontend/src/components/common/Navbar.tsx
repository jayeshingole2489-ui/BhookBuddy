import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { useCart } from '../../contexts/CartContext';
import { notificationService } from '../../services/notificationService';

const Navbar: React.FC = () => {
  const { user, isAuthenticated, isAdmin, logout } = useAuth();
  const { cartItemCount, toggleCart } = useCart();
  const navigate = useNavigate();
  const location = useLocation();
  const [menuOpen, setMenuOpen] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    if (isAuthenticated) {
      notificationService.getUnreadCount()
        .then(setUnreadCount)
        .catch(() => {});
    } else {
      setUnreadCount(0);
    }
  }, [isAuthenticated, location.pathname]);

  const handleLogout = () => {
    logout();
    navigate('/');
    setMenuOpen(false);
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/search?q=${encodeURIComponent(searchQuery.trim())}`);
      setSearchQuery('');
    }
  };

  const isActive = (path: string) => location.pathname === path;

  return (
    <nav style={{
      background: 'white',
      borderBottom: '1px solid #E5E5E5',
      position: 'sticky',
      top: 0,
      zIndex: 90,
      height: '70px',
    }}>
      <div style={{
        maxWidth: '1200px',
        margin: '0 auto',
        padding: '0 24px',
        height: '100%',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        gap: '20px',
      }}>
        {/* Logo */}
        <Link to="/" style={{ textDecoration: 'none', flexShrink: 0 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span style={{
              fontSize: '22px',
              fontWeight: 800,
              color: '#1A1A2E',
              letterSpacing: '-0.5px',
            }}>
              Bhook
            </span>
            <span style={{
              fontSize: '22px',
              fontWeight: 800,
              color: '#FF6B35',
              letterSpacing: '-0.5px',
            }}>
              Buddy
            </span>
            <span style={{
              width: '8px',
              height: '8px',
              borderRadius: '50%',
              background: '#FF6B35',
              display: 'inline-block',
              marginLeft: '2px',
              marginBottom: '8px',
            }} />
          </div>
        </Link>

        {/* Search bar */}
        <form onSubmit={handleSearch} style={{ flex: 1, maxWidth: '400px' }}>
          <div style={{ position: 'relative' }}>
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search restaurants..."
              style={{
                width: '100%',
                padding: '10px 16px 10px 40px',
                border: '1.5px solid #E5E5E5',
                borderRadius: '24px',
                fontSize: '14px',
                outline: 'none',
                background: '#f8f8f8',
                transition: 'border-color 0.2s',
              }}
              onFocus={(e) => (e.target.style.borderColor = '#FF6B35')}
              onBlur={(e) => (e.target.style.borderColor = '#E5E5E5')}
            />
            <span style={{
              position: 'absolute',
              left: '14px',
              top: '50%',
              transform: 'translateY(-50%)',
              fontSize: '16px',
              pointerEvents: 'none',
            }}>🔍</span>
          </div>
        </form>

        {/* Nav links */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexShrink: 0 }}>
          <Link
            to="/"
            style={{
              padding: '8px 12px',
              borderRadius: '6px',
              fontSize: '14px',
              fontWeight: 500,
              color: isActive('/') ? '#FF6B35' : '#1A1A2E',
              background: isActive('/') ? '#FFF3EE' : 'transparent',
              textDecoration: 'none',
            }}
          >
            Home
          </Link>
          <Link
            to="/restaurants"
            style={{
              padding: '8px 12px',
              borderRadius: '6px',
              fontSize: '14px',
              fontWeight: 500,
              color: isActive('/restaurants') ? '#FF6B35' : '#1A1A2E',
              background: isActive('/restaurants') ? '#FFF3EE' : 'transparent',
              textDecoration: 'none',
            }}
          >
            Restaurants
          </Link>

          {isAuthenticated ? (
            <>
              {isAdmin && (
                <Link
                  to="/admin"
                  style={{
                    padding: '8px 12px',
                    borderRadius: '6px',
                    fontSize: '14px',
                    fontWeight: 500,
                    color: location.pathname.startsWith('/admin') ? '#FF6B35' : '#1A1A2E',
                    background: location.pathname.startsWith('/admin') ? '#FFF3EE' : 'transparent',
                    textDecoration: 'none',
                  }}
                >
                  Admin
                </Link>
              )}

              {/* Cart button */}
              <button
                onClick={toggleCart}
                style={{
                  position: 'relative',
                  background: 'none',
                  border: 'none',
                  cursor: 'pointer',
                  fontSize: '22px',
                  padding: '8px',
                  borderRadius: '50%',
                  lineHeight: 1,
                }}
              >
                🛒
                {cartItemCount > 0 && (
                  <span style={{
                    position: 'absolute',
                    top: '2px',
                    right: '2px',
                    background: '#FF6B35',
                    color: 'white',
                    borderRadius: '50%',
                    width: '18px',
                    height: '18px',
                    fontSize: '11px',
                    fontWeight: 700,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                  }}>
                    {cartItemCount > 9 ? '9+' : cartItemCount}
                  </span>
                )}
              </button>

              {/* Notifications */}
              <Link
                to="/notifications"
                style={{
                  position: 'relative',
                  fontSize: '22px',
                  padding: '8px',
                  borderRadius: '50%',
                  lineHeight: 1,
                  display: 'flex',
                  alignItems: 'center',
                }}
              >
                🔔
                {unreadCount > 0 && (
                  <span style={{
                    position: 'absolute',
                    top: '2px',
                    right: '2px',
                    background: '#e74c3c',
                    color: 'white',
                    borderRadius: '50%',
                    width: '18px',
                    height: '18px',
                    fontSize: '11px',
                    fontWeight: 700,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                  }}>
                    {unreadCount > 9 ? '9+' : unreadCount}
                  </span>
                )}
              </Link>

              {/* User menu */}
              <div style={{ position: 'relative' }}>
                <button
                  onClick={() => setMenuOpen(!menuOpen)}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '8px',
                    padding: '8px 12px',
                    background: '#f8f8f8',
                    border: '1px solid #E5E5E5',
                    borderRadius: '24px',
                    cursor: 'pointer',
                    fontSize: '14px',
                    fontWeight: 500,
                    color: '#1A1A2E',
                  }}
                >
                  <span style={{
                    width: '28px',
                    height: '28px',
                    borderRadius: '50%',
                    background: '#FF6B35',
                    color: 'white',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontSize: '13px',
                    fontWeight: 700,
                  }}>
                    {user?.firstName?.[0]?.toUpperCase() ?? 'U'}
                  </span>
                  <span>{user?.firstName}</span>
                  <span style={{ fontSize: '10px' }}>▼</span>
                </button>

                {menuOpen && (
                  <>
                    <div
                      style={{ position: 'fixed', inset: 0, zIndex: 50 }}
                      onClick={() => setMenuOpen(false)}
                    />
                    <div style={{
                      position: 'absolute',
                      right: 0,
                      top: 'calc(100% + 8px)',
                      background: 'white',
                      borderRadius: '12px',
                      boxShadow: '0 8px 24px rgba(0,0,0,0.12)',
                      minWidth: '180px',
                      overflow: 'hidden',
                      zIndex: 100,
                    }}>
                      {[
                        { to: '/profile', label: '👤 Profile' },
                        { to: '/orders', label: '📦 My Orders' },
                        { to: '/addresses', label: '📍 Addresses' },
                        { to: '/notifications', label: '🔔 Notifications' },
                      ].map(({ to, label }) => (
                        <Link
                          key={to}
                          to={to}
                          onClick={() => setMenuOpen(false)}
                          style={{
                            display: 'block',
                            padding: '12px 16px',
                            fontSize: '14px',
                            color: '#1A1A2E',
                            textDecoration: 'none',
                            borderBottom: '1px solid #f5f5f5',
                          }}
                          onMouseEnter={(e) => (e.currentTarget.style.background = '#FFF3EE')}
                          onMouseLeave={(e) => (e.currentTarget.style.background = 'white')}
                        >
                          {label}
                        </Link>
                      ))}
                      <button
                        onClick={handleLogout}
                        style={{
                          width: '100%',
                          padding: '12px 16px',
                          fontSize: '14px',
                          color: '#e74c3c',
                          background: 'white',
                          border: 'none',
                          textAlign: 'left',
                          cursor: 'pointer',
                        }}
                        onMouseEnter={(e) => (e.currentTarget.style.background = '#FDEDEC')}
                        onMouseLeave={(e) => (e.currentTarget.style.background = 'white')}
                      >
                        🚪 Logout
                      </button>
                    </div>
                  </>
                )}
              </div>
            </>
          ) : (
            <>
              <Link
                to="/login"
                style={{
                  padding: '9px 20px',
                  borderRadius: '8px',
                  fontSize: '14px',
                  fontWeight: 600,
                  color: '#FF6B35',
                  border: '2px solid #FF6B35',
                  textDecoration: 'none',
                }}
              >
                Login
              </Link>
              <Link
                to="/register"
                style={{
                  padding: '9px 20px',
                  borderRadius: '8px',
                  fontSize: '14px',
                  fontWeight: 600,
                  color: 'white',
                  background: '#FF6B35',
                  textDecoration: 'none',
                }}
              >
                Register
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
