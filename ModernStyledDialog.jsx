import React, { useState, useEffect } from 'react';
import {
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    Grid,
    IconButton,
    Typography,
    Box,
    Button,
    withStyles,
    Fade,
    Zoom,
    Slide,
    Backdrop,
    CircularProgress,
    Avatar,
    Divider
} from '@material-ui/core';
import { 
    Close as CloseIcon,
    Check as CheckIcon,
    Warning as WarningIcon,
    Info as InfoIcon,
    Error as ErrorIcon
} from '@material-ui/icons';

// Enhanced backdrop with blur effect
const ModernBackdrop = withStyles(theme => ({
    root: {
        backgroundColor: 'rgba(15, 23, 42, 0.6)',
        backdropFilter: 'blur(12px)',
        transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
    }
}))(Backdrop);

// Main dialog container with glass-morphism
const DialogContainer = withStyles(theme => ({
    root: {
        '& .MuiDialog-paper': {
            borderRadius: 28,
            padding: 0,
            background: `
                linear-gradient(145deg, 
                    rgba(255, 255, 255, 0.95) 0%, 
                    rgba(248, 250, 252, 0.9) 50%,
                    rgba(241, 245, 249, 0.95) 100%
                )
            `,
            backdropFilter: 'blur(20px)',
            border: '1px solid rgba(255, 255, 255, 0.3)',
            boxShadow: `
                0 25px 50px -12px rgba(0, 0, 0, 0.25),
                0 0 0 1px rgba(255, 255, 255, 0.1),
                inset 0 1px 0 rgba(255, 255, 255, 0.6)
            `,
            overflow: 'hidden',
            position: 'relative',
            maxHeight: '90vh',
            '&::before': {
                content: '""',
                position: 'absolute',
                top: 0,
                left: 0,
                right: 0,
                height: 1,
                background: 'linear-gradient(90deg, transparent, rgba(255,255,255,0.8), transparent)',
            },
            // Animated entrance
            animation: '$dialogEntrance 0.4s cubic-bezier(0.34, 1.56, 0.64, 1)',
        },
        '@keyframes dialogEntrance': {
            '0%': {
                opacity: 0,
                transform: 'scale(0.8) translateY(20px)',
            },
            '100%': {
                opacity: 1,
                transform: 'scale(1) translateY(0)',
            }
        }
    }
}))(Dialog);

// Enhanced header with gradient and animations
const DialogHeader = withStyles(theme => ({
    root: {
        padding: theme.spacing(4, 5),
        background: `
            linear-gradient(135deg, 
                #667eea 0%, 
                #764ba2 50%,
                #667eea 100%
            )
        `,
        backgroundSize: '200% 200%',
        animation: '$gradientShift 6s ease infinite',
        color: 'white',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        position: 'relative',
        minHeight: 80,
        '&::before': {
            content: '""',
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: 'linear-gradient(135deg, rgba(255,255,255,0.1) 0%, transparent 50%, rgba(255,255,255,0.05) 100%)',
            pointerEvents: 'none',
        },
        '&::after': {
            content: '""',
            position: 'absolute',
            bottom: 0,
            left: '10%',
            right: '10%',
            height: 3,
            background: 'linear-gradient(90deg, transparent, rgba(255,255,255,0.6), transparent)',
            borderRadius: '2px 2px 0 0',
        },
        '@keyframes gradientShift': {
            '0%': { backgroundPosition: '0% 50%' },
            '50%': { backgroundPosition: '100% 50%' },
            '100%': { backgroundPosition: '0% 50%' }
        }
    }
}))(DialogTitle);

// Enhanced content area
const DialogBody = withStyles(theme => ({
    root: {
        padding: theme.spacing(5),
        background: 'transparent',
        position: 'relative',
        '&::before': {
            content: '""',
            position: 'absolute',
            top: 0,
            left: '5%',
            right: '5%',
            height: 1,
            background: 'linear-gradient(90deg, transparent, rgba(148, 163, 184, 0.3), transparent)',
        }
    }
}))(DialogContent);

// Modern footer with glass effect
const DialogFooter = withStyles(theme => ({
    root: {
        padding: theme.spacing(4, 5),
        background: `
            linear-gradient(135deg, 
                rgba(248, 250, 252, 0.8) 0%, 
                rgba(241, 245, 249, 0.9) 100%
            )
        `,
        backdropFilter: 'blur(10px)',
        borderTop: '1px solid rgba(226, 232, 240, 0.4)',
        justifyContent: 'flex-end',
        gap: theme.spacing(2),
        position: 'relative',
        '&::before': {
            content: '""',
            position: 'absolute',
            top: 0,
            left: '10%',
            right: '10%',
            height: 1,
            background: 'linear-gradient(90deg, transparent, rgba(255,255,255,0.8), transparent)',
        }
    }
}))(DialogActions);

