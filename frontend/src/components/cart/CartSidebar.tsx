import React from 'react';
import { useCart } from '../../contexts/CartContext';
import { useNavigate } from 'react-router-dom';
import { useToast } from '../../App';

const CartSidebar: React.FC = () => {
  const { cart, isCartOpen, toggleCart, removeFromCart, updateQuantity, clearCart } = useCart();
  const navigate = useNavigate();
  const { showToast } = useToast();

  const handleRemove = async (itemId: number) => {
    try {
      await removeFromCart(itemId);
    } catch {
      showToast('Failed to remove item', 'error');
    }
  };

  const handleQuantity = async (itemId: number, qty: number) => {
    if (qty < 1) {
      await handleRemove(itemId);
      return;
    }
    try {
      await updateQuantity(itemId, qty);
    } catch {
      showToast('Failed to update quantity', 'error');
    }
  };

  const handleCheckout = () => {
    toggleCart();
    navigate('/checkout');
  };

  if (!isCartOpen) return null;

  return (
    <>
      {/* Backdrop */}
      <div
        style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)', zIndex: 101 }}
        onClick={toggleCart}
      />
      {/* Sidebar */}
      <div style={{
        position: 'fixed',
        top: 0,
        right: 0,
        bottom: 0,
        width: '380px',
        maxWidth: '100vw',
        background: 'white',
        zIndex: 102,
        display: 'flex',
        flexDirection: 'column',
        boxShadow: '-4px 0 24px rgba(0,0,0,0.15)',
      }}>
        {/* Header */}
        <div style={{
          padding: '20px 24px',
          borderBottom: '1px solid #E5E5E5',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
        }}>
          <div>
            <h2 style={{ fontSize: '18px', fontWeight: 700, margin: 0 }}>Your Cart</h2>
            {cart && <p style={{ fontSize: '13px', color: '#666', margin: '2px 0 0' }}>{cart.restaurantName}</p>}
          </div>
          <button
            onClick={toggleCart}
            style={{ background: 'none', border: 'none', fontSize: '22px', cursor: 'pointer', color: '#666', lineHeight: 1 }}
          >
            ✕
          </button>
        </div>

        {/* Items */}
        <div style={{ flex: 1, overflowY: 'auto', padding: '16px' }}>
          {!cart || cart.items.length === 0 ? (
            <div className="empty-state" style={{ padding: '60px 20px' }}>
              <div className="empty-icon">🛒</div>
              <h3>Your cart is empty</h3>
              <p>Add items from a restaurant to get started</p>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              {cart.items.map((item) => (
                <div key={item.id} style={{
                  display: 'flex',
                  gap: '12px',
                  padding: '12px',
                  background: '#f9f9f9',
                  borderRadius: '10px',
                }}>
                  <div style={{ flex: 1 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '4px' }}>
                      <span style={{ fontSize: '12px' }}>{item.vegetarian ? '🟢' : '🔴'}</span>
                      <span style={{ fontSize: '14px', fontWeight: 600 }}>{item.name}</span>
                    </div>
                    <span style={{ fontSize: '14px', color: '#FF6B35', fontWeight: 600 }}>₹{item.subtotal.toFixed(2)}</span>
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '8px' }}>
                    <button
                      onClick={() => handleRemove(item.id)}
                      style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#e74c3c', fontSize: '14px' }}
                    >
                      🗑
                    </button>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <button
                        onClick={() => handleQuantity(item.id, item.quantity - 1)}
                        style={{
                          width: '26px', height: '26px', borderRadius: '50%',
                          border: '1.5px solid #E5E5E5', background: 'white',
                          cursor: 'pointer', fontWeight: 700, fontSize: '16px', lineHeight: 1,
                        }}
                      >−</button>
                      <span style={{ fontSize: '14px', fontWeight: 600, minWidth: '16px', textAlign: 'center' }}>{item.quantity}</span>
                      <button
                        onClick={() => handleQuantity(item.id, item.quantity + 1)}
                        style={{
                          width: '26px', height: '26px', borderRadius: '50%',
                          border: '1.5px solid #FF6B35', background: '#FF6B35',
                          cursor: 'pointer', color: 'white', fontWeight: 700, fontSize: '16px', lineHeight: 1,
                        }}
                      >+</button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Footer */}
        {cart && cart.items.length > 0 && (
          <div style={{ padding: '16px 24px', borderTop: '1px solid #E5E5E5' }}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', marginBottom: '16px', fontSize: '14px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ color: '#666' }}>Subtotal</span>
                <span>₹{cart.subtotal.toFixed(2)}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ color: '#666' }}>Delivery fee</span>
                <span>₹{cart.deliveryFee.toFixed(2)}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: 700, fontSize: '16px', borderTop: '1px solid #E5E5E5', paddingTop: '8px', marginTop: '4px' }}>
                <span>Total</span>
                <span style={{ color: '#FF6B35' }}>₹{cart.total.toFixed(2)}</span>
              </div>
            </div>
            <button
              onClick={handleCheckout}
              className="btn-primary"
              style={{ width: '100%', justifyContent: 'center', fontSize: '15px' }}
            >
              Proceed to Checkout →
            </button>
            <button
              onClick={() => clearCart().catch(() => {})}
              style={{
                width: '100%', marginTop: '8px', padding: '8px',
                background: 'none', border: 'none', cursor: 'pointer',
                fontSize: '13px', color: '#999',
              }}
            >
              Clear cart
            </button>
          </div>
        )}
      </div>
    </>
  );
};

export default CartSidebar;
