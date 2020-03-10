package methods;

public enum Activation {
    LOGISTIC {
        @Override
        public double calc(final double x, final boolean derivative) {
            final double fx = 1 / (1 + Math.exp(-x));
            if (derivative) {
                return fx * 1 - fx * fx;
            } else {
                return fx;
            }
        }
    }, TANH {
        @Override
        public double calc(final double x, final boolean derivative) {
            if (derivative) {
                return 1 - Math.pow(Math.tanh(x), 2);
            } else {
                return Math.tanh(x);
            }
        }
    }, IDENTITY {
        @Override
        public double calc(final double x, final boolean derivative) {
            if (derivative) {
                return 1;
            } else {
                return x;
            }
        }
    }, STEP {
        @Override
        public double calc(final double x, final boolean derivative) {
            if (derivative) {
                return 0;
            } else {
                return x > 0 ? 1 : 0;
            }
        }
    }, RELU {
        @Override
        public double calc(final double x, final boolean derivative) {
            if (derivative) {
                return x > 0 ? 1 : 0;
            } else {
                return x > 0 ? x : 0;
            }
        }
    }, SOFTSIGN {
        @Override
        public double calc(final double x, final boolean derivative) {
            final double d = 1 + Math.abs(x);
            if (derivative) {
                return x / Math.pow(d, 2);
            } else {
                return x / d;
            }
        }
    }, SINUSOID {
        @Override
        public double calc(final double x, final boolean derivative) {
            if (derivative) {
                return Math.cos(x);
            } else {
                return Math.sin(x);
            }
        }
    }, GAUSSIAN {
        @Override
        public double calc(final double x, final boolean derivative) {
            final double d = Math.exp(-Math.pow(x, 2));
            if (derivative) {
                return -2 * x * d;
            } else {
                return d;
            }
        }
    }, BENT_IDENTITY {
        @Override
        public double calc(final double x, final boolean derivative) {
            final double d = Math.sqrt(Math.pow(x, 2) + 1);
            if (derivative) {
                return x / (2 * d) + 1;
            } else {
                return (d - 1) / 2 + x;
            }
        }
    }, BIPOLAR {
        @Override
        public double calc(final double x, final boolean derivative) {
            if (derivative) {
                return 0;
            } else {
                return x > 0 ? 1 : -1;
            }
        }
    }, BIPOLAR_SIGMOID {
        @Override
        public double calc(final double x, final boolean derivative) {
            final double d = 2 / (1 + Math.exp(-x)) - 1;
            if (derivative) {
                return 0.5 - 0.5 * d * d;
            } else {
                return d;
            }
        }
    }, HARD_TANH {
        @Override
        public double calc(final double x, final boolean derivative) {
            if (derivative) {
                return Math.abs(x) < 1 ? 1 : 0;
            } else {
                return Math.max(-1, Math.min(1, x));
            }
        }
    }, ABSOLUTE {
        @Override
        public double calc(final double x, final boolean derivative) {
            if (derivative) {
                return x < 0 ? -1 : 1;
            } else {
                return Math.abs(x);
            }
        }
    }, INVERSE {
        @Override
        public double calc(final double x, final boolean derivative) {
            if (derivative) {
                return -1;
            } else {
                return 1 - x;
            }
        }
    };

    public double calc(final double x) {
        return this.calc(x, false);
    }

    public abstract double calc(double x, boolean derivative);
}
