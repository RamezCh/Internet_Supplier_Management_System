import { Customer } from "../types";
import {FaEdit, FaShoppingBag, FaFileInvoiceDollar, FaTrash} from "react-icons/fa";
import { useNavigate } from "react-router-dom";
import {ReactNode} from "react";

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

const getStatusStyles = (status: string) => {
    switch (status) {
        case 'ACTIVE': return 'bg-green-100 text-green-800';
        case 'EXPIRED': return 'bg-gray-100 text-gray-800';
        case 'SUSPENDED': return 'bg-red-100 text-red-800';
        case 'EXPIRING': return 'bg-orange-100 text-orange-800';
        default: return 'bg-yellow-100 text-yellow-800';
    }
};

const truncateText = (text: string, maxLength = 24) => {
    if (!text) return '-';
    return text.length > maxLength ? `${text.substring(0, maxLength)}...` : text;
};

interface ResponsiveTableCellProps {
    content: string | ReactNode;
    visible?: boolean;
    isStatus?: boolean;
    label?: string;
}

const ResponsiveTableCell = ({
                                 content,
                                 visible = true,
                                 isStatus = false,
                                 label = ""
                             }: ResponsiveTableCellProps) => {
    if (!visible) return null;

    const contentString = typeof content === 'string' ? content : '';
    const truncatedContent = isStatus ? content : truncateText(contentString);

    return (
        <div className="relative group p-1 sm:p-0 w-[180px]">
            <div className="md:hidden font-medium text-gray-500 text-xs mb-1">
                {label}:
            </div>
            {isStatus ? (
                <span className={`px-2 py-1 text-xs rounded-full ${getStatusStyles(contentString)}`}>
                    {content}
                </span>
            ) : (
                <>
                    <span className="text-gray-800 truncate block w-full" title={contentString}>
                        {truncatedContent}
                    </span>
                    <div className="absolute z-10 hidden group-hover:block bg-gray-800 text-white text-xs rounded p-1 bottom-full mb-1 whitespace-normal max-w-xs break-words">
                        {content}
                    </div>
                </>
            )}
        </div>
    );
};

interface ActionButtonsProps {
    handleViewSubscription: () => void;
    handleViewInvoices: () => void;
    handleEdit: () => void;
    handleDelete: () => void;
}

const ActionButtons = ({
                           handleViewSubscription,
                           handleViewInvoices,
                           handleEdit,
                           handleDelete
                       }: ActionButtonsProps) => (
    <div className="flex gap-3">
        <button
            onClick={handleViewSubscription}
            className="p-2 text-blue-600 hover:text-blue-800 hover:bg-blue-50 rounded-full transition-colors duration-200"
            aria-label="View Subscription"
        >
            <FaShoppingBag className="w-4 h-4" />
        </button>
        <button
            onClick={handleViewInvoices}
            className="p-2 text-purple-600 hover:text-purple-800 hover:bg-purple-50 rounded-full transition-colors duration-200"
            aria-label="View Invoices"
        >
            <FaFileInvoiceDollar className="w-4 h-4" />
        </button>
        <button
            onClick={handleEdit}
            className="p-2 text-green-600 hover:text-green-800 hover:bg-green-50 rounded-full transition-colors duration-200"
            aria-label="Edit customer"
        >
            <FaEdit className="w-4 h-4" />
        </button>
        <button
            onClick={handleDelete}
            className="p-2 text-red-600 hover:text-red-800 hover:bg-red-50 rounded-full transition-colors duration-200"
            aria-label="Delete customer"
        >
            <FaTrash className="w-4 h-4" />
        </button>
    </div>
);

export const CustomerCard = ({ customer, onDelete, columnVisibility }: CustomerCardProps) => {
    const navigate = useNavigate();
    const handleEdit = () => navigate(`/customer/${customer.id}/edit`);
    const handleViewSubscription = () => navigate(`/customer/subscription/${customer.id}`);
    const handleViewInvoices = () => navigate(`/customer/${customer.id}/invoices`);
    const handleDelete = () => onDelete(customer.id);

    const formatAddress = () => {
        const { country, city, street, postalCode } = customer.address;
        return [country, city, street, postalCode].filter(Boolean).join(', ');
    };

    const formatDate = (dateString: string) => {
        return dateString ? new Date(dateString).toLocaleString(undefined, {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
        }) : '-';
    };

    const fields = [
        { key: 'username', value: customer.username, label: 'Username' },
        { key: 'fullName', value: customer.fullName, label: 'Full Name' },
        { key: 'phone', value: customer.phone, label: 'Phone' },
        { key: 'address', value: formatAddress(), label: 'Address' },
        { key: 'status', value: customer.status, label: 'Status', isStatus: true },
        { key: 'registrationDate', value: formatDate(customer.registrationDate), label: 'Registered' },
        { key: 'notes', value: customer.notes, label: 'Notes' },
    ];

    return (
        <div className="w-full p-4 border border-gray-200 rounded-lg bg-white hover:shadow-sm transition-shadow duration-200 mb-3">
            {/* Mobile view - stacked layout */}
            <div className="md:hidden grid grid-cols-1 gap-3">
                {fields.map((field) => (
                    columnVisibility[field.key as keyof typeof columnVisibility] && (
                        <ResponsiveTableCell
                            key={field.key}
                            content={field.value}
                            label={field.label}
                            isStatus={field.isStatus}
                        />
                    )
                ))}
                <div className="flex justify-end gap-3 mt-3">
                    <ActionButtons
                        handleViewSubscription={handleViewSubscription}
                        handleViewInvoices={handleViewInvoices}
                        handleEdit={handleEdit}
                        handleDelete={handleDelete}
                    />
                </div>
            </div>

            {/* Desktop view - table row layout */}
            <div className="hidden md:flex items-center w-full">
                <div className="flex gap-8 flex-grow overflow-hidden">
                    {fields.map((field) => (
                        <ResponsiveTableCell
                            key={field.key}
                            content={field.value}
                            visible={columnVisibility[field.key as keyof typeof columnVisibility]}
                            isStatus={field.isStatus}
                            label={field.label}
                        />
                    ))}
                </div>
                <div className="flex gap-3 ml-4 shrink-0">
                    <ActionButtons
                        handleViewSubscription={handleViewSubscription}
                        handleViewInvoices={handleViewInvoices}
                        handleEdit={handleEdit}
                        handleDelete={handleDelete}
                    />
                </div>
            </div>
        </div>
    );
};