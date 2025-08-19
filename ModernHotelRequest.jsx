import React, {useEffect, useState, useMemo} from 'react'
import {
    Box, Button,
    Card,
    CardActions,
    CardContent,
    CardHeader,
    Chip,
    Grid,
    IconButton, Link,
    TextField,
    Typography,
    useMediaQuery,
    useTheme,
    InputAdornment,
    Fade,
    Zoom,
    Slide,
    Backdrop,
    CircularProgress,
    Container,
    Avatar,
    Divider,
    LinearProgress,
    Skeleton
} from '@material-ui/core'
import Pagination from '@material-ui/lab/Pagination'
import {
    Add as AddIcon, 
    CalendarToday as CalendarIcon, 
    Clear as ClearIcon,
    LocationOn as LocationIcon, 
    People as PeopleIcon,
    Person as PersonIcon,
    Phone as PhoneIcon, 
    Save as SaveIcon,
    Star as StarIcon,
    Fingerprint as FingerprintIcon,
    Home as HomeIcon,
    LocationCity as CityIcon,
    Hotel as HotelIcon,
    Send as SendIcon,
    Close as CloseIcon,
    CheckCircle as CheckCircleIcon,
    Error as ErrorIcon,
    Info as InfoIcon
} from '@material-ui/icons'
import {useHistory, useLocation} from 'react-router-dom'
import {FusePageSimple} from '../../../../../@fuse'
import axios from 'axios'
import {SERVER_URL, isNumber, isPersian} from '../../../../../configs'
import {setAlertContent} from '../../../../store/actions'
import {useDispatch} from 'react-redux'
import {Slide as ImageSlide} from 'react-slideshow-image'
import 'react-slideshow-image/dist/styles.css'
import hotelPlaceholder from './../../../../../images/hotel.jpg'
import StyledDialog from "./components/StyledDialog"
import Autocomplete from "@material-ui/lab/Autocomplete"
import DatePicker from "../../../components/DatePicker"
import {makeStyles, withStyles} from "@material-ui/core/styles"

