import React, { useState, useCallback, useMemo, useEffect } from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  TableSortLabel,
  Paper,
  Checkbox,
  IconButton,
  Tooltip,
  Typography,
  TextField,
  Button,
  Dialog,
  DialogTitle,
  DialogActions,
  DialogContent,
  CircularProgress,
  Collapse,
  Box,
  Grid,
  Card,
  CardHeader,
  InputAdornment,
  Chip,
  Switch,
  Fade,
  Zoom,
  useTheme,
  alpha,
} from '@mui/material';
import {
  Search as SearchIcon,
  FilterList as FilterListIcon,
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Done as DoneIcon,
  Close as CloseIcon,
  CloudDownload as CloudDownloadIcon,
  MoreVert as MoreVertIcon,
} from '@mui/icons-material';
import { styled } from '@mui/material/styles';
import { useDispatch } from 'react-redux';
import PropTypes from 'prop-types';
import axios from 'axios';
import { toast } from 'react-toastify';
import DisplayField from './DisplayField';
import FormInput from './FormInput';
import { ALERT_TYPES, setAlertContent } from '../../store/actions/nama';
import { errorText, marginButton, requestText, toastTop } from '../../../configs';

// Styled Components for Modern Look
const StyledTableContainer = styled(TableContainer)(({ theme }) => ({
  borderRadius: theme.spacing(2),
  boxShadow: '0 8px 32px rgba(0, 0, 0, 0.08)',
  border: `1px solid ${theme.palette.divider}`,
  overflow: 'hidden',
  background: theme.palette.background.paper,
}));

const StyledTableHead = styled(TableHead)(({ theme }) => ({
  background: `linear-gradient(135deg, ${theme.palette.primary.main} 0%, ${theme.palette.primary.dark} 100%)`,
  '& .MuiTableCell-head': {
    color: theme.palette.primary.contrastText,
    fontWeight: 600,
    fontSize: '0.875rem',
    letterSpacing: '0.5px',
    textTransform: 'uppercase',
    borderBottom: 'none',
    padding: theme.spacing(2),
  },
}));

const StyledTableRow = styled(TableRow)(({ theme, isSelected, isEven }) => ({
  cursor: 'pointer',
  transition: 'all 0.2s cubic-bezier(0.4, 0, 0.2, 1)',
  backgroundColor: isSelected 
    ? alpha(theme.palette.primary.main, 0.12)
    : isEven 
    ? alpha(theme.palette.action.hover, 0.3)
    : 'transparent',
  '&:hover': {
    backgroundColor: isSelected 
      ? alpha(theme.palette.primary.main, 0.16)
      : alpha(theme.palette.action.hover, 0.08),
    transform: 'translateY(-1px)',
    boxShadow: '0 4px 12px rgba(0, 0, 0, 0.1)',
  },
  '& .MuiTableCell-root': {
    borderBottom: `1px solid ${alpha(theme.palette.divider, 0.5)}`,
    padding: theme.spacing(1.5, 2),
  },
}));

const ActionButton = styled(IconButton)(({ theme, color: buttonColor }) => ({
  padding: theme.spacing(0.75),
  borderRadius: theme.spacing(1),
  transition: 'all 0.2s ease-in-out',
  '&:hover': {
    backgroundColor: alpha(theme.palette[buttonColor]?.main || theme.palette.action.hover, 0.1),
    transform: 'scale(1.1)',
  },
}));

const SearchField = styled(TextField)(({ theme }) => ({
  '& .MuiOutlinedInput-root': {
    borderRadius: theme.spacing(3),
    backgroundColor: alpha(theme.palette.background.paper, 0.8),
    transition: 'all 0.3s ease-in-out',
    '&:hover': {
      backgroundColor: theme.palette.background.paper,
      boxShadow: '0 4px 12px rgba(0, 0, 0, 0.1)',
    },
    '&.Mui-focused': {
      backgroundColor: theme.palette.background.paper,
      boxShadow: '0 4px 16px rgba(0, 0, 0, 0.15)',
    },
  },
}));

const ModernChip = styled(Chip)(({ theme }) => ({
  borderRadius: theme.spacing(1.5),
  fontWeight: 500,
  boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
  transition: 'all 0.2s ease-in-out',
  '&:hover': {
    transform: 'translateY(-1px)',
    boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)',
  },
}));

