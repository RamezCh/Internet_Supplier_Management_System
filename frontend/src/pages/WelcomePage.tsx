import SignInButton from "../shared/SignInButton.tsx";

export const WelcomePage = () => {

    function login() {
        // window.location.host is whats after www. and the port
        // e.g. localhost:5173
        // window.location.origin is complete url with port
        // e.g. http://localhost:5173
        // if url is same as localhost then we are in production
        // then our backend is at localhost:8080
        // if url isnt same as localhost then we are in development
        // then our backend is at url:8080
        const host = window.location.host === 'localhost:5173' ? 'http://localhost:8080' : window.location.origin
        window.open(host + '/oauth2/authorization/github', '_self')
    }

    return (
        <div className="flex flex-col items-center justify-center min-h-screen p-6">
            <div className="bg-white shadow-lg rounded-lg p-8 max-w-lg text-center">
                <h1 className="text-2xl font-bold text-gray-800">Welcome to the Management System</h1>
                <p className="text-gray-600 mt-2">Internet suppliers, please log in to access your customers.</p>
                <div className="mt-6 space-y-4">
                    <SignInButton provider="google" onClick={login} />
                    <SignInButton provider="github" onClick={login} />
                </div>
            </div>
        </div>
    );
};