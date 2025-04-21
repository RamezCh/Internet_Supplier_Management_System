import { KeyboardEvent, useEffect, useState } from "react";
import axios from "axios";
import { toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import { FaChevronLeft, FaChevronRight, FaColumns, FaSort, FaSortUp, FaSortDown } from "react-icons/fa";

import { Customer } from "../types.ts";
import { CustomerCard } from "../components/CustomerCard.tsx";
import { Input } from "../shared/Input.tsx";
import { Button } from "../shared/Button.tsx";
import { RadioButton } from "../shared/RadioButton.tsx";

interface ApiResponse {
    content: Customer[];
    pageable: { pageNumber: number; pageSize: number };
    totalPages: number;
    totalElements: number;
    first: boolean;
    last: boolean;
}

type SortDirection = 'asc' | 'desc' | 'none';

interface ColumnVisibility {
    username: boolean;
    fullName: boolean;
    phone: boolean;
    address: boolean;
    status: boolean;
    registrationDate: boolean;
    notes: boolean;
}

export const Customers = () => {
    const [customers, setCustomers] = useState<Customer[]>([]);
    const [searchQuery, setSearchQuery] = useState<string>("");
    const [status, setStatus] = useState<string>("");
    const [isLoading, setIsLoading] = useState<boolean>(false);
    const [currentPage, setCurrentPage] = useState<number>(0);
    const [pageSize, setPageSize] = useState<number>(5);
    const [totalPages, setTotalPages] = useState<number>(1);
    const [showColumnMenu, setShowColumnMenu] = useState<boolean>(false);
    const [columnVisibility, setColumnVisibility] = useState<ColumnVisibility>({
        username: true,
        fullName: true,
        phone: true,
        address: true,
        status: true,
        registrationDate: true,
        notes: true,
    });
    const [sortDirection, setSortDirection] = useState<SortDirection>('none');

    const statusOptions = [
        { value: "", label: "All" },
        { value: "ACTIVE", label: "Active" },
        { value: "EXPIRING", label: "Expiring" },
        { value: "SUSPENDED", label: "Suspended" },
        { value: "EXPIRED", label: "Expired" },
        { value: "PENDING_ACTIVATION", label: "Pending Activation" },
    ];
    const pageSizeOptions = [5, 10, 15, 20];

    const fetchCustomers = async (
        page: number,
        size: number,
        direction: SortDirection,
        query: string = searchQuery,
        statusFilter: string = status
    ) => {
        setIsLoading(true);
        try {
            const params: Record<string, string> = { page: String(page), size: String(size) };
            if (direction !== 'none') {
                params.sort = `registrationDate,${direction}`;
            }

            const queryString = new URLSearchParams({
                ...params,
                ...(query && { searchTerm: query.trim() }),
                ...(statusFilter && { status: statusFilter }),
            }).toString();

            const url = (query || statusFilter)
                ? `/api/customers/search?${queryString}`
                : `/api/customers?${queryString}`;

            const { data } = await axios.get<ApiResponse>(url);
            setCustomers(data.content);
            setTotalPages(data.totalPages);
            setCurrentPage(page);
        } catch (error) {
            console.error("Error loading customers:", error);
            toast.error("Failed to load customers");
        } finally {
            setIsLoading(false);
        }
    };

    const toggleSort = () => {
        const next: SortDirection = sortDirection === 'asc' ? 'desc' : 'asc';
        setSortDirection(next);
        fetchCustomers(0, pageSize, next);
    };

    const resetFilters = () => {
        setSearchQuery("");
        setStatus("");
        setSortDirection('none');
        fetchCustomers(0, pageSize, 'none', '', '');
    };

    const handleDelete = async (id: string) => {
        try {
            await axios.delete(`/api/customers/${id}`);
            toast.success("Customer deleted successfully");
            fetchCustomers(currentPage, pageSize, sortDirection);
        } catch (error) {
            console.error("Error deleting customer:", error);
            toast.error("Failed to delete customer");
        }
    };

    const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            fetchCustomers(0, pageSize, sortDirection);
        }
    };

    const handlePageChange = (page: number) => {
        fetchCustomers(page, pageSize, sortDirection);
    };

    const handlePageSizeChange = (size: number) => {
        setPageSize(size);
        fetchCustomers(0, size, sortDirection);
    };

    const toggleColumnVisibility = (column: keyof ColumnVisibility) => {
        if (column === 'username' || column === 'fullName') return;
        setColumnVisibility(prev => ({ ...prev, [column]: !prev[column] }));
    };

    const getSortIcon = () => {
        switch (sortDirection) {
            case 'asc': return <FaSortUp className="ml-1" />;
            case 'desc': return <FaSortDown className="ml-1" />;
            default: return <FaSort className="ml-1 opacity-30" />;
        }
    };

    useEffect(() => {
        fetchCustomers(0, pageSize, sortDirection);
    }, []);

    return (
        <div className="flex flex-col gap-4 p-4 max-w-full">
            {/* Search and Actions */}
            <div className="flex flex-col sm:flex-row items-center mb-5 gap-4 w-full">
                <Input
                    label="Search Bar"
                    placeholder="Search by username, name, city..."
                    value={searchQuery}
                    onChange={e => setSearchQuery(e.target.value)}
                    onKeyDown={handleKeyDown}
                    containerClassName="flex-grow w-full sm:w-auto mb-0"
                />
                <Button
                    onClick={() => fetchCustomers(0, pageSize, sortDirection)}
                    className="h-[42px] w-full sm:w-auto"
                    disabled={isLoading}
                >
                    {isLoading ? "Loading..." : "Search"}
                </Button>
            </div>

            {/* Filters / Sort / Columns / PageSize */}
            <div className="flex flex-col lg:flex-row items-center justify-between mb-5 gap-4 w-full">
                <div className="flex flex-col sm:flex-row items-center gap-4 w-full sm:w-auto">
                    <RadioButton
                        name="customerStatus"
                        options={statusOptions}
                        selectedValue={status}
                        onChange={setStatus}
                        orientation="horizontal"
                        className="p-4 border rounded-lg w-full sm:w-auto"
                    />
                    <Button
                        onClick={resetFilters}
                        variant="red"
                        disabled={isLoading || (!searchQuery && !status && sortDirection === 'none')}
                        className="w-full sm:w-auto"
                    >
                        Reset
                    </Button>
                </div>

                <div className="flex items-center gap-4 w-full sm:w-auto justify-between sm:justify-normal">
                    <Button
                        onClick={toggleSort}
                        variant="secondary"
                        className="h-[42px] flex items-center"
                    >
                        Sort by Date{getSortIcon()}
                    </Button>

                    <div className="relative w-full sm:w-auto">
                        <Button
                            onClick={() => setShowColumnMenu(!showColumnMenu)}
                            variant="secondary"
                            className="h-[42px] flex items-center gap-2 w-full sm:w-auto"
                        >
                            <FaColumns /> Columns
                        </Button>
                        {showColumnMenu && (
                            <div className="absolute right-0 mt-2 w-full sm:w-48 bg-white rounded-md shadow-lg z-10 border border-gray-200">
                                <div className="p-2">
                                    <div className="flex items-center gap-2 p-1 opacity-50 cursor-not-allowed">
                                        <input type="checkbox" checked disabled className="rounded text-blue-600" /> <span>Username</span>
                                    </div>
                                    <div className="flex items-center gap-2 p-1 opacity-50 cursor-not-allowed">
                                        <input type="checkbox" checked disabled className="rounded text-blue-600" /> <span>Full Name</span>
                                    </div>
                                    {Object.entries(columnVisibility)
                                        .filter(([k]) => k !== 'username' && k !== 'fullName')
                                        .map(([key, visible]) => (
                                            <label key={key} className="flex items-center gap-2 p-1 hover:bg-gray-100 cursor-pointer">
                                                <input
                                                    type="checkbox"
                                                    checked={visible}
                                                    onChange={() => toggleColumnVisibility(key as keyof ColumnVisibility)}
                                                    className="rounded text-blue-600"
                                                />
                                                <span className="capitalize">{key.replace(/([A-Z])/g, ' $1').trim()}</span>
                                            </label>
                                        ))}
                                </div>
                            </div>
                        )}
                    </div>

                    <div className="flex items-center gap-2 w-full sm:w-auto">
                        <span className="text-sm text-gray-600">Items per page:</span>
                        <select
                            value={pageSize}
                            onChange={e => handlePageSizeChange(Number(e.target.value))}
                            className="border rounded-md p-1 text-sm w-full sm:w-auto"
                        >
                            {pageSizeOptions.map(size => <option key={size} value={size}>{size}</option>)}
                        </select>
                    </div>
                </div>
            </div>

            {/* List */}
            {isLoading ? (
                <div className="flex justify-center py-8">
                    <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
                </div>
            ) : (
                <>
                    <div className="grid gap-2">
                        {customers.length > 0 && (
                            <div className="flex justify-between items-start w-full p-6 border-b border-gray-200 bg-gray-50 rounded-t-lg">
                                <div className="flex flex-wrap gap-5 flex-grow">
                                    {/* Always visible columns */}
                                    <div className="min-w-[200px]">
                                        <span className="font-medium text-gray-600">Username</span>
                                    </div>
                                    <div className="min-w-[200px]">
                                        <span className="font-medium text-gray-600">Name</span>
                                    </div>

                                    {/* Conditionally visible columns */}
                                    {columnVisibility.phone && (
                                        <div className="min-w-[200px]">
                                            <span className="font-medium text-gray-600">Phone</span>
                                        </div>
                                    )}

                                    {columnVisibility.address && (
                                        <div className="min-w-[200px]">
                                            <span className="font-medium text-gray-600">Address</span>
                                        </div>
                                    )}

                                    {columnVisibility.status && (
                                        <div className="min-w-[200px]">
                                            <span className="font-medium text-gray-600">Status</span>
                                        </div>
                                    )}

                                    {columnVisibility.registrationDate && (
                                        <div className="min-w-[200px]">
                                            <span className="font-medium text-gray-600">Registered</span>
                                        </div>
                                    )}

                                    {columnVisibility.notes && (
                                        <div className="min-w-[200px]">
                                            <span className="font-medium text-gray-600">Notes</span>
                                        </div>
                                    )}
                                </div>

                                <div className="min-w-[120px] ml-4">
                                    <span className="font-medium text-gray-600">Actions</span>
                                </div>
                            </div>
                        )}
                        {customers.length ? (
                            customers.map(customer => (
                                <CustomerCard
                                    key={customer.username}
                                    customer={customer}
                                    onDelete={handleDelete}
                                    columnVisibility={columnVisibility}
                                />
                            ))
                        ) : (
                            <div className="text-center py-8 text-gray-500">No customers found matching your criteria</div>
                        )}
                    </div>

                    {/* Pagination */}
                    {customers.length > 0 && (
                        <div className="flex justify-center mt-6 gap-4">
                            <button
                                onClick={() => handlePageChange(currentPage - 1)}
                                disabled={currentPage === 0}
                                className="p-2 rounded-md disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-100"
                            ><FaChevronLeft className="w-4 h-4" /></button>

                            <span className="flex items-center">Page {currentPage + 1} of {totalPages}</span>

                            <button
                                onClick={() => handlePageChange(currentPage + 1)}
                                disabled={currentPage + 1 >= totalPages}
                                className="p-2 rounded-md disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-100"
                            ><FaChevronRight className="w-4 h-4" /></button>
                        </div>
                    )}
                </>
            )}
        </div>
    );
};
