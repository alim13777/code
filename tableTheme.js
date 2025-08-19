import { createTheme } from '@mui/material/styles';
import { alpha } from '@mui/material/styles';

// Modern Table Theme Configuration
export const createTableTheme = (baseTheme) => createTheme({
  ...baseTheme,
  components: {
    ...baseTheme?.components,
    
    // Table Container
    MuiTableContainer: {
      styleOverrides: {
        root: {
          borderRadius: 16,
          boxShadow: '0 8px 32px rgba(0, 0, 0, 0.08)',
          border: `1px solid ${alpha('#000', 0.08)}`,
          overflow: 'hidden',
          background: '#ffffff',
          '&:hover': {
            boxShadow: '0 12px 40px rgba(0, 0, 0, 0.12)',
          },
        },
      },
    },

    // Table
    MuiTable: {
      styleOverrides: {
        root: {
          '& .MuiTableCell-root': {
            borderBottom: `1px solid ${alpha('#000', 0.06)}`,
          },
        },
      },
    },

    // Table Head
    MuiTableHead: {
      styleOverrides: {
        root: {
          background: 'linear-gradient(135deg, #1976d2 0%, #1565c0 100%)',
          '& .MuiTableCell-head': {
            color: '#ffffff',
            fontWeight: 600,
            fontSize: '0.875rem',
            letterSpacing: '0.5px',
            textTransform: 'uppercase',
            borderBottom: 'none',
            padding: '16px',
            '& .MuiTableSortLabel-root': {
              color: 'inherit',
              '&:hover': {
                color: 'inherit',
              },
              '&.Mui-active': {
                color: 'inherit',
                '& .MuiTableSortLabel-icon': {
                  color: 'inherit',
                },
              },
            },
          },
        },
      },
    },

    // Table Body
    MuiTableBody: {
      styleOverrides: {
        root: {
          '& .MuiTableRow-root': {
            transition: 'all 0.2s cubic-bezier(0.4, 0, 0.2, 1)',
            '&:nth-of-type(even)': {
              backgroundColor: alpha('#f5f5f5', 0.3),
            },
            '&:hover': {
              backgroundColor: alpha('#1976d2', 0.04),
              transform: 'translateY(-1px)',
              boxShadow: '0 4px 12px rgba(0, 0, 0, 0.1)',
              cursor: 'pointer',
            },
            '&.Mui-selected': {
              backgroundColor: alpha('#1976d2', 0.12),
              '&:hover': {
                backgroundColor: alpha('#1976d2', 0.16),
              },
            },
          },
        },
      },
    },

    // Table Cell
    MuiTableCell: {
      styleOverrides: {
        root: {
          padding: '12px 16px',
          '&.MuiTableCell-head': {
            padding: '16px',
          },
        },
      },
    },

    // Pagination
    MuiTablePagination: {
      styleOverrides: {
        root: {
          borderTop: `1px solid ${alpha('#000', 0.08)}`,
          backgroundColor: alpha('#f8f9fa', 0.8),
        },
        toolbar: {
          paddingLeft: 16,
          paddingRight: 16,
        },
        selectLabel: {
          fontWeight: 500,
        },
        displayedRows: {
          fontWeight: 500,
        },
      },
    },

    // Buttons
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 12,
          textTransform: 'none',
          fontWeight: 600,
          padding: '8px 24px',
          transition: 'all 0.2s ease-in-out',
          '&:hover': {
            transform: 'translateY(-1px)',
            boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)',
          },
        },
        contained: {
          boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
          '&:hover': {
            boxShadow: '0 4px 16px rgba(0, 0, 0, 0.2)',
          },
        },
        outlined: {
          borderWidth: 2,
          '&:hover': {
            borderWidth: 2,
          },
        },
      },
    },

    // Icon Buttons
    MuiIconButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          transition: 'all 0.2s ease-in-out',
          '&:hover': {
            transform: 'scale(1.1)',
          },
        },
      },
    },

    // Chips
    MuiChip: {
      styleOverrides: {
        root: {
          borderRadius: 12,
          fontWeight: 500,
          boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
          transition: 'all 0.2s ease-in-out',
          '&:hover': {
            transform: 'translateY(-1px)',
            boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)',
          },
        },
      },
    },

    // Text Fields
    MuiTextField: {
      styleOverrides: {
        root: {
          '& .MuiOutlinedInput-root': {
            borderRadius: 12,
            transition: 'all 0.3s ease-in-out',
            '&:hover': {
              boxShadow: '0 4px 12px rgba(0, 0, 0, 0.1)',
            },
            '&.Mui-focused': {
              boxShadow: '0 4px 16px rgba(0, 0, 0, 0.15)',
            },
          },
        },
      },
    },

    // Cards
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 16,
          boxShadow: '0 4px 20px rgba(0, 0, 0, 0.08)',
          transition: 'all 0.3s ease-in-out',
          '&:hover': {
            boxShadow: '0 8px 32px rgba(0, 0, 0, 0.12)',
          },
        },
      },
    },

    // Dialog
    MuiDialog: {
      styleOverrides: {
        paper: {
          borderRadius: 16,
          boxShadow: '0 16px 48px rgba(0, 0, 0, 0.2)',
        },
      },
    },

    // Collapse
    MuiCollapse: {
      styleOverrides: {
        root: {
          '&.border-top': {
            borderTop: `1px solid ${alpha('#000', 0.08)}`,
          },
        },
      },
    },

    // Switch
    MuiSwitch: {
      styleOverrides: {
        root: {
          '& .MuiSwitch-track': {
            borderRadius: 12,
          },
          '& .MuiSwitch-thumb': {
            boxShadow: '0 2px 8px rgba(0, 0, 0, 0.2)',
          },
        },
      },
    },

    // Checkbox
    MuiCheckbox: {
      styleOverrides: {
        root: {
          borderRadius: 4,
          '&:hover': {
            backgroundColor: alpha('#1976d2', 0.04),
          },
        },
      },
    },

    // Tooltip
    MuiTooltip: {
      styleOverrides: {
        tooltip: {
          backgroundColor: alpha('#000', 0.9),
          borderRadius: 8,
          fontSize: '0.75rem',
          fontWeight: 500,
        },
        arrow: {
          color: alpha('#000', 0.9),
        },
      },
    },
  },

  // Custom palette extensions
  palette: {
    ...baseTheme?.palette,
    background: {
      ...baseTheme?.palette?.background,
      default: '#f8f9fa',
      paper: '#ffffff',
    },
    divider: alpha('#000', 0.08),
  },

  // Typography enhancements
  typography: {
    ...baseTheme?.typography,
    h6: {
      ...baseTheme?.typography?.h6,
      fontWeight: 600,
    },
    subtitle2: {
      ...baseTheme?.typography?.subtitle2,
      fontWeight: 600,
    },
    body2: {
      ...baseTheme?.typography?.body2,
      lineHeight: 1.5,
    },
  },

  // Custom breakpoints for responsive design
  breakpoints: {
    ...baseTheme?.breakpoints,
    values: {
      xs: 0,
      sm: 600,
      md: 900,
      lg: 1200,
      xl: 1536,
    },
  },

  // Custom spacing
  spacing: 8,

  // Custom shadows
  shadows: [
    'none',
    '0 1px 3px rgba(0, 0, 0, 0.12), 0 1px 2px rgba(0, 0, 0, 0.24)',
    '0 3px 6px rgba(0, 0, 0, 0.16), 0 3px 6px rgba(0, 0, 0, 0.23)',
    '0 10px 20px rgba(0, 0, 0, 0.19), 0 6px 6px rgba(0, 0, 0, 0.23)',
    '0 14px 28px rgba(0, 0, 0, 0.25), 0 10px 10px rgba(0, 0, 0, 0.22)',
    '0 19px 38px rgba(0, 0, 0, 0.30), 0 15px 12px rgba(0, 0, 0, 0.22)',
    ...Array(19).fill('0 19px 38px rgba(0, 0, 0, 0.30), 0 15px 12px rgba(0, 0, 0, 0.22)'),
  ],
});