const useStyles = makeStyles((theme) => ({
    // Main container styles
    pageContainer: {
        minHeight: '100vh',
        background: `linear-gradient(135deg, 
            ${theme.palette.primary.light}15 0%, 
            ${theme.palette.secondary.light}08 50%, 
            ${theme.palette.primary.dark}10 100%)`,
        position: 'relative',
        overflow: 'hidden',
        '&::before': {
            content: '""',
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: 'url("data:image/svg+xml,%3Csvg width="60" height="60" viewBox="0 0 60 60" xmlns="http://www.w3.org/2000/svg"%3E%3Cg fill="none" fill-rule="evenodd"%3E%3Cg fill="%23f0f4f8" fill-opacity="0.4"%3E%3Ccircle cx="30" cy="30" r="2"/%3E%3C/g%3E%3C/g%3E%3C/svg%3E")',
            opacity: 0.3,
        }
    },

    // Enhanced card styles
    hotelCard: {
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        borderRadius: theme.spacing(3),
        background: 'rgba(255, 255, 255, 0.95)',
        backdropFilter: 'blur(20px)',
        border: '1px solid rgba(255, 255, 255, 0.2)',
        boxShadow: `
            0 8px 32px rgba(0, 0, 0, 0.08),
            0 4px 16px rgba(0, 0, 0, 0.04),
            inset 0 1px 0 rgba(255, 255, 255, 0.5)
        `,
        transition: 'all 0.4s cubic-bezier(0.4, 0, 0.2, 1)',
        transform: 'translateY(0px)',
        position: 'relative',
        overflow: 'hidden',
        '&::before': {
            content: '""',
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: 'linear-gradient(135deg, rgba(25,118,210,0.02) 0%, rgba(156,39,176,0.01) 100%)',
            opacity: 0,
            transition: 'opacity 0.3s ease',
            zIndex: 1,
        },
        '&:hover': {
            transform: 'translateY(-12px) scale(1.02)',
            boxShadow: `
                0 20px 40px rgba(0, 0, 0, 0.15),
                0 8px 24px rgba(0, 0, 0, 0.08),
                inset 0 1px 0 rgba(255, 255, 255, 0.6)
            `,
            '&::before': {
                opacity: 1,
            },
            '& $cardMedia img': {
                transform: 'scale(1.08)',
            },
            '& $requestButton': {
                transform: 'scale(1.15) rotate(180deg)',
                background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                boxShadow: '0 8px 25px rgba(102, 126, 234, 0.4)',
            },
            '& $ratingChip': {
                transform: 'scale(1.1)',
                boxShadow: '0 4px 12px rgba(255, 193, 7, 0.4)',
            }
        },
    },

    // Image container with enhanced styling
    cardMedia: {
        position: 'relative',
        height: 220,
        overflow: 'hidden',
        borderRadius: `${theme.spacing(3)}px ${theme.spacing(3)}px 0 0`,
        '& img': {
            width: '100%',
            height: '100%',
            objectFit: 'cover',
            transition: 'transform 0.6s cubic-bezier(0.4, 0, 0.2, 1)',
        },
        '&::after': {
            content: '""',
            position: 'absolute',
            bottom: 0,
            left: 0,
            right: 0,
            height: '30%',
            background: 'linear-gradient(to top, rgba(0,0,0,0.3) 0%, transparent 100%)',
            zIndex: 2,
        }
    },

    // Enhanced rating chip
    ratingChip: {
        position: 'absolute',
        top: theme.spacing(2),
        right: theme.spacing(2),
        background: 'linear-gradient(135deg, #FFD700 0%, #FF8C00 100%)',
        color: theme.palette.common.white,
        fontWeight: 700,
        fontSize: '0.75rem',
        backdropFilter: 'blur(8px)',
        borderRadius: theme.spacing(2.5),
        padding: theme.spacing(0.5, 1.5),
        zIndex: 3,
        border: '2px solid rgba(255, 255, 255, 0.3)',
        boxShadow: '0 4px 12px rgba(255, 193, 7, 0.3)',
        transition: 'all 0.3s ease',
        '& .MuiChip-icon': {
            color: 'inherit',
            fontSize: '1rem',
        }
    },

    // Enhanced card header
    cardHeader: {
        padding: theme.spacing(2.5, 3, 1, 3),
        '& .MuiCardHeader-title': {
            fontSize: '1.25rem',
            fontWeight: 700,
            background: 'linear-gradient(135deg, #1976d2 0%, #42a5f5 100%)',
            backgroundClip: 'text',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
            lineHeight: 1.3,
        },
        '& .MuiCardHeader-subheader': {
            fontSize: '0.875rem',
            color: theme.palette.text.secondary,
            marginTop: theme.spacing(0.5),
            display: 'flex',
            alignItems: 'center',
            '&::before': {
                content: '""',
                display: 'inline-block',
                width: 4,
                height: 4,
                borderRadius: '50%',
                backgroundColor: theme.palette.primary.main,
                marginRight: theme.spacing(1),
            }
        }
    },

    // Enhanced card content
    cardContent: {
        padding: theme.spacing(1, 3, 2, 3),
        flexGrow: 1,
        display: 'flex',
        flexDirection: 'column',
        gap: theme.spacing(1.5),
    },

    // Phone info styling
    phoneInfo: {
        display: 'flex',
        alignItems: 'center',
        gap: theme.spacing(1),
        padding: theme.spacing(1),
        borderRadius: theme.spacing(1.5),
        background: 'rgba(25, 118, 210, 0.04)',
        border: '1px solid rgba(25, 118, 210, 0.1)',
        transition: 'all 0.3s ease',
        '&:hover': {
            background: 'rgba(25, 118, 210, 0.08)',
            transform: 'translateX(4px)',
        }
    },

    // Description styling
    description: {
        fontSize: '0.875rem',
        lineHeight: 1.6,
        color: theme.palette.text.secondary,
        display: '-webkit-box',
        WebkitLineClamp: 3,
        WebkitBoxOrient: 'vertical',
        overflow: 'hidden',
        textAlign: 'justify',
    },

    // Enhanced request button
    requestButton: {
        width: 56,
        height: 56,
        background: 'linear-gradient(135deg, #1976d2 0%, #42a5f5 100%)',
        color: theme.palette.common.white,
        boxShadow: '0 6px 20px rgba(25, 118, 210, 0.3)',
        transition: 'all 0.4s cubic-bezier(0.4, 0, 0.2, 1)',
        border: '3px solid rgba(255, 255, 255, 0.2)',
        position: 'relative',
        overflow: 'hidden',
        '&::before': {
            content: '""',
            position: 'absolute',
            top: 0,
            left: '-100%',
            width: '100%',
            height: '100%',
            background: 'linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent)',
            transition: 'left 0.6s ease',
        },
        '&:hover::before': {
            left: '100%',
        },
        '& .MuiSvgIcon-root': {
            fontSize: '1.5rem',
        }
    },

    // Card actions styling
    cardActions: {
        padding: theme.spacing(0, 3, 3, 3),
        justifyContent: 'center',
    },

    // Enhanced pagination
    paginationContainer: {
        display: 'flex',
        justifyContent: 'center',
        marginTop: theme.spacing(6),
        '& .MuiPagination-root': {
            background: 'rgba(255, 255, 255, 0.9)',
            backdropFilter: 'blur(20px)',
            borderRadius: theme.spacing(3),
            padding: theme.spacing(1.5, 3),
            border: '1px solid rgba(255, 255, 255, 0.2)',
            boxShadow: '0 8px 32px rgba(0, 0, 0, 0.08)',
        },
        '& .MuiPaginationItem-root': {
            borderRadius: theme.spacing(1.5),
            fontWeight: 600,
            transition: 'all 0.3s ease',
            '&:hover': {
                background: 'rgba(25, 118, 210, 0.08)',
                transform: 'scale(1.1)',
            },
            '&.Mui-selected': {
                background: 'linear-gradient(135deg, #1976d2 0%, #42a5f5 100%)',
                color: 'white',
                boxShadow: '0 4px 12px rgba(25, 118, 210, 0.3)',
                '&:hover': {
                    background: 'linear-gradient(135deg, #1565c0 0%, #1e88e5 100%)',
                }
            }
        }
    },

    // Form styling improvements
    formContainer: {
        '& .MuiGrid-item': {
            position: 'relative',
        }
    },

    // Enhanced text field styling
    modernTextField: {
        '& .MuiOutlinedInput-root': {
            borderRadius: theme.spacing(2),
            background: 'rgba(255, 255, 255, 0.8)',
            backdropFilter: 'blur(10px)',
            transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
            border: '1px solid rgba(25, 118, 210, 0.1)',
            '&:hover': {
                background: 'rgba(255, 255, 255, 0.95)',
                borderColor: 'rgba(25, 118, 210, 0.2)',
                transform: 'translateY(-2px)',
                boxShadow: '0 8px 25px rgba(0, 0, 0, 0.1)',
            },
            '&.Mui-focused': {
                background: 'rgba(255, 255, 255, 1)',
                borderColor: theme.palette.primary.main,
                boxShadow: `0 0 0 3px ${theme.palette.primary.main}20`,
                transform: 'translateY(-2px)',
            },
            '&.Mui-error': {
                borderColor: theme.palette.error.main,
                '&:hover, &.Mui-focused': {
                    boxShadow: `0 0 0 3px ${theme.palette.error.main}20`,
                }
            }
        },
        '& .MuiInputLabel-outlined': {
            fontWeight: 600,
            color: theme.palette.text.secondary,
            '&.Mui-focused': {
                color: theme.palette.primary.main,
            },
            '&.Mui-error': {
                color: theme.palette.error.main,
            }
        },
        '& .MuiInputAdornment-root .MuiSvgIcon-root': {
            color: theme.palette.primary.main,
            transition: 'all 0.3s ease',
        },
        '& .MuiOutlinedInput-root.Mui-focused .MuiInputAdornment-root .MuiSvgIcon-root': {
            transform: 'scale(1.1)',
            color: theme.palette.primary.dark,
        }
    },

    // Enhanced autocomplete styling
    modernAutocomplete: {
        '& .MuiAutocomplete-inputRoot': {
            borderRadius: theme.spacing(2),
            background: 'rgba(255, 255, 255, 0.8)',
            backdropFilter: 'blur(10px)',
            border: '1px solid rgba(25, 118, 210, 0.1)',
            transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
            '&:hover': {
                background: 'rgba(255, 255, 255, 0.95)',
                borderColor: 'rgba(25, 118, 210, 0.2)',
                transform: 'translateY(-2px)',
                boxShadow: '0 8px 25px rgba(0, 0, 0, 0.1)',
            },
            '&.Mui-focused': {
                background: 'rgba(255, 255, 255, 1)',
                borderColor: theme.palette.primary.main,
                boxShadow: `0 0 0 3px ${theme.palette.primary.main}20`,
                transform: 'translateY(-2px)',
            }
        },
        '& .MuiAutocomplete-popupIndicator': {
            color: theme.palette.primary.main,
        },
        '& .MuiAutocomplete-clearIndicator': {
            color: theme.palette.grey[500],
        }
    },

    // Loading states
    loadingContainer: {
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '60vh',
        gap: theme.spacing(3),
    },

    loadingSpinner: {
        background: 'linear-gradient(135deg, #1976d2 0%, #42a5f5 100%)',
        borderRadius: '50%',
        padding: theme.spacing(2),
        boxShadow: '0 8px 32px rgba(25, 118, 210, 0.3)',
    },

    // Skeleton loading for cards
    skeletonCard: {
        height: '100%',
        borderRadius: theme.spacing(3),
        background: 'rgba(255, 255, 255, 0.7)',
        backdropFilter: 'blur(10px)',
        padding: 0,
        overflow: 'hidden',
    },

    // Section headers
    sectionHeader: {
        marginBottom: theme.spacing(4),
        textAlign: 'center',
        '& h4': {
            background: 'linear-gradient(135deg, #1976d2 0%, #42a5f5 100%)',
            backgroundClip: 'text',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
            fontWeight: 700,
            marginBottom: theme.spacing(1),
        },
        '& p': {
            color: theme.palette.text.secondary,
            fontSize: '1.1rem',
            maxWidth: 600,
            margin: '0 auto',
            lineHeight: 1.6,
        }
    },

    // Enhanced dialog styles
    dialogContent: {
        padding: theme.spacing(3),
        background: 'linear-gradient(135deg, rgba(255,255,255,0.9) 0%, rgba(248,250,252,0.9) 100%)',
        borderRadius: theme.spacing(2),
    },

    // Form section dividers
    formSection: {
        marginBottom: theme.spacing(3),
        '&:not(:last-child)': {
            paddingBottom: theme.spacing(3),
            borderBottom: `1px solid ${theme.palette.divider}`,
        }
    },

    // Animation delays for staggered entrance
    '@keyframes slideInUp': {
        from: {
            opacity: 0,
            transform: 'translateY(30px)',
        },
        to: {
            opacity: 1,
            transform: 'translateY(0)',
        }
    },

    cardAnimation: {
        animation: '$slideInUp 0.6s ease-out forwards',
        opacity: 0,
    },

    // Responsive adjustments
    [theme.breakpoints.down('sm')]: {
        hotelCard: {
            '&:hover': {
                transform: 'translateY(-6px) scale(1.01)',
            }
        },
        cardMedia: {
            height: 180,
        },
        requestButton: {
            width: 48,
            height: 48,
        }
    }
}))

