import React from 'react';
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
    withStyles
} from '@material-ui/core';
import { Close as CloseIcon } from '@material-ui/icons';

// Modern styled components with contemporary design
const DialogPaper = withStyles(theme => ({
    root: {
        '& .MuiPaper-root': {
            borderRadius: 32,
            padding: 0,
            background: 'linear-gradient(145deg, rgba(255, 255, 255, 0.95) 0%, rgba(248, 250, 252, 0.9) 100%)',
            backdropFilter: 'blur(20px) saturate(180%)',
            WebkitBackdropFilter: 'blur(20px) saturate(180%)',
            boxShadow: `
                0 32px 64px rgba(31, 38, 135, 0.15),
                0 8px 32px rgba(31, 38, 135, 0.1),
                inset 0 1px 0 rgba(255, 255, 255, 0.8),
                inset 0 -1px 0 rgba(255, 255, 255, 0.2)
            `,
            border: '1px solid rgba(255, 255, 255, 0.4)',
            overflow: 'hidden',
            position: 'relative',
            '&::before': {
                content: '""',
                position: 'absolute',
                top: 0,
                left: 0,
                right: 0,
                bottom: 0,
                background: 'radial-gradient(circle at 50% 0%, rgba(124, 58, 237, 0.05) 0%, transparent 50%)',
                pointerEvents: 'none',
            }
        }
    }
}))(Dialog);

const DialogHeader = withStyles(theme => ({
    root: {
        padding: theme.spacing(4, 5),
        background: `
            linear-gradient(135deg, 
                rgba(99, 102, 241, 0.9) 0%, 
                rgba(139, 92, 246, 0.9) 50%,
                rgba(167, 139, 250, 0.9) 100%
            )
        `,
        color: 'white',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        position: 'relative',
        overflow: 'hidden',
        '&::before': {
            content: '""',
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: 'linear-gradient(90deg, rgba(255,255,255,0.1) 0%, transparent 50%, rgba(255,255,255,0.1) 100%)',
            animation: '$shimmer 3s ease-in-out infinite',
        },
        '&::after': {
            content: '""',
            position: 'absolute',
            bottom: 0,
            left: 0,
            right: 0,
            height: 1,
            background: 'linear-gradient(90deg, transparent 0%, rgba(255,255,255,0.6) 50%, transparent 100%)',
        }
    },
    '@keyframes shimmer': {
        '0%': { transform: 'translateX(-100%)' },
        '50%': { transform: 'translateX(100%)' },
        '100%': { transform: 'translateX(100%)' }
    }
}))(DialogTitle);

const DialogBody = withStyles(theme => ({
    root: {
        padding: theme.spacing(5),
        background: 'rgba(255, 255, 255, 0.6)',
        backdropFilter: 'blur(10px)',
        WebkitBackdropFilter: 'blur(10px)',
        minHeight: 120,
        position: 'relative',
        '&::before': {
            content: '""',
            position: 'absolute',
            top: 0,
            left: theme.spacing(2),
            right: theme.spacing(2),
            height: 1,
            background: 'linear-gradient(90deg, transparent 0%, rgba(99, 102, 241, 0.2) 50%, transparent 100%)',
        }
    }
}))(DialogContent);

const DialogFooter = withStyles(theme => ({
    root: {
        padding: theme.spacing(4, 5),
        background: 'rgba(248, 250, 252, 0.8)',
        backdropFilter: 'blur(10px)',
        WebkitBackdropFilter: 'blur(10px)',
        borderTop: '1px solid rgba(99, 102, 241, 0.1)',
        justifyContent: 'flex-end',
        gap: theme.spacing(2),
        position: 'relative',
        '&::before': {
            content: '""',
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            height: 1,
            background: 'linear-gradient(90deg, transparent 0%, rgba(255,255,255,0.8) 50%, transparent 100%)',
        }
    }
}))(DialogActions);

const CloseBtn = withStyles(theme => ({
    root: {
        color: 'white',
        backgroundColor: 'rgba(255, 255, 255, 0.15)',
        backdropFilter: 'blur(10px)',
        WebkitBackdropFilter: 'blur(10px)',
        border: '1px solid rgba(255, 255, 255, 0.2)',
        borderRadius: 12,
        width: 40,
        height: 40,
        transition: 'all 0.4s cubic-bezier(0.4, 0, 0.2, 1)',
        '&:hover': {
            backgroundColor: 'rgba(255, 255, 255, 0.25)',
            transform: 'rotate(90deg) scale(1.05)',
            boxShadow: '0 8px 25px rgba(0, 0, 0, 0.15)',
        },
        '&:active': {
            transform: 'rotate(90deg) scale(0.95)',
        }
    }
}))(IconButton);

