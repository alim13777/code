import React, { useEffect, useState } from "react";
import {
    Plus,
    Trash2,
    Download,
    CheckCircle,
    XCircle,
    AlertCircle,
    Users,
    Home,
    DollarSign,
    Calendar
} from "lucide-react";
import axios from "axios";
import { useDispatch } from "react-redux";
import { errorText, SERVER_URL } from "../../../../../../../../configs";
import {
    compError,
    imgHeight,
    imgWidth,
    modifyError,
    roomEmptyError,
    roomFullError
} from "../../../../taskConfig";
import { toCurrency } from "../../../../../arman/armanConfig";
import { setAlertContent } from "../../../../../../../store/actions";

const Submit = ({ formData, formDefaults, submitCallBack, rooms, setRooms, setFormDefaults }) => {
    const dispatch = useDispatch();

    let buttonTxt = "تایید";
    if (formData?.reserveType?.type === "STLPeriod" && formData?.processStep === "modir")
        buttonTxt = "ثبت در قرعه کشی";

    const [rows, setRows] = useState([]);
    const [totalCost, setTotalCost] = useState(0);
    const [isLoading, setIsLoading] = useState(false);
    const [description, setDescription] = useState(formData?.description || "");

    useEffect(() => {
        calculateCost();
    }, []);

    const handleRoom = async (value, index) => {
        if (value?.status === "full") {
            dispatch(setAlertContent("error", roomFullError));
            return;
        }
        let room = [...rooms];
        room[index] = {
            ...room[index],
            roomId: value?.roomId,
            room: value,
            cost: value?.cost,
            extraCost: value?.extraCost,
            capacity: value?.capacity,
            name: value?.name
        };
        setRooms(room);

        if (formData?.processStep === "modir") {
            if (!value && rooms.length === 1) {
                setTotalCost(0);
                setRows([]);
            } else {
                await calculateCost();
                let ar = [...formDefaults?.room];
                let rm = ar.find(e => e.roomId === value?.roomId);
                let idx = ar.indexOf(rm);
                rm["status"] = "full";
                ar[idx] = rm;
                setFormDefaults({ ...formDefaults, room: ar });
            }
        }
    };

    const handleComp = async (event, index, name) => {
        let value = event.target.value;
        let room = [...rooms];
        room[index][name] = value;
        setRooms(room);
        if (formData?.processStep === "modir") await calculateCost();
    };

    const calculateCost = () => {
        if (rooms.length === 0) return;
        return new Promise(resolve => {
            axios.put("/rest/s1/welfare/cost", { rooms, reserveId: formData?.reserveId }).then(res => {
                setTotalCost(res?.data?.totalCost);
                setRows(res?.data?.costs ?? []);
                resolve("ok");
            });
        });
    };

    const addRoom = () => setRooms([...rooms, { companion: 1 }]);

    const delRoom = index => {
        let room = [...rooms];
        room.splice(index, 1);
        setRooms(room);
    };

    const submit = async type => {
        setIsLoading(true);

        formData["result"] = type;
        formData["description"] = description;

        if (type !== "acceptDarkhast" && (!description || description === "")) {
            dispatch(setAlertContent("error", modifyError));
            setIsLoading(false);
            return;
        }

        if (type !== "acceptDarkhast") {
            setIsLoading(false);
            submitCallBack(formData);
            return;
        }

        if (formData?.processStep === "modir" && formData?.reserveType?.type !== "STLPeriod") {
            if (!rooms || rooms?.length === 0) {
                dispatch(setAlertContent("error", roomEmptyError));
                setIsLoading(false);
                return;
            }
            let companionError = [];
            let roomError = [];
            rooms.forEach(ele => {
                if (!ele.companion) companionError.push("false");
                if (!ele.room) roomError.push("false");
            });
            if (companionError.includes("false")) {
                dispatch(setAlertContent("error", compError));
                setIsLoading(false);
                return;
            }
            if (roomError.includes("false")) {
                dispatch(setAlertContent("error", roomEmptyError));
                setIsLoading(false);
                return;
            }
        }

        if (formData?.processStep === "starter" && type === "acceptDarkhast") {
            formData["result"] = "baresiDarkhast";
        } else if (formData?.processStep !== "starter" && type === "acceptDarkhast") {
            formData["result"] = formData?.reserveType?.type !== "STLPeriod" ? "PayCost" : "LReserve";
        } else if (formData?.processStep === "paidCost" && type === "acceptDarkhast") {
            formData["result"] = "acceptDarkhast";
        }

        try {
            await setRoom();
            formData["costs"] = rows;
            formData["rooms"] = rooms;
            formData["totalCost"] = totalCost;
            submitCallBack(formData);
        } catch (error) {
            dispatch(setAlertContent("error", errorText));
        } finally {
            setIsLoading(false);
        }
    };

    const setRoom = () => {
        return new Promise((resolve, reject) => {
            axios.patch("/rest/s1/welfare/rooms", { rooms, reserveId: formData?.reserveId })
                .then(() => resolve("ok"))
                .catch(error => reject(error));
        });
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-100 p-6">
            <div className="max-w-6xl mx-auto space-y-8">

                {/* Room Management Section */}
                {(formData?.processStep === "modir" || formData?.processStep === "paidCost") &&
                    formData?.reserveType?.type !== "STLPeriod" && formData?.processStep !== "starter" && (
                        <div className="bg-white/80 backdrop-blur-sm rounded-3xl shadow-2xl border border-white/20 overflow-hidden">
                            <div className="bg-gradient-to-r from-indigo-600 to-purple-600 p-6">
                                <div className="flex items-center justify-between">
                                    <div className="flex items-center space-x-3 space-x-reverse">
                                        <Home className="w-7 h-7 text-white" />
                                        <h2 className="text-2xl font-bold text-white">سوییت های اقامتگاه</h2>
                                    </div>
                                    <button
                                        onClick={addRoom}
                                        className="group bg-white/20 hover:bg-white/30 backdrop-blur-sm p-3 rounded-full transition-all duration-300 transform hover:scale-110"
                                    >
                                        <Plus className="w-6 h-6 text-white group-hover:rotate-90 transition-transform duration-300" />
                                    </button>
                                </div>
                            </div>

                            <div className="p-6 space-y-6">
                                {rooms?.map((ele, index) => (
                                    <div key={index} className="group bg-gradient-to-r from-slate-50 to-blue-50 rounded-2xl p-6 border border-slate-200 hover:shadow-lg transition-all duration-300">
                                        <div className="grid grid-cols-1 md:grid-cols-6 gap-6 items-end">

                                            {/* Room Selection */}
                                            <div className="md:col-span-2">
                                                <label className="block text-sm font-semibold text-slate-700 mb-2">انتخاب سوییت</label>
                                                <div className="relative">
                                                    <select
                                                        value={ele?.roomId || ''}
                                                        onChange={(e) => {
                                                            const selectedRoom = formDefaults?.room?.find(r => r.roomId === e.target.value);
                                                            handleRoom(selectedRoom, index);
                                                        }}
                                                        className="w-full p-4 bg-white/80 backdrop-blur-sm border-2 border-slate-200 rounded-xl focus:border-indigo-500 focus:ring-4 focus:ring-indigo-100 transition-all duration-300 appearance-none pr-10"
                                                    >
                                                        <option value="">انتخاب کنید...</option>
                                                        {formDefaults?.room?.map(room => (
                                                            <option key={room.roomId} value={room.roomId}>
                                                                {room.name} - ظرفیت: {room.capacity} - {room.status === "full" ? "پر" : "خالی"}
                                                            </option>
                                                        ))}
                                                    </select>
                                                    <Home className="absolute right-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-slate-400 pointer-events-none" />
                                                </div>
                                            </div>

                                            {/* Companions */}
                                            <div>
                                                <label className="block text-sm font-semibold text-slate-700 mb-2">تعداد مهمانان</label>
                                                <div className="relative">
                                                    <input
                                                        type="number"
                                                        value={ele?.companion ?? ""}
                                                        onChange={event => handleComp(event, index, "companion")}
                                                        className="w-full p-4 bg-white/80 backdrop-blur-sm border-2 border-slate-200 rounded-xl focus:border-indigo-500 focus:ring-4 focus:ring-indigo-100 transition-all duration-300 pr-10"
                                                        min="1"
                                                    />
                                                    <Users className="absolute right-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-slate-400" />
                                                </div>
                                            </div>

                                            {/* Cost per night */}
                                            <div>
                                                <label className="block text-sm font-semibold text-slate-700 mb-2">هزینه هر شب</label>
                                                <div className="relative">
                                                    <input
                                                        type="number"
                                                        value={ele?.cost ?? ""}
                                                        onChange={event => handleComp(event, index, "cost")}
                                                        className="w-full p-4 bg-white/80 backdrop-blur-sm border-2 border-slate-200 rounded-xl focus:border-indigo-500 focus:ring-4 focus:ring-indigo-100 transition-all duration-300 pr-10"
                                                    />
                                                    <DollarSign className="absolute right-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-slate-400" />
                                                </div>
                                            </div>

                                            {/* Extra Cost */}
                                            <div>
                                                <label className="block text-sm font-semibold text-slate-700 mb-2">هزینه نفر اضافه</label>
                                                <div className="relative">
                                                    <input
                                                        type="number"
                                                        value={ele?.extraCost ?? ""}
                                                        onChange={event => handleComp(event, index, "extraCost")}
                                                        className="w-full p-4 bg-white/80 backdrop-blur-sm border-2 border-slate-200 rounded-xl focus:border-indigo-500 focus:ring-4 focus:ring-indigo-100 transition-all duration-300 pr-10"
                                                    />
                                                    <DollarSign className="absolute right-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-slate-400" />
                                                </div>
                                            </div>

                                            {/* Delete button */}
                                            <div className="flex justify-center">
                                                <button
                                                    onClick={() => delRoom(index)}
                                                    className="group bg-red-50 hover:bg-red-100 p-3 rounded-full transition-all duration-300 transform hover:scale-110"
                                                >
                                                    <Trash2 className="w-5 h-5 text-red-500 group-hover:scale-110 transition-transform duration-300" />
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}

                {/* Cost Summary */}
                {rows.length > 0 && (
                    <div className="bg-white/80 backdrop-blur-sm rounded-3xl shadow-2xl border border-white/20 overflow-hidden">
                        <div className="bg-gradient-to-r from-emerald-600 to-teal-600 p-6">
                            <div className="flex items-center space-x-3 space-x-reverse">
                                <DollarSign className="w-7 h-7 text-white" />
                                <h2 className="text-2xl font-bold text-white">
                                    هزینه کل: {toCurrency(totalCost)} ریال
                                </h2>
                            </div>
                        </div>

                        <div className="p-6">
                            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                                {rows.map((ele, idx) => (
                                    <div key={idx} className="group bg-gradient-to-br from-white to-slate-50 rounded-2xl p-6 shadow-lg border border-slate-200 hover:shadow-xl transition-all duration-300 transform hover:-translate-y-1">
                                        <div className="flex items-center justify-between mb-4">
                                            <h3 className="text-lg font-bold text-slate-800">{ele.name}</h3>
                                            <div className="bg-indigo-100 p-2 rounded-lg">
                                                <Home className="w-5 h-5 text-indigo-600" />
                                            </div>
                                        </div>

                                        <div className="space-y-3">
                                            <div className="flex items-center justify-between text-sm">
                        <span className="text-slate-600 flex items-center">
                          <Users className="w-4 h-4 mr-2" />
                          ظرفیت
                        </span>
                                                <span className="font-semibold text-slate-800">{ele.capacity}</span>
                                            </div>

                                            <div className="flex items-center justify-between text-sm">
                        <span className="text-slate-600 flex items-center">
                          <Users className="w-4 h-4 mr-2" />
                          مهمانان
                        </span>
                                                <span className="font-semibold text-slate-800">{ele.companion}</span>
                                            </div>

                                            <div className="flex items-center justify-between text-sm">
                        <span className="text-slate-600 flex items-center">
                          <Calendar className="w-4 h-4 mr-2" />
                          مدت اقامت
                        </span>
                                                <span className="font-semibold text-slate-800">{ele.day} شب</span>
                                            </div>

                                            <div className="flex items-center justify-between text-sm">
                                                <span className="text-slate-600">هزینه یک شب:</span>
                                                <span className="font-semibold text-slate-800">{toCurrency(ele.cost)} ریال</span>
                                            </div>

                                            <div className="flex items-center justify-between text-sm">
                                                <span className="text-slate-600">نفر اضافه هر شب:</span>
                                                <span className="font-semibold text-slate-800">{toCurrency(ele.extraCost)} ریال</span>
                                            </div>

                                            <div className="flex items-center justify-between text-sm">
                                                <span className="text-slate-600">تعداد نفر اضافه:</span>
                                                <span className="font-semibold text-slate-800">{ele.extraCompanion}</span>
                                            </div>

                                            <div className="flex items-center justify-between text-sm">
                                                <span className="text-slate-600">هزینه نفر اضافه کل:</span>
                                                <span className="font-semibold text-slate-800">{toCurrency(ele.extraTotalCost)} ریال</span>
                                            </div>

                                            <div className="pt-3 border-t border-slate-200">
                                                <div className="flex items-center justify-between">
                                                    <span className="text-slate-600">هزینه کل:</span>
                                                    <span className="font-bold text-lg text-emerald-600">
                            {toCurrency(ele.totalCost)} ریال
                          </span>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    </div>
                )}

                {/* Payment Receipt */}
                {formData?.processStep === "paidCost" && (
                    <div className="bg-white/80 backdrop-blur-sm rounded-3xl shadow-2xl border border-white/20 overflow-hidden">
                        <div className="bg-gradient-to-r from-blue-600 to-indigo-600 p-6">
                            <div className="flex items-center space-x-3 space-x-reverse">
                                <Download className="w-7 h-7 text-white" />
                                <h2 className="text-2xl font-bold text-white">فایل رسید پرداخت</h2>
                            </div>
                        </div>

                        <div className="p-6">
                            <div className="flex flex-col items-center space-y-6">
                                <a
                                    href={SERVER_URL + "/rest/s1/general/download?fileId=" + formData?.fileId}
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    className="group bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white px-8 py-4 rounded-2xl shadow-lg transition-all duration-300 transform hover:-translate-y-1 flex items-center space-x-3 space-x-reverse"
                                >
                                    <Download className="w-5 h-5 group-hover:animate-bounce" />
                                    <span className="font-semibold">دانلود رسید</span>
                                </a>

                                <div className="bg-slate-50 p-4 rounded-2xl shadow-inner">
                                    <img
                                        height={imgHeight}
                                        width={imgWidth}
                                        src={SERVER_URL + "/rest/s1/general/download?fileId=" + formData?.fileId}
                                        alt="رسید پرداخت"
                                        className="rounded-xl shadow-lg max-w-full h-auto"
                                        style={{
                                            maxHeight: imgHeight,
                                            maxWidth: imgWidth,
                                            objectFit: 'contain'
                                        }}
                                    />
                                </div>
                            </div>
                        </div>
                    </div>
                )}

                {/* Description */}
                <div className="bg-white/80 backdrop-blur-sm rounded-3xl shadow-2xl border border-white/20 p-6">
                    <label className="block text-lg font-bold text-slate-800 mb-4">دلایل اصلاح</label>
                    <textarea
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                        className="w-full p-4 bg-white/80 backdrop-blur-sm border-2 border-slate-200 rounded-xl focus:border-indigo-500 focus:ring-4 focus:ring-indigo-100 transition-all duration-300 resize-none"
                        rows="4"
                        placeholder="توضیحات خود را وارد کنید..."
                    />
                </div>

                {/* Action Buttons */}
                <div className="flex justify-center space-x-4 space-x-reverse">
                    <button
                        onClick={() => submit("acceptDarkhast")}
                        disabled={isLoading}
                        className="group bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-700 hover:to-teal-700 disabled:from-slate-400 disabled:to-slate-500 text-white px-8 py-4 rounded-2xl shadow-lg transition-all duration-300 transform hover:-translate-y-1 disabled:hover:translate-y-0 flex items-center space-x-3 space-x-reverse font-semibold"
                    >
                        {isLoading ? (
                            <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
                        ) : (
                            <CheckCircle className="w-5 h-5 group-hover:scale-110 transition-transform duration-300" />
                        )}
                        <span>{buttonTxt}</span>
                    </button>

                    {(formData?.processStep === "modir" || formData?.processStep === "paidCost") ? (
                        <button
                            onClick={() => submit("rejectDarkhast")}
                            disabled={isLoading}
                            className="group bg-gradient-to-r from-red-600 to-pink-600 hover:from-red-700 hover:to-pink-700 disabled:from-slate-400 disabled:to-slate-500 text-white px-8 py-4 rounded-2xl shadow-lg transition-all duration-300 transform hover:-translate-y-1 disabled:hover:translate-y-0 flex items-center space-x-3 space-x-reverse font-semibold"
                        >
                            <XCircle className="w-5 h-5 group-hover:scale-110 transition-transform duration-300" />
                            <span>رد</span>
                        </button>
                    ) : (
                        <button
                            onClick={() => submit("enseraf")}
                            disabled={isLoading}
                            className="group bg-gradient-to-r from-slate-600 to-gray-600 hover:from-slate-700 hover:to-gray-700 disabled:from-slate-400 disabled:to-slate-500 text-white px-8 py-4 rounded-2xl shadow-lg transition-all duration-300 transform hover:-translate-y-1 disabled:hover:translate-y-0 flex items-center space-x-3 space-x-reverse font-semibold"
                        >
                            <AlertCircle className="w-5 h-5 group-hover:scale-110 transition-transform duration-300" />
                            <span>لغو</span>
                        </button>
                    )}
                </div>
            </div>
        </div>
    );
};

export default Submit;