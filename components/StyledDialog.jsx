import React from 'react'
import {
  Dialog,
  DialogTitle,
  DialogContent,
  IconButton,
  Typography,
  withStyles
} from '@material-ui/core'
import {Close as CloseIcon} from '@material-ui/icons'

/*
 * A glass-morphic, modern looking dialog that supports a header, body
 * and optional primary/secondary actions. Inspired by neumorphism / glass
 * design trends while still remaining accessible. – 2025 redesign ✨
 */

/* ------------------------------------------------------------------
 * Paper – blurry glass container with a subtle multi-stop gradient
 * ------------------------------------------------------------------ */
const GlassDialog = withStyles(() => ({
  /* The class name for Dialog is `MuiDialog-container`, but we need the
   * paper element which receives the `paper` className.  When creating
   * an HOC with withStyles(Dialog) we can simply override the `paper` key. */
  paper: {
    borderRadius: 24,
    padding: 0,
    /* Frosted-glass / glass-morphism background */
    background: 'rgba(255,255,255,0.75)',
    backdropFilter: 'blur(14px) saturate(180%)',
    WebkitBackdropFilter: 'blur(14px) saturate(180%)',
    /* Fancy multi-direction shadow to create depth */
    boxShadow: `
      0 10px 20px rgba(0,0,0,0.08),
      0 6px 8px rgba(0,0,0,0.04),
      inset 0 1px 0 rgba(255,255,255,0.60)`,
    border: '1px solid rgba(255,255,255,0.25)',
    overflow: 'hidden'
  }
}))(Dialog)

/* Header – gradient bar, sticky to the top, houses the title/close btn */
const Header = withStyles(theme => ({
  root: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: theme.spacing(3,4),
    background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    color: '#fff',
    position: 'relative',
    boxShadow: '0 4px 16px rgba(0,0,0,0.10)',
    /* Shine separator at the bottom */
    '&::after': {
      content: '""',
      position: 'absolute',
      bottom: 0,
      left: 0,
      right: 0,
      height: 2,
      background: 'linear-gradient(90deg, rgba(255,255,255,0.6) 0%, rgba(255,255,255,0) 100%)'
    }
  }
}))(DialogTitle)

/* Body */
const Body = withStyles(theme => ({
  root: {
    padding: theme.spacing(4),
    background: 'rgba(249, 250, 252, 0.85)'
  }
}))(DialogContent)

const CloseBtn = withStyles(() => ({
  root: {
    color: '#fff',
    transition: 'all 0.3s ease',
    backgroundColor: 'rgba(255,255,255,0.15)',
    '&:hover': {
      backgroundColor: 'rgba(255,255,255,0.25)',
      transform: 'rotate(90deg)'
    }
  }
}))(IconButton)

/* ------------------------------------------------------------------
 * Main Component
 * ------------------------------------------------------------------ */
const StyledDialog = ({
  open,
  onClose,
  title,
  children,
  maxWidth = 'md'
}) => {
  return (
    <GlassDialog
      open={open}
      onClose={onClose}
      fullWidth
      maxWidth={maxWidth}
      aria-labelledby="styled-dialog-title"
      TransitionProps={{ timeout: { enter: 400, exit: 250 } }}
    >
      <Header disableTypography>
        {/* Title */}
        {typeof title === 'string' ? (
          <Typography variant="h5" style={{ fontWeight: 700, letterSpacing: 0.5 }}>
            {title}
          </Typography>
        ) : (
          title
        )}

        {/* Close icon */}
        <CloseBtn onClick={onClose} aria-label="close-dialog">
          <CloseIcon />
        </CloseBtn>
      </Header>

      <Body>{children}</Body>
    </GlassDialog>
  )
}

export default StyledDialog