const LoadingOverlay = styled(Box)(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  justifyContent: 'center',
  padding: theme.spacing(4),
  gap: theme.spacing(2),
  color: theme.palette.text.secondary,
}));

// Utility Functions
const requiredPlaceholder = (label, required) => `${label}${required ? '*' : ''}`;

const validateForm = (columns, formValues, setFormValidation) => {
  return new Promise((resolve, reject) => {
    let requiredError = false;
    let validationBuffer = {};
    
    const buffer = (group, name, value) => {
      if (group) {
        validationBuffer[group] = validationBuffer[group] || {};
        validationBuffer[group][name] = value;
      } else {
        validationBuffer[name] = value;
      }
    };

    for (let i in columns) {
      const input = columns[i];
      if (input.required && !formValues[input.name]) {
        buffer(input.group, input.name, { error: true });
        requiredError = true;
      }
    }
    
    setFormValidation(validationBuffer);
    if (requiredError) {
      reject();
    } else {
      resolve();
    }
  });
};

const getDateString = (date) => {
  const moment = require('moment-jalaali');
  return date ? moment(date).format('jYYYY/jM/jD') : '-';
};

const showIndicator = (value, indicator) => (
  <Switch 
    checked={value === indicator.true} 
    size="small" 
    sx={{ cursor: 'default' }}
    color="primary"
  />
);

const getCellContent = (col, rowData, index, handleCheckBox) => {
  let value;
  
  const changeCheckbox = () => {
    handleCheckBox(rowData);
  };

  switch (col.type) {
    case 'date':
      value = getDateString(rowData[col.name]);
      break;
    case 'select':
    case 'multiselect':
      const otherProps = {
        ...(col.optionLabelField && { optionLabelField: col.optionLabelField }),
        ...(col.optionIdField && { optionIdField: col.optionIdField }),
        ...(col.type === 'multiselect' && { multiselect: true }),
      };
      value = (
        <DisplayField 
          value={rowData[col.name]} 
          options={col.options}
          appendOptions={col.appendOptions} 
          {...otherProps}
        />
      );
      break;
    case 'indicator':
      value = showIndicator(rowData[col.name], col.indicator ?? { true: 'Y', false: 'N' });
      break;
    case 'render':
      value = col.render(rowData);
      break;
    case 'checkbox':
      value = (
        <Checkbox 
          onChange={changeCheckbox} 
          checked={rowData[col.name] ? rowData[col.name] : false}
          sx={{ padding: 0 }}
          color="primary"
        />
      );
      break;
    case 'chip':
      value = (
        <ModernChip 
          label={rowData[col.name] || '-'} 
          size="small"
          color={col.chipColor || 'default'}
          variant={col.chipVariant || 'filled'}
        />
      );
      break;
    default:
      value = rowData[col.name] ?? '-';
  }
  
  return value;
};

