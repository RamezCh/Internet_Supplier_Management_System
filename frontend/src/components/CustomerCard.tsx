import { Customer } from "../types";
import {FaEdit, FaReceipt, FaTrash} from "react-icons/fa";
import { useNavigate } from "react-router-dom";

interface CustomerCardProps {
    customer: Customer;
    onDelete: (id: string) => void;
    columnVisibility: {
        username: boolean;
        fullName: boolean;
        phone: boolean;
        address: boolean;
        status: boolean;
        registrationDate: boolean;
        notes: boolean;
    };
}

export const CustomerCard = ({ customer, onDelete, columnVisibility }: CustomerCardProps) => {
    const navigate = useNavigate();

    const handleEdit = () => {
        navigate(`/customer/${customer.id}/edit`);
    };

    const handleViewSubscription = () => {
        navigate(`/customer/subscription/${customer.id}`);
    };

    const formatAddress = () => {
        const { country, city, street, postalCode } = customer.address;
        const parts = [country, city, street, postalCode].filter(Boolean);
        return parts.join(', ');
    };

    const getStatusStyles = (status: string) => {
        switch (status) {
            case 'ACTIVE':
                return 'bg-green-100 text-green-800';
            case 'EXPIRED':
            case 'SUSPENDED':
                return 'bg-red-100 text-red-800';
            default:
                return 'bg-yellow-100 text-yellow-800';
        }
    };

    return (
        <div className="flex justify-between items-start w-full p-4 border border-gray-200 rounded-lg mb-3 bg-white shadow-sm hover:shadow-md transition-shadow duration-200">
            <div className="flex flex-wrap gap-4 flex-grow">
                {/* Always visible columns */}
                <div className="flex gap-2 items-center min-w-[200px]">
                    <span className="font-medium text-gray-600">Username:</span>
                    <span className="text-gray-800">{customer.username || '-'}</span>
                </div>

                <div className="flex gap-2 items-center min-w-[200px]">
                    <span className="font-medium text-gray-600">Name:</span>
                    <span className="text-gray-800">{customer.fullName || '-'}</span>
                </div>

                {/* Conditionally visible columns */}
                {columnVisibility.phone && (
                    <div className="flex gap-2 items-center min-w-[200px]">
                        <span className="font-medium text-gray-600">Phone:</span>
                        <span className="text-gray-800">{customer.phone || '-'}</span>
                    </div>
                )}

                {columnVisibility.address && formatAddress() && (
                    <div className="flex gap-2 items-center min-w-[200px]">
                        <span className="font-medium text-gray-600">Address:</span>
                        <span className="text-gray-800">{formatAddress()}</span>
                    </div>
                )}

                {columnVisibility.status && (
                    <div className="flex gap-2 items-center min-w-[200px]">
                        <span className="font-medium text-gray-600">Status:</span>
                        <span className={`px-2 py-1 text-xs rounded-full ${getStatusStyles(customer.status)}`}>
                        {customer.status || '-'}
                        </span>
                    </div>
                )}

                {columnVisibility.registrationDate && (
                    <div className="flex gap-2 items-center min-w-[200px]">
                        <span className="font-medium text-gray-600">Registered:</span>
                        <span className="text-gray-800">{new Date(customer.registrationDate).toLocaleString(undefined, {
                            year: "numeric",
                            month: "short",
                            day: "numeric",
                            hour: "2-digit",
                            minute: "2-digit",
                        }) || '-'}</span>
                    </div>
                )}

                {columnVisibility.notes && customer.notes && (
                    <div className="flex gap-2 items-center min-w-[200px]">
                        <span className="font-medium text-gray-600">Notes:</span>
                        <span className="text-gray-800 max-w-xs truncate">
                            {customer.notes}
                        </span>
                    </div>
                )}
            </div>

            <div className="flex gap-3 ml-4">
                <button
                    onClick={handleViewSubscription}
                    className="p-2 text-blue-600 hover:text-blue-800 hover:bg-blue-50 rounded-full transition-colors duration-200"
                    aria-label="View Subscription"
                >
                    <FaReceipt className="w-5 h-5" />
                </button>
                <button
                    onClick={handleEdit}
                    className="p-2 text-blue-600 hover:text-blue-800 hover:bg-blue-50 rounded-full transition-colors duration-200"
                    aria-label="Edit customer"
                >
                    <FaEdit className="w-5 h-5" />
                </button>
                <button
                    onClick={() => onDelete(customer.id)}
                    className="p-2 text-red-600 hover:text-red-800 hover:bg-red-50 rounded-full transition-colors duration-200"
                    aria-label="Delete customer"
                >
                    <FaTrash className="w-5 h-5" />
                </button>
            </div>
        </div>
    );
};