import { ButtonHTMLAttributes, FC, ReactNode } from "react";

type ButtonVariant = "primary" | "secondary" | "red";

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
    variant?: ButtonVariant;
    children: ReactNode;
    handleClick?: () => void;
    isLoading?: boolean;
    loadingText?: string;
}

export const Button: FC<ButtonProps> = ({
                                            variant = "primary",
                                            children,
                                            handleClick,
                                            isLoading = false,
                                            loadingText = "Loading...",
                                            type = "button",
                                            className = "",
                                            disabled = false,
                                            ...props
                                        }) => {
    // Base styles (applied to all buttons)
    const baseStyles =
        "px-4 py-2 rounded-xl font-medium transition-colors duration-200 focus:outline-none focus:ring-2 disabled:opacity-70 disabled:cursor-not-allowed";

    // Variant-specific styles
    const variantStyles = {
        primary: "bg-blue-500 hover:bg-blue-600 text-white focus:ring-blue-400",
        secondary: "bg-gray-200 hover:bg-gray-300 text-gray-800 border border-gray-300 focus:ring-gray-400",
        red: "bg-red-500 hover:bg-red-600 text-white focus:ring-red-400",
    };

    return (
        <button
            onClick={handleClick}
            className={`${baseStyles} ${variantStyles[variant]} ${className}`}
            disabled={disabled || isLoading}
            type={type}
            {...props}
        >
            {isLoading ? (
                <span className="flex items-center justify-center gap-2">
                    <svg className="animate-spin h-5 w-5 text-current" fill="none" viewBox="0 0 24 24">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    {loadingText}
                </span>
            ) : (
                children
            )}
        </button>
    );
};