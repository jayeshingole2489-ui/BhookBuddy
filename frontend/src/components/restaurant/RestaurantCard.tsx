import React from 'react';
import { Link } from 'react-router-dom';
import { Restaurant } from '../../types';

interface RestaurantCardProps {
  restaurant: Restaurant;
}

const CUISINE_COLORS: Record<string, string> = {
  Indian: '#FF6B35',
  Chinese: '#E74C3C',
  Italian: '#27AE60',
  Pizza: '#F39C12',
  Burger: '#8E44AD',
  Mexican: '#16A085',
};

const RestaurantCard: React.FC<RestaurantCardProps> = ({ restaurant }) => {
  const formatPrice = (price: number) => `₹${price.toFixed(2)}`;

  return (
    <Link to={`/restaurants/${restaurant.id}`} style={{ textDecoration: 'none' }}>
      <div className="card" style={{ overflow: 'hidden', cursor: 'pointer' }}>
        {/* Image */}
        <div style={{
          height: '180px',
          background: restaurant.imageUrl
            ? `url(${restaurant.imageUrl}) center/cover no-repeat`
            : `linear-gradient(135deg, ${CUISINE_COLORS[restaurant.cuisineType] ?? '#FF6B35'}22, ${CUISINE_COLORS[restaurant.cuisineType] ?? '#FF6B35'}44)`,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          position: 'relative',
        }}>
          {!restaurant.imageUrl && (
            <span style={{ fontSize: '48px' }}>
              {restaurant.cuisineType === 'Indian' ? '🍛' :
               restaurant.cuisineType === 'Chinese' ? '🍜' :
               restaurant.cuisineType === 'Italian' ? '🍝' :
               restaurant.cuisineType === 'Pizza' ? '🍕' :
               restaurant.cuisineType === 'Burger' ? '🍔' : '🍽️'}
            </span>
          )}
          {!restaurant.active && (
            <div style={{
              position: 'absolute',
              inset: 0,
              background: 'rgba(0,0,0,0.5)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}>
              <span style={{ color: 'white', fontWeight: 700, fontSize: '14px', background: '#e74c3c', padding: '4px 12px', borderRadius: '20px' }}>
                Closed
              </span>
            </div>
          )}
        </div>

        {/* Info */}
        <div style={{ padding: '16px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
            <h3 style={{ fontSize: '16px', fontWeight: 700, color: '#1A1A2E', margin: 0 }}>{restaurant.name}</h3>
            <div style={{
              display: 'flex',
              alignItems: 'center',
              gap: '4px',
              background: restaurant.rating >= 4 ? '#EAFAF1' : '#FEF9E7',
              padding: '3px 8px',
              borderRadius: '12px',
              flexShrink: 0,
            }}>
              <span>⭐</span>
              <span style={{ fontSize: '13px', fontWeight: 600, color: restaurant.rating >= 4 ? '#1E8449' : '#D4AC0D' }}>
                {restaurant.rating.toFixed(1)}
              </span>
            </div>
          </div>

          <p style={{ fontSize: '13px', color: '#666', marginBottom: '12px' }}>
            {restaurant.cuisineType} · {restaurant.city}
          </p>

          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13px', color: '#666', borderTop: '1px solid #f5f5f5', paddingTop: '12px' }}>
            <span>🕐 {restaurant.estimatedDeliveryMinutes} min</span>
            <span>🛵 {formatPrice(restaurant.deliveryFee)} delivery</span>
            <span>Min. {formatPrice(restaurant.minOrderAmount)}</span>
          </div>
        </div>
      </div>
    </Link>
  );
};

export default RestaurantCard;
