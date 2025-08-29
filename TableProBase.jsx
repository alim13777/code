import React, { useState, useMemo } from "react";
import {
    Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
    TablePagination, Paper, Checkbox, IconButton, TextField, Toolbar, Typography, Tooltip, Fade
} from "@material-ui/core";
import { makeStyles } from "@material-ui/core/styles";
import DeleteIcon from "@material-ui/icons/Delete";
import EditIcon from "@material-ui/icons/Edit";
import SearchIcon from '@material-ui/icons/Search';

const useStyles = makeStyles((theme) => ({
    paper: {
        width: '100%',
        marginBottom: theme.spacing(3),
        borderRadius: 14,
        boxShadow: '0 8px 24px rgba(60,60,120,0.18)',
        background: 'linear-gradient(115deg, #f5f7fa 0%, #c3cfe2 100%)',
        overflow: "hidden"
    },
    toolbar: {
        paddingLeft: theme.spacing(3),
        paddingRight: theme.spacing(3),
        background: 'linear-gradient(90deg, #3f51b5 0%, #2196f3 100%)',
        color: '#fff',
        borderTopLeftRadius: 14,
        borderTopRightRadius: 14,
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between"
    },
    searchInput: {
        width: "250px",
        background: "#fff",
        borderRadius: 8,
        fontWeight: 500
    },
    table: {
        minWidth: 800,
        background: "transparent"
    },
    headRow: {
        background: 'linear-gradient(90deg, #2196f3 40%, #3f51b5 100%)'
    },
    headCell: {
        fontWeight: "bold",
        color: "#fff",
        fontSize: "18px",
        borderBottom: `2px solid #2196f3`
    },
    bodyRow: {
        transition: "background .2s",
        "&:hover": {
            background: "#e3f2fd"
        }
    },
    actionCell: {
        minWidth: 120,
        display: "flex",
        alignItems: "center",
        gap: theme.spacing(1)
    },
    actions: {
        opacity: 0,
        transition: "opacity .3s",
        pointerEvents: "none"
    },
    rowHover: {
        "& $actions": {
            opacity: 1,
            pointerEvents: "auto"
        }
    },
    editBtn: {
        backgroundColor: "#3f51b522 !important",
        color: "#3f51b5",
        "&:hover": {
            backgroundColor: "#3f51b544 !important"
        }
    },
    deleteBtn: {
        backgroundColor: "#2196f322 !important",
        color: "#2196f3",
        "&:hover": {
            backgroundColor: "#2196f344 !important"
        }
    },
    pagination: {
        background: "#e3f2fd",
        borderBottomLeftRadius: 14,
        borderBottomRightRadius: 14,
        fontWeight: 500
    }
}));