// Color scheme presets
export const colorSchemes = {
  default: {
    primary: '#1976d2',
    secondary: '#dc004e',
  },
  success: {
    primary: '#2e7d32',
    secondary: '#ed6c02',
  },
  warning: {
    primary: '#ed6c02',
    secondary: '#9c27b0',
  },
  error: {
    primary: '#d32f2f',
    secondary: '#1976d2',
  },
  dark: {
    primary: '#90caf9',
    secondary: '#f48fb1',
  },
};

// Animation presets
export const animations = {
  fadeIn: {
    animation: 'fadeIn 0.3s ease-in-out',
    '@keyframes fadeIn': {
      from: { opacity: 0, transform: 'translateY(10px)' },
      to: { opacity: 1, transform: 'translateY(0)' },
    },
  },
  slideIn: {
    animation: 'slideIn 0.3s ease-in-out',
    '@keyframes slideIn': {
      from: { transform: 'translateX(-100%)' },
      to: { transform: 'translateX(0)' },
    },
  },
  zoomIn: {
    animation: 'zoomIn 0.2s ease-in-out',
    '@keyframes zoomIn': {
      from: { transform: 'scale(0.8)', opacity: 0 },
      to: { transform: 'scale(1)', opacity: 1 },
    },
  },
};

export default createTableTheme;