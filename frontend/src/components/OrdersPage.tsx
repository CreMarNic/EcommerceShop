import { Fragment, useEffect, useState } from 'react';
import {
    addCartItem,
    deleteOrder,
    getApiErrorMessage,
    getCurrentUserId,
    getOrders,
    type AuthenticatedUser,
    type Order,
} from '../api/ecommerceApi';
import { Header } from './Header';
import './OrdersPage.css';

type OrdersPageProps = {
    user?: AuthenticatedUser | null;
    onLogout: () => void;
};

export function OrdersPage({ user, onLogout }: OrdersPageProps) {
    const [orders, setOrders] = useState<Order[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        async function loadOrders() {
            try {
                setOrders(await getOrders(getCurrentUserId()));
            } catch (err) {
                setError(getApiErrorMessage(err, 'Could not load orders'));
            } finally {
                setLoading(false);
            }
        }

        loadOrders();
    }, []);

    async function handleBuyAgain(productId: number) {
        try {
            await addCartItem(getCurrentUserId(), productId, 1);
        } catch (err) {
            setError(getApiErrorMessage(err, 'Could not add product to cart'));
        }
    }

    async function handleDeleteOrder(orderId: number) {
        try {
            await deleteOrder(orderId);
            setOrders((currentOrders) => currentOrders.filter((order) => order.id !== orderId));
        } catch (err) {
            setError(getApiErrorMessage(err, 'Could not delete order'));
        }
    }

    return (
        <>
            <title>Orders</title>

            <Header user={user} onLogout={onLogout} />

            <div className="orders-page">
                <div className="page-title">Your Orders</div>
                {loading && <div>Loading orders...</div>}
                {error && <div>{error}</div>}

                {!loading && !error && <div className="orders-grid">
                    {orders.length === 0 && <div>You do not have any orders yet.</div>}
                    {orders.map((order) => (
                        <div key={order.id} className="order-container">
                            <div className="order-header">
                                <div className="order-header-left-section">
                                    <div className="order-date">
                                        <div className="order-header-label">Order Placed:</div>
                                        <div>{formatDate(order.createdAt)}</div>
                                    </div>
                                    <div className="order-total">
                                        <div className="order-header-label">Total:</div>
                                        <div>€{Number(order.total).toFixed(2)}</div>
                                    </div>
                                </div>

                                <div className="order-header-right-section">
                                    <div className="order-header-label">Order ID:</div>
                                    <div>{order.id}</div>
                                    <button
                                        className="delete-order-button button-secondary"
                                        onClick={() => handleDeleteOrder(order.id)}
                                    >
                                        Delete order
                                    </button>
                                </div>
                            </div>

                            <div className="order-details-grid">
                                {order.items.map((item) => (
                                    <Fragment key={item.id}>
                                        <div className="product-image-container">
                                            <img src={item.imageUrl}/>
                                        </div>

                                        <div className="product-details">
                                            <div className="product-name">{item.productName}</div>
                                            <div className="product-delivery-date">Status: {order.status}</div>
                                            <div className="product-quantity">Quantity: {item.quantity}</div>
                                            <button
                                                className="buy-again-button button-primary"
                                                onClick={() => handleBuyAgain(item.productId)}
                                            >
                                                <img className="buy-again-icon" src="images/icons/buy-again.png"/>
                                                <span className="buy-again-message">Add to Cart</span>
                                            </button>
                                        </div>

                                    </Fragment>
                                ))}
                            </div>
                        </div>
                    ))}
                </div>}
            </div>
        </>
    )
}

function formatDate(value: string) {
    return new Intl.DateTimeFormat('en-US', {
        month: 'long',
        day: 'numeric',
        year: 'numeric',
    }).format(new Date(value));
}