const PrimaryBtn = withStyles(theme => ({
    root: {
        borderRadius: 16,
        padding: theme.spacing(2, 5),
        fontWeight: 600,
        textTransform: 'none',
        fontSize: '1rem',
        letterSpacing: 0.3,
        minWidth: 120,
        position: 'relative',
        overflow: 'hidden',
        background: `
            linear-gradient(135deg, 
                #6366f1 0%, 
                #8b5cf6 50%, 
                #a78bfa 100%
            )
        `,
        color: 'white',
        border: 'none',
        boxShadow: '0 4px 15px rgba(99, 102, 241, 0.4)',
        transition: 'all 0.4s cubic-bezier(0.4, 0, 0.2, 1)',
        '&::before': {
            content: '""',
            position: 'absolute',
            top: 0,
            left: '-100%',
            width: '100%',
            height: '100%',
            background: 'linear-gradient(90deg, transparent, rgba(255,255,255,0.4), transparent)',
            transition: 'left 0.6s',
        },
        '&:hover': {
            background: `
                linear-gradient(135deg, 
                    #5855eb 0%, 
                    #7c3aed 50%, 
                    #9333ea 100%
                )
            `,
            transform: 'translateY(-2px)',
            boxShadow: '0 8px 25px rgba(99, 102, 241, 0.5)',
            '&::before': {
                left: '100%',
            }
        },
        '&:active': {
            transform: 'translateY(0px)',
            boxShadow: '0 4px 15px rgba(99, 102, 241, 0.4)',
        }
    }
}))(Button);

const SecondaryBtn = withStyles(theme => ({
    root: {
        borderRadius: 16,
        padding: theme.spacing(2, 5),
        fontWeight: 600,
        textTransform: 'none',
        fontSize: '1rem',
        letterSpacing: 0.3,
        minWidth: 120,
        backgroundColor: 'rgba(255, 255, 255, 0.8)',
        backdropFilter: 'blur(10px)',
        WebkitBackdropFilter: 'blur(10px)',
        color: '#4b5563',
        border: '1px solid rgba(99, 102, 241, 0.2)',
        boxShadow: '0 4px 15px rgba(0, 0, 0, 0.05)',
        transition: 'all 0.4s cubic-bezier(0.4, 0, 0.2, 1)',
        position: 'relative',
        overflow: 'hidden',
        '&::before': {
            content: '""',
            position: 'absolute',
            top: 0,
            left: '-100%',
            width: '100%',
            height: '100%',
            background: 'linear-gradient(90deg, transparent, rgba(99, 102, 241, 0.1), transparent)',
            transition: 'left 0.6s',
        },
        '&:hover': {
            backgroundColor: 'rgba(255, 255, 255, 0.95)',
            transform: 'translateY(-2px)',
            boxShadow: '0 8px 25px rgba(0, 0, 0, 0.1)',
            borderColor: 'rgba(99, 102, 241, 0.4)',
            color: '#374151',
            '&::before': {
                left: '100%',
            }
        },
        '&:active': {
            transform: 'translateY(0px)',
            boxShadow: '0 4px 15px rgba(0, 0, 0, 0.05)',
        }
    }
}))(Button);

const StyledDialog = ({
                          open,
                          onClose,
                          title,
                          children,
                          primaryAction,
                          secondaryAction,
                          maxWidth = "md"
                      }) => {
    return (
        <DialogPaper
            open={open}
            fullWidth
            maxWidth={maxWidth}
            onClose={onClose}
            aria-labelledby="modern-dialog-title"
            TransitionProps={{ 
                timeout: { enter: 400, exit: 300 },
                style: {
                    transition: 'all 0.4s cubic-bezier(0.4, 0, 0.2, 1)'
                }
            }}
            PaperProps={{
                style: {
                    animation: open ? 'slideInUp 0.4s cubic-bezier(0.4, 0, 0.2, 1)' : undefined
                }
            }}
        >
            <style jsx global>{`
                @keyframes slideInUp {
                    from {
                        transform: translateY(30px) scale(0.95);
                        opacity: 0;
                    }
                    to {
                        transform: translateY(0) scale(1);
                        opacity: 1;
                    }
                }
                
                @keyframes fadeInBackdrop {
                    from { backdrop-filter: blur(0px); }
                    to { backdrop-filter: blur(8px); }
                }
            `}</style>
            
            <DialogHeader disableTypography>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                    <Box 
                        sx={{
                            width: 6,
                            height: 6,
                            borderRadius: '50%',
                            backgroundColor: 'rgba(255, 255, 255, 0.8)',
                            animation: 'pulse 2s infinite'
                        }}
                    />
                    <Typography 
                        variant="h5" 
                        style={{ 
                            fontWeight: 700, 
                            letterSpacing: 0.3,
                            fontSize: '1.5rem',
                            textShadow: '0 2px 4px rgba(0,0,0,0.1)'
                        }}
                    >
                        {title}
                    </Typography>
                </Box>
                <CloseBtn onClick={onClose}>
                    <CloseIcon style={{ fontSize: 20 }} />
                </CloseBtn>
            </DialogHeader>

            <DialogBody>
                <Box sx={{ 
                    '& > *': { 
                        animation: 'fadeInContent 0.6s ease-out 0.2s both' 
                    } 
                }}>
                    {children}
                </Box>
            </DialogBody>

            {(primaryAction || secondaryAction) && (
                <DialogFooter>
                    {secondaryAction && (
                        <SecondaryBtn onClick={secondaryAction.onClick}>
                            {secondaryAction.label}
                        </SecondaryBtn>
                    )}
                    {primaryAction && (
                        <PrimaryBtn onClick={primaryAction.onClick}>
                            {primaryAction.label}
                        </PrimaryBtn>
                    )}
                </DialogFooter>
            )}
        </DialogPaper>
    );
};

export default StyledDialog;