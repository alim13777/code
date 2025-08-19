import React, { useState } from 'react';
import {
    Box,
    Button,
    Grid,
    Typography,
    TextField,
    Container,
    Card,
    CardContent,
    CardActions,
    Avatar,
    Chip
} from '@material-ui/core';
import {
    Delete as DeleteIcon,
    Save as SaveIcon,
    Info as InfoIcon,
    Warning as WarningIcon,
    CheckCircle as CheckCircleIcon,
    Error as ErrorIcon,
    Person as PersonIcon,
    Settings as SettingsIcon
} from '@material-ui/icons';
import { makeStyles } from '@material-ui/core/styles';
import ModernStyledDialog, { 
    ConfirmDialog, 
    InfoDialog, 
    ErrorDialog, 
    SuccessDialog 
} from './ModernStyledDialog';

const useStyles = makeStyles((theme) => ({
    container: {
        padding: theme.spacing(4),
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        minHeight: '100vh',
    },
    card: {
        borderRadius: 20,
        background: 'rgba(255, 255, 255, 0.9)',
        backdropFilter: 'blur(10px)',
        border: '1px solid rgba(255, 255, 255, 0.2)',
        boxShadow: '0 8px 32px rgba(0, 0, 0, 0.1)',
        marginBottom: theme.spacing(3),
    },
    title: {
        color: 'white',
        textAlign: 'center',
        marginBottom: theme.spacing(4),
        fontWeight: 700,
        textShadow: '0 2px 4px rgba(0, 0, 0, 0.3)',
    },
    demoButton: {
        borderRadius: 12,
        padding: theme.spacing(1.5, 3),
        fontWeight: 600,
        textTransform: 'none',
        margin: theme.spacing(1),
        transition: 'all 0.3s ease',
        '&:hover': {
            transform: 'translateY(-2px)',
            boxShadow: '0 6px 20px rgba(0, 0, 0, 0.15)',
        }
    },
    formField: {
        marginBottom: theme.spacing(3),
        '& .MuiOutlinedInput-root': {
            borderRadius: 12,
        }
    }
}));

