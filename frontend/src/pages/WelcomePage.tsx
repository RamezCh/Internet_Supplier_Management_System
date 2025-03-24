import { FC } from "react";
import SignInButton from "../shared/SignInButton.tsx";

interface WelcomePageProps {
    onGoogleLogin: () => void;
    onGitHubLogin: () => void;
}

export const WelcomePage: FC<WelcomePageProps> = ({ onGoogleLogin, onGitHubLogin }) => {
    return (
        <div className="flex flex-col items-center justify-center min-h-screen p-6">
            <div className="bg-white shadow-lg rounded-lg p-8 max-w-lg text-center">
                <h1 className="text-2xl font-bold text-gray-800">Welcome to the Management System</h1>
                <p className="text-gray-600 mt-2">Internet suppliers, please log in to access your customers.</p>
                <div className="mt-6 space-y-4">
                    <SignInButton provider="google" onClick={onGoogleLogin} />
                    <SignInButton provider="github" onClick={onGitHubLogin} />
                </div>
            </div>
        </div>
    );
};