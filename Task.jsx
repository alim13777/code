import React, {useRef, useState} from 'react';
import {CardContent, Grid, IconButton, TextField, Typography} from "@material-ui/core";
import {FusePageSimple} from "../../../../@fuse";
import Card from "@material-ui/core/Card";
import Box from "@material-ui/core/Box";
import Pagination from '@material-ui/lab/Pagination';
import {useDispatch} from "react-redux";
import ListItem from "@material-ui/core/ListItem";
import ListItemText from "@material-ui/core/ListItemText";
import List from "@material-ui/core/List";
import TaskPanel from "./TaskPanel";
import {setAlertContent} from "app/store/actions";
import CardHeader from "@material-ui/core/CardHeader";
import {makeStyles} from "@material-ui/core/styles";
import axios from "axios";
import {errorText, marginButton} from "../../../../configs";
import CircularProgress from "@material-ui/core/CircularProgress";
import ListSubheader from "@material-ui/core/ListSubheader";
import {useLocation} from "react-router-dom";
import InputAdornment from '@material-ui/core/InputAdornment';
import SearchIcon from "@material-ui/icons/Search";
import {ArrowDownwardSharp, ArrowUpwardSharp} from "@material-ui/icons";
import {filterId} from "./taskConfig";
import Autocomplete from "@material-ui/lab/Autocomplete";

const useStyles = makeStyles((theme) => ({
    container: {
        height: "100%"
    },
    sidebar: {
        backgroundColor: theme.palette.primary.light + "0d",
        borderRight: "1px solid #ddd"
    },
    selectedTask: {
        backgroundColor: "#0000073b"
    }
}));

const ViewLoading = () => (
    <Box textAlign="center" color="text.secondary" p={2}>
        <CircularProgress/>
        <Typography variant={"body1"}>در حال دریافت</Typography>
    </Box>
)