const DialogExamples = () => {
    const classes = useStyles();
    
    // Dialog states
    const [basicOpen, setBasicOpen] = useState(false);
    const [confirmOpen, setConfirmOpen] = useState(false);
    const [infoOpen, setInfoOpen] = useState(false);
    const [errorOpen, setErrorOpen] = useState(false);
    const [successOpen, setSuccessOpen] = useState(false);
    const [formOpen, setFormOpen] = useState(false);
    const [loadingOpen, setLoadingOpen] = useState(false);
    const [loading, setLoading] = useState(false);
    
    // Form data
    const [formData, setFormData] = useState({
        name: '',
        email: '',
        message: ''
    });

    const handleFormSubmit = () => {
        setLoading(true);
        // Simulate API call
        setTimeout(() => {
            setLoading(false);
            setFormOpen(false);
            setSuccessOpen(true);
            setFormData({ name: '', email: '', message: '' });
        }, 2000);
    };

    const handleLoadingDemo = () => {
        setLoadingOpen(true);
        setLoading(true);
        setTimeout(() => {
            setLoading(false);
            setLoadingOpen(false);
        }, 3000);
    };

    return (
        <div className={classes.container}>
            <Container maxWidth="lg">
                <Typography variant="h3" className={classes.title}>
                    Modern Dialog Components
                </Typography>

                <Grid container spacing={3}>
                    {/* Basic Dialog Examples */}
                    <Grid item xs={12} md={6}>
                        <Card className={classes.card}>
                            <CardContent>
                                <Typography variant="h6" gutterBottom>
                                    Basic Dialogs
                                </Typography>
                                <Typography variant="body2" color="textSecondary" paragraph>
                                    Simple dialogs with different configurations
                                </Typography>
                            </CardContent>
                            <CardActions>
                                <Button 
                                    className={classes.demoButton}
                                    variant="contained" 
                                    color="primary"
                                    onClick={() => setBasicOpen(true)}
                                >
                                    Basic Dialog
                                </Button>
                                <Button 
                                    className={classes.demoButton}
                                    variant="contained" 
                                    color="secondary"
                                    onClick={() => setFormOpen(true)}
                                    startIcon={<PersonIcon />}
                                >
                                    Form Dialog
                                </Button>
                            </CardActions>
                        </Card>
                    </Grid>

                    {/* Notification Dialogs */}
                    <Grid item xs={12} md={6}>
                        <Card className={classes.card}>
                            <CardContent>
                                <Typography variant="h6" gutterBottom>
                                    Notification Dialogs
                                </Typography>
                                <Typography variant="body2" color="textSecondary" paragraph>
                                    Dialogs for different types of notifications
                                </Typography>
                            </CardContent>
                            <CardActions>
                                <Button 
                                    className={classes.demoButton}
                                    variant="outlined"
                                    onClick={() => setInfoOpen(true)}
                                    startIcon={<InfoIcon />}
                                    style={{ color: '#2196f3', borderColor: '#2196f3' }}
                                >
                                    Info
                                </Button>
                                <Button 
                                    className={classes.demoButton}
                                    variant="outlined"
                                    onClick={() => setErrorOpen(true)}
                                    startIcon={<ErrorIcon />}
                                    style={{ color: '#f44336', borderColor: '#f44336' }}
                                >
                                    Error
                                </Button>
                                <Button 
                                    className={classes.demoButton}
                                    variant="outlined"
                                    onClick={() => setSuccessOpen(true)}
                                    startIcon={<CheckCircleIcon />}
                                    style={{ color: '#4caf50', borderColor: '#4caf50' }}
                                >
                                    Success
                                </Button>
                            </CardActions>
                        </Card>
                    </Grid>

                    {/* Interactive Dialogs */}
                    <Grid item xs={12} md={6}>
                        <Card className={classes.card}>
                            <CardContent>
                                <Typography variant="h6" gutterBottom>
                                    Interactive Dialogs
                                </Typography>
                                <Typography variant="body2" color="textSecondary" paragraph>
                                    Dialogs with user interactions and confirmations
                                </Typography>
                            </CardContent>
                            <CardActions>
                                <Button 
                                    className={classes.demoButton}
                                    variant="outlined"
                                    onClick={() => setConfirmOpen(true)}
                                    startIcon={<WarningIcon />}
                                    style={{ color: '#ff9800', borderColor: '#ff9800' }}
                                >
                                    Confirm Delete
                                </Button>
                                <Button 
                                    className={classes.demoButton}
                                    variant="contained"
                                    onClick={handleLoadingDemo}
                                    style={{ 
                                        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                                        color: 'white'
                                    }}
                                >
                                    Loading Demo
                                </Button>
                            </CardActions>
                        </Card>
                    </Grid>

                    {/* Features */}
                    <Grid item xs={12} md={6}>
                        <Card className={classes.card}>
                            <CardContent>
                                <Typography variant="h6" gutterBottom>
                                    Features
                                </Typography>
                                <Box display="flex" flexWrap="wrap" gap={1}>
                                    <Chip label="Glass-morphism" size="small" color="primary" />
                                    <Chip label="Smooth Animations" size="small" color="secondary" />
                                    <Chip label="Loading States" size="small" />
                                    <Chip label="Type Icons" size="small" color="primary" />
                                    <Chip label="Backdrop Blur" size="small" color="secondary" />
                                    <Chip label="Gradient Effects" size="small" />
                                </Box>
                            </CardContent>
                        </Card>
                    </Grid>
                </Grid>

                {/* Basic Dialog */}
                <ModernStyledDialog
                    open={basicOpen}
                    onClose={() => setBasicOpen(false)}
                    title="Modern Dialog Example"
                    headerIcon={<SettingsIcon />}
                    primaryAction={{
                        label: "Save Changes",
                        onClick: () => setBasicOpen(false),
                        icon: <SaveIcon />
                    }}
                    secondaryAction={{
                        label: "Cancel",
                        onClick: () => setBasicOpen(false)
                    }}
                >
                    <Typography paragraph>
                        This is a modern styled dialog with glass-morphism effects, smooth animations, 
                        and beautiful gradients. The dialog uses backdrop blur and modern design patterns 
                        to create a premium user experience.
                    </Typography>
                    <Typography variant="body2" color="textSecondary">
                        Features include animated entrance, gradient headers, and modern button styling.
                    </Typography>
                </ModernStyledDialog>

                {/* Form Dialog */}
                <ModernStyledDialog
                    open={formOpen}
                    onClose={() => setFormOpen(false)}
                    title="Contact Form"
                    type="info"
                    loading={loading}
                    maxWidth="sm"
                    primaryAction={{
                        label: "Send Message",
                        loadingLabel: "Sending...",
                        onClick: handleFormSubmit,
                        icon: <SaveIcon />
                    }}
                    secondaryAction={{
                        label: "Cancel",
                        onClick: () => setFormOpen(false)
                    }}
                >
                    <Grid container spacing={3}>
                        <Grid item xs={12}>
                            <TextField
                                fullWidth
                                label="Full Name"
                                variant="outlined"
                                value={formData.name}
                                onChange={(e) => setFormData({...formData, name: e.target.value})}
                                className={classes.formField}
                            />
                        </Grid>
                        <Grid item xs={12}>
                            <TextField
                                fullWidth
                                label="Email Address"
                                variant="outlined"
                                type="email"
                                value={formData.email}
                                onChange={(e) => setFormData({...formData, email: e.target.value})}
                                className={classes.formField}
                            />
                        </Grid>
                        <Grid item xs={12}>
                            <TextField
                                fullWidth
                                label="Message"
                                variant="outlined"
                                multiline
                                rows={4}
                                value={formData.message}
                                onChange={(e) => setFormData({...formData, message: e.target.value})}
                                className={classes.formField}
                            />
                        </Grid>
                    </Grid>
                </ModernStyledDialog>

                {/* Confirm Dialog */}
                <ConfirmDialog
                    open={confirmOpen}
                    onClose={() => setConfirmOpen(false)}
                    title="Confirm Deletion"
                    primaryAction={{
                        label: "Delete",
                        onClick: () => setConfirmOpen(false),
                        icon: <DeleteIcon />
                    }}
                    secondaryAction={{
                        label: "Cancel",
                        onClick: () => setConfirmOpen(false)
                    }}
                >
                    <Typography>
                        Are you sure you want to delete this item? This action cannot be undone.
                    </Typography>
                </ConfirmDialog>

                {/* Info Dialog */}
                <InfoDialog
                    open={infoOpen}
                    onClose={() => setInfoOpen(false)}
                    title="Information"
                    primaryAction={{
                        label: "Got it",
                        onClick: () => setInfoOpen(false)
                    }}
                >
                    <Typography>
                        This is an informational dialog with a modern design and smooth animations.
                    </Typography>
                </InfoDialog>

                {/* Error Dialog */}
                <ErrorDialog
                    open={errorOpen}
                    onClose={() => setErrorOpen(false)}
                    title="Error Occurred"
                    primaryAction={{
                        label: "Try Again",
                        onClick: () => setErrorOpen(false)
                    }}
                    secondaryAction={{
                        label: "Cancel",
                        onClick: () => setErrorOpen(false)
                    }}
                >
                    <Typography>
                        An error occurred while processing your request. Please try again later.
                    </Typography>
                </ErrorDialog>

                {/* Success Dialog */}
                <SuccessDialog
                    open={successOpen}
                    onClose={() => setSuccessOpen(false)}
                    title="Success!"
                    primaryAction={{
                        label: "Continue",
                        onClick: () => setSuccessOpen(false)
                    }}
                >
                    <Typography>
                        Your message has been sent successfully! We'll get back to you soon.
                    </Typography>
                </SuccessDialog>

                {/* Loading Dialog */}
                <ModernStyledDialog
                    open={loadingOpen}
                    title="Processing Request"
                    loading={loading}
                    maxWidth="xs"
                    primaryAction={{
                        label: "Process",
                        loadingLabel: "Processing...",
                        onClick: () => {}
                    }}
                >
                    <Typography align="center">
                        Please wait while we process your request...
                    </Typography>
                </ModernStyledDialog>
            </Container>
        </div>
    );
};

export default DialogExamples;