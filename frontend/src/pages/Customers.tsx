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
    pageable: {
        pageNumber: number;
        pageSize: number;
    };
    totalPages: number;
    totalElements: number;
    first: boolean;
    last: boolean;
}

interface ApiParams {
    page: number;
    size: number;
    sort?: string;
}

interface ColumnVisibility {
    username: boolean;
    fullName: boolean;
    phone: boolean;
    address: boolean;
    status: boolean;
    registrationDate: boolean;
    notes: boolean;
}

type SortDirection = 'asc' | 'desc' | 'none';

interface StatusOption {
    value: string;
    label: string;
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

    const statusOptions: StatusOption[] = [
        { value: "", label: "All" },
        { value: "ACTIVE", label: "Active" },
        { value: "EXPIRING", label: "Expiring" },
        { value: "SUSPENDED", label: "Suspended" },
        { value: "EXPIRED", label: "Expired" },
        { value: "PENDING_ACTIVATION", label: "Pending Activation" },
    ];

    const pageSizeOptions: number[] = [5, 10, 15, 20];

    const getCustomers = async (page: number = currentPage, size: number = pageSize) => {
        setIsLoading(true);
        try {
            const params: ApiParams = {
                page,
                size,
            };

            if (sortDirection !== 'none') {
                params.sort = `registrationDate,${sortDirection}`;
            }

            const response = await axios.get<ApiResponse>("/api/customers", { params });
            setCustomers(response.data.content);
            setTotalPages(response.data.totalPages);
        } catch (error) {
            console.error("Error fetching customers:", error);
            toast.error("Failed to load customers");
        } finally {
            setIsLoading(false);
        }
    };

    const searchCustomers = async (page: number = 0, size: number = pageSize) => {
        setIsLoading(true);
        try {
            const params = new URLSearchParams({
                page: page.toString(),
                size: size.toString(),
            });

            if (searchQuery) {
                params.append("searchTerm", searchQuery.trim());
            }

            if (status) {
                params.append("status", status);
            }

            if (sortDirection !== 'none') {
                params.append("sort", `registrationDate,${sortDirection}`);
            }

            const response = await axios.get<ApiResponse>(
                `/api/customers/search?${params.toString()}`
            );
            setCustomers(response.data.content);
            setCurrentPage(page);
            setTotalPages(response.data.totalPages);
        } catch (error) {
            console.error("Error searching customers:", error);
            toast.error("Failed to load customers");
        } finally {
            setIsLoading(false);
        }
    };

    const getNextSortDirection = (current: SortDirection): SortDirection => {
        const order: Record<SortDirection, SortDirection> = {
            'none': 'desc',
            'desc': 'asc',
            'asc': 'none'
        };
        return order[current];
    };

    const toggleSort = () => {
        const newDirection = getNextSortDirection(sortDirection);
        setSortDirection(newDirection);

        if (searchQuery || status) {
            searchCustomers(0);
        } else {
            getCustomers(0);
        }
    };

    const resetFilters = () => {
        setSearchQuery("");
        setStatus("");
        setSortDirection('none');
        getCustomers(0);
    };

    const handleKeyDown = (event: KeyboardEvent<HTMLInputElement>) => {
        if (event.key === "Enter") {
            event.preventDefault();
            searchCustomers(0);
        }
    };

    const handleDelete = async (id: string) => {
        try {
            await axios.delete(`/api/customers/${id}`);
            toast.success("Customer deleted successfully");
            await searchCustomers(currentPage);
        } catch (error) {
            console.error("Error deleting customer:", error);
            toast.error("Failed to delete customer");
        }
    };

    const handlePageChange = (page: number) => {
        setCurrentPage(page);
        if (searchQuery || status) {
            searchCustomers(page);
        } else {
            getCustomers(page);
        }
    };

    const handlePageSizeChange = (size: number) => {
        setPageSize(size);
        if (searchQuery || status) {
            searchCustomers(0, size);
        } else {
            getCustomers(0, size);
        }
    };

    const toggleColumnVisibility = (column: keyof ColumnVisibility) => {
        if (column === 'username' || column === 'fullName') return;

        setColumnVisibility(prev => ({
            ...prev,
            [column]: !prev[column]
        }));
    };

    useEffect(() => {
        getCustomers();
    }, []);

    const getSortIcon = () => {
        switch (sortDirection) {
            case 'asc': return <FaSortUp className="ml-1" />;
            case 'desc': return <FaSortDown className="ml-1" />;
            default: return <FaSort className="ml-1 opacity-30" />;
        }
    };

    return (
        <div className="flex flex-col gap-4 p-4 max-w-full">
            <div className="flex flex-col sm:flex-row items-center mb-5 gap-4 w-full">
                <Input
                    label="Search Bar"
                    placeholder="Search by username, name, city, phone number..."
                    value={searchQuery}
                    onChange={(event) => setSearchQuery(event.target.value)}
                    onKeyDown={handleKeyDown}
                    containerClassName="flex-grow w-full sm:w-auto mb-0"
                />
                <Button
                    onClick={() => searchCustomers(0)}
                    className="h-[42px] w-full sm:w-auto"
                    disabled={isLoading}
                >
                    {isLoading ? "Searching..." : "Search"}
                </Button>
            </div>

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
                    <div className="flex items-center gap-2">
                        <Button
                            onClick={toggleSort}
                            variant="secondary"
                            className="h-[42px] flex items-center"
                        >
                            Sort by Date
                            {getSortIcon()}
                        </Button>
                    </div>

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
                                        <input type="checkbox" checked disabled className="rounded text-blue-600" />
                                        <span>Username</span>
                                    </div>
                                    <div className="flex items-center gap-2 p-1 opacity-50 cursor-not-allowed">
                                        <input type="checkbox" checked disabled className="rounded text-blue-600" />
                                        <span>Full Name</span>
                                    </div>
                                    {Object.entries(columnVisibility)
                                        .filter(([key]) => key !== 'username' && key !== 'fullName')
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
                            onChange={(e) => handlePageSizeChange(Number(e.target.value))}
                            className="border rounded-md p-1 text-sm w-full sm:w-auto"
                        >
                            {pageSizeOptions.map((size) => (
                                <option key={size} value={size}>{size}</option>
                            ))}
                        </select>
                    </div>
                </div>
            </div>

            {isLoading ? (
                <div className="flex justify-center py-8">
                    <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
                </div>
            ) : (
                <>
                    <div className="grid gap-4">
                        {customers.length > 0 ? (
                            customers.map((customer) => (
                                <CustomerCard
                                    key={customer.username}
                                    customer={customer}
                                    onDelete={handleDelete}
                                    columnVisibility={columnVisibility}
                                />
                            ))
                        ) : (
                            <div className="text-center py-8 text-gray-500">
                                No customers found matching your criteria
                            </div>
                        )}
                    </div>

                    {customers.length > 0 && (
                        <div className="flex justify-center mt-6 gap-4">
                            <button
                                onClick={() => handlePageChange(currentPage - 1)}
                                disabled={currentPage === 0}
                                className="p-2 rounded-md disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-100"
                            >
                                <FaChevronLeft className="w-4 h-4" />
                            </button>

                            <span className="flex items-center">
                                Page {currentPage + 1} of {totalPages}
                            </span>

                            <button
                                onClick={() => handlePageChange(currentPage + 1)}
                                disabled={totalPages - 1 <= currentPage}
                                className="p-2 rounded-md disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-100"
                            >
                                <FaChevronRight className="w-4 h-4" />
                            </button>
                        </div>
                    )}
                </>
            )}
        </div>
    );
};