// Enhanced dialog button styles
const ModernDialogButton = withStyles((theme) => ({
    root: {
        borderRadius: theme.spacing(2),
        padding: theme.spacing(1.5, 4),
        fontWeight: 700,
        textTransform: 'none',
        fontSize: '1rem',
        letterSpacing: '0.5px',
        transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
        position: 'relative',
        overflow: 'hidden',
        minWidth: 120,
        '&::before': {
            content: '""',
            position: 'absolute',
            top: 0,
            left: '-100%',
            width: '100%',
            height: '100%',
            background: 'linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent)',
            transition: 'left 0.6s ease',
        },
        '&:hover': {
            transform: 'translateY(-3px)',
            boxShadow: '0 8px 25px rgba(0, 0, 0, 0.15)',
            '&::before': {
                left: '100%',
            },
        },
        '&:active': {
            transform: 'translateY(-1px)',
        },
    },
}))(Button)

const PrimaryDialogButton = withStyles((theme) => ({
    root: {
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        color: 'white',
        boxShadow: '0 6px 20px rgba(102, 126, 234, 0.4)',
        '&:hover': {
            background: 'linear-gradient(135deg, #5a67d8 0%, #6b46c1 100%)',
            boxShadow: '0 8px 25px rgba(102, 126, 234, 0.5)',
        },
        '&:disabled': {
            background: theme.palette.grey[300],
            color: theme.palette.grey[500],
        }
    },
}))(ModernDialogButton)

