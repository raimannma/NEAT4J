package methods;

public enum Mutation {
    ADD_NODE(false),
    SUB_NODE(false),
    ADD_CONN(false),
    SUB_CONN(false),
    MOD_WEIGHT(false),
    MOD_BIAS(false),
    MOD_ACTIVATION(Activation.values()),
    ADD_SELF_CONN(false),
    SUB_SELF_CONN(false),
    ADD_GATE(false),
    SUB_GATE(false),
    ADD_BACK_CONN(false),
    SUB_BACK_CONN(false),
    SWAP_NODES(true);

    public static final Mutation[] ALL = Mutation.values();
    public static final Mutation[] FFW = new Mutation[]{
            ADD_NODE,
            SUB_NODE,
            ADD_CONN,
            SUB_CONN,
            MOD_WEIGHT,
            MOD_BIAS,
            MOD_ACTIVATION,
            SWAP_NODES
    };
    public final boolean mutateOutput;
    public Activation[] allowed;
    public int min;
    public int max;
    public boolean keepGates;

    Mutation(final boolean mutateOutput) {
        this.min = -1;
        this.max = 1;
        this.mutateOutput = mutateOutput;
        this.keepGates = !mutateOutput;
    }

    Mutation(final Activation[] allowed) {
        this.mutateOutput = true;
        this.allowed = allowed;
    }
}
