import Switch from "@material-ui/core/Switch";
import DisplayField from "./DisplayField";
import React, {useState} from "react";
import PropTypes from "prop-types";
import InputAdornment from '@material-ui/core/InputAdornment';
import Typography from "@material-ui/core/Typography";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import TableCell from "@material-ui/core/TableCell";
import Checkbox from "@material-ui/core/Checkbox";
import TableSortLabel from "@material-ui/core/TableSortLabel";
import CircularProgress from "@material-ui/core/CircularProgress";
import TableBody from "@material-ui/core/TableBody";
import {useDispatch} from "react-redux";
import FormInput from "./FormInput";
import Tooltip from "@material-ui/core/Tooltip";
import IconButton from "@material-ui/core/IconButton";
import CloseIcon from "@material-ui/icons/Close";
import DoneIcon from "@material-ui/icons/Done";
import DeleteIcon from "@material-ui/icons/Delete";
import Dialog from "@material-ui/core/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle";
import DialogActions from "@material-ui/core/DialogActions";
import EditIcon from "@material-ui/icons/Edit";
import TablePagination from "@material-ui/core/TablePagination";
import ToggleButtonGroup from "@material-ui/lab/ToggleButtonGroup";
import ToggleButton from "@material-ui/lab/ToggleButton";
import AddIcon from "@material-ui/icons/Add";
import FilterListRoundedIcon from "@material-ui/icons/FilterListRounded";
import Collapse from "@material-ui/core/Collapse";
import CardHeader from "@material-ui/core/CardHeader";
import TableContainer from "@material-ui/core/TableContainer";
import Table from "@material-ui/core/Table";
import SearchIcon from '@material-ui/icons/Search';
import {makeStyles} from "@material-ui/core/styles";
import Draggable from "react-draggable";
import {Box, Button, Grid, TextField} from '@material-ui/core';
import {ALERT_TYPES, setAlertContent} from "../../store/actions/nama";
import CloudDownloadIcon from "@material-ui/icons/CloudDownload";
import axios from "axios";
import {errorText, marginButton, requestText, toastTop} from "../../../configs";
import {toast} from "react-toastify";
import 'react-toastify/dist/ReactToastify.css';

function requiredPlaceholder(label, required) {
    return `${label}${required ? '*' : ''}`
}
function validateForm(columns, formValues, setFormValidation) {
    return new Promise((resolve, reject) => {
        let requiredError = false
        let validationBuffer = {}
        const buffer = (group, name, value) => {
            if (group) {
                validationBuffer[group][name] = value
            } else {
                validationBuffer[name] = value
            }
        }
        for (let i in columns) {
            const input = columns[i]
            if (input.required && !formValues[input.name]) {
                buffer(input.group, input.name, {
                    error: true
                })
                requiredError = true
            }
        }
        setFormValidation(validationBuffer)
        if (requiredError) {
            reject()
        } else {
            resolve()
        }
    })
}
function getDateString(date) {
    let moment = require('moment-jalaali')
    return date ? moment(date).format('jYYYY/jM/jD') : "-"
}
function showIndicator(value, indicator) {
    return <Switch checked={value === indicator.true} size="small" style={{cursor: 'default'}}/>
}
function getCellContent(col, rowData, index,handleCheckBox) {

    let value;
    const changeCheckbox = () => {
        handleCheckBox(rowData)
    }

    switch (col.type) {
        case "date":
            value = getDateString(rowData[col.name])
            break
        case "select":
        case "multiselect":
            const otherProps = {
                ...(col.optionLabelField && {optionLabelField: col.optionLabelField}),
                ...(col.optionIdField && {optionIdField: col.optionIdField}),
                ...(col.type === "multiselect" && {multiselect: true}),
            }
            value = <DisplayField value={rowData[col.name]} options={col.options}
                                  appendOptions={col.appendOptions} {...otherProps}/>//getEnumDescription(enums,col.options,rowData[col.name])
            break
        case "indicator":
            value = showIndicator(rowData[col.name], col.indicator ?? {true: "Y", false: "N"})
            break
        case "render":
            value = col.render(rowData)
            break
        case "checkbox":
            value = <Checkbox onChange={changeCheckbox} checked={rowData[col.name] ? rowData[col.name] : false}
                              style={{padding: '0'}}/>
            break
        default:
            value = rowData[col.name] ?? "-";
    }
    return value
}


class TableProBase extends React.Component {

