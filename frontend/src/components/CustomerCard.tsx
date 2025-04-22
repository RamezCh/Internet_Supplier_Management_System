import { Customer } from "../types";
import { FaEdit, FaReceipt, FaTrash } from "react-icons/fa";
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
    const handleEdit = () => navigate(`/customer/${customer.id}/edit`);
    const handleViewSubscription = () => navigate(`/customer/subscription/${customer.id}`);
    const formatAddress = () => {
        const { country, city, street, postalCode } = customer.address;
        return [country, city, street, postalCode].filter(Boolean).join(', ');
    };
    const getStatusStyles = (status: string) => {
        switch (status) {
            case 'ACTIVE': return 'bg-green-100 text-green-800';
            case 'EXPIRED':
            case 'SUSPENDED': return 'bg-red-100 text-red-800';
            default: return 'bg-yellow-100 text-yellow-800';
        }
    };

    return (
        <div className="flex justify-between items-start w-full p-4 border border-gray-200 rounded-lg bg-white hover:shadow-sm transition-shadow duration-200">
            <div className="flex gap-4 flex-grow">
                <div className="min-w-[200px]"><span className="text-gray-800">{customer.username || '-'}</span></div>
                <div className="min-w-[200px]"><span className="text-gray-800">{customer.fullName || '-'}</span></div>

                <div className={`min-w-[200px] ${columnVisibility.phone ? '' : 'invisible'}`}><span className="text-gray-800">{customer.phone || '-'}</span></div>
                <div className={`min-w-[200px] ${columnVisibility.address ? '' : 'invisible'}`}><span className="text-gray-800">{formatAddress() || '-'}</span></div>
                <div className={`min-w-[200px] ${columnVisibility.status ? '' : 'invisible'}`}><span className={`px-2 py-1 text-xs rounded-full ${getStatusStyles(customer.status)}`}>{customer.status || '-'}</span></div>
                <div className={`min-w-[200px] ${columnVisibility.registrationDate ? '' : 'invisible'}`}><span className="text-gray-800">{customer.registrationDate
                    ? new Date(customer.registrationDate).toLocaleString(undefined, {
                        year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit',
                    }) : '-'}</span></div>
                <div className={`min-w-[200px] ${columnVisibility.notes ? '' : 'invisible'}`}><span className="text-gray-800 truncate">{customer.notes || '-'}</span></div>
            </div>

            <div className="flex gap-3 ml-4">
                <button onClick={handleViewSubscription} className="p-2 text-blue-600 hover:text-blue-800 hover:bg-blue-50 rounded-full transition-colors duration-200" aria-label="View Subscription"><FaReceipt className="w-5 h-5" /></button>
                <button onClick={handleEdit} className="p-2 text-green-600 hover:text-green-800 hover:bg-green-50 rounded-full transition-colors duration-200" aria-label="Edit customer"><FaEdit className="w-5 h-5" /></button>
                <button onClick={() => onDelete(customer.id)} className="p-2 text-red-600 hover:text-red-800 hover:bg-red-50 rounded-full transition-colors duration-200" aria-label="Delete customer"><FaTrash className="w-5 h-5" /></button>
            </div>
        </div>
    );
};