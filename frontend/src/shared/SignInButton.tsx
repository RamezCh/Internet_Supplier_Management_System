import { FC } from "react";
import { FaGoogle, FaGithub } from "react-icons/fa";

interface SignInButtonProps {
    onClick: () => void;
    provider: "google" | "github";
}

const SignInButton: FC<SignInButtonProps> = ({ onClick, provider }) => {
    return (
        <button
            onClick={onClick}
            className="flex items-center gap-2 w-full p-3 text-white rounded-lg shadow-md transition hover:shadow-xl focus:outline-none hover:opacity-90 active:scale-95"
            style={{
                backgroundColor: provider === "google" ? "#DB4437" : "#24292E",
            }}
        >
            {provider === "google" ? <FaGoogle size={20} /> : <FaGithub size={20} />}
            Sign in with {provider.charAt(0).toUpperCase() + provider.slice(1)}
        </button>
    );
};

export default SignInButton;