// Enhanced close button with modern styling
const ModernCloseButton = withStyles(theme => ({
    root: {
        color: 'white',
        backgroundColor: 'rgba(255, 255, 255, 0.15)',
        backdropFilter: 'blur(8px)',
        border: '1px solid rgba(255, 255, 255, 0.2)',
        width: 44,
        height: 44,
        transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
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
        '&:hover': {
            backgroundColor: 'rgba(255, 255, 255, 0.25)',
            transform: 'rotate(90deg) scale(1.1)',
            boxShadow: '0 8px 25px rgba(0, 0, 0, 0.2)',
            '&::before': {
                left: '100%',
            }
        },
        '&:active': {
            transform: 'rotate(90deg) scale(0.95)',
        }
    }
}))(IconButton);

// Primary action button with enhanced styling
const ModernPrimaryButton = withStyles(theme => ({
    root: {
        borderRadius: 16,
        padding: theme.spacing(1.8, 5),
        fontWeight: 700,
        textTransform: 'none',
        fontSize: '1rem',
        letterSpacing: '0.5px',
        minWidth: 140,
        height: 52,
        position: 'relative',
        overflow: 'hidden',
        background: `
            linear-gradient(135deg, 
                #667eea 0%, 
                #764ba2 50%,
                #667eea 100%
            )
        `,
        backgroundSize: '200% 200%',
        color: 'white',
        border: '1px solid rgba(255, 255, 255, 0.2)',
        boxShadow: '0 8px 25px rgba(102, 126, 234, 0.4)',
        transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
        '&::before': {
            content: '""',
            position: 'absolute',
            top: 0,
            left: '-100%',
            width: '100%',
            height: '100%',
            background: 'linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent)',
            transition: 'left 0.8s ease',
        },
        '&:hover': {
            backgroundPosition: '100% 50%',
            transform: 'translateY(-3px) scale(1.02)',
            boxShadow: '0 12px 35px rgba(102, 126, 234, 0.5)',
            '&::before': {
                left: '100%',
            }
        },
        '&:active': {
            transform: 'translateY(-1px) scale(0.98)',
        },
        '&:disabled': {
            background: theme.palette.grey[300],
            color: theme.palette.grey[500],
            transform: 'none',
            boxShadow: 'none',
        }
    }
}))(Button);

// Secondary button with glass-morphism
const ModernSecondaryButton = withStyles(theme => ({
    root: {
        borderRadius: 16,
        padding: theme.spacing(1.8, 5),
        fontWeight: 600,
        textTransform: 'none',
        fontSize: '1rem',
        letterSpacing: '0.5px',
        minWidth: 140,
        height: 52,
        backgroundColor: 'rgba(255, 255, 255, 0.7)',
        backdropFilter: 'blur(10px)',
        color: theme.palette.text.primary,
        border: '1px solid rgba(226, 232, 240, 0.8)',
        boxShadow: '0 4px 15px rgba(0, 0, 0, 0.08)',
        transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
        position: 'relative',
        overflow: 'hidden',
        '&::before': {
            content: '""',
            position: 'absolute',
            top: 0,
            left: '-100%',
            width: '100%',
            height: '100%',
            background: 'linear-gradient(90deg, transparent, rgba(148, 163, 184, 0.1), transparent)',
            transition: 'left 0.8s ease',
        },
        '&:hover': {
            backgroundColor: 'rgba(255, 255, 255, 0.9)',
            borderColor: 'rgba(148, 163, 184, 0.4)',
            transform: 'translateY(-3px) scale(1.02)',
            boxShadow: '0 8px 25px rgba(0, 0, 0, 0.12)',
            '&::before': {
                left: '100%',
            }
        },
        '&:active': {
            transform: 'translateY(-1px) scale(0.98)',
        }
    }
}))(Button);

// Loading button variant
const LoadingButton = withStyles(theme => ({
    root: {
        position: 'relative',
        '& .MuiCircularProgress-root': {
            position: 'absolute',
            left: '50%',
            top: '50%',
            marginLeft: -12,
            marginTop: -12,
        }
    }
}))(ModernPrimaryButton);

// Dialog type icons
const getDialogIcon = (type) => {
    const iconStyle = {
        width: 28,
        height: 28,
    };
    
    switch (type) {
        case 'success':
            return <CheckIcon style={iconStyle} />;
        case 'warning':
            return <WarningIcon style={iconStyle} />;
        case 'error':
            return <ErrorIcon style={iconStyle} />;
        case 'info':
        default:
            return <InfoIcon style={iconStyle} />;
    }
};

