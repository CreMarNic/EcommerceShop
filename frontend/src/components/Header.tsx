import { Link } from 'react-router';
import type { AuthenticatedUser } from '../api/ecommerceApi';
import './Header.css';

type HeaderProps = {
    cartQuantity?: number;
    user?: AuthenticatedUser | null;
    onLogout: () => void;
};

export function Header({ cartQuantity = 0, user, onLogout }: HeaderProps) {
    return (

        <div className="header">
            <div className="left-section">
                <Link to="/" className="header-link">
                    <span className="logo">EcommerceShop</span>
                    <span className="mobile-logo">ES</span>
                </Link>
            </div>

            <div className="middle-section">
                <input className="search-bar" type="text" placeholder="Search"/>

                <button className="search-button">
                    <img className="search-icon" src="images/icons/search-icon.png"/>
                </button>
            </div>

            <div className="right-section">
                <div className="auth-summary">
                    <div className="auth-email">{user?.username ?? 'Unknown user'}</div>
                    <div className="auth-status">Authenticated</div>
                </div>

                <button className="logout-button" onClick={onLogout}>
                    Logout
                </button>

                <Link className="orders-link header-link" to="/orders">
                    <span className="orders-text">Orders</span>
                </Link>

                <Link className="cart-link header-link" to="/checkout">
                    <img className="cart-icon" src="images/icons/cart-icon.png"/>
                    <div className="cart-quantity">{cartQuantity}</div>
                    <div className="cart-text">Cart</div>
                </Link>
            </div>
        </div>


    )
}
