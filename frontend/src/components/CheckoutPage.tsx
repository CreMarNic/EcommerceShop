import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    checkoutCart,
    getApiErrorMessage,
    getCart,
    getCurrentUserId,
    removeCartItem,
    type AuthenticatedUser,
    type Cart,
} from '../api/ecommerceApi';
import './CheckoutPage.css';

type CheckoutPageProps = {
    user?: AuthenticatedUser | null;
    onLogout: () => void;
};

export function CheckoutPage({ user, onLogout }: CheckoutPageProps) {
    const navigate = useNavigate();
    const [cart, setCart] = useState<Cart | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        loadCart();
    }, []);

    async function loadCart() {
        try {
            setCart(await getCart(getCurrentUserId()));
        } catch (err) {
            setError(getApiErrorMessage(err, 'Could not load cart'));
        } finally {
            setLoading(false);
        }
    }

    async function handleRemoveItem(productId: number) {
        try {
            setCart(await removeCartItem(getCurrentUserId(), productId));
        } catch (err) {
            setError(getApiErrorMessage(err, 'Could not remove cart item'));
        }
    }

    async function handleCheckout() {
        try {
            const userId = getCurrentUserId();
            await checkoutCart(userId);
            navigate('/orders');
        } catch (err) {
            setError(getApiErrorMessage(err, 'Could not place order'));
        }
    }

    const itemCount = cart?.items.reduce((total, item) => total + item.quantity, 0) ?? 0;
    const itemTotal = Number(cart?.total ?? 0);
    const shipping = itemCount > 0 ? 4.99 : 0;
    const totalBeforeTax = itemTotal + shipping;
    const tax = totalBeforeTax * 0.1;
    const orderTotal = totalBeforeTax + tax;

    return (
        <>
            <title>Checkout</title>

            <div className="checkout-header">
                <div className="header-content">
                    <div className="checkout-header-left-section">
                        <a href="/">
                            <span className="logo">EcommerceShop</span>
                            <span className="mobile-logo">ES</span>
                        </a>
                    </div>

                    <div className="checkout-header-middle-section">
                        Checkout (<a className="return-to-home-link"
                                     href="/">{itemCount} items</a>)
                    </div>

                    <div className="checkout-header-right-section">
                        <div className="auth-summary">
                            <div className="auth-email">{user?.username ?? 'Unknown user'}</div>
                            <div className="auth-status">Authenticated</div>
                        </div>
                        <button className="logout-button" onClick={onLogout}>
                            Logout
                        </button>
                        <img src="images/icons/checkout-lock-icon.png"/>
                    </div>
                </div>
            </div>

            <div className="checkout-page">
                <div className="page-title">Review your order</div>
                {loading && <div>Loading cart...</div>}
                {error && <div>{error}</div>}

                {!loading && !error && <div className="checkout-grid">
                    <div className="order-summary">
                        {cart?.items.length === 0 && <div>Your cart is empty.</div>}
                        {cart?.items.map((item) => (
                            <div key={item.id} className="cart-item-container">
                                <div className="delivery-date">
                                    Delivery date: Tuesday, June 21
                                </div>

                                <div className="cart-item-details-grid">
                                    <img className="product-image" src={item.imageUrl}/>

                                    <div className="cart-item-details">
                                        <div className="product-name">{item.productName}</div>
                                        <div className="product-price">€{Number(item.price).toFixed(2)}</div>
                                        <div className="product-quantity">
                                            <span>
                                                Quantity: <span className="quantity-label">{item.quantity}</span>
                                            </span>
                                            <span
                                                className="delete-quantity-link link-primary"
                                                onClick={() => handleRemoveItem(item.productId)}
                                            >
                                                Delete
                                            </span>
                                        </div>
                                    </div>

                                    <div className="delivery-options">
                                        <div className="delivery-options-title">
                                            Choose a delivery option:
                                        </div>
                                        <div className="delivery-option">
                                            <input type="radio" checked readOnly className="delivery-option-input"/>
                                            <div>
                                                <div className="delivery-option-date">Tuesday, June 21</div>
                                                <div className="delivery-option-price">FREE Shipping</div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>

                    <div className="payment-summary">
                        <div className="payment-summary-title">
                            Payment Summary
                        </div>

                        <div className="payment-summary-row">
                            <div>Items ({itemCount}):</div>
                            <div className="payment-summary-money">€{itemTotal.toFixed(2)}</div>
                        </div>

                        <div className="payment-summary-row">
                            <div>Shipping &amp; handling:</div>
                            <div className="payment-summary-money">€{shipping.toFixed(2)}</div>
                        </div>

                        <div className="payment-summary-row subtotal-row">
                            <div>Total before tax:</div>
                            <div className="payment-summary-money">€{totalBeforeTax.toFixed(2)}</div>
                        </div>

                        <div className="payment-summary-row">
                            <div>Estimated tax (10%):</div>
                            <div className="payment-summary-money">€{tax.toFixed(2)}</div>
                        </div>

                        <div className="payment-summary-row total-row">
                            <div>Order total:</div>
                            <div className="payment-summary-money">€{orderTotal.toFixed(2)}</div>
                        </div>

                        <button
                            className="place-order-button button-primary"
                            onClick={handleCheckout}
                            disabled={itemCount === 0}
                        >
                            Place your order
                        </button>
                    </div>
                </div>}
            </div>
        </>
    )
}