// Main component
const ModernStyledDialog = ({
    open,
    onClose,
    title,
    children,
    primaryAction,
    secondaryAction,
    maxWidth = "md",
    type = null,
    loading = false,
    showDivider = false,
    headerIcon = null,
    fullScreen = false,
    ...props
}) => {
    const [isVisible, setIsVisible] = useState(false);

    useEffect(() => {
        if (open) {
            setIsVisible(true);
        } else {
            const timer = setTimeout(() => setIsVisible(false), 300);
            return () => clearTimeout(timer);
        }
    }, [open]);

    return (
        <DialogContainer
            open={open}
            fullWidth
            fullScreen={fullScreen}
            maxWidth={maxWidth}
            onClose={onClose}
            aria-labelledby="modern-dialog-title"
            BackdropComponent={ModernBackdrop}
            TransitionComponent={Zoom}
            TransitionProps={{ 
                timeout: { enter: 400, exit: 300 },
                easing: 'cubic-bezier(0.34, 1.56, 0.64, 1)'
            }}
            {...props}
        >
            {/* Enhanced Header */}
            <DialogHeader disableTypography id="modern-dialog-title">
                <Box display="flex" alignItems="center" gap={2}>
                    {(headerIcon || type) && (
                        <Zoom in={isVisible} timeout={600}>
                            <Avatar
                                style={{
                                    backgroundColor: 'rgba(255, 255, 255, 0.2)',
                                    backdropFilter: 'blur(8px)',
                                    border: '1px solid rgba(255, 255, 255, 0.3)',
                                    width: 48,
                                    height: 48,
                                }}
                            >
                                {headerIcon || getDialogIcon(type)}
                            </Avatar>
                        </Zoom>
                    )}
                    <Box>
                        <Fade in={isVisible} timeout={800}>
                            <Typography 
                                variant="h5" 
                                style={{ 
                                    fontWeight: 700, 
                                    letterSpacing: '0.5px',
                                    lineHeight: 1.2,
                                    textShadow: '0 2px 4px rgba(0, 0, 0, 0.1)'
                                }}
                            >
                                {title}
                            </Typography>
                        </Fade>
                    </Box>
                </Box>
                
                <Fade in={isVisible} timeout={1000}>
                    <ModernCloseButton onClick={onClose} aria-label="close">
                        <CloseIcon />
                    </ModernCloseButton>
                </Fade>
            </DialogHeader>

            {/* Optional divider */}
            {showDivider && (
                <Fade in={isVisible} timeout={1200}>
                    <Divider style={{ 
                        background: 'linear-gradient(90deg, transparent, rgba(148, 163, 184, 0.3), transparent)',
                        height: 1,
                        margin: 0
                    }} />
                </Fade>
            )}

            {/* Content Body */}
            <DialogBody>
                <Slide direction="up" in={isVisible} timeout={600}>
                    <div>
                        {children}
                    </div>
                </Slide>
            </DialogBody>

            {/* Footer with Actions */}
            {(primaryAction || secondaryAction) && (
                <DialogFooter>
                    <Slide direction="up" in={isVisible} timeout={800}>
                        <Box display="flex" gap={2} alignItems="center">
                            {secondaryAction && (
                                <ModernSecondaryButton 
                                    onClick={secondaryAction.onClick}
                                    disabled={loading}
                                    startIcon={secondaryAction.icon}
                                >
                                    {secondaryAction.label}
                                </ModernSecondaryButton>
                            )}
                            
                            {primaryAction && (
                                loading ? (
                                    <LoadingButton disabled>
                                        <CircularProgress size={24} color="inherit" />
                                        {primaryAction.loadingLabel || 'در حال پردازش...'}
                                    </LoadingButton>
                                ) : (
                                    <ModernPrimaryButton 
                                        onClick={primaryAction.onClick}
                                        startIcon={primaryAction.icon}
                                    >
                                        {primaryAction.label}
                                    </ModernPrimaryButton>
                                )
                            )}
                        </Box>
                    </Slide>
                </DialogFooter>
            )}
        </DialogContainer>
    );
};

// Export with additional utility components
export default ModernStyledDialog;

// Additional export for specific dialog types
export const ConfirmDialog = (props) => (
    <ModernStyledDialog type="warning" {...props} />
);

export const InfoDialog = (props) => (
    <ModernStyledDialog type="info" {...props} />
);

export const ErrorDialog = (props) => (
    <ModernStyledDialog type="error" {...props} />
);

export const SuccessDialog = (props) => (
    <ModernStyledDialog type="success" {...props} />
);