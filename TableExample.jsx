import React, { useState, useEffect } from 'react';
import { ThemeProvider } from '@mui/material/styles';
import { CssBaseline, Container, Typography, Box } from '@mui/material';
import ModernTablePro from './ModernTablePro';
import { createTableTheme } from './tableTheme';

// Example data and configuration
const sampleData = [
  {
    id: 1,
    name: 'احمد محمدی',
    email: 'ahmad@example.com',
    role: 'مدیر',
    status: 'فعال',
    joinDate: '2023-01-15',
    salary: 5000000,
    active: true,
  },
  {
    id: 2,
    name: 'فاطمه حسینی',
    email: 'fateme@example.com',
    role: 'توسعه‌دهنده',
    status: 'فعال',
    joinDate: '2023-02-20',
    salary: 4000000,
    active: true,
  },
  {
    id: 3,
    name: 'علی رضایی',
    email: 'ali@example.com',
    role: 'طراح',
    status: 'غیرفعال',
    joinDate: '2023-03-10',
    salary: 3500000,
    active: false,
  },
  {
    id: 4,
    name: 'مریم کریمی',
    email: 'maryam@example.com',
    role: 'تحلیلگر',
    status: 'فعال',
    joinDate: '2023-04-05',
    salary: 3800000,
    active: true,
  },
  {
    id: 5,
    name: 'محمد صادقی',
    email: 'mohammad@example.com',
    role: 'مدیر پروژه',
    status: 'فعال',
    joinDate: '2023-05-12',
    salary: 4500000,
    active: true,
  },
];

const columns = [
  {
    name: 'name',
    label: 'نام',
    sortable: true,
    style: { minWidth: 150 },
  },
  {
    name: 'email',
    label: 'ایمیل',
    sortable: true,
    style: { minWidth: 200 },
  },
  {
    name: 'role',
    label: 'نقش',
    type: 'chip',
    chipColor: 'primary',
    chipVariant: 'outlined',
    sortable: true,
    style: { minWidth: 120 },
  },
  {
    name: 'status',
    label: 'وضعیت',
    type: 'chip',
    chipColor: 'success',
    sortable: true,
    style: { minWidth: 100 },
    render: (rowData) => (
      <Chip
        label={rowData.status}
        color={rowData.status === 'فعال' ? 'success' : 'error'}
        variant="filled"
        size="small"
      />
    ),
  },
  {
    name: 'joinDate',
    label: 'تاریخ پیوستن',
    type: 'date',
    sortable: true,
    style: { minWidth: 130 },
  },
  {
    name: 'active',
    label: 'فعال',
    type: 'indicator',
    indicator: { true: true, false: false },
    sortable: false,
    style: { minWidth: 80, textAlign: 'center' },
  },
  {
    name: 'actions',
    label: 'عملیات',
    sortable: false,
    style: { minWidth: 120, textAlign: 'center' },
  },
];

