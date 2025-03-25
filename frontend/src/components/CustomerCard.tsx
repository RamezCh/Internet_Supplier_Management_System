import { Customer } from "../types";
import { FaEdit, FaTrash } from "react-icons/fa";
import {useNavigate} from "react-router-dom";

interface CustomerCardProps {
    customer: Customer;
    onDelete: (username:string) => void;
}

export const CustomerCard = ({ customer, onDelete }: CustomerCardProps) => {
    const navigate = useNavigate();

    const handleEdit = () => {
        navigate(`/customer/${customer.username}/edit`);
    }

    return (
        <div className="flex justify-between items-center w-full p-4 border border-gray-200 rounded-lg mb-3 bg-white shadow-sm hover:shadow-md transition-shadow">
            <div className="flex gap-6 flex-grow flex-wrap">
                <div className="flex gap-2 items-center">
                    <span className="font-medium text-gray-600">Name:</span>
                    <span className="text-gray-800">{customer.fullName}</span>
                </div>
                <div className="flex gap-2 items-center">
                    <span className="font-medium text-gray-600">Notes:</span>
                    <span className="text-gray-800">{customer.notes}</span>
                </div>
            </div>
            <div className="flex gap-3">
                <button
                    onClick={handleEdit}
                    className="p-2 text-blue-600 hover:text-blue-800 hover:bg-blue-50 rounded-full transition-colors"
                    aria-label="Edit customer"
                >
                    <FaEdit className="w-5 h-5" />
                </button>
                <button
                    onClick={() => onDelete(customer.username)}
                    className="p-2 text-red-600 hover:text-red-800 hover:bg-red-50 rounded-full transition-colors"
                    aria-label="Delete customer"
                >
                    <FaTrash className="w-5 h-5" />
                </button>
            </div>
        </div>
    );
};