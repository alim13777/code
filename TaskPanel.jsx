import React from "react";
import {withStyles} from "@material-ui/styles";
import {Grid, IconButton, Typography} from "@material-ui/core";
import Box from "@material-ui/core/Box";

import CircularProgress from "@material-ui/core/CircularProgress";
import EventNoteIcon from '@material-ui/icons/EventNote';
import axios from "axios";
import {errorText, requestText, successText, toastTop} from "../../../../configs";
import {setAlertContent} from "app/store/actions";
import {useDispatch} from "react-redux";
import Snackbar from '@material-ui/core/Snackbar';
import Alert from '@material-ui/lab/Alert';
import CloseIcon from "@material-ui/icons/Close";
import {ArrowLeftSharp, ArrowRightSharp} from "@material-ui/icons";
import {toast} from "react-toastify";

const useStyles = theme => ({});

function loadComponent(name) {

    const Component = React.lazy(() =>
        import(`./forms/${name}.jsx`).catch(() => import(`./forms/FormNotFound.jsx`))
    );

    return Component
}

const ViewDefault = ({taskNumber, setTaskNumber, setTask, taskList}) => {

    const nextTask = () => {
        if (taskNumber != null) {
            let currentTask = taskList[taskNumber + 1]
            setTask(currentTask)
            setTaskNumber(taskNumber + 1)
        } else {
            let currentTask = taskList[0]
            setTask(currentTask)
            setTaskNumber(0)
        }
    }

    const lastTask = () => {

        if (taskNumber != null && taskNumber != 0) {
            let currentTask = taskList[taskNumber - 1]
            setTask(currentTask)
            setTaskNumber(taskNumber - 1)
        }
        if (taskNumber == 0) {
            setTask(null)
            setTaskNumber(null)
        }
    }

    return (
        <Box textAlign="center" color="text.secondary" p={4}>
            <EventNoteIcon/>
            <Typography variant={"body1"}></Typography>
            <Grid container spacing={0}>
                <Grid item xs={4} sm={4}>
            <IconButton onClick={lastTask} title="کار قبلی">
                <ArrowRightSharp/>
            </IconButton>
                </Grid>
                <Grid item xs={4} sm={4}>
            لیست کارها
                </Grid>
                <Grid item xs={4} sm={4}>
            <IconButton onClick={nextTask} title="کار بعدی">
                <ArrowLeftSharp/>
            </IconButton>
                </Grid>
            </Grid>
        </Box>
    )
}

const ViewLoading = () => (
    <Box textAlign="center" color="text.secondary" p={4}>
        <CircularProgress/>
        <Typography variant={"body1"}>در حال دریافت اطلاعات</Typography>
    </Box>
)

class TaskPanel extends React.Component {

    constructor(props) {
        super(props);
    }


    render() {
        const {task, setTaskList, taskList, setTask, taskNumber, setTaskNumber,scroll} = this.props;
        const ViewContent = () => {
            const [formVariables, setFormVariables] = React.useState({})
            const [open, setOpen] = React.useState(false);
            const dispatch = useDispatch();
            const handleClose = () => {
                setOpen(false)
            }
            const submitCallback = (formData) => {
                toast.warn(requestText,toastTop)
                const packet = {
                    taskId: task.taskId,
                    variables: formData
                }
                axios.post("/rest/s1/process/task/complete", packet, {
                }).then(res => {
                    toast.dismiss()
                    if(res.data.state!=1){
                        dispatch(setAlertContent("error",res?.data?.description??errorText))
                        return false;
                    }
                    dispatch(setAlertContent("success", successText))
                    let taskId = task.taskId
                    setTimeout(() => window.scrollTo({top: 0, behavior: "smooth"}), 0)
                    setTask({...task, ["taskId"]: null})
                    setTaskList(taskList => taskList.filter(member => {
                        return member.taskId !== taskId
                    }))
                    setTaskNumber(taskNumber - 1)
                    setTask(null)
                }).catch(err => {
                    toast.dismiss()
                    dispatch(setAlertContent("error", errorText))
                });
            }
            React.useEffect(() => {
                getVariable()
            }, [])
            const getVariable = () => {
                if (task.taskId == null) return;
                axios.get("/rest/s1/process/task/variable", {
                    params: {
                        taskId: task.taskId
                    },
                }).then(res => {
                    setFormVariables({...res.data.variable,["taskId"]:task.taskId,["scroll"]:scroll})
                }).catch(err => {
                    setFormVariables({})
                });
            }
            const nextTask = () => {
                let allTasks = [...taskList]
                let index = allTasks.indexOf(task)
                if (index < allTasks.length - 1) {
                    let nextTask = allTasks[index + 1]
                    setTask(nextTask);
                    setTaskNumber(taskNumber + 1)
                } else {
                    setTask(null)
                    setTaskNumber(null)
                }
            }
            const lastTask = () => {
                let allTasks = [...taskList]
                let index = allTasks.indexOf(task)
                if (index > 1 || index == 1) {
                    let nextTask = allTasks[index - 1]
                    setTask(nextTask);
                    setTaskNumber(taskNumber - 1)
                } else {
                    setTask(null)
                    setTaskNumber(null)
                }
            }
            const TaskForm = loadComponent(task.formKey)
            const closeTask = () => {
                setTask(null)
                setTaskNumber(null)
            }
            return (
                <React.Suspense fallback={<ViewLoading/>}>
                    <Snackbar anchorOrigin={{vertical: "center", horizontal: "left"}} open={open}
                              autoHideDuration={6000} onClose={handleClose}>
                        <Alert variant="filled" onClose={handleClose} severity="warning">
                            در حال انجام درخواست
                        </Alert>
                    </Snackbar>
                    <Grid container spacing={0}>
                        <Grid item xs={1} sm={1}>
                            <IconButton onClick={lastTask} title="کار قبلی">
                                <ArrowRightSharp/>
                            </IconButton>
                        </Grid>
                        <Grid item xs={10} sm={10}>
                            {task.taskName}&nbsp;---&nbsp;
                            کد پیگیری : {formVariables.businessKey}&nbsp;
                            <IconButton title="بستن" style={{"margin-top": "2px"}}
                                        onClick={closeTask}><CloseIcon/></IconButton>
                        </Grid>
                        <Grid item sm={1} xs={1}>
                            <IconButton onClick={nextTask} title="کار بعدی">
                                <ArrowLeftSharp/>
                            </IconButton>
                        </Grid>
                    </Grid>
                    <hr/>
                    <TaskForm formValues={formVariables} submitCallBack={submitCallback}
                              setFormValues={setFormVariables}/>
                </React.Suspense>

            )
        }
        if (!task) {
            return <ViewDefault taskNumber={taskNumber} setTaskNumber={setTaskNumber} setTask={setTask} taskList={taskList}/>
        }
        return <ViewContent  onKeyDown={this.handleEsc}/>

    }
}

export default withStyles(useStyles)(TaskPanel);
