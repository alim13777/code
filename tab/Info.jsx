import React, {useEffect,useState} from "react"
import {Grid, TextField} from "@material-ui/core";
import Autocomplete from "@material-ui/lab/Autocomplete";
import DatePicker from "../../../../../../components/DatePicker";
import axios from "axios";


const Info = ({formData, formDefaults}) => {




    return (
        <>
            <br/>
            <Grid container spacing={1}>
                <Grid item xs={12} sm={4}>
                    <TextField variant="outlined" name="user" label="درخواست دهنده" value={formData?.starterName ?? ""}
                               margin="normal" fullWidth/>
                </Grid>
                <Grid item xs={12} sm={4}>
                    <TextField variant="outlined" name="name" label="مهمان" value={formData?.name ?? ""}
                               margin="normal" fullWidth/>
                </Grid>
                <Grid item xs={12} sm={4}>
                    <TextField variant="outlined" name="mobile" label="شماره همراه" value={formData?.mobile ?? ""}
                               margin="normal" fullWidth/>
                </Grid>
            </Grid>
            <Grid container spacing={1}>
                <Grid item xs={12} sm={4}>
                    <TextField variant="outlined" name="nationalId" label="کد ملی" value={formData?.nationalId ?? ""}
                               margin="normal" fullWidth/>
                </Grid>
                <Grid item xs={12} sm={4}>
                    <TextField variant="outlined" name="nationalId" label="استان" value={formData?.province?.province_name ?? ""}
                               margin="normal" fullWidth/>
                </Grid>
                <Grid item xs={12} sm={4}>
                    <TextField variant="outlined" name="nationalId" label="شهر" value={formData?.town?.township_name ?? ""}
                               margin="normal" fullWidth/>
                </Grid>
            </Grid>
            <Grid container spacing={1}>
                <Grid item xs={12} sm={4}>
                    <TextField name="companion" label="تعداد کل مهمانان" fullWidth value={formData?.companion ?? ""}
                               margin="normal" variant="outlined"/>
                </Grid>
                <Grid item sm={4} xs={12} style={{"margin-top": "15px"}}>
                    <DatePicker variant="outlined" id="fromDate" name="fromDate"
                                value={formData?.fromDate ?? null} disabled
                                withTime={false}
                                format={"jYYYY/jMMMM/jDD"}
                                label="از تاریخ" fullWidth/>
                </Grid>
                <Grid item sm={4} xs={12} style={{"margin-top": "15px"}}>
                    <DatePicker variant="outlined" id="toDate" name="toDate"
                                value={formData?.toDate ?? null} disabled
                                withTime={false}
                                format={"jYYYY/jMMMM/jDD"}
                                label="تا تاریخ" fullWidth/>
                </Grid>
            </Grid>
            <Grid container spacing={1}>
                {formData?.reserveType?.type == "STPeriod" || formData?.reserveType?.type == "STLPeriod" ?
                    <Grid item xs={12} sm={4}>
                        <Autocomplete name="period" disabled
                                      value={formData?.period ?? null}
                                      renderInput={params => {
                                          return <TextField required {...params} variant="outlined"
                                                            label="بازه های زمانی"
                                                            margin="normal" fullWidth/>
                                      }}
                                      options={formData?.periodList ?? []}
                                      getOptionLabel={option => "از " + option?.startDate + " تا " + option?.endDate ?? ""}></Autocomplete>
                    </Grid>
                    : ""}
                <Grid item xs={12} sm={4}>
                    <TextField name="reserveTypeText" label="نوع دوره"
                               value={formData?.reserveTypeText ?? ""}
                               disabled variant="outlined" margin="normal" fullWidth/>
                </Grid>
                <Grid item xs={12} sm={4}>
                    <TextField name="hotel" label="اقامتگاه" value={formDefaults?.hotel?.name ?? ""} disabled
                               variant="outlined" margin="normal" fullWidth/>
                </Grid>
            </Grid>
        </>
    )


}


export default Info