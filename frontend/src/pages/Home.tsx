import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Restaurant, Category } from '../types';
import { restaurantService } from '../services/restaurantService';
import RestaurantCard from '../components/restaurant/RestaurantCard';
import LoadingSpinner from '../components/common/LoadingSpinner';
import Footer from '../components/common/Footer';
import CartSidebar from '../components/cart/CartSidebar';

const DEFAULT_CATEGORIES: Category[] = [
  { id: 1, name: 'Indian', icon: '🍛' },
  { id: 2, name: 'Chinese', icon: '🍜' },
  { id: 3, name: 'Italian', icon: '🍝' },
  { id: 4, name: 'Pizza', icon: '🍕' },
  { id: 5, name: 'Burgers', icon: '🍔' },
  { id: 6, name: 'Biryani', icon: '🍚' },
];

const Home: React.FC = () => {
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState('');
  const [restaurants, setRestaurants] = useState<Restaurant[]>([]);
  const [categories, setCategories] = useState<Category[]>(DEFAULT_CATEGORIES);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [restaurantData, categoryData] = await Promise.allSettled([
          restaurantService.getActive(),
          restaurantService.getCategories(),
        ]);
        if (restaurantData.status === 'fulfilled') {
          setRestaurants(restaurantData.value.slice(0, 6));
        }
        if (categoryData.status === 'fulfilled' && categoryData.value.length > 0) {
          setCategories(categoryData.value);
        }
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/search?q=${encodeURIComponent(searchQuery.trim())}`);
    }
  };

  return (
    <div>
      <CartSidebar />

      {/* Hero */}
      <div
        style={{
          background: 'linear-gradient(135deg, #1A1A2E 0%, #FF6B35 100%)',
          padding: '80px 24px',
          textAlign: 'center',
        }}
      >
        <h1
          style={{
            fontSize: '42px',
            fontWeight: 800,
            color: 'white',
            marginBottom: '12px',
            lineHeight: 1.2,
          }}
        >
          What are you craving today?
        </h1>
        <p
          style={{
            fontSize: '18px',
            color: 'rgba(255,255,255,0.85)',
            marginBottom: '36px',
          }}
        >
          Order from your favourite restaurants — delivered swift to your door
        </p>
        <form
          onSubmit={handleSearch}
          style={{
            display: 'flex',
            maxWidth: '560px',
            margin: '0 auto',
            gap: '0',
            boxShadow: '0 8px 32px rgba(0,0,0,0.2)',
            borderRadius: '50px',
            overflow: 'hidden',
          }}
        >
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search for restaurants or cuisines..."
            style={{
              flex: 1,
              padding: '18px 24px',
              fontSize: '16px',
              border: 'none',
              outline: 'none',
              background: 'white',
            }}
          />
          <button
            type="submit"
            style={{
              padding: '18px 32px',
              background: '#FF6B35',
              color: 'white',
              border: 'none',
              fontSize: '16px',
              fontWeight: 700,
              cursor: 'pointer',
              whiteSpace: 'nowrap',
            }}
          >
            Search 🔍
          </button>
        </form>
      </div>

      {/* Categories */}
      <div
        style={{
          background: 'white',
          padding: '32px 24px',
          borderBottom: '1px solid #E5E5E5',
        }}
      >
        <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
          <h2
            style={{
              fontSize: '18px',
              fontWeight: 700,
              marginBottom: '20px',
              color: '#1A1A2E',
            }}
          >
            Browse by Category
          </h2>
          <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
            {categories.map((cat) => (
              <button
                key={cat.id}
                onClick={() =>
                  navigate(`/search?cuisine=${encodeURIComponent(cat.name)}`)
                }
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px',
                  padding: '10px 20px',
                  borderRadius: '50px',
                  border: '1.5px solid #E5E5E5',
                  background: 'white',
                  fontSize: '14px',
                  fontWeight: 600,
                  color: '#1A1A2E',
                  cursor: 'pointer',
                  transition: 'all 0.2s',
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.borderColor = '#FF6B35';
                  e.currentTarget.style.background = '#FFF3EE';
                  e.currentTarget.style.color = '#FF6B35';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.borderColor = '#E5E5E5';
                  e.currentTarget.style.background = 'white';
                  e.currentTarget.style.color = '#1A1A2E';
                }}
              >
                <span style={{ fontSize: '20px' }}>{cat.icon}</span>
                {cat.name}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Popular Restaurants */}
      <div
        style={{ padding: '48px 24px', maxWidth: '1200px', margin: '0 auto' }}
      >
        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            marginBottom: '28px',
          }}
        >
          <h2 style={{ fontSize: '24px', fontWeight: 700, color: '#1A1A2E' }}>
            Popular Restaurants
          </h2>
          <button
            onClick={() => navigate('/restaurants')}
            style={{
              background: 'none',
              border: 'none',
              color: '#FF6B35',
              fontWeight: 600,
              cursor: 'pointer',
              fontSize: '14px',
            }}
          >
            View all →
          </button>
        </div>

        {loading ? (
          <LoadingSpinner />
        ) : restaurants.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">🏪</div>
            <h3>No restaurants available</h3>
            <p>Check back soon for new restaurants!</p>
          </div>
        ) : (
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))',
              gap: '24px',
            }}
          >
            {restaurants.map((r) => (
              <RestaurantCard key={r.id} restaurant={r} />
            ))}
          </div>
        )}
      </div>

      {/* How it works */}
      <div style={{ background: '#FFF3EE', padding: '64px 24px' }}>
        <div
          style={{
            maxWidth: '1200px',
            margin: '0 auto',
            textAlign: 'center',
          }}
        >
          <h2
            style={{
              fontSize: '28px',
              fontWeight: 700,
              color: '#1A1A2E',
              marginBottom: '48px',
            }}
          >
            How SwiftBite Works
          </h2>
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
              gap: '32px',
            }}
          >
            {[
              {
                icon: '🔍',
                title: 'Browse',
                desc: 'Explore restaurants and cuisines near you',
              },
              {
                icon: '🍽️',
                title: 'Order',
                desc: 'Choose your favourite dishes and add to cart',
              },
              {
                icon: '💳',
                title: 'Pay',
                desc: 'Secure payment with multiple options',
              },
              {
                icon: '🛵',
                title: 'Enjoy',
                desc: 'Fast delivery right to your doorstep',
              },
            ].map((step) => (
              <div key={step.title} style={{ textAlign: 'center' }}>
                <div
                  style={{
                    width: '72px',
                    height: '72px',
                    borderRadius: '50%',
                    background: 'white',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    margin: '0 auto 16px',
                    fontSize: '32px',
                    boxShadow: '0 4px 16px rgba(255,107,53,0.15)',
                  }}
                >
                  {step.icon}
                </div>
                <h3
                  style={{
                    fontSize: '16px',
                    fontWeight: 700,
                    marginBottom: '8px',
                    color: '#1A1A2E',
                  }}
                >
                  {step.title}
                </h3>
                <p
                  style={{ fontSize: '14px', color: '#666', lineHeight: 1.5 }}
                >
                  {step.desc}
                </p>
              </div>
            ))}
          </div>
        </div>
      </div>

      <Footer />
    </div>
  );
};

export default Home;