    static defaultProps = {
        texts: {},
        defaultPage:5,
        className: "",
        title: "",
        defaultOrderBy: "",
        columns: [],
        rows: [],
        control:{},
        originalRows:[],
        setRows: null,
        getExcelDate:null,
        selectedRows: [],
        contentSearch:false,
        setSelectedRows: () => {
            return false
        },
        isSelected: (row, selectedRows) => selectedRows.indexOf(row) !== -1,
        loading: false,
        size: "medium",
        showTitleBar: true,
        showRowNumber: true,
        fixedLayout: false,
        pagination: true,
        selectable: false,
        singleSelect: false,
        actions: [],
        rowActions: [],
        handleCell:null,
        handleDrag:null,
        handleDrop:null,
        draggable: false,
        paginationTop:false,
        exportExcel:false,
        filter: false,
        filterForm: null,
        add: false,
        addForm: null,
        addCallback: null,
        edit: false,
        editForm: null,
        editCallback: null,
        editCondition: () => {
            return true
        },
        removeCallback: null,
        removeCondition: () => {
            return true
        },
        exportCsv: null,
        handleCheckBox: null,
        csvRenderer: null,
        classes: {},
        sortable: true,
        addTitle: "افزودن",
        search: false,
        immediateEdit: false,
        immediateEditRemoveCallback: null,
        immediateEditModificationCallback: null,
        changeRowsPerPage:null,
    }
    static propTypes = {
        texts: PropTypes.objectOf(PropTypes.string),
        className: PropTypes.string,
        defaultPage: PropTypes.string,
        title: PropTypes.string,
        defaultOrderBy: PropTypes.string,
        columns: PropTypes.arrayOf(PropTypes.object).isRequired,
        rows: PropTypes.arrayOf(PropTypes.object),
        setRows: PropTypes.func,
        control: PropTypes.arrayOf(PropTypes.object),
        selectedRows: PropTypes.arrayOf(PropTypes.object),
        setSelectedRows: PropTypes.func,
        isSelected: PropTypes.func,
        loading: PropTypes.bool,
        size: PropTypes.oneOf(["small", "medium"]),
        showTitleBar: PropTypes.bool,
        showRowNumber: PropTypes.bool,
        paginationTop: PropTypes.bool,
        contentSearch: PropTypes.bool,
        exportExcel: PropTypes.bool,
        rowNumberWidth: PropTypes.string,
        fixedLayout: PropTypes.bool,
        handleCell:PropTypes.func,
        handleDrag:PropTypes.func,
        handleDrop:PropTypes.func,
        draggable: PropTypes.bool,
        pagination: PropTypes.bool,
        removeCallback: PropTypes.func,
        removeCondition: PropTypes.func,
        selectable: PropTypes.bool,
        singleSelect: PropTypes.bool,
        actions: PropTypes.arrayOf(PropTypes.object),
        rowActions: PropTypes.arrayOf(PropTypes.object),
        filter: PropTypes.oneOf([false, "external"]),
        filterForm: PropTypes.node,
        add: PropTypes.oneOf([false, "external", "inline"]),
        addForm: PropTypes.node,
        addCallback: PropTypes.func,
        edit: PropTypes.oneOf([false, "external", "inline", "callback"]),
        editForm: PropTypes.node,
        editCallback: PropTypes.func,
        editCondition: PropTypes.func,
        exportCsv: PropTypes.string,
        csvRenderer: PropTypes.func,
        sortable: PropTypes.bool,
        addTitle: PropTypes.string,
        search: PropTypes.bool,
        immediateEdit: PropTypes.bool,
        immediateEditRemoveCallback: PropTypes.func,
        immediateEditModificationCallback: PropTypes.func,
        handleCheckBox: PropTypes.func,
        changeRowsPerPage: PropTypes.func,
        getExcelData: PropTypes.func,
    }
    constructor(props) {
        super(props);
        this.defaultTexts = {
            removeDialog: "آیا از حذف این ردیف اطمینان دارید؟",
            removeAlertSuccess: "ردیف مورد نظر با موفقیت حذف شد.",
            removeAlertFailed: "خطا در عملیات حذف!",
            editAlertSuccess: "تغییرات ردیف مورد نظر با موفقیت انجام شد.",
            editAlertFailed: "خطا در عملیات ویرایش!",
            addAlertSuccess: "ردیف مورد نظر با موفقیت اضافه شد.",
            addAlertFailed: "خطا در عملیات افزودن!",
            alertFormRequired: "باید تمام فیلدهای ضروری وارد شوند!",
        }
        this.state = {
            order: "asc",
            orderBy: "",
            rowsPerPage: 5,
            page: 0,
            rows: [],
            // selected:       [],
            formData: {},
            showForm: null,
            showFilter: false,
            showAddForm: false,
            paginationTop: false,
            filterRows:[]
        }
    }
    shouldComponentUpdate(nextProps, nextState, nextContext) {
        this.state.allRows=nextProps.rows
        if (nextProps.rows !== this.state.rows) {
            this.setState({page: 0, rows: nextProps.rows})
        }
        return true
    }
    componentDidMount() {
        this.setState({orderBy: this.props.defaultOrderBy})
    }
    CellContent=({col, rowData, index, textColor,handleCell,handleCheckBox})=> {
        let value = getCellContent(col, rowData, index,handleCheckBox);
        const handleClickCell=()=>{
            if(handleCell) {
                handleCell(rowData, col)
            }
        }
        const useStyles = makeStyles((theme) => ({
            selectedColumn:{
                cursor:"pointer"
            }
        }));
        const classes = useStyles();
        return (
            <Typography  onClick={()=>{handleClickCell()}} className={handleCell?classes.selectedColumn:""} noWrap={col.noWrap??true} style={{color: textColor ?? "#01579b"}}>
                {value}
            </Typography>
        )
    }
    HeadCellSelect = () => {
        const {
            selectedRows,
            setSelectedRows,
            singleSelect,
            rowNumberWidth = this.props.fixedLayout ? "35px" : "1%"
        } = this.props
        const {rows} = this.state
        const numSelected = selectedRows.length;
        const rowCount = rows.length;
        const handleSelectAllClick = (event) => {
            if (event.target.checked) {
                setSelectedRows(rows);
                return;
            }
            setSelectedRows([]);
        };
        return (
            <TableCell padding={"checkbox"} style={{width: rowNumberWidth}}>
                {!singleSelect &&
                    <Checkbox
                        indeterminate={numSelected > 0 && numSelected < rowCount}
                        checked={rowCount > 0 && numSelected === rowCount}
                        onChange={handleSelectAllClick}
                        inputProps={{'aria-label': 'select all desserts'}}
                        style={{padding: '0'}}
                    />
                }
            </TableCell>
        )
    }
    HeadCellNumber = () => {
        const {rowNumberWidth = this.props.fixedLayout ? "35px" : "1%"} = this.props
        return (
            <TableCell style={{width: rowNumberWidth}}>
                <Typography noWrap>ردیف</Typography>
            </TableCell>
        )
    }
    HeadCellLabel = ({col,key}) => {

        const {classes, sortable,draggable} = this.props
        const {orderBy, order} = this.state;
        const handleRequestSort = (event, property) => {
            const isAsc = orderBy === property && order === 'asc';
            this.setState({
                order: isAsc ? 'desc' : 'asc',
                orderBy: property
            });
        };
        const createSortHandler = (property) => (event) => {
            handleRequestSort(event, property);
        };
        const useStyles = makeStyles({  table: {    minWidth: 650  },
            sticky: {position: "sticky",    left: 0,   zIndex:214124124124 }});
        const classesTable=useStyles()

        return (
            <TableCell key={col.name} style={col.style} className={col.name=="name" && draggable  ?classesTable.sticky:""}>
                {(col.sortable !== false && sortable) ? (
                    <TableSortLabel
                        active={orderBy === col.name}
                        direction={orderBy === col.name ? order : 'asc'}
                        onClick={createSortHandler(col.name)}
                        classes={{root: classes.sortLabelActive, icon: classes.sortLabelActive}}
                    >
                        <Typography noWrap>{col.label}</Typography>
                    </TableSortLabel>
                ) : (
                    <Typography noWrap>{col.label}</Typography>
                )}
            </TableCell>
        )
    }
    THead = () => {
        const {columns, showRowNumber, selectable} = this.props
        const HeadCellSelect = this.HeadCellSelect.bind(this)
        const HeadCellNumber = this.HeadCellNumber.bind(this)
        const HeadCellLabel = this.HeadCellLabel.bind(this)
        return (
            <TableHead>
                <TableRow>
                    {selectable && <HeadCellSelect/>}
                    {showRowNumber && <HeadCellNumber/>}
                    {columns.map((col, ind) => (
                        <HeadCellLabel col={col} key={ind}/>
                    ))}
                </TableRow>
            </TableHead>
        )
    }
    RowLoading=({colSpan, height})=> {
        return (
            <TableRow style={{height: height}}>
                <TableCell colSpan={colSpan}>
                    <Box textAlign="center" color="text.secondary">
                        <CircularProgress/>
                        <Typography variant={"body1"}>در حال دریافت اطلاعات</Typography>
                    </Box>
                </TableCell>
            </TableRow>
        )
    }
    RowEmpty=({colSpan, height})=> {
        return (
            <TableRow style={{height: height}}>
                <TableCell colSpan={colSpan}>
                    <Typography color={"textSecondary"} align={"center"}>
                        داده ای وجود ندارد!
                    </Typography>
                </TableCell>
            </TableRow>
        )
    }
    RowRem=({colSpan, height})=> {
        return (
            <TableRow style={{height: height}}>
                <TableCell colSpan={colSpan}>
                    <Typography color={"textSecondary"}/>
                </TableCell>
            </TableRow>
        )
    }
    RenderRows=({rows})=> {
        const {orderBy, order, rowsPerPage, page} = this.state;
        const {control}=this.props
        function descendingComparator(a, b, orderBy) {
            if (!orderBy) {
                return 1;
            }
            if (!a[orderBy]) {
                return -1;
            }
            if (!b[orderBy]) {
                return 1;
            }
            if (b[orderBy] < a[orderBy]) {
                return -1;
            }
            if (b[orderBy] > a[orderBy]) {
                return 1;
            }
            return 0;
        }

        function getComparator(order, orderBy) {
            return order === 'desc'
                ? (a, b) => descendingComparator(a, b, orderBy)
                : (a, b) => -descendingComparator(a, b, orderBy);
        }

        function stableSort(array, comparator) {
            const stabilizedThis = array.map((el, index) => [el, index]);
            stabilizedThis.sort((a, b) => {
                const order = comparator(a[0], b[0]);
                if (order !== 0) return order;
                return a[1] - b[1];
            });
            return stabilizedThis.map((el) => el[0]);
        }

        const TRow = this.TRow.bind(this)

        let rpp=control["rowsPerPage"]??rowsPerPage
        let p=control["page"]??page

        return stableSort(rows, getComparator(order, orderBy))
            .slice(p * rpp, p * rpp + rpp)
            .map((rowData, index) => (
                    <TRow  key={index} index={page * rowsPerPage + index} rowData={rowData}/>
                )
            )
    }
    RenderRemindRow=()=> {
        const {columns, pagination, selectable, showRowNumber} = this.props;
        const {rowsPerPage, page, rows} = this.state;
        const colsCount = columns.length + (showRowNumber ? 1 : 0) + (selectable ? 1 : 0)
        const RowRem = this.RowRem.bind(this)
        const rem = rowsPerPage - rows.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage).length;
        const remRowsHeight = `${53 * rem}px`;
        if (pagination && rem > 0)
            return <RowRem colSpan={colsCount} height={remRowsHeight}/>
        else return null
    }
    TBody = () => {
        const {columns, loading, selectable, showRowNumber, pagination} = this.props;
        const {rowsPerPage, rows} = this.state;
        const rowsHeight = pagination ? `${53 * rowsPerPage}px` : "53px";
        const colsCount = columns.length + (showRowNumber ? 1 : 0) + (selectable ? 1 : 0)
        const RowLoading = this.RowLoading.bind(this)
        const RowEmpty = this.RowEmpty.bind(this)
        const RenderRows = this.RenderRows.bind(this)
        const RenderRemindRow = this.RenderRemindRow.bind(this)
        return (
            <TableBody>
                {loading ? (
                    <RowLoading colSpan={colsCount} height={rowsHeight}/>
                ) : rows.length > 0 ? (
                    <>
                        <RenderRows rows={rows}/>
                        <RenderRemindRow/>
                    </>
                ) : (
                    <RowEmpty colSpan={colsCount} height={rowsHeight}/>
                )}
            </TableBody>
        )
    }
    TRow=({index, rowData})=> {
        const {
            classes,
            selectedRows,
            setSelectedRows,
            singleSelect,
            isSelected,
            selectable,
            showRowNumber
        } = this.props;
        const [removing, setRemoving] = React.useState(false);
        const [editing, setEditing] = React.useState(false);
        const RenderCellsEdit = this.RenderCellsEdit.bind(this)
        const RenderCellsShow = this.RenderCellsShow.bind(this)
        const handleClick = (event, name) => {
            if (selectable) {
                const selectedIndex = selectedRows.indexOf(name);
                let newSelected = [];

                if (singleSelect) {
                    newSelected = newSelected.concat(name);
                } else {
                    if (selectedIndex === -1) {
                        newSelected = newSelected.concat(selectedRows, name);
                    } else if (selectedIndex === 0) {
                        newSelected = newSelected.concat(selectedRows.slice(1));
                    } else if (selectedIndex === selectedRows.length - 1) {
                        newSelected = newSelected.concat(selectedRows.slice(0, -1));
                    } else if (selectedIndex > 0) {
                        newSelected = newSelected.concat(
                            selectedRows.slice(0, selectedIndex),
                            selectedRows.slice(selectedIndex + 1),
                        );
                    }
                }
                setSelectedRows(newSelected)
            }
        };
        const isItemSelected = isSelected(rowData, selectedRows);
        const useStyles = makeStyles({  table: {    minWidth: 650  },
            sticky: {position: "sticky",    left: 0,    background: "white",    boxShadow: "5px 2px 5px grey",    borderRight: "2px solid black"  }});
        const classesTable=useStyles()
        return (
            <TableRow key={index} hover selected={isItemSelected}
                      className={((index % 2 == 0 && !removing && !isItemSelected) ? classes.evenRowBackGround : (removing || isItemSelected) ? classes.selected : "")}
                      onClick={(event) => handleClick(event, rowData)}>
                {selectable && (
                    <TableCell padding="checkbox">
                        <Checkbox checked={isItemSelected} style={{padding: '0'}}/>
                    </TableCell>
                )}
                {showRowNumber &&
                    <TableCell>{index + 1}</TableCell>
                }
                {editing ? (
                    <RenderCellsEdit  rowData={rowData} setEditing={setEditing}/>
                ) : (
                    <RenderCellsShow  rowData={rowData} setEditing={setEditing} removing={removing}
                                      setRemoving={setRemoving}/>
                )}
            </TableRow>
        )
    }
    RenderCellsEdit=({rowData, setEditing})=> {
        const {classes, columns, setRows, editCallback} = this.props;
        const {rows} = this.state
        const texts = Object.assign({}, this.defaultTexts, this.props.texts)
        const dispatch = useDispatch();
        const [loading, setLoading] = React.useState(false);
        const [formData, setFormData] = React.useState(rowData);
        const [formValidation, setFormValidation] = useState({});

        function handleEditCancel() {
            setEditing(false)
        }

        function handleEdit() {
            setLoading(true)
            validateForm(columns, formData, setFormValidation).then(() => {
                const ind = rows.indexOf(rowData)
                editCallback(formData, rowData, ind).then(() => {
                    setLoading(false)
                    setEditing(false)
                    let newData = Object.assign([], rows);
                    newData[ind] = formData;
                    setRows(newData)
                    dispatch(setAlertContent(ALERT_TYPES.SUCCESS, texts.editAlertSuccess));
                }).catch((message = texts.editAlertFailed) => {
                    setLoading(false)
                    setEditing(false)
                    dispatch(setAlertContent(ALERT_TYPES.ERROR, message));
                })
            }).catch(() => {
                setLoading(false)
                dispatch(setAlertContent(ALERT_TYPES.ERROR, texts.alertFormRequired));
            })
        }

        return (
            <React.Fragment>
                {columns.slice(0, -1).map((col, ind) => (
                    <TableCell key={ind} style={{...col.style}}  >
                        <FormInput {...col} style={{}} placeholder={requiredPlaceholder(col.label, col.required)}
                                   label={null} size="small" variant="standard" grid={false} valueObject={formData}
                                   valueHandler={setFormData} validationObject={formValidation}
                                   validationHandler={setFormValidation}/>
                    </TableCell>
                ))}
                {columns.slice(-1).map((col, ind) => (
                    <TableCell key={ind} style={{...col.style, paddingLeft: '110px'}}  >
                        <FormInput {...col} style={{}} label={null}
                                   placeholder={requiredPlaceholder(col.label, col.required)} size="small"
                                   variant="standard" grid={false} valueObject={formData} valueHandler={setFormData}
                                   validationObject={formValidation} validationHandler={setFormValidation}/>
                        <div className={classes.editingActionBox}>
                            {this.props.immediateEdit ?
                                <div style={{display: "flex"}}>
                                    {this.props.immediateEditRemoveCallback ?
                                        <Tooltip title="حذف">
                                            <IconButton onClick={this.props.immediateEditRemoveCallback}
                                                        size={texts.size} disabled={loading}>
                                                <DeleteIcon style={{color: "#d32f2f"}}/>
                                            </IconButton>
                                        </Tooltip> : ""}
                                    {this.props.immediateEditModificationCallback ?
                                        <Tooltip title="حذف">
                                            <IconButton onClick={this.props.immediateEditModificationCallback}
                                                        size={texts.size} disabled={loading}>
                                                <EditIcon style={{color: "#1976d2"}}/>
                                            </IconButton>
                                        </Tooltip> : ""}
                                </div>
                                :
                                <div>
                                    <Tooltip title="لغو">
                                        <IconButton onClick={handleEditCancel} size={texts.size} disabled={loading}>
                                            <CloseIcon/>
                                        </IconButton>
                                    </Tooltip>
                                    <Tooltip title="تایید">
                                        <IconButton onClick={handleEdit} size={texts.size}>
                                            {loading ? <CircularProgress size={24}/> : <DoneIcon/>}
                                        </IconButton>
                                    </Tooltip>
                                </div>
                            }
                        </div>
                    </TableCell>
                ))}
            </React.Fragment>
        )
    }
    RenderCellsShow=({rowData, setEditing, removing, setRemoving})=> {
        const {
            classes,
            columns,
            setRows,
            rowActions,
            size,
            edit,
            editCallback,
            removeCallback,
            removeCondition,
            editCondition,
            handleCell,
            handleDrag,
            handleDrop,
            draggable,
            handleCheckBox
        } = this.props;
        const {rows} = this.state
        const texts = Object.assign({}, this.defaultTexts, this.props.texts)
        const [displayDialog, setDisplayDialog] = React.useState(false);
        const [loading, setLoading] = React.useState(false);
        const dispatch = useDispatch();
        React.useEffect(() => {
            if (removing) {
                setDisplayDialog(true);
            }
        }, [removing])
        function handleRemoveConfirm() {
            setRemoving(true);
        }
        function handleRemoveCancel() {
            setRemoving(false)
            setDisplayDialog(false);
        }
        function handleRemove() {
            setLoading(true);
            setDisplayDialog(false)
            removeCallback(rowData).then((message = texts.removeAlertSuccess) => {
                setLoading(false)
                setRemoving(false)
                let newData = Object.assign([], rows);
                const ind = newData.indexOf(rowData)
                newData.splice(ind, 1);
                setRows(newData)
                dispatch(setAlertContent(ALERT_TYPES.SUCCESS, message));
            }).catch((message = texts.removeAlertFailed) => {
                setLoading(false)
                setRemoving(false)
                dispatch(setAlertContent(ALERT_TYPES.ERROR, message));
            })
        }
        const handleEdit = () => {
            switch (edit) {
                case "inline":
                    setEditing(true)
                    break;
                case "external":
                    this.toggleShowForm('edit-external', rowData)
                    break;
                case "callback":
                    editCallback(rowData)
                    break;
                default:
            }
        }
        const index = rows.indexOf(rowData)
        const dragFunc=(event,col,rowData)=>{
            handleDrag(event,col,rowData)
        }
        const handleDragOver=(event)=>{
            event.preventDefault()
        }
        const dropFunc=(event,rowData)=>{
            handleDrop(event,rowData)
        }
        const useStyles = makeStyles({  table: {    minWidth: 650  },
            sticky: {position: "sticky" ,    right: 0, zIndex:124124124 ,    background: "white",    boxShadow: "5px 2px 5px grey",    borderRight: "2px solid black"  }});
        const classesTable=useStyles()
        return (
            <React.Fragment>
                {draggable ?columns.slice(0, -1).map((col, idx) => (
                        <Draggable>
                            {idx==0 || idx==-1 ?
                                <TableCell style={{position: "sticky" , backgroundColor:"#eeeeee" ,  right: 0, zIndex:124124124 }} onDrop={(event) => {
                                    dropFunc(event, rowData)
                                }} onDragOver={handleDragOver} onDragStart={(event, data) => {
                                    dragFunc(event, col, rowData)
                                }} draggable={true} key={idx + col.name}>
                                    <this.CellContent col={col} rowData={rowData} index={idx} textColor={col.textColor}
                                                      handleCell={handleCell}  handleCheckBox={handleCheckBox}/>
                                </TableCell>
                                :    <TableCell  onDrop={(event) => {
                                    dropFunc(event, rowData)
                                }} onDragOver={handleDragOver} onDragStart={(event, data) => {
                                    dragFunc(event, col, rowData)
                                }} draggable={true} key={idx + col.name} style={col.style} >
                                    <this.CellContent col={col} rowData={rowData} index={idx} textColor={col.textColor} handleCheckBox={handleCheckBox}
                                                      handleCell={handleCell}/>
                                </TableCell>
                            }
                        </Draggable>

                    )):
                    columns.slice(0, -1).map((col, idx) => {
                        return(
                            <TableCell key={idx + col.name} style={col.style} rowSpan={rowData?.rowSpan??1} >
                                <this.CellContent  col={col} rowData={rowData} index={idx} textColor={col.textColor} handleCell={handleCell} handleCheckBox={handleCheckBox}/>
                            </TableCell>
                        )}
                    )}
                {columns.slice(-1).map((col, idx) => (
                    <TableCell  draggable={true} key={col.name} style={col.style}   >
                        <this.CellContent  col={col} rowData={rowData} index={index} handleCheckBox={handleCheckBox}/>
                        <div className={classes.actionBox}>
                            {rowActions.filter(i => !i.display || (i.display && i.display(rowData) !== false)).map((act, idx) => (
                                <Tooltip key={idx} title={act.title}>
                                    <IconButton onClick={() => act.onClick(rowData)} size={size}>
                                        <act.icon style={{color: act.color}}/>
                                    </IconButton>
                                </Tooltip>
                            ))}
                            {(removeCallback && removeCondition(rowData)) &&
                                <React.Fragment>
                                    <Tooltip title="حذف">
                                        <IconButton onClick={handleRemoveConfirm} size={size}>
                                            {loading ? <CircularProgress size={24}/> :
                                                <DeleteIcon style={{color: "#d32f2f"}}/>}
                                        </IconButton>
                                    </Tooltip>
                                    <Dialog open={displayDialog}
                                            onClose={handleRemoveCancel}
                                            aria-labelledby="alert-dialog-title"
                                            aria-describedby="alert-dialog-description">
                                        <DialogTitle id="alert-dialog-title">{texts.removeDialog}</DialogTitle>
                                        {/*<DialogContent>*/}
                                        {/*    <DialogContentText id="alert-dialog-description">{texts.removeDialog}</DialogContentText>*/}
                                        {/*</DialogContent>*/}
                                        <DialogActions>
                                            <Button onClick={handleRemoveCancel} color="primary">خیر</Button>
                                            <Button onClick={handleRemove} color="primary" autoFocus>بلی</Button>
                                        </DialogActions>
                                    </Dialog>
                                </React.Fragment>
                            }
                            {(edit && editCondition(rowData) && this.props.immediateEdit) ? handleEdit() :
                                (edit && editCondition(rowData)) ?
                                    <Tooltip title="ویرایش">
                                        <IconButton onClick={handleEdit} size={size}>
                                            <EditIcon style={{color: "#1976d2"}}/>
                                        </IconButton>
                                    </Tooltip>
                                    : ""
                            }
                        </div>
                    </TableCell>
                ))}
            </React.Fragment>
        )
    }
    toggleShowForm = (form, formData = {}) => {
        if (form && form !== this.state.showForm) {
            this.setState({
                showForm: form,
                formData
            })
        } else {
            this.setState({
                showForm: null,
                formData: {}
            })
        }
    }
    ExternalAddForm = () => {
        const {classes = {}, setRows} = this.props;
        const {rows} = this.state
        const texts = Object.assign({}, this.defaultTexts, this.props.texts)
        const dispatch = useDispatch();
        const [formValues, setFormValues] = React.useState({});
        const addSuccessCallback = (newData) => {
            setRows(rows.concat(newData))
            dispatch(setAlertContent(ALERT_TYPES.SUCCESS, texts.addAlertSuccess));
            this.toggleShowForm(null)
        }
        const addFailedCallback = (message = texts.addAlertFailed) => {
            dispatch(setAlertContent(ALERT_TYPES.ERROR, message));
        }
        return (
            <Collapse in={this.state.showForm === "add-external"} className={classes.borderTop}>
                <Box p={2}>
                    {React.cloneElement(this.props.addForm, {
                        formValues, setFormValues,
                        successCallback: addSuccessCallback,
                        failedCallback: addFailedCallback,
                        handleClose: () => this.toggleShowForm(null)
                    })}
                </Box>
            </Collapse>
        )
    }
    ExternalEditForm = () => {
        const [formValues, setFormValues] = React.useState(this.state.formData);
        const {classes, setRows} = this.props;
        const {rows} = this.state
        const texts = Object.assign({}, this.defaultTexts, this.props.texts)
        const dispatch = useDispatch();
        const oldData = this.state.formData
        const editSuccessCallback = (newData) => {
            let newRows = Object.assign([], rows)
            const ind = newRows.indexOf(oldData)
            newRows[ind] = newData
            setRows(newRows)
            dispatch(setAlertContent(ALERT_TYPES.SUCCESS, texts.editAlertSuccess));
            this.toggleShowForm(null)
        }
        const editFailedCallback = (message = texts.editAlertFailed) => {
            dispatch(setAlertContent(ALERT_TYPES.ERROR, message));
        }
        return (
            <Collapse in={this.state.showForm === "edit-external"} className={classes.borderTop}>
                <Box p={2}>
                    {React.cloneElement(this.props.editForm, {
                        formValues, setFormValues, oldData,
                        successCallback: editSuccessCallback,
                        failedCallback: editFailedCallback,
                        handleClose: () => this.toggleShowForm(null)
                    })}
                </Box>
            </Collapse>
        )
    }
    InlineAddForm=()=> {
        const {classes, columns, setRows, showRowNumber, addCallback} = this.props;
        const {rows} = this.state
        const texts = Object.assign({}, this.defaultTexts, this.props.texts)
        const dispatch = useDispatch();
        const [loading, setLoading] = React.useState(false);
        const [formData, setFormData] = React.useState({});
        const [formValidation, setFormValidation] = useState({});
        const handleAddConfirm = () => {
            setLoading(true)
            validateForm(columns, formData, setFormValidation).then(() => {
                addCallback(formData).then((newData) => {
                    setRows(rows.concat(newData))
                    setLoading(false)
                    handleAddCancel()
                    dispatch(setAlertContent(ALERT_TYPES.SUCCESS, texts.addAlertSuccess));
                }).catch((message = texts.addAlertFailed) => {
                    setLoading(false)
                    dispatch(setAlertContent(ALERT_TYPES.ERROR, message));
                })
            }).catch(() => {
                setLoading(false)
                dispatch(setAlertContent(ALERT_TYPES.ERROR, texts.alertFormRequired));
            })

        }
        const handleAddCancel = () => {
            setFormData({})
            this.toggleShowForm(null)
        }
        return (
            <TableRow className={classes.formRow}>
                {showRowNumber && <TableCell/>}
                {columns.slice(0, -1).map((col, ind) => (
                    <TableCell key={ind + col.name} style={{...col.style}}>
                        <FormInput {...col} style={{}} label={null}
                                   placeholder={requiredPlaceholder(col.label, col.required)} size="small"
                                   variant="standard" grid={false} valueObject={formData} valueHandler={setFormData}
                                   validationObject={formValidation} validationHandler={setFormValidation}/>
                    </TableCell>
                ))}
                {columns.slice(-1).map((col) => (
                    <TableCell key={col.name} style={{...col.style, paddingLeft: '110px'}}>
                        <FormInput {...col} style={{}} label={null}
                                   placeholder={requiredPlaceholder(col.label, col.required)} size="small"
                                   variant="standard" grid={false} valueObject={formData} valueHandler={setFormData}
                                   validationObject={formValidation} validationHandler={setFormValidation}/>
                        <div className={classes.editingActionBox}>
                            <Tooltip title="لغو">
                                <IconButton onClick={handleAddCancel} size={texts.size} disabled={loading}>
                                    <CloseIcon/>
                                </IconButton>
                            </Tooltip>
                            <Tooltip title="تایید">
                                <IconButton onClick={handleAddConfirm} size={texts.size}>
                                    {loading ? <CircularProgress size={24}/> : <DoneIcon/>}
                                </IconButton>
                            </Tooltip>
                        </div>
                    </TableCell>
                ))}
            </TableRow>
        )
    }
    TTool = () => {
        const InlineAddForm = this.InlineAddForm.bind(this)
        if (this.state.showForm === "add-inline")
            return (
                <TableBody>
                    <InlineAddForm/>
                </TableBody>
            )
        return null
    }
    ActionGroup = () => {
        const {classes} = this.props;
        return (
            <React.Fragment>
                <ToggleButtonGroup size="small" className={classes.marginEnd}>
                    {this.props.actions.map((act, ind) => (
                        <Tooltip key={ind} title={act.title}>
                            <ToggleButton onClick={act.onClick} size={"large"}>
                                <act.icon style={{color: act.color}}/>
                            </ToggleButton>
                        </Tooltip>
                    ))}
                    {this.props.exportCsv && (<></>)}
                </ToggleButtonGroup>
                <ToggleButtonGroup size="large">
                    {this.props.add && (
                        <Tooltip title={this.props.addTitle}>
                            <ToggleButton
                                size={"large"}
                                onClick={() => this.toggleShowForm(`add-${this.props.add}`)}
                                selected={this.state.showForm === `add-${this.props.add}`} value="showAddForm"
                            >
                                <AddIcon style={{color: "#558b2f"}}/>
                            </ToggleButton>
                        </Tooltip>
                    )}
                    {this.props.filter && (
                        <Tooltip title="فیلتر">
                            <ToggleButton
                                size={"large"}
                                onClick={() => this.toggleShowForm(`filter-${this.props.filter}`)}
                                selected={this.state.showForm === `filter-${this.props.filter}`} value="showFilter">
                                <FilterListRoundedIcon/>
                            </ToggleButton>
                        </Tooltip>
                    )}
                </ToggleButtonGroup>
            </React.Fragment>
        )
    }
    Pagination = () => {

        const handleChangePage = (event, newPage) => {
            this.setState({page: newPage});
        };
        const handleChangeRowsPerPage = (event) => {
            this.setState({
                page: 0,
                rowsPerPage: parseInt(event.target.value, 10)
            });
        };
        return (
            <TablePagination
                rowsPerPageOptions={[5, 10, 25,30,50,75,100]}
                component="div"
                count={this.state.rows.length}
                rowsPerPage={this.state.rowsPerPage}
                page={this.state.page}
                onChangePage={handleChangePage}
                onChangeRowsPerPage={handleChangeRowsPerPage}
                backIconButtonText="صفحه قبلی"
                nextIconButtonText="صفحه بعدی"
                labelRowsPerPage="میزان نمایش در هر صفحه"
                labelDisplayedRows={({from, to, count}) => (` نمایش ${from} - ${to} از ${count} داده `)}
            />
        )
    }


    render() {
        const {classes} = this.props;
        const originalRows=classes.rows;
        const cx = require('classnames');
        const requestSearch = (event) => {
            let value=event.target.value
            if(value!="") {
                let colName=this.props.columns.map(ele=>ele.name)
                const filteredRows = this.props.originalRows.filter((row) => {
                    let newRow
                    if(this.props.contentSearch==true) newRow=row
                    else newRow = Object.fromEntries(Object.entries(row).filter(([key]) => colName.indexOf(key) != -1));
                    return Object.values(newRow).join("").indexOf(event.target.value.toString()) != -1
                })
                this.props.setRows(filteredRows)
            }
            if(value=="") {
                this.props.setRows(this.props.originalRows)
            }
        }
        const cancelSearch = () => {
            requestSearch("");
        };
        const pageChange=(event)=>{
            let value=event.target.value;
            value=convertToEn(value)
            if(value>1) {
                this.setState({page:value-1})
            }
            else if(value==1 || value==0) {
                this.setState({page:0})
            }
            else {
                this.setState({page:0})
            }
        }
        const convertToEn = (str) => {
            let persianNumbers = [/۰/g, /۱/g, /۲/g, /۳/g, /۴/g, /۵/g, /۶/g, /۷/g, /۸/g, /۹/g]
            let arabicNumbers = [/٠/g, /١/g, /٢/g, /٣/g, /٤/g, /٥/g, /٦/g, /٧/g, /٨/g, /٩/g]
            if (typeof str === 'string') {
                for (let i = 0; i < 10; i++) {
                    str = str.replace(persianNumbers[i], i).replace(arabicNumbers[i], i);
                }
            }
            return str;
        }
        const exportExcel = async () => {
            let postData = {}
            toast.warn(requestText,toastTop);
            let data=this.props.rows
            if(this.props.getExcelData){
                data=await this.props.getExcelData()
            }
            postData.data = data.map((row, ind) => {
                let csvRow = {"ردیف": ind + 1}
                this.props.columns.map(col => {
                    csvRow[col.label] = row[col.name]
                })
                return csvRow
            })
            axios.post("/rest/s1/general/excel", postData, {}).then(res => {
                toast.dismiss()
                const dataEncode = res.data.encoded
                const linkSource = `data:application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;base64,${dataEncode}`;
                const downloadLink = document.createElement("a");
                downloadLink.href = linkSource;
                downloadLink.target = "_self";
                downloadLink.click();
            }).catch(err => {
                toast.dismiss()
                toast.error(errorText,toastTop)
            })
        }

        return (
            <div className={this.props.className}>
                {this.props.showTitleBar && (
                    <CardHeader title={
                        <Grid container spacing={1}>
                            <Grid item xs={5} sm={3}>
                                <TextField variant="outlined" fullWidth margin="normal" placeholder="جستجو"
                                           InputProps={{
                                               endAdornment: <InputAdornment position="left"><SearchIcon/></InputAdornment>,
                                           }}
                                           onChange={requestSearch}
                                />
                            </Grid>
                            <Grid item xs={5} sm={3}>
                                <TextField variant="outlined" fullWidth margin="normal" onChange={pageChange} placeholder="برو به صفحه"/>
                            </Grid>
                            {this.props.exportExcel==true ?
                                <Grid item xs={2} sm={3} style={marginButton}>
                                    <Button onClick={exportExcel} title="خروجی اکسل" variant="contained"
                                            style={{
                                                height: "50px",
                                            }}>
                                        <IconButton variant="contained">
                                            <CloudDownloadIcon variant="contained"></CloudDownloadIcon>
                                        </IconButton>
                                    </Button>
                                </Grid>
                                :""}
                            {this.props.pagination && this.props.paginationTop===true ?
                                <Grid item xs={12} sm={6} style={{marginTop:"30px"}}>
                                    <this.Pagination/>
                                </Grid>
                                :"" }
                        </Grid>
                    }
                                action={<this.ActionGroup/>}
                    />
                )}
                {this.props.filter === "external" && (
                    <Collapse in={this.state.showForm === "filter-external"} className={classes.borderTop}>
                        <Box p={2}>
                            {this.props.filterForm}
                        </Box>
                    </Collapse>
                )}
                {this.props.add === "external" && <this.ExternalAddForm/>}
                {(this.props.edit === "external" && this.state.showForm === "edit-external") &&
                    <this.ExternalEditForm/>}
                <TableContainer>
                    <Table size={this.props.size}
                           className={cx(classes.root, this.props.fixedLayout && classes.tableLayoutFixed, this.props.className)}>
                        <this.THead/>
                        <this.TTool/>
                        <this.TBody/>
                    </Table>
                </TableContainer>
                {this.props.pagination && !this.props.paginationTop ?
                    <this.Pagination/>
                    :""}
            </div>
        )
    }
}


export default TableProBase