const TableExample = () => {
  const [rows, setRows] = useState(sampleData);
  const [originalRows, setOriginalRows] = useState(sampleData);
  const [selectedRows, setSelectedRows] = useState([]);
  const [loading, setLoading] = useState(false);

  // Theme
  const theme = createTableTheme();

  // Sample actions
  const rowActions = [
    {
      title: 'مشاهده جزئیات',
      icon: ({ fontSize }) => <VisibilityIcon fontSize={fontSize} />,
      color: 'primary',
      onClick: (rowData) => {
        console.log('View details:', rowData);
        alert(`مشاهده جزئیات: ${rowData.name}`);
      },
    },
    {
      title: 'ارسال ایمیل',
      icon: ({ fontSize }) => <EmailIcon fontSize={fontSize} />,
      color: 'info',
      onClick: (rowData) => {
        console.log('Send email to:', rowData.email);
        alert(`ارسال ایمیل به: ${rowData.email}`);
      },
    },
  ];

  const actions = [
    {
      title: 'حذف گروهی',
      icon: ({ fontSize }) => <DeleteIcon fontSize={fontSize} />,
      color: 'error',
      onClick: () => {
        if (selectedRows.length === 0) {
          alert('لطفاً ردیف‌هایی را برای حذف انتخاب کنید');
          return;
        }
        const confirmed = window.confirm(`آیا می‌خواهید ${selectedRows.length} ردیف را حذف کنید؟`);
        if (confirmed) {
          const newRows = rows.filter(row => !selectedRows.includes(row));
          setRows(newRows);
          setOriginalRows(newRows);
          setSelectedRows([]);
          alert('ردیف‌های انتخاب شده حذف شدند');
        }
      },
    },
    {
      title: 'فعال‌سازی گروهی',
      icon: ({ fontSize }) => <CheckCircleIcon fontSize={fontSize} />,
      color: 'success',
      onClick: () => {
        if (selectedRows.length === 0) {
          alert('لطفاً ردیف‌هایی را برای فعال‌سازی انتخاب کنید');
          return;
        }
        const newRows = rows.map(row => 
          selectedRows.includes(row) ? { ...row, active: true, status: 'فعال' } : row
        );
        setRows(newRows);
        setOriginalRows(newRows);
        setSelectedRows([]);
        alert(`${selectedRows.length} ردیف فعال شد`);
      },
    },
  ];

  // Callbacks
  const handleAdd = async (formData) => {
    return new Promise((resolve) => {
      setTimeout(() => {
        const newRow = {
          ...formData,
          id: Math.max(...rows.map(r => r.id)) + 1,
        };
        const newRows = [...rows, newRow];
        setRows(newRows);
        setOriginalRows(newRows);
        resolve(newRow);
      }, 1000);
    });
  };

  const handleEdit = async (formData, oldData) => {
    return new Promise((resolve) => {
      setTimeout(() => {
        const newRows = rows.map(row => 
          row.id === oldData.id ? { ...formData, id: oldData.id } : row
        );
        setRows(newRows);
        setOriginalRows(newRows);
        resolve(formData);
      }, 1000);
    });
  };

  const handleRemove = async (rowData) => {
    return new Promise((resolve) => {
      setTimeout(() => {
        const newRows = rows.filter(row => row.id !== rowData.id);
        setRows(newRows);
        setOriginalRows(newRows);
        resolve();
      }, 1000);
    });
  };

  const handleCellClick = (rowData, column) => {
    console.log('Cell clicked:', { rowData, column });
    if (column.name !== 'actions') {
      alert(`کلیک روی: ${column.label} - ${rowData[column.name]}`);
    }
  };

  const simulateLoading = () => {
    setLoading(true);
    setTimeout(() => {
      setLoading(false);
    }, 2000);
  };

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Container maxWidth="xl" sx={{ py: 4 }}>
        <Box mb={4}>
          <Typography variant="h4" component="h1" gutterBottom fontWeight="bold" color="primary">
            جدول مدرن و پیشرفته
          </Typography>
          <Typography variant="body1" color="textSecondary" paragraph>
            نمونه‌ای از جدول مدرن با قابلیت‌های پیشرفته شامل جستجو، مرتب‌سازی، صفحه‌بندی، و عملیات CRUD
          </Typography>
        </Box>

        <Box mb={2}>
          <Button
            variant="outlined"
            onClick={simulateLoading}
            disabled={loading}
            sx={{ mr: 2 }}
          >
            شبیه‌سازی بارگذاری
          </Button>
        </Box>

        <ModernTablePro
          title="مدیریت کاربران"
          columns={columns}
          rows={rows}
          originalRows={originalRows}
          setRows={setRows}
          selectedRows={selectedRows}
          setSelectedRows={setSelectedRows}
          loading={loading}
          showTitleBar={true}
          showRowNumber={true}
          selectable={true}
          singleSelect={false}
          pagination={true}
          sortable={true}
          search={true}
          contentSearch={false}
          exportExcel={true}
          actions={actions}
          rowActions={rowActions}
          handleCell={handleCellClick}
          addCallback={handleAdd}
          editCallback={handleEdit}
          removeCallback={handleRemove}
          add="external"
          edit="callback"
          size="medium"
          texts={{
            removeDialog: 'آیا از حذف این کاربر اطمینان دارید؟',
            removeAlertSuccess: 'کاربر با موفقیت حذف شد.',
            removeAlertFailed: 'خطا در حذف کاربر!',
          }}
        />
      </Container>
    </ThemeProvider>
  );
};

// Additional imports for icons used in the example
import {
  Visibility as VisibilityIcon,
  Email as EmailIcon,
  Delete as DeleteIcon,
  CheckCircle as CheckCircleIcon,
} from '@mui/icons-material';
import { Button, Chip } from '@mui/material';

export default TableExample;