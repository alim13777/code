import React, {useState} from 'react';
import 'react-big-calendar-forked-persian/lib/css/react-big-calendar.css';
import {FusePageSimple} from "../../../../../../../@fuse";
import {Card, CardContent} from "@material-ui/core";
import {makeStyles} from "@material-ui/core/styles";
import axios from "axios";
import {useDispatch} from "react-redux/es/hooks/useDispatch";
import Info from "./tab/Info";
import Submit from "./tab/Submit";
import TabPro from "../../../../../components/TabPro";
import Alert from "@material-ui/lab/Alert";
import {refahiError} from "../../../taskConfig";


const Request = ({formValues, submitCallBack}) => {

    const useStyles = makeStyles(() => ({
        paper: {minWidth: "1000px"},
    }));
    const classes = useStyles();
    const [open, setOpen] = useState(false)
    const [headerTxt, setHeaderTxt] = useState("")
    const [day, setDay] = useState(0)
    const [rooms, setRooms] = useState([])
    const [totalCost, setTotalCost] = useState(0)
    const [rows, setRows] = useState([])
    const [formData, setFormData] = useState(formValues)
    const [formDefaults, setFormDefaults] = useState({})
    const dispatch = useDispatch()

    React.useEffect(() => {
        getData()
        setFormData({...formValues})
    }, [formValues])

    const getData = async () => {
        if (formValues.hotelId == undefined) return false;
        axios.get( "/rest/s1/welfare/" + formValues?.hotelId+"/detail", {params:{"startDate":formValues?.fromDate,"endDate":formValues?.toDate}}).then(res => {
            if (res.data.state != 1) {
                return false;
            }
            setFormDefaults(res.data)
            if (typeof formValues?.rooms == "string") {
                setRooms(JSON.parse(formValues?.rooms))
            } else setRooms(formValues?.rooms ?? [{companion:formValues?.companion}])
        })
    }
    const handleChange = (event) => {
        setFormData({...formData, [event.target.name]: event.target.value})
    }




    let tabs = [
        {
            "panel": <Info formData={formData} formDefaults={formDefaults}/>, "label": "اطلاعات درخواست"
        },
        {
            "panel": <Submit formData={formData} formDefaults={formDefaults} submitCallBack={submitCallBack}
                             rooms={rooms} setRooms={setRooms} setFormDefaults={setFormDefaults}/>, "label": "بررسی درخواست"
        },

    ]

    return (
        <>
            <FusePageSimple
                content={
                    <>
                        {formData?.processStep == "starter" ?
                            <Alert severity="error"
                                   variant="filled">
                                {refahiError}
                            </Alert>
                            :
                            ""
                        }
                            <Card>
                                <CardContent>
                                    <TabPro tabs={tabs}/>
                                </CardContent>
                            </Card>
                    </>
                }
            />
        </>
    )


}


export default Request