const Task = () => {

    const classes = useStyles();
    const dispatch = useDispatch();
    const [action, setAction] = React.useState('Default');
    const [taskList, setTaskList] = React.useState([]);
    const [taskLoading, taskLoadingHandler] = React.useState(true);
    const taskLoadingComplete = () => taskLoadingHandler(false)
    const [task, setTask] = React.useState(null);
    const [taskNumber, setTaskNumber] = useState(null)
    const location = useLocation()
    const [taskData, setTaskData] = React.useState([])
    const [scroll,setScroll]=useState(false)
    const [page, setPage] = React.useState(1)
    const [sortAsc, setSortAsc] = useState(false)
    const [process,setProcess]=useState([])
    const paragraphRef = useRef(null);

    React.useEffect(() => {
        getTask()
    }, [])
    React.useEffect(() => {
        const taskId = new URLSearchParams(location.search).get("taskId")
        if (taskId) {
            setTask(taskList.find(ele => ele.taskId == taskId))
            paragraphRef.current.scrollIntoView()
            setScroll(true)

        }
    }, [taskList])
    React.useEffect(() => {
        const handleEsc = (event) => {
            if (event.keyCode === 27) {
                setTask(null)
                setAction("Default")
            }
        };
        window.addEventListener('keydown', handleEsc);
        return () => {
            window.removeEventListener('keydown', handleEsc);
        };
    }, []);

    const getTask = () => {
        const moment = require('moment-jalaali')
        let url="/rest/s1/process/task"
        axios.get(url, {
            params: {
                filterId: filterId,
                firstResult: 0,
                maxResults: 15,
            }
        }).then(res => {
            if (res.data.state != 1) {
                dispatch(setAlertContent("error", errorText))
                return false
            }
            let rows = [];
            let process = res?.data?._embedded?.processDefinition
            res.data.task.forEach((task) => {
                let entry = {};
                entry.taskName = task.name;
                entry.processKey = task.processKey;
                entry.processName = process?.find(ele => ele.id === task.processDefinitionId)?.name
                entry.recieveDate = moment(task.created).format('HH:mm jYYYY/jM/jD')
                entry.taskId = task.id;
                entry.formKey = task.formKey
                rows.push(entry)
            })
            setTaskList(rows)
            setTaskData(rows)
            setProcess(res.data.processList)
            taskLoadingComplete()
        }).catch(err => {
            dispatch(setAlertContent("error", errorText))
            taskLoadingComplete()
        });
    }
    const loadTask = (e) => {

        const taskId = e.currentTarget.attributes['data-taskid'].value;
        let taskObj = taskList.find(i => i.taskId === taskId)
        setTask(taskObj)
        setAction("Default")
        setTaskNumber(taskList.indexOf(taskObj))
        paragraphRef.current.scrollIntoView()
        setScroll(false)

    }
    const handlePageChange = (event, value) => {
        setPage(value);
    };
    const handleSearch = (event) => {
        setTask(null)
        let name = event?.target?.value
        if (name && name != "") {
            let tasks = taskList.filter(ele => ele.taskName.indexOf(name) != -1);
            setTaskList(tasks)
        } else {
            setTaskList(taskData)
        }
    }
    const sort = () => {
        let allTask = [...taskList]
        allTask.reverse();
        setTaskList(allTask)
        setSortAsc(!sortAsc);
    }
    const handleAutoComplete=(name,value)=>{
        if(value==null){
            let tasks=[...taskData]
            setTaskList(tasks)
            return false;
        }
        if(name=="process"){
            let allTask=[...taskData]
            allTask=allTask.filter(ele=>ele.processKey==value.key)
            setTaskList(allTask)
        }
    }

    return (
        <FusePageSimple header={<CardHeader title={"کارپوشه"}/>} style={{overFlowY:"auto"}}
                        content={
                            <Grid container className={classes.container}>
                                <Grid item xs={12} sm={4} md={3} className={classes.sidebar}>
                                    <List component="nav" aria-labelledby="nested-list-subheader" subheader={<ListSubheader component="div" id="nested-list-subheader">
                                                  <Grid container spacing={0}>
                                                      <Grid item xs={10} sm={11}>
                                                          <TextField  placeholder="لیست کارها"
                                                                     InputProps={{
                                                                         endAdornment: <InputAdornment
                                                                             position="left"><SearchIcon/></InputAdornment>,
                                                                     }} variant="outlined" margin="normal"
                                                                     onChange={(event) => {
                                                                         handleSearch(event)
                                                                     }} fullWidth/>
                                                      </Grid>
                                                      <Grid item xs={2} sm={1} style={marginButton}>
                                                          <IconButton onClick={() => {sort()}} title={sortAsc ? "مرتب سازی نزولی براساس تاریخ" : "مرتب سازی صعودی براساس تاریخ"}>
                                                              {sortAsc ? <ArrowUpwardSharp/> : <ArrowDownwardSharp/>}
                                                          </IconButton>
                                                      </Grid>
                                                  </Grid>
                                                  <Grid container spacing={0}>
                                                      <Grid item xs={12} sm={11}>
                                                      <Autocomplete name="process"
                                                                    onChange={(event, value) => handleAutoComplete("process", value)}
                                                                    renderInput={params => {
                                                                        return <TextField {...params} variant="outlined"
                                                                                          label="فرآیند" margin="normal" fullWidth/>
                                                                    }}
                                                                    options={process??[]}
                                                                    getOptionLabel={option => option?.name}></Autocomplete>
                                                      </Grid>
                                                  </Grid>
                                                  <hr/>
                                              </ListSubheader>}>

                                        {taskLoading ?
                                            <ViewLoading/>
                                            : taskList?.slice(((page - 1) * (10)), ((page) * (10))).map((task, ind) => (
                                                <ListItem button key={ind}
                                                          className={taskNumber != null && (taskNumber == ind || taskNumber==((page-1)*10)+ind) ? classes.selectedTask : null}>
                                                    <ListItemText primary={task.taskName} data-taskid={task.taskId}
                                                                  onClick={loadTask}
                                                                  secondary={<Box display="flex"><Box
                                                                      flexGrow={1}>{task.processName}</Box><Box>{task.recieveDate}</Box></Box>}
                                                                  secondaryTypographyProps={{component: 'div'}}
                                                    />
                                                </ListItem>
                                            ))}
                                        <br/>
                                        <Pagination count={Math.floor(taskList.length / 10) + 1} page={page} onChange={handlePageChange}/>
                                    </List>
                                </Grid>
                                <Grid item xs={12} sm={8} md={9} >
                                    <Box p={2} ref={paragraphRef}>
                                        <Card>
                                            <CardContent >
                                                <TaskPanel task={task} setTaskList={setTaskList} taskList={taskList}
                                                           setTask={setTask} taskNumber={taskNumber} scroll={scroll}
                                                           setTaskNumber={setTaskNumber}/>
                                            </CardContent>
                                        </Card>
                                    </Box>
                                </Grid>
                            </Grid>
                        }
        />
    )
}
export default Task;
