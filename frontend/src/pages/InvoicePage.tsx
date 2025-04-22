import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { FaCheck, FaEuroSign, FaFileInvoiceDollar } from 'react-icons/fa';
import axios from 'axios';
import {Invoice, InvoiceUpdateDTO} from "../types.ts";
import {toast} from "react-toastify";

const apiRequest = async <T,>(url: string, method: 'GET' | 'POST' | 'PUT' | 'DELETE', data?: InvoiceUpdateDTO): Promise<T> => {
    try {
        const response = await axios({
            method,
            url,
            data,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
        });
        return response.data;
    } catch (error) {
        console.error('API request failed:', error);
        throw error;
    }
};

const getCustomerInvoices = async (customerId: string): Promise<Invoice[]> => {
    return apiRequest<Invoice[]>(`/api/invoices/customer/${customerId}`, 'GET');
};

const getInvoiceById = async (invoiceId: string): Promise<Invoice> => {
    return apiRequest<Invoice>(`/api/invoices/${invoiceId}`, 'GET');
};

const updateInvoice = async (invoiceId: string, amountPaid: number): Promise<Invoice> => {
    const updateDTO: InvoiceUpdateDTO = { id: invoiceId, amountPaid };
    return apiRequest<Invoice>(`/api/invoices`, 'PUT', updateDTO);
};

const formatDate = (dateString: string) => {
    return dateString ? new Date(dateString).toLocaleDateString(undefined, {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    }) : '-';
};