// Main Component
const ModernTablePro = ({
  texts = {},
  className = '',
  title = '',
  defaultOrderBy = '',
  columns = [],
  rows = [],
  originalRows = [],
  setRows = null,
  selectedRows = [],
  setSelectedRows = () => false,
  isSelected = (row, selectedRows) => selectedRows.indexOf(row) !== -1,
  loading = false,
  size = 'medium',
  showTitleBar = true,
  showRowNumber = true,
  pagination = true,
  selectable = false,
  singleSelect = false,
  actions = [],
  rowActions = [],
  handleCell = null,
  handleCheckBox = null,
  filter = false,
  filterForm = null,
  add = false,
  addForm = null,
  addCallback = null,
  edit = false,
  editForm = null,
  editCallback = null,
  editCondition = () => true,
  removeCallback = null,
  removeCondition = () => true,
  sortable = true,
  addTitle = 'افزودن',
  search = true,
  contentSearch = false,
  exportExcel = false,
  getExcelData = null,
  paginationTop = false,
  ...props
}) => {
  const theme = useTheme();
  const dispatch = useDispatch();

  // Default texts
  const defaultTexts = {
    removeDialog: 'آیا از حذف این ردیف اطمینان دارید؟',
    removeAlertSuccess: 'ردیف مورد نظر با موفقیت حذف شد.',
    removeAlertFailed: 'خطا در عملیات حذف!',
    editAlertSuccess: 'تغییرات ردیف مورد نظر با موفقیت انجام شد.',
    editAlertFailed: 'خطا در عملیات ویرایش!',
    addAlertSuccess: 'ردیف مورد نظر با موفقیت اضافه شد.',
    addAlertFailed: 'خطا در عملیات افزودن!',
    alertFormRequired: 'باید تمام فیلدهای ضروری وارد شوند!',
  };

  const finalTexts = { ...defaultTexts, ...texts };

  // State
  const [order, setOrder] = useState('asc');
  const [orderBy, setOrderBy] = useState(defaultOrderBy);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [searchTerm, setSearchTerm] = useState('');
  const [showForm, setShowForm] = useState(null);
  const [formData, setFormData] = useState({});
  const [removeDialog, setRemoveDialog] = useState({ open: false, rowData: null });

  // Memoized filtered and sorted data
  const filteredRows = useMemo(() => {
    if (!searchTerm) return rows;
    
    const searchLower = searchTerm.toLowerCase();
    return rows.filter((row) => {
      if (contentSearch) {
        return Object.values(row).some(value => 
          String(value).toLowerCase().includes(searchLower)
        );
      } else {
        const searchableColumns = columns.map(col => col.name);
        const searchableData = Object.fromEntries(
          Object.entries(row).filter(([key]) => searchableColumns.includes(key))
        );
        return Object.values(searchableData).some(value =>
          String(value).toLowerCase().includes(searchLower)
        );
      }
    });
  }, [rows, searchTerm, contentSearch, columns]);

  const sortedRows = useMemo(() => {
    if (!orderBy) return filteredRows;

    const comparator = (a, b) => {
      const aVal = a[orderBy];
      const bVal = b[orderBy];
      
      if (bVal < aVal) return order === 'desc' ? -1 : 1;
      if (bVal > aVal) return order === 'desc' ? 1 : -1;
      return 0;
    };

    return [...filteredRows].sort(comparator);
  }, [filteredRows, order, orderBy]);

  const paginatedRows = useMemo(() => {
    if (!pagination) return sortedRows;
    return sortedRows.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);
  }, [sortedRows, page, rowsPerPage, pagination]);

  // Event handlers
  const handleRequestSort = useCallback((property) => {
    const isAsc = orderBy === property && order === 'asc';
    setOrder(isAsc ? 'desc' : 'asc');
    setOrderBy(property);
  }, [order, orderBy]);

  const handleChangePage = useCallback((event, newPage) => {
    setPage(newPage);
  }, []);

  const handleChangeRowsPerPage = useCallback((event) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  }, []);

  const handleSearchChange = useCallback((event) => {
    setSearchTerm(event.target.value);
    setPage(0);
  }, []);

  const handleSelectAllClick = useCallback((event) => {
    if (event.target.checked) {
      setSelectedRows(filteredRows);
    } else {
      setSelectedRows([]);
    }
  }, [filteredRows, setSelectedRows]);

  const handleRowClick = useCallback((rowData) => {
    if (!selectable) return;

    const selectedIndex = selectedRows.indexOf(rowData);
    let newSelected = [];

    if (singleSelect) {
      newSelected = selectedIndex === -1 ? [rowData] : [];
    } else {
      if (selectedIndex === -1) {
        newSelected = [...selectedRows, rowData];
      } else {
        newSelected = selectedRows.filter(item => item !== rowData);
      }
    }

    setSelectedRows(newSelected);
  }, [selectable, singleSelect, selectedRows, setSelectedRows]);

  const handleRemoveClick = useCallback((rowData) => {
    setRemoveDialog({ open: true, rowData });
  }, []);

  const handleRemoveConfirm = useCallback(async () => {
    const { rowData } = removeDialog;
    try {
      await removeCallback(rowData);
      const newRows = rows.filter(row => row !== rowData);
      setRows(newRows);
      dispatch(setAlertContent(ALERT_TYPES.SUCCESS, finalTexts.removeAlertSuccess));
    } catch (error) {
      dispatch(setAlertContent(ALERT_TYPES.ERROR, finalTexts.removeAlertFailed));
    } finally {
      setRemoveDialog({ open: false, rowData: null });
    }
  }, [removeDialog, removeCallback, rows, setRows, dispatch, finalTexts]);

  const exportToExcel = useCallback(async () => {
    try {
      toast.warn(requestText, toastTop);
      let data = rows;
      if (getExcelData) {
        data = await getExcelData();
      }

      const postData = {
        data: data.map((row, ind) => {
          let csvRow = { 'ردیف': ind + 1 };
          columns.forEach(col => {
            csvRow[col.label] = row[col.name];
          });
          return csvRow;
        })
      };

      const res = await axios.post('/rest/s1/general/excel', postData);
      toast.dismiss();
      
      const dataEncode = res.data.encoded;
      const linkSource = `data:application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;base64,${dataEncode}`;
      const downloadLink = document.createElement('a');
      downloadLink.href = linkSource;
      downloadLink.target = '_self';
      downloadLink.click();
    } catch (error) {
      toast.dismiss();
      toast.error(errorText, toastTop);
    }
  }, [rows, getExcelData, columns]);

  // Table Header Component
  const TableHeader = () => (
    <StyledTableHead>
      <TableRow>
        {selectable && (
          <TableCell padding="checkbox">
            {!singleSelect && (
              <Checkbox
                indeterminate={selectedRows.length > 0 && selectedRows.length < filteredRows.length}
                checked={filteredRows.length > 0 && selectedRows.length === filteredRows.length}
                onChange={handleSelectAllClick}
                sx={{ color: 'inherit' }}
              />
            )}
          </TableCell>
        )}
        {showRowNumber && (
          <TableCell>
            <Typography variant="subtitle2">ردیف</Typography>
          </TableCell>
        )}
        {columns.map((column) => (
          <TableCell key={column.name} style={column.style}>
            {sortable && column.sortable !== false ? (
              <TableSortLabel
                active={orderBy === column.name}
                direction={orderBy === column.name ? order : 'asc'}
                onClick={() => handleRequestSort(column.name)}
                sx={{ color: 'inherit', '&:hover': { color: 'inherit' } }}
              >
                <Typography variant="subtitle2" noWrap>
                  {column.label}
                </Typography>
              </TableSortLabel>
            ) : (
              <Typography variant="subtitle2" noWrap>
                {column.label}
              </Typography>
            )}
          </TableCell>
        ))}
      </TableRow>
    </StyledTableHead>
  );

  // Table Row Component
  const TableRowComponent = React.memo(({ rowData, index }) => {
    const isItemSelected = isSelected(rowData, selectedRows);
    const isEven = index % 2 === 0;

    return (
      <StyledTableRow
        isSelected={isItemSelected}
        isEven={isEven}
        onClick={() => handleRowClick(rowData)}
      >
        {selectable && (
          <TableCell padding="checkbox">
            <Checkbox checked={isItemSelected} color="primary" />
          </TableCell>
        )}
        {showRowNumber && (
          <TableCell>
            <Typography variant="body2" color="textSecondary">
              {page * rowsPerPage + index + 1}
            </Typography>
          </TableCell>
        )}
        {columns.map((column, colIndex) => (
          <TableCell key={column.name} style={column.style}>
            <Box display="flex" alignItems="center" justifyContent="space-between">
              <Typography
                variant="body2"
                noWrap={column.noWrap ?? true}
                sx={{
                  color: column.textColor || 'text.primary',
                  cursor: handleCell ? 'pointer' : 'default',
                  '&:hover': handleCell ? { color: 'primary.main' } : {},
                }}
                onClick={() => handleCell?.(rowData, column)}
              >
                {getCellContent(column, rowData, index, handleCheckBox)}
              </Typography>
              
              {colIndex === columns.length - 1 && (rowActions.length > 0 || removeCallback || edit) && (
                <Box display="flex" gap={0.5}>
                  {rowActions.map((action, actionIndex) => (
                    <Tooltip key={actionIndex} title={action.title}>
                      <ActionButton
                        size="small"
                        onClick={(e) => {
                          e.stopPropagation();
                          action.onClick(rowData);
                        }}
                        color={action.color || 'default'}
                      >
                        <action.icon fontSize="small" />
                      </ActionButton>
                    </Tooltip>
                  ))}
                  
                  {edit && editCondition(rowData) && (
                    <Tooltip title="ویرایش">
                      <ActionButton
                        size="small"
                        onClick={(e) => {
                          e.stopPropagation();
                          editCallback?.(rowData);
                        }}
                        color="primary"
                      >
                        <EditIcon fontSize="small" />
                      </ActionButton>
                    </Tooltip>
                  )}
                  
                  {removeCallback && removeCondition(rowData) && (
                    <Tooltip title="حذف">
                      <ActionButton
                        size="small"
                        onClick={(e) => {
                          e.stopPropagation();
                          handleRemoveClick(rowData);
                        }}
                        color="error"
                      >
                        <DeleteIcon fontSize="small" />
                      </ActionButton>
                    </Tooltip>
                  )}
                </Box>
              )}
            </Box>
          </TableCell>
        ))}
      </StyledTableRow>
    );
  });

  // Loading Component
  const LoadingComponent = () => (
    <TableRow>
      <TableCell colSpan={columns.length + (selectable ? 1 : 0) + (showRowNumber ? 1 : 0)}>
        <LoadingOverlay>
          <CircularProgress size={40} thickness={4} />
          <Typography variant="body2">در حال بارگذاری...</Typography>
        </LoadingOverlay>
      </TableCell>
    </TableRow>
  );

  // Empty Component
  const EmptyComponent = () => (
    <TableRow>
      <TableCell colSpan={columns.length + (selectable ? 1 : 0) + (showRowNumber ? 1 : 0)}>
        <LoadingOverlay>
          <Typography variant="h6" color="textSecondary">
            داده‌ای یافت نشد
          </Typography>
          <Typography variant="body2" color="textSecondary">
            {searchTerm ? 'نتیجه‌ای برای جستجوی شما یافت نشد' : 'هیچ داده‌ای برای نمایش وجود ندارد'}
          </Typography>
        </LoadingOverlay>
      </TableCell>
    </TableRow>
  );

  return (
    <Card className={className} elevation={0}>
      {showTitleBar && (
        <CardHeader
          title={
            <Grid container spacing={2} alignItems="center">
              <Grid item xs={12} md={4}>
                <Typography variant="h6" fontWeight="600" color="primary">
                  {title}
                </Typography>
              </Grid>
              
              {search && (
                <Grid item xs={12} md={4}>
                  <SearchField
                    fullWidth
                    size="small"
                    placeholder="جستجو در جدول..."
                    value={searchTerm}
                    onChange={handleSearchChange}
                    InputProps={{
                      startAdornment: (
                        <InputAdornment position="start">
                          <SearchIcon color="action" />
                        </InputAdornment>
                      ),
                    }}
                  />
                </Grid>
              )}
              
              <Grid item xs={12} md={4}>
                <Box display="flex" gap={1} justifyContent="flex-end">
                  {exportExcel && (
                    <Tooltip title="خروجی اکسل">
                      <Button
                        variant="outlined"
                        size="small"
                        startIcon={<CloudDownloadIcon />}
                        onClick={exportToExcel}
                        sx={{ borderRadius: 2 }}
                      >
                        اکسل
                      </Button>
                    </Tooltip>
                  )}
                  
                  {add && (
                    <Tooltip title={addTitle}>
                      <Button
                        variant="contained"
                        size="small"
                        startIcon={<AddIcon />}
                        onClick={() => setShowForm(`add-${add}`)}
                        sx={{ borderRadius: 2 }}
                      >
                        {addTitle}
                      </Button>
                    </Tooltip>
                  )}
                  
                  {filter && (
                    <Tooltip title="فیلتر">
                      <IconButton
                        onClick={() => setShowForm(`filter-${filter}`)}
                        color={showForm === `filter-${filter}` ? 'primary' : 'default'}
                      >
                        <FilterListIcon />
                      </IconButton>
                    </Tooltip>
                  )}
                </Box>
              </Grid>
            </Grid>
          }
        />
      )}

      {/* Filter Form */}
      {filter === 'external' && (
        <Collapse in={showForm === 'filter-external'}>
          <Box p={2} bgcolor="grey.50">
            {filterForm}
          </Box>
        </Collapse>
      )}

      {/* Add Form */}
      {add === 'external' && (
        <Collapse in={showForm === 'add-external'}>
          <Box p={2} bgcolor="grey.50">
            {addForm}
          </Box>
        </Collapse>
      )}

      {/* Top Pagination */}
      {pagination && paginationTop && (
        <Box px={2}>
          <TablePagination
            component="div"
            count={filteredRows.length}
            page={page}
            onPageChange={handleChangePage}
            rowsPerPage={rowsPerPage}
            onRowsPerPageChange={handleChangeRowsPerPage}
            rowsPerPageOptions={[5, 10, 25, 50, 100]}
            labelRowsPerPage="تعداد در صفحه:"
            labelDisplayedRows={({ from, to, count }) => 
              `${from}-${to} از ${count}`
            }
          />
        </Box>
      )}

      {/* Main Table */}
      <StyledTableContainer component={Paper} elevation={0}>
        <Table size={size} stickyHeader>
          <TableHeader />
          <TableBody>
            {loading ? (
              <LoadingComponent />
            ) : paginatedRows.length > 0 ? (
              paginatedRows.map((row, index) => (
                <TableRowComponent
                  key={`row-${index}`}
                  rowData={row}
                  index={index}
                />
              ))
            ) : (
              <EmptyComponent />
            )}
          </TableBody>
        </Table>
      </StyledTableContainer>

      {/* Bottom Pagination */}
      {pagination && !paginationTop && (
        <TablePagination
          component="div"
          count={filteredRows.length}
          page={page}
          onPageChange={handleChangePage}
          rowsPerPage={rowsPerPage}
          onRowsPerPageChange={handleChangeRowsPerPage}
          rowsPerPageOptions={[5, 10, 25, 50, 100]}
          labelRowsPerPage="تعداد در صفحه:"
          labelDisplayedRows={({ from, to, count }) => 
            `${from}-${to} از ${count}`
          }
          sx={{ borderTop: 1, borderColor: 'divider' }}
        />
      )}

      {/* Remove Confirmation Dialog */}
      <Dialog
        open={removeDialog.open}
        onClose={() => setRemoveDialog({ open: false, rowData: null })}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>
          <Box display="flex" alignItems="center" gap={2}>
            <DeleteIcon color="error" />
            <Typography variant="h6">تأیید حذف</Typography>
          </Box>
        </DialogTitle>
        <DialogContent>
          <Typography>{finalTexts.removeDialog}</Typography>
        </DialogContent>
        <DialogActions sx={{ p: 2, gap: 1 }}>
          <Button
            onClick={() => setRemoveDialog({ open: false, rowData: null })}
            variant="outlined"
            color="inherit"
          >
            انصراف
          </Button>
          <Button
            onClick={handleRemoveConfirm}
            variant="contained"
            color="error"
            startIcon={<DeleteIcon />}
          >
            حذف
          </Button>
        </DialogActions>
      </Dialog>
    </Card>
  );
};

