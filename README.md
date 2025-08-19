# Modern Table Pro Component

A modern, stylish, and feature-rich table component built with React and Material-UI (MUI) v5. This component transforms the original class-based table into a functional component with modern styling, improved performance, and enhanced user experience.

## ✨ Key Features

### 🎨 Modern Design
- **Gradient headers** with professional styling
- **Smooth animations** and hover effects
- **Glass-morphism effects** with subtle shadows
- **Responsive design** that works on all devices
- **Dark/Light theme** support

### 🚀 Performance Optimizations
- **React.memo** for preventing unnecessary re-renders
- **useMemo** for expensive calculations
- **useCallback** for stable function references
- **Virtualization-ready** architecture

### 🔧 Advanced Functionality
- **Multi-column sorting** with visual indicators
- **Advanced search** with content filtering
- **Bulk operations** with row selection
- **Inline editing** capabilities
- **Export to Excel** functionality
- **Dynamic pagination** with customizable page sizes
- **Drag and drop** support (when enabled)

### 🎯 User Experience
- **Smooth transitions** for all interactions
- **Loading states** with elegant spinners
- **Empty states** with helpful messages
- **Confirmation dialogs** for destructive actions
- **Tooltips** for better accessibility
- **Keyboard navigation** support

## 📦 Installation

```bash
npm install @mui/material @mui/icons-material @emotion/react @emotion/styled
npm install react-redux redux axios react-toastify moment-jalaali
```

## 🚀 Quick Start

```jsx
import React, { useState } from 'react';
import { ThemeProvider } from '@mui/material/styles';
import ModernTablePro from './ModernTablePro';
import { createTableTheme } from './tableTheme';

const MyComponent = () => {
  const [rows, setRows] = useState(yourData);
  const [selectedRows, setSelectedRows] = useState([]);
  
  const columns = [
    { name: 'name', label: 'Name', sortable: true },
    { name: 'email', label: 'Email', sortable: true },
    { name: 'status', label: 'Status', type: 'chip' },
    { name: 'actions', label: 'Actions', sortable: false },
  ];

  return (
    <ThemeProvider theme={createTableTheme()}>
      <ModernTablePro
        title="Users Management"
        columns={columns}
        rows={rows}
        setRows={setRows}
        selectedRows={selectedRows}
        setSelectedRows={setSelectedRows}
        selectable={true}
        pagination={true}
        search={true}
        exportExcel={true}
      />
    </ThemeProvider>
  );
};
```

## 🎨 Styling & Theming

### Custom Theme
The component comes with a comprehensive theme configuration:

```jsx
import { createTableTheme, colorSchemes } from './tableTheme';

// Use default theme
const theme = createTableTheme();

// Use with custom color scheme
const customTheme = createTableTheme({
  palette: {
    primary: { main: colorSchemes.success.primary },
    secondary: { main: colorSchemes.success.secondary },
  },
});
```

### Available Color Schemes
- `default` - Blue primary theme
- `success` - Green primary theme  
- `warning` - Orange primary theme
- `error` - Red primary theme
- `dark` - Dark mode optimized

### Custom Styling
```jsx
<ModernTablePro
  sx={{
    '& .MuiTableContainer-root': {
      borderRadius: 3,
      boxShadow: 'custom-shadow',
    }
  }}
/>
```

## 📋 Props Reference

### Core Props
| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `title` | `string` | `""` | Table title displayed in header |
| `columns` | `array` | `[]` | Column definitions (required) |
| `rows` | `array` | `[]` | Data rows |
| `loading` | `boolean` | `false` | Show loading state |
| `className` | `string` | `""` | Additional CSS classes |

### Selection Props
| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `selectable` | `boolean` | `false` | Enable row selection |
| `singleSelect` | `boolean` | `false` | Allow only single selection |
| `selectedRows` | `array` | `[]` | Currently selected rows |
| `setSelectedRows` | `function` | `() => false` | Selection change handler |

### Display Props
| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `showTitleBar` | `boolean` | `true` | Show table header bar |
| `showRowNumber` | `boolean` | `true` | Show row numbers |
| `size` | `"small" \| "medium"` | `"medium"` | Table size |
| `pagination` | `boolean` | `true` | Enable pagination |
| `search` | `boolean` | `true` | Enable search functionality |

### Action Props
| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `actions` | `array` | `[]` | Bulk actions for selected rows |
| `rowActions` | `array` | `[]` | Individual row actions |
| `add` | `false \| "external" \| "inline"` | `false` | Add functionality type |
| `edit` | `false \| "external" \| "inline" \| "callback"` | `false` | Edit functionality type |
| `removeCallback` | `function` | `null` | Row removal handler |

### Advanced Props
| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `sortable` | `boolean` | `true` | Enable column sorting |
| `exportExcel` | `boolean` | `false` | Enable Excel export |
| `contentSearch` | `boolean` | `false` | Search in all content vs columns only |
| `handleCell` | `function` | `null` | Cell click handler |
| `handleCheckBox` | `function` | `null` | Checkbox change handler |

## 🔧 Column Configuration

### Basic Column
```jsx
{
  name: 'fieldName',        // Data field name
  label: 'Display Name',    // Column header
  sortable: true,           // Enable sorting
  style: { minWidth: 150 }, // Custom styling
}
```

### Column Types

#### Text Column (Default)
```jsx
{
  name: 'name',
  label: 'Name',
  type: 'text', // Optional, default
}
```

#### Date Column
```jsx
{
  name: 'createdAt',
  label: 'Created',
  type: 'date',
}
```