const SecondaryDialogButton = withStyles((theme) => ({
    root: {
        backgroundColor: 'rgba(255, 255, 255, 0.9)',
        color: theme.palette.text.primary,
        border: `2px solid ${theme.palette.grey[200]}`,
        backdropFilter: 'blur(10px)',
        '&:hover': {
            backgroundColor: 'rgba(255, 255, 255, 1)',
            borderColor: theme.palette.grey[300],
            boxShadow: '0 4px 12px rgba(0, 0, 0, 0.1)',
        },
    },
}))(ModernDialogButton)

// Skeleton card component
const SkeletonCard = ({ classes }) => (
    <Card className={classes.skeletonCard}>
        <Skeleton variant="rect" height={220} />
        <CardHeader
            title={<Skeleton variant="text" width="80%" height={32} />}
            subheader={<Skeleton variant="text" width="60%" height={20} />}
        />
        <CardContent>
            <Skeleton variant="text" width="90%" height={20} />
            <Skeleton variant="text" width="70%" height={20} />
            <Skeleton variant="text" width="85%" height={20} />
        </CardContent>
        <CardActions style={{ justifyContent: 'center', paddingBottom: 24 }}>
            <Skeleton variant="circle" width={56} height={56} />
        </CardActions>
    </Card>
)

const ModernHotelRequest = () => {
    const classes = useStyles()
    const theme = useTheme()
    const isMobile = useMediaQuery(theme.breakpoints.down('sm'))
    const dispatch = useDispatch()
    const [page, setPage] = useState(1)
    const [hotelList, setHotelList] = useState([])
    const [initialLoading, setInitialLoading] = useState(true)
    const history = useHistory()

    useEffect(() => {
        getData()
    }, [])

    const getData = () => {
        setInitialLoading(true)
        axios.get('/rest/s1/welfare/hotel', {})
            .then(res => {
                setHotelList(res?.data?.hotel || [])
                setInitialLoading(false)
            })
            .catch(() => {
                dispatch(setAlertContent({
                    message: 'خطا در بارگذاری هتل‌ها',
                    severity: 'error'
                }))
                setInitialLoading(false)
            })
    }

    const handlePageChange = (event, value) => {
        setPage(value)
        // Smooth scroll to top
        window.scrollTo({ top: 0, behavior: 'smooth' })
    }

    const itemsPerPage = isMobile ? 4 : 8

    const location = useLocation()
    const [town, setTown] = useState([])
    const [periodList, setPeriodList] = useState([])
    const [reserveType, setReserveType] = useState({})
    const [formDefaults, setFormDefaults] = useState({})
    const [formData, setFormData] = useState({})
    const [error, setError] = useState({})
    const [loading, setLoading] = useState(false)
    const [add, setAdd] = useState(false)
    const persiansField = ["name"]
    const numberField = ["mobile", "nationalId"]
    const requiredField = [
        "firstName",
        "mobile",
        "nationalId",
        "province",
        "town",
        "lastName",
        "companion",
        "fromDate",
        "toDate",
    ]

    const enabledDatesFrom = useMemo(() => {
        return new Set(formDefaults?.fromDates)
    }, [formDefaults?.fromDates])
    
    const enabledDatesTo = useMemo(() => {
        return new Set(formDefaults?.toDates)
    }, [formDefaults?.toDates])

    const text = "درخواست شما با موفقیت ثبت شد. لطفا جهت پیگیری های بعدی به لیست درخواست ها مراجعه نمایید."
    
    const dateHandler = (date, name) => {
        setFormData({ ...formData, [name]: date?.format("yyyy-MM-DD") })
        if (date) setError({ ...error, [name]: false })
        if (
            (formData["fromDate"] !== undefined && name === "toDate") ||
            (formData["toDate"] !== undefined && name === "fromDate")
        ) {
            handleReservationType(name)
        }
    }
    
    const handleReservationType = () => {
        let fromList = formDefaults?.enableDateFrom
        let toList = formDefaults?.enableDateTo
        let fromReserve = fromList?.find((e) => e.date === formData?.fromDate)
        let toReserve = toList?.find((e) => e.date === formData?.toDate)

        if (fromReserve?.id !== toReserve?.id) {
            dispatch(setAlertContent("error", "تاریخ شروع و پایان باید از یک دوره باشند"))
            setFormData({ ...formData, fromDate: null, toDate: null })
            return
        }

        let reserveType = formDefaults?.reservationType?.find(
            (e) => e.reservationTypeId === fromReserve?.id
        )
        let period = formDefaults?.periods?.filter(
            (e) => e.reservationTypeId === fromReserve?.id
        )
        setReserveType(reserveType)
        setPeriodList(period)
    }
    
    const handleLink = () => {
        history.push("/user/request")
    }
    
    const submit = () => {
        let validate = validateForm()
        if (!validate) return false

        setLoading(true)
        let vars = { ...formData }
        vars.hotelId = formData?.hotel?.hotelId

        let data = {
            periodId: formData?.period?.periodId,
            fromDate: formData?.fromDate,
            toDate: formData?.toDate,
            reserveTypeId: reserveType?.reservationTypeId,
            variables: vars,
        }

        axios.post("/rest/s1/welfare/requests", data)
            .then((res) => {
                setLoading(false)
                if (res.data.state !== 1) {
                    dispatch(setAlertContent("error", res.data?.description || "خطا در ثبت اطلاعات"))
                    return false
                }
                dispatch(
                    setAlertContent(
                        "success",
                        <Link style={{ cursor: "pointer" }} onClick={handleLink}>
                            {text}
                        </Link>
                    )
                )
                setFormData({})
                setAdd(false)
            })
            .catch(() => {
                setLoading(false)
                dispatch(setAlertContent("error", "خطا در ارتباط با سرور"))
            })
    }
    
    const handleAutoComplete = (name, value) => {
        setFormData({ ...formData, [name]: value })
        if (value != null) setError({ ...error, [name]: false })
    }
    
    const disableDateToDate = (date) => {
        return !enabledDatesTo?.has(date.format("yyyy-MM-DD"))
    }
    
    const disableDateFromDate = (date) => {
        return !enabledDatesFrom?.has(date.format("yyyy-MM-DD"))
    }
    
    const clearForm = () => {
        setFormData({})
        setError({})
        setAdd(false)
    }
    
    const getDetailData = () => {
        const hotelId = formData?.hotel?.hotelId
        return new Promise((resolve) => {
            axios.get(`/rest/s1/welfare/${hotelId}/requestPage`)
                .then((res) => {
                    setFormDefaults(res?.data)
                    resolve("ok")
                })
                .catch(() => {
                    dispatch(setAlertContent("error", "خطا در دریافت اطلاعات"))
                })
        })
    }
    
    const handleChange = (event) => {
        if (
            event.target.value !== "" &&
            persiansField.includes(event.target.name) &&
            !isPersian(event.target.value)
        ) return false

        if (
            event.target.value !== "" &&
            numberField.includes(event.target.name) &&
            !isNumber(event.target.value)
        ) return false

        setFormData({ ...formData, [event.target.name]: event.target.value })
        setError({ ...error, [event.target.name]: false })
    }
    
    const handleProvince = (value) => {
        if (!value) return
        setError({ ...error, province: false })
        setFormData({ ...formData, province: value })

        axios.get(`/rest/s1/general/location?input=province_id&output=township&inputValue=${value?.province_id}`)
            .then((res) => {
                if (res.data.state === 1) {
                    setTown(res?.data?.location)
                }
            })
    }
    
    const validateForm = () => {
        let isValid = true
        const newError = { ...error }

        if (reserveType?.type === "STPeriod" || reserveType?.type === "STLPeriod") {
            requiredField.push("period")
        } else {
            delete newError.period
        }

        requiredField.forEach((field) => {
            if (!formData[field]) {
                newError[field] = true
                isValid = false
            } else {
                newError[field] = false
            }
        })

        setError(newError)
        return isValid
    }
    
    const showAdd = async (hotel) => {
        formData["hotel"] = hotel
        await getDetailData()
        setAdd(true)
    }

    // Enhanced dialog content with better styling
    const dialogContent = (
        <Box className={classes.dialogContent}>
            <div className={classes.formSection}>
                <Typography variant="h6" gutterBottom style={{ 
                    color: theme.palette.primary.main, 
                    fontWeight: 700,
                    display: 'flex',
                    alignItems: 'center',
                    gap: theme.spacing(1)
                }}>
                    <PersonIcon />
                    اطلاعات شخصی
                </Typography>
                <Grid container spacing={3} className={classes.formContainer}>
                    <Grid item xs={12} md={6}>
                        <TextField
                            variant="outlined"
                            required
                            name="firstName"
                            label="نام"
                            value={formData?.firstName || ""}
                            error={error.firstName}
                            helperText={error.firstName && "لطفا نام را وارد نمایید"}
                            fullWidth
                            className={classes.modernTextField}
                            InputProps={{
                                startAdornment: (
                                    <InputAdornment position="start">
                                        <PersonIcon />
                                    </InputAdornment>
                                )
                            }}
                            onChange={handleChange}
                        />
                    </Grid>
                    <Grid item xs={12} md={6}>
                        <TextField
                            variant="outlined"
                            required
                            name="lastName"
                            label="نام خانوادگی"
                            value={formData?.lastName || ""}
                            error={error.lastName}
                            helperText={error.lastName && "لطفا نام خانوادگی را وارد نمایید"}
                            fullWidth
                            className={classes.modernTextField}
                            InputProps={{
                                startAdornment: (
                                    <InputAdornment position="start">
                                        <PersonIcon />
                                    </InputAdornment>
                                )
                            }}
                            onChange={handleChange}
                        />
                    </Grid>
                    <Grid item xs={12} md={6}>
                        <TextField
                            variant="outlined"
                            required
                            name="mobile"
                            label="شماره همراه"
                            error={error.mobile}
                            helperText={error.mobile ? "شماره همراه معتبر وارد نمایید" : ""}
                            value={formData?.mobile || ""}
                            inputProps={{ maxLength: 11 }}
                            fullWidth
                            className={classes.modernTextField}
                            InputProps={{
                                startAdornment: (
                                    <InputAdornment position="start">
                                        <PhoneIcon />
                                    </InputAdornment>
                                )
                            }}
                            onChange={handleChange}
                        />
                    </Grid>
                    <Grid item xs={12} md={6}>
                        <TextField
                            variant="outlined"
                            required
                            name="nationalId"
                            label="کد ملی"
                            error={error.nationalId}
                            helperText={error.nationalId ? "کد ملی معتبر وارد نمایید" : ""}
                            value={formData?.nationalId || ""}
                            inputProps={{ maxLength: 10 }}
                            fullWidth
                            className={classes.modernTextField}
                            InputProps={{
                                startAdornment: (
                                    <InputAdornment position="start">
                                        <FingerprintIcon />
                                    </InputAdornment>
                                )
                            }}
                            onChange={handleChange}
                        />
                    </Grid>
                </Grid>
            </div>

            <div className={classes.formSection}>
                <Typography variant="h6" gutterBottom style={{ 
                    color: theme.palette.primary.main, 
                    fontWeight: 700,
                    display: 'flex',
                    alignItems: 'center',
                    gap: theme.spacing(1)
                }}>
                    <LocationIcon />
                    اطلاعات آدرس
                </Typography>
                <Grid container spacing={3} className={classes.formContainer}>
                    <Grid item xs={12} md={6}>
                        <Autocomplete
                            name="province"
                            onChange={(_, value) => handleProvince(value)}
                            value={formData?.province || null}
                            options={formDefaults?.province || []}
                            getOptionLabel={(option) => option?.province_name || ""}
                            getOptionSelected={(option, value) => option.province_id === value.province_id}
                            renderInput={(params) => (
                                <TextField
                                    {...params}
                                    required
                                    error={error.province}
                                    helperText={error.province && "لطفا استان را انتخاب نمایید"}
                                    variant="outlined"
                                    label="استان"
                                    fullWidth
                                    className={classes.modernTextField}
                                    InputProps={{
                                        ...params.InputProps,
                                        startAdornment: (
                                            <>
                                                <InputAdornment position="start">
                                                    <HomeIcon />
                                                </InputAdornment>
                                                {params.InputProps.startAdornment}
                                            </>
                                        )
                                    }}
                                />
                            )}
                            className={classes.modernAutocomplete}
                        />
                    </Grid>
                    <Grid item xs={12} md={6}>
                        <Autocomplete
                            name="city"
                            onChange={(_, value) => handleAutoComplete("town", value)}
                            value={formData?.town || null}
                            options={town || []}
                            getOptionLabel={(option) => option?.township_name || ""}
                            getOptionSelected={(option, value) => option.township_id === value.township_id}
                            renderInput={(params) => (
                                <TextField
                                    {...params}
                                    required
                                    error={error.town}
                                    helperText={error.town && "لطفا شهر را انتخاب نمایید"}
                                    variant="outlined"
                                    label="شهر"
                                    fullWidth
                                    className={classes.modernTextField}
                                    InputProps={{
                                        ...params.InputProps,
                                        startAdornment: (
                                            <>
                                                <InputAdornment position="start">
                                                    <CityIcon />
                                                </InputAdornment>
                                                {params.InputProps.startAdornment}
                                            </>
                                        )
                                    }}
                                />
                            )}
                            className={classes.modernAutocomplete}
                        />
                    </Grid>
                </Grid>
            </div>

            <div className={classes.formSection}>
                <Typography variant="h6" gutterBottom style={{ 
                    color: theme.palette.primary.main, 
                    fontWeight: 700,
                    display: 'flex',
                    alignItems: 'center',
                    gap: theme.spacing(1)
                }}>
                    <CalendarIcon />
                    اطلاعات رزرو
                </Typography>
                <Grid container spacing={3} className={classes.formContainer}>
                    <Grid item xs={12} md={4}>
                        <TextField
                            name="companion"
                            required
                            label="تعداد کل نفرات"
                            fullWidth
                            error={error.companion}
                            type="number"
                            helperText={error.companion ? "لطفا تعداد نفرات را وارد نمایید" : ""}
                            value={formData?.companion || ""}
                            variant="outlined"
                            className={classes.modernTextField}
                            InputProps={{
                                startAdornment: (
                                    <InputAdornment position="start">
                                        <PeopleIcon />
                                    </InputAdornment>
                                )
                            }}
                            onChange={handleChange}
                        />
                    </Grid>
                    <Grid item xs={12} md={4}>
                        <DatePicker
                            variant="outlined"
                            name="fromDate"
                            value={formData?.fromDate || null}
                            setValue={(date) => dateHandler(date, "fromDate")}
                            shouldDisableDate={disableDateFromDate}
                            error={error.fromDate}
                            helperText={error.fromDate ? "لطفا تاریخ شروع را انتخاب نمایید" : ""}
                            required
                            format="jYYYY/jMM/jDD"
                            label="از تاریخ"
                            fullWidth
                            className={classes.modernTextField}
                            InputProps={{
                                startAdornment: (
                                    <InputAdornment position="start">
                                        <CalendarIcon />
                                    </InputAdornment>
                                )
                            }}
                        />
                    </Grid>
                    <Grid item xs={12} md={4}>
                        <DatePicker
                            variant="outlined"
                            name="toDate"
                            value={formData?.toDate || null}
                            setValue={(date) => dateHandler(date, "toDate")}
                            shouldDisableDate={disableDateToDate}
                            error={error.toDate}
                            helperText={error.toDate ? "لطفا تاریخ پایان را انتخاب نمایید" : ""}
                            required
                            format="jYYYY/jMM/jDD"
                            label="تا تاریخ"
                            fullWidth
                            className={classes.modernTextField}
                            InputProps={{
                                startAdornment: (
                                    <InputAdornment position="start">
                                        <CalendarIcon />
                                    </InputAdornment>
                                )
                            }}
                        />
                    </Grid>
                </Grid>
            </div>

            {/* Loading backdrop for form submission */}
            <Backdrop open={loading} style={{ zIndex: theme.zIndex.drawer + 1, color: '#fff' }}>
                <Box display="flex" flexDirection="column" alignItems="center" gap={2}>
                    <CircularProgress color="inherit" size={60} />
                    <Typography variant="h6">در حال ثبت درخواست...</Typography>
                </Box>
            </Backdrop>
        </Box>
    )

    const dialogActions = (
        <Box display="flex" gap={2} justifyContent="flex-end" p={3}>
            <SecondaryDialogButton 
                onClick={clearForm}
                disabled={loading}
                startIcon={<CloseIcon />}
            >
                لغو
            </SecondaryDialogButton>
            <PrimaryDialogButton 
                onClick={submit}
                disabled={loading}
                startIcon={loading ? <CircularProgress size={20} color="inherit" /> : <SendIcon />}
            >
                {loading ? 'در حال ثبت...' : 'ثبت درخواست'}
            </PrimaryDialogButton>
        </Box>
    )

    return (
        <div className={classes.pageContainer}>
            <StyledDialog
                open={add}
                onClose={clearForm}
                title={
                    <Box display="flex" alignItems="center" gap={2}>
                        <Avatar style={{ 
                            background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                            width: 40,
                            height: 40
                        }}>
                            <HotelIcon />
                        </Avatar>
                        <Box>
                            <Typography variant="h5" style={{ fontWeight: 700 }}>
                                ثبت درخواست رزرو
                            </Typography>
                            <Typography variant="body2" color="textSecondary">
                                {formData?.hotel?.name}
                            </Typography>
                        </Box>
                    </Box>
                }
                maxWidth="md"
                fullWidth
                PaperProps={{
                    style: {
                        borderRadius: 24,
                        background: 'rgba(255, 255, 255, 0.95)',
                        backdropFilter: 'blur(20px)',
                        border: '1px solid rgba(255, 255, 255, 0.2)',
                    }
                }}
            >
                {dialogContent}
                {dialogActions}
            </StyledDialog>

            <FusePageSimple
                content={
                    <Container maxWidth="xl">
                        <Box py={4}>
                            {/* Header Section */}
                            <div className={classes.sectionHeader}>
                                <Typography variant="h4" component="h1">
                                    رزرو هتل های رفاهی
                                </Typography>
                                <Typography variant="body1">
                                    بهترین هتل‌های رفاهی را انتخاب کنید و تجربه‌ای فراموش‌نشدنی داشته باشید
                                </Typography>
                            </div>

                            {/* Loading State */}
                            {initialLoading ? (
                                <Grid container spacing={3}>
                                    {Array.from({ length: 8 }).map((_, index) => (
                                        <Grid item xs={12} sm={6} md={4} lg={3} key={index}>
                                            <SkeletonCard classes={classes} />
                                        </Grid>
                                    ))}
                                </Grid>
                            ) : (
                                /* Hotel Cards */
                                <Grid container spacing={3}>
                                    {hotelList?.slice((page - 1) * itemsPerPage, page * itemsPerPage)?.map((hotel, index) => (
                                        <Grid
                                            item
                                            xs={12}
                                            sm={6}
                                            md={4}
                                            lg={3}
                                            key={hotel.hotelId || index}
                                        >
                                            <Fade in timeout={600 + index * 100}>
                                                <Card
                                                    className={`${classes.hotelCard} ${classes.cardAnimation}`}
                                                    style={{
                                                        animationDelay: `${index * 0.1}s`,
                                                    }}
                                                >
                                                    {/* Enhanced Image Section */}
                                                    <div className={classes.cardMedia}>
                                                        <ImageSlide 
                                                            autoplay={false} 
                                                            indicators={hotel?.files?.length > 1} 
                                                            transitionDuration={800}
                                                            arrows={hotel?.files?.length > 1}
                                                        >
                                                            {hotel?.files?.length > 0 ?
                                                                hotel.files.map((file) => (
                                                                    <div className="each-slide-effect" key={file.fileId}>
                                                                        <img
                                                                            src={`${SERVER_URL}/rest/s1/general/download?fileId=${file.fileId}`}
                                                                            alt={hotel.name}
                                                                            loading="lazy"
                                                                        />
                                                                    </div>
                                                                )) : (
                                                                    <div className="each-slide-effect">
                                                                        <img
                                                                            src={hotelPlaceholder}
                                                                            alt="Hotel placeholder"
                                                                            loading="lazy"
                                                                        />
                                                                    </div>
                                                                )
                                                            }
                                                        </ImageSlide>
                                                        
                                                        {hotel.rating && (
                                                            <Zoom in timeout={800 + index * 100}>
                                                                <Chip
                                                                    icon={<StarIcon />}
                                                                    label={`${hotel.rating} ⭐`}
                                                                    className={classes.ratingChip}
                                                                    size="small"
                                                                />
                                                            </Zoom>
                                                        )}
                                                    </div>

                                                    {/* Enhanced Hotel Info */}
                                                    <CardHeader
                                                        title={hotel?.name || "نام نامشخص"}
                                                        subheader={hotel?.location || "موقعیت نامشخص"}
                                                        className={classes.cardHeader}
                                                    />

                                                    <CardContent className={classes.cardContent}>
                                                        {hotel.phone && (
                                                            <div className={classes.phoneInfo}>
                                                                <PhoneIcon fontSize="small" />
                                                                <Typography variant="body2">
                                                                    {hotel.phone}
                                                                </Typography>
                                                            </div>
                                                        )}

                                                        {hotel.description && (
                                                            <Typography 
                                                                variant="body2" 
                                                                className={classes.description}
                                                            >
                                                                {hotel.description}
                                                            </Typography>
                                                        )}
                                                    </CardContent>

                                                    <CardActions className={classes.cardActions}>
                                                        <Zoom in timeout={1000 + index * 100}>
                                                            <IconButton
                                                                aria-label="درخواست رزرو"
                                                                onClick={() => showAdd(hotel)}
                                                                className={classes.requestButton}
                                                            >
                                                                <AddIcon />
                                                            </IconButton>
                                                        </Zoom>
                                                    </CardActions>
                                                </Card>
                                            </Fade>
                                        </Grid>
                                    ))}
                                </Grid>
                            )}

                            {/* Enhanced Pagination */}
                            {hotelList.length > 0 && (
                                <Fade in timeout={1200}>
                                    <div className={classes.paginationContainer}>
                                        <Pagination
                                            count={Math.ceil(hotelList.length / itemsPerPage)}
                                            page={page}
                                            onChange={handlePageChange}
                                            color="primary"
                                            size={isMobile ? 'small' : 'large'}
                                            showFirstButton
                                            showLastButton
                                            siblingCount={isMobile ? 0 : 2}
                                        />
                                    </div>
                                </Fade>
                            )}

                            {/* Empty State */}
                            {!initialLoading && hotelList.length === 0 && (
                                <Box className={classes.loadingContainer}>
                                    <Avatar style={{ 
                                        width: 80, 
                                        height: 80, 
                                        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                                        marginBottom: theme.spacing(2)
                                    }}>
                                        <InfoIcon style={{ fontSize: 40 }} />
                                    </Avatar>
                                    <Typography variant="h5" color="textSecondary" gutterBottom>
                                        هیچ هتلی یافت نشد
                                    </Typography>
                                    <Typography variant="body1" color="textSecondary">
                                        در حال حاضر هیچ هتل رفاهی در دسترس نیست
                                    </Typography>
                                </Box>
                            )}
                        </Box>
                    </Container>
                }
            />
        </div>
    )
}

export default ModernHotelRequest