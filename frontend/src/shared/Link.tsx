import { NavLink, To } from 'react-router-dom';
import { ReactNode } from 'react';

interface LinkProps {
    to: To;
    children: ReactNode;
}

export const Link = ({
                         to,
                         children
                     }: LinkProps) => {
    return (
        <NavLink
            to={to}
            className={({ isActive }) =>
                `px-4 py-3 rounded-lg text-lg font-semibold transition-all duration-200 ${
                    isActive
                        ? 'bg-blue-50 text-blue-700 border-b-2 border-blue-500'
                        : 'text-gray-700 hover:text-blue-600 hover:bg-blue-50'
                }`
            }
        >
            {children}
        </NavLink>
    );
};