#### Chip Column
```jsx
{
  name: 'status',
  label: 'Status',
  type: 'chip',
  chipColor: 'primary',
  chipVariant: 'filled',
}
```

#### Indicator Column (Switch)
```jsx
{
  name: 'active',
  label: 'Active',
  type: 'indicator',
  indicator: { true: true, false: false },
}
```

#### Custom Render Column
```jsx
{
  name: 'custom',
  label: 'Custom',
  type: 'render',
  render: (rowData) => (
    <CustomComponent data={rowData} />
  ),
}
```

#### Checkbox Column
```jsx
{
  name: 'selected',
  label: 'Select',
  type: 'checkbox',
}
```

## 🎯 Actions Configuration

### Row Actions
```jsx
const rowActions = [
  {
    title: 'Edit',
    icon: EditIcon,
    color: 'primary',
    onClick: (rowData) => handleEdit(rowData),
    display: (rowData) => rowData.editable, // Optional condition
  },
  {
    title: 'Delete',
    icon: DeleteIcon,
    color: 'error',
    onClick: (rowData) => handleDelete(rowData),
  },
];
```

### Bulk Actions
```jsx
const actions = [
  {
    title: 'Delete Selected',
    icon: DeleteIcon,
    color: 'error',
    onClick: () => handleBulkDelete(selectedRows),
  },
  {
    title: 'Export Selected',
    icon: ExportIcon,
    color: 'primary',
    onClick: () => handleExport(selectedRows),
  },
];
```

## 🔄 CRUD Operations

### Add Functionality
```jsx
const handleAdd = async (formData) => {
  // API call to create new record
  const response = await api.create(formData);
  return response.data; // Return new record
};

<ModernTablePro
  add="external" // or "inline"
  addCallback={handleAdd}
  addForm={<YourAddForm />} // For external type
/>
```

### Edit Functionality
```jsx
const handleEdit = async (formData, oldData, index) => {
  // API call to update record
  const response = await api.update(oldData.id, formData);
  return response.data;
};

<ModernTablePro
  edit="callback" // or "external", "inline"
  editCallback={handleEdit}
  editCondition={(rowData) => rowData.editable}
/>
```

### Delete Functionality
```jsx
const handleRemove = async (rowData) => {
  // API call to delete record
  await api.delete(rowData.id);
  // Component will handle UI updates
};

<ModernTablePro
  removeCallback={handleRemove}
  removeCondition={(rowData) => rowData.deletable}
/>
```

## 📱 Responsive Design

The table automatically adapts to different screen sizes:

- **Desktop**: Full feature set with all columns visible
- **Tablet**: Optimized column widths and spacing
- **Mobile**: Responsive design with horizontal scrolling for wide tables

### Custom Breakpoints
```jsx
// In your theme configuration
const theme = createTableTheme({
  breakpoints: {
    values: {
      xs: 0,
      sm: 600,
      md: 900,
      lg: 1200,
      xl: 1536,
    },
  },
});
```

## 🎨 Animation System

### Built-in Animations
- **Fade In**: Smooth appearance of elements
- **Slide In**: Sliding transitions for forms
- **Zoom In**: Scale animations for dialogs
- **Hover Effects**: Subtle transform on interactions

### Custom Animations
```jsx
import { animations } from './tableTheme';

// Use in your custom components
const MyComponent = styled(Box)(({ theme }) => ({
  ...animations.fadeIn,
  // Your custom styles
}));
```

## 🔍 Search & Filtering

### Basic Search
```jsx
<ModernTablePro
  search={true}
  contentSearch={false} // Search only in defined columns
/>
```

### Advanced Search
```jsx
<ModernTablePro
  search={true}
  contentSearch={true} // Search in all row content
/>
```

### External Filter
```jsx
<ModernTablePro
  filter="external"
  filterForm={<YourFilterForm />}
/>
```

## 📊 Export Functionality

### Excel Export
```jsx
const getExcelData = async () => {
  // Optional: Fetch additional data for export
  return await api.getAllData();
};

<ModernTablePro
  exportExcel={true}
  getExcelData={getExcelData} // Optional
/>
```

## 🎯 Event Handlers

### Cell Click Handler
```jsx
const handleCellClick = (rowData, column) => {
  console.log('Cell clicked:', rowData, column);
  // Your custom logic
};

<ModernTablePro
  handleCell={handleCellClick}
/>
```

### Checkbox Handler
```jsx
const handleCheckBox = (rowData) => {
  console.log('Checkbox changed:', rowData);
  // Your custom logic
};

<ModernTablePro
  handleCheckBox={handleCheckBox}
/>
```

## 🚀 Performance Tips

1. **Memoize your data**: Use `useMemo` for expensive data transformations
2. **Stable references**: Use `useCallback` for event handlers
3. **Pagination**: Enable pagination for large datasets
4. **Virtual scrolling**: Consider virtualization for very large tables
5. **Debounced search**: Implement debouncing for search inputs

## 🔧 Migration from Original Component

### Key Changes
1. **Imports**: Update to `@mui/material` from `@material-ui/core`
2. **Styling**: Use `styled` components instead of `makeStyles`
3. **Props**: Some prop names have been updated for clarity
4. **Theming**: New theme structure with enhanced customization

### Migration Steps
1. Update your imports
2. Replace the old component with `ModernTablePro`
3. Update theme configuration
4. Test all functionality
5. Customize styling as needed

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 License

This project is licensed under the MIT License.

## 🙏 Acknowledgments

- Material-UI team for the excellent component library
- React team for the amazing framework
- Contributors who helped improve this component

---

**Happy coding! 🚀**