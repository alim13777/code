import React from 'react';
import { withStyles } from '@material-ui/core/styles';
import MuiTextField from '@material-ui/core/TextField';
import Autocomplete from '@material-ui/lab/Autocomplete';

// Custom TextField (MUI v4) styles
const customTextFieldStyles = (theme) => ({
  '& label.Mui-focused': {
    color: '#667eea',
  },
  '& .MuiOutlinedInput-root': {
    borderRadius: '12px',
    transition: 'all 0.3s ease',
    '& .MuiOutlinedInput-notchedOutline': {
      borderColor: 'rgba(0,0,0,0.12)',
      borderWidth: '1px',
    },
    '&:hover .MuiOutlinedInput-notchedOutline': {
      borderColor: '#667eea',
    },
    '&.Mui-focused': {
      '& .MuiOutlinedInput-notchedOutline': {
        borderColor: '#667eea',
        borderWidth: '2px',
      },
    },
    '&.Mui-disabled': {
      backgroundColor: '#f8fafc',
      '& .MuiOutlinedInput-notchedOutline': {
        borderColor: 'rgba(0,0,0,0.08)',
      },
    },
  },
  '& .MuiInputLabel-root': {
    '&.Mui-focused': {
      color: '#667eea',
    },
  },
});

const CustomSmallTextFieldStyles = (theme) => ({
  '& label.Mui-focused': {
    color: '#667eea',
  },
  '& .MuiOutlinedInput-root': {
    borderRadius: '12px',
    fontSize: '0.875rem',
    height: '56px',
    transition: 'all 0.3s ease',
    '& .MuiOutlinedInput-notchedOutline': {
      borderColor: 'rgba(0,0,0,0.12)',
      borderWidth: '1px',
    },
    '&:hover .MuiOutlinedInput-notchedOutline': {
      borderColor: '#667eea',
    },
    '&.Mui-focused': {
      '& .MuiOutlinedInput-notchedOutline': {
        borderColor: '#667eea',
        borderWidth: '2px',
      },
    },
    '&.Mui-disabled': {
      backgroundColor: '#f8fafc',
      '& .MuiOutlinedInput-notchedOutline': {
        borderColor: 'rgba(0,0,0,0.08)',
      },
    },
    '& .MuiOutlinedInput-input': {
      padding: '12px 16px',
      height: 'auto',
    },
  },
  '& .MuiInputLabel-root': {
    fontSize: '0.875rem',
    transform: 'translate(14px, 16px) scale(1)',
    '&.Mui-focused': {
      color: '#667eea',
      transform: 'translate(14px, -9px) scale(0.75)',
    },
    '&.MuiFormLabel-filled': {
      transform: 'translate(14px, -9px) scale(0.75)',
    },
  },
});

const CustomSmallAutocompleteStyles = (theme) => ({
  '& .MuiOutlinedInput-root': {
    fontSize: '0.875rem !important',
    borderRadius: '12px !important',
    height: '56px !important',
    minHeight: '56px !important',
    maxHeight: '56px !important',
    backgroundColor: '#fafbfc !important',
    transition: 'all 0.2s ease !important',
    border: '1px solid #e1e5e9 !important',
    '& .MuiOutlinedInput-input': {
      padding: '12px 16px !important',
      height: 'auto !important',
      lineHeight: '1.4 !important',
      fontSize: '0.875rem !important',
    },
    '&:hover': {
      backgroundColor: '#f5f6f7 !important',
      borderColor: '#c7cdd4 !important',
    },
    '&.Mui-focused': {
      backgroundColor: '#fff !important',
      borderColor: '#667eea !important',
      boxShadow: '0 0 0 2px rgba(102, 126, 234, 0.1) !important',
    },
    '& .MuiOutlinedInput-notchedOutline': {
      border: 'none !important',
    },
  },
  '& .MuiAutocomplete-input': {
    padding: '0 !important',
    fontSize: '0.875rem !important',
    color: '#374151 !important',
  },
  '& .MuiAutocomplete-inputRoot': {
    padding: '0 !important',
    height: '56px !important',
    minHeight: '56px !important',
  },
  '& .MuiInputLabel-root': {
    fontSize: '0.875rem !important',
    color: '#6b7280 !important',
    transform: 'translate(14px, 16px) scale(1) !important',
    '&.Mui-focused': {
      color: '#667eea !important',
      transform: 'translate(14px, -9px) scale(0.75) !important',
    },
    '&.MuiFormLabel-filled': {
      color: '#667eea !important',
      transform: 'translate(14px, -9px) scale(0.75) !important',
    },
  },
  '& .MuiAutocomplete-popupIndicator': {
    padding: '4px !important',
    color: '#6b7280 !important',
    transition: 'color 0.2s ease !important',
    '&:hover': {
      color: '#667eea !important',
      backgroundColor: 'rgba(102, 126, 234, 0.04) !important',
    },
  },
  '& .MuiAutocomplete-clearIndicator': {
    padding: '4px !important',
    color: '#6b7280 !important',
    transition: 'color 0.2s ease !important',
    '&:hover': {
      color: '#ef4444 !important',
      backgroundColor: 'rgba(239, 68, 68, 0.04) !important',
    },
  },
});

const TextField = withStyles(customTextFieldStyles)(MuiTextField);
const SmallTextField = withStyles(CustomSmallTextFieldStyles)(MuiTextField);
const SmallAutocomplete = withStyles(CustomSmallAutocompleteStyles)(Autocomplete);

export { TextField, SmallTextField, SmallAutocomplete };