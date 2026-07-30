import { FormEvent, useState } from 'react';
import {
    authenticate,
    getApiErrorMessage,
    registerUser,
    type AuthenticatedUser,
} from '../api/ecommerceApi';
import './SignInPage.css';

type SignInPageProps = {
    onAuthenticated: (user: AuthenticatedUser) => void;
};

export function SignInPage({ onAuthenticated }: SignInPageProps) {
    const [mode, setMode] = useState<'sign-in' | 'register'>('sign-in');
    const [name, setName] = useState('');
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [submitting, setSubmitting] = useState(false);

    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();
        setError('');
        setSubmitting(true);

        try {
            if (mode === 'register') {
                await registerUser({
                    name,
                    email: username,
                    password,
                });
            }

            const user = await authenticate(username, password);
            onAuthenticated(user);
        } catch (err) {
            setError(getApiErrorMessage(
                err,
                mode === 'register' ? 'Could not register user' : 'Invalid username or password'
            ));
        } finally {
            setSubmitting(false);
        }
    }

    function toggleMode() {
        setMode(mode === 'sign-in' ? 'register' : 'sign-in');
        setError('');
    }

    return (
        <main className="sign-in-page">
            <form className="sign-in-form" onSubmit={handleSubmit}>
                <div className="sign-in-logo">EcommerceShop</div>
                <div className="sign-in-title">
                    {mode === 'register' ? 'Create your account' : 'Sign in to your account'}
                </div>

                {mode === 'register' && (
                    <label>
                        Name
                        <input
                            type="text"
                            value={name}
                            onChange={(event) => setName(event.target.value)}
                            autoComplete="name"
                            required
                        />
                    </label>
                )}

                <label>
                    Email
                    <input
                        type="email"
                        value={username}
                        onChange={(event) => setUsername(event.target.value)}
                        autoComplete="username"
                        required
                    />
                </label>

                <label>
                    Password
                    <input
                        type="password"
                        value={password}
                        onChange={(event) => setPassword(event.target.value)}
                        autoComplete="current-password"
                        required
                    />
                </label>

                {error && <div className="sign-in-error">{error}</div>}

                <button className="button-primary sign-in-button" disabled={submitting}>
                    {submitting
                        ? mode === 'register' ? 'Creating account...' : 'Signing in...'
                        : mode === 'register' ? 'Create account' : 'Sign in'}
                </button>

                <button
                    className="register-toggle-button"
                    type="button"
                    onClick={toggleMode}
                >
                    {mode === 'register'
                        ? 'Already have an account? Sign in'
                        : 'New to EcommerceShop? Register'}
                </button>
            </form>
        </main>
    );
}
