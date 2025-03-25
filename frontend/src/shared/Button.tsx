import {ButtonHTMLAttributes, FC, ReactNode} from "react";

type ButtonVariant = "primary" | "secondary" | "red";

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
    variant?: ButtonVariant;
    children: ReactNode;
    handleClick: () => void
}

export const Button: FC<ButtonProps> = ({
                                           variant = "primary",
                                           children,
                                            handleClick
                                       }) => {
    // Base styles (applied to all buttons)
    const baseStyles =
        "px-4 py-2 rounded-xl font-medium transition-colors duration-200 focus:outline-none focus:ring-2";

    // Variant-specific styles
    const variantStyles = {
        primary: "bg-blue-500 hover:bg-blue-600 text-white focus:ring-blue-400",
        secondary:
            "bg-gray-200 hover:bg-gray-300 text-gray-800 border border-gray-300 focus:ring-gray-400",
        red: "bg-red-500 hover:bg-red-600 text-white focus:ring-red-400",
    };

    return (
        <button
            onClick={handleClick}
            className={`${baseStyles} ${variantStyles[variant]}`}
        >
            {children}
        </button>
    );
};