// PropTypes
ModernTablePro.propTypes = {
  texts: PropTypes.objectOf(PropTypes.string),
  className: PropTypes.string,
  title: PropTypes.string,
  defaultOrderBy: PropTypes.string,
  columns: PropTypes.arrayOf(PropTypes.object).isRequired,
  rows: PropTypes.arrayOf(PropTypes.object),
  originalRows: PropTypes.arrayOf(PropTypes.object),
  setRows: PropTypes.func,
  selectedRows: PropTypes.arrayOf(PropTypes.object),
  setSelectedRows: PropTypes.func,
  isSelected: PropTypes.func,
  loading: PropTypes.bool,
  size: PropTypes.oneOf(['small', 'medium']),
  showTitleBar: PropTypes.bool,
  showRowNumber: PropTypes.bool,
  pagination: PropTypes.bool,
  selectable: PropTypes.bool,
  singleSelect: PropTypes.bool,
  actions: PropTypes.arrayOf(PropTypes.object),
  rowActions: PropTypes.arrayOf(PropTypes.object),
  handleCell: PropTypes.func,
  handleCheckBox: PropTypes.func,
  filter: PropTypes.oneOf([false, 'external']),
  filterForm: PropTypes.node,
  add: PropTypes.oneOf([false, 'external', 'inline']),
  addForm: PropTypes.node,
  addCallback: PropTypes.func,
  edit: PropTypes.oneOf([false, 'external', 'inline', 'callback']),
  editForm: PropTypes.node,
  editCallback: PropTypes.func,
  editCondition: PropTypes.func,
  removeCallback: PropTypes.func,
  removeCondition: PropTypes.func,
  sortable: PropTypes.bool,
  addTitle: PropTypes.string,
  search: PropTypes.bool,
  contentSearch: PropTypes.bool,
  exportExcel: PropTypes.bool,
  getExcelData: PropTypes.func,
  paginationTop: PropTypes.bool,
};

export default React.memo(ModernTablePro);