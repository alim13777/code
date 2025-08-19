import * as React from 'react';
import { useState } from 'react';
import {
    Card,
    CardActions,
    CardContent,
    CardHeader,
    CardMedia,
    Grid,
    IconButton,
    Typography,
    Avatar,
    Pagination,
    Box,
    Chip,
    Stack
} from "@mui/material";
import { Add as AddIcon, Phone as PhoneIcon, Star as StarIcon } from "@mui/icons-material";
// import { useNavigate } from "react-router-dom";
import { FusePageSimple } from "../../../../../@fuse";
import axios from "axios";
import { SERVER_URL } from "../../../../../configs";
import { setAlertContent } from "../../../../store/actions";
import { useDispatch } from "react-redux";
import { Slide } from "react-slideshow-image";
import 'react-slideshow-image/dist/styles.css';
import hotelPlaceholder from "./../../../../../images/hotel.jpg";

// Custom styles
const cardStyles = {
    height: '100%',
    display: 'flex',
    flexDirection: 'column',
    transition: 'transform 0.3s, box-shadow 0.3s',
    '&:hover': {
        transform: 'translateY(-5px)',
        boxShadow: '0 10px 20px rgba(0,0,0,0.1)'
    }
};

const mediaStyles = {
    position: 'relative',
    pt: '56.25%', // 16:9 aspect ratio
};

const slideContainerStyles = {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    '& .each-slide-effect': {
        height: '100%',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundSize: 'cover',
        backgroundPosition: 'center'
    },
    '& img': {
        width: '100%',
        height: '100%',
        objectFit: 'cover'
    }
};

const ratingChipStyles = {
    position: 'absolute',
    top: 8,
    right: 8,
    backgroundColor: 'rgba(0, 0, 0, 0.7)',
    color: 'white',
    zIndex: 1
};

const Request = () => {
    const [expanded, setExpanded] = React.useState(false);
    const dispatch = useDispatch();
    const [page, setPage] = useState(1);
    const [hotelList, setHotelList] = useState([]);
    // const navigate = useNavigate();
    const navigate=()=>{

    }
    React.useEffect(() => {
        getData();
    }, []);

    const getData = () => {
        axios.get("/rest/s1/welfare/hotel", {})
            .then(res => {
                setHotelList(res?.data?.hotel || []);
            })
            .catch(error => {
                dispatch(setAlertContent({
                    message: "Failed to load hotels",
                    severity: "error"
                }));
            });
    };

    const handlePageChange = (event, value) => {
        setPage(value);
    };

    return (
        <FusePageSimple
            content={
                <Box sx={{ p: { xs: 2, md: 3 } }}>
                    <Grid container spacing={3}>
                        {hotelList?.slice((page - 1) * 8, page * 8)?.map((hotel, index) => (
                            <Grid item xs={12} sm={6} md={4} lg={3} key={hotel.hotelId || index}>
                                <Card sx={cardStyles}>
                                    <Box sx={mediaStyles}>
                                        <Box sx={slideContainerStyles}>
                                            <Slide autoplay={false} indicators={true}>
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
                                            </Slide>
                                        </Box>
                                        {hotel.rating && (
                                            <Chip
                                                icon={<StarIcon sx={{ color: 'gold' }} />}
                                                label={hotel.rating}
                                                sx={ratingChipStyles}
                                                size="small"
                                            />
                                        )}
                                    </Box>

                                    <CardHeader
                                        title={hotel?.name || "Unnamed Hotel"}
                                        titleTypographyProps={{
                                            variant: 'h6',
                                            component: 'h3',
                                            noWrap: true
                                        }}
                                        subheader={hotel?.location}
                                        subheaderTypographyProps={{
                                            variant: 'body2',
                                            color: 'text.secondary',
                                            noWrap: true
                                        }}
                                    />

                                    <CardContent sx={{ flexGrow: 1 }}>
                                        <Stack direction="row" alignItems="center" spacing={1} mb={1}>
                                            <PhoneIcon color="primary" fontSize="small" />
                                            <Typography variant="body2" color="text.primary">
                                                {hotel.phone || "Not specified"}
                                            </Typography>
                                        </Stack>

                                        {hotel.description && (
                                            <Typography variant="body2" color="text.secondary" sx={{
                                                display: '-webkit-box',
                                                WebkitLineClamp: 2,
                                                WebkitBoxOrient: 'vertical',
                                                overflow: 'hidden'
                                            }}>
                                                {hotel.description}
                                            </Typography>
                                        )}
                                    </CardContent>

                                    <CardActions sx={{ justifyContent: 'flex-end' }}>
                                        <IconButton
                                            aria-label="Request reservation"
                                            onClick={() => navigate("/welfare/subPage/requestDetail", {
                                                state: {
                                                    hotelId: hotel.hotelId,
                                                    hotelName: hotel?.name
                                                }
                                            })}
                                            sx={{
                                                backgroundColor: 'primary.main',
                                                color: 'primary.contrastText',
                                                '&:hover': {
                                                    backgroundColor: 'primary.dark'
                                                }
                                            }}
                                        >
                                            <AddIcon />
                                        </IconButton>
                                    </CardActions>
                                </Card>
                            </Grid>
                        ))}
                    </Grid>

                    {hotelList.length > 0 && (
                        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
                            <Pagination
                                count={Math.ceil(hotelList.length / 8)}
                                page={page}
                                onChange={handlePageChange}
                                color="primary"
                                size="large"
                                showFirstButton
                                showLastButton
                            />
                        </Box>
                    )}
                </Box>
            }
        />
    );
};

export default Request;