function ModernTable({
                         columns = [],
                         rows = [],
                         onEditRow,
                         onDeleteRow,
                         selectable = true,
                         pagination = true,
                         title = "",
                         actions = []
                     }) {
    const classes = useStyles();
    const [selected, setSelected] = useState([]);
    const [page, setPage] = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(10);
    const [search, setSearch] = useState("");
    const [hoveredRow, setHoveredRow] = useState(null);

    // Filter rows by search
    const filteredRows = useMemo(() => {
        if (!search) return rows;
        return rows.filter(row =>
            columns.some(col =>
                String(row[col.name] || "")
                    .toLowerCase()
                    .includes(search.toLowerCase())
            )
        );
    }, [search, rows, columns]);

    const handleSelectAllClick = (event) => {
        if (event.target.checked) {
            const newSelecteds = filteredRows.map((n) => n);
            setSelected(newSelecteds);
            return;
        }
        setSelected([]);
    };

    const handleClick = (event, row) => {
        const selectedIndex = selected.indexOf(row);
        let newSelected = [];

        if (selectedIndex === -1) {
            newSelected = newSelected.concat(selected, row);
        } else if (selectedIndex === 0) {
            newSelected = newSelected.concat(selected.slice(1));
        } else if (selectedIndex === selected.length - 1) {
            newSelected = newSelected.concat(selected.slice(0, -1));
        } else if (selectedIndex > 0) {
            newSelected = newSelected.concat(
                selected.slice(0, selectedIndex),
                selected.slice(selectedIndex + 1)
            );
        }
        setSelected(newSelected);
    };

    const isSelected = (row) => selected.indexOf(row) !== -1;

    // Pagination handlers
    const handleChangePage = (event, newPage) => setPage(newPage);
    const handleChangeRowsPerPage = (event) => {
        setRowsPerPage(parseInt(event.target.value, 10));
        setPage(0);
    };

    // Table headers
    const headCells = columns.map(col => ({
        id: col.name,
        label: col.label,
        numeric: col.numeric,
        disablePadding: col.disablePadding || false
    }));

    return (
        <Paper className={classes.paper}>
            <Toolbar className={classes.toolbar}>
                <Typography variant="h5" style={{fontWeight: 700, letterSpacing: 1}}>{title}</Typography>
                <TextField
                    className={classes.searchInput}
                    variant="outlined"
                    size="small"
                    placeholder="جستجو..."
                    value={search}
                    onChange={e => setSearch(e.target.value)}
                    InputProps={{
                        endAdornment: <SearchIcon style={{color: "#3f51b5"}} />
                    }}
                />
            </Toolbar>

            <TableContainer>
                <Table className={classes.table}>
                    <TableHead>
                        <TableRow className={classes.headRow}>
                            {selectable && (
                                <TableCell padding="checkbox" className={classes.headCell}>
                                    <Checkbox
                                        indeterminate={selected.length > 0 && selected.length < filteredRows.length}
                                        checked={filteredRows.length > 0 && selected.length === filteredRows.length}
                                        onChange={handleSelectAllClick}
                                        style={{color: "#3f51b5"}}
                                    />
                                </TableCell>
                            )}
                            {headCells.map(headCell => (
                                <TableCell
                                    key={headCell.id}
                                    align={headCell.numeric ? 'right' : 'left'}
                                    padding={headCell.disablePadding ? 'none' : 'default'}
                                    className={classes.headCell}
                                >
                                    {headCell.label}
                                </TableCell>
                            ))}
                            <TableCell className={classes.headCell}>عملیات</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {(pagination
                                ? filteredRows.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                                : filteredRows
                        ).map((row, idx) => {
                            const isItemSelected = isSelected(row);
                            const isHovered = hoveredRow === idx;
                            return (
                                <TableRow
                                    hover
                                    className={`${classes.bodyRow} ${isHovered ? classes.rowHover : ""}`}
                                    onClick={event => handleClick(event, row)}
                                    onMouseEnter={() => setHoveredRow(idx)}
                                    onMouseLeave={() => setHoveredRow(null)}
                                    role="checkbox"
                                    aria-checked={isItemSelected}
                                    tabIndex={-1}
                                    key={idx}
                                    selected={isItemSelected}
                                    style={isItemSelected ? {background: "#c3cfe2"} : {}}
                                >
                                    {selectable &&
                                        <TableCell padding="checkbox">
                                            <Checkbox checked={isItemSelected} style={{color: "#2196f3"}} />
                                        </TableCell>
                                    }
                                    {columns.map(col => (
                                        <TableCell key={col.name} align={col.numeric ? 'right' : 'left'}>
                                            {row[col.name]}
                                        </TableCell>
                                    ))}
                                    <TableCell className={classes.actionCell}>
                                        <Fade in={isHovered}>
                                            <div className={classes.actions}>
                                                <Tooltip title="ویرایش">
                                                    <IconButton
                                                        onClick={() => onEditRow && onEditRow(row)}
                                                        size="small"
                                                        className={classes.editBtn}
                                                    >
                                                        <EditIcon />
                                                    </IconButton>
                                                </Tooltip>
                                                <Tooltip title="حذف">
                                                    <IconButton
                                                        onClick={() => onDeleteRow && onDeleteRow(row)}
                                                        size="small"
                                                        className={classes.deleteBtn}
                                                    >
                                                        <DeleteIcon />
                                                    </IconButton>
                                                </Tooltip>
                                                {actions.map((action, i) => (
                                                    <Tooltip key={i} title={action.title}>
                                                        <IconButton onClick={() => action.onClick(row)} size="small">
                                                            {action.icon}
                                                        </IconButton>
                                                    </Tooltip>
                                                ))}
                                            </div>
                                        </Fade>
                                    </TableCell>
                                </TableRow>
                            );
                        })}
                    </TableBody>
                </Table>
            </TableContainer>
            {pagination &&
                <TablePagination
                    rowsPerPageOptions={[5, 10, 25, 50]}
                    component="div"
                    count={filteredRows.length}
                    rowsPerPage={rowsPerPage}
                    page={page}
                    labelRowsPerPage="تعداد سطر در هر صفحه"
                    labelDisplayedRows={({ from, to, count }) => `نمایش ${from} تا ${to} از ${count}`}
                    onChangePage={handleChangePage}
                    onChangeRowsPerPage={handleChangeRowsPerPage}
                    className={classes.pagination}
                />
            }
        </Paper>
    );
}

export default ModernTable;