export const InvoicePage = () => {
    const { customerId, invoiceId } = useParams<{ customerId: string; invoiceId?: string }>();
    const [invoices, setInvoices] = useState<Invoice[]>([]);
    const [selectedInvoice, setSelectedInvoice] = useState<Invoice | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchInvoices = async () => {
            try {
                if (!customerId) return;

                const data = await getCustomerInvoices(customerId);
                setInvoices(data);

                if (invoiceId) {
                    const invoice = data.find(i => i.id === invoiceId) || await getInvoiceById(invoiceId);
                    setSelectedInvoice(invoice);
                } else if (data.length > 0) {
                    setSelectedInvoice(data[0]);
                }
            } catch (err) {
                setError('Failed to load invoices');
                console.error(err);
            } finally {
                setLoading(false);
            }
        };

        fetchInvoices();
    }, [customerId, invoiceId]);

    const handleInvoiceSelect = (invoice: Invoice) => {
        setSelectedInvoice(invoice);
    };

    const handleMarkAsPaid = async () => {
        if (!selectedInvoice || selectedInvoice.isPaid) return;

        try {
            const partialUpdate = await updateInvoice(
                selectedInvoice.id,
                selectedInvoice.amountDue
            );

            const isPaid = partialUpdate.amountPaid === selectedInvoice.amountDue;

            const updatedInvoice: Invoice = {
                ...selectedInvoice,
                amountPaid: partialUpdate.amountPaid,
                isPaid: isPaid,
            };

            setInvoices(invoices.map(i =>
                i.id === updatedInvoice.id ? updatedInvoice : i
            ));
            setSelectedInvoice(updatedInvoice);

            toast.success('Invoice marked as paid successfully');
        } catch (err) {
            toast.error('Failed to mark invoice as paid');
            console.error(err);
        }
    };

    if (loading) return <div className="p-4">Loading...</div>;
    if (error) return <div className="p-4 text-red-500">{error}</div>;
    if (!invoices.length) return <div className="p-4">No invoices found</div>;

    return (
        <div className="container mx-auto p-4">
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* Invoice List */}
                <div className="lg:col-span-1 bg-white rounded-lg shadow p-4">
                    <h2 className="text-lg font-semibold mb-4 flex items-center gap-2">
                        <FaFileInvoiceDollar className="text-blue-600" />
                        Invoices
                    </h2>
                    <div className="space-y-2 max-h-[calc(100vh-200px)] overflow-y-auto">
                        {invoices.map(invoice => (
                            <button
                                key={invoice.id}
                                onClick={() => handleInvoiceSelect(invoice)}
                                className={`w-full text-left p-3 rounded transition-colors ${
                                    selectedInvoice?.id === invoice.id
                                        ? 'bg-blue-50 border border-blue-200'
                                        : 'hover:bg-gray-50 border border-transparent'
                                }`}
                            >
                                <div className="flex justify-between items-baseline">
                                    <div>
                                        <div className="font-medium text-gray-900">
                                            {formatDate(invoice.issueDate)}
                                        </div>
                                        <div className="text-sm text-gray-600 mt-1">
                                            <FaEuroSign className="inline mr-1" />
                                            {invoice.amountDue?.toFixed(2)}
                                        </div>
                                    </div>
                                    <span className={`px-2 py-1 text-xs rounded-full ${
                                        invoice.isPaid
                                            ? 'bg-green-100 text-green-800'
                                            : 'bg-yellow-100 text-yellow-800'
                                    }`}>
                                      {invoice.isPaid ? 'Paid' : 'Pending'}
                                    </span>
                                </div>
                            </button>
                        ))}
                    </div>
                </div>

                {/* Invoice Details */}
                {selectedInvoice && (
                    <div className="lg:col-span-2 bg-white rounded-lg shadow p-6">
                        <div className="flex justify-between items-start mb-6">
                            <div>
                                <h1 className="text-2xl font-bold">Invoice #{selectedInvoice.id.substring(0, 8)}</h1>
                                <p className="text-gray-600">
                                    Issued: {formatDate(selectedInvoice.issueDate)}
                                </p>
                                <p className="text-gray-600">
                                    Due: {formatDate(selectedInvoice.dueDate)}
                                </p>
                            </div>
                            <span className={`px-3 py-1 rounded-full text-sm font-medium ${
                                selectedInvoice.isPaid
                                    ? 'bg-green-100 text-green-800'
                                    : 'bg-yellow-100 text-yellow-800'
                            }`}>
                                {selectedInvoice.isPaid ? 'Paid' : 'Pending'}
                            </span>
                        </div>

                        <div className="grid grid-cols-2 gap-4 mb-8">
                            <div>
                                <h3 className="font-medium text-gray-700 mb-2">Customer ID</h3>
                                <p>{selectedInvoice.customerId}</p>
                            </div>
                            <div>
                                <h3 className="font-medium text-gray-700 mb-2">Subscription ID</h3>
                                <p>{selectedInvoice.subscriptionId}</p>
                            </div>
                        </div>

                        <div className="border-t border-b border-gray-200 py-6 my-6">
                            <div className="flex justify-between items-center mb-4">
                                <h3 className="font-medium text-gray-700">Amount Due</h3>
                                <p className="text-lg font-bold">
                                    <FaEuroSign className="inline mr-1" />
                                    {selectedInvoice.amountDue?.toFixed(2)}
                                </p>
                            </div>
                            <div className="flex justify-between items-center">
                                <h3 className="font-medium text-gray-700">Amount Paid</h3>
                                <p className="text-lg">
                                    <FaEuroSign className="inline mr-1" />
                                    {selectedInvoice.amountPaid?.toFixed(2)}
                                </p>
                            </div>
                        </div>

                        {!selectedInvoice.isPaid && (
                            <div className="mt-8">
                                <button
                                    onClick={handleMarkAsPaid}
                                    className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 flex items-center gap-2"
                                >
                                    <FaCheck /> Mark as Paid
                                </button>
                                <p className="mt-2 text-gray-600 text-sm">
                                    This will set the paid amount to {selectedInvoice.amountDue?.toFixed(2)}€
                                    and mark the invoice as paid.
                                </p>
                            </div>
                        )}

                        {selectedInvoice.isPaid && (
                            <div className="mt-6 p-4 bg-green-50 text-green-800 rounded-md">
                                <p>This invoice has been fully paid.</p>
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
};