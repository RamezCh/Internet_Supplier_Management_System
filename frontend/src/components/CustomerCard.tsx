import { Customer } from "../types";
import { FaEdit, FaTrash } from "react-icons/fa";
import { useNavigate } from "react-router-dom";

interface CustomerCardProps {
    customer: Customer;
    onDelete: (id: string) => void;
}

export const CustomerCard = ({ customer, onDelete }: CustomerCardProps) => {
    const navigate = useNavigate();

    const handleEdit = () => {
        navigate(`/customer/${customer.id}/edit`);
    };

    // Format address with only existing parts and proper commas
    const formatAddress = () => {
        if (!customer.address) return null;

        const { country, city, street, postalCode } = customer.address;
        const parts = [country, city, street, postalCode].filter(Boolean);
        return parts.join(', ');
    };

    // Only show fields that have content
    const shouldShowField = (value: string | undefined | null) => {
        return value !== undefined && value !== null && value !== '';
    };

    return (
        <div className="flex justify-between items-center w-full p-4 border border-gray-200 rounded-lg mb-3 bg-white shadow-sm hover:shadow-md transition-shadow duration-200">
            <div className="flex gap-6 flex-grow flex-wrap">
                {shouldShowField(customer.username) && (
                    <div className="flex gap-2 items-center min-w-[200px]">
                        <span className="font-medium text-gray-600">Username:</span>
                        <span className="text-gray-800">{customer.username}</span>
                    </div>
                )}

                {shouldShowField(customer.fullName) && (
                    <div className="flex gap-2 items-center min-w-[200px]">
                        <span className="font-medium text-gray-600">Name:</span>
                        <span className="text-gray-800">{customer.fullName}</span>
                    </div>
                )}

                {shouldShowField(customer.phone) && (
                    <div className="flex gap-2 items-center min-w-[200px]">
                        <span className="font-medium text-gray-600">Phone:</span>
                        <span className="text-gray-800">{customer.phone}</span>
                    </div>
                )}

                {formatAddress() && (
                    <div className="flex gap-2 items-center min-w-[200px]">
                        <span className="font-medium text-gray-600">Address:</span>
                        <span className="text-gray-800">{formatAddress()}</span>
                    </div>
                )}

                {shouldShowField(customer.status) && (
                    <div className="flex gap-2 items-center min-w-[200px]">
                        <span className="font-medium text-gray-600">Status:</span>
                        <span className={`px-2 py-1 text-xs rounded-full ${
                            customer.status === 'ACTIVE'
                                ? 'bg-green-100 text-green-800'
                                : 'bg-red-100 text-red-800'
                        }`}>
                            {customer.status}
                        </span>
                    </div>
                )}

                {shouldShowField(customer.registrationDate) && (
                    <div className="flex gap-2 items-center min-w-[200px]">
                        <span className="font-medium text-gray-600">Registered:</span>
                        <span className="text-gray-800">{customer.registrationDate}</span>
                    </div>
                )}

                {shouldShowField(customer.notes) && (
                    <div className="flex gap-2 items-center mt-2">
                        <span className="font-medium text-gray-600">Notes:</span>
                        <span className="text-gray-800 bg-gray-50 p-2 rounded w-full">
                            {customer.notes}
                        </span>
                    </div>
                )}
            </div>

            <div className="flex gap-3